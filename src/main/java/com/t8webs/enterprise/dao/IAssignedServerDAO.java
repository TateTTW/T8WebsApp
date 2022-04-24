package com.t8webs.enterprise.dao;

import com.t8webs.enterprise.dto.Server;

import java.util.List;

public interface IAssignedServerDAO {
    /**
     * Method for adding a Server assigned to a user
     *
     * @param server Server object to be saved as a record in the database
     * @return boolean indicating a successful save
     */
    boolean save(Server server) throws DbQuery.IntegrityConstraintViolationException;

    /**
     * Method for checking whether a record exists for the given server name
     *
     * @param name String uniquely identifying a server by name
     * @return boolean indicating whether a record exists for this server
     */
    boolean existsBy(String name);

    /**
     * Method for fetching all assigned servers
     *
     * @return List of all assigned servers
     */
    List<Server> fetchAll();

    /**
     * Method for fetching servers assigned to a user
     *
     * @param username String uniquely identifying a User
     * @return List of Servers assigned to the given user
     */
    List<Server> fetchByUsername(String username);

    /**
     * @param username String uniquely identifying a User
     * @param vmid String uniquely identifying a server record
     * @return server record assigned to the user
     */
    Server fetchUserServer(String username, int vmid);

    /**
     * Method for deleting a Server when it has been unassigned
     *
     * @param vmid int uniquely identifying a Server
     * @return boolean indicating a successful delete
     */
    boolean delete(int vmid);

    /**
     * Method for updating an existing Server record in the database
     *
     * @param server Server object to be used for updating a database record
     * @return boolean indicating a successful update
     */
    boolean update(Server server);
}
