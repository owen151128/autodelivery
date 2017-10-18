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

    /**
     * DownloadManager Request 를 보낼때 subpath에 File.separater 가 들어가면 Fail 이 발생한다.
     * 따라서 File.separater 를 "_" 로 replace 하여 문제를 해결한다.
     */
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

    /**
     * Download_Path 에서 FILE.separater 가 "_" 으로 변경되었음 으로
     * apkDownloadedPath 역시 File.separater 를 "_" 로 replace 가 필요하다.
     */
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
