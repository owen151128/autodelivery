package com.hpcnt.autodelivery.model;

import android.support.annotation.NonNull;

import com.hpcnt.autodelivery.util.StringUtil;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.TextNode;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.TreeSet;

public class BuildList {
    @NonNull
    private List<Build> buildList = new ArrayList<>();

    public void add(Build build) {
        buildList.add(build);
    }

    public Build getLastestBuild() {
        if (buildList.size() == 0) return null;

        String lastestDate = buildList.get(0).getDate();
        int lastestIndex = 0;
        for (int i = 1; i < buildList.size(); i++) {
            if (lastestDate.compareTo(buildList.get(i).getDate()) < 0) {
                lastestDate = buildList.get(i).getDate();
                lastestIndex = i;
            }
        }

        return buildList.get(lastestIndex);
    }

    @NonNull
    public static BuildList fromHtml(String response) {
        BuildList builds = new BuildList();
        Document document = Jsoup.parse(response);
        Element preElement = document.select("pre").first();
        for (Element element : preElement.children()) {
            if (!StringUtil.isDigitFirstWord(element.text())
                    && !StringUtil.isApkFile(element.text()))
                continue;

            Build build = new Build();
            build.setVersionName(element.text());

            builds.add(build);
        }

        SimpleDateFormat inputFormat = new SimpleDateFormat("dd-MMM-yyyy kk:mm", Locale.US);
        SimpleDateFormat transFormat = new SimpleDateFormat("yy년 MM월 dd일 kk시 mm분", Locale.KOREAN);

        List<TextNode> textNodes = preElement.textNodes();
        for (int i = 1; i <= builds.size(); i++) {
            TextNode textNode = textNodes.get(i);
            String date = textNode.text().split(" -")[0].trim();

            Date inputDate;
            try {
                inputDate = inputFormat.parse(date);
            } catch (ParseException e) {
                inputDate = new Date(0);
            }

            String transDate = transFormat.format(inputDate);
            builds.get(i - 1).setDate(transDate);
        }
        return builds;
    }

    public int size() {
        return buildList.size();
    }

    public Build get(int index) {
        return buildList.get(index);
    }

    public Set<String> getVersionSet(List<String> separateName, int index) {
        Set<String> versionSet = new TreeSet<>((o1, o2) -> {
            try {
                int first = Integer.parseInt(o1);
                int second = Integer.parseInt(o2);
                return second - first;
            } catch (NumberFormatException e) {
                return -o1.compareTo(o2);
            }
        });

        for (Build build : buildList) {
            int listSize = build.getSeparateName().size();
            if (index < listSize) {
                // 인자로 받은 separateName의 모든것이 build.separateName에 포함되어있는지 확인
                boolean isContain = true;
                for (int i = 0; i < separateName.size(); i++) {
                    if (!build.getSeparateName().get(i).equals(separateName.get(i))) {
                        isContain = false;
                        break;
                    }
                }
                if (isContain || separateName.size() == 0)
                    versionSet.add(build.getSeparateName().get(index));
            }
        }
        return versionSet;
    }

    public Build get(List<String> separateName) {
        for (Build build : buildList) {
            if (build.getSeparateName().size() != separateName.size()) continue;
            boolean isEqual = true;
            for (int i = 0; i < separateName.size(); i++) {
                if (!build.getSeparateName().get(i).equals(separateName.get(i))) {
                    isEqual = false;
                    break;
                }
            }
            if (isEqual) return build;
        }
        return null;
    }

    public List<Build> getList() {
        return buildList;
    }

    public Build get(String selectedVersion) {
        Build build = new Build();
        build.setVersionName(selectedVersion);
        return get(build.getSeparateName());
    }

    public void remove(Build build) {
        buildList.remove(build);
    }

    public void reverse() {
        Collections.reverse(buildList);
    }
}
