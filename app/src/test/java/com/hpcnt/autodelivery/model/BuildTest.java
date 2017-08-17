package com.hpcnt.autodelivery.model;

import com.hpcnt.autodelivery.TestUtil;

import org.junit.Test;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNotSame;

public class BuildTest {

    @Test
    public void testGetLatestBuild() {
        String response = TestUtil.getStringFromResource(getClass().getClassLoader(), "index_3_18_9.html");
        assertNotSame("파일을 읽었을 때 공백이면 안된다", "", response);
        BuildList buildList = BuildList.fromHtml(response);
        Build build = buildList.getLatestBuild();

        Build mockBuild = new Build("3.18.9/", "17년 07월 31일 14시 14분", "");

        assertEquals("BuildList에서 최신 Build를 가지고 와야한다", mockBuild, build);
    }
}