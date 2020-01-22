package com.aaptrix.savitri.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.PermissionChecker;
import androidx.viewpager.widget.ViewPager;

import android.Manifest;
import android.app.DownloadManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.StrictMode;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.aaptrix.savitri.R;
import com.aaptrix.savitri.adapter.FullscrAdapter;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;

import static com.aaptrix.savitri.session.SharedPrefsNames.KEY_ORG_ID;
import static com.aaptrix.savitri.session.SharedPrefsNames.USER_PREFS;
import static com.aaptrix.savitri.activities.SplashScreen.DATA_URL;

public class FullscreenView extends AppCompatActivity {

    Toolbar toolbar;
    ViewPager pager;
    ArrayList<String> certificate;
    int position;
    String strOrgId;
    long downloadID;
    File file;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fullscreen_view);
        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        setTitle("");
        pager = findViewById(R.id.viewpager);
        certificate = getIntent().getStringArrayListExtra("certificate");
        position = getIntent().getIntExtra("position", 0);

        StrictMode.VmPolicy.Builder builder = new StrictMode.VmPolicy.Builder();
        StrictMode.setVmPolicy(builder.build());

        FullscrAdapter adapter = new FullscrAdapter(this, certificate);
        pager.setAdapter(adapter);
        adapter.notifyDataSetChanged();
        pager.setCurrentItem(position);

        SharedPreferences sp = getSharedPreferences(USER_PREFS, Context.MODE_PRIVATE);
        strOrgId = sp.getString(KEY_ORG_ID, "");
        registerReceiver(onDownloadComplete,new IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE));
    }

    private void downloadFile(String url) {
        String path = Environment.DIRECTORY_DOWNLOADS;
        String downloadUrl = DATA_URL + strOrgId + "/compliances/" + url;
        DownloadManager.Request request = new DownloadManager.Request(Uri.parse(downloadUrl))
                .setTitle(url)
                .setDescription("Downloading")
                .setMimeType("application/octet-stream")
                .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
                .setDestinationInExternalPublicDir(path, url);
        request.allowScanningByMediaScanner();
        DownloadManager downloadManager= (DownloadManager) getSystemService(DOWNLOAD_SERVICE);
        assert downloadManager != null;
        downloadID = downloadManager.enqueue(request);
    }

    private BroadcastReceiver onDownloadComplete = new BroadcastReceiver() {
        @Override
        public void onReceive(Context ctx, Intent intent) {
            long id = intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1);
            if (downloadID == id) {
                Toast.makeText(FullscreenView.this, "Download Completed", Toast.LENGTH_SHORT).show();
            }
        }
    };

    private void isPermissionGranted() {
        ActivityCompat.requestPermissions(this,
                new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
    }


    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == 1) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "Permission granted", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Permission denied", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        unregisterReceiver(onDownloadComplete);
        if (file != null) {
            file.delete();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.share_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
        } else if (item.getItemId() == R.id.download) {
            if (PermissionChecker.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                downloadFile(certificate.get(pager.getCurrentItem()));
            } else {
                isPermissionGranted();
            }
        } else if (item.getItemId() == R.id.share) {
            String url = DATA_URL + strOrgId + "/compliances/" + certificate.get(pager.getCurrentItem());
            String fileExt = certificate.get(pager.getCurrentItem()).substring(certificate.get(pager.getCurrentItem()).lastIndexOf(".") + 1);
            if (PermissionChecker.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                File directory = Environment.getExternalStorageDirectory();
                file = new File(directory, "temp." + fileExt);
                new Thread(() -> {
                    try {
                        URL u = new URL(url);
                        URLConnection conn = u.openConnection();
                        int contentLength = conn.getContentLength();
                        DataInputStream stream = new DataInputStream(u.openStream());
                        byte[] buffer = new byte[contentLength];
                        stream.readFully(buffer);
                        stream.close();
                        DataOutputStream fos = new DataOutputStream(new FileOutputStream(file));
                        fos.write(buffer);
                        fos.flush();
                        fos.close();
                        Intent intent = new Intent(Intent.ACTION_SEND);
                        intent.setDataAndType(Uri.fromFile(file), "application/octet-stream");
                        intent.putExtra(Intent.EXTRA_STREAM, Uri.parse("file://" + file));
                        startActivity(Intent.createChooser(intent, "Share via..."));
                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }).start();
            } else {
                isPermissionGranted();
            }
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        finish();
        super.onBackPressed();
    }
}
