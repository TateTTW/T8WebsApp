package com.t8webs.enterprise.dao.AssignedServer;

import com.t8webs.enterprise.dao.DbQuery;
import com.t8webs.enterprise.dto.Server;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

@Repository
@Profile("dev")
public class AssignedServerDAO implements IAssignedServerDAO {

    /**
     * Method for creating a new Server record in the database
     *
     * @param server Server object to be saved as a record in the database
     * @return boolean indicating a successful save
     */
    @Override
    public boolean save(Server server) throws DbQuery.IntegrityConstraintViolationException {
        DbQuery query = newQuery();
        query.setColumnValue("vmid", server.getVmid());
        query.setColumnValue("userId", server.getUserId());
        query.setColumnValue("name", server.getName());
        query.setColumnValue("ipAddress", server.getIpAddress());
        query.setColumnValue("dnsId", server.getDnsId());
        query.setColumnValue("creationStatus", server.getCreationStatus().name());

        return query.insert();
    }

    /**
     * Method for checking whether a record exists for the given server name
     *
     * @param name String uniquely identifying a server by name
     * @return boolean indicating whether a record exists for this server
     */
    @Override
    public boolean nameExists(String name) {
        DbQuery query = newQuery();
        query.addWhere("name", name);
        return !query.select().isEmpty();
    }

    /**
     * Method for fetching all assigned servers
     *
     * @return List of all assigned servers
     */
    @Override
    public List<Server> fetchAll() {
        return parse(newQuery().select());
    }

    /**
     * Method for fetching all servers assigned to a user
     *
     * @param userId String uniquely identifying a user
     * @return List of Servers assigned to the given user
     */
    @Override
    public List<Server> fetchByUserId(String userId) {
        DbQuery query = newQuery();
        query.addWhere("userId", userId);
        return parse(query.select());
    }

    /**
     * @param userId String uniquely identifying a User
     * @param vmid     String uniquely identifying a server record
     * @return server record assigned to the user
     */
    @Override
    public Server fetchUserServer(String userId, int vmid) {
        DbQuery query = newQuery();
        query.addWhere("vmid", vmid);
        query.addWhere("userId", userId);
        List<Server> servers = parse(query.select());

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
    public boolean delete(int vmid) {
        DbQuery query = newQuery();
        query.addWhere("vmid", vmid);
        return query.delete();
    }

    /**
     * Method for updating an existing Server record in the database
     *
     * @param server Server object to be used for updating a database record
     * @return boolean indicating a successful update
     */
    @Override
    public boolean update(Server server) {
        DbQuery query = newQuery();
        query.setColumnValue("name", server.getName());
        query.setColumnValue("dnsId", server.getDnsId());
        query.setColumnValue("creationStatus", server.getCreationStatus().name());
        query.addWhere("vmid", server.getVmid());

        return query.update();
    }

    /**
     * @return DbQuery object for querying database
     */
    private DbQuery newQuery() {
        DbQuery query = new DbQuery();
        query.setTableName("AssignedServer");
        return query;
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
            server.setVmid((Integer) valuesMap.get("vmid"));
            server.setUserId((String) valuesMap.get("userId"));
            server.setName((String) valuesMap.get("name"));
            server.setIpAddress((String) valuesMap.get("ipAddress"));
            server.setDnsId((String) valuesMap.get("dnsId"));
            server.setCreationStatus((String) valuesMap.get("creationStatus"));
            server.setFound(true);
            servers.add(server);
        }
        return servers;
    }
}
