package com.t8webs.enterprise.dao;

import com.t8webs.enterprise.dto.Server;

import java.io.IOException;
import java.sql.SQLException;

/**
 * Data Access Object for Servers
 * <p>
 *     This class allows access to Server records in our underlying database.
 * </p>
 */
public interface IAvailableServerDAO {
    /**
     * Method for inserting a Server when it has been unassigned
     *
     * @param server Server object to be saved as a record in the database
     * @return boolean indicating a successful save
     */
    boolean save(Server server) throws SQLException, IOException, ClassNotFoundException;

    /**
     * Method for fetching an available server
     *
     * @return an available Server
     */
    Server fetchAvailable() throws SQLException, IOException, ClassNotFoundException;

    /**
     * Method for deleting an available Server when it has been assigned
     *
     * @param vmid int uniquely identifying a Server
     * @return boolean indicating a successful delete
     */
    boolean delete(int vmid) throws SQLException, IOException, ClassNotFoundException;

}
