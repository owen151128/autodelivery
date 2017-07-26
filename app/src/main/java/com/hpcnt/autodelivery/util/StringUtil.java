package com.hpcnt.autodelivery.util;

public class StringUtil {

    public static boolean isDirectory(String path) {
        return path.charAt(path.length() - 1) == '/';
    }
}
