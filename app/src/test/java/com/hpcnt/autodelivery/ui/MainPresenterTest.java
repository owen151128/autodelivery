package com.hpcnt.autodelivery.ui;

import android.Manifest;
import android.app.DownloadManager;
import android.content.Intent;

import com.hpcnt.autodelivery.BuildConfig;
import com.hpcnt.autodelivery.R;
import com.hpcnt.autodelivery.model.Build;
import com.hpcnt.autodelivery.model.BuildList;
import com.hpcnt.autodelivery.network.BuildFetcher;
import com.hpcnt.autodelivery.ui.dialog.BuildEditContract;
import com.hpcnt.autodelivery.ui.dialog.BuildEditDialog;
import com.hpcnt.autodelivery.TestUtil;

import junit.framework.Assert;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;
import org.robolectric.shadows.ShadowApplication;
import org.robolectric.shadows.ShadowDownloadManager;

import io.reactivex.Single;
import io.reactivex.plugins.RxJavaPlugins;
import io.reactivex.schedulers.Schedulers;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.robolectric.Shadows.shadowOf;

@RunWith(RobolectricTestRunner.class)
@Config(constants = BuildConfig.class)
public class MainPresenterTest {
    private MainActivity mActivity = Robolectric.buildActivity(MainActivity.class).create().get();
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

        Build mockBuild = new Build("3.18.9/", "17년 07월 31일 14시 14분", "app-playstore-armeabi-v7a-qatest.apk");
        executeLoadLatestBuildSuccess(mockBuild);
    }

    @Test
    public void testGetLatestBuildRecursiveSuccess() {
        String responseFirst = getResString("index_3_18_0.html");
        String responseSecond = getResString("index_3_18_0_201707171052.html");
        String responseApk = getResString("index_3_18_0_201707171052_apk.html");

        mPresenter.setBuildFetcher(mock(BuildFetcher.class));

        when(mPresenter.getFetchedList(mPresenter.getBuildFetcher(), "")).thenReturn(Single.just(responseFirst));
        when(mPresenter.getFetchedList(mPresenter.getBuildFetcher(), "3.18.0/")).thenReturn(Single.just(responseSecond));
        when(mPresenter.getFetchedList(mPresenter.getBuildFetcher(), "3.18.0/201707171052/")).thenReturn(Single.just(responseApk));

        Build mockBuild = new Build("3.18.0/201707171052/", "17년 07월 17일 10시 55분", "app-playstore-armeabi-v7a-qatest.apk");

        executeLoadLatestBuildSuccess(mockBuild);
    }

    @Test
    public void testGetLatestBuildFail() {
        String responseFirst = getResString("index_3_18_9.html");

        mPresenter.setBuildFetcher(mock(BuildFetcher.class));

        when(mPresenter.getFetchedList(mPresenter.getBuildFetcher(), "")).thenReturn(Single.error(new RuntimeException()));
        mPresenter.loadLatestBuild();
        Assert.assertEquals(Build.EMPTY, mPresenter.getBuild());

        when(mPresenter.getFetchedList(mPresenter.getBuildFetcher(), "")).thenReturn(Single.just(responseFirst));
        when(mPresenter.getFetchedList(mPresenter.getBuildFetcher(), "3.18.9/")).thenReturn(Single.error(new RuntimeException()));
        mPresenter.loadLatestBuild();
        Assert.assertEquals(Build.EMPTY, mPresenter.getBuild());
    }

    @Test
    public void testDownloadApkSuccess() {
        String responseFirst = getResString("index_3_18_9.html");
        String responseApk = getResString("index_3_18_9_apk.html");

        mPresenter.setBuildFetcher(mock(BuildFetcher.class));

        when(mPresenter.getFetchedList(mPresenter.getBuildFetcher(), "")).thenReturn(Single.just(responseFirst));
        when(mPresenter.getFetchedList(mPresenter.getBuildFetcher(), "3.18.9/")).thenReturn(Single.just(responseApk));

        Build mockBuild = new Build("3.18.9/", "17년 07월 31일 14시 14분", "app-playstore-armeabi-v7a-qatest.apk");
        mPresenter.loadLatestBuild();

        ShadowApplication shadowApplication = shadowOf(mActivity.getApplication());
        shadowApplication.grantPermissions(Manifest.permission.WRITE_EXTERNAL_STORAGE);
        mPresenter.downloadApk();

        assertState(MainContract.STATE.DOWNLOADING, mPresenter.getState());
        DownloadManager downloadManager = mActivity.getDownloadManager();
        ShadowDownloadManager shadowDownloadManager = shadowOf(downloadManager);
        Assert.assertTrue(shadowDownloadManager.getRequestCount() > 0);
        ShadowDownloadManager.ShadowRequest shadowRequest = shadowOf(shadowDownloadManager.getRequest(0));
        Assert.assertNotNull(shadowRequest);
        Assert.assertEquals(mockBuild.getVersionName(), shadowRequest.getTitle());
        Assert.assertEquals(mockBuild.getDate(), shadowRequest.getDescription());
        Assert.assertEquals(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED, shadowRequest.getNotificationVisibility());
    }

    @Test
    public void testDownloadApkFail() {
        mPresenter.setBuild(new Build("", "", "asdf"));
        mPresenter.setState(MainContract.STATE.DOWNLOAD);

        ShadowApplication shadowApplication = shadowOf(mActivity.getApplication());
        shadowApplication.grantPermissions(Manifest.permission.WRITE_EXTERNAL_STORAGE);
        mPresenter.downloadApk();

        assertState(MainContract.STATE.DOWNLOADING, mPresenter.getState());
        mActivity.downloadCompleteReceiver.onReceive(mActivity, new Intent());
        assertState(MainContract.STATE.FAIL, ((MainPresenter) mActivity.getPresenter()).getState());
    }

    @Test
    public void testEditCurrentBuild() {
        String helloWorld = "hello world";
        mPresenter.editCurrentBuild(helloWorld, BuildEditContract.FLAG.EDIT);
        BuildEditDialog buildEditDialog = (BuildEditDialog) mActivity.getSupportFragmentManager()
                .findFragmentByTag(BuildEditDialog.class.getSimpleName());

        Assert.assertNotNull(buildEditDialog);
        Assert.assertEquals(helloWorld,
                buildEditDialog.getArguments().get(BuildEditContract.KEY_VERSION_PATH));
        Assert.assertEquals(BuildEditContract.FLAG.EDIT,
                buildEditDialog.getArguments().get(BuildEditContract.KEY_FLAG));
    }

    @Test
    public void testGetMyAbiBuildSuccess() {
        BuildList buildList = new BuildList();
        buildList.add(new Build("app-playstore-arm64-v8a-qatest.apk", "31-Jul-2017 14:14", ""));
        buildList.add(new Build("app-playstore-armeabi-v7a-qatest.apk", "31-Jul-2017 14:14", ""));
        buildList.add(new Build("app-playstore-x86-qatest.apk", "31-Jul-2017 14:14", ""));
        buildList.add(new Build("app-playstore-x86_64-qatest.apk", "31-Jul-2017 14:14", ""));

        Build presenterBuild = mPresenter.getMyAbiBuild(buildList, "3.18.9/");

        Build mockBuild = new Build("3.18.9/", "17년 07월 31일 14시 14분", "app-playstore-armeabi-v7a-qatest.apk");

        Assert.assertEquals(mockBuild, presenterBuild);
    }

    @Test
    public void testGetMyAbiBuildFail() {
        BuildList buildList = new BuildList();
        buildList.add(new Build("azar-android-playstoreArm-qatest.apk", "16-Nov-2016 13:59", ""));
        buildList.add(new Build("azar-android-playstoreX86-qatest.apk", "16-Nov-2016 14:00", ""));

        Build presenterBuild = mPresenter.getMyAbiBuild(buildList, "3.11.0-alpha-13/");

        Build mockBuild = new Build("3.11.0-alpha-13/", "16년 11월 16일 14시 00분", "");

        Assert.assertEquals(mockBuild, presenterBuild);
    }

    @Test
    public void testSelectMyAbiBuild() {
        BuildList buildList = new BuildList();
        buildList.add(new Build("app-playstore-arm64-v8a-qatest.apk", "31-Jul-2017 14:14", ""));
        buildList.add(new Build("app-playstore-armeabi-v7a-qatest.apk", "31-Jul-2017 14:14", ""));
        buildList.add(new Build("app-playstore-x86-qatest.apk", "31-Jul-2017 14:14", ""));
        buildList.add(new Build("app-playstore-x86_64-qatest.apk", "31-Jul-2017 14:14", ""));

        mPresenter.selectMyAbiBuild(buildList, "3.18.9/");
        Build mockBuild = new Build("3.18.9/", "17년 07월 31일 14시 14분", "app-playstore-armeabi-v7a-qatest.apk");
        Assert.assertEquals(mockBuild, mPresenter.getBuild());
        Assert.assertEquals(mockBuild.getVersionName(), mActivity.getBinding().mainVersionName.getText().toString());
        Assert.assertEquals(mockBuild.getDate(), mActivity.getBinding().mainDate.getText().toString());
    }

    private void executeLoadLatestBuildSuccess(Build mockBuild) {
        mPresenter.loadLatestBuild();

        assertState(MainContract.STATE.DOWNLOAD, mPresenter.getState());

        Assert.assertNotNull(mPresenter.getBuild());

        Assert.assertEquals(mockBuild, mPresenter.getBuild());

        String versionName = mActivity.getBinding().mainVersionName.getText().toString();
        Assert.assertEquals(mockBuild.getVersionName(), versionName);

        String date = mActivity.getBinding().mainDate.getText().toString();
        Assert.assertEquals(mockBuild.getDate(), date);
    }

    private void assertState(MainContract.STATE expectedState, MainContract.STATE actualState) {
        Assert.assertEquals(expectedState, actualState);
        boolean isEnable;
        int stringResId;
        switch (actualState) {
            case DOWNLOAD:
                isEnable = true;
                stringResId = R.string.download;
                break;
            case DOWNLOADING:
                isEnable = false;
                stringResId = R.string.downloading;
                break;
            case LOADING:
                isEnable = false;
                stringResId = R.string.loading;
                break;
            case INSTALL:
                isEnable = true;
                stringResId = R.string.install;
                break;
            case FAIL:
                isEnable = false;
                stringResId = R.string.fail;
                break;
            default:
                isEnable = false;
                stringResId = 0;
                break;
        }

        Assert.assertEquals(isEnable, mActivity.getBinding().mainBtnAction.isEnabled());
        String btnString = mActivity.getBinding().mainBtnAction.getText().toString();
        String resString = mActivity.getResources().getString(stringResId);
        Assert.assertEquals(resString, btnString);
    }

    private String getResString(String resource) {
        String responseFirst = TestUtil.getStringFromResource(getClass().getClassLoader(), resource);
        Assert.assertNotSame("", responseFirst);
        return responseFirst;
    }
}