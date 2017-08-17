package com.hpcnt.autodelivery.model;

import com.google.gson.reflect.TypeToken;
import com.hpcnt.autodelivery.TestUtil;

import org.junit.Test;

import java.util.List;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNotSame;

public class BuildListTest {

    @Test
    public void testParseHtmlToBuildList() {
        String response = TestUtil.getStringFromResource(getClass().getClassLoader(), "index_from_html.html");
        assertNotSame("파일을 읽었을 때 공백이면 안된다", "", response);

        String setupDataJson = TestUtil.getStringFromResource(getClass().getClassLoader(), "build_list_setup_data.json");
        assertNotSame("파일을 읽었을 때 공백이면 안된다", "", setupDataJson);

        List<Build> builds = TestUtil.getListObjectFromJson(setupDataJson, Build.class, new TypeToken<List<Build>>() {
        }.getType(), (json, typeOfT, context) -> {
            String versionName = json.getAsJsonObject().get("versionName").getAsString();
            String date = json.getAsJsonObject().get("date").getAsString();
            String apkName = json.getAsJsonObject().get("apkName").getAsString();
            return new Build(versionName, date, apkName);
        });

        BuildList buildListFromTest = new BuildList();
        buildListFromTest.setList(builds);

        BuildList buildListFromResponse = BuildList.fromHtml(response);

        assertEquals("html을 파싱한 데이터와 준비한 데이터 같이야한다", buildListFromTest.getList(), buildListFromResponse.getList());
    }
}