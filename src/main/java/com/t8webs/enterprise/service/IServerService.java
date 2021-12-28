package com.t8webs.enterprise.service;

import com.mashape.unirest.http.exceptions.UnirestException;

import java.io.IOException;
import java.sql.SQLException;

public interface IServerService {

    /**
     * @param username String user to assign a server to
     * @param serverName String name to give server
     * @return
     */
    boolean assignUserServer(String username, String serverName) throws SQLException, IOException, ClassNotFoundException, UnirestException;

    /**
     * @param username String user assigned to server
     * @param vmid int uniquely identifying the server
     * @param serverName String to rename the server
     * @return
     */
    boolean renameServer(String username, int vmid, String serverName) throws SQLException, IOException, ClassNotFoundException;


}
