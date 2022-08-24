package com.t8webs.enterprise.dto;

import lombok.Data;

@Data
public class User {
    String userId;
    String name;
    String email;
    Status status = Status.NONE;
    boolean isFound;

    public void setStatus(String status) {
        for (Status s : Status.values()) {
            if (s.name().equals(status)) {
                this.status = s;
                break;
            }
        }
    }

    public void setStatus(Status status) {
        this.status = status != null ? status : Status.NONE;
    }

    public boolean isAdmin() {
        return status == Status.ADMIN;
    }

    public boolean isApproved() {
        return status == Status.ADMIN || status == Status.APPROVED;
    }

    @Override
    public String toString() {
        return "userId: " + userId + " name: " + name + " email: " + email + " status: " + status;
    }

    public enum Status {
        NONE,
        REQUESTED,
        APPROVED,
        ADMIN
    }
}

