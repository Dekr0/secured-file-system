package com.ece.sfs.io;

import io.vavr.control.Either;
import org.apache.commons.io.FileUtils;
import org.springframework.stereotype.Component;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.Objects;

import com.ece.sfs.core.FileSystem.FileType;


@Component
public class IOUtil {

    private static final File TMP = new File(System.getProperty("java.io.tmpdir"));

    private static final File APPDATA = new File(TMP, "appdata");

    private static final File ETC = new File(APPDATA, "etc");

    private static final File FSTAB = new File(ETC, "fstab");

    public static Either<Boolean, String> init() {
        if (!APPDATA.exists()) {
            if (!APPDATA.mkdir()) {
                return Either.right("Fatal : cannot create root directory");
            }
        }

        if (!ETC.exists()) {
            if (!ETC.mkdir()) {
                return Either.right("Fatal : cannot create sys directory");
            }

            try {
                FSTAB.createNewFile();
            } catch (IOException e) {
                return Either.right("Fatal : cannot create file system table");
            }

        }

        return create("/home", FileType.DIR);
    }

    public static Either<Boolean, String> create(String logicPath, FileType type) {
        File reflection = new File(APPDATA, resolvePath(logicPath));
        if (type == FileType.FILE) {
            try {
                return Either.left(reflection.createNewFile());
            } catch (IOException e) {
                return Either.right("IOError : cannot create file");
            }
        } else if (type == FileType.DIR) {
            if (reflection.exists()) {
                return Either.left(false);
            }

            return reflection.mkdir()
                    ? Either.left(true)
                    : Either.right("IOError : cannot create directory");
        }

        return Either.right("Invalid file type");
    }

    public static Either<Void, String> delete(String logicalPath) {
        File reflection = new File(APPDATA, resolvePath(logicalPath));
        if (reflection.exists()) {
            return reflection.delete() ? Either.left(null) : Either.right("IOError : cannot delete " + logicalPath);
        }

        return Either.right(logicalPath + "does not exists");
    }

    public static Either<Void, String> rename(String oldLogicalPath, String newLogicalPath) {
        File oldReflection = new File(APPDATA.getAbsolutePath(), resolvePath(oldLogicalPath));
        File newReflection = new File(APPDATA.getAbsolutePath(), resolvePath(newLogicalPath));
        if (oldReflection.exists()) {
            if (!newReflection.exists()) {
                    return oldReflection.renameTo(newReflection) ?
                            Either.left(null) :
                            Either.right("IOError : cannot rename" + oldLogicalPath + " to " + newLogicalPath);
            }

            return Either.right("IOError : " + newLogicalPath + "exists");
        }

        return Either.right("File " + oldLogicalPath + "does not exist");
    }

    public static Either<Void, String> modify(String logicalPath, String content) {
        File reflection = new File(APPDATA, resolvePath(logicalPath));

        if (!reflection.exists()) {
            return Either.right("File " + logicalPath + "does not exist");
        }

        try (RandomAccessFile writer = new RandomAccessFile(reflection, "rw")) {
            writer.setLength(0);
            writer.writeBytes(content);
        } catch (IOException e) {
            return Either.right("IOError : cannot open file stream");
        }

        return Either.left(null);
    }

    public static Boolean exist(String logicalPath) {
        File file = new File(APPDATA, logicalPath);

        return file.exists();
    }

    public static long getContentLength(String logicalPath) {
        File file = new File(APPDATA, logicalPath);

        return file.length();
    }

    public static Either<String, String> getContent(String logicalPath) {
        File file = new File(APPDATA, logicalPath);

        try {
            String content = FileUtils.readFileToString(file, StandardCharsets.UTF_8);

            return Either.left(content);
        } catch (IOException e) {
            return Either.right("Fatal : cannot read content");
        }
    }

    public static long getNumFiles(String logicalPath) {
        File dir = new File(APPDATA, logicalPath);

        return Objects.requireNonNull(dir.list()).length;
    }

    public static String resolvePath(String app) {
        return app.replaceFirst("/", "");
    }
}
