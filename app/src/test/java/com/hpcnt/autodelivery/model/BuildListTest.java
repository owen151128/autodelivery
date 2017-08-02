package com.hpcnt.autodelivery.model;

import com.hpcnt.autodelivery.util.StringUtil;

import junit.framework.Assert;

import org.junit.Test;

import static org.junit.Assert.*;

public class BuildListTest {

    @Test
    public void testParseHtmlToBuildList() {
        String response = StringUtil.getStringFromResource(getClass().getClassLoader(), "ci_index_page_from_html.html");
        Assert.assertTrue(!"".equals(response));

        BuildList buildListFromTest = new BuildList();
        buildListFromTest.add(new Build("3.11.0-beta-5/", "21-Nov-2016 15:48", ""));
        buildListFromTest.add(new Build("3.12.0-beta-1/", "20-Dec-2016 22:44", ""));
        buildListFromTest.add(new Build("3.13.0-beta-3/", "01-Feb-2017 17:23", ""));
        buildListFromTest.add(new Build("3.14.7/", "12-Mar-2017 12:31", ""));
        buildListFromTest.add(new Build("3.15.3/", "10-Apr-2017 12:21", ""));
        buildListFromTest.add(new Build("3.16.2/", "15-May-2017 17:51", ""));
        buildListFromTest.add(new Build("3.17.0-CN/", "25-Jul-2017 14:37", ""));
        buildListFromTest.add(new Build("3.17.0-purchasing-test/", "19-Jun-2017 18:48", ""));
        buildListFromTest.add(new Build("3.18.0/", "17-Jul-2017 10:52", ""));
        buildListFromTest.add(new Build("3.5.0/", "14-Jun-2016 17:56", ""));
        buildListFromTest.add(new Build("3.6.0-beta-3/", "13-Jul-2016 16:29", ""));
        buildListFromTest.add(new Build("3.7.0-beta-3/", "27-Jul-2016 14:48", ""));
        buildListFromTest.add(new Build("3.8.4/", "26-Aug-2016 11:33", ""));
        buildListFromTest.add(new Build("3.9.0-alpha-1/", "31-Aug-2016 19:12", ""));

        BuildList buildListFromResponse = BuildList.fromHtml(response);

        boolean isEquals = buildListFromResponse.getList().equals(buildListFromTest.getList());
        Assert.assertTrue(isEquals);
    }
}