package com.t8webs.enterprise.service;


import com.fasterxml.jackson.databind.node.ArrayNode;
import com.t8webs.enterprise.dto.Server;
import com.t8webs.enterprise.utils.ProxmoxUtil.ProxmoxUtil;
import kong.unirest.json.JSONObject;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;


public interface IServerService {

    /**
     * @param userId      String user to assign a server to
     * @param serverName  String name to give server
     * @return CreationStatus status that was reached before an error occurred
     */
    Server.CreationStatus addServer(String userId, String serverName);

    /**
     * @param userId      String user assigned to server
     * @param vmid        int uniquely identifying the server
     * @param serverName  String to rename the server
     * @return
     */
    boolean renameServer(String userId, int vmid, String serverName);

    ArrayNode getAllServers();

    ArrayNode getUserServers(String userId);

    boolean deployBuild(String userId, int vmid, MultipartFile buildFile) throws IOException;

    boolean startVM(String userId, int vmid) throws ProxmoxUtil.InvalidVmStateException;

    boolean shutdownVM(String userId, int vmid) throws ProxmoxUtil.InvalidVmStateException;

    boolean rebootVM(String userId, int vmid) throws ProxmoxUtil.InvalidVmStateException;

    boolean deleteVM(String userId, int vmid);

    /**
     * Method for Admins to force delete a server
     *
     * @param vmid int uniquely identifying the server
     * @return boolean indicating a successful delete
     */
    boolean forceDeleteVM(int vmid);

    String getVmStatus(String userId, int vmid);

    JSONObject getVmData(String userId, int vmid, ProxmoxUtil.TimeFrame timeFrame);
}

