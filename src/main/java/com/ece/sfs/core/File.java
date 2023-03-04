package com.ece.sfs.core;

import com.ece.sfs.Util;
import io.vavr.control.Either;

import java.util.Date;
import java.util.UUID;

import static com.ece.sfs.Util.validFileName;


public class File extends Component {
    private Date lastModifiedDate;
    private Directory parent;
    private String name = "";

    private String uuid;

    private String nameChecksum;
    private long lengthChecksum = 0;
    private String contentChecksum = "";

    // TODO: Implement File Navigation
    // TODO: Implement Group and User Access for file

    public File(String name, String nameChecksum, Directory parent, Date lastModifiedDate, UUID uuid) {
        this.uuid = uuid.toString();

        setName(name);
        setNameChecksum(nameChecksum);
        setParent(parent);
        setDate(lastModifiedDate);
    }

    @Override
    public Directory getParent() {
        return parent;
    }

    @Override
    public void setParent(Directory parent) {
        if (parent == null) {
            throw new IllegalArgumentException("Parent directory cannot be null");
        }

        this.parent = parent;
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
    public String getNameChecksum() {
        return nameChecksum;
    }

    @Override
    public Either<Void, String> setNameChecksum(String nameChecksum) {
        this.nameChecksum = nameChecksum;

        return Either.left(null);
    }

    @Override
    public Either<Void, String> setLengthChecksum(long lengthChecksum) {
        this.lengthChecksum = lengthChecksum;

        return Either.left(null);
    }

    @Override
    public Either<Void, String> setContentChecksum(String contentChecksum) {
        this.contentChecksum = contentChecksum;

        return Either.left(null);
    }

    @Override
    public boolean checkNameChecksum(String checksum) {
        return nameChecksum.compareTo(checksum) == 0;
    }

    @Override
    public boolean checkLengthChecksum(long checksum) {
        return lengthChecksum == checksum;
    }

    @Override
    public boolean checkContentChecksum(String checksum) {
        return contentChecksum.compareTo(checksum) == 0;
    }

    @Override
    public String getUUID() {
        return uuid;
    }
}
