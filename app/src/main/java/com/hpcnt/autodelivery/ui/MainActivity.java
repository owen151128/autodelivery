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
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.hpcnt.autodelivery.R;
import com.hpcnt.autodelivery.databinding.ActivityMainBinding;
import com.hpcnt.autodelivery.model.Build;

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
        mPresenter.loadLastestBuild();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.menu_main, menu);
        return true;
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
        Button button = (Button) view;
        if (button.getText().toString().equals("Download")) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                    == PackageManager.PERMISSION_GRANTED) {
                mPresenter.downloadApk();
            } else {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                        MainContract.PERMISSION_REQUEST_WRITE_EXTERNAL_STORAGE);
            }
        } else if (button.getText().toString().equals("Install")) {
            mPresenter.installApk();
        }
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
    public void showDownload() {
        binding.mainBtnAction.setEnabled(true);
        binding.mainBtnAction.setText("Download");
    }

    @Override
    public void showLoading() {
        binding.mainBtnAction.setEnabled(false);
        binding.mainBtnAction.setText("Loading...");
    }

    @Override
    public void showDownloading() {
        binding.mainBtnAction.setEnabled(false);
        binding.mainBtnAction.setText("Downloading...");

    }

    @Override
    public void showInstall() {
        binding.mainBtnAction.setEnabled(true);
        binding.mainBtnAction.setText("Install");
    }

    @Override
    public void showApkInstall(String apkPath) {
        Intent installIntent = new Intent(Intent.ACTION_VIEW);
        installIntent.setDataAndType(Uri.parse(apkPath), "application/vnd.android.package-archive");
        startActivity(installIntent);
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
                binding.mainBtnAction.setText("Permission Denied");
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
