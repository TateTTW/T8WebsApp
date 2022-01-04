package com.t8webs.enterprise.dao;

import com.t8webs.enterprise.dto.Server;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Repository;

import java.io.IOException;
import java.sql.SQLException;
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
public class ServerDAOStub implements IServerDAO {
    private static HashMap<Integer, Server> servers;
    static {
        servers = new HashMap<>();
        for(int i=120; i<=130; i++){
            Server server = new Server();
            server.setVmid(i);
            server.setIpAddress("192.168.90."+i);
            if(i==128){
                server.setUsername("tatettw@gmail.com");
                server.setName("tvtracker");
            } else {
                server.setUsername("");
                server.setName("");
            }
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
    public boolean save(Server server) throws SQLException, IOException, ClassNotFoundException {
        if(servers.containsKey(server.getVmid())){
            return false;
        }

        server.setFound(true);

        servers.put(server.getVmid(), server);

        return true;
    }

    /**
     * Method for fetching servers
     *
     * @param vmid int uniquely identifying a Server record
     * @return Server with the given server name
     */
    @Override
    public Server fetch(int vmid) throws SQLException, IOException, ClassNotFoundException {
        Server server = servers.get(vmid);

        if(server != null && server.isFound()){
            return server;
        }

        return new Server();
    }

    /**
     * Method for fetching servers
     *
     * @param name String uniquely identifying a Server record
     * @return Server with the given server name
     */
    @Override
    public Server fetch(String name) throws SQLException, IOException, ClassNotFoundException {
        for(Server server: servers.values()){
            if(server.getName().equals(name)){
                return server;
            }
        }

        return new Server();
    }

    /**
     * Method for fetching an unassigned server
     *
     * @return Server available to be assigned
     */
    @Override
    public Server fetchAvailable() throws SQLException, IOException, ClassNotFoundException {
        for(Server server: servers.values()){
            if(server.getName().isEmpty() && server.getUsername().isEmpty()){
                return server;
            }
        }

        return new Server();
    }

    /**
     * Method for checking whether a record exists for the given server name
     *
     * @param name String uniquely identifying a server by name
     * @return boolean indicating whether a record exists for this server
     */
    @Override
    public boolean existsBy(String name) throws SQLException, IOException, ClassNotFoundException {
        for(Server server: servers.values()){
            if(server.getName().equals(name)){
                return true;
            }
        }

        return false;
    }

    /**
     * Method for fetching servers assigned to a user
     *
     * @param username String uniquely identifying a UserAccount record
     * @return List of Servers assigned to the given user
     */
    @Override
    public List<Server> fetchByUsername(String username) throws SQLException, IOException, ClassNotFoundException {
        List<Server> userServers = new ArrayList<Server>();
        for(Server server: servers.values()){
            if(server.getUsername().equals(username)){
                userServers.add(server);
            }
        }

        return userServers;
    }

    /**
     * Method for deleting a distinct UserAccount record from the database
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

    /**
     * Method for updating an existing Server record in the database
     *
     * @param server Server object to be used for updating a database record
     * @return boolean indicating a successful update
     */
    @Override
    public boolean update(Server server) throws SQLException, IOException, ClassNotFoundException {
        if(servers.containsKey(server.getVmid())){
            servers.put(server.getVmid(), server);
            return true;
        }
        return false;
    }
}
