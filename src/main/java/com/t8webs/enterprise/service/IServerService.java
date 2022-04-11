package com.t8webs.enterprise.service;


import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.t8webs.enterprise.utils.ProxmoxUtil;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.sql.SQLException;


public interface IServerService {

    /**
     * @param username    String user to assign a server to
     * @param serverName  String name to give server
     * @return
     */
    ObjectNode assignUserServer(String username, String serverName) throws SQLException, IOException, ClassNotFoundException, ProxmoxUtil.InvalidVmStateException;

    /**
     * @param username    String user assigned to server
     * @param vmid        int uniquely identifying the server
     * @param serverName  String to rename the server
     * @return
     */
    boolean renameServer(String username, int vmid, String serverName) throws SQLException, IOException, ClassNotFoundException;

    ArrayNode getUserServers(String username) throws SQLException, IOException, ClassNotFoundException;

    boolean deployBuild(String username, int vmid, MultipartFile buildFile) throws SQLException, IOException, ClassNotFoundException;

    boolean startVM(String username, int vmid) throws SQLException, IOException, ClassNotFoundException, ProxmoxUtil.InvalidVmStateException;

    boolean shutdownVM(String username, int vmid) throws SQLException, IOException, ClassNotFoundException, ProxmoxUtil.InvalidVmStateException;

    boolean rebootVM(String username, int vmid) throws SQLException, IOException, ClassNotFoundException, ProxmoxUtil.InvalidVmStateException;

    boolean deleteVM(String username, int vmid) throws SQLException, IOException, ClassNotFoundException, ProxmoxUtil.InvalidVmStateException;

    String getVmStatus(String username, int vmid) throws SQLException, IOException, ClassNotFoundException;
}

