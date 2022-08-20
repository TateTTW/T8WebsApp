package com.t8webs.enterprise.dao.AssignedServer;

import com.t8webs.enterprise.dto.Server;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Data Access Object for Servers
 * <p>
 *     This class allows access to Server records in our underlying database.
 * </p>
 */
@Repository
@Profile("test")
public class AssignedServerDAOStub implements IAssignedServerDAO {
    private static HashMap<Integer, Server> servers = new HashMap<>();
    static {
        for(int i=120; i<=122; i++){
            Server server = new Server();
            server.setVmid(i);
            server.setIpAddress("192.168.90."+i);
            server.setUserId("100201287479296569425");
            server.setName("T8Server"+i);
            server.setCreationStatus(Server.CreationStatus.COMPLETED);
            server.setFound(true);
            servers.put(server.getVmid(), server);
        }
    }
    /**
     * Method for creating a new Server record in the database
     *
     * @param server Server object to be saved as a record in the database
     * @return boolean indicating a successful save
     */
    @Override
    public boolean save(Server server) {
        if(servers.containsKey(server.getVmid())){
            return false;
        }

        server.setFound(true);

        servers.put(server.getVmid(), server);

        return true;
    }


    /**
     * Method for checking whether a record exists for the given server name
     *
     * @param name String uniquely identifying a server by name
     * @return boolean indicating whether a record exists for this server
     */
    @Override
    public boolean nameExists(String name) {
        for(Server server: servers.values()){
            if(server.getName().equals(name)){
                return true;
            }
        }

        return false;
    }

    /**
     * Method for fetching all assigned servers
     *
     * @return List of all assigned servers
     */
    @Override
    public List<Server> fetchAll() {
        return new ArrayList<>(servers.values());
    }

    /**
     * Method for fetching servers assigned to a user
     *
     * @param userId String uniquely identifying a User
     * @return List of Servers assigned to the given user
     */
    @Override
    public List<Server> fetchByUserId(String userId) {
        List<Server> userServers = new ArrayList<Server>();
        for(Server server: servers.values()){
            if(server.getUserId().equals(userId)){
                userServers.add(server);
            }
        }

        return userServers;
    }

    /**
     * @param userId String uniquely identifying a User
     * @param vmid     String uniquely identifying a server record
     * @return server record assigned to the user
     */
    @Override
    public Server fetchUserServer(String userId, int vmid) {
        for(Server server: servers.values()){
            if(server.getVmid() == vmid && server.getUserId().equals(userId)){
                return server;
            }
        }

        return new Server();
    }

    /**
     * Method for deleting an assigned server record
     *
     * @param vmid int uniquely identifying a Server
     * @return boolean indicating a successful delete
     */
    @Override
    public boolean delete(int vmid) {
        Server server = servers.get(vmid);

        if(server == null || !server.isFound()){
            return false;
        }

        servers.remove(vmid);

        return true;
    }

    /**
     * Method for updating an existing Server record in the database
     *
     * @param server Server object to be used for updating a database record
     * @return boolean indicating a successful update
     */
    @Override
    public boolean update(Server server) {
        if(servers.containsKey(server.getVmid())){
            servers.put(server.getVmid(), server);
            return true;
        }
        return false;
    }
}
