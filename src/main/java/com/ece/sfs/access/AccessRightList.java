package com.ece.sfs.access;

import java.util.ArrayList;
import java.util.HashMap;


public class AccessRightList {
    private final HashMap<String, ArrayList<AccessRight>> userAccessRightList;
    private final HashMap<String, ArrayList<AccessRight>> groupAccessRightList;

    public AccessRightList(
            HashMap<String, ArrayList<AccessRight>> userAccessRightList,
            HashMap<String, ArrayList<AccessRight>> groupAccessRightList
    ) {
        this.userAccessRightList = userAccessRightList;
        this.groupAccessRightList = groupAccessRightList;
    }

    public void addUserAccessRight(String username, ArrayList<AccessRight> newAccessRightsUser) {
        if (!userAccessRightList.containsKey(username)) {
            userAccessRightList.put(username, new ArrayList<>(newAccessRightsUser));
        } else {
            for (AccessRight newAccessRight : newAccessRightsUser) {
                if (!userAccessRightList.get(username).contains(newAccessRight)) {
                    userAccessRightList.get(username).add(newAccessRight);
                }
            }
        }
    }

    public void addGroupAccessRight(String groupName, ArrayList<AccessRight> newAccessRightsGroup) {
        if (!groupAccessRightList.containsKey(groupName)) {
            groupAccessRightList.put(groupName, new ArrayList<>(newAccessRightsGroup));
        } else {
            for (AccessRight newAccessRight : newAccessRightsGroup) {
                if (!groupAccessRightList.get(groupName).contains(newAccessRight)) {
                    groupAccessRightList.get(groupName).add(newAccessRight);
                }
            }
        }
    }

    public boolean hasUserAccessRight(String username, AccessRight accessRight) {
        return userAccessRightList.containsKey(username) && userAccessRightList.get(username).contains(accessRight);
    }

    public boolean hasGroupAccessRight(String groupName, AccessRight accessRight) {
        return groupAccessRightList.containsKey(groupName) && groupAccessRightList.get(groupName).contains(accessRight);
    }
}
