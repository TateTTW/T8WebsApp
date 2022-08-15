package com.t8webs.enterprise.dto;

import lombok.Data;

@Data
public class Server {
    /**
     * Server's unique identifier
     */
    private int vmid;
    private String name = "";
    private String username = "";
    private String ipAddress = "";
    private String dnsId = "";
    private CreationStatus creationStatus = CreationStatus.NONE;
    private boolean isFound;

    public CreationStatus getCreationStatus() {
        return creationStatus;
    }

    public void setCreationStatus(String status) {
        for (CreationStatus creationStatus : CreationStatus.values()) {
            if (creationStatus.name().equals(status)) {
                this.creationStatus = creationStatus;
                break;
            }
        }
    }

    public void setCreationStatus(CreationStatus status) {
        creationStatus = status != null ? status : CreationStatus.NONE;
    }

    @Override
    public String toString() {
        return "Server { vmid: " + vmid + " name: " + name + " creationStatus: " + creationStatus + " }";
    }

    public enum CreationStatus {
        NONE,
        BEGIN,
        VERIFIED_NAME,
        ASSIGNED,
        HAS_DNS_RECORD,
        SAVED_DNS_ID,
        HAS_VM,
        HAS_PROXY_CFG,
        COMPLETED
    }
}
