package com.hpcnt.autodelivery.util;

import org.apache.commons.io.IOUtils;

import java.io.IOException;
import java.io.InputStream;

public class StringUtil {

    public static boolean isDirectory(String path) {
        return !"".equals(path) && path.charAt(path.length() - 1) == '/';
    }

    public static boolean isDigitFirstWord(String string) {
        try {
            //noinspection ResultOfMethodCallIgnored
            Integer.parseInt(String.valueOf(string.charAt(0)));
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    public static boolean isApkFile(String fileName) {
        fileName = fileName.toLowerCase();
        return fileName.endsWith(".apk");
    }

    public static String getStringFromResource(ClassLoader classLoader, String fileName) {
        InputStream inputStream = classLoader.getResourceAsStream(fileName);
        String response = "";
        try {
            response = IOUtils.toString(inputStream, "UTF-8");
        } catch (IOException e) {
            e.printStackTrace();
        }
        return response;
    }
}
