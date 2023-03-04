package com.ece.sfs.access;


import org.springframework.stereotype.Component;

@Component
public class SessionManager {

    private String username;

    private String groupName;


    public void setUsername(String username) {
        this.username = username;
    }

    public String getUsername() {
        return username;
    }

    public void setGroupName(String groupName) {
        this.groupName = groupName;
    }

    public String getGroupName() {
        return groupName;
    }
}
