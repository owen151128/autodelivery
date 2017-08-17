package com.hpcnt.autodelivery.model;

import com.hpcnt.autodelivery.TestUtil;

import junit.framework.Assert;

import org.junit.Test;

public class BuildTest {

    @Test
    public void testGetLatestBuild() {
        String response = TestUtil.getStringFromResource(getClass().getClassLoader(), "index_3_18_9.html");
        Assert.assertTrue(!"".equals(response));
        BuildList buildList = BuildList.fromHtml(response);
        Build build = buildList.getLatestBuild();
        Assert.assertTrue(build != null);

        Build mockBuild = new Build("3.18.9/", "17년 07월 31일 14시 14분", "");

        Assert.assertTrue(build.equals(mockBuild));
    }
}