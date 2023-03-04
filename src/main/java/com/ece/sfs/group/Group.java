package com.ece.sfs.group;

import org.springframework.security.core.GrantedAuthority;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

public class Group {

    private ArrayList<GrantedAuthority> authorities;
    private String name;
    private HashSet<String> usernames;

    public Group(ArrayList<GrantedAuthority> authorities, String name, HashSet<String> usernames) {
        this.authorities = authorities;
        this.name = name;
        this.usernames = usernames;
    }

    public boolean addAuthority(GrantedAuthority authority) {
        boolean flag = false;

        if (!authorities.contains(authority)) {
            authorities.add(authority);

            flag = true;
        }

        return flag;
    }

    public List<GrantedAuthority> getAuthorities() {
        return new ArrayList<>(authorities);
    }

    public boolean removeAuthority(GrantedAuthority authority) {
        return authorities.remove(authority);
    }

    public boolean addUser(String username) {
        return usernames.add(username);
    }

    public List<String> getUsers() {
        return new ArrayList<>(usernames);
    }

    public boolean hasUser(String username) {
        return usernames.contains(username);
    }

    public boolean removeUser(String username) {
        return usernames.remove(username);
    }

    public void setName(String name) {
        if (name != null && !name.isEmpty()) {
            this.name = name;

            return;
        }

        throw new IllegalArgumentException("Name cannot be null or empty");
    }

    public String getName() {
        return name;
    }
}
