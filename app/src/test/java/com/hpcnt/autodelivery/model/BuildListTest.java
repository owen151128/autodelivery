package com.hpcnt.autodelivery.model;

import com.google.gson.reflect.TypeToken;
import com.hpcnt.autodelivery.TestUtil;

import org.junit.Test;

import java.util.List;

import static junit.framework.Assert.assertTrue;

public class BuildListTest {

    @Test
    public void testParseHtmlToBuildList() {
        String response = TestUtil.getStringFromResource(getClass().getClassLoader(), "index_from_html.html");
        assertTrue(!"".equals(response));

        String setupDataJson = TestUtil.getStringFromResource(getClass().getClassLoader(), "build_list_setup_data.json");
        assertTrue(!"".equals(response));

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

        boolean isEquals = buildListFromResponse.getList().equals(buildListFromTest.getList());
        assertTrue(isEquals);
    }
}