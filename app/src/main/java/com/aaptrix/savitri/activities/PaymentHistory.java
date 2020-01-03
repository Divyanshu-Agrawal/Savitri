package com.aaptrix.savitri.activities;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.aaptrix.savitri.R;
import com.aaptrix.savitri.adapter.PaymentHistoryAdapter;
import com.aaptrix.savitri.databeans.PaymentData;
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
import java.util.ArrayList;

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
import static com.aaptrix.savitri.session.SharedPrefsNames.USER_PREFS;
import static java.security.AccessController.getContext;

public class PaymentHistory extends AppCompatActivity {

    Toolbar toolbar;
    ProgressBar progressBar;
    ListView listView;
    TextView noPayment;
    ArrayList<PaymentData> paymentArray = new ArrayList<>();
    String orgId, sessionId;
    RelativeLayout layout;
    SwipeRefreshLayout swipeRefreshLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_payment_history);
        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        listView = findViewById(R.id.listview);
        progressBar = findViewById(R.id.progress_bar);
        noPayment = findViewById(R.id.no_payment);
        layout = findViewById(R.id.layout);
        swipeRefreshLayout = findViewById(R.id.swipe_refresh);

        SharedPreferences sp = getSharedPreferences(USER_PREFS, Context.MODE_PRIVATE);
        orgId = sp.getString(KEY_ORG_ID, "");
        sessionId = sp.getString(KEY_SESSION_ID, "");
        setPaymentHistory();

        swipeRefreshLayout.setOnRefreshListener(() -> {
            paymentArray.clear();
            setPaymentHistory();
            swipeRefreshLayout.setRefreshing(true);
            listView.setEnabled(false);
        });
    }

    private void setPaymentHistory() {
        progressBar.setVisibility(View.VISIBLE);
        if (checkConnection()) {
            noPayment.setVisibility(View.GONE);
            fetchPaymentHistory();
        } else {
            Snackbar snackbar = Snackbar.make(layout, "No Internet Connection", Snackbar.LENGTH_INDEFINITE)
                    .setActionTextColor(Color.WHITE)
                    .setAction("Ok", null);
            snackbar.show();
            try {
                File directory = this.getFilesDir();
                ObjectInputStream in = new ObjectInputStream(new FileInputStream(new File(directory, "paymentData")));
                String json = in.readObject().toString();
                in.close();
                JSONObject jsonObject = new JSONObject(json);
                JSONArray onlineArray = jsonObject.getJSONArray("OnlinePayment");
                for (int i = 0; i < onlineArray.length(); i++) {
                    JSONObject jObject = onlineArray.getJSONObject(i);
                    PaymentData data = new PaymentData();
                    data.setType("Online");
                    data.setStatus(jObject.getString("payment_status"));
                    data.setAmount(jObject.getString("txnAmount"));
                    data.setDate(jObject.getString("entrydt"));
                    data.setEmail(jObject.getString("email"));
                    data.setMethod(jObject.getString("payment_method"));
                    data.setMobno(jObject.getString("mobileNo"));
                    data.setOrderId(jObject.getString("orderId"));
                    data.setResmsg(jObject.getString("response_msg"));
                    data.setTxnId(jObject.getString("txn_id"));
                    data.setPlanName(jObject.getString("plan_name"));
                    paymentArray.add(data);
                }
                JSONArray offlineArray = jsonObject.getJSONArray("OfflinePayment");
                for (int i = 0; i < offlineArray.length(); i++) {
                    JSONObject jObject = offlineArray.getJSONObject(i);
                    PaymentData data = new PaymentData();
                    data.setType("Offline");
                    data.setDesc(jObject.getString("offline_payment_desc"));
                    data.setTxnId(jObject.getString("offline_payment_transection_id"));
                    data.setImage(jObject.getString("offline_payment_images"));
                    data.setDate(jObject.getString("offline_payment_date"));
                    data.setPlanName(jObject.getString("plan_name"));
                    data.setMethod("Offline");
                    paymentArray.add(data);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            if (paymentArray.size() == 0) {
                noPayment.setVisibility(View.VISIBLE);
                progressBar.setVisibility(View.GONE);
            } else {
                listItem();
            }
        }
    }

    private void fetchPaymentHistory() {
        new Thread(() -> {
            try {
                HttpClient httpclient = new DefaultHttpClient();
                HttpPost httppost = new HttpPost(URLs.PAYMENT_HISTORY);
                MultipartEntityBuilder entityBuilder = MultipartEntityBuilder.create();
                entityBuilder.setMode(HttpMultipartMode.BROWSER_COMPATIBLE);
                entityBuilder.addTextBody("org_details_id", orgId);
                entityBuilder.addTextBody("app_session_id", sessionId);
                HttpEntity entity = entityBuilder.build();
                httppost.setEntity(entity);
                HttpResponse response = httpclient.execute(httppost);
                HttpEntity httpEntity = response.getEntity();
                String result = EntityUtils.toString(httpEntity);
                Handler handler = new Handler(Looper.getMainLooper());
                handler.post(() -> {
                    try {
                        if (result.contains("\"success\":false,\"msg\":\"Session Expire\"")) {
                            progressBar.setVisibility(View.GONE);
                            Toast.makeText(this, "Your Session is expired please login again", Toast.LENGTH_SHORT).show();
                            SharedPrefsManager.getInstance(this).logout();
                            Intent intent = new Intent(this, AppLogin.class);
                            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                            startActivity(intent);
                            finish();
                        } else if (result.contains("{\"OnlinePayment\":null}") && result.contains("{\"OfflinePayment\":null}")) {
                            noPayment.setVisibility(View.VISIBLE);
                            progressBar.setVisibility(View.GONE);
                        } else {
                            JSONObject jsonObject = new JSONObject(result);
                            cacheJson(jsonObject);
                            JSONArray onlineArray = jsonObject.getJSONArray("OnlinePayment");
                            for (int i = 0; i < onlineArray.length(); i++) {
                                JSONObject jObject = onlineArray.getJSONObject(i);
                                PaymentData data = new PaymentData();
                                data.setType("Online");
                                data.setStatus(jObject.getString("payment_status"));
                                data.setAmount(jObject.getString("txnAmount"));
                                data.setDate(jObject.getString("entrydt"));
                                data.setEmail(jObject.getString("email"));
                                data.setMethod(jObject.getString("payment_method"));
                                data.setMobno(jObject.getString("mobileNo"));
                                data.setOrderId(jObject.getString("orderId"));
                                data.setResmsg(jObject.getString("response_msg"));
                                data.setTxnId(jObject.getString("txn_id"));
                                data.setPlanName(jObject.getString("plan_name"));
                                paymentArray.add(data);
                            }
                            JSONArray offlineArray = jsonObject.getJSONArray("OfflinePayment");
                            for (int i = 0; i < offlineArray.length(); i++) {
                                JSONObject jObject = offlineArray.getJSONObject(i);
                                PaymentData data = new PaymentData();
                                data.setType("Offline");
                                data.setDesc(jObject.getString("offline_payment_desc"));
                                data.setTxnId(jObject.getString("offline_payment_transection_id"));
                                data.setImage(jObject.getString("offline_payment_images"));
                                data.setDate(jObject.getString("offline_payment_date"));
                                data.setPlanName(jObject.getString("plan_name"));
                                data.setMethod("Offline");
                                paymentArray.add(data);
                            }
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    if (paymentArray.size() == 0) {
                        noPayment.setVisibility(View.VISIBLE);
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

    private void listItem() {
        progressBar.setVisibility(View.GONE);
        swipeRefreshLayout.setRefreshing(false);
        listView.setEnabled(true);
        PaymentHistoryAdapter adapter = new PaymentHistoryAdapter(this, R.layout.list_payment_history, paymentArray);
        listView.setAdapter(adapter);
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
                    out = new ObjectOutputStream(new FileOutputStream(new File(directory, "paymentData")));
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
        }
        return super.onOptionsItemSelected(item);
    }
}
