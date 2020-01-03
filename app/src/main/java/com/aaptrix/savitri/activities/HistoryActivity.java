package com.aaptrix.savitri.activities;

import com.aaptrix.savitri.R;
import com.aaptrix.savitri.databeans.ComplianceData;
import com.aaptrix.savitri.fragment.HistoryFragment;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.cardview.widget.CardView;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.content.PermissionChecker;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentStatePagerAdapter;
import androidx.viewpager.widget.ViewPager;
import cz.msebera.android.httpclient.HttpEntity;
import cz.msebera.android.httpclient.HttpResponse;
import cz.msebera.android.httpclient.client.HttpClient;
import cz.msebera.android.httpclient.client.methods.HttpPost;
import cz.msebera.android.httpclient.entity.mime.HttpMultipartMode;
import cz.msebera.android.httpclient.entity.mime.MultipartEntityBuilder;
import cz.msebera.android.httpclient.impl.client.DefaultHttpClient;
import cz.msebera.android.httpclient.util.EntityUtils;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.os.StrictMode;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.aaptrix.savitri.session.SharedPrefsManager;
import com.aaptrix.savitri.session.URLs;
import com.google.android.material.tabs.TabLayout;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashSet;
import java.util.Locale;
import java.util.Objects;

import static com.aaptrix.savitri.session.SharedPrefsNames.KEY_DATA_DOWNLOAD;
import static com.aaptrix.savitri.session.SharedPrefsNames.KEY_ORG_ID;
import static com.aaptrix.savitri.session.SharedPrefsNames.KEY_ORG_NAME;
import static com.aaptrix.savitri.session.SharedPrefsNames.KEY_SESSION_ID;
import static com.aaptrix.savitri.session.SharedPrefsNames.KEY_STORAGE_CYCLE;
import static com.aaptrix.savitri.session.SharedPrefsNames.USER_PREFS;

public class HistoryActivity extends AppCompatActivity {

    Toolbar toolbar;
    TabLayout tabLayout;
    ViewPager pager;
    int cycleLimit, dataDownload;
    ArrayList<ComplianceData> complianceArray = new ArrayList<>();
    String strOrgId, strSessionId;
    RelativeLayout progress;

    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history);
        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        Objects.requireNonNull(getSupportActionBar()).setTitle("History");
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        pager = findViewById(R.id.viewpager);
        tabLayout = findViewById(R.id.history_tab);
        progress = findViewById(R.id.progress_layout);
        tabLayout.setupWithViewPager(pager);
        tabLayout.setTabGravity(TabLayout.GRAVITY_FILL);
        tabLayout.addTab(tabLayout.newTab().setText("Cycle 1"));
        tabLayout.addTab(tabLayout.newTab().setText("Cycle 2"));
        tabLayout.addTab(tabLayout.newTab().setText("Cycle 3"));
        tabLayout.addTab(tabLayout.newTab().setText("Cycle 4"));
        tabLayout.addTab(tabLayout.newTab().setText("Cycle 5"));
        ViewPagerAdapter adapter = new ViewPagerAdapter(getSupportFragmentManager());
        pager.setAdapter(adapter);
        adapter.notifyDataSetChanged();
        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                if (tab.getPosition() < cycleLimit) {
                    pager.setCurrentItem(tab.getPosition(), true);
                } else {
                    new AlertDialog.Builder(HistoryActivity.this)
                            .setTitle("Please upgrade your plan to access more..")
                            .setPositiveButton("Upgrade", (dialog, which) -> startActivity(new Intent(HistoryActivity.this, PlansActivity.class)))
                            .setNegativeButton("Cancel", (dialog, which) -> pager.setCurrentItem(cycleLimit - 1, true))
                            .setCancelable(false)
                            .show();
                }
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {

            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        });

        StrictMode.VmPolicy.Builder builder = new StrictMode.VmPolicy.Builder();
        StrictMode.setVmPolicy(builder.build());

        SharedPreferences sp = getSharedPreferences(USER_PREFS, Context.MODE_PRIVATE);
        cycleLimit = sp.getInt(KEY_STORAGE_CYCLE, 0);
        dataDownload = sp.getInt(KEY_DATA_DOWNLOAD, 0);
        strOrgId = sp.getString(KEY_ORG_ID, "");
        strSessionId = sp.getString(KEY_SESSION_ID, "");
    }

    private class ViewPagerAdapter extends FragmentStatePagerAdapter {

        ViewPagerAdapter(FragmentManager manager) {
            super(manager);
        }

        @NonNull
        @Override
        public Fragment getItem(int position) {
            Fragment fragment;
            Bundle bundle;
            switch (position) {
                case 0:
                    fragment = new HistoryFragment();
                    bundle = new Bundle();
                    bundle.putString("cycleCount", "1");
                    if (position >= cycleLimit)
                        bundle.putString("visibility", "no");
                    else
                        bundle.putString("visibility", "yes");
                    fragment.setArguments(bundle);
                    return fragment;
                case 1:
                    fragment = new HistoryFragment();
                    bundle = new Bundle();
                    bundle.putString("cycleCount", "2");
                    if (position >= cycleLimit)
                        bundle.putString("visibility", "no");
                    else
                        bundle.putString("visibility", "yes");
                    fragment.setArguments(bundle);
                    return fragment;
                case 2:
                    fragment = new HistoryFragment();
                    bundle = new Bundle();
                    bundle.putString("cycleCount", "3");
                    if (position >= cycleLimit)
                        bundle.putString("visibility", "no");
                    else
                        bundle.putString("visibility", "yes");
                    fragment.setArguments(bundle);
                    return fragment;
                case 3:
                    fragment = new HistoryFragment();
                    bundle = new Bundle();
                    bundle.putString("cycleCount", "4");
                    if (position >= cycleLimit)
                        bundle.putString("visibility", "no");
                    else
                        bundle.putString("visibility", "yes");
                    fragment.setArguments(bundle);
                    return fragment;
                case 4:
                    fragment = new HistoryFragment();
                    bundle = new Bundle();
                    bundle.putString("cycleCount", "5");
                    if (position >= cycleLimit)
                        bundle.putString("visibility", "no");
                    else
                        bundle.putString("visibility", "yes");
                    fragment.setArguments(bundle);
                    return fragment;
                default:
                    return null;
            }
        }

        @Override
        public int getCount() {
            return 5;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            String title = null;
            if (position == 0) {
                title = "Cycle 1";
            } else if (position == 1) {
                title = "Cycle 2";
            } else if (position == 2) {
                title = "Cycle 3";
            } else if (position == 3) {
                title = "Cycle 4";
            } else if (position == 4) {
                title = "Cycle 5";
            }
            return title;
        }

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.download_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
        } else if (item.getItemId() == R.id.download) {
            if (dataDownload == 1) {
                if (checkConnection()) {
                    if (PermissionChecker.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                        progress.bringToFront();
                        fetchCompliance();
                    } else {
                        isPermissionGranted();
                    }
                } else {
                    Toast.makeText(this, "Please connect to internet", Toast.LENGTH_SHORT).show();
                    try {
                        FileNotFoundException fe = new FileNotFoundException();
                        File directory = this.getFilesDir();
                        ObjectInputStream in = new ObjectInputStream(new FileInputStream(new File(directory, "historyData")));
                        String json = in.readObject().toString();
                        JSONObject jsonObject = new JSONObject(json);
                        JSONArray jsonArray = jsonObject.getJSONArray("complianceHistory");
                        for (int i = 0; i < jsonArray.length(); i++) {
                            JSONObject jObject = jsonArray.getJSONObject(i);
                            ComplianceData data = new ComplianceData();
                            data.setId(jObject.getString("compliance_id"));
                            data.setName(jObject.getString("compliance_name"));
                            data.setIssueAuth(jObject.getString("compliance_issuing_auth"));
                            data.setNotes(jObject.getString("compliance_notes"));
                            data.setOtherAuth(jObject.getString("compliance_issuing_auth_other"));
                            data.setRefNo(jObject.getString("compliance_reference_no"));
                            data.setRenewCount(jObject.getString("compliance_renew_count"));
                            data.setValidfrom(jObject.getString("compliance_valid_from"));
                            data.setValidTo(jObject.getString("compliance_valid_upto"));
                            data.setCertificate(jObject.getString("compliance_certificates"));
                            complianceArray.add(data);
                        }
                        in.close();
                        throw fe;
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    dataDownload();
                }
            } else {
                new AlertDialog.Builder(HistoryActivity.this)
                        .setTitle("Please upgrade your plan to download reports..")
                        .setPositiveButton("Upgrade", (dialog, which) -> startActivity(new Intent(HistoryActivity.this, PlansActivity.class)))
                        .setNegativeButton("Cancel",null)
                        .setCancelable(false)
                        .show();
            }
        }
        return super.onOptionsItemSelected(item);
    }

    private void fetchCompliance() {
        progress.setVisibility(View.VISIBLE);
        new Thread(() -> {
            try {
                HttpClient httpclient = new DefaultHttpClient();
                HttpPost httppost = new HttpPost(URLs.ALL_HISTORY);
                MultipartEntityBuilder entityBuilder = MultipartEntityBuilder.create();
                entityBuilder.setMode(HttpMultipartMode.BROWSER_COMPATIBLE);
                entityBuilder.addTextBody("org_details_id", strOrgId);
                entityBuilder.addTextBody("app_session_id", strSessionId);
                HttpEntity entity = entityBuilder.build();
                httppost.setEntity(entity);
                HttpResponse response = httpclient.execute(httppost);
                HttpEntity httpEntity = response.getEntity();
                String result = EntityUtils.toString(httpEntity);
                Handler handler = new Handler(Looper.getMainLooper());
                handler.post(() -> {
                    try {
                        Log.e("res", result);
                        if (result.contains("\"success\":false,\"msg\":\"Session Expire\"")) {
                            Toast.makeText(this, "Your Session is expired please login again", Toast.LENGTH_SHORT).show();
                            SharedPrefsManager.getInstance(this).logout();
                            Intent intent = new Intent(this, AppLogin.class);
                            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                            startActivity(intent);
                            finish();
                        } else if (result.equals("{\"complianceList\":null}")) {
                            Toast.makeText(this, "Nothing to download", Toast.LENGTH_SHORT).show();
                        } else {
                            JSONObject jsonObject = new JSONObject(result);
                            JSONArray jsonArray = jsonObject.getJSONArray("complianceHistory");
                            for (int i = 0; i < jsonArray.length(); i++) {
                                JSONObject jObject = jsonArray.getJSONObject(i);
                                ComplianceData data = new ComplianceData();
                                data.setId(jObject.getString("compliance_id"));
                                data.setName(jObject.getString("compliance_name"));
                                data.setIssueAuth(jObject.getString("compliance_issuing_auth"));
                                data.setNotes(jObject.getString("compliance_notes"));
                                data.setOtherAuth(jObject.getString("compliance_issuing_auth_other"));
                                data.setRefNo(jObject.getString("compliance_reference_no"));
                                data.setRenewCount(jObject.getString("compliance_renew_count"));
                                data.setValidfrom(jObject.getString("compliance_valid_from"));
                                data.setValidTo(jObject.getString("compliance_valid_upto"));
                                data.setCertificate(jObject.getString("compliance_certificates"));
                                complianceArray.add(data);
                            }
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    dataDownload();
                });
            } catch (IOException e) {
                e.printStackTrace();
            }
        }).start();
    }

    private void dataDownload() {
        ArrayList<ArrayList<ComplianceData>> sortedArray = new ArrayList<>();
        ArrayList<ComplianceData> viewArray = new ArrayList<>();
        ArrayList<ComplianceData> secondArray = new ArrayList<>(complianceArray);
        for (int i = 0; i < complianceArray.size(); i++) {
            ArrayList<ComplianceData> arrayList = new ArrayList<>();
            for (int j = 0; j < secondArray.size(); j++) {
                if (complianceArray.get(i).getId().equals(secondArray.get(j).getId())) {
                    arrayList.add(secondArray.get(j));
                }
            }
            if (!arrayList.isEmpty()) {
                sortedArray.add(arrayList);
            }
        }

        for (int i = 0; i < sortedArray.size(); i++) {
            for (int j = 0; j < sortedArray.get(i).size(); j++) {
                if (sortedArray.get(i).get(j).getRenewCount().equals("1")) {
                    viewArray.add(sortedArray.get(i).get(j));
                }
            }
        }

        ArrayList<ComplianceData> set = new ArrayList<>(viewArray);
        viewArray.clear();
        for (int i = 0; i < set.size(); i++) {
            if (!viewArray.contains(set.get(i))) {
                viewArray.add(set.get(i));
            }
        }

        XSSFWorkbook workbook = new XSSFWorkbook();
        XSSFSheet sheet = workbook.createSheet("Report");
        int rowCount = 1;
        XSSFRow row = sheet.createRow(0);
        XSSFCell name = row.createCell(0);
        name.setCellValue("Compliance Name");
        XSSFCell refNo = row.createCell(1);
        refNo.setCellValue("Compliance Ref. No.");
        XSSFCell auth = row.createCell(2);
        auth.setCellValue("Issuing Auth");
        XSSFCell from = row.createCell(3);
        from.setCellValue("Valid From");
        XSSFCell upto = row.createCell(4);
        upto.setCellValue("Valid Upto");
        XSSFCell notes = row.createCell(5);
        notes.setCellValue("Additional Notes");
        XSSFCell added = row.createCell(6);
        added.setCellValue("Added Date");

        for (int i = 0; i < viewArray.size(); i++) {
            ArrayList<ComplianceData> comArray = new ArrayList<>();
            for (int k = 0; k < sortedArray.size(); k++) {
                for (int j = 0; j < sortedArray.get(k).size(); j++) {
                    if (sortedArray.get(k).get(j).getId().equals(viewArray.get(i).getId())) {
                        comArray.add(sortedArray.get(k).get(j));
                    }
                }
            }
            ArrayList<ComplianceData> hashSet = new ArrayList<>(comArray);
            comArray.clear();
            for (int j = 0; j < hashSet.size(); j++) {
                if (!comArray.contains(hashSet.get(j))) {
                    comArray.add(hashSet.get(j));
                }
            }
            row = sheet.createRow(++rowCount);
            name = row.createCell(0);
            name.setCellValue(viewArray.get(i).getName());
            refNo = row.createCell(1);
            refNo.setCellValue(viewArray.get(i).getRefNo());
            auth = row.createCell(2);
            if (viewArray.get(i).getIssueAuth().equals("Other")) {
                auth.setCellValue(viewArray.get(i).getOtherAuth());
            } else {
                auth.setCellValue(viewArray.get(i).getIssueAuth());
            }
            notes = row.createCell(5);
            notes.setCellValue(viewArray.get(i).getNotes());
            added = row.createCell(6);
            added.setCellValue(viewArray.get(i).getAddedDate());
            from = row.createCell(3);
            from.setCellValue(comArray.get(0).getValidfrom());
            upto = row.createCell(4);
            upto.setCellValue(comArray.get(0).getValidTo());
            for (int j = 1; j < comArray.size(); j++) {
                row = sheet.createRow(++rowCount);
                from = row.createCell(3);
                from.setCellValue(comArray.get(j).getValidfrom());
                upto = row.createCell(4);
                upto.setCellValue(comArray.get(j).getValidTo());
            }
            rowCount++;
        }

        try {
            File storageDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
            String date = new SimpleDateFormat("dd_MM_yyyy_hh_mm_ss", Locale.getDefault()).format(Calendar.getInstance().getTime());
            String fileName = getSharedPreferences(USER_PREFS, Context.MODE_PRIVATE).getString(KEY_ORG_NAME,"")
                    + "_Compliance_Report_" + date + ".xlsx";
            File file = new File(storageDir, fileName);
            file.createNewFile();
            FileOutputStream outputStream = new FileOutputStream(file);
            workbook.write(outputStream);
            outputStream.close();
            Toast.makeText(this, "Report Saved in Downloads", Toast.LENGTH_SHORT).show();
            progress.setVisibility(View.GONE);
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setDataAndType(Uri.fromFile(file), "application/vnd.ms-excel");
            PendingIntent contentIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
            NotificationCompat.Builder b = new NotificationCompat.Builder(this);
            b.setAutoCancel(true)
                    .setDefaults(Notification.DEFAULT_ALL)
                    .setWhen(System.currentTimeMillis())
                    .setSmallIcon(R.mipmap.ic_launcher)
                    .setContentTitle(fileName)
                    .setContentText("Download Complete")
                    .setDefaults(Notification.DEFAULT_LIGHTS | Notification.DEFAULT_SOUND)
                    .setContentIntent(contentIntent);
            NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                int importance = NotificationManager.IMPORTANCE_HIGH;
                NotificationChannel notificationChannel = new NotificationChannel("00", "NOTIFICATION_CHANNEL_NAME", importance);
                notificationChannel.enableLights(true);
                notificationChannel.setLightColor(Color.RED);
                notificationChannel.enableVibration(true);
                notificationChannel.setVibrationPattern(new long[]{100, 200, 300});
                b.setChannelId("00");
                notificationManager.createNotificationChannel(notificationChannel);
            }
            notificationManager.notify(1, b.build());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void isPermissionGranted() {
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

    private boolean checkConnection() {
        ConnectivityManager connec;
        connec = (ConnectivityManager) getSystemService(CONNECTIVITY_SERVICE);
        assert connec != null;
        return connec.getActiveNetworkInfo() != null && connec.getActiveNetworkInfo().isAvailable() && connec.getActiveNetworkInfo().isConnectedOrConnecting();
    }
}
