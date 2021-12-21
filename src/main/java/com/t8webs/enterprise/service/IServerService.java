package com.t8webs.enterprise.service;

import com.mashape.unirest.http.exceptions.UnirestException;
import com.t8webs.enterprise.dto.UserAccount;

import java.io.IOException;
import java.sql.SQLException;

public interface IServerService {

    /**
     * @param userAccount UserAccount to assign a server to
     * @param serverName String name to give server
     * @return
     */
    boolean assignUserServer(UserAccount userAccount, String serverName) throws SQLException, IOException, ClassNotFoundException, UnirestException;

    /**
     * @param userAccount UserAccount tied to server
     * @param vmid int uniquely identifying the server
     * @param serverName String to rename the server
     * @return
     */
    boolean renameServer(UserAccount userAccount, int vmid, String serverName) throws SQLException, IOException, ClassNotFoundException;


}
