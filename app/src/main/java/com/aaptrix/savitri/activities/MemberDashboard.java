package com.aaptrix.savitri.activities;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.Uri;
import android.os.Bundle;

import com.aaptrix.savitri.R;
import com.aaptrix.savitri.adapter.TasksAdapter;
import com.aaptrix.savitri.databeans.ComplianceData;
import com.aaptrix.savitri.session.SharedPrefsManager;
import com.aaptrix.savitri.session.URLs;
import com.google.android.material.button.MaterialButton;

import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.core.app.ShareCompat;
import androidx.core.view.GravityCompat;
import androidx.appcompat.app.ActionBarDrawerToggle;

import android.view.MenuItem;

import com.google.android.material.navigation.NavigationView;
import com.squareup.picasso.Picasso;

import androidx.drawerlayout.widget.DrawerLayout;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

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
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Locale;

import cz.msebera.android.httpclient.HttpEntity;
import cz.msebera.android.httpclient.HttpResponse;
import cz.msebera.android.httpclient.client.HttpClient;
import cz.msebera.android.httpclient.client.methods.HttpPost;
import cz.msebera.android.httpclient.entity.mime.HttpMultipartMode;
import cz.msebera.android.httpclient.entity.mime.MultipartEntityBuilder;
import cz.msebera.android.httpclient.impl.client.DefaultHttpClient;
import cz.msebera.android.httpclient.util.EntityUtils;
import de.hdodenhof.circleimageview.CircleImageView;

import static com.aaptrix.savitri.session.SharedPrefsNames.KEY_ORG_ID;
import static com.aaptrix.savitri.session.SharedPrefsNames.KEY_ORG_PLAN_TYPE;
import static com.aaptrix.savitri.session.SharedPrefsNames.KEY_PLAN_NAME;
import static com.aaptrix.savitri.session.SharedPrefsNames.KEY_SESSION_ID;
import static com.aaptrix.savitri.session.SharedPrefsNames.KEY_USER_ID;
import static com.aaptrix.savitri.session.SharedPrefsNames.KEY_USER_IMAGE;
import static com.aaptrix.savitri.session.SharedPrefsNames.KEY_USER_NAME;
import static com.aaptrix.savitri.session.SharedPrefsNames.KEY_USER_ROLE;
import static com.aaptrix.savitri.session.SharedPrefsNames.USER_PREFS;
import static com.aaptrix.savitri.activities.SplashScreen.DATA_URL;
import static java.security.AccessController.getContext;

public class MemberDashboard extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    DrawerLayout drawer;
    CircleImageView profImage;
    ListView listView;
    MaterialButton feedback;
    TextView planName;
    String planId, strOrgId, strUserId, strSessionId, strUserRole, strUserName;
    SharedPreferences sp;
    ProgressBar progressBar;
    SwipeRefreshLayout swipeRefreshLayout;
    TextView noTasks;
    TasksAdapter adapter;
    ArrayList<ComplianceData> tasksArray = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_member_dashboard);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        progressBar = findViewById(R.id.progress_bar);
        noTasks = findViewById(R.id.no_tasks);
        listView = findViewById(R.id.listview);
        swipeRefreshLayout = findViewById(R.id.swipe_refresh);
        feedback = findViewById(R.id.feedback_btn);

        drawer = findViewById(R.id.drawer_layout);
        NavigationView navigationView = findViewById(R.id.nav_view);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();
        navigationView.setNavigationItemSelectedListener(this);

        View headerView = navigationView.getHeaderView(0);
        profImage = headerView.findViewById(R.id.logged_user_image);
        TextView userName = headerView.findViewById(R.id.logged_user_name);
        TextView userRole = headerView.findViewById(R.id.logged_user_role);
        planName = headerView.findViewById(R.id.plan_name);
        MaterialButton upgrade = headerView.findViewById(R.id.upgrade_plan_btn);
        upgrade.setVisibility(View.GONE);

        sp = getSharedPreferences(USER_PREFS, Context.MODE_PRIVATE);
        planId = sp.getString(KEY_ORG_PLAN_TYPE, "");
        strOrgId = sp.getString(KEY_ORG_ID, "");
        strSessionId = sp.getString(KEY_SESSION_ID, "");
        strUserId = sp.getString(KEY_USER_ID, "");
        strUserRole = sp.getString(KEY_USER_ROLE, "");
        strUserName = sp.getString(KEY_USER_NAME, "");
        progressBar.setVisibility(View.VISIBLE);
        String strPlanName = sp.getString(KEY_PLAN_NAME, "");

        String url = DATA_URL + sp.getString(KEY_ORG_ID, "") + "/profile/" + sp.getString(KEY_USER_IMAGE, "");
        Picasso.with(this).load(url).placeholder(R.drawable.user_placeholder).into(profImage);
        userName.setText(sp.getString(KEY_USER_NAME, ""));
        userRole.setText(sp.getString(KEY_USER_ROLE, ""));

        if (strPlanName != null && !strPlanName.isEmpty()) {
            planName.setText(strPlanName);
        } else {
            fetchPlans();
        }

        feedback.setOnClickListener(v -> startActivity(new Intent(this, FeedbackActivity.class)));

        RelativeLayout profLayout = headerView.findViewById(R.id.profile_layout);
        profLayout.setOnClickListener(v -> startActivity(new Intent(this, UserProfile.class)
                .putExtra("userId", sp.getString(KEY_USER_ID, ""))));

        setTasks();
        swipeRefreshLayout.setOnRefreshListener(() -> {
            swipeRefreshLayout.setRefreshing(true);
            listView.setEnabled(false);
            tasksArray.clear();
            setTasks();
        });
    }

    private void setTasks() {
        if (checkConnection()) {
            noTasks.setVisibility(View.GONE);
            fetchTasks();
        } else {
            Toast.makeText(this, "No Internet Connection", Toast.LENGTH_SHORT).show();
            try {
                FileNotFoundException fe = new FileNotFoundException();
                File directory = this.getFilesDir();
                ObjectInputStream in = new ObjectInputStream(new FileInputStream(new File(directory, "tasksData")));
                String json = in.readObject().toString();
                in.close();
                JSONObject jsonObject = new JSONObject(json);
                JSONArray jsonArray = jsonObject.getJSONArray("allCompliance");
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
                    data.setAssignedTo(jObject.getString("assign_users_name"));
                    data.setStatus(jObject.getString("compliance_assign_status"));
                    data.setMarkReview(jObject.getString("markas_review"));
                    tasksArray.add(data);
                }
                throw fe;
            } catch (Exception e) {
                e.printStackTrace();
            }
            if (tasksArray.size() == 0) {
                noTasks.setVisibility(View.VISIBLE);
                progressBar.setVisibility(View.GONE);
            } else {
                listItem();
            }
        }
    }

    private void fetchTasks() {
        new Thread(() -> {
            try {
                HttpClient httpclient = new DefaultHttpClient();
                HttpPost httppost = new HttpPost(URLs.ALL_TASKS);
                MultipartEntityBuilder entityBuilder = MultipartEntityBuilder.create();
                entityBuilder.setMode(HttpMultipartMode.BROWSER_COMPATIBLE);
                entityBuilder.addTextBody("org_details_id", strOrgId);
                entityBuilder.addTextBody("app_session_id", strSessionId);
                entityBuilder.addTextBody("users_type", strUserRole);
                entityBuilder.addTextBody("users_details_id", strUserId);
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
                        } else if (result.contains("{\"allCompliance\":null}") || result.isEmpty()) {
                            noTasks.setVisibility(View.VISIBLE);
                            progressBar.setVisibility(View.GONE);
                        } else {
                            JSONObject jsonObject = new JSONObject(result);
                            cacheJson(jsonObject);
                            JSONArray jsonArray = jsonObject.getJSONArray("allCompliance");
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
                                data.setAssignedTo(jObject.getString("assign_users_name"));
                                data.setStatus(jObject.getString("compliance_assign_status"));
                                data.setMarkReview(jObject.getString("markas_review"));
                                tasksArray.add(data);
                            }
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    listItem();
                });
            } catch (IOException e) {
                e.printStackTrace();
            }
        }).start();
    }

    private void listItem() {
        Collections.sort(tasksArray, (o1, o2) -> {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
            try {
                return sdf.parse(o1.getValidTo()).compareTo(sdf.parse(o2.getValidTo()));
            } catch (ParseException e) {
                e.printStackTrace();
            }
            return 0;
        });
        progressBar.setVisibility(View.GONE);
        swipeRefreshLayout.setRefreshing(false);
        adapter = new TasksAdapter(this, R.layout.list_renewal_status, tasksArray);
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
                    out = new ObjectOutputStream(new FileOutputStream(new File(directory, "tasksData")));
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
    public void onResume() {
        super.onResume();
        SharedPreferences sp = getSharedPreferences(USER_PREFS, Context.MODE_PRIVATE);
        String url = DATA_URL + sp.getString(KEY_ORG_ID, "") + "/profile/" + sp.getString(KEY_USER_IMAGE, "");
        Picasso.with(this).load(url).placeholder(R.drawable.user_placeholder).into(profImage);
    }

    @Override
    public void onBackPressed() {
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            new AlertDialog.Builder(this)
                    .setMessage("Are you sure you want to exit?")
                    .setPositiveButton("Yes", (dialog, which) -> {
                        Intent a = new Intent(Intent.ACTION_MAIN);
                        a.addCategory(Intent.CATEGORY_HOME);
                        a.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        startActivity(a);
                        finish();
                        System.exit(0);
                    })
                    .setNegativeButton("No", null)
                    .show();
        }
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.faqs) {
            startActivity(new Intent(this, FaqActivity.class));
        } else if (id == R.id.feedback) {
            startActivity(new Intent(this, FeedbackActivity.class));
        } else if (id == R.id.rate_us) {
            Intent i = new Intent(Intent.ACTION_VIEW);
            i.setData(Uri.parse("market://details?id=com.aaptrix.savitri"));
            startActivity(i);
        } else if (id == R.id.share_us) {
            String msg = "Install Savitri: Your compliance tracking assistant, and never miss any compliance renewals." + " " + "\nhttp://play.google.com/store/apps/details?id=com.aaptrix.savitri";
            ShareCompat.IntentBuilder.from(this)
                    .setType("text/plain")
                    .setChooserTitle("Share via...")
                    .setText(msg)
                    .startChooser();
        } else if (id == R.id.app_info) {
            startActivity(new Intent(this, AppInfo.class));
        } else if (id == R.id.logout) {
            new AlertDialog.Builder(this)
                    .setMessage("Are you sure you want to logout?")
                    .setPositiveButton("Yes", (dialog, which) -> {
                        SharedPrefsManager.getInstance(this).logout();
                        Intent intent = new Intent(this, AppLogin.class);
                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        startActivity(intent);
                        finish();
                    })
                    .setNegativeButton("No", null)
                    .show();
        } else if (id == R.id.pay_history) {
            startActivity(new Intent(this, PaymentHistory.class));
        } else if (id == R.id.about_savitri) {
            startActivity(new Intent(this, AboutSavitri.class));
        }
        drawer.closeDrawer(GravityCompat.START);
        return false;
    }

    private void fetchPlans() {
        new Thread(() -> {
            try {
                HttpClient httpclient = new DefaultHttpClient();
                HttpPost httppost = new HttpPost(URLs.ALL_PLANS);
                MultipartEntityBuilder entityBuilder = MultipartEntityBuilder.create();
                entityBuilder.setMode(HttpMultipartMode.BROWSER_COMPATIBLE);
                HttpEntity entity = entityBuilder.build();
                httppost.setEntity(entity);
                HttpResponse response = httpclient.execute(httppost);
                HttpEntity httpEntity = response.getEntity();
                String result = EntityUtils.toString(httpEntity);
                Handler handler = new Handler(Looper.getMainLooper());
                handler.post(() -> {
                    try {
                        if (result.equals("null")) {
                            planName.setVisibility(View.GONE);
                        } else {
                            JSONObject jsonObject = new JSONObject(result);
                            JSONArray jsonArray = jsonObject.getJSONArray("allPlans");
                            for (int i = 0; i < jsonArray.length(); i++) {
                                JSONObject jObject = jsonArray.getJSONObject(i);
                                if (jObject.getString("plan_id").equals(planId)) {
                                    planName.setText(jObject.getString("plan_name"));
                                    sp.edit().putString(KEY_PLAN_NAME, jObject.getString("plan_name")).apply();
                                    break;
                                }
                            }
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
}
