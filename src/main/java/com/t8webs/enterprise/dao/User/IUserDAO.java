package com.t8webs.enterprise.dao.User;

import com.t8webs.enterprise.dao.DbQuery;
import com.t8webs.enterprise.dto.User;

import java.util.List;

/**
 * Data Access Object for UserStatuses
 * <p>
 *     This class allows access to UserStatus records in our underlying database.
 * </p>
 */
public interface IUserDAO {

    /**
     * Method for inserting a new User record
     *
     * @param user User object to be saved in the database
     * @return boolean indicating a successful save
     */

    boolean save(User user) throws DbQuery.IntegrityConstraintViolationException;

    /**
     * Method for updating a User record
     *
     * @param user User object to be updated in the database
     * @return boolean indicating the user has been approved
     */

    boolean update(User user);

    /**
     * Method for retrieving a User record
     *
     * @param userId String uniquely identifying a user
     * @return User object
     */
    User getUserById(String userId);

    /**
     * Method for verifying a user exists
     *
     * @param userId String uniquely identifying a user
     * @return boolean indicating a user record exists
     */
    boolean userExists(String userId);

    /**
     * Method for confirming a given userId has CRUD server privileges
     *
     * @param userId String uniquely identifying a user
     * @return boolean indicating the user has been approved
     */
    boolean isApproved(String userId);

    /**
     * Method for confirming a given userId has admin privileges
     *
     * @param userId String uniquely identifying a user
     * @return boolean indicating the user is an admin
     */
    boolean isAdmin(String userId);

    /**
     * Method for requesting user access
     *
     * @param userId String uniquely identifying a user
     * @return boolean indicating a successful save
     */
    boolean requestAccess(String userId);

    /**
     * Method for revoking a user's access
     *
     * @param userId String uniquely identifying a user
     * @return boolean indicating a successful update
     */
    boolean revokeAccess(String userId);

    /**
     * Method for approving access for a user
     *
     * @param userId String uniquely identifying a user
     * @return boolean indicating a successful update
     */
    boolean grantAccess(String userId);

    /**
     * Method for retrieving all user records
     *
     * @return List of Users
     */
    List<User> getAllUsers();
}
