package com.hpcnt.autodelivery.ui;

import com.hpcnt.autodelivery.BuildConfig;
import com.hpcnt.autodelivery.R;
import com.hpcnt.autodelivery.model.Build;
import com.hpcnt.autodelivery.network.BuildFetcher;
import com.hpcnt.autodelivery.util.StringUtil;

import junit.framework.Assert;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import io.reactivex.Single;
import io.reactivex.plugins.RxJavaPlugins;
import io.reactivex.schedulers.Schedulers;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(RobolectricTestRunner.class)
@Config(constants = BuildConfig.class)
public class MainPresenterTest {
    private MainActivity mActivity = Robolectric.setupActivity(MainActivity.class);
    private MainPresenter mPresenter = new MainPresenter(mActivity);

    @Before
    public void setUp() throws Exception {
        RxJavaPlugins.setIoSchedulerHandler(scheduler -> Schedulers.trampoline());
    }

    @After
    public void tearDown() throws Exception {
        RxJavaPlugins.reset();
    }

    @Test
    public void testGetLatestBuildSuccess() {
        String responseFirst = getResString("index_3_18_9.html");
        String responseApk = getResString("index_3_18_9_apk.html");

        mPresenter.setBuildFetcher(mock(BuildFetcher.class));

        when(mPresenter.getFetchedList(mPresenter.getBuildFetcher(), "")).thenReturn(Single.just(responseFirst));
        when(mPresenter.getFetchedList(mPresenter.getBuildFetcher(), "3.18.9/")).thenReturn(Single.just(responseApk));

        mPresenter.loadLatestBuild();

        Assert.assertTrue(mActivity.getBinding().mainBtnAction.isEnabled());
        String btnString = mActivity.getBinding().mainBtnAction.getText().toString();
        String resString = mActivity.getResources().getString(R.string.download);
        Assert.assertTrue(btnString.equals(resString));

        Build mockBuild = new Build("3.18.9/", "17년 07월 31일 14시 14분", "app-playstore-armeabi-v7a-qatest.apk");
        Assert.assertTrue(mPresenter.getBuild() != null);

        Assert.assertTrue(mPresenter.getBuild().equals(mockBuild));

        String versionName = mActivity.getBinding().mainVersionName.getText().toString();
        Assert.assertEquals(mockBuild.getVersionName(), versionName);

        String date = mActivity.getBinding().mainDate.getText().toString();
        Assert.assertEquals(mockBuild.getDate(), date);
    }

    private String getResString(String resource) {
        String responseFirst = StringUtil.getStringFromResource(getClass().getClassLoader(), resource);
        Assert.assertTrue(!"".equals(responseFirst));
        return responseFirst;
    }
}