package com.ece.sfs.core;


import io.vavr.control.Either;

import java.util.List;

public abstract class Component {

    /*
        TODO:
            Integrity for file
                - Filename
                - File content
                - What if there are no content written in the file
            Integrity for directory
                - Integrity of every file and directory under current directory
                - How to ensure there's no file or directory are removed: Compared the saved stated when the current state
                    - 1. Check number of components under current directory
                    - 2. Store every encrypted (non-encrypted ?) filename, and check if they are still in there
     */

    /* The following methods are not supported for File class */

    public Either<Boolean, String> addComponent(Component component) {
        throw new UnsupportedOperationException("Not implemented");
    }

    public Either<Component, String> getComponent(String name) {
        throw new UnsupportedOperationException("Not implemented");
    }

    public boolean hasComponent(String name) {
        throw new UnsupportedOperationException("Not implemented");
    }

    public Either<Boolean, String> removeComponent(String name) {
        throw new UnsupportedOperationException("Not implemented");
    }

    public List<Component> getComponents() {
        throw new UnsupportedOperationException("Not implemented");
    }

    /* ------------------------------------------------------ */

    public String getDate() {
        throw new UnsupportedOperationException("Unsupported operation: cannot " +
                "get modified date of a file system component");
    }

    public void setDate(Object date) {
        throw new UnsupportedOperationException("Unsupported operation: cannot " +
                "set modified date of a file system component");
    }

    public Component getParent() {
        throw new UnsupportedOperationException("Not implemented");
    }

    public void setParent(Directory parent) {
        throw new UnsupportedOperationException("Not implemented");
    }

    public String getName() {
        throw new UnsupportedOperationException("Unsupported operation: cannot " +
                "get name of a file system component");
    }

    public Either<Boolean, String> setName(String name) {
        throw new UnsupportedOperationException("Unsupported operation: cannot " +
                "set name of a file system component");
    }

    public String getUUID(){
        throw new UnsupportedOperationException("Not implemented");
    }

    public Either<Void, String> setNameChecksum (String checksum) {
        throw new UnsupportedOperationException("Not implemented");
    }

    public String getNameChecksum() {
        throw new UnsupportedOperationException("Not implemented");
    }

    public Either<Void, String> setLengthChecksum(long checksum) {
        throw new UnsupportedOperationException("Not implemented");
    }

    public Either<Void, String> setContentChecksum(String checksum) {
        throw new UnsupportedOperationException("Not implemented");
    }

    public boolean checkNameChecksum(String checksum) {
        throw new UnsupportedOperationException("Not implemented");
    }

    public boolean checkLengthChecksum(long checksum) {
        throw new UnsupportedOperationException("Not implemented");
    }

    public boolean checkContentChecksum(String checksum) {
        throw new UnsupportedOperationException("Not implemented");
    }
}
