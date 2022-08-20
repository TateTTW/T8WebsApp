package com.t8webs.enterprise.dao.User;

import com.t8webs.enterprise.dao.DbQuery;
import com.t8webs.enterprise.dto.User;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

@Repository
@Profile("test")
public class UserDAOStub implements IUserDAO {
    private static HashMap<String, User> users = new HashMap<>();

    /**
     * Method for inserting a new User record
     *
     * @param user User object to be saved in the database
     * @return boolean indicating a successful save
     */
    @Override
    public boolean save(User user) throws DbQuery.IntegrityConstraintViolationException {
        if (users.containsKey(user.getUserId())) {
           return false;
        }

        users.put(user.getUserId(), user);
        return true;
    }

    /**
     * Method for updating a User record
     *
     * @param user User object to be updated in the database
     * @return boolean indicating the user has been approved
     */
    @Override
    public boolean update(User user) {
        User updateUser = users.get(user.getUserId());
        if (updateUser != null) {
            updateUser.setName(user.getName());
            updateUser.setEmail(user.getEmail());
            return true;
        }

        return false;
    }

    /**
     * Method for retrieving a User record
     *
     * @param userId String uniquely identifying a user
     * @return User object
     */
    @Override
    public User getUserById(String userId) {
        User user = users.get(userId);
        if (user != null) {
            return user;
        }

        return new User();
    }

    /**
     * Method for verifying a user exists
     *
     * @param userId String uniquely identifying a user
     * @return boolean indicating a user record exists
     */
    @Override
    public boolean userExists(String userId) {
        return users.get(userId) != null;
    }

    /**
     * Method for confirming a given userId has CRUD server privileges
     *
     * @param userId String uniquely identifying a user
     * @return boolean indicating the user has been approved
     */
    @Override
    public boolean isApproved(String userId) {
        User user = users.get(userId);
        return user != null && user.isApproved();
    }

    /**
     * Method for confirming a given userId has admin privileges
     *
     * @param userId String uniquely identifying a user
     * @return boolean indicating the user is an admin
     */
    @Override
    public boolean isAdmin(String userId) {
        User user = users.get(userId);
        return user != null && user.isAdmin();
    }

    /**
     * Method for requesting user access
     *
     * @param userId String uniquely identifying a user
     * @return boolean indicating a successful save
     */
    @Override
    public boolean requestAccess(String userId) {
        User user = users.get(userId);
        if (user != null) {
            user.setStatus(User.Status.REQUESTED);
            return true;
        }

        return false;
    }

    /**
     * Method for revoking a user's access
     *
     * @param userId String uniquely identifying a user
     * @return boolean indicating a successful update
     */
    @Override
    public boolean revokeAccess(String userId) {
        User user = users.get(userId);
        if (user != null) {
            user.setStatus(User.Status.NONE);
            return true;
        }

        return false;
    }

    /**
     * Method for approving access for a user
     *
     * @param userId String uniquely identifying a user
     * @return boolean indicating a successful update
     */
    @Override
    public boolean grantAccess(String userId) {
        User user = users.get(userId);
        if (user != null) {
            user.setStatus(User.Status.APPROVED);
            return true;
        }

        return false;
    }

    /**
     * Method for retrieving all user records
     *
     * @return List of Users
     */
    @Override
    public List<User> getAllUsers() {
        return users.values().stream().collect(Collectors.toCollection(ArrayList::new));
    }
}
