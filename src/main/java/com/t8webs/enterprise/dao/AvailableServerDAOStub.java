package com.t8webs.enterprise.dao;

import com.t8webs.enterprise.dto.Server;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Repository;

import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

@Repository
@Profile("test")
public class AvailableServerDAOStub implements IAvailableServerDAO{

    private static HashMap<Integer, Server> servers = new HashMap<>();
    static {
        for(int i=123; i<=130; i++){
            Server server = new Server();
            server.setVmid(i);
            server.setIpAddress("192.168.90."+i);
            server.setUsername("");
            server.setName("");
            server.setFound(true);
            servers.put(server.getVmid(), server);
        }
    }
    /**
     * Method for inserting a Server when it has been unassigned
     *
     * @param server Server object to be saved as a record in the database
     * @return boolean indicating a successful save
     */
    @Override
    public boolean save(Server server) throws SQLException, IOException, ClassNotFoundException {
        if(servers.containsKey(server.getVmid())){
            return false;
        }

        server.setFound(true);
        server.setUsername("");
        server.setName("");

        servers.put(server.getVmid(), server);

        return true;
    }

    /**
     * Method for fetching an available server
     *
     * @return available Servers
     */
    @Override
    public Server fetchAvailable() throws SQLException, IOException, ClassNotFoundException {
        List<Server> availableServers = new ArrayList<>(servers.values());

        if(availableServers.isEmpty()){
            return new Server();
        }

        return availableServers.get(0);
    }

    /**
     * Method for deleting available Server when it has been assigned
     *
     * @param vmid int uniquely identifying a Server
     * @return boolean indicating a successful delete
     */
    @Override
    public boolean delete(int vmid) throws SQLException, IOException, ClassNotFoundException {
        Server server = servers.get(vmid);

        if(server == null || !server.isFound()){
            return false;
        }

        servers.remove(vmid);

        return true;
    }
}
