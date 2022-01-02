package com.t8webs.enterprise.service;

import com.t8webs.enterprise.dao.IServerDAO;
import com.t8webs.enterprise.dto.Server;
import com.t8webs.enterprise.utils.ClientServerUtil;
import com.t8webs.enterprise.utils.ProxmoxUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class ServerService {
    @Autowired
    IServerDAO serverDA0;

    /**
     * @param username    String user to assign a server to
     * @param serverName  String name to give server
     * @return
     */
    public boolean assignUserServer(String username, String serverName) throws SQLException, IOException, ClassNotFoundException, ServerNameNotAvailable, NoAvailableServers {
        // Confirm that the server name can be used
        if(!serverNameConforms(serverName) || serverDA0.existsBy(serverName.trim())){
            throw new ServerNameNotAvailable();
        }
        // Find an available server entry
        Server server = serverDA0.fetchAvailable();
        if(!server.isFound()){
            throw new NoAvailableServers();
        }
        // Set user info on available server
        server.setName(serverName.trim());
        server.setUsername(username);
        // Create a vm from template and retrieve dhcp assigned ip
        String dhcpIp = ProxmoxUtil.createServer(server.getVmid(), server.getName());

        if(!dhcpIp.isEmpty()){
            // Set the vm's ip to a static ip defined in the server database entry
            ClientServerUtil.updateServerIp(dhcpIp, server.getIpAddress());
            // Shutdown vm to apply ip on startup
            ProxmoxUtil.shutdownVM(server.getVmid());
            // Update server entry in the server database table
            return serverDA0.update(server);
        }

        return false;
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

    /**
     * @param username    String user assigned to server
     * @param vmid        int uniquely identifying the server
     * @param serverName  String to rename the server
     * @return
     */
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

    public class ServerNameNotAvailable extends Exception { }

    public class NoAvailableServers extends Exception { }
}
