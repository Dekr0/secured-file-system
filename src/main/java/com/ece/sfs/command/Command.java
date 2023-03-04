package com.ece.sfs.command;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.shell.Availability;
import org.springframework.shell.standard.ShellMethodAvailability;

public abstract class Command {

    private GrantedAuthority adminAuthority;

    @ShellMethodAvailability({"cat", "cd", "ls", "mkdir", "rename", "touch"})
    public Availability isUserSignedIn() {
        Authentication auth = SecurityContextHolder
                .getContext()
                .getAuthentication();

        if (!(auth instanceof UsernamePasswordAuthenticationToken)) {
            return Availability.unavailable("You must be signed in to use this command");
        }

        return Availability.available();
    }

    @ShellMethodAvailability({"groupadd", "groupdel", "useradd", "userdel", "usermod"})
    public Availability isAdminSignedIn() {
        Authentication auth = SecurityContextHolder
                .getContext()
                .getAuthentication();

        if (!(auth instanceof UsernamePasswordAuthenticationToken)) {
            return Availability.unavailable("You must be signed in to use this command");
        }

        if (!auth.getAuthorities().contains(adminAuthority)) {
            return Availability.unavailable("You must be an admin to use this command");
        }

        return Availability.available();
    }

    @Autowired
    public void setAdminAuthority(GrantedAuthority adminAuthority) {
        this.adminAuthority = adminAuthority;
    }
}
