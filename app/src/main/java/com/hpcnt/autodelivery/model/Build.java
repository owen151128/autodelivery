package com.hpcnt.autodelivery.model;

import android.os.Environment;
import android.support.annotation.NonNull;

import com.hpcnt.autodelivery.BaseApplication;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@ToString(exclude = "separateName")
@EqualsAndHashCode(exclude = "separateName")
public class Build {

    public static final Build EMPTY = new Build();
    @NonNull
    @Getter
    private String versionName = "";
    @NonNull
    @Getter
    private String date = "";
    @NonNull
    @Getter
    @Setter
    private String apkName = "";
    @NonNull
    private List<String> separateName = new ArrayList<>();
    private final SimpleDateFormat engFormat =
            new SimpleDateFormat("dd-MMM-yyyy kk:mm", Locale.US);
    private final SimpleDateFormat korFormat =
            new SimpleDateFormat("yy년 MM월 dd일 kk시 mm분", Locale.KOREAN);

    public Build() {
    }

    public Build(@NonNull String versionName, @NonNull String date, @NonNull String apkName) {
        setVersionName(versionName);
        setDate(date);
        setApkName(apkName);
    }

    public void setVersionName(@NonNull String versionName) {
        this.versionName = versionName;
        separateName = Arrays.asList(versionName.split("[^\\p{Alnum}]"));
    }

    public String getDownloadVersionNamePath() {
        return this.getVersionName().replace("/", "_");
    }

    public void setDate(@NonNull String date) {
        if (isLanguageFormat(engFormat, date)) {
            Date inputDate;
            try {
                inputDate = engFormat.parse(date);
            } catch (ParseException e) {
                inputDate = new Date(0);
            }

            this.date = korFormat.format(inputDate);
        } else if (isLanguageFormat(korFormat, date)) {
            this.date = date;
        } else {
            this.date = "";
        }
    }

    @NonNull
    public String getApkUrl() {
        return BaseApplication.BUILD_SERVER_URL + versionName + apkName;
    }

    @NonNull
    public String getApkDownloadedPath() {
        return Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_DOWNLOADS).getAbsolutePath() + "/" + (getVersionName()
                + getApkName()).replace("/", "_");
    }

    @NonNull
    public List<String> getSeparateName() {
        return separateName;
    }

    private boolean isLanguageFormat(SimpleDateFormat format, String date) {
        try {
            format.parse(date);
        } catch (ParseException e) {
            return false;
        }
        return true;
    }
}
