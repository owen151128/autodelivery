package com.hpcnt.autodelivery.model;

import android.support.annotation.NonNull;

import com.hpcnt.autodelivery.BaseApplication;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class Build {
    @NonNull
    private String versionName = "";
    @NonNull
    private String date = "";
    @NonNull
    private String apkName = "";
    @NonNull
    private List<String> separateName = new ArrayList<>();

    private final SimpleDateFormat engFormat;
    private final SimpleDateFormat korFormat;

    {
        engFormat = new SimpleDateFormat("dd-MMM-yyyy kk:mm", Locale.US);
        korFormat = new SimpleDateFormat("yy년 MM월 dd일 kk시 mm분", Locale.KOREAN);
    }

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

    public void setApkName(@NonNull String apkName) {
        this.apkName = apkName;
    }

    @NonNull
    public String getVersionName() {
        return versionName;
    }

    @NonNull
    public String getDate() {
        return date;
    }

    @NonNull
    public String getApkName() {
        return apkName;
    }

    @NonNull
    public String getApkUrl() {
        return BaseApplication.BUILD_SERVER_URL + versionName + apkName;
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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Build build = (Build) o;

        return versionName.equals(build.versionName)
                && date.equals(build.date)
                && apkName.equals(build.apkName);

    }

    @Override
    public int hashCode() {
        int result = versionName.hashCode();
        result = 31 * result + date.hashCode();
        result = 31 * result + apkName.hashCode();
        return result;
    }
}
