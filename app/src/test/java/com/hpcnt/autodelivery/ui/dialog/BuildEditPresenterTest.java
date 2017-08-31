package com.hpcnt.autodelivery.ui.dialog;

import com.hpcnt.autodelivery.BuildConfig;
import com.hpcnt.autodelivery.TestUtil;
import com.hpcnt.autodelivery.network.BuildFetcher;
import com.hpcnt.autodelivery.ui.MainActivity;

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

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNotNull;
import static junit.framework.Assert.assertNotSame;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(RobolectricTestRunner.class)
@Config(constants = BuildConfig.class)
public class BuildEditPresenterTest {

    private MainActivity activity;
    private BuildEditDialog mView;
    private BuildEditPresenter mPresenter;

    @Before
    public void setUp() throws Exception {
        RxJavaPlugins.setIoSchedulerHandler(scheduler -> Schedulers.trampoline());
        activity = Robolectric.buildActivity(MainActivity.class).create().get();
    }

    @After
    public void tearDown() throws Exception {
        RxJavaPlugins.reset();
    }

    @Test
    public void testSetList() throws Exception {
        initMember("", BuildEditContract.FLAG.EDIT);
        mPresenter.setList(new BuildEditAdapter());

        assertNotNull("list를 설정하면 LayoutManager에 값이 있어야한다", mView.getBinding().editDialogList.getLayoutManager());
        assertNotNull("list를 설정하면 adapter에 값이 있어야한다", mView.getBinding().editDialogList.getAdapter());
    }

    @Test
    public void testLoadBuildListEditFlagSuccess() throws Exception {
        testLoadBuildListSuccess("", BuildEditContract.FLAG.EDIT, "index_3_18_9.html", 14);
    }

    @Test
    public void testLoadBuildListApkFlagSuccess() throws Exception {
        testLoadBuildListSuccess("3.18.9/", BuildEditContract.FLAG.APK, "index_3_18_9_apk.html", 4);
    }

    private void testLoadBuildListSuccess(String versionPath, BuildEditContract.FLAG expectedFlag, String htmlPath, int expectedSize) {
        initMember(versionPath, expectedFlag);

        String response = getResString(htmlPath);

        BuildFetcher mockFetcher = mock(BuildFetcher.class);
        when(mockFetcher.fetchBuildList(versionPath)).thenReturn(Single.just(response));

        mPresenter.setList(new BuildEditAdapter());

        mPresenter.loadBuildList(mockFetcher, versionPath);
        assertEquals("성공적으로 buildlist를 불러오면 flag가 설정되어야 한다", expectedFlag, mPresenter.getFlag());
        assertEquals("성공적으로 buildlist를 불러오면 list가 설정된다", expectedSize, mPresenter.getAdapterModel().getCount());
    }

    @Test
    public void testOnItemClick() throws Exception {
        String versionPath = "";
        initMember(versionPath, BuildEditContract.FLAG.EDIT);
        mPresenter.setList(new BuildEditAdapter());

        String response = getResString("index_3_18_9.html");
        String responseApk = getResString("index_3_18_9_apk.html");

        BuildFetcher mockFetcher = mock(BuildFetcher.class);
        when(mockFetcher.fetchBuildList(versionPath)).thenReturn(Single.just(response));
        when(mockFetcher.fetchBuildList("3.14.0-alpha-1/")).thenReturn(Single.just(responseApk));
        mPresenter.loadBuildList(mockFetcher, versionPath);

        mPresenter.onItemClick(mockFetcher, binding.editDialogCurrentTitle.getText().toString(), "3.14.0");
        assertEquals("버전 하위 단위가 하나일땐 그 다음 요소를 선택해야한다 ", "3.14.0-alpha-1/", activity.getBinding().mainVersionName.getText().toString());

        mPresenter.onItemClick(mockFetcher, binding.editDialogCurrentTitle.getText().toString(), "3.18");
        assertEquals("버전 하위 단위가 여러개일 땐 adapter list에 설정되어야 한다", 7, mPresenter.getAdapterModel().getCount());
        assertEquals("버전 하위 단위가 여러개일 땐 현재 버전을 TextView에 보여줘야한다", "3.18", mView.getBinding().editDialogCurrentTitle.getText().toString());
    }

    private void initMember(String versionPath, BuildEditContract.FLAG flag) {
        activity.getPresenter().editCurrentBuild(versionPath, flag);
        mView = (BuildEditDialog) activity.getSupportFragmentManager()
                .findFragmentByTag(BuildEditDialog.class.getSimpleName());
        mView.onCreateView(activity.getLayoutInflater(), null, activity.getIntent().getExtras());
        mPresenter = (BuildEditPresenter) mView.getPresenter();
    }

    private String getResString(String resource) {
        String responseFirst = TestUtil.getStringFromResource(getClass().getClassLoader(), resource);
        assertNotSame("파일을 읽었을 때 공백이면 안된다", "", responseFirst);
        return responseFirst;
    }
}