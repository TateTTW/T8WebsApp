package com.t8webs.enterprise.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.t8webs.enterprise.dao.IAssignedServerDAO;
import com.t8webs.enterprise.dao.IAvailableServerDAO;
import com.t8webs.enterprise.dto.Server;
import com.t8webs.enterprise.utils.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class ServerService implements IServerService {
    @Autowired
    IAvailableServerDAO availableServerDAO;
    @Autowired
    IAssignedServerDAO assignedServerDAO;
    @Autowired
    IProxmoxUtil proxmoxUtil;
    @Autowired
    IClientServerUtil clientServerUtil;
    @Autowired
    IReverseProxyUtil reverseProxyUtil;

    private ObjectMapper mapper = new ObjectMapper();
    /**
     * @param username    String user to assign a server to
     * @param serverName  String name to give server
     * @return
     */
    @Override
    public ObjectNode assignUserServer(String username, String serverName) throws SQLException, IOException, ClassNotFoundException, ProxmoxUtil.InvalidVmStateException, ReverseProxyUtil.ProxyConfigLockedException {
        ObjectNode node = mapper.createObjectNode();
        node.put("error", "");
        node.put("success", false);

        // Confirm that the server name can be used
        if(!serverNameConforms(serverName) || assignedServerDAO.existsBy(serverName.trim())){
            node.put("error", "The server name, " + serverName + ", is not available.");
            return node;
        }

        // Assign an available to server to the user
        Server server = clientServerUtil.assignUserServer(username, serverName);

        if(!server.isFound()){
            node.put("error", "Could not allocate an available server.");
            return node;
        }

        // Create a vm from template and retrieve dhcp assigned ip
        String dhcpIp = createProxServer(server.getVmid(), serverName);
        if(dhcpIp.isEmpty()){
            node.put("error", "Failed to create server.");
            return node;
        }

        // Set the vm's ip to a static ip defined in the server database entry
        if(!clientServerUtil.updateServerIp(dhcpIp, server.getIpAddress())) {
            node.put("error", "Failed to connect server to valid ip address.");
            return node;
        }

        // Shutdown vm to apply ip on startup
        proxmoxUtil.shutdownVM(server.getVmid());

        // Update proxy config to route traffic to the server
        if(!reverseProxyUtil.reconfigure()) {
            node.put("error", "Failed to configure server routing.");
            return node;
        }

        // Confirm server is stopped
        if(!proxmoxUtil.reachedState(ProxmoxUtil.State.STOPPED, server.getVmid())){
            node.put("error", "Failed to shutdown server.");
            return node;
        }

        node.put("vmid", server.getVmid());
        node.put("success", true);

        return node;
    }

    private boolean serverNameConforms(String serverName) {
        if (serverName == null || serverName.trim().length() < 2 || serverName.trim().length() > 20 || serverName.contains(" ")){
            return false;
        }

        Pattern pattern = Pattern.compile("[^A-Za-z0-9]");
        Matcher matcher = pattern.matcher(serverName);

        if(matcher.find()){
            return false;
        }

        return true;
    }

    // ProxmoxUtil 'retry' methods must be called from outside class
    private String createProxServer(int vmid, String vmName) {
        if(proxmoxUtil.cloneVM(vmid, vmName)){
            try {
                if (proxmoxUtil.startVM(vmid)) {
                    String ipAddress = proxmoxUtil.getServerIp(vmid);

                    if (!ipAddress.trim().isEmpty()) {
                        return ipAddress;
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        return "";
    }

    /**
     * @param username    String user assigned to server
     * @param vmid        int uniquely identifying the server
     * @param serverName  String to rename the server
     * @return
     */
    @Override
    public boolean renameServer(String username, int vmid, String serverName) throws SQLException, IOException, ClassNotFoundException, ReverseProxyUtil.ProxyConfigLockedException {
        // Confirm that the server name can be used
        if(!serverNameConforms(serverName) || assignedServerDAO.existsBy(serverName.trim())){
            return false;
        }

        // Confirm server is assigned to user
        Server server = assignedServerDAO.fetchUserServer(username, vmid);
        if(!server.isFound()){
            return false;
        }

        // Set new server name
        server.setName(serverName);

        // Update database record and then update proxy configuration
        if (assignedServerDAO.update(server) && reverseProxyUtil.reconfigure()) {
            return true;
        }

        return false;
    }

    /**
     * @param username    String uniquely identifying user
     * @return
     */
    @Override
    public ArrayNode getUserServers(String username) throws SQLException, IOException, ClassNotFoundException {
        List<Server> servers = assignedServerDAO.fetchByUsername(username);
        ArrayNode serverNodes = mapper.createArrayNode();
        for(Server server: servers){
            ObjectNode serverNode = mapper.createObjectNode();
            serverNode.put("id", String.valueOf(server.getVmid()));
            serverNode.put("name", server.getName());

            ObjectNode attributes = mapper.createObjectNode();
            attributes.put("type", 1);
            attributes.put("status", "");
            serverNode.put("hasAttributes", attributes);

            serverNodes.add(serverNode);
        }

        return serverNodes;
    }

    @Override
    public boolean deployBuild(String username, int vmid, MultipartFile buildFile) throws SQLException, IOException, ClassNotFoundException {
        Server server = assignedServerDAO.fetchUserServer(username, vmid);

        if(server.isFound()){
            if(proxmoxUtil.isVmRunning(vmid)){
                return clientServerUtil.deployBuild(server.getIpAddress(), buildFile);
            } else {
                return false;
            }
        }

        return false;
    }

    @Override
    public boolean startVM(String username, int vmid) throws SQLException, IOException, ClassNotFoundException, ProxmoxUtil.InvalidVmStateException {
        Server server = assignedServerDAO.fetchUserServer(username, vmid);

        if(server.isFound()
            && proxmoxUtil.startVM(vmid)
            && proxmoxUtil.reachedState(ProxmoxUtil.State.RUNNING, vmid)){
            return true;
        }

        return false;
    }

    @Override
    public boolean shutdownVM(String username, int vmid) throws SQLException, IOException, ClassNotFoundException, ProxmoxUtil.InvalidVmStateException {
        Server server = assignedServerDAO.fetchUserServer(username, vmid);

        if(server.isFound()
            && proxmoxUtil.shutdownVM(vmid)
            && proxmoxUtil.reachedState(ProxmoxUtil.State.STOPPED, vmid)){
            return true;
        }

        return false;
    }

    @Override
    public boolean rebootVM(String username, int vmid) throws SQLException, IOException, ClassNotFoundException, ProxmoxUtil.InvalidVmStateException {
        Server server = assignedServerDAO.fetchUserServer(username, vmid);

        if(server.isFound()
            && proxmoxUtil.rebootVM(vmid)
            && proxmoxUtil.reachedState(ProxmoxUtil.State.RUNNING, vmid))
        {
            return true;
        }

        return false;
    }

    @Override
    public boolean deleteVM(String username, int vmid) throws SQLException, IOException, ClassNotFoundException, ProxmoxUtil.InvalidVmStateException, ReverseProxyUtil.ProxyConfigLockedException {
        Server server = assignedServerDAO.fetchUserServer(username, vmid);

        if(server.isFound()
            && !proxmoxUtil.isVmRunning(vmid)
            && proxmoxUtil.deleteVM(vmid)
            && assignedServerDAO.delete(vmid)
            && availableServerDAO.save(server)
            && reverseProxyUtil.reconfigure())
        {
            return true;
        }

        return false;
    }

    @Override
    public String getVmStatus(String username, int vmid) throws SQLException, IOException, ClassNotFoundException {
        Server server = assignedServerDAO.fetchUserServer(username, vmid);

        if(server.isFound()){
            if(proxmoxUtil.isVmLocked(vmid)){
                return "locked";
            } else {
                return proxmoxUtil.getVmStatus(vmid);
            }
        }

        return "";
    }
}
