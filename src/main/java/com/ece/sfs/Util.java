package com.ece.sfs;

import java.text.SimpleDateFormat;
import java.util.Date;

public class Util {

    public static String dateToString(Date date, String format) {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat(format);
        return simpleDateFormat.format(date);
    }

    public static boolean validName(String string) {
        return string == null || string.isEmpty() || !string.matches("^[A-za-z0-9_ ]{1,255}$");
    }

    public static boolean validFileName(String string) {
        return string == null || string.isEmpty() || !string.matches("^[A-za-z0-9_ ./]{1,255}$");
    }

    public static boolean validPassword(String string) {
        if (string.length() < 8)
            return true;

        if (!string.matches("(.*[A-Z].*)"))
            return true;

        if (!string.matches("(.*[a-z].*)"))
            return true;

        if (!string.matches("(.*[0-9].*)"))
            return true;

        return !string.matches("(.*[@,#,$,%,!].*$)");
    }

    public static boolean validUUID(String string) {
        return string == null || string.isEmpty() || !string
                .matches("^[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}$");
    }
}
