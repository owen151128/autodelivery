package com.hpcnt.autodelivery.ui;

import android.Manifest;
import android.app.DownloadManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.databinding.DataBindingUtil;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.hpcnt.autodelivery.R;
import com.hpcnt.autodelivery.databinding.ActivityMainBinding;
import com.hpcnt.autodelivery.model.Build;
import com.hpcnt.autodelivery.ui.dialog.BuildEditDialog;

import java.io.File;

public class MainActivity extends AppCompatActivity implements MainContract.View {
    private static final String TAG = MainActivity.class.getSimpleName();

    private DownloadManager downloadManager;
    private ActivityMainBinding binding;
    private MainContract.Presenter mPresenter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_main);
        binding.setAction(this);
        downloadManager = (DownloadManager) getSystemService(Context.DOWNLOAD_SERVICE);
        mPresenter = new MainPresenter(this);
        mPresenter.loadLatestBuild();
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
                mPresenter.loadLatestBuild();
                return true;
            case R.id.menu_edit:
                mPresenter.setEditBuild();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onResume() {
        super.onResume();
        IntentFilter downloadCompleteFilter = new IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE);
        registerReceiver(downloadCompleteReceiver, downloadCompleteFilter);
    }

    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(downloadCompleteReceiver);
    }

    public void onClickBtnAction(View view) {
        mPresenter.onClickButton();
    }

    @Override
    public void showLastestBuild(Build lastestBuild) {
        binding.mainVersionName.setText(lastestBuild.getVersionName());
        binding.mainDate.setText(lastestBuild.getDate());
    }

    @Override
    public void showToast(String response) {
        Toast.makeText(getApplicationContext(), response, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void showButton(MainContract.STATE state) {
        int stringResId = 0;
        boolean isEnable = false;
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
        }
        binding.mainBtnAction.setEnabled(isEnable);
        binding.mainBtnAction.setText(stringResId);
    }

    @Override
    public void showApkInstall(String apkPath) {
        Uri uri = null;
        if (android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.N) {
            uri = Uri.parse("file://" + apkPath);
        } else {
            File file = new File(apkPath);
            uri = FileProvider.getUriForFile(this, getApplicationContext().getPackageName() + ".provider", file);
        }
        Intent installIntent = new Intent(Intent.ACTION_VIEW);
        installIntent.setDataAndType(uri, "application/vnd.android.package-archive");
        installIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        startActivity(installIntent);
    }

    @Override
    public void showEditDialog() {
        BuildEditDialog buildEditDialog = BuildEditDialog.newInstance();
        buildEditDialog.show(getSupportFragmentManager(), BuildEditDialog.class.getSimpleName());
    }

    @Override
    public void addDownloadRequest(DownloadManager.Request request) {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                == PackageManager.PERMISSION_GRANTED) {
            downloadManager.enqueue(request);
        } else {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                    MainContract.PERMISSION_REQUEST_WRITE_EXTERNAL_STORAGE);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
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

    private BroadcastReceiver downloadCompleteReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            mPresenter.downloadComplete();
        }
    };
}
