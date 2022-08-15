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
    private String creationStatus = CreationStatus.BEGIN.name();
    private boolean isFound;

    public CreationStatus getCreationStatus() {
        return CreationStatus.valueOf(creationStatus);
    }

    public void setCreationStatus(String status) {
        for (CreationStatus s : CreationStatus.values()) {
            if (s.name().equals(status)) {
                this.creationStatus = status;
                break;
            }
        }
    }

    public void setCreationStatus(CreationStatus status) {
        this.creationStatus = status.name();
    }

    public enum CreationStatus {
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
