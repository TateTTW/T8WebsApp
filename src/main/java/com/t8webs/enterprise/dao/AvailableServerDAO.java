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
public class AvailableServerDAO extends BaseDAO implements IAvailableServerDAO {

    public AvailableServerDAO() {
        super.setTableName("AvailableServer");
    }

    /**
     * Method for inserting a Server when it has been unassigned
     *
     * @param server Server object to be saved as a record in the database
     * @return boolean indicating a successful save
     */
    @Override
    public boolean save(Server server) throws SQLException, IOException, ClassNotFoundException {
        setColumnValue("vmid", server.getVmid());
        setColumnValue("ipAddress", server.getIpAddress());

        return insert();
    }

    /**
     * Method for fetching an available server
     *
     * @return available Servers
     */
    @Override
    public Server fetchAvailable() throws SQLException, IOException, ClassNotFoundException {
        List<Server> servers = parse(select());

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
    public boolean delete(int vmid) throws SQLException, IOException, ClassNotFoundException {
        addWhere("vmid", vmid);
        return delete();
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
