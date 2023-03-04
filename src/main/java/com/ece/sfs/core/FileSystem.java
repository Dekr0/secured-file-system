package com.ece.sfs.core;

import com.ece.sfs.io.IOUtil;
import com.ece.sfs.access.AccessManager;
import com.ece.sfs.access.AccessRight;
import com.ece.sfs.access.AccessRightList;
import io.vavr.control.Either;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.*;

import static com.ece.sfs.Util.validFileName;


@org.springframework.stereotype.Component
public class FileSystem {

    public enum FileType {
        FILE, DIR
    }

    private String username;
    private String groupName;

    private Directory currentDirectory;
    private final Directory root;
    private final AccessManager accessManager;

    @Autowired
    public FileSystem(AccessManager accessManager, Directory root) {
        this.accessManager = accessManager;
        this.root = root;
    }

    private static String getAbsolutePath(Component c, String name, boolean encrypt) {
        StringBuilder builder = new StringBuilder();

        Directory current = (Directory) c.getParent();
        while (current.getName().compareTo("/") != 0) {
            builder.insert(0, "/" + (encrypt ? current.getNameChecksum() : current.getName()));
            current = (Directory) current.getParent();
        }

        return builder.append("/").append(name).toString();
    }

    private Either<String, String> integrityCheck(Directory start) {
        for (Component c : start.getComponents()) {
            String nameChecksum = c.getNameChecksum();
            String absolutePath = getAbsolutePath(c, nameChecksum, true);
            String relativePath = getAbsolutePath(c, c.getName(), false);

            if (!IOUtil.exist(absolutePath)) {
                return Either.left(relativePath);
            }

            if (c instanceof File) {
                if (!c.checkLengthChecksum(IOUtil.getContentLength(absolutePath))) {
                    return Either.left(relativePath);
                }

                Either<String, String> contentChecksum = IOUtil.getContent(absolutePath);
                if (contentChecksum.isRight()) {
                    return Either.right(contentChecksum.get());
                }

                if (!c.checkContentChecksum(contentChecksum.getLeft())) {
                    return Either.left(relativePath);
                }
            } else {
                if (!c.checkLengthChecksum(IOUtil.getNumFiles(absolutePath))) {
                    return Either.left(relativePath);
                }

                Either<String, String> subDirectoryIntegrity = integrityCheck((Directory) c);
                if (subDirectoryIntegrity.isRight()) {
                    return Either.right(subDirectoryIntegrity.get());
                } else if (!subDirectoryIntegrity.getLeft().isEmpty()) {
                    return Either.left(subDirectoryIntegrity.getLeft());
                }
            }
        }

        return Either.left("");
    }

    public Either<Boolean, String> changeDirectory(String name) {
        Directory destination;

        if (name.compareTo("..") == 0) {
            destination = (Directory) currentDirectory.getParent();
        } else {
            Either<Component, String> hasComponent = currentDirectory.getComponent(name);

            if (hasComponent.isRight()) {
                return Either.right(hasComponent.get());
            }

            if (hasComponent.getLeft() instanceof Directory) {
                destination = (Directory) hasComponent.getLeft();
            } else {
                return Either.right(name + "Not a directory");
            }
        }

        Either<Boolean, String> hasAccess = accessManager
                .hasAccessRight(groupName, username, destination.getUUID(), AccessRight.READ);

        if (hasAccess.isLeft()) {
            if (hasAccess.getLeft()) {
                currentDirectory = destination;
            } else {
                return Either.right("You do not have access rights to this directory");
            }

            return Either.left(hasAccess.getLeft());
        }

        return Either.right(hasAccess.get());
    }

    public String getCurrentPath() {
        StringBuilder currentPath = new StringBuilder();

        Directory current = currentDirectory;
        while (current.getName().compareTo("/") != 0) {
            currentPath.insert(0, "/" + current.getName());
            current = (Directory) current.getParent();
        }

        return currentPath.toString();
    }

    public Either<Boolean, String> changeComponent(String name, String contentChecksum) {
        Either<Component, String> hasComponent = currentDirectory.getComponent(name);

        if (hasComponent.isRight()) {
            return Either.right(hasComponent.get());
        }

        if (hasComponent.getLeft() instanceof Directory) {
            return Either.right("Cannot apply on a directory");
        }

        Either<Boolean, String> hasAccess = accessManager.hasAccessRight(
                groupName, username, hasComponent.getLeft().getUUID(), AccessRight.WRITE
        );

        if (hasAccess.isRight()) {
            return Either.right(hasAccess.get());
        }

        if (hasAccess.getLeft()) {
            hasComponent.getLeft().setLengthChecksum(contentChecksum.length());
            hasComponent.getLeft().setContentChecksum(contentChecksum);

            return Either.left(hasAccess.getLeft());
        }

        return Either.right("You do not have permission to modify this file");
    }

    public Either<Boolean, String> createComponent(String name, String nameChecksum, FileType type) {
        if (validFileName(name)) {
            return Either.right("Name cannot be empty");
        }

        Either<Boolean, String> hasAccess = accessManager.hasAccessRight(
                groupName,
                username,
                currentDirectory.getUUID(),
                AccessRight.WRITE
        );

        if (currentDirectory.hasComponent(name)) {
            return Either.right("Component " + name + " already exists");
        }

        if (hasAccess.isLeft()) {
            if (hasAccess.getLeft()) {
                if (type == FileType.FILE) {
                    currentDirectory.addComponent(
                            new File(
                                    name,
                                    nameChecksum,
                                    currentDirectory,
                                    Calendar.getInstance().getTime(),
                                    UUID.randomUUID()
                            )
                    );
                } else if (type == FileType.DIR) {
                    currentDirectory.addComponent(
                            new Directory(
                                    name,
                                    nameChecksum,
                                    currentDirectory,
                                    Calendar.getInstance().getTime(),
                                    UUID.randomUUID(),
                                    new HashMap<>()
                            )
                    );
                }

                accessManager.addAccessRightList(
                        currentDirectory.getComponent(name).getLeft().getUUID(),
                        new AccessRightList(
                                AccessRight.defaultAR(username),
                                AccessRight.defaultRead(groupName)
                        )
                );
            } else {
                return Either.right("You do not have permission to create a component in this directory");
            }

            return Either.left(hasAccess.getLeft());
        }

        return Either.right(hasAccess.get());
    }

    public Either<Boolean, String> deleteComponent(String name) {
        Either<Component, String> hasComponent = currentDirectory.getComponent(name);

        if (hasComponent.isRight()) {
            return Either.right(hasComponent.get());
        }

        Either<Boolean, String> hasAccess = accessManager
                .hasAccessRight(groupName, username, hasComponent.getLeft().getUUID(), AccessRight.WRITE);

        if (hasAccess.isLeft()) {
            if (hasAccess.getLeft()) {
                currentDirectory.removeComponent(name);
            } else {
                return Either.right("You do not have permission to delete this component");
            }

            return Either.left(hasAccess.getLeft());
        }

        return Either.right(hasAccess.get());
    }

    public Either<String, String> ls() {
        Either<Boolean, String> hasAccess = accessManager
                .hasAccessRight(groupName, username, currentDirectory.getUUID(), AccessRight.READ);

        String output = "";

        if (hasAccess.isLeft()) {
            if (hasAccess.getLeft()) {
                for (Component c : currentDirectory.getComponents()) {
                    Either<Boolean, String> own = accessManager
                            .hasAccessRight(groupName, username, c.getUUID(), AccessRight.OWN);
                    if (own.isLeft()) {
                        output += own.getLeft() ? c.getName() + "\n" : c.getUUID() + "\n";
                    } else {
                        return Either.right(own.get());
                    }
                }
            } else {
                return Either.right("You do not have permission to list this directory");
            }
        }

        return Either.left(output);
    }

    public Either<Boolean, String> readComponent(String filename) {
        Either<Component, String> hasComponent = currentDirectory.getComponent(filename);
        if (hasComponent.isRight()) {
            return Either.right(hasComponent.get());
        }

        Either<Boolean, String> owned = accessManager
                .hasAccessRight(groupName, username, hasComponent.getLeft().getUUID(), AccessRight.OWN);
        Either<Boolean, String> hasAccessRead = accessManager
                .hasAccessRight(groupName, username, hasComponent.getLeft().getUUID(), AccessRight.READ);
        if (hasAccessRead.isLeft() && owned.isLeft()) {
            return hasAccessRead.getLeft() && owned.getLeft()
                    ? Either.left(true)
                    : Either.right("You do not have permission to read this file");
        }

        return Either.right(hasAccessRead.get());
    }

    public Either<Boolean, String> renameComponent(String oldName, String newName, String nameChecksum) {
        Either<Component, String> hasComponent = currentDirectory.getComponent(oldName);

        if (hasComponent.isRight()) {
            return Either.right(hasComponent.get());
        }

        Either<Boolean, String> hasAccess = accessManager
                .hasAccessRight(groupName, username, hasComponent.getLeft().getUUID(), AccessRight.WRITE);

        if (hasAccess.isLeft()) {
            if (hasAccess.getLeft()) {
                hasComponent.getLeft().setName(newName);
                hasComponent.getLeft().setNameChecksum(nameChecksum);
                currentDirectory.removeComponent(oldName);
                currentDirectory.addComponent(hasComponent.getLeft());
            } else {
                return Either.right("You do not have permission to rename " + oldName);
            }

            return Either.left(hasAccess.getLeft());
        }

        return Either.right(hasAccess.get());
    }

    public void setGroupName(String groupName) throws IllegalArgumentException {
        if (validFileName(groupName)) {
            throw new IllegalArgumentException("Group name cannot be empty");
        } else {
            this.groupName = groupName;
        }
    }

    public void setUsername(String username) throws IllegalArgumentException {
        if (validFileName(username)) {
            throw new IllegalArgumentException("Username cannot be empty");
        } else {

            this.username = username;
        }
    }

    public Either<String, String> loginHome(String groupName, String username) throws IllegalArgumentException {
        setUsername(username);
        setGroupName(groupName);

        currentDirectory = (Directory) root.getComponent("home").getLeft();
        boolean hasUserDir = currentDirectory.hasComponent(username);
        if (!hasUserDir) {
            currentDirectory.addComponent(
                    new Directory(
                            username,
                            username,
                            currentDirectory,
                            Calendar.getInstance().getTime(),
                            UUID.randomUUID(),
                            new HashMap<>()
                    )
            );

            accessManager.addAccessRightList(
                    currentDirectory.getComponent(username).getLeft().getUUID(),
                    new AccessRightList(
                            AccessRight.defaultAR(username),
                            AccessRight.defaultRead(groupName)
                    )
            );
        }

        currentDirectory = (Directory) currentDirectory.getComponent(username).getLeft();

        if (!hasUserDir) {
            Either<Boolean, String> isCreated = IOUtil.create(getCurrentPath(), FileType.DIR);
            if (isCreated.isRight()) {
                return Either.right(isCreated.get());
            }
        }

        if (!hasUserDir) {
            return Either.left("");
        }

        Either<String, String> integrity = integrityCheck(currentDirectory);
        if (integrity.isRight()) {
            return Either.right(integrity.get());
        }

        return Either.left(integrity.getLeft());
    }

    public String resolvePath(String filename) {
        return getCurrentPath() + "/" + filename;
    }

    public String resolveEncryptedPath(String nameChecksum) {
        StringBuilder currentPath = new StringBuilder();

        Directory current = currentDirectory;
        while (current.getName().compareTo("/") != 0) {
            currentPath.insert(0, "/" + current.getNameChecksum());
            current = (Directory) current.getParent();
        }

        return currentPath + "/" + nameChecksum;
    }
}
