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

import com.aaptrix.savitri.activities.AppLogin;
import com.aaptrix.savitri.activities.TasksActivity;
import com.aaptrix.savitri.session.SharedPrefsManager;
import com.aaptrix.savitri.session.URLs;

import org.json.JSONException;
import org.json.JSONObject;

import cz.msebera.android.httpclient.HttpEntity;
import cz.msebera.android.httpclient.HttpResponse;
import cz.msebera.android.httpclient.client.HttpClient;
import cz.msebera.android.httpclient.client.methods.HttpPost;
import cz.msebera.android.httpclient.entity.mime.HttpMultipartMode;
import cz.msebera.android.httpclient.entity.mime.MultipartEntityBuilder;
import cz.msebera.android.httpclient.impl.client.DefaultHttpClient;
import cz.msebera.android.httpclient.util.EntityUtils;

import static com.aaptrix.savitri.session.SharedPrefsNames.FLAG;
import static com.aaptrix.savitri.session.SharedPrefsNames.TASK_PREFS;

@SuppressLint("StaticFieldLeak")
public class UploadTask extends AsyncTask<String, String, String> {

    @SuppressLint("StaticFieldLeak")
    private Context context;
    private ProgressBar progressBar;
    private String type;

    public UploadTask(Context context, ProgressBar progressBar, String type) {
        this.context = context;
        this.progressBar = progressBar;
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
        String userId = params[1];
        String sessionId = params[2];
        String name = params[3];
        String desc = params[4];
        String dueDate = params[5];
        String assignPeople = params[6];

        try {
            HttpClient httpclient = new DefaultHttpClient();
            HttpPost httppost = new HttpPost(URLs.ADD_TASK);
            MultipartEntityBuilder entityBuilder = MultipartEntityBuilder.create();
            entityBuilder.setMode(HttpMultipartMode.BROWSER_COMPATIBLE);
            entityBuilder.addTextBody("org_details_id", orgId);
            entityBuilder.addTextBody("tasks_details_name", name);
            entityBuilder.addTextBody("tasks_details_desc", desc);
            entityBuilder.addTextBody("tasks_details_due_date", dueDate);
            entityBuilder.addTextBody("users_details_id", userId);
            entityBuilder.addTextBody("assign_users", assignPeople);
            entityBuilder.addTextBody("app_session_id", sessionId);
            HttpEntity entity = entityBuilder.build();
            httppost.setEntity(entity);
            HttpResponse response = httpclient.execute(httppost);
            HttpEntity httpEntity = response.getEntity();
            return EntityUtils.toString(httpEntity);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    protected void onPostExecute(String result) {
        if (result != null) {
            Log.e("result", result);
            if (progressBar != null) {
                progressBar.setVisibility(View.GONE);
            }
            try {
                JSONObject jsonObject = new JSONObject(result);
                if (jsonObject.getBoolean("success")) {
                    if (type.equals("online")) {
                        Toast.makeText(context, "Added Successfully", Toast.LENGTH_SHORT).show();
                        context.startActivity(new Intent(context, TasksActivity.class).setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP));
                    } else {
                        Toast.makeText(context, "Task Added Successfully", Toast.LENGTH_SHORT).show();
                        SharedPreferences sp = context.getSharedPreferences(TASK_PREFS, Context.MODE_PRIVATE);
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
