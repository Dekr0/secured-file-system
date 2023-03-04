package com.ece.sfs.core;

import java.util.*;

import com.ece.sfs.Util;
import io.vavr.control.Either;

import static com.ece.sfs.Util.validFileName;


public class Directory extends Component {

    private Directory parent = null;
    private Date lastModifiedDate;
    private String name = "";

    private final String uuid;

    private String nameChecksum;

    private final HashMap<String, Component> components;

    public Directory(
            String name,
            String nameChecksum,
            Directory parent,
            Date lastModifiedDate,
            UUID uuid,
            HashMap<String, Component> components
    ) {
        this.uuid = uuid.toString();
        this.components = components;

        setName(name);
        setNameChecksum(nameChecksum);
        setParent(parent);
        setDate(lastModifiedDate);
    }

    @Override
    public Either<Boolean, String> addComponent(Component component) {
        if (component == null) {
            return Either.right("Adding a null object into the directory");
        }

        components.put(component.getName(), component);

        return Either.left(true);
    }

    @Override
    public Either<Component, String> getComponent(String name) {
        if (hasComponent(name)) {
            return Either.left(components.get(name));
        }

        return Either.right("No such file or directory: " + name);
    }

    @Override
    public List<Component> getComponents() {
        return new ArrayList<>(components.values());
    }

    @Override
    public boolean hasComponent(String name) {
        return components.containsKey(name);
    }

    @Override
    public Either<Boolean, String> removeComponent(String name) {
        if (components.remove(name) == null) {
            return Either.right("File or Directory" + name + "does not exist");
        }

        return Either.left(true);
    }

    @Override
    public Component getParent() {
        return parent;
    }

    @Override
    public void setParent(Directory parent) {
        if (name.compareTo("/") == 0) {
            if (parent != null) {
                throw new IllegalArgumentException("/ directory does not have a parent directory");
            }
        } else {
            if (parent == null) {
                throw new IllegalArgumentException("Parent directory cannot be null");
            }

            this.parent = parent;
        }
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public Either<Boolean, String> setName(String name) {
        if (validFileName(name)) {
            return Either.right("Invalid file name");
        }

        this.name = name;

        return Either.left(true);
    }

    @Override
    public String getDate() {
        return Util.dateToString(lastModifiedDate, "dd/MM/yyyy");
    }

    @Override
    public void setDate(Object date) {
        if (date == null) {
            throw new IllegalArgumentException("Invalid date");
        }

        if (date instanceof Date) {
            this.lastModifiedDate = (Date) date;
        }
    }

    @Override
    public Either<Void, String> setNameChecksum(String nameChecksum) {
        this.nameChecksum = nameChecksum;

        return Either.left(null);
    }

    @Override
    public String getNameChecksum() {
        return nameChecksum;
    }

    @Override
    public boolean checkNameChecksum(String checksum) {
        return nameChecksum.compareTo(checksum) == 0;
    }

    @Override
    public boolean checkLengthChecksum(long checksum) {
        return components.size() == checksum;
    }

    @Override
    public String getUUID() {
        return uuid;
    }
}
