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
@Profile("dev")
public class AssignedServerDAO extends BaseDAO implements IAssignedServerDAO {

    public AssignedServerDAO() {
        super.setTableName("AssignedServer");
    }

    /**
     * Method for creating a new Server record in the database
     *
     * @param server Server object to be saved as a record in the database
     * @return boolean indicating a successful save
     */
    @Override
    public boolean save(Server server) throws SQLException, IOException, ClassNotFoundException {
        setColumnValue("name", server.getName());
        setColumnValue("username", server.getUsername());
        setColumnValue("vmid", server.getVmid());
        setColumnValue("ipAddress", server.getIpAddress());
        setColumnValue("dnsId", server.getDnsId());

        return insert();
    }

    /**
     * Method for checking whether a record exists for the given server name
     *
     * @param name String uniquely identifying a server by name
     * @return boolean indicating whether a record exists for this server
     */
    @Override
    public boolean existsBy(String name) throws SQLException, IOException, ClassNotFoundException {
        addWhere("name", name);
        return !select().isEmpty();
    }

    /**
     * Method for fetching all assigned servers
     *
     * @return List of all assigned servers
     */
    @Override
    public List<Server> fetchAll() throws SQLException, IOException, ClassNotFoundException {
        return parse(select());
    }

    /**
     * Method for fetching all servers assigned to a user
     *
     * @param username String uniquely identifying a user
     * @return List of Servers assigned to the given user
     */
    @Override
    public List<Server> fetchByUsername(String username) throws SQLException, IOException, ClassNotFoundException {
        addWhere("username", username);
        return parse(select());
    }

    /**
     * @param username String uniquely identifying a User
     * @param vmid     String uniquely identifying a server record
     * @return server record assigned to the user
     */
    @Override
    public Server fetchUserServer(String username, int vmid) throws SQLException, IOException, ClassNotFoundException {
        addWhere("vmid", vmid);
        addWhere("username", username);
        List<Server> servers = parse(select());

        if(servers.isEmpty()){
            return new Server();
        }

        return servers.get(0);
    }

    /**
     * Method for deleting an assigned server record
     *
     * @param vmid int uniquely identifying a Server
     * @return boolean indicating a successful delete
     */
    @Override
    public boolean delete(int vmid) throws SQLException, IOException, ClassNotFoundException {
        addWhere("vmid", vmid);
        return delete();
    }

    /**
     * Method for updating an existing Server record in the database
     *
     * @param server Server object to be used for updating a database record
     * @return boolean indicating a successful update
     */
    @Override
    public boolean update(Server server) throws SQLException, IOException, ClassNotFoundException {
        setColumnValue("name", server.getName());
        setColumnValue("dnsId", server.getDnsId());
        addWhere("vmid", server.getVmid());

        return update();
    }

    /**
     * Method for parsing SQL results into List of Server objects
     *
     * @param results data structure representation of sql results
     * @return List of Server objects
     */
    private List<Server> parse(ArrayList<HashMap<String, Object>> results) {
        ArrayList<Server> servers = new ArrayList<>();
        for (HashMap valuesMap: results) {
            Server server = new Server();
            server.setName((String) valuesMap.get("name"));
            server.setUsername((String) valuesMap.get("username"));
            server.setIpAddress((String) valuesMap.get("ipAddress"));
            server.setDnsId((String) valuesMap.get("dnsId"));
            server.setVmid((Integer) valuesMap.get("vmid"));
            server.setFound(true);
            servers.add(server);
        }
        return servers;
    }
}
