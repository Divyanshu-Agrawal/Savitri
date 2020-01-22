package com.aaptrix.savitri.activities;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.aaptrix.savitri.R;
import com.aaptrix.savitri.adapter.CertificateAdapter;
import com.aaptrix.savitri.adapter.CommentAdapter;
import com.aaptrix.savitri.databeans.CommentData;
import com.aaptrix.savitri.session.FormatDate;
import com.aaptrix.savitri.session.SharedPrefsManager;
import com.aaptrix.savitri.session.URLs;
import com.google.android.material.button.MaterialButton;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Locale;
import java.util.Objects;

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
import static com.aaptrix.savitri.session.SharedPrefsNames.KEY_USER_ID;
import static com.aaptrix.savitri.session.SharedPrefsNames.KEY_USER_ROLE;
import static com.aaptrix.savitri.session.SharedPrefsNames.USER_PREFS;

public class RenewDetails extends AppCompatActivity {

    TextView name, notes, issueAuth, validFrom, validTo, refNo, commentTitle, assignedTo, status;
    ListView certificateList;
    Toolbar toolbar;
    ListView commentsList;
    ArrayList<String> certUrl = new ArrayList<>();
    String strName, strNotes, strIssueAuth, strValidFrom, strValidTo, strRefno,
            strCertificate, strId, strStatus, strassignTo, strMarkReview;
    SharedPreferences sp;
    String orgId, sessionId, userId, userType;
    CertificateAdapter adaptor;
    EditText comment;
    ProgressBar progressBar, comProgress;
    ImageButton sendComment;
    ScrollView scrollView;
    MaterialButton renewBtn;
    ArrayList<CommentData> commentArray = new ArrayList<>();
    Handler handler = new Handler();
    Runnable runnable;
    int delay = 5000;

    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_renew_details);
        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        Objects.requireNonNull(getSupportActionBar()).setDisplayShowHomeEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        name = findViewById(R.id.compliance_name);
        notes = findViewById(R.id.compliance_notes);
        issueAuth = findViewById(R.id.compliance_issue_auth);
        validFrom = findViewById(R.id.compliance_valid_from);
        validTo = findViewById(R.id.compliance_valid_upto);
        refNo = findViewById(R.id.compliance_ref_no);
        certificateList = findViewById(R.id.certificate_listview);
        comProgress = findViewById(R.id.complete_progressbar);
        comProgress.bringToFront();
        progressBar = findViewById(R.id.progress_bar);
        commentsList = findViewById(R.id.comments);
        comment = findViewById(R.id.comment);
        sendComment = findViewById(R.id.send_comment);
        sendComment.bringToFront();
        assignedTo = findViewById(R.id.compliance_assign_to);
        commentTitle = findViewById(R.id.comment_title);
        scrollView = findViewById(R.id.scrollview);
        renewBtn = findViewById(R.id.renew_btn);
        status = findViewById(R.id.compliance_status);


        sp = getSharedPreferences(USER_PREFS, Context.MODE_PRIVATE);
        orgId = sp.getString(KEY_ORG_ID, "");
        sessionId = sp.getString(KEY_SESSION_ID, "");
        userId = sp.getString(KEY_USER_ID, "");
        userType = sp.getString(KEY_USER_ROLE, "");

        if (checkConnection()) {
            fetchComment();
        } else {
            commentsList.setVisibility(View.GONE);
            commentTitle.setVisibility(View.GONE);
        }

        Handler handler = new Handler();
        Runnable runnable;

        strName = getIntent().getStringExtra("name");
        strNotes = getIntent().getStringExtra("notes");
        strIssueAuth = getIntent().getStringExtra("issueAuth");
        strId = getIntent().getStringExtra("id");
        strValidFrom = getIntent().getStringExtra("validFrom");
        strValidTo = getIntent().getStringExtra("validTo");
        strRefno = getIntent().getStringExtra("refNo");
        strCertificate = getIntent().getStringExtra("certificate");
        strStatus = getIntent().getStringExtra("status");
        strassignTo = getIntent().getStringExtra("assignedTo");
        strMarkReview = getIntent().getStringExtra("markReview");

        if (strMarkReview.equals("1")) {
            fetchReviewData();
        }

        renewBtn.setOnClickListener(v -> {
            if (userType.equals("Admin")) {
                if (strMarkReview.equals("1")) {
                    Renew renew = new Renew(this);
                    renew.execute(orgId, sessionId, strValidFrom, strValidTo, strId, strCertificate);
                } else {
                    Intent intent = new Intent(this, RenewCompliance.class);
                    intent.putExtra("name", strName);
                    intent.putExtra("refNo", strRefno);
                    intent.putExtra("issueAuth", strIssueAuth);
                    intent.putExtra("id", strId);
                    intent.putExtra("validFrom", strValidFrom);
                    intent.putExtra("validTo", strValidTo);
                    intent.putExtra("notes", strNotes);
                    startActivity(intent);
                }
            } else {
                Intent intent = new Intent(this, RenewCompliance.class);
                intent.putExtra("name", strName);
                intent.putExtra("refNo", strRefno);
                intent.putExtra("issueAuth", strIssueAuth);
                intent.putExtra("id", strId);
                intent.putExtra("validFrom", strValidFrom);
                intent.putExtra("validTo", strValidTo);
                intent.putExtra("notes", strNotes);
                startActivity(intent);
            }
        });


        for (String aStrUrl : strCertificate.split(",")) {
            certUrl.add(aStrUrl.replace("[", "")
                    .replace("]", "")
                    .replace("\"", "")
                    .replace(" ", "")
                    .replace("\\", ""));
        }

        name.setText(strName);
        notes.setText(strNotes);
        issueAuth.setText(strIssueAuth);
        assignedTo.setText(strassignTo);
        if (strMarkReview.equals("1")) {
            status.setText("Marked for review");
        } else {
            status.setText(strStatus);
        }

//        FormatDate date = new FormatDate(strAddedOn, "yyyy-MM-dd", "dd-MM-yyyy");
//        addedOn.setText(date.format());
        FormatDate date = new FormatDate(strValidFrom, "yyyy-MM-dd", "dd-MM-yyyy");
        validFrom.setText(date.format());
        date = new FormatDate(strValidTo, "yyyy-MM-dd", "dd-MM-yyyy");
        validTo.setText(date.format());
        refNo.setText(strRefno);

        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        layoutParams.height = (int) (getResources().getDimension(R.dimen._220sdp)) * certUrl.size();
        certificateList.setLayoutParams(layoutParams);
        certificateList.setPadding(0, 0, 0, 50);
        adaptor = new CertificateAdapter(this, R.layout.list_certificate, certUrl, this);
        certificateList.setAdapter(adaptor);
        adaptor.notifyDataSetChanged();

        scrollView.setOnTouchListener((v, event) -> {
            if (comment.hasFocus()) {
                comment.clearFocus();
                hideKeyboard();
                return true;
            }
            return false;
        });

        sendComment.setOnClickListener(v -> {
            if (checkConnection()) {
                if (!TextUtils.isEmpty(comment.getText().toString())) {
                    progressBar.setVisibility(View.VISIBLE);
                    addComment(comment.getText().toString());
                    hideKeyboard();
                }
            } else {
                Toast.makeText(this, "No Internet", Toast.LENGTH_SHORT).show();
            }
        });

        comment.setOnFocusChangeListener((v, hasFocus) -> {
            if (!hasFocus) {
                hideKeyboard();
            }
        });
    }

    @Override
    public void onResume() {
        handler.postDelayed(
                runnable = () -> {
                    fetchComment();
                    handler.postDelayed(runnable, delay);
                }, delay);
        super.onResume();
    }

    @Override
    public void onPause() {
        handler.removeCallbacks(runnable);
        super.onPause();
    }

    private void fetchReviewData() {
        comProgress.setVisibility(View.VISIBLE);
        new Thread(() -> {
            try {
                HttpClient httpclient = new DefaultHttpClient();
                HttpPost httppost = new HttpPost(URLs.GET_REVIEW);
                MultipartEntityBuilder entityBuilder = MultipartEntityBuilder.create();
                entityBuilder.setMode(HttpMultipartMode.BROWSER_COMPATIBLE);
                entityBuilder.addTextBody("org_details_id", orgId);
                entityBuilder.addTextBody("app_session_id", sessionId);
                entityBuilder.addTextBody("users_details_id", userId);
                entityBuilder.addTextBody("compliance_id", strId);
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
                        } else if (result.contains("{\"MarkasReviewList\":null}") || result.isEmpty()) {
                            progressBar.setVisibility(View.GONE);
                        } else {
                            JSONObject jsonObject = new JSONObject(result);
                            JSONArray jsonArray = jsonObject.getJSONArray("MarkasReviewList");
                            for (int i = 0; i < jsonArray.length(); i++) {
                                JSONObject jObject = jsonArray.getJSONObject(i);
                                strValidFrom = jObject.getString("compliance_valid_from");
                                strValidTo = jObject.getString("compliance_valid_upto");
                                strCertificate = jObject.getString("compliance_certificates");
                            }
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    setReviewData();
                });
            } catch (IOException e) {
                e.printStackTrace();
            }
        }).start();
    }

    private void setReviewData() {
        comProgress.setVisibility(View.GONE);
        certUrl.clear();
        for (String aStrUrl : strCertificate.split(",")) {
            certUrl.add(aStrUrl.replace("[", "")
                    .replace("]", "")
                    .replace("\"", "")
                    .replace(" ", "")
                    .replace("\\", ""));
        }

        FormatDate date = new FormatDate(strValidFrom, "yyyy-MM-dd", "dd-MM-yyyy");
        validFrom.setText(date.format());
        date = new FormatDate(strValidTo, "yyyy-MM-dd", "dd-MM-yyyy");
        validTo.setText(date.format());

        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        layoutParams.height = (int) (getResources().getDimension(R.dimen._220sdp)) * certUrl.size();
        certificateList.setLayoutParams(layoutParams);
        certificateList.setPadding(0, 0, 0, 50);
        adaptor = new CertificateAdapter(this, R.layout.list_certificate, certUrl, this);
        certificateList.setAdapter(adaptor);
        adaptor.notifyDataSetChanged();
    }


    private void hideKeyboard() {
        InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(Activity.INPUT_METHOD_SERVICE);
        View view = this.getCurrentFocus();
        if (view == null) {
            view = new View(this);
        }
        inputMethodManager.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
    }

    private void fetchComment() {
        commentArray.clear();
        progressBar.setVisibility(View.GONE);
        new Thread(() -> {
            try {
                HttpClient httpclient = new DefaultHttpClient();
                HttpPost httppost = new HttpPost(URLs.ALL_COMMENTS);
                MultipartEntityBuilder entityBuilder = MultipartEntityBuilder.create();
                entityBuilder.setMode(HttpMultipartMode.BROWSER_COMPATIBLE);
                entityBuilder.addTextBody("org_details_id", orgId);
                entityBuilder.addTextBody("compliance_id", strId);
                entityBuilder.addTextBody("app_session_id", sessionId);

                HttpEntity entity = entityBuilder.build();
                httppost.setEntity(entity);
                HttpResponse response = httpclient.execute(httppost);
                HttpEntity httpEntity = response.getEntity();
                String result = EntityUtils.toString(httpEntity);
                Handler handler = new Handler(Looper.getMainLooper());
                handler.post(() -> {
                    if (result.contains("{\"allComments\":null}")) {
                        commentsList.setVisibility(View.GONE);
                        commentTitle.setVisibility(View.GONE);
                    } else {
                        try {
                            JSONObject jsonObject = new JSONObject(result);
                            JSONArray jsonArray = jsonObject.getJSONArray("allComments");
                            for (int i = 0; i < jsonArray.length(); i++) {
                                JSONObject jObject = jsonArray.getJSONObject(i);
                                CommentData data = new CommentData();
                                data.setName(jObject.getString("users_name"));
                                data.setComment(jObject.getString("task_comment"));
                                data.setDate(jObject.getString("task_comment_date"));
                                commentArray.add(data);
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                        if (commentArray.size() != 0) {
                            setComment();
                        } else {
                            commentsList.setVisibility(View.GONE);
                            commentTitle.setVisibility(View.GONE);
                        }
                    }
                });
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }

    private void setComment() {

        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(-1, -1);
        params.height = (int) getResources().getDimension(R.dimen._70sdp) * commentArray.size();
        commentsList.setLayoutParams(params);

        CommentAdapter adapter = new CommentAdapter(this, R.layout.list_comment, commentArray, this);
        commentsList.setAdapter(adapter);
        adapter.notifyDataSetChanged();
    }

    private void addComment(String comment) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss", Locale.getDefault());
        String date = sdf.format(Calendar.getInstance().getTime());
        new Thread(() -> {
            try {
                HttpClient httpclient = new DefaultHttpClient();
                HttpPost httppost = new HttpPost(URLs.ADD_COMMENT);
                MultipartEntityBuilder entityBuilder = MultipartEntityBuilder.create();
                entityBuilder.setMode(HttpMultipartMode.BROWSER_COMPATIBLE);
                entityBuilder.addTextBody("org_details_id", orgId);
                entityBuilder.addTextBody("compliance_id", strId);
                entityBuilder.addTextBody("app_session_id", sessionId);
                entityBuilder.addTextBody("users_details_id", userId);
                entityBuilder.addTextBody("task_comment", comment);
                entityBuilder.addTextBody("date", date);

                HttpEntity entity = entityBuilder.build();
                httppost.setEntity(entity);
                HttpResponse response = httpclient.execute(httppost);
                HttpEntity httpEntity = response.getEntity();
                String result = EntityUtils.toString(httpEntity);
                Handler handler = new Handler(Looper.getMainLooper());
                handler.post(() -> {
                    Log.e("res", result);
                    this.comment.getText().clear();
                    fetchComment();
                });
            } catch (Exception e) {
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

    @SuppressLint("StaticFieldLeak")
    class Renew extends AsyncTask<String, String, String> {

        @SuppressLint("StaticFieldLeak")
        private Context context;

        Renew(Context context) {
            this.context = context;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            comProgress.setVisibility(View.VISIBLE);
            progressBar.bringToFront();
        }


        @Override
        protected String doInBackground(String... params) {

            String orgId = params[0];
            String sessionId = params[1];
            String validFrom = params[2];
            String validTo = params[3];
            String compliance_id = params[4];
            String certificate = params[5];

            try {
                HttpClient httpclient = new DefaultHttpClient();
                HttpPost httppost = new HttpPost(URLs.RENEW_COMPLIANCE);
                MultipartEntityBuilder entityBuilder = MultipartEntityBuilder.create();
                entityBuilder.setMode(HttpMultipartMode.BROWSER_COMPATIBLE);
                entityBuilder.addTextBody("compliance_certificates", certificate);
                entityBuilder.addTextBody("org_details_id", orgId);
                entityBuilder.addTextBody("app_session_id", sessionId);
                entityBuilder.addTextBody("compliance_id", compliance_id);
                entityBuilder.addTextBody("compliance_valid_from", validFrom);
                entityBuilder.addTextBody("compliance_valid_upto", validTo);
                entityBuilder.addTextBody("users_details_id", userId);
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
            return null;
        }

        @Override
        protected void onPostExecute(String result) {
            if (result != null) {
                Log.e("result", result);
                try {
                    JSONObject jsonObject = new JSONObject(result);
                    if (jsonObject.getBoolean("success")) {
                        Toast.makeText(context, "Renewed Successfully", Toast.LENGTH_SHORT).show();
                        startActivity(new Intent(context, TasksActivity.class).setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP));
                    } else if (result.contains("\"success\":false,\"msg\":\"Session Expire\"")) {
                        comProgress.setVisibility(View.GONE);
                        Toast.makeText(context, "Your Session is expired please login again", Toast.LENGTH_SHORT).show();
                        SharedPrefsManager.getInstance(context).logout();
                        Intent intent = new Intent(context, AppLogin.class);
                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        startActivity(intent);
                        finish();
                    } else {
                        progressBar.setVisibility(View.GONE);
                        Toast.makeText(context, "Error Occured. Please try again", Toast.LENGTH_SHORT).show();
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
            super.onPostExecute(result);
        }
    }
}
