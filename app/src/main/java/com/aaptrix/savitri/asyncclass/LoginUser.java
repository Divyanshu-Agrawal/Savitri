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

import androidx.appcompat.app.AlertDialog;

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

    public LoginUser(Context ctx, ProgressBar progressBar) {
        this.ctx = ctx;
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
        String data;

        try {
            URL url = new URL(URLs.LOGIN_URL);
            HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();
            httpURLConnection.setRequestMethod("POST");
            httpURLConnection.setDoOutput(true);
            httpURLConnection.setDoInput(true);

            OutputStream outputStream = httpURLConnection.getOutputStream();
            BufferedWriter bufferedWriter = new BufferedWriter(new OutputStreamWriter(outputStream, StandardCharsets.UTF_8));
            data = URLEncoder.encode("users_mobileno", "UTF-8") + "=" + URLEncoder.encode(username, "UTF-8") + "&" +
                    URLEncoder.encode("users_password", "UTF-8") + "=" + URLEncoder.encode(password, "UTF-8") + "&" +
                    URLEncoder.encode("token", "UTF-8") + "=" + URLEncoder.encode(token, "UTF-8");

            outputStream.write(data.getBytes());

            bufferedWriter.write(data);
            bufferedWriter.flush();
            bufferedWriter.close();
            outputStream.flush();
            outputStream.close();
            httpURLConnection.connect();

            int responseCode = httpURLConnection.getResponseCode();
            if (responseCode == HttpURLConnection.HTTP_OK) {
                InputStream inputStream = httpURLConnection.getInputStream();
                BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.ISO_8859_1));
                StringBuilder response = new StringBuilder();
                String line;

                while ((line = bufferedReader.readLine()) != null) {
                    response.append(line);
                }

                bufferedReader.close();
                inputStream.close();
                httpURLConnection.disconnect();
                return response.toString();
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    protected void onPostExecute(String result) {
        Log.e("result", result);
        if (result != null) {
            progressBar.setVisibility(View.GONE);
            try {
                JSONObject jsonObject = new JSONObject(result);
                if (jsonObject.getBoolean("success")) {
                    JSONArray jsonArray = jsonObject.getJSONArray("result");
                    JSONObject jObject = jsonArray.getJSONObject(0);
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
                    editor.putInt(KEY_GRACE, Integer.parseInt(jObject.getString("grace_period")));
                    editor.putString(KEY_PLAN_EXPIRE_DATE, jObject.getString("org_plan_expire_date"));
                    editor.putInt(KEY_COMPLIANCE_COUNT, Integer.parseInt(jObject.getString("plan_compliance_limit")));
                    editor.putInt(KEY_MEMBER_COUNT, Integer.parseInt(jObject.getString("plan_user_assign_limit")));
                    editor.putInt(KEY_DATA_DOWNLOAD, Integer.parseInt(jObject.getString("plan_data_download")));
                    editor.putInt(KEY_STORAGE_CYCLE, Integer.parseInt(jObject.getString("plan_data_storage_cycle")));
                    editor.apply();
                    Intent intent = new Intent(ctx, Dashboard.class);
                    ctx.startActivity(intent);
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
                    Toast.makeText(ctx, jsonObject.getString("msg"), Toast.LENGTH_SHORT).show();
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        super.onPostExecute(result);
    }
}
