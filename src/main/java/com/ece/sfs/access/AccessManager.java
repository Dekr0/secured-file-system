package com.ece.sfs.access;

import com.ece.sfs.group.UserGroupManager;
import io.vavr.control.Either;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import static com.ece.sfs.Util.validName;
import static com.ece.sfs.Util.validUUID;


public class AccessManager {

    private final UserGroupManager userGroupManager;

    private HashMap<String, AccessRightList> accessRightListMap;

    public AccessManager(UserGroupManager userGroupManager) {
        accessRightListMap = new HashMap<>();

        this.userGroupManager = userGroupManager;
    }

    public Either<Void, String> addUserAccessRight(String username, String uuid, ArrayList<AccessRight> newAccessRightsUser) {
        if (validName(username)) {
            return Either.right("Username is not valid");
        }

        if (validUUID(uuid)) {
            return Either.right("UUID cannot be empty");
        }

        if (!accessRightListMap.containsKey(uuid)) {
            accessRightListMap.put(uuid, new AccessRightList(
                    new HashMap<>(Map.of(username, newAccessRightsUser)),
                    new HashMap<>()
            ));
        } else {
            accessRightListMap.get(uuid).addUserAccessRight(username, newAccessRightsUser);
        }

        return Either.left(null);
    }

    public Either<Void, String> addGroupAccessRight(String groupName, String uuid, ArrayList<AccessRight> newAccessRightsGroup) {
        if (validName(groupName)) {
            return Either.right("Group name is not valid");
        }

        if (validUUID(uuid)) {
            return Either.right("UUID cannot be empty");
        }

        if (!accessRightListMap.containsKey(uuid)) {
            accessRightListMap.put(uuid, new AccessRightList(
                    new HashMap<>(),
                    new HashMap<>(Map.of(groupName, newAccessRightsGroup))
            ));
        } else {
            accessRightListMap.get(uuid).addGroupAccessRight(groupName, newAccessRightsGroup);
        }

        return Either.left(null);
    }

    public Either<Void, String> addAccessRightList(String uuid, AccessRightList accessRightList) {
        if (validUUID(uuid)) {
            return Either.right("UUID cannot be null or empty");
        }

        if (accessRightList == null) {
            return Either.right("AccessRightList cannot be null");
        }

        if (accessRightListMap.containsKey(uuid)) {
            return Either.right("Component with UUID " + uuid + " already has an access right list");
        } else {
            accessRightListMap.put(uuid, accessRightList);
        }

        return Either.left(null);
    }

    public Either<Boolean, String> hasAccessRight(
            String groupName, String username, String uuid, AccessRight accessRight) {

        if (validName(username)) {
            return Either.right("Username is not valid");
        }

        if (validName(groupName)) {
            return Either.right("Group name cannot be empty");
        }

        if (validUUID(uuid)) {
            return Either.right("UUID cannot be empty");
        }

        if (!accessRightListMap.containsKey(uuid)) {
            return Either.right("Component with UUID " + uuid + " does not have an access right list");
        }

        if (accessRightListMap.get(uuid).hasUserAccessRight(username, accessRight)) {
            return Either.left(true);
        }

        for (String group : userGroupManager.getGroupsForUser(username)) {
                if (accessRightListMap.get(uuid).hasGroupAccessRight(group, accessRight)) {
                    return Either.left(true);
                }
        }

        return Either.left(false);
    }
}
