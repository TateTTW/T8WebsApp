package com.t8webs.enterprise.dto;

import lombok.Data;

@Data
public class Server {
    /**
     * Server's unique identifier
     */
    private int vmid;
    private String name;
    private String username;
    private String ipAddress;
    private boolean isFound;
}
