package com.aaptrix.savitri.fragment;

import android.content.Context;
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

import com.aaptrix.savitri.R;
import com.aaptrix.savitri.adapter.PlansAdapter;
import com.aaptrix.savitri.databeans.PlansData;
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
import java.util.ArrayList;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import cz.msebera.android.httpclient.HttpEntity;
import cz.msebera.android.httpclient.HttpResponse;
import cz.msebera.android.httpclient.client.HttpClient;
import cz.msebera.android.httpclient.client.methods.HttpPost;
import cz.msebera.android.httpclient.entity.mime.HttpMultipartMode;
import cz.msebera.android.httpclient.entity.mime.MultipartEntityBuilder;
import cz.msebera.android.httpclient.impl.client.DefaultHttpClient;
import cz.msebera.android.httpclient.util.EntityUtils;

import static android.content.Context.CONNECTIVITY_SERVICE;
import static java.security.AccessController.getContext;

public class PlanFragment extends Fragment {

    private Context context;
    private ProgressBar progressBar;
    private ListView listView;
    private TextView noPlan;
    private ArrayList<PlansData> plansArray = new ArrayList<>();

    public PlanFragment() {

    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        this.context = context;
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_plan, container, false);
        progressBar = view.findViewById(R.id.progress_bar);
        listView = view.findViewById(R.id.plans_listview);
        noPlan = view.findViewById(R.id.no_plan);
        FrameLayout layout = view.findViewById(R.id.layout);
        progressBar.setVisibility(View.VISIBLE);
        if (checkConnection()) {
            fetchPlans();
        } else {
            Snackbar snackbar = Snackbar.make(layout, "No Internet Connection", Snackbar.LENGTH_INDEFINITE)
                    .setActionTextColor(Color.WHITE)
                    .setAction("Ok", null);
            snackbar.show();
            try {
                File directory = context.getFilesDir();
                ObjectInputStream in = new ObjectInputStream(new FileInputStream(new File(directory, "plans")));
                String json = in.readObject().toString();
                in.close();
                JSONObject jsonObject = new JSONObject(json);
                JSONArray jsonArray = jsonObject.getJSONArray("allPlans");
                for (int i = 0; i < jsonArray.length(); i++) {
                    JSONObject jObject = jsonArray.getJSONObject(i);
                    if (!jObject.getString("plan_name").contains("Free")) {
                        PlansData data = new PlansData();
                        data.setId(jObject.getString("plan_id"));
                        data.setName(jObject.getString("plan_name"));
                        data.setComplianceLimit(jObject.getString("plan_compliance_limit"));
                        data.setDataDownload(jObject.getString("plan_data_download"));
                        data.setStorageCycle(jObject.getString("plan_data_storage_cycle"));
                        data.setAlertByApp(jObject.getString("plan_alert_by_app"));
                        data.setAlertByEmail(jObject.getString("plan_alert_by_email"));
                        data.setAlertBySms(jObject.getString("plan_alert_by_sms"));
                        data.setPlanCost(jObject.getString("plan_cost"));
                        data.setUserLimit(jObject.getString("plan_user_assign_limit"));
                        plansArray.add(data);
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            if (plansArray.size() == 0) {
                noPlan.setVisibility(View.VISIBLE);
                progressBar.setVisibility(View.GONE);
            } else {
                listItem();
            }
        }
        return view;
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
                            progressBar.setVisibility(View.GONE);
                        } else {
                            JSONObject jsonObject = new JSONObject(result);
                            cacheJson(jsonObject);
                            JSONArray jsonArray = jsonObject.getJSONArray("allPlans");
                            for (int i = 0; i < jsonArray.length(); i++) {
                                JSONObject jObject = jsonArray.getJSONObject(i);
                                if (!jObject.getString("plan_name").contains("Free")) {
                                    PlansData data = new PlansData();
                                    data.setId(jObject.getString("plan_id"));
                                    data.setName(jObject.getString("plan_name"));
                                    data.setComplianceLimit(jObject.getString("plan_compliance_limit"));
                                    data.setDataDownload(jObject.getString("plan_data_download"));
                                    data.setStorageCycle(jObject.getString("plan_data_storage_cycle"));
                                    data.setAlertByApp(jObject.getString("plan_alert_by_app"));
                                    data.setAlertByEmail(jObject.getString("plan_alert_by_email"));
                                    data.setAlertBySms(jObject.getString("plan_alert_by_sms"));
                                    data.setPlanCost(jObject.getString("plan_cost"));
                                    data.setUserLimit(jObject.getString("plan_user_assign_limit"));
                                    plansArray.add(data);
                                }
                            }
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    if (plansArray.size() == 0) {
                        noPlan.setVisibility(View.VISIBLE);
                        progressBar.setVisibility(View.GONE);
                    } else {
                        listItem();
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
                if (context != null) {
                    File directory = context.getFilesDir();
                    directory.mkdir();
                    out = new ObjectOutputStream(new FileOutputStream(new File(directory, "plans")));
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
        connec = (ConnectivityManager) context.getSystemService(CONNECTIVITY_SERVICE);
        assert connec != null;
        return connec.getActiveNetworkInfo() != null && connec.getActiveNetworkInfo().isAvailable() && connec.getActiveNetworkInfo().isConnectedOrConnecting();
    }

    private void listItem() {
        progressBar.setVisibility(View.GONE);
        PlansAdapter adapter = new PlansAdapter(context, R.layout.list_plans, plansArray);
        listView.setAdapter(adapter);
        adapter.notifyDataSetChanged();
    }
}
