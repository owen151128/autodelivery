package com.hpcnt.autodelivery.model;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.TextNode;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class BuildList {
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

    public static BuildList fromHtml(String response) {
        BuildList builds = new BuildList();
        Document document = Jsoup.parse(response);
        Element preElement = document.select("pre").first();
        for (Element element : preElement.children()) {
            if (element.text().equals("../")) continue;
            if (element.text().equals("china/")) continue;
            if (element.text().equals("develop/")) continue;
            if (element.text().equals("pr/")) continue;
            if (element.text().equals("qatest/")) continue;

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

            Date inputDate = null;
            try {
                inputDate = inputFormat.parse(date);
            } catch (ParseException e) {
                e.printStackTrace();
                return null;
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
}
