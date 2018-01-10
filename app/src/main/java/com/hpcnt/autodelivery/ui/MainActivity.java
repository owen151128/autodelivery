package com.hpcnt.autodelivery.ui;

import android.Manifest;
import android.app.DownloadManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.databinding.DataBindingUtil;
import android.net.ConnectivityManager;
import android.net.Uri;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.SystemClock;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AlertDialog;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Toast;

import com.hpcnt.autodelivery.BaseApplication;
import com.hpcnt.autodelivery.BuildConfig;
import com.hpcnt.autodelivery.R;
import com.hpcnt.autodelivery.databinding.ActivityMainBinding;
import com.hpcnt.autodelivery.model.Build;
import com.hpcnt.autodelivery.network.BuildFetcher;
import com.hpcnt.autodelivery.ui.dialog.BuildEditContract;
import com.hpcnt.autodelivery.ui.dialog.BuildEditDialog;
import com.hpcnt.autodelivery.util.ABIWrapper;
import com.hpcnt.autodelivery.util.RxSelectorEventUtil;
import com.hpcnt.autodelivery.util.NetworkStateUtil;
import com.trello.rxlifecycle2.components.support.RxAppCompatActivity;

import java.io.File;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.exceptions.UndeliverableException;
import io.reactivex.plugins.RxJavaPlugins;
import io.reactivex.schedulers.Schedulers;

public class MainActivity extends RxAppCompatActivity implements MainContract.View {

    private static final int UNINSTALL_ACTIVITY = 1000;
    private static final String AZAR_PACKAGE = "com.azarlive.android";
    private DownloadManager downloadManager;
    private AlertDialog.Builder alertDialogBuilder;
    private NetworkStateUtil networkStateUtil;
    private long downloadQueueId;
    private long timer;
    private String apkPath;
    private RxSelectorEventUtil selectorEventUtil;
    private boolean isShowSelectFragment;
    private ActivityMainBinding binding;
    private MainContract.Presenter mPresenter;

    /**
     * I/O 수행시 Activity 의 onPause 가 불릴 경우 UndeliverableException 이 BuildFetcher 에서 발생하여
     * 크래시가 발생한다. 따라서 UndeliverableException 의 대한 Error Handling 이 필요하다.
     * RxJavaPlugins.setErrorHandler 을 이용 하면 가능
     * 해당 작업을 onCreate 에서 하는 이유는 onStart 에서 BuildFetcher를 사용하기 때문에 onStart 전에 처리
     * 되어야 한다.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_main);
        binding.setAction(this);
        RxJavaPlugins.setErrorHandler(throwable -> {
            if (throwable instanceof UndeliverableException) {
                throwable.printStackTrace();
            }
        });
        downloadManager = (DownloadManager) getSystemService(Context.DOWNLOAD_SERVICE);
        mPresenter = new MainPresenter(this);
        alertDialogBuilder = new AlertDialog.Builder(MainActivity.this);
        networkStateUtil = NetworkStateUtil.getInstance((ConnectivityManager) getApplicationContext()
                        .getSystemService(CONNECTIVITY_SERVICE)
                , (WifiManager) getApplicationContext().getSystemService(WIFI_SERVICE));
        binding.version.setText(BuildConfig.VERSION_NAME);
        binding.mainAdbi.setText(new ABIWrapper().getABI());
        binding.mainBtnAction.setOnLongClickListener(v -> {
            mPresenter.onLongClickButton();
            return true;
        });
        selectorEventUtil = RxSelectorEventUtil.getInstance();
        selectorEventUtil.receiveSelectorEvent()
                .subscribeOn(Schedulers.single())
                .compose(this.bindToLifecycle())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(s -> binding.modeText.setText(s));
    }

    @Override
    protected void onStart() {
        super.onStart();
        checkNetwork();
        BaseApplication.setNormalMode();
        binding.modeText.setText(getString(R.string.selector_qa));
        mPresenter.setCurrentFlag(BuildEditContract.FLAG.EDIT);
        mPresenter.loadLatestBuild(new BuildFetcher(this));
    }

    @Override
    protected void onDestroy() {
        selectorEventUtil.close();
        super.onDestroy();
    }

    @Override
    public void onBackPressed() {
        if (isShowSelectFragment) {
            mPresenter.editCurrentBuild("", BuildEditContract.FLAG.SELECTOR);
            isShowSelectFragment = false;
        } else {
            long now = SystemClock.uptimeMillis();
            if (now - timer < 2000)
                super.onBackPressed();
            else {
                showToast(R.string.back_button_toast);
                timer = now;
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == UNINSTALL_ACTIVITY) {
            Uri uri;
            if (android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.N) {
                uri = Uri.parse("file://" + apkPath);
            } else {
                File file = new File(apkPath);
                uri = FileProvider.getUriForFile(this,
                        getApplicationContext().getPackageName() + ".provider", file);
            }
            Intent installIntent = new Intent(Intent.ACTION_VIEW);
            installIntent.setDataAndType(uri, "application/vnd.android.package-archive");
            installIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            startActivity(installIntent);
        }
    }

    @Override
    public void makeDialog(String title, String message,
                           boolean isAlert, boolean cancelable, View view,
                           DialogInterface.OnClickListener onYesListener,
                           DialogInterface.OnClickListener onNoListener) {
        alertDialogBuilder.setTitle(title).setMessage(message)
                .setPositiveButton("OK", onYesListener);
        if (isAlert) {
            alertDialogBuilder.setNegativeButton(null, null);
        } else {
            alertDialogBuilder.setNegativeButton("NO", onNoListener);
        }
        if (cancelable)
            alertDialogBuilder.setCancelable(true);
        else
            alertDialogBuilder.setCancelable(false);

        alertDialogBuilder.setView(view);

        alertDialogBuilder.show();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_refresh:
                binding.modeText.setText(getString(R.string.selector_qa));
                checkNetwork();
                mPresenter.loadLatestBuild(new BuildFetcher(this));
                return true;
            case R.id.menu_selector:
                mPresenter.editCurrentBuild("", BuildEditContract.FLAG.SELECTOR);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        IntentFilter downloadCompleteFilter =
                new IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE);
        registerReceiver(downloadCompleteReceiver, downloadCompleteFilter);
    }

    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(downloadCompleteReceiver);
    }

    public void onClickBtnAction(View view) {
        if (networkStateUtil.isNetworkConnected()) {
            if (networkStateUtil.isAvailableNetwork()) {
                mPresenter.onClickButton();
            } else {
                makeDialog("오류"
                        , "WI-FI 가 빌드 서버에 접속할수 없는 망 입니다. 빌드서버에 접속할수 있는 망으로 접속 해주세요."
                        , true, false, null, null, null);
            }
        } else {
            makeDialog("오류"
                    , "WI-FI 가 활성화 되어 있지 않습니다. WI-FI 를 활성화 하시겠습니까?"
                    , false, false, null
                    , (DialogInterface d, int w) -> networkStateUtil.setWifiEnable(), null);
        }

    }

    @Override
    public void showLastestBuild(Build lastestBuild) {
        binding.mainVersionName.setText(lastestBuild.getVersionName());
    }

    @Override
    public void showToast(String response) {
        Toast.makeText(getApplicationContext(), response, Toast.LENGTH_LONG).show();
    }

    @Override
    public void showToast(int resId) {
        Toast.makeText(getApplicationContext(), resId, Toast.LENGTH_LONG).show();
    }

    @Override
    public void showButton(MainContract.State state) {
        int stringResId;
        boolean isEnable;
        switch (state) {
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
        binding.mainBtnAction.setEnabled(isEnable);
        binding.mainBtnAction.setText(stringResId);
    }

    @Override
    public void showApkInstall(String apkPath) {
        this.apkPath = apkPath;
        Uri uri;
        try {
            ApplicationInfo info = getApplicationContext().getPackageManager().getApplicationInfo(AZAR_PACKAGE, 0);
            Intent unInstallIntent = new Intent(Intent.ACTION_UNINSTALL_PACKAGE)
                    .setData(Uri.parse("package:" + AZAR_PACKAGE));
            startActivityForResult(unInstallIntent, UNINSTALL_ACTIVITY);
        } catch (PackageManager.NameNotFoundException e) {
            if (android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.N) {
                uri = Uri.parse("file://" + apkPath);
            } else {
                File file = new File(apkPath);
                uri = FileProvider.getUriForFile(this,
                        getApplicationContext().getPackageName() + ".provider", file);
            }
            Intent installIntent = new Intent(Intent.ACTION_VIEW);
            installIntent.setDataAndType(uri, "application/vnd.android.package-archive");
            installIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            startActivity(installIntent);
        }
    }

    @Override
    public void showApkDeleteDialog(String build, String apkPath) {
        makeDialog("경고", build + " apk 를 삭제하시겠습니까?", false, true, null, (dialog, which) -> {
            File file = new File(apkPath);
            if (file.delete()) {
                showToast(build + " apk 가 성공적으로 삭제 되었습니다.");
            } else {
                showToast("오류 : " + build + " apk 삭제 를 실패 하였습니다.");
            }
            BaseApplication.setNormalMode();
            mPresenter.setCurrentFlag(BuildEditContract.FLAG.EDIT);
            mPresenter.loadLatestBuild(new BuildFetcher(this));
        }, null);
    }

    @Override
    public void showEditDialog(String versionPath, BuildEditContract.FLAG flag) {
        BuildEditDialog buildEditDialog = BuildEditDialog.newInstance(versionPath, flag);
        if (flag == BuildEditContract.FLAG.SELECTOR) {
            isShowSelectFragment = false;
            buildEditDialog.show(getSupportFragmentManager(), BuildEditDialog.class.getSimpleName());
            buildEditDialog.setOnDismissSelectorListener((result) -> {
                if (result.equals(getString(R.string.selector_qa))) {
                    mPresenter.editCurrentBuild("", BuildEditContract.FLAG.EDIT);
                } else if (result.equals(getString(R.string.selector_master))) {
                    mPresenter.editCurrentBuild("", BuildEditContract.FLAG.MASTER);
                } else if (result.equals(getString(R.string.selector_pr))) {
                    mPresenter.editCurrentBuild("pr/", BuildEditContract.FLAG.PR);
                }
            });
        } else {
            switch (flag) {
                case MASTER:
                    BaseApplication.setMasterBranchMode();
                    mPresenter.setCurrentFlag(flag);
                    break;
                case APK:
                    mPresenter.setCurrentFlag(flag);
                    break;
                case MASTER_APK:
                    mPresenter.setCurrentFlag(flag);
                    break;
                default:
                    BaseApplication.setNormalMode();
                    mPresenter.setCurrentFlag(flag);
            }
            buildEditDialog.setOnDismissListener(
                    (buildList, versionName) -> {
                        if (flag == BuildEditContract.FLAG.MASTER) {
                            mPresenter.setCurrentFlag(BuildEditContract.FLAG.MASTER_APK);
                            mPresenter.editCurrentBuild(versionName, BuildEditContract.FLAG.MASTER_APK);
                        }
                        mPresenter.selectMyAbiBuild(buildList, versionName);
                    });
            buildEditDialog.setOnDismissApkListener(apkName -> mPresenter.setApkName(apkName));
            buildEditDialog.setOnDismissBuildListener(build -> {
                InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                inputMethodManager.toggleSoftInput(InputMethodManager.RESULT_UNCHANGED_SHOWN, InputMethodManager.RESULT_UNCHANGED_SHOWN);
                mPresenter.setBuild(build);
                showLastestBuild(build);
                mPresenter.setApkName(build.getApkName());
            });
            buildEditDialog.setmOnDismissBackListener(() -> {
                if (flag != BuildEditContract.FLAG.APK && flag != BuildEditContract.FLAG.MASTER_APK) {
                    isShowSelectFragment = true;
                    onBackPressed();
                }
            });
            buildEditDialog.show(getSupportFragmentManager(), BuildEditDialog.class.getSimpleName());
        }
    }

    @Override
    public void addDownloadRequest(DownloadManager.Request request) {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                == PackageManager.PERMISSION_GRANTED) {
            downloadQueueId = downloadManager.enqueue(request);
        } else {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                    MainContract.PERMISSION_REQUEST_WRITE_EXTERNAL_STORAGE);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == MainContract.PERMISSION_REQUEST_WRITE_EXTERNAL_STORAGE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                mPresenter.downloadApk();
            } else {
                binding.mainBtnAction.setEnabled(false);
                binding.mainBtnAction.setText(R.string.permission_denied);
            }
        }
    }

    @Override
    public void checkNetwork() {
        if (networkStateUtil.isNetworkConnected()) {
            if (!networkStateUtil.isAvailableNetwork()) {
                makeDialog("오류"
                        , "WI-FI 가 빌드 서버에 접속할수 없는 망 입니다. 빌드서버에 접속할수 있는 망으로 접속 해주세요."
                        , true, false, null, null, null);
            }
        } else {
            makeDialog("오류"
                    , "WI-FI 가 활성화 되어 있지 않습니다. WI-FI 를 활성화 하시겠습니까?"
                    , false, false, null
                    , (DialogInterface d, int w) -> networkStateUtil.setWifiEnable(), null);
        }
    }

    BroadcastReceiver downloadCompleteReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            DownloadManager.Query query = new DownloadManager.Query();
            query.setFilterById(downloadQueueId);
            Cursor cursor = downloadManager.query(query);
            cursor.moveToFirst();
            int status = cursor.getInt(cursor.getColumnIndex(DownloadManager.COLUMN_STATUS));
            switch (status) {
                case DownloadManager.STATUS_SUCCESSFUL:
                    mPresenter.stateSetting();
                    break;
                default:
                    mPresenter.setState(MainContract.State.FAIL);
                    break;
            }
        }
    };

    public ActivityMainBinding getBinding() {
        return binding;
    }

    DownloadManager getDownloadManager() {
        return downloadManager;
    }

    public MainContract.Presenter getPresenter() {
        return mPresenter;
    }
}
