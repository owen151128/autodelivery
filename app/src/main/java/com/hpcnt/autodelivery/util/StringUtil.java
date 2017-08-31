package com.hpcnt.autodelivery.util;

import android.util.Log;

public class StringUtil {

    public static boolean isDirectory(String path) {
        return !path.isEmpty() && path.charAt(path.length() - 1) == '/';
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

    public static boolean isLastWord(String currentVersion, String word) {
        int currentLength = currentVersion.length();
        int wordLength = word.length();

        int totalIndex = currentVersion.lastIndexOf(word) + wordLength;
        if (totalIndex == currentLength) {
            return true;
        } else {
            return false;
        }
    }
}
