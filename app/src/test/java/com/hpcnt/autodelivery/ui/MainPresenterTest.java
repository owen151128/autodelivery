package com.hpcnt.autodelivery.ui;

import android.Manifest;
import android.app.DownloadManager;
import android.content.Intent;

import com.hpcnt.autodelivery.BuildConfig;
import com.hpcnt.autodelivery.R;
import com.hpcnt.autodelivery.TestUtil;
import com.hpcnt.autodelivery.model.Build;
import com.hpcnt.autodelivery.model.BuildList;
import com.hpcnt.autodelivery.network.BuildFetcher;
import com.hpcnt.autodelivery.ui.dialog.BuildEditContract;
import com.hpcnt.autodelivery.ui.dialog.BuildEditDialog;

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

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNotNull;
import static junit.framework.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.robolectric.Shadows.shadowOf;

@RunWith(RobolectricTestRunner.class)
@Config(constants = BuildConfig.class)
public class MainPresenterTest {

    private MainActivity mActivity;
    private MainPresenter mPresenter;

    @Before
    public void setUp() throws Exception {
        RxJavaPlugins.setIoSchedulerHandler(scheduler -> Schedulers.trampoline());
        mActivity = Robolectric.buildActivity(MainActivity.class).create().get();
        mPresenter = new MainPresenter(mActivity);
    }

    @After
    public void tearDown() throws Exception {
        RxJavaPlugins.reset();
        mActivity = null;
        mPresenter = null;
    }

    @Test
    public void testGetLatestBuildSuccess() {
        String responseFirst = TestUtil.getStringFromResource(getClass().getClassLoader(), "index_3_18_9.html");
        String responseApk = TestUtil.getStringFromResource(getClass().getClassLoader(), "index_3_18_9_apk.html");

        BuildFetcher mockFetcher = mock(BuildFetcher.class);

        when(mockFetcher.fetchBuildList("")).thenReturn(Single.just(responseFirst));
        when(mockFetcher.fetchBuildList("3.18.9/")).thenReturn(Single.just(responseApk));

        Build mockBuild = new Build("3.18.9/", "17년 07월 31일 14시 14분", "app-playstore-armeabi-v7a-qatest.apk");

        mPresenter.loadLatestBuild(mockFetcher);

        verifyLoadLatestBuildSuccess(mockBuild, mPresenter.getBuild(), mPresenter.getState());
    }

    @Test
    public void testGetLatestBuildRecursiveSuccess() {
        String responseFirst = TestUtil.getStringFromResource(getClass().getClassLoader(), "index_3_18_0.html");
        String responseSecond = TestUtil.getStringFromResource(getClass().getClassLoader(), "index_3_18_0_201707171052.html");
        String responseApk = TestUtil.getStringFromResource(getClass().getClassLoader(), "index_3_18_0_201707171052_apk.html");

        BuildFetcher mockFetcher = mock(BuildFetcher.class);

        when(mockFetcher.fetchBuildList("")).thenReturn(Single.just(responseFirst));
        when(mockFetcher.fetchBuildList("3.18.0/")).thenReturn(Single.just(responseSecond));
        when(mockFetcher.fetchBuildList("3.18.0/201707171052/")).thenReturn(Single.just(responseApk));

        Build mockBuild = new Build("3.18.0/201707171052/", "17년 07월 17일 10시 55분", "app-playstore-armeabi-v7a-qatest.apk");

        mPresenter.loadLatestBuild(mockFetcher);

        verifyLoadLatestBuildSuccess(mockBuild, mPresenter.getBuild(), mPresenter.getState());
    }

    @Test
    public void testGetLatestBuildFail() {
        String responseFirst = TestUtil.getStringFromResource(getClass().getClassLoader(), "index_3_18_9.html");

        BuildFetcher mockFetcher = mock(BuildFetcher.class);

        when(mockFetcher.fetchBuildList("")).thenReturn(Single.error(new RuntimeException()));
        mPresenter.loadLatestBuild(mockFetcher);
        assertEquals("최신빌드가 실패했을 때 EMPTY 빌드가 되어야 한다", Build.EMPTY, mPresenter.getBuild());

        when(mockFetcher.fetchBuildList("")).thenReturn(Single.just(responseFirst));
        when(mockFetcher.fetchBuildList("3.18.9/")).thenReturn(Single.error(new RuntimeException()));
        mPresenter.loadLatestBuild(mockFetcher);
        assertEquals("최신빌드가 실패했을 때 EMPTY 빌드가 되어야 한다", Build.EMPTY, mPresenter.getBuild());
    }

    @Test
    public void testDownloadApkSuccess() {
        String responseFirst = TestUtil.getStringFromResource(getClass().getClassLoader(), "index_3_18_9.html");
        String responseApk = TestUtil.getStringFromResource(getClass().getClassLoader(), "index_3_18_9_apk.html");

        BuildFetcher mockFetcher = mock(BuildFetcher.class);

        when(mockFetcher.fetchBuildList("")).thenReturn(Single.just(responseFirst));
        when(mockFetcher.fetchBuildList("3.18.9/")).thenReturn(Single.just(responseApk));

        Build mockBuild = new Build("3.18.9/", "17년 07월 31일 14시 14분", "app-playstore-armeabi-v7a-qatest.apk");
        grantPermissions(Manifest.permission.WRITE_EXTERNAL_STORAGE);

        mPresenter.loadLatestBuild(mockFetcher);
        mPresenter.downloadApk();

        ShadowDownloadManager.ShadowRequest shadowRequest = getShadowRequest();
        assertDownloadApkSuccess(mockBuild, shadowRequest);
    }

    @Test
    public void testDownloadApkFail() {
        mPresenter.setBuild(new Build("", "", "asdf"));
        mPresenter.setState(MainContract.State.DOWNLOAD);

        ShadowApplication shadowApplication = shadowOf(mActivity.getApplication());
        shadowApplication.grantPermissions(Manifest.permission.WRITE_EXTERNAL_STORAGE);
        mPresenter.downloadApk();

        assertState(MainContract.State.DOWNLOADING, mPresenter.getState());
        mActivity.downloadCompleteReceiver.onReceive(mActivity, new Intent());
        assertState(MainContract.State.FAIL, ((MainPresenter) mActivity.getPresenter()).getState());
    }

    @Test
    public void testEditCurrentBuild() {
        String helloWorld = "hello world";
        mPresenter.editCurrentBuild(helloWorld, BuildEditContract.FLAG.EDIT);
        BuildEditDialog buildEditDialog = (BuildEditDialog) mActivity.getSupportFragmentManager().findFragmentByTag(BuildEditDialog.class.getSimpleName());
        assertNotNull(buildEditDialog);
        assertEquals("build 수정할때 넣는 버전 경로가 dialog argument에 있어야한다.", helloWorld, buildEditDialog.getArguments().get(BuildEditContract.KEY_VERSION_PATH));
        assertEquals("Build 수정할 때 넣는 Flag가 dialog argument에 있어야한다.", BuildEditContract.FLAG.EDIT, buildEditDialog.getArguments().get(BuildEditContract.KEY_FLAG));
    }

    @Test
    public void testGetMyAbiBuildSuccess() {
        BuildList buildList = new BuildList();
        buildList.add(new Build("app-playstore-arm64-v8a-qatest.apk", "31-Jul-2017 14:14", ""));
        buildList.add(new Build("app-playstore-armeabi-v7a-qatest.apk", "31-Jul-2017 14:14", ""));
        buildList.add(new Build("app-playstore-x86-qatest.apk", "31-Jul-2017 14:14", ""));
        buildList.add(new Build("app-playstore-x86_64-qatest.apk", "31-Jul-2017 14:14", ""));

        MainPresenter.ABIWrapper mockAbiWrapper = mock(MainPresenter.ABIWrapper.class);
        when(mockAbiWrapper.getABI()).thenReturn("x86");
        Build presenterBuild = mPresenter.getMyAbiBuild(mockAbiWrapper, buildList, "3.18.9/");

        Build mockBuild = new Build("3.18.9/", "17년 07월 31일 14시 14분", "app-playstore-x86-qatest.apk");

        assertEquals("여러개 Build가 주어졌을 때 자신의 ABI에 맞는 Build를 반환해야한다.", mockBuild, presenterBuild);
    }

    @Test
    public void testGetMyAbiBuildFail() {
        BuildList buildList = new BuildList();
        buildList.add(new Build("azar-android-playstoreArm-qatest.apk", "16-Nov-2016 13:59", ""));
        buildList.add(new Build("azar-android-playstoreX86-qatest.apk", "16-Nov-2016 14:00", ""));

        MainPresenter.ABIWrapper mockAbiWrapper = mock(MainPresenter.ABIWrapper.class);
        when(mockAbiWrapper.getABI()).thenReturn("armeabi-v7a");
        Build presenterBuild = mPresenter.getMyAbiBuild(mockAbiWrapper, buildList, "3.11.0-alpha-13/");

        Build mockBuild = new Build("3.11.0-alpha-13/", "16년 11월 16일 14시 00분", "");

        assertEquals("자신의 ABI에 맞는 Build가 없으면 apkName은 공백이어야한다.", mockBuild, presenterBuild);
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
        assertSelectMyAbiBuild(mockBuild);
    }

    private void verifyLoadLatestBuildSuccess(Build mockBuild, Build actualBuild, MainContract.State actualState) {

        String versionName = mActivity.getBinding().mainVersionName.getText().toString();
        String date = mActivity.getBinding().mainDate.getText().toString();

        assertState(MainContract.State.DOWNLOAD, actualState);
        assertEquals("최신빌드가져오기가 성공하면 presenter에 mockbuild가 담긴다", mockBuild, actualBuild);
        assertEquals("최신빌드가져오기가 성공하면 activity의 TextView에도 표시되어야 한다", mockBuild.getVersionName(), versionName);
        assertEquals("최신빌드가져오기가 성공하면 activity의 TextView에도 표시되어야 한다", mockBuild.getDate(), date);
    }

    private void assertState(MainContract.State expectedState, MainContract.State actualState) {
        assertEquals(expectedState, actualState);
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

        assertEquals("각 설정마다 버튼의 상태를 바꾼다", isEnable, mActivity.getBinding().mainBtnAction.isEnabled());

        String btnString = mActivity.getBinding().mainBtnAction.getText().toString();
        String resString = mActivity.getResources().getString(stringResId);
        assertEquals("각 설정마다 버튼에 Text를 바꾼다", resString, btnString);
    }

    private void grantPermissions(String permission) {
        ShadowApplication shadowApplication = shadowOf(mActivity.getApplication());
        shadowApplication.grantPermissions(permission);
    }

    private ShadowDownloadManager.ShadowRequest getShadowRequest() {
        DownloadManager downloadManager = mActivity.getDownloadManager();
        ShadowDownloadManager shadowDownloadManager = shadowOf(downloadManager);
        assertTrue("Download를 하면 Count가 0보다 커야한다", shadowDownloadManager.getRequestCount() > 0);
        ShadowDownloadManager.ShadowRequest shadowRequest = shadowOf(shadowDownloadManager.getRequest(0));
        assertNotNull(shadowRequest);
        return shadowRequest;
    }

    private void assertDownloadApkSuccess(Build mockBuild, ShadowDownloadManager.ShadowRequest shadowRequest) {
        assertState(MainContract.State.DOWNLOADING, mPresenter.getState());
        assertEquals("Request 제목은 버전명이어야 한다", mockBuild.getVersionName(), shadowRequest.getTitle());
        assertEquals("Request 설명은 날짜여야 한다", mockBuild.getDate(), shadowRequest.getDescription());
        assertEquals("Request가 끝나면 남아있게 한다", DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED, shadowRequest.getNotificationVisibility());
    }

    private void assertSelectMyAbiBuild(Build mockBuild) {
        assertEquals("여러개 Build가 주어졌을 때 자신의 ABI에 맞는 Build를 반환해야한다.", mockBuild, mPresenter.getBuild());
        assertEquals("activity의 TextView에 versionName이 표시되어야한다.", mockBuild.getVersionName(), mActivity.getBinding().mainVersionName.getText().toString());
        assertEquals("activity의 TextView에 date가 표시되어야한다.", mockBuild.getDate(), mActivity.getBinding().mainDate.getText().toString());
    }
}