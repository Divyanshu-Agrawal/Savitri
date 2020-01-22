package com.aaptrix.savitri.asyncclass;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.aaptrix.savitri.activities.AppLogin;
import com.aaptrix.savitri.activities.CompliancesActivity;
import com.aaptrix.savitri.session.SharedPrefsManager;
import com.aaptrix.savitri.session.URLs;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.util.ArrayList;

import cz.msebera.android.httpclient.HttpEntity;
import cz.msebera.android.httpclient.HttpResponse;
import cz.msebera.android.httpclient.client.HttpClient;
import cz.msebera.android.httpclient.client.methods.HttpPost;
import cz.msebera.android.httpclient.entity.mime.HttpMultipartMode;
import cz.msebera.android.httpclient.entity.mime.MultipartEntityBuilder;
import cz.msebera.android.httpclient.entity.mime.content.FileBody;
import cz.msebera.android.httpclient.impl.client.DefaultHttpClient;
import cz.msebera.android.httpclient.util.EntityUtils;

import static com.aaptrix.savitri.session.SharedPrefsNames.COM_PREFS;
import static com.aaptrix.savitri.session.SharedPrefsNames.FLAG;

public class UploadCompliance extends AsyncTask<String, String, String> {

    @SuppressLint("StaticFieldLeak")
    private Context context;
    @SuppressLint("StaticFieldLeak")
    private RelativeLayout progressBar;
    private ArrayList<File> filepath;
    private String type;

    public UploadCompliance(Context context, RelativeLayout progressBar, ArrayList<File> filepath, String type) {
        this.context = context;
        this.progressBar = progressBar;
        this.filepath = filepath;
        this.type = type;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        if (progressBar != null) {
            progressBar.setVisibility(View.VISIBLE);
            progressBar.bringToFront();
        }
    }


    @Override
    protected String doInBackground(String... params) {

        String orgId = params[0];
        String sessionId = params[9];
        String name = params[1];
        String refNo = params[2];
        String issueAuth = params[3];
        String notes = params[4];
        String validFrom = params[5];
        String validTo = params[6];
        String userId = params[7];
        String otherAuth = params[8];

        try {
            ArrayList<String> fileNames = new ArrayList<>();
            for (int i = 0; i < filepath.size(); i++) {
                try {
                    HttpClient httpclient = new DefaultHttpClient();
                    HttpPost httppost = new HttpPost(URLs.ADD_COMPLIANCE);
                    MultipartEntityBuilder entityBuilder = MultipartEntityBuilder.create();
                    entityBuilder.setMode(HttpMultipartMode.BROWSER_COMPATIBLE);
                    FileBody image = new FileBody(filepath.get(i));
                    entityBuilder.addPart("image", image);
                    entityBuilder.addTextBody("org_details_id", orgId);
                    entityBuilder.addTextBody("app_session_id", sessionId);
                    HttpEntity entity = entityBuilder.build();
                    httppost.setEntity(entity);
                    HttpResponse response = httpclient.execute(httppost);
                    HttpEntity httpEntity = response.getEntity();
                    String result = EntityUtils.toString(httpEntity);
                    Log.e("image", String.valueOf(result));
                    if (result.contains("\"success\":false,\"msg\":\"Session Expire\"")) {
                        Toast.makeText(context, "Your Session is expired please login again", Toast.LENGTH_SHORT).show();
                        SharedPrefsManager.getInstance(context).logout();
                        Intent intent = new Intent(context, AppLogin.class);
                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        context.startActivity(intent);
                    } else {
                        JSONObject jsonObject = new JSONObject(result);
                        fileNames.add("\"" + jsonObject.getString("imageNm") + "\"");
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            try {
                HttpClient httpclient = new DefaultHttpClient();
                HttpPost httppost = new HttpPost(URLs.ADD_COMPLIANCE);
                MultipartEntityBuilder entityBuilder = MultipartEntityBuilder.create();
                entityBuilder.setMode(HttpMultipartMode.BROWSER_COMPATIBLE);
                entityBuilder.addTextBody("compliance_certificates", fileNames.toString());
                entityBuilder.addTextBody("org_details_id", orgId);
                entityBuilder.addTextBody("compliance_name", name);
                entityBuilder.addTextBody("compliance_reference_no", refNo);
                entityBuilder.addTextBody("compliance_notes", notes);
                entityBuilder.addTextBody("users_details_id", userId);
                entityBuilder.addTextBody("app_session_id", sessionId);
                if (issueAuth.equals("Other")) {
                    entityBuilder.addTextBody("compliance_issuing_auth", issueAuth);
                    entityBuilder.addTextBody("compliance_issuing_auth_other", otherAuth);
                } else {
                    entityBuilder.addTextBody("compliance_issuing_auth", issueAuth);
                }
                entityBuilder.addTextBody("compliance_valid_from", validFrom);
                entityBuilder.addTextBody("compliance_valid_upto", validTo);
                HttpEntity entity = entityBuilder.build();
                httppost.setEntity(entity);
                HttpResponse response = httpclient.execute(httppost);
                HttpEntity httpEntity = response.getEntity();
                String res = EntityUtils.toString(httpEntity);
                Log.e("res", res);
                return res;
            } catch (Exception e) {
                e.printStackTrace();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    protected void onPostExecute(String result) {
        if (result != null) {
            try {
                if (progressBar != null) {
                    progressBar.setVisibility(View.GONE);
                }
                JSONObject jsonObject = new JSONObject(result);
                if (jsonObject.getBoolean("success")) {
                    if (type.equals("online")) {
                        Toast.makeText(context, "Added Successfully", Toast.LENGTH_SHORT).show();
                        context.startActivity(new Intent(context, CompliancesActivity.class).setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP));
                    } else {
                        Toast.makeText(context, "Compliance Added Successfully", Toast.LENGTH_SHORT).show();
                        SharedPreferences sp = context.getSharedPreferences(COM_PREFS, Context.MODE_PRIVATE);
                        SharedPreferences.Editor editor = sp.edit();
                        editor.clear();
                        editor.putBoolean(FLAG, false);
                        editor.apply();
                    }
                } else if (result.contains("\"success\":false,\"msg\":\"Session Expire\"")) {
                    Toast.makeText(context, "Your Session is expired please login again", Toast.LENGTH_SHORT).show();
                    SharedPrefsManager.getInstance(context).logout();
                    Intent intent = new Intent(context, AppLogin.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    context.startActivity(intent);
                } else {
                    Toast.makeText(context, "Error Occured. Please try again", Toast.LENGTH_SHORT).show();
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        super.onPostExecute(result);
    }
}
