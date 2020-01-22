package com.aaptrix.savitri.asyncclass;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.aaptrix.savitri.activities.Dashboard;
import com.aaptrix.savitri.activities.MemberDashboard;
import com.aaptrix.savitri.activities.PlansActivity;
import com.aaptrix.savitri.session.URLs;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;

import cz.msebera.android.httpclient.HttpEntity;
import cz.msebera.android.httpclient.HttpResponse;
import cz.msebera.android.httpclient.client.HttpClient;
import cz.msebera.android.httpclient.client.methods.HttpPost;
import cz.msebera.android.httpclient.entity.mime.HttpMultipartMode;
import cz.msebera.android.httpclient.entity.mime.MultipartEntityBuilder;
import cz.msebera.android.httpclient.impl.client.DefaultHttpClient;
import cz.msebera.android.httpclient.util.EntityUtils;

import static com.aaptrix.savitri.session.SharedPrefsNames.KEY_COMPLIANCE_COUNT;
import static com.aaptrix.savitri.session.SharedPrefsNames.KEY_DATA_DOWNLOAD;
import static com.aaptrix.savitri.session.SharedPrefsNames.KEY_GRACE;
import static com.aaptrix.savitri.session.SharedPrefsNames.KEY_MEMBER_COUNT;
import static com.aaptrix.savitri.session.SharedPrefsNames.KEY_ORG_ID;
import static com.aaptrix.savitri.session.SharedPrefsNames.KEY_ORG_NAME;
import static com.aaptrix.savitri.session.SharedPrefsNames.KEY_ORG_PLAN_TYPE;
import static com.aaptrix.savitri.session.SharedPrefsNames.KEY_PLAN_EXPIRE_DATE;
import static com.aaptrix.savitri.session.SharedPrefsNames.KEY_SESSION_ID;
import static com.aaptrix.savitri.session.SharedPrefsNames.KEY_STORAGE_CYCLE;
import static com.aaptrix.savitri.session.SharedPrefsNames.KEY_USEREMAIL;
import static com.aaptrix.savitri.session.SharedPrefsNames.KEY_USERPHONE;
import static com.aaptrix.savitri.session.SharedPrefsNames.KEY_USER_ID;
import static com.aaptrix.savitri.session.SharedPrefsNames.KEY_USER_IMAGE;
import static com.aaptrix.savitri.session.SharedPrefsNames.KEY_USER_NAME;
import static com.aaptrix.savitri.session.SharedPrefsNames.KEY_USER_ROLE;
import static com.aaptrix.savitri.session.SharedPrefsNames.USER_PREFS;

public class LoginUser extends AsyncTask<String, String, String> {

    @SuppressLint("StaticFieldLeak")
    private Context ctx;
    @SuppressLint("StaticFieldLeak")
    private ProgressBar progressBar;
    private String type;

    public LoginUser(Context ctx, @Nullable ProgressBar progressBar, String type) {
        this.ctx = ctx;
        this.type = type;
        if (type.equals("login"))
            this.progressBar = progressBar;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
    }

    @Override
    protected String doInBackground(String... params) {

        String username = params[0];
        String password = params[1];
        String token = params[2];

        try {
            HttpClient httpclient = new DefaultHttpClient();
            HttpPost httppost = new HttpPost(URLs.LOGIN_URL);
            MultipartEntityBuilder entityBuilder = MultipartEntityBuilder.create();
            entityBuilder.setMode(HttpMultipartMode.BROWSER_COMPATIBLE);
            entityBuilder.addTextBody("users_mobileno", username);
            entityBuilder.addTextBody("users_password", password);
            entityBuilder.addTextBody("token", token);

            HttpEntity entity = entityBuilder.build();
            httppost.setEntity(entity);
            HttpResponse response = httpclient.execute(httppost);
            HttpEntity httpEntity = response.getEntity();
            String result = EntityUtils.toString(httpEntity);
            return result;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    protected void onPostExecute(String result) {
        if (result != null) {
            if (type.equals("login"))
                progressBar.setVisibility(View.GONE);
            try {
                JSONObject jsonObject = new JSONObject(result);
                if (jsonObject.getBoolean("success")) {
                    JSONArray jsonArray = jsonObject.getJSONArray("result");
                    JSONObject jObject = jsonArray.getJSONObject(0);
                    if (type.equals("login"))
                        Toast.makeText(ctx, "Login Successfull", Toast.LENGTH_SHORT).show();
                    SharedPreferences sp = ctx.getSharedPreferences(USER_PREFS, Context.MODE_PRIVATE);
                    SharedPreferences.Editor editor = sp.edit();
                    editor.putString(KEY_USER_ID, jObject.getString("users_details_id"));
                    editor.putString(KEY_USER_NAME, jObject.getString("users_name"));
                    editor.putString(KEY_USER_ROLE, jObject.getString("users_type"));
                    editor.putString(KEY_USER_IMAGE, jObject.getString("users_profile_img"));
                    editor.putString(KEY_ORG_ID, jObject.getString("org_details_id"));
                    editor.putString(KEY_ORG_PLAN_TYPE, jObject.getString("org_plan_type"));
                    editor.putString(KEY_ORG_NAME, jObject.getString("org_name"));
                    editor.putString(KEY_SESSION_ID, jObject.getString("app_session_id"));
                    editor.putString(KEY_USERPHONE, jObject.getString("users_mobileno"));
                    editor.putString(KEY_USEREMAIL, jObject.getString("users_email"));
                    editor.putInt(KEY_GRACE, Integer.parseInt(jObject.getString("grace_period")));
                    editor.putString(KEY_PLAN_EXPIRE_DATE, jObject.getString("org_plan_expire_date"));
                    editor.putInt(KEY_COMPLIANCE_COUNT, Integer.parseInt(jObject.getString("plan_compliance_limit")));
                    editor.putInt(KEY_MEMBER_COUNT, Integer.parseInt(jObject.getString("plan_user_assign_limit")));
                    editor.putInt(KEY_DATA_DOWNLOAD, Integer.parseInt(jObject.getString("plan_data_download")));
                    editor.putInt(KEY_STORAGE_CYCLE, Integer.parseInt(jObject.getString("plan_data_storage_cycle")));
                    editor.apply();
                    if (type.equals("login")) {
                        if (jObject.getString("users_type").equals("Admin")) {
                            Intent intent = new Intent(ctx, Dashboard.class);
                            ctx.startActivity(intent);
                        } else {
                            ctx.startActivity(new Intent(ctx, MemberDashboard.class));
                        }
                    }
                } else if (jsonObject.getString("msg").contains("Plan Inactive")) {
                    JSONArray jsonArray = jsonObject.getJSONArray("result");
                    JSONObject jObject = jsonArray.getJSONObject(0);
                    SharedPreferences sp = ctx.getSharedPreferences(USER_PREFS, Context.MODE_PRIVATE);
                    SharedPreferences.Editor editor = sp.edit();
                    editor.putString(KEY_USER_ID, jObject.getString("users_details_id"));
                    editor.putString(KEY_ORG_ID, jObject.getString("org_details_id"));
                    editor.putString(KEY_SESSION_ID, jObject.getString("app_session_id"));
                    editor.putString(KEY_PLAN_EXPIRE_DATE, jObject.getString("org_plan_expire_date"));
                    editor.apply();
                    new AlertDialog.Builder(ctx).setMessage("Your plan has expired. Please purchase a plan to continue")
                            .setPositiveButton("Purchase Plan", (dialog, which) -> ctx.startActivity(new Intent(ctx, PlansActivity.class).setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)))
                            .setNegativeButton("Cancel", (dialog, which) -> System.exit(0))
                            .setCancelable(false)
                            .show();
                } else {
                    if (type.equals("login"))
                        Toast.makeText(ctx, jsonObject.getString("msg"), Toast.LENGTH_SHORT).show();
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        super.onPostExecute(result);
    }
}
