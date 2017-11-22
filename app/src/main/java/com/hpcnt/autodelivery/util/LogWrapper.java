package com.hpcnt.autodelivery.util;

import android.os.Environment;
import android.support.annotation.NonNull;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

public class LogWrapper {
    private static LogWrapper instance;
    private String initTime;
    private File logFile;
    private FileWriter writer;

    private LogWrapper(String initTime) {
        try {
            this.initTime = initTime;
            logFile = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), "autodelivery.log");
            if (logFile.exists()) {
                writer = new FileWriter(logFile, true);
            } else {
                if (logFile.createNewFile()) {
                    writer = new FileWriter(logFile, true);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static synchronized LogWrapper getInstance(@NonNull String initTime) {
        if (instance == null)
            instance = new LogWrapper(initTime);
        return instance;
    }

    public static synchronized LogWrapper getInstance() {
        return instance;
    }

    public void saveTextViewLabel(String text) {
        try {
            writer = new FileWriter(logFile, true);
            writer.write("=======================================================================\n");
            writer.write("Select_Build Text : " + text + "\n");
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void saveUrlAndPath(String time, String url, String path) {
        try {
            writer = new FileWriter(logFile, true);
            writer.write("Init Time : " + initTime + "\n");
            writer.write("Download Time : " + time + "\n");
            writer.write("Request Url : " + url + "\n");
            writer.write("Download path : " + path + "\n");
            writer.write("=======================================================================\n");
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void saveInstallButtonLog(String text) {
        try {
            writer = new FileWriter(logFile, true);
            writer.write("=======================================================================\n");
            writer.write("install_Build Text : " + text + "\n");
            writer.write("=======================================================================\n");
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
