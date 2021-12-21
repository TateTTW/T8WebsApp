package com.t8webs.enterprise.service;

import com.mashape.unirest.http.exceptions.UnirestException;
import com.t8webs.enterprise.dao.IServerDAO;
import com.t8webs.enterprise.dao.IUserAccountDAO;
import com.t8webs.enterprise.dto.Server;
import com.t8webs.enterprise.dto.UserAccount;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

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
    IUserAccountDAO userAccountDAO;
    @Autowired
    IDomainService domainService;

    /**
     * @param userAccount UserAccount to assign a server to
     * @param serverName  String name to give server
     * @return
     */
    @Override
    public boolean assignUserServer(UserAccount userAccount, String serverName) throws SQLException, IOException, ClassNotFoundException, UnirestException {
        if(!serverNameConforms(serverName) || serverDA0.existsBy(serverName.trim())){
            return false;
        }

        List<Server> availableServers = serverDA0.fetchByUsername("");
        if(availableServers.isEmpty()){
            return false;
        }

        Server server = availableServers.get(0);
        server.setName(serverName.trim());
        server.setUsername(userAccount.getUsername());

        boolean assignedServer = serverDA0.update(server);

        if(assignedServer){
            return domainService.createDomain(server.getName());
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
     * @param userAccount UserAccount tied to server
     * @param vmid        int uniquely identifying the server
     * @param serverName  String to rename the server
     * @return
     */
    @Override
    public boolean renameServer(UserAccount userAccount, int vmid, String serverName) throws SQLException, IOException, ClassNotFoundException {
        List<Server> userServers= serverDA0.fetchByUsername(userAccount.getUsername());

        for(Server server: userServers){
            if(server.getVmid() == vmid){
                server.setName(serverName);
                return serverDA0.update(server);
            }
        }

        return false;
    }
}
