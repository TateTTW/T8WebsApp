package com.t8webs.enterprise.dao;

import com.t8webs.enterprise.dto.Server;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;

/**
 * Data Access Object for Servers
 * <p>
 *     This class allows access to Server records in our underlying database.
 * </p>
 */
public interface IServerDAO {
    /**
     * Method for creating a new Server record in the database
     *
     * @param server Server object to be saved as a record in the database
     * @return boolean indicating a successful save
     */
    boolean save(Server server) throws SQLException, IOException, ClassNotFoundException;

    /**
     * Method for fetching servers
     *
     * @param vmid int uniquely identifying a Server record
     * @return Server with the given server name
     */
    Server fetch(int vmid) throws SQLException, IOException, ClassNotFoundException;

    /**
     * Method for fetching servers
     *
     * @param name String uniquely identifying a Server record
     * @return Server with the given server name
     */
    Server fetch(String name) throws SQLException, IOException, ClassNotFoundException;

    /**
     * Method for fetching an unassigned server
     * @return Server available to be assigned
     */
    Server fetchAvailable() throws SQLException, IOException, ClassNotFoundException;

    /**
     * Method for checking whether a record exists for the given server name
     *
     * @param name String uniquely identifying a server by name
     * @return boolean indicating whether a record exists for this server
     */
    boolean existsBy(String name) throws SQLException, IOException, ClassNotFoundException;

    /**
     * Method for fetching servers assigned to a user
     *
     * @param username String uniquely identifying a UserAccount record
     * @return List of Servers assigned to the given user
     */
    List<Server> fetchByUsername(String username) throws SQLException, IOException, ClassNotFoundException;

    /**
     * Method for deleting a distinct UserAccount record from the database
     *
     * @param vmid int uniquely identifying a Server
     * @return boolean indicating a successful delete
     */
    boolean delete(int vmid) throws SQLException, IOException, ClassNotFoundException;

    /**
     * Method for updating an existing Server record in the database
     *
     * @param server Server object to be used for updating a database record
     * @return boolean indicating a successful update
     */
    boolean update(Server server) throws SQLException, IOException, ClassNotFoundException;
}
