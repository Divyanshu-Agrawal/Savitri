package com.aaptrix.savitri.activities;

import com.aaptrix.savitri.R;
import com.aaptrix.savitri.adapter.ComplianceAdapter;
import com.aaptrix.savitri.databeans.ComplianceData;
import com.aaptrix.savitri.session.SharedPrefsManager;
import com.aaptrix.savitri.session.URLs;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.content.PermissionChecker;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import cz.msebera.android.httpclient.HttpEntity;
import cz.msebera.android.httpclient.HttpResponse;
import cz.msebera.android.httpclient.client.HttpClient;
import cz.msebera.android.httpclient.client.methods.HttpPost;
import cz.msebera.android.httpclient.entity.mime.HttpMultipartMode;
import cz.msebera.android.httpclient.entity.mime.MultipartEntityBuilder;
import cz.msebera.android.httpclient.impl.client.DefaultHttpClient;
import cz.msebera.android.httpclient.util.EntityUtils;

import android.Manifest;
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
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

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
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;
import java.lang.reflect.Type;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Locale;
import java.util.Objects;

import static com.aaptrix.savitri.session.SharedPrefsNames.COM_ADDED;
import static com.aaptrix.savitri.session.SharedPrefsNames.COM_AUTH;
import static com.aaptrix.savitri.session.SharedPrefsNames.COM_CERT;
import static com.aaptrix.savitri.session.SharedPrefsNames.COM_NAME;
import static com.aaptrix.savitri.session.SharedPrefsNames.COM_NOTES;
import static com.aaptrix.savitri.session.SharedPrefsNames.COM_OTHER_AUTH;
import static com.aaptrix.savitri.session.SharedPrefsNames.COM_PREFS;
import static com.aaptrix.savitri.session.SharedPrefsNames.COM_REF;
import static com.aaptrix.savitri.session.SharedPrefsNames.COM_VALID_FROM;
import static com.aaptrix.savitri.session.SharedPrefsNames.COM_VALID_TO;
import static com.aaptrix.savitri.session.SharedPrefsNames.FLAG;
import static com.aaptrix.savitri.session.SharedPrefsNames.KEY_COMPLIANCE_COUNT;
import static com.aaptrix.savitri.session.SharedPrefsNames.KEY_COM_COUNT;
import static com.aaptrix.savitri.session.SharedPrefsNames.KEY_DATA_DOWNLOAD;
import static com.aaptrix.savitri.session.SharedPrefsNames.KEY_ORG_ID;
import static com.aaptrix.savitri.session.SharedPrefsNames.KEY_ORG_NAME;
import static com.aaptrix.savitri.session.SharedPrefsNames.KEY_SESSION_ID;
import static com.aaptrix.savitri.session.SharedPrefsNames.KEY_USER_ID;
import static com.aaptrix.savitri.session.SharedPrefsNames.KEY_USER_ROLE;
import static com.aaptrix.savitri.session.SharedPrefsNames.USER_PREFS;
import static java.security.AccessController.getContext;

public class CompliancesActivity extends AppCompatActivity {

    Toolbar toolbar;
    ListView listView;
    FloatingActionButton addCompliance;
    ComplianceAdapter adapter;
    ArrayList<ComplianceData> complianceArray = new ArrayList<>();
    TextView noCompliance;
    ProgressBar progressBar;
    String strUserType, strOrgId, strSessionId, strUserId;
    int complianceCount, dataDownload;
    SwipeRefreshLayout swipeRefreshLayout;
    RelativeLayout progress;
    RelativeLayout layout;
    SharedPreferences sp;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_compliances);
        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        Objects.requireNonNull(getSupportActionBar()).setDisplayShowHomeEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        listView = findViewById(R.id.compliance_listview);
        addCompliance = findViewById(R.id.add_compliance);
        noCompliance = findViewById(R.id.no_compliance);
        progressBar = findViewById(R.id.progress_bar);
        swipeRefreshLayout = findViewById(R.id.swipe_refresh);
        progress = findViewById(R.id.progress_layout);
        layout = findViewById(R.id.layout);

        sp = getSharedPreferences(USER_PREFS, Context.MODE_PRIVATE);
        strUserType = sp.getString(KEY_USER_ROLE, "");
        complianceCount = sp.getInt(KEY_COMPLIANCE_COUNT, 0);
        strOrgId = sp.getString(KEY_ORG_ID, "");
        strUserId = sp.getString(KEY_USER_ID, "");
        strSessionId = sp.getString(KEY_SESSION_ID, "");
        dataDownload = sp.getInt(KEY_DATA_DOWNLOAD, 0);
        progressBar.setVisibility(View.VISIBLE);
        setCompliance();

        swipeRefreshLayout.setOnRefreshListener(() -> {
            complianceArray.clear();
            swipeRefreshLayout.setRefreshing(true);
            listView.setEnabled(false);
            setCompliance();
        });

        if (strUserType.equals("Admin")) {
            addCompliance.setVisibility(View.VISIBLE);
        } else {
            addCompliance.setVisibility(View.GONE);
        }

        addCompliance.setOnClickListener(v -> {
            if (sp.getInt(KEY_COM_COUNT, 0) < complianceCount) {
                startActivity(new Intent(this, AddNewCompliance.class));
            } else {
                new AlertDialog.Builder(this)
                        .setMessage("Please upgrade your plan to add more compliances.")
                        .setPositiveButton("Upgrade", (dialog, which) -> startActivity(new Intent(this, PlansActivity.class)))
                        .setNegativeButton("Close", null)
                        .show();
            }
        });

        StrictMode.VmPolicy.Builder builder = new StrictMode.VmPolicy.Builder();
        StrictMode.setVmPolicy(builder.build());
    }

    private void setCompliance() {
        if (checkConnection()) {
            noCompliance.setVisibility(View.GONE);
            fetchCompliance();
        } else {
            Snackbar snackbar = Snackbar.make(layout, "No Internet Connection", Snackbar.LENGTH_INDEFINITE)
                    .setActionTextColor(Color.WHITE)
                    .setAction("Ok", null);
            snackbar.show();
            try {
                File directory = this.getFilesDir();
                ObjectInputStream in = new ObjectInputStream(new FileInputStream(new File(directory, "complianceData")));
                String json = in.readObject().toString();
                in.close();
                JSONObject jsonObject = new JSONObject(json);
                JSONArray jsonArray = jsonObject.getJSONArray("complianceList");
                for (int i = 0; i < jsonArray.length(); i++) {
                    JSONObject jObject = jsonArray.getJSONObject(i);
                    ComplianceData data = new ComplianceData();
                    data.setId(jObject.getString("compliance_id"));
                    data.setName(jObject.getString("compliance_name"));
                    data.setIssueAuth(jObject.getString("compliance_issuing_auth"));
                    data.setAddedDate(jObject.getString("compliance_added_date"));
                    data.setCertificate(jObject.getString("compliance_certificates"));
                    data.setNotes(jObject.getString("compliance_notes"));
                    data.setOtherAuth(jObject.getString("compliance_issuing_auth_other"));
                    data.setRefNo(jObject.getString("compliance_reference_no"));
                    data.setValidfrom(jObject.getString("compliance_valid_from"));
                    data.setValidTo(jObject.getString("compliance_valid_upto"));
                    complianceArray.add(data);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            if (complianceArray.size() == 0) {
                noCompliance.setVisibility(View.VISIBLE);
                progressBar.setVisibility(View.GONE);
            } else {
                listItem();
            }
        }
    }

    private void fetchCompliance() {
        new Thread(() -> {
            try {
                HttpClient httpclient = new DefaultHttpClient();
                HttpPost httppost = new HttpPost(URLs.ALL_COMPLIANCE);
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
                            progressBar.setVisibility(View.GONE);
                            Toast.makeText(this, "Your Session is expired please login again", Toast.LENGTH_SHORT).show();
                            SharedPrefsManager.getInstance(this).logout();
                            Intent intent = new Intent(this, AppLogin.class);
                            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                            startActivity(intent);
                            finish();
                        } else if (result.equals("{\"complianceList\":null}")) {
                            noCompliance.setVisibility(View.VISIBLE);
                            progressBar.setVisibility(View.GONE);
                        } else {
                            JSONObject jsonObject = new JSONObject(result);
                            cacheJson(jsonObject);
                            JSONArray jsonArray = jsonObject.getJSONArray("complianceList");
                            for (int i = 0; i < jsonArray.length(); i++) {
                                JSONObject jObject = jsonArray.getJSONObject(i);
                                ComplianceData data = new ComplianceData();
                                data.setId(jObject.getString("compliance_id"));
                                data.setName(jObject.getString("compliance_name"));
                                data.setIssueAuth(jObject.getString("compliance_issuing_auth"));
                                data.setAddedDate(jObject.getString("compliance_added_date"));
                                data.setCertificate(jObject.getString("compliance_certificates"));
                                data.setNotes(jObject.getString("compliance_notes"));
                                data.setOtherAuth(jObject.getString("compliance_issuing_auth_other"));
                                data.setRefNo(jObject.getString("compliance_reference_no"));
                                data.setValidfrom(jObject.getString("compliance_valid_from"));
                                data.setValidTo(jObject.getString("compliance_valid_upto"));
                                complianceArray.add(data);
                            }
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                        noCompliance.setVisibility(View.VISIBLE);
                        progressBar.setVisibility(View.GONE);
                    }
                    listItem();
                });
            } catch (IOException e) {
                e.printStackTrace();
            }
        }).start();
    }

    private void listItem() {
        SharedPreferences comPrefs = getSharedPreferences(COM_PREFS, Context.MODE_PRIVATE);
        if (comPrefs.getBoolean(FLAG, false)) {
            ComplianceData data = new ComplianceData();
            data.setId(null);
            data.setName(comPrefs.getString(COM_NAME, ""));
            data.setIssueAuth(comPrefs.getString(COM_AUTH, "Other"));
            data.setAddedDate(comPrefs.getString(COM_ADDED, ""));
            data.setCertificate(null);
            data.setNotes(comPrefs.getString(COM_NOTES, ""));
            data.setOtherAuth(comPrefs.getString(COM_OTHER_AUTH, ""));
            data.setRefNo(comPrefs.getString(COM_REF, ""));
            data.setValidfrom(comPrefs.getString(COM_VALID_FROM, ""));
            data.setValidTo(comPrefs.getString(COM_VALID_TO, ""));
            complianceArray.add(data);
        }

        Collections.sort(complianceArray, (o1, o2) -> {
            SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy", Locale.getDefault());
            try {
                return sdf.parse(o1.getAddedDate()).compareTo(sdf.parse(o2.getAddedDate()));
            } catch (ParseException e) {
                e.printStackTrace();
            }
            return 0;
        });
        sp.edit().putInt(KEY_COM_COUNT, complianceArray.size()).apply();
        progressBar.setVisibility(View.GONE);
        swipeRefreshLayout.setRefreshing(false);
        adapter = new ComplianceAdapter(this, R.layout.list_compliances, complianceArray);
        listView.setAdapter(adapter);
        listView.setEnabled(true);
        adapter.notifyDataSetChanged();
    }

    private void cacheJson(final JSONObject jsonObject) {
        new Thread(() -> {
            ObjectOutput out;
            String data = jsonObject.toString();
            try {
                if (getContext() != null) {
                    File directory = this.getFilesDir();
                    directory.mkdir();
                    out = new ObjectOutputStream(new FileOutputStream(new File(directory, "complianceData")));
                    out.writeObject(data);
                    out.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }).start();
    }

    public boolean checkConnection() {
        ConnectivityManager connec;
        connec = (ConnectivityManager) getSystemService(CONNECTIVITY_SERVICE);
        assert connec != null;
        return connec.getActiveNetworkInfo() != null && connec.getActiveNetworkInfo().isAvailable() && connec.getActiveNetworkInfo().isConnectedOrConnecting();
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
                if (complianceArray != null) {
                    if (PermissionChecker.checkSelfPermission(this,
                            Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                        progress.setVisibility(View.VISIBLE);
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

                        for (int i = 0; i < complianceArray.size(); i++) {
                            row = sheet.createRow(++rowCount);
                            name = row.createCell(0);
                            name.setCellValue(complianceArray.get(i).getName());
                            refNo = row.createCell(1);
                            refNo.setCellValue(complianceArray.get(i).getRefNo());
                            auth = row.createCell(2);
                            if (complianceArray.get(i).getIssueAuth().equals("Other")) {
                                auth.setCellValue(complianceArray.get(i).getOtherAuth());
                            } else {
                                auth.setCellValue(complianceArray.get(i).getIssueAuth());
                            }
                            notes = row.createCell(5);
                            notes.setCellValue(complianceArray.get(i).getNotes());
                            added = row.createCell(6);
                            added.setCellValue(complianceArray.get(i).getAddedDate());
                            from = row.createCell(3);
                            from.setCellValue(complianceArray.get(0).getValidfrom());
                            upto = row.createCell(4);
                            upto.setCellValue(complianceArray.get(0).getValidTo());
                        }

                        try {
                            File storageDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
                            String date = new SimpleDateFormat("dd_MM_yyyy_hh_mm_ss", Locale.getDefault()).format(Calendar.getInstance().getTime());
                            String fileName = getSharedPreferences(USER_PREFS, Context.MODE_PRIVATE).getString(KEY_ORG_NAME, "")
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
                            intent.putExtra(Intent.EXTRA_STREAM, Uri.parse("file://" + file));
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
                    } else {
                        isPermissionGranted();
                    }
                }
            } else {
                new AlertDialog.Builder(this)
                        .setTitle("Please upgrade your plan to download reports..")
                        .setPositiveButton("Upgrade", (dialog, which) -> startActivity(new Intent(this, PlansActivity.class)))
                        .setNegativeButton("Cancel", null)
                        .setCancelable(false)
                        .show();
            }
        }
        return true;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.download_menu, menu);
        return super.onCreateOptionsMenu(menu);
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
}
