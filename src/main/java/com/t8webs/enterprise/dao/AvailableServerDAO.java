package com.t8webs.enterprise.dao;

import com.t8webs.enterprise.dto.Server;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

@Repository
@Profile("dev")
public class AvailableServerDAO implements IAvailableServerDAO {

    /**
     * Method for inserting a Server when it has been unassigned
     *
     * @param server Server object to be saved as a record in the database
     * @return boolean indicating a successful save
     */
    @Override
    public boolean save(Server server) {
        DbQuery query = newQuery();
        query.setColumnValue("vmid", server.getVmid());
        query.setColumnValue("ipAddress", server.getIpAddress());

        try {
            return query.insert();
        } catch (DbQuery.IntegrityConstraintViolationException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Method for fetching an available server
     *
     * @return available Servers
     */
    @Override
    public Server fetchAvailable() {
        List<Server> servers = parse(newQuery().select());

        if(servers.isEmpty()){
            return new Server();
        }

        return servers.get(0);
    }

    /**
     * Method for deleting available Server when it has been assigned
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
     * @return DbQuery object for querying database
     */
    private DbQuery newQuery() {
        DbQuery dao = new DbQuery();
        dao.setTableName("AvailableServer");
        return dao;
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
            server.setName("");
            server.setUsername("");
            server.setIpAddress((String) valuesMap.get("ipAddress"));
            server.setVmid((Integer) valuesMap.get("vmid"));
            server.setFound(true);
            servers.add(server);
        }
        return servers;
    }
}
