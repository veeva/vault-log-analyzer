package com.veeva.vault.tools.util;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;

public class DateUtils {

    private static String DATE_DATE_FORMAT = "yyyy-MM-dd";
    private static String DATE_UTC_FORMAT = "yyyy-MM-dd HH:mm:ss,SSS";

    public static boolean isDateTime(String value) {
        DateFormat dateFormat = new SimpleDateFormat(DATE_UTC_FORMAT);
        dateFormat.setLenient(false);
        try {
            dateFormat.parse(value);
        } catch (ParseException e) {
            return false;
        }
        return true;
    }

    public static boolean isDate(String value) {
        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        dateFormat.setLenient(false);
        try {
            dateFormat.parse(value);
        } catch (ParseException e) {
            return false;
        }
        return true;
    }
}
