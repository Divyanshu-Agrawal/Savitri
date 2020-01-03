package com.aaptrix.savitri.activities;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.aaptrix.savitri.R;
import com.aaptrix.savitri.session.FormatDate;
import com.aaptrix.savitri.session.SharedPrefsManager;
import com.aaptrix.savitri.session.URLs;
import com.google.android.material.snackbar.Snackbar;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;

import cz.msebera.android.httpclient.HttpEntity;
import cz.msebera.android.httpclient.HttpResponse;
import cz.msebera.android.httpclient.client.HttpClient;
import cz.msebera.android.httpclient.client.methods.HttpPost;
import cz.msebera.android.httpclient.entity.mime.HttpMultipartMode;
import cz.msebera.android.httpclient.entity.mime.MultipartEntityBuilder;
import cz.msebera.android.httpclient.impl.client.DefaultHttpClient;
import cz.msebera.android.httpclient.util.EntityUtils;

import static com.aaptrix.savitri.session.SharedPrefsNames.KEY_ORG_ID;
import static com.aaptrix.savitri.session.SharedPrefsNames.KEY_SESSION_ID;
import static com.aaptrix.savitri.session.SharedPrefsNames.KEY_USER_ROLE;
import static com.aaptrix.savitri.session.SharedPrefsNames.USER_PREFS;
import static java.security.AccessController.getContext;

public class OrgProfile extends AppCompatActivity {

    Toolbar toolbar;
    ProgressBar progressBar;
    TextView name, type, phone, address, details, addedOn, planExpire;
    String strUserRole, strOrgId, strSessionId;
    String strName, strType, strPhone, strDetails, strCity, strState, strDistrict, strPincode, strAddress;
    RelativeLayout layout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_org_profile);
        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        progressBar = findViewById(R.id.progress_bar);
        name = findViewById(R.id.org_name);
        type = findViewById(R.id.org_type);
        phone = findViewById(R.id.org_contact);
        address = findViewById(R.id.org_address);
        details = findViewById(R.id.org_details);
        addedOn = findViewById(R.id.org_added);
        planExpire = findViewById(R.id.org_plan_expire);
        layout = findViewById(R.id.layout);

        SharedPreferences sp = getSharedPreferences(USER_PREFS, Context.MODE_PRIVATE);
        strUserRole = sp.getString(KEY_USER_ROLE, "");
        strOrgId = sp.getString(KEY_ORG_ID, "");
        strSessionId = sp.getString(KEY_SESSION_ID, "");

        if (checkConnection()) {
            fetchOrgDetails();
        } else {
            Snackbar snackbar = Snackbar.make(layout, "No Internet Connection", Snackbar.LENGTH_INDEFINITE)
                    .setActionTextColor(Color.WHITE)
                    .setAction("Ok", null);
            snackbar.show();
            try {
                File directory = this.getFilesDir();
                ObjectInputStream in = new ObjectInputStream(new FileInputStream(new File(directory, "orgData")));
                String json = in.readObject().toString();
                in.close();
                JSONObject jsonObject = new JSONObject(json);
                cacheJson(jsonObject);
                JSONArray jsonArray = jsonObject.getJSONArray("orgDetails");
                JSONObject jObject = jsonArray.getJSONObject(0);
                name.setText(jObject.getString("org_name"));
                type.setText(jObject.getString("org_type"));
                phone.setText(jObject.getString("org_landline_no"));
                String add = jObject.getString("org_address") + ", " + jObject.getString("org_city") + ", " +
                        jObject.getString("org_state") + ", (" + jObject.getString("org_pincode") + ")";
                address.setText(add);
                details.setText(jObject.getString("org_details"));
                FormatDate date = new FormatDate(jObject.getString("org_added_date"), "yyyy-MM-dd", "dd-MM-yyyy");
                addedOn.setText(date.format());
                date = new FormatDate(jObject.getString("org_plan_expire_date"), "yyyy-MM-dd", "dd-MM-yyyy");
                planExpire.setText(date.format());
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private void fetchOrgDetails() {
        progressBar.setVisibility(View.VISIBLE);
        new Thread(() -> {
            try {
                HttpClient httpclient = new DefaultHttpClient();
                HttpPost httppost = new HttpPost(URLs.ORG_DETAILS);
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
                        progressBar.setVisibility(View.GONE);
                        if (result.contains("\"success\":false,\"msg\":\"Session Expire\"")){
                            Toast.makeText(this, "Your Session is expired please login again", Toast.LENGTH_SHORT).show();
                            SharedPrefsManager.getInstance(this).logout();
                            Intent intent = new Intent(this, AppLogin.class);
                            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                            startActivity(intent);
                            finish();
                        } else if (result.equals("null")) {
                            Toast.makeText(this, "Error Occured", Toast.LENGTH_SHORT).show();
                        }  else {
                            Log.e("result", result);
                            JSONObject jsonObject = new JSONObject(result);
                            cacheJson(jsonObject);
                            JSONArray jsonArray = jsonObject.getJSONArray("orgDetails");
                            JSONObject jObject = jsonArray.getJSONObject(0);
                            name.setText(jObject.getString("org_name"));
                            type.setText(jObject.getString("org_type"));
                            phone.setText(jObject.getString("org_landline_no"));
                            String add = jObject.getString("org_address") + ", " + jObject.getString("org_city") + ", " +
                                    jObject.getString("org_state") + ", (" + jObject.getString("org_pincode") + ")";
                            address.setText(add);
                            address.setTextColor(Color.BLACK);
                            details.setText(jObject.getString("org_details"));
                            FormatDate date = new FormatDate(jObject.getString("org_added_date"), "yyyy-MM-dd", "dd-MM-yyyy");
                            addedOn.setText(date.format());
                            date = new FormatDate(jObject.getString("org_plan_expire_date"), "yyyy-MM-dd", "dd-MM-yyyy");
                            planExpire.setText(date.format());

                            strName = jObject.getString("org_name");
                            strAddress = jObject.getString("org_address");
                            strPhone = jObject.getString("org_landline_no");
                            strType = jObject.getString("org_type");
                            strCity = jObject.getString("org_city");
                            strState = jObject.getString("org_state");
                            strPincode = jObject.getString("org_pincode");
                            strDetails = jObject.getString("org_details");
                            strDistrict = jObject.getString("org_district");
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                });
            } catch (IOException e) {
                e.printStackTrace();
            }
        }).start();
    }

    private void cacheJson(final JSONObject jsonObject) {
        new Thread(() -> {
            ObjectOutput out;
            String data = jsonObject.toString();
            try {
                if (getContext() != null) {
                    File directory = this.getFilesDir();
                    directory.mkdir();
                    out = new ObjectOutputStream(new FileOutputStream(new File(directory, "orgData")));
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

//    @Override
//    public boolean onCreateOptionsMenu(Menu menu) {
//        if (strUserRole.contains("Admin")) {
//            getMenuInflater().inflate(R.menu.edit_menu, menu);
//        }
//        return super.onCreateOptionsMenu(menu);
//    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
        }
//        else if (item.getItemId() == R.id.edit) {
//            if (checkConnection()) {
//                Intent intent = new Intent(this, UpdateOrgDetails.class);
//                intent.putExtra("name", strName);
//                intent.putExtra("phone", strPhone);
//                intent.putExtra("type", strType);
//                intent.putExtra("details", strDetails);
//                intent.putExtra("address", strAddress);
//                intent.putExtra("city", strCity);
//                intent.putExtra("district", strDistrict);
//                intent.putExtra("state", strState);
//                intent.putExtra("pincode", strPincode);
//                startActivity(intent);
//            } else {
//                Snackbar snackbar = Snackbar.make(layout, "No Internet Connection", Snackbar.LENGTH_LONG)
//                        .setActionTextColor(Color.WHITE)
//                        .setAction("Ok", null);
//                snackbar.show();
//            }
//        }
        return super.onOptionsItemSelected(item);
    }
}
