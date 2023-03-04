package com.ece.sfs.group;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.provisioning.GroupManager;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import static com.ece.sfs.Util.validName;


public class UserGroupManager implements GroupManager {

    private final ArrayList<Group> groups;

    public UserGroupManager(ArrayList<Group> groups) {
        this.groups = groups;
    }

    @Override
    public List<String> findAllGroups() {
        ArrayList<String> groupNames = new ArrayList<>();

        groups.forEach(group -> groupNames.add(group.getName()));

        return groupNames;
    }

    @Override
    public List<String> findUsersInGroup(String groupName) {
        if (validName(groupName)) {
            throw new IllegalArgumentException("Invalid group name");
        }

        if (!hasGroup(groupName)) {
            throw new IllegalArgumentException("Group does not exist");
        }

        for (Group group : groups) {
            if (group.getName().compareTo(groupName) == 0) {
                return group.getUsers();
            }
        }

        return null;
    }

    @Override
    public void createGroup(String groupName, List<GrantedAuthority> authorities)
            throws IllegalArgumentException {
        if (validName(groupName)) {
            throw new IllegalArgumentException("Group name cannot be null or empty");
        }

        if (hasGroup(groupName)) {
            throw new IllegalArgumentException("Group already exists");
        } else {
            groups.add(new Group(new ArrayList<>(authorities), groupName, new HashSet<>()));
        }
    }

    public boolean hasGroup(String groupName) {
        for (Group group : groups) {
            if (group.getName().equals(groupName)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public void deleteGroup(String groupName) throws IllegalArgumentException {
        if (validName(groupName)) {
            throw new IllegalArgumentException("Group name cannot be null or empty");
        }

        if (hasGroup(groupName)) {
            groups.removeIf(group -> group.getName().equals(groupName));
        } else {
            throw new IllegalArgumentException("Group does not exist");
        }
    }

    @Override
    public void renameGroup(String oldName, String newName)
            throws IllegalArgumentException {
        if (validName(oldName) || validName(newName)) {
            throw new IllegalArgumentException("Old group name or new name cannot be null or empty");
        }

        if (hasGroup(oldName) && !hasGroup(newName)) {
            groups.forEach(group -> {
                if (group.getName().equals(oldName)) {
                    group.setName(newName);
                }
            });
        } else {
            throw new IllegalArgumentException("Group does not exist");
        }
    }

    @Override
    public void addUserToGroup(String username, String group)
            throws IllegalArgumentException {
        if (validName(username) || validName(group)) {
            throw new IllegalArgumentException("Username or group name cannot be null or empty");
        }

        if (hasGroup(group)) {
            groups.forEach(g -> {
                if (g.getName().equals(group)) {
                    if (!g.addUser(username)) {
                        throw new IllegalArgumentException("User already exists in group");
                    }
                }
            });
        } else {
            throw new IllegalArgumentException("Group does not exist");
        }
    }

    public boolean isUserInGroup(String username, String groupName) {
        if (validName(username) || validName(groupName)) {
            throw new IllegalArgumentException("Username or group name cannot be null or empty");
        }

        if (hasGroup(groupName)) {
            for (Group group : groups) {
                if (group.getName().equals(groupName)) {
                    return group.hasUser(username);
                }
            }
        } else {
            throw new IllegalArgumentException("Group does not exist");
        }

        return false;
    }

    public List<String> getGroupsForUser(String username) {
        ArrayList<String> userGroups = new ArrayList<>();

        groups.forEach(group -> {
            if (group.hasUser(username)) {
                userGroups.add(group.getName());
            }
        });

        return userGroups;
    }

    @Override
    public void removeUserFromGroup(String username, String groupName)
            throws IllegalArgumentException {
        if (validName(username) || validName(groupName)) {
            throw new IllegalArgumentException("Username or group name cannot be null or empty");
        }

        if (hasGroup(groupName)) {
            groups.forEach(g -> {
                if (g.getName().equals(groupName)) {
                    if (!g.removeUser(username)) {
                        throw new IllegalArgumentException("User does not exist in group");
                    }
                }
            });
        } else {
            throw new IllegalArgumentException("Group does not exist");
        }
    }

    @Override
    public List<GrantedAuthority> findGroupAuthorities(String groupName)
            throws IllegalArgumentException {

        if (validName(groupName)) {
            throw new IllegalArgumentException("Group name cannot be null or empty");
        }

        if (hasGroup(groupName)) {
            for (Group group : groups) {
                if (group.getName().equals(groupName)) {
                    return group.getAuthorities();
                }
            }
        } else {
            throw new IllegalArgumentException("Group does not exist");
        }

        return null;
    }

    @Override
    public void addGroupAuthority(String groupName, GrantedAuthority authority)
            throws IllegalArgumentException {
        if (validName(groupName) || authority == null) {
            throw new IllegalArgumentException("Group name or authority cannot be null or empty");
        }

        if (hasGroup(groupName)) {
            groups.forEach(g -> {
                if (g.getName().equals(groupName)) {
                    if (!g.addAuthority(authority)) {
                        throw new IllegalArgumentException("Authority already exists in group");
                    }
                }
            });
        } else {
            throw new IllegalArgumentException("Group does not exist");
        }
    }

    @Override
    public void removeGroupAuthority(String groupName, GrantedAuthority authority)
            throws IllegalArgumentException {
        if (validName(groupName) || authority == null) {
            throw new IllegalArgumentException("Group name or authority cannot be null or empty");
        }

        if (hasGroup(groupName)) {
            groups.forEach(g -> {
                if (g.getName().equals(groupName)) {
                    if (!g.removeAuthority(authority)) {
                        throw new IllegalArgumentException("Authority does not exist in group");
                    }
                }
            });
        } else {
            throw new IllegalArgumentException("Group does not exist");
        }
    }
}
