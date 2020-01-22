package com.aaptrix.savitri.fragment;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.aaptrix.savitri.R;
import com.aaptrix.savitri.activities.AppLogin;
import com.aaptrix.savitri.adapter.RenewalStatusAdapter;
import com.aaptrix.savitri.databeans.ComplianceData;
import com.aaptrix.savitri.session.SharedPrefsManager;
import com.aaptrix.savitri.session.URLs;
import com.google.android.material.snackbar.Snackbar;

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

import static android.content.Context.CONNECTIVITY_SERVICE;
import static com.aaptrix.savitri.session.SharedPrefsNames.KEY_ORG_ID;
import static com.aaptrix.savitri.session.SharedPrefsNames.KEY_SESSION_ID;
import static com.aaptrix.savitri.session.SharedPrefsNames.KEY_USER_ID;
import static com.aaptrix.savitri.session.SharedPrefsNames.KEY_USER_ROLE;
import static com.aaptrix.savitri.session.SharedPrefsNames.USER_PREFS;

public class NewRenewalFragment extends Fragment {

    private ArrayList<ComplianceData> renewalArray = new ArrayList<>();
    private ListView listView;
    private TextView noStatus;
    private ProgressBar progressBar;
    private SwipeRefreshLayout swipeRefreshLayout;
    private String strOrgId, strSessionId, strUserId, strUserType;
    private Context context;
    private FrameLayout layout;

    public NewRenewalFragment() {

    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        this.context = context;
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_renewal, container, false);
        noStatus = view.findViewById(R.id.no_renewal);
        progressBar = view.findViewById(R.id.progress_bar);
        listView = view.findViewById(R.id.renewal_listview);
        swipeRefreshLayout = view.findViewById(R.id.swipe_refresh);
        layout= view.findViewById(R.id.layout);

        SharedPreferences sp = context.getSharedPreferences(USER_PREFS, Context.MODE_PRIVATE);
        strOrgId = sp.getString(KEY_ORG_ID, "");
        strSessionId = sp.getString(KEY_SESSION_ID, "");
        strUserId = sp.getString(KEY_USER_ID, "");
        strUserType = sp.getString(KEY_USER_ROLE, "");
        progressBar.setVisibility(View.VISIBLE);
        setRenewalStatus();

        swipeRefreshLayout.setOnRefreshListener(() -> {
            swipeRefreshLayout.setRefreshing(true);
            listView.setEnabled(false);
            renewalArray.clear();
            setRenewalStatus();
        });
        return view;
    }

    private void setRenewalStatus() {
        if (checkConnection()) {
            noStatus.setVisibility(View.GONE);
            fetchRenewalStatus();
        } else {
            Snackbar snackbar = Snackbar.make(layout, "No Internet Connection", Snackbar.LENGTH_INDEFINITE)
                    .setActionTextColor(Color.WHITE)
                    .setAction("Ok", null);
            snackbar.show();
            try {
                File directory = context.getFilesDir();
                ObjectInputStream in = new ObjectInputStream(new FileInputStream(new File(directory, "newRenewalData")));
                String json = in.readObject().toString();
                in.close();
                JSONObject jsonObject = new JSONObject(json);
                JSONArray jsonArray = jsonObject.getJSONArray("allRenews");
                for (int i = 0; i < jsonArray.length(); i++) {
                    JSONObject jObject = jsonArray.getJSONObject(i);
                    ComplianceData data = new ComplianceData();
                    data.setId(jObject.getString("compliance_id"));
                    data.setName(jObject.getString("compliance_name"));
                    data.setIssueAuth(jObject.getString("compliance_issuing_auth"));
                    data.setAddedDate(jObject.getString("color"));
                    data.setCertificate(jObject.getString("compliance_certificates"));
                    data.setNotes(jObject.getString("compliance_notes"));
                    data.setOtherAuth(jObject.getString("compliance_issuing_auth_other"));
                    data.setRefNo(jObject.getString("compliance_reference_no"));
                    data.setValidfrom(jObject.getString("compliance_valid_from"));
                    data.setValidTo(jObject.getString("compliance_valid_upto"));
                    data.setAssignedTo(jObject.getString("assign_users_name"));
                    renewalArray.add(data);
                }
            } catch (Exception e) {
                e.printStackTrace();
                noStatus.setVisibility(View.VISIBLE);
                progressBar.setVisibility(View.GONE);
            }
            if (renewalArray.size() == 0) {
                noStatus.setVisibility(View.VISIBLE);
                progressBar.setVisibility(View.GONE);
            } else {
                listItem();
            }
        }
    }

    private void fetchRenewalStatus() {
        new Thread(() -> {
            try {
                HttpClient httpclient = new DefaultHttpClient();
                HttpPost httppost = new HttpPost(URLs.ALL_RENEWAL_STATUS);
                MultipartEntityBuilder entityBuilder = MultipartEntityBuilder.create();
                entityBuilder.setMode(HttpMultipartMode.BROWSER_COMPATIBLE);
                entityBuilder.addTextBody("org_details_id", strOrgId);
                entityBuilder.addTextBody("app_session_id", strSessionId);
                entityBuilder.addTextBody("users_details_id", strUserId);
                entityBuilder.addTextBody("users_type", strUserType);
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
                            Toast.makeText(context, "Your Session is expired please login again", Toast.LENGTH_SHORT).show();
                            SharedPrefsManager.getInstance(context).logout();
                            Intent intent = new Intent(context, AppLogin.class);
                            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                            startActivity(intent);
                        } else if (result.equals("{\"allRenews\":null}")) {
                            noStatus.setVisibility(View.VISIBLE);
                            progressBar.setVisibility(View.GONE);
                        } else {
                            JSONObject jsonObject = new JSONObject(result);
                            cacheJson(jsonObject);
                            JSONArray jsonArray = jsonObject.getJSONArray("allRenews");
                            for (int i = 0; i < jsonArray.length(); i++) {
                                JSONObject jObject = jsonArray.getJSONObject(i);
                                ComplianceData data = new ComplianceData();
                                data.setId(jObject.getString("compliance_id"));
                                data.setName(jObject.getString("compliance_name"));
                                data.setIssueAuth(jObject.getString("compliance_issuing_auth"));
                                data.setAddedDate(jObject.getString("color"));
                                data.setCertificate(jObject.getString("compliance_certificates"));
                                data.setNotes(jObject.getString("compliance_notes"));
                                data.setOtherAuth(jObject.getString("compliance_issuing_auth_other"));
                                data.setRefNo(jObject.getString("compliance_reference_no"));
                                data.setValidfrom(jObject.getString("compliance_valid_from"));
                                data.setValidTo(jObject.getString("compliance_valid_upto"));
                                data.setAssignedTo(jObject.getString("assign_users_name"));
                                renewalArray.add(data);
                            }
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                        noStatus.setVisibility(View.VISIBLE);
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
        Collections.sort(renewalArray, (o1, o2) -> {
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
        RenewalStatusAdapter adapter = new RenewalStatusAdapter(context, R.layout.list_renewal_status, renewalArray, "new");
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
                    File directory = context.getFilesDir();
                    directory.mkdir();
                    out = new ObjectOutputStream(new FileOutputStream(new File(directory, "newRenewalData")));
                    out.writeObject(data);
                    out.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }).start();
    }

    private boolean checkConnection() {
        ConnectivityManager connec;
        connec = (ConnectivityManager) context.getSystemService(CONNECTIVITY_SERVICE);
        assert connec != null;
        return connec.getActiveNetworkInfo() != null && connec.getActiveNetworkInfo().isAvailable() && connec.getActiveNetworkInfo().isConnectedOrConnecting();
    }
}
