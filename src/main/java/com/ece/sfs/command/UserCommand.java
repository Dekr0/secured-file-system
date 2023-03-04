package com.ece.sfs.command;

import com.ece.sfs.core.FileSystem;
import com.ece.sfs.io.Cryptography;
import com.ece.sfs.io.IOUtil;
import com.ece.sfs.prompt.ShellPrompt;
import com.ece.sfs.prompt.TerminalColor;
import io.vavr.control.Either;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.provisioning.GroupManager;
import org.springframework.shell.standard.ShellComponent;
import org.springframework.shell.standard.ShellMethod;
import org.springframework.shell.standard.ShellOption;

import static com.ece.sfs.Util.*;
import static com.ece.sfs.io.Cryptography.b64StrToBytes;
import static com.ece.sfs.io.Cryptography.toB64Str;


@ShellComponent
public class UserCommand extends Command {

    private final AuthenticationManager authManager;

    private final Cryptography cryptography;

    private final FileSystem fileSystem;

    private final GroupManager groupManager;

    private final ShellPrompt shellPrompt;

    @Autowired
    public UserCommand(
            AuthenticationManager authManager,
            Cryptography cryptography,
            FileSystem fileSystem,
            GroupManager groupManager,
            ShellPrompt shellPrompt
    ) {
        this.authManager = authManager;
        this.cryptography = cryptography;
        this.fileSystem = fileSystem;
        this.groupManager = groupManager;
        this.shellPrompt = shellPrompt;
    }

    @ShellMethod("ls")
    public void ls() {
        fileSystem.ls().bimap(
                        s -> { shellPrompt.print(s); return null; },
                        s -> { shellPrompt.printError(s); return null; }
        );
    }

    @ShellMethod("cat")
    public void cat(String filename,
                    @ShellOption(defaultValue = "") String content,
                    @ShellOption(defaultValue = "0") int position
    ) {
        if (validFileName(filename)) {
            shellPrompt.printError("Invalid filename");
            return;
        }

        if (position < 0) {
            shellPrompt.printError("Invalid position");
            return;
        }

        try {
            String nameChecksum = toB64Str(cryptography.encrypt(filename.getBytes()));
            Either<String, String> isGetSuccess = IOUtil.getContent(fileSystem.resolveEncryptedPath(nameChecksum));

            if (isGetSuccess.isRight()) {
                shellPrompt.printError(isGetSuccess.get());
                return;
            }

            byte[] currentEncryptedBytes = b64StrToBytes(isGetSuccess.getLeft());
            String currentContent = new String(cryptography.decrypt(currentEncryptedBytes));

            if (position > currentContent.length()) {
                shellPrompt.printError("Invalid position");
                return;
            }

            String newContent = currentContent.substring(0, position) + content + currentContent.substring(position);
            String newContentChecksum = toB64Str(cryptography.encrypt(newContent.getBytes()));

            fileSystem.changeComponent(filename, newContentChecksum).bimap(
                    b -> {
                        IOUtil.modify(fileSystem.resolveEncryptedPath(nameChecksum), newContentChecksum).map(
                                s -> { shellPrompt.printError(s); return null; }
                        );
                        return null;
                    },
                    s -> { shellPrompt.printError(s); return null; }
            );
        } catch (Exception e) {
            shellPrompt.printError("Fatal : cannot encrypt");
        }
    }

    @ShellMethod("display file content")
    public void echo(String filename) {
        if (validFileName(filename)) {
            shellPrompt.printError("Invalid filename");
            return;
        }

        try {
            String nameChecksum = toB64Str(cryptography.encrypt(filename.getBytes()));

            fileSystem.readComponent(filename).bimap(
                    b -> {
                        IOUtil.getContent(fileSystem.resolveEncryptedPath(nameChecksum)).bimap(
                                s -> {
                                    try {
                                        byte[] encryptedBytes = b64StrToBytes(s);
                                        String content = new String(cryptography.decrypt(encryptedBytes));
                                        shellPrompt.print(content);
                                    } catch (Exception e) {
                                        shellPrompt.printError("Fatal : cannot decrypt");
                                    }
                                    return null;
                                },
                                s -> { shellPrompt.printError(s); return null; }
                        );
                        return null;
                    },
                    s -> { shellPrompt.printError(s); return null; }
            );
        } catch (Exception e) {
            shellPrompt.printError("Fatal : cannot encrypt");
        }
    }

    @ShellMethod("cd")
    public void cd(String dirname) {
        if (validFileName(dirname)) {
            shellPrompt.printError("Invalid directory name");
        }

        fileSystem.changeDirectory(dirname).map(
                s -> { shellPrompt.printError(s); return null; }
        );
    }

    @ShellMethod("mkdir")
    public void mkdir(String dirname) {
        if (validFileName(dirname)) {
            shellPrompt.printError("Invalid directory name");
        }

        try {
            String nameChecksum = toB64Str(cryptography.encrypt(dirname.getBytes()));

            fileSystem.createComponent(dirname, nameChecksum, FileSystem.FileType.DIR)
                    .map( s -> { shellPrompt.printError(s); return null; } )
                    .mapLeft(b -> {
                        IOUtil.create(
                                fileSystem.resolveEncryptedPath(nameChecksum), FileSystem.FileType.DIR).map(
                                s -> { shellPrompt.printError(s); return null; }
                        );
                        return null;
                    });
        } catch (Exception e) {
            shellPrompt.printError("Fatal : cannot encrypt");
        }
    }

    @ShellMethod("touch")
    public void touch(String filename) {
        if (validFileName(filename)) {
            shellPrompt.printError("Invalid filename");
        }

        try {
            String nameChecksum = toB64Str(cryptography.encrypt(filename.getBytes()));

            fileSystem.createComponent(filename, nameChecksum, FileSystem.FileType.FILE).map(s -> {
                        shellPrompt.printError(s);
                        return null;
                    }).mapLeft(
                            b -> {
                                IOUtil.create(fileSystem.resolveEncryptedPath(nameChecksum), FileSystem.FileType.FILE).map(
                                        s -> { shellPrompt.printError(s); return null; }
                                );
                                return null;
                            }
                    );
        } catch (Exception e) {
            shellPrompt.printError("Fatal : cannot encrypt");
        }
    }

    @ShellMethod("rename")
    public void rename(String oldFilename, String newFilename) {
        if (validFileName(oldFilename)) {
            shellPrompt.printError("Invalid old filename");
            return;
        }

        if (validFileName(newFilename)) {
            shellPrompt.printError("Invalid new filename");
            return;
        }

        try {
            String oldNameChecksum = toB64Str(cryptography.encrypt(oldFilename.getBytes()));
            String newNameChecksum = toB64Str(cryptography.encrypt(newFilename.getBytes()));

            fileSystem.renameComponent(oldFilename, newFilename, newNameChecksum)
                    .map(s -> { shellPrompt.printError(s); return null; })
                    .mapLeft(
                            b -> {
                                IOUtil
                                        .rename(
                                                fileSystem.resolveEncryptedPath(oldNameChecksum),
                                                fileSystem.resolveEncryptedPath(newNameChecksum))
                                        .map(s -> { shellPrompt.printError(s); return null; });
                                return null;
                            }
                    );
        } catch (Exception e) {
            shellPrompt.printError("Fatal : cannot encrypt");
        }
    }

    @ShellMethod("rm")
    public void rm(String filename) {
        if (validFileName(filename)) {
            shellPrompt.printError("Invalid filename");
        }

        fileSystem.deleteComponent(filename)
                .map(s -> { shellPrompt.printError(s); return null; })
                .mapLeft(
                        b -> {
                            try {
                                byte[] encryptBytes = cryptography.encrypt(filename.getBytes());

                                IOUtil.delete(fileSystem.resolveEncryptedPath(toB64Str(encryptBytes)))
                                        .map(s -> { shellPrompt.printError(s); return null; });
                            } catch (Exception e) {
                                shellPrompt.printError(e.getMessage());
                            }
                            return null;
                        }
                );
    }

    @ShellMethod("login as user")
    public void login(String username, String groupName, String password) {
        if (validName(username)) {
            shellPrompt.printError("Invalid username");
            return;
        }

        if (validName(groupName)) {
            shellPrompt.printError("Invalid group name");
            return;
        }

        if (validPassword(password)) {
            shellPrompt.printError("Invalid password");
            return;
        }

        if (!groupManager.findAllGroups().contains(groupName)) {
            shellPrompt.printError("Group name " + groupName + " does not exist");
            return;
        }

        if (!groupManager.findUsersInGroup(groupName).contains(username)) {
            shellPrompt.printError("User " + username + " does not in Group " + groupName);
            return;
        }

        Authentication rq = new UsernamePasswordAuthenticationToken(username, password);
        try {
            Authentication r = authManager.authenticate(rq);
            SecurityContextHolder.getContext().setAuthentication(r);
            shellPrompt.print("Logged in as " + username + " in Group " + groupName, TerminalColor.GREEN);

            fileSystem.loginHome(groupName, username).bimap(
                    s -> {
                        if (!s.isEmpty()) {
                            shellPrompt.printWarning("Warning : your file " + s + " was maliciously modified");
                        }
                        return null;
                    },
                    s -> {
                        shellPrompt.printError(s);
                        SecurityContextHolder.clearContext();
                        return null;
                    }
            );
        } catch (AuthenticationException e) {
            shellPrompt.printError("Auth failed. Either username " + username +
                    " does not exist or password is incorrect");
        }
    }

    @ShellMethod("Logout")
    public void logout() {
        SecurityContextHolder.clearContext();
    }
}
