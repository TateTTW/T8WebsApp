package com.t8webs.enterprise.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.t8webs.enterprise.dao.AssignedServer.IAssignedServerDAO;
import com.t8webs.enterprise.dto.Server;
import com.t8webs.enterprise.utils.DnsUtil.IDnsUtil;
import com.t8webs.enterprise.utils.ProxmoxUtil.IProxmoxUtil;
import com.t8webs.enterprise.utils.ProxmoxUtil.ProxmoxUtil;
import com.t8webs.enterprise.utils.ReverseProxyUtil.IReverseProxyUtil;
import com.t8webs.enterprise.utils.UserServerUtil.IUserServerUtil;
import kong.unirest.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class ServerService implements IServerService {
    @Autowired
    IDnsUtil domainUtil;
    @Autowired
    IAssignedServerDAO assignedServerDAO;
    @Autowired
    IProxmoxUtil proxmoxUtil;
    @Autowired
    IReverseProxyUtil reverseProxyUtil;
    @Autowired
    IUserServerUtil userServerUtil;

    Logger logger = LoggerFactory.getLogger(this.getClass());

    private final ObjectMapper mapper = new ObjectMapper();

    /**
     * @param userId    String userId to assign a server to
     * @param serverName  String name to give server
     * @return
     */
    @Override
    public Server.CreationStatus addServer(String userId, String serverName) {
        Server.CreationStatus status = Server.CreationStatus.BEGIN;
        Server server = new Server();

        try {
            // Confirm that the server name can be used
            if (!serverNameConforms(serverName) || assignedServerDAO.nameExists(serverName)) {
                throw new ServerCreationException(server);
            }
            status = Server.CreationStatus.VERIFIED_NAME;

            // Assign an available server to the user
            server = userServerUtil.assignUserServer(userId, serverName);
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
            if (!userServerUtil.updateServerIp(dhcpIp, server.getIpAddress())) {
                throw new ServerCreationException(server);
            }

            // Update proxy config to route traffic to the server
            if(!reverseProxyUtil.addHostEntry(server.getName(), server.getVmid())) {
                throw new ServerCreationException(server);
            }
            status = Server.CreationStatus.HAS_PROXY_CFG;

            // Shutdown vm to apply ip on startup
            if(!proxmoxUtil.shutdownVM(server.getVmid())) {
                throw new ServerCreationException(server);
            }

            // Server creation has completed successfully
            status = Server.CreationStatus.COMPLETED;
            server.setCreationStatus(status);
            assignedServerDAO.update(server);

            // Wait for server to complete shutdown
            try {
                proxmoxUtil.reachedState(ProxmoxUtil.State.STOPPED, server.getVmid());
            } catch (ProxmoxUtil.InvalidVmStateException e) {
                e.printStackTrace();
            }
        } catch (Exception e) {
            logger.error("Failed to add server: " + server.toString());
            e.printStackTrace();

            server.setCreationStatus(status);
            rollbackServer(server);
        }

        return status;
    }

    private void rollbackServer(Server server) {
        Server.CreationStatus status = server.getCreationStatus();

        try {
            switch (status) {
                case COMPLETED:
                case HAS_PROXY_CFG:
                    if (reverseProxyUtil.deleteHostEntry(server.getName())) {
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
                    if (userServerUtil.unassignUserServer(server)) {
                        server.setCreationStatus(Server.CreationStatus.VERIFIED_NAME);
                        server.setFound(false);
                    }
            }
        } catch (ProxmoxUtil.InvalidVmStateException e) {
            logger.error("Server Rollback Error: " + server.getName() + "(" + server.getVmid() + ")");
            e.printStackTrace();
        }

        if (server.isFound()) {
            assignedServerDAO.update(server);
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
     * @param userId      String user assigned to server
     * @param vmid        int uniquely identifying the server
     * @param serverName  String to rename the server
     * @return
     */
    @Override
    public HttpStatus renameServer(String userId, int vmid, String serverName) {
        // Confirm that the server is assigned to user && the new server name can be used
        Server server = assignedServerDAO.fetchUserServer(userId, vmid);
        if(!server.isFound() || !serverNameConforms(serverName) || assignedServerDAO.nameExists(serverName.trim())){
            return HttpStatus.CONFLICT;
        }

        // Update database record, proxy configuration, and dns record
        if (reverseProxyUtil.deleteHostEntry(server.getName())) {
            String originalName = server.getName();
            server.setName(serverName);
            if (assignedServerDAO.update(server)) {
                boolean addedProxyEntry = reverseProxyUtil.addHostEntry(serverName, server.getVmid());
                boolean updatedDnsRecord = domainUtil.renameDnsRecord(serverName, server.getDnsId());

                if (!addedProxyEntry) {
                    logger.error("Failed to add proxy entry for " + server.toString());
                }

                if (!updatedDnsRecord) {
                    logger.error("Failed to update dns record for " + server.toString());
                }

                if (addedProxyEntry && updatedDnsRecord) {
                    return HttpStatus.OK;
                }
            } else {
                if (!reverseProxyUtil.addHostEntry(originalName, server.getVmid())) {
                    logger.error(server.toString() + " is missing a reverse proxy host entry.");
                }
            }
        }

        return HttpStatus.INTERNAL_SERVER_ERROR;
    }

    @Override
    public ArrayNode getAllServers() {
        List<HashMap<String, Object>> servers = assignedServerDAO.fetchAll();

        ArrayNode serverNodes = mapper.createArrayNode();
        for(HashMap<String, Object> server: servers){
            ObjectNode serverNode = mapper.valueToTree(server);
            serverNode.remove("ipAddress");
            serverNode.remove("dnsId");
            serverNodes.add(serverNode);
        }

        return serverNodes;
    }

    /**
     * @param userId    String uniquely identifying user
     * @return
     */
    @Override
    public ArrayNode getUserServers(String userId) {
        List<Server> servers = assignedServerDAO.fetchByUserId(userId);
        ArrayNode serverNodes = mapper.createArrayNode();
        for(Server server: servers){
            ObjectNode serverNode = mapper.createObjectNode();
            serverNode.put("id", String.valueOf(server.getVmid()));
            serverNode.put("name", server.getName());

            ObjectNode attributes = mapper.createObjectNode();
            attributes.put("type", 5);
            attributes.put("status", "");
            serverNode.put("hasAttributes", attributes);

            serverNodes.add(serverNode);
        }

        return serverNodes;
    }

    @Override
    public boolean deployBuild(String userId, int vmid, MultipartFile buildFile) throws IOException {
        Server server = assignedServerDAO.fetchUserServer(userId, vmid);

        if (server.isFound() && proxmoxUtil.isVmRunning(vmid)) {
            return userServerUtil.deployBuild(server.getIpAddress(), buildFile);
        }

        return false;
    }

    @Override
    public boolean startVM(String userId, int vmid) throws ProxmoxUtil.InvalidVmStateException {
        Server server = assignedServerDAO.fetchUserServer(userId, vmid);

        return server.isFound()
                && proxmoxUtil.startVM(vmid)
                && proxmoxUtil.reachedState(ProxmoxUtil.State.RUNNING, vmid);
    }

    @Override
    public boolean shutdownVM(String userId, int vmid) throws ProxmoxUtil.InvalidVmStateException {
        Server server = assignedServerDAO.fetchUserServer(userId, vmid);

        return server.isFound()
                && proxmoxUtil.shutdownVM(vmid)
                && proxmoxUtil.reachedState(ProxmoxUtil.State.STOPPED, vmid);
    }

    @Override
    public boolean rebootVM(String userId, int vmid) throws ProxmoxUtil.InvalidVmStateException {
        Server server = assignedServerDAO.fetchUserServer(userId, vmid);

        return server.isFound()
                && proxmoxUtil.rebootVM(vmid)
                && proxmoxUtil.reachedState(ProxmoxUtil.State.RUNNING, vmid);
    }

    @Override
    public boolean deleteVM(String userId, int vmid) {
        Server server = assignedServerDAO.fetchUserServer(userId, vmid);

        if (server.isFound() && !proxmoxUtil.isVmRunning(vmid)) {
            rollbackServer(server);
            return !server.isFound();
        }

        return false;
    }

    /**
     * Method for Admins to force delete a server
     *
     * @param vmid int uniquely identifying the server
     * @return boolean indicating a successful delete
     */
    @Override
    public boolean forceDeleteVM(int vmid) {
        // ALWAYS CHECK THAT USER IS ADMIN BEFORE CALLING THIS METHOD
        Server server = assignedServerDAO.fetchByVmId(vmid);

        if (server.isFound()) {
            rollbackServer(server);
            return !server.isFound();
        }

        return false;
    }

    @Override
    public String getVmStatus(String userId, int vmid) {
        Server server = assignedServerDAO.fetchUserServer(userId, vmid);

        if (server.isFound()) {
            if (proxmoxUtil.isVmLocked(vmid)) {
                return "locked";
            } else {
                return proxmoxUtil.getVmStatus(vmid);
            }
        }

        return "";
    }

    @Override
    public JSONObject getVmData(String userId, int vmid, ProxmoxUtil.TimeFrame timeFrame) {
        Server server = assignedServerDAO.fetchUserServer(userId, vmid);

        if (server.isFound()) {
            return proxmoxUtil.getVmData(vmid, timeFrame);
        }

        return new JSONObject();
    }

    private class ServerCreationException extends Exception {
        public ServerCreationException(Server server) {
            super("Error occurred during the creation of " + server.toString());
        }
    }
}
