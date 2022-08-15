package com.t8webs.enterprise.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.t8webs.enterprise.dao.IAssignedServerDAO;
import com.t8webs.enterprise.dto.Server;
import com.t8webs.enterprise.utils.*;
import kong.unirest.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class ServerService implements IServerService {
    @Autowired
    IDomainUtil domainUtil;
    @Autowired
    IAssignedServerDAO assignedServerDAO;
    @Autowired
    IProxmoxUtil proxmoxUtil;
    @Autowired
    IReverseProxyUtil reverseProxyUtil;
    @Autowired
    IClientServerUtil clientServerUtil;

    Logger logger = LoggerFactory.getLogger(this.getClass());

    private final ObjectMapper mapper = new ObjectMapper();

    /**
     * @param username    String user to assign a server to
     * @param serverName  String name to give server
     * @return
     */
    @Override
    public Server.CreationStatus addServer(String username, String serverName) {
        Server.CreationStatus status = Server.CreationStatus.BEGIN;
        Server server = new Server();

        try {
            // Confirm that the server name can be used
            if (!serverNameConforms(serverName) || assignedServerDAO.existsBy(serverName)) {
                throw new ServerCreationException(server);
            }
            status = Server.CreationStatus.VERIFIED_NAME;

            // Assign an available server to the user
            server = clientServerUtil.assignUserServer(username, serverName);
            if (!server.isFound()) {
                throw new ServerCreationException(server);
            }
            status = Server.CreationStatus.ASSIGNED;

            // Add a dns record for the server
            String dnsId = domainUtil.addDnsRecord(server.getName());
            if (dnsId.isEmpty()) {
                throw new ServerCreationException(server);
            }
            status = Server.CreationStatus.HAS_DNS_RECORD;

            // Save dnsId to database
            server.setDnsId(dnsId);
            if (!assignedServerDAO.update(server)) {
                throw new ServerCreationException(server);
            }
            status = Server.CreationStatus.SAVED_DNS_ID;

            // Create a vm from template
            if (!proxmoxUtil.cloneVM(server.getVmid(), serverName)) {
                throw new ServerCreationException(server);
            }
            status = Server.CreationStatus.HAS_VM;

            // Start virtual machine
            if (!proxmoxUtil.startVM(server.getVmid())) {
                throw new ServerCreationException(server);
            }

            // Retrieve virtual machine's dhcp ip address
            String dhcpIp = proxmoxUtil.getServerIp(server.getVmid());
            if (dhcpIp.isEmpty()) {
                throw new ServerCreationException(server);
            }

            // Set the vm's ip to a static ip defined in the server database entry
            if (!clientServerUtil.updateServerIp(dhcpIp, server.getIpAddress())) {
                throw new ServerCreationException(server);
            }

            // Update proxy config to route traffic to the server
            if(!reverseProxyUtil.addHostEntry(server)) {
                throw new ServerCreationException(server);
            }
            status = Server.CreationStatus.HAS_PROXY_CFG;

            // Shutdown vm to apply ip on startup
            if(!proxmoxUtil.shutdownVM(server.getVmid())) {
                throw new ServerCreationException(server);
            }

            // Server creation has completed successfully
            server.setCreationStatus(Server.CreationStatus.COMPLETED);
            status = Server.CreationStatus.COMPLETED;

            // Wait for server to complete shutdown
            proxmoxUtil.reachedState(ProxmoxUtil.State.STOPPED, server.getVmid());

        } catch (Exception e) {
            logger.error("Failed to add server: " + server.getName() + "(" + server.getVmid() + ")");
            logger.error("Creation status: " + status.name());
            e.printStackTrace();

            server.setCreationStatus(status);
            rollbackServerCreation(server);
        }

        if (server.isFound()) {
            assignedServerDAO.update(server);
        }

        return status;
    }

    private void rollbackServerCreation(Server server) {
        logger.error("Rollback Server: " + server.getName() + "(" + server.getVmid() + ")");
        Server.CreationStatus status = server.getCreationStatus();

        try {
            switch (status) {
                case HAS_PROXY_CFG:
                    if (reverseProxyUtil.deleteHostEntry(server)) {
                        server.setCreationStatus(Server.CreationStatus.HAS_VM);
                    } else {
                        break;
                    }
                case HAS_VM:
                    if (!proxmoxUtil.isVmRunning(server.getVmid())
                            || (proxmoxUtil.shutdownVM(server.getVmid()) && proxmoxUtil.reachedState(ProxmoxUtil.State.STOPPED, server.getVmid())))
                    {
                        if (proxmoxUtil.deleteVM(server.getVmid())) {
                            server.setCreationStatus(Server.CreationStatus.SAVED_DNS_ID);
                        } else {
                            break;
                        }
                    } else {
                        break;
                    }
                case SAVED_DNS_ID:
                case HAS_DNS_RECORD:
                    if (domainUtil.deleteDnsRecord(server.getDnsId())) {
                        server.setCreationStatus(Server.CreationStatus.ASSIGNED);
                    } else {
                        break;
                    }
                case ASSIGNED:
                    if (clientServerUtil.unassignUserServer(server)) {
                        server.setFound(false);
                        server.setCreationStatus(Server.CreationStatus.VERIFIED_NAME);
                    }
            }
        } catch (ProxmoxUtil.InvalidVmStateException e) {
            logger.error("Server Rollback Error: " + server.getName() + "(" + server.getVmid() + ")");
            e.printStackTrace();
        }
    }

    private boolean serverNameConforms(String serverName) {
        if (serverName == null || serverName.trim().length() < 2 || serverName.trim().length() > 20 || serverName.contains(" ")){
            return false;
        }

        Pattern pattern = Pattern.compile("[^A-Za-z0-9]");
        Matcher matcher = pattern.matcher(serverName);

        return !matcher.find();
    }

    /**
     * @param username    String user assigned to server
     * @param vmid        int uniquely identifying the server
     * @param serverName  String to rename the server
     * @return
     */
    @Override
    public boolean renameServer(String username, int vmid, String serverName) {
        // Confirm that the server name can be used
        if(!serverNameConforms(serverName) || assignedServerDAO.existsBy(serverName.trim())){
            return false;
        }

        // Confirm server is assigned to user
        Server server = assignedServerDAO.fetchUserServer(username, vmid);
        if(!server.isFound()){
            return false;
        }

        // Remove previous reverse proxy host config entry
        reverseProxyUtil.deleteHostEntry(server);

        // Set new server name
        server.setName(serverName);

        // Update database record, update proxy configuration, & update dns record
        if (assignedServerDAO.update(server) && domainUtil.renameDnsRecord(serverName, server.getDnsId()))
        {
            return reverseProxyUtil.addHostEntry(server);
        }

        return false;
    }

    /**
     * @param username    String uniquely identifying user
     * @return
     */
    @Override
    public ArrayNode getUserServers(String username) {
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
    public boolean deployBuild(String username, int vmid, MultipartFile buildFile) throws IOException {
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
    public boolean startVM(String username, int vmid) throws ProxmoxUtil.InvalidVmStateException {
        Server server = assignedServerDAO.fetchUserServer(username, vmid);

        return server.isFound()
                && proxmoxUtil.startVM(vmid)
                && proxmoxUtil.reachedState(ProxmoxUtil.State.RUNNING, vmid);
    }

    @Override
    public boolean shutdownVM(String username, int vmid) throws ProxmoxUtil.InvalidVmStateException {
        Server server = assignedServerDAO.fetchUserServer(username, vmid);

        return server.isFound()
                && proxmoxUtil.shutdownVM(vmid)
                && proxmoxUtil.reachedState(ProxmoxUtil.State.STOPPED, vmid);
    }

    @Override
    public boolean rebootVM(String username, int vmid) throws ProxmoxUtil.InvalidVmStateException {
        Server server = assignedServerDAO.fetchUserServer(username, vmid);

        return server.isFound()
                && proxmoxUtil.rebootVM(vmid)
                && proxmoxUtil.reachedState(ProxmoxUtil.State.RUNNING, vmid);
    }

    @Override
    public boolean deleteVM(String username, int vmid) throws ProxmoxUtil.InvalidVmStateException {
        Server server = assignedServerDAO.fetchUserServer(username, vmid);

        if(server.isFound()
            && !proxmoxUtil.isVmRunning(vmid)
            && proxmoxUtil.deleteVM(vmid)
            && clientServerUtil.unassignUserServer(server)
            && domainUtil.deleteDnsRecord(server.getDnsId()))
        {
            return reverseProxyUtil.deleteHostEntry(server);
        }

        return false;
    }

    @Override
    public String getVmStatus(String username, int vmid) {
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

    @Override
    public JSONObject getVmData(String username, int vmid, ProxmoxUtil.TimeFrame timeFrame) {
        Server server = assignedServerDAO.fetchUserServer(username, vmid);

        if(server.isFound()){
            return proxmoxUtil.getVmData(vmid, timeFrame);
        }

        return new JSONObject();
    }

    public class ServerCreationException extends Exception {
        public ServerCreationException(Server server) {
            super("Error occurred during the creation of server " + server.getName() + "(" + server.getVmid() + ")");
        }
    }
}
