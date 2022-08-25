package com.t8webs.enterprise.dao.User;

import com.t8webs.enterprise.dao.DbQuery;
import com.t8webs.enterprise.dto.User;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

@Repository
@Profile("dev")
public class UserDAO implements IUserDAO {
    /**
     * Method for inserting a new User record
     *
     * @param user User object to be saved in the database
     * @return boolean indicating a successful save
     */
    @Override
    public boolean save(User user) throws DbQuery.IntegrityConstraintViolationException {
        DbQuery query = newQuery();
        query.setColumnValue("userId", user.getUserId());
        query.setColumnValue("username", user.getName());
        query.setColumnValue("email", user.getEmail());
        query.setColumnValue("status", User.Status.NONE.name());

        return query.insert();
    }

    /**
     * Method for updating a User record
     *
     * @param user User object to be updated in the database
     * @return boolean indicating the user has been approved
     */
    @Override
    public boolean update(User user) {
        DbQuery query = newQuery();
        query.setColumnValue("username", user.getName());
        query.setColumnValue("email", user.getEmail());
        query.addWhere("userId", user.getUserId());

        return query.update();
    }

    /**
     * Method for retrieving a User record
     *
     * @param userId String uniquely identifying a user
     * @return User object
     */
    @Override
    public User getUserById(String userId) {
        DbQuery query = newQuery();
        query.addWhere("userId", userId);

        List<User> users = parse(query.select());

        if(users.isEmpty()){
            return new User();
        }

        return users.get(0);
    }

    /**
     * Method for verifying a user exists
     *
     * @param userId String uniquely identifying a user
     * @return boolean indicating a user record exists
     */
    @Override
    public boolean userExists(String userId) {
        DbQuery query = newQuery();
        query.addWhere("userId", userId);

        return !query.select().isEmpty();
    }

    /**
     * Method for confirming a given userId has CRUD server privileges
     *
     * @param userId String uniquely identifying a user
     * @return boolean indicating the user has been approved
     */
    @Override
    public boolean isApproved(String userId) {
        DbQuery query = newQuery();
        query.addWhere("userId", userId);
        query.addOrWhere("status", new String[]{ User.Status.ADMIN.name(), User.Status.APPROVED.name() });

        return !query.select().isEmpty();
    }

    /**
     * Method for confirming a given userId has admin privileges
     *
     * @param userId String uniquely identifying a user
     * @return boolean indicating the user is an admin
     */
    @Override
    public boolean isAdmin(String userId) {
        DbQuery query = newQuery();
        query.addWhere("userId", userId);
        query.addWhere("status", User.Status.ADMIN.name());

        return !query.select().isEmpty();
    }

    /**
     * Method for requesting user access
     *
     * @param userId String uniquely identifying a user
     * @return boolean indicating a successful save
     */
    @Override
    public boolean requestAccess(String userId) {
        DbQuery query = newQuery();
        query.setColumnValue("status", User.Status.REQUESTED.name());
        query.addWhere("userId", userId);
        query.addWhere("status", User.Status.NONE.name());

        return query.update();
    }

    /**
     * Method for revoking a user's access
     *
     * @param userId String uniquely identifying a user
     * @return boolean indicating a successful update
     */
    @Override
    public boolean revokeAccess(String userId) {
        DbQuery query = newQuery();
        query.setColumnValue("status", User.Status.NONE.name());
        query.addWhere("userId", userId);
        query.addWhere("status", User.Status.APPROVED.name());

        return query.update();
    }

    /**
     * Method for approving access for a user
     *
     * @param userId String uniquely identifying a user
     * @return boolean indicating a successful update
     */
    @Override
    public boolean grantAccess(String userId) {
        DbQuery query = newQuery();
        query.setColumnValue("status", User.Status.APPROVED.name());
        query.addWhere("userId", userId);
        query.addOrWhere("status", new String[]{ User.Status.NONE.name(), User.Status.REQUESTED.name() });

        return query.update();
    }

    /**
     * Method for retrieving all user records
     *
     * @return List of Users
     */
    @Override
    public List<User> getAllUsers() {
        return parse(newQuery().select());
    }

    /**
     * @return DbQuery object for querying database
     */
    private DbQuery newQuery() {
        DbQuery query = new DbQuery();
        query.setTableName("User");
        return query;
    }

    /**
     * Method for parsing SQL results into List of UserStatus objects
     *
     * @param results data structure representation of sql results
     * @return List of UserStatus objects
     */
    private List<User> parse(ArrayList<HashMap<String, Object>> results) {
        ArrayList<User> users = new ArrayList<>();
        for (HashMap valuesMap: results) {
            User user = new User();
            user.setUserId((String) valuesMap.get("userId"));
            user.setName((String) valuesMap.get("username"));
            user.setEmail((String) valuesMap.get("email"));
            user.setStatus((String) valuesMap.get("status"));
            user.setFound(true);
            users.add(user);
        }
        return users;
    }
}
