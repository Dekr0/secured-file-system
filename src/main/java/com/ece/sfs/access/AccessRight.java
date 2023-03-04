package com.ece.sfs.access;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public enum AccessRight {
    EXECUTE,
    OWN,
    READ,
    WRITE;

    public static HashMap<String, ArrayList<AccessRight>> defaultAR(String name) {
        return new HashMap<>(Map.of(name, new ArrayList<>(List.of(OWN, READ, WRITE, EXECUTE))));
    }

    public static ArrayList<AccessRight> defaultAR() {
        return new ArrayList<>(List.of(OWN, READ, WRITE, EXECUTE));
    }

    public static HashMap<String, ArrayList<AccessRight>> defaultRead(String name) {
        return new HashMap<>(Map.of(name, new ArrayList<>(List.of(READ))));
    }

    public static HashMap<String, ArrayList<AccessRight>> defaultWrite(String name) {
        return new HashMap<>(Map.of(name, new ArrayList<>(List.of(WRITE))));
    }

    public static HashMap<String, ArrayList<AccessRight>> defaultExecute(String name) {
        return new HashMap<>(Map.of(name, new ArrayList<>(List.of(EXECUTE))));
    }
}
