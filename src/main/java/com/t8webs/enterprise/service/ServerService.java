package com.t8webs.enterprise.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.t8webs.enterprise.dao.IServerDAO;
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
    IServerDAO serverDA0;
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
    public ObjectNode assignUserServer(String username, String serverName) throws SQLException, IOException, ClassNotFoundException {
        ObjectNode node = mapper.createObjectNode();
        node.put("error", "");
        node.put("success", false);

        // Confirm that the server name can be used
        if(!serverNameConforms(serverName) || serverDA0.existsBy(serverName.trim())){
            node.put("error", "The server name, " + serverName + ", is not available.");
            return node;
        }

        // Find an available server entry
        Server server = serverDA0.fetchAvailable();
        if(!server.isFound()){
            node.put("error", "There are no available servers.");
            return node;
        }

        // Set user info on available server
        server.setName(serverName.trim());
        server.setUsername(username);

        // Create a vm from template and retrieve dhcp assigned ip
        String dhcpIp = createProxServer(server.getVmid(), server.getName());
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

        // Route server name subdomain to server ip address
        if(!reverseProxyUtil.configServer(server)) {
            node.put("error", "Failed to configure server routing.");
            return node;
        }

        // update server database entry
        if(!serverDA0.update(server)){
            node.put("error", "Failed to update server entry.");
            return node;
        }

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
    public boolean renameServer(String username, int vmid, String serverName) throws SQLException, IOException, ClassNotFoundException {
        List<Server> userServers= serverDA0.fetchByUsername(username);

        for(Server server: userServers){
            if(server.getVmid() == vmid){
                server.setName(serverName);
                return serverDA0.update(server);
            }
        }

        return false;
    }

    @Override
    public boolean deployBuild(String username, int vmid, MultipartFile buildFile) throws SQLException, IOException, ClassNotFoundException {
        List<Server> userServers = serverDA0.fetchByUsername(username);

        Server deployServer = new Server();

        for(Server server: userServers){
            if(server.getVmid() == vmid){
                deployServer = server;
                break;
            }
        }

        if(deployServer.isFound()){
            if(proxmoxUtil.isVmRunning(vmid)){
                return clientServerUtil.updateBuildFile(deployServer.getIpAddress(), buildFile);
            } else {
                return false;
            }
        }

        return false;
    }
}
