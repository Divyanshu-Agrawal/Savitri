package com.aaptrix.savitri.activities;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.cardview.widget.CardView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.aaptrix.savitri.R;
import com.aaptrix.savitri.databeans.PlansData;
import com.aaptrix.savitri.session.URLs;
import com.paytm.pgsdk.PaytmMerchant;
import com.paytm.pgsdk.PaytmOrder;
import com.paytm.pgsdk.PaytmPGService;
import com.paytm.pgsdk.PaytmPaymentTransactionCallback;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Random;

import cz.msebera.android.httpclient.HttpEntity;
import cz.msebera.android.httpclient.HttpResponse;
import cz.msebera.android.httpclient.client.HttpClient;
import cz.msebera.android.httpclient.client.methods.HttpPost;
import cz.msebera.android.httpclient.entity.mime.HttpMultipartMode;
import cz.msebera.android.httpclient.entity.mime.MultipartEntityBuilder;
import cz.msebera.android.httpclient.impl.client.DefaultHttpClient;
import cz.msebera.android.httpclient.util.EntityUtils;

import static com.aaptrix.savitri.session.SharedPrefsNames.KEY_ORG_ID;
import static com.aaptrix.savitri.session.SharedPrefsNames.KEY_ORG_NAME;
import static com.aaptrix.savitri.session.SharedPrefsNames.KEY_RESPONSE;
import static com.aaptrix.savitri.session.SharedPrefsNames.KEY_SESSION_ID;
import static com.aaptrix.savitri.session.SharedPrefsNames.KEY_USER_ID;
import static com.aaptrix.savitri.session.SharedPrefsNames.PAYMENT_PREFS;
import static com.aaptrix.savitri.session.SharedPrefsNames.USER_PREFS;

public class BuyPlan extends AppCompatActivity {

    PlansData plansData;
    Toolbar toolbar;
    RelativeLayout progressLayout;
    TextView planName, planCost;
    CardView offlineBtn, onlineBtn;
    String userPhone, userEmail, userId, orgId, orgName, sessionId, orderId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_buy_plan);
        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        progressLayout = findViewById(R.id.progress_layout);
        offlineBtn = findViewById(R.id.offline_btn);
        onlineBtn = findViewById(R.id.online_btn);
        planName = findViewById(R.id.plan_name);
        planCost = findViewById(R.id.plan_cost);

        plansData = (PlansData) getIntent().getSerializableExtra("data");
        SharedPreferences sp = getSharedPreferences(USER_PREFS, Context.MODE_PRIVATE);
        userId = sp.getString(KEY_USER_ID, "");
        orgId = sp.getString(KEY_ORG_ID, "");
        sessionId = sp.getString(KEY_SESSION_ID, "");
        orgName = sp.getString(KEY_ORG_NAME, "");

        planCost.setText(plansData.getPlanCost());
        planName.setText(plansData.getName());

        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmss", Locale.getDefault());
        String date = sdf.format(Calendar.getInstance().getTimeInMillis());


        if (orgName.length() > 4) {
            orderId = orgName.substring(0, 3).toUpperCase() + orgId + plansData.getId() + date;
        } else {
            orderId = orgName.toUpperCase() + orgId + plansData.getId() + date;
        }

        offlineBtn.setOnClickListener(v -> startActivity(new Intent(this, OfflinePurchase.class).putExtra("plandata", plansData)));

        onlineBtn.setOnClickListener(v -> {
            progressLayout.setVisibility(View.VISIBLE);
            userDetail(userId, orgId, sessionId);
        });

    }

    private void userDetail(String userId, String orgId, String sessionID) {
        new Thread(() -> {
            try {
                HttpClient httpclient = new DefaultHttpClient();
                HttpPost httppost = new HttpPost(URLs.ALL_PEOPLE);
                MultipartEntityBuilder entityBuilder = MultipartEntityBuilder.create();
                entityBuilder.setMode(HttpMultipartMode.BROWSER_COMPATIBLE);
                entityBuilder.addTextBody("org_details_id", orgId);
                entityBuilder.addTextBody("app_session_id", sessionID);
                entityBuilder.addTextBody("users_details_id", userId);
                entityBuilder.addTextBody("profile", "profile");
                HttpEntity entity = entityBuilder.build();
                httppost.setEntity(entity);
                HttpResponse response = httpclient.execute(httppost);
                HttpEntity httpEntity = response.getEntity();
                String result = EntityUtils.toString(httpEntity);
                Handler handler = new Handler(Looper.getMainLooper());
                handler.post(() -> {
                    try {
                        JSONObject jsonObject = new JSONObject(result);
                        JSONArray jsonArray = jsonObject.getJSONArray("profileDetails");
                        JSONObject jObject = jsonArray.getJSONObject(0);
                        userPhone = jObject.getString("users_mobileno");
                        userEmail = jObject.getString("users_email");
                        generateChecksum();
                    } catch (JSONException e) {
                        e.printStackTrace();
                        userPhone = "1234567890";
                        userEmail = "testemail@test.com";
                    }
                });
            } catch (IOException e) {
                e.printStackTrace();
            }
        }).start();
    }

    private void generateChecksum() {
        new Thread(() -> {
            try {
                HttpClient httpclient = new DefaultHttpClient();
                HttpPost httppost = new HttpPost(URLs.GENERATE_CHECKSUM);
                MultipartEntityBuilder entityBuilder = MultipartEntityBuilder.create();
                entityBuilder.setMode(HttpMultipartMode.BROWSER_COMPATIBLE);
                entityBuilder.addTextBody("merchantMid", "MID Here");
                entityBuilder.addTextBody("merchantKey", "Merchant Key Here");
                entityBuilder.addTextBody("orderId", orderId);
                entityBuilder.addTextBody("channelId", "WAP");
                entityBuilder.addTextBody("custId", orgId);
                entityBuilder.addTextBody("mobileNo", userPhone);
                entityBuilder.addTextBody("email", userEmail);
                entityBuilder.addTextBody("txnAmount", plansData.getPlanCost());
                entityBuilder.addTextBody("website", "APPSTAGING");
                entityBuilder.addTextBody("industryTypeId", "Retail");
                entityBuilder.addTextBody("callbackUrl", "https://securegw-stage.paytm.in/theia/paytmCallback");
                HttpEntity entity = entityBuilder.build();
                httppost.setEntity(entity);
                HttpResponse response = httpclient.execute(httppost);
                HttpEntity httpEntity = response.getEntity();
                String result = EntityUtils.toString(httpEntity);
                Handler handler = new Handler(Looper.getMainLooper());
                handler.post(() -> {
                    try {
                        Log.e("res", result);
                        JSONObject jsonObject = new JSONObject(result);
                        requestPaytm(jsonObject.getString("CHECKSUMHASH"));
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                });
            } catch (IOException e) {
                e.printStackTrace();
            }
        }).start();
    }

    private void requestPaytm(String checksum) {
        PaytmPGService Service = PaytmPGService.getStagingService();
        HashMap<String, String> paramMap = new HashMap<>();
        paramMap.put("MID", "MID Here");
        paramMap.put("ORDER_ID", orderId);
        paramMap.put("CUST_ID", orgId);
        paramMap.put("MOBILE_NO", userPhone);
        paramMap.put("EMAIL", userEmail);
        paramMap.put("CHANNEL_ID", "WAP");
        paramMap.put("TXN_AMOUNT", plansData.getPlanCost());
        paramMap.put("WEBSITE", "APPSTAGING");
        paramMap.put("INDUSTRY_TYPE_ID", "Retail");
        paramMap.put("CALLBACK_URL", "https://securegw-stage.paytm.in/theia/paytmCallback");
        paramMap.put("CHECKSUMHASH", checksum);
        PaytmOrder Order = new PaytmOrder(paramMap);
        Service.initialize(Order, null);
        Service.startPaymentTransaction(this, true, true, new PaytmPaymentTransactionCallback() {

            public void someUIErrorOccurred(String inErrorMessage) {
                if (!BuyPlan.this.isFinishing()) {
                    new AlertDialog.Builder(BuyPlan.this)
                            .setMessage(inErrorMessage)
                            .setPositiveButton("Ok", (dialog, which) -> dialog.dismiss())
                            .setCancelable(false)
                            .show();
                    progressLayout.setVisibility(View.GONE);
                }
            }

            public void onTransactionResponse(Bundle inResponse) {
                progressLayout.setVisibility(View.GONE);
                SharedPreferences sp = BuyPlan.this.getSharedPreferences(PAYMENT_PREFS, Context.MODE_PRIVATE);
                sp.edit().putString(KEY_RESPONSE, inResponse.toString()).apply();
                if (checksum.equals(inResponse.getString("CHECKSUMHASH"))) {
                    Intent intent = new Intent(BuyPlan.this, PaymentActivity.class);
                    intent.putExtra("response", inResponse);
                    intent.putExtra("type", "payment");
                    intent.putExtra("mobile", userPhone);
                    intent.putExtra("email", userEmail);
                    intent.putExtra("plansdata", plansData);
                    startActivity(intent);
                } else {
                    new AlertDialog.Builder(BuyPlan.this)
                            .setMessage("Error!")
                            .setPositiveButton("Ok", (dialog, which) -> dialog.dismiss())
                            .setCancelable(false)
                            .show();
                }
            }

            public void networkNotAvailable() {
                if (!BuyPlan.this.isFinishing()) {
                    new AlertDialog.Builder(BuyPlan.this)
                            .setMessage("Network Not Available")
                            .setPositiveButton("Ok", (dialog, which) -> dialog.dismiss())
                            .setCancelable(false)
                            .show();
                    progressLayout.setVisibility(View.GONE);
                }
            }

            public void clientAuthenticationFailed(String inErrorMessage) {
                if (!BuyPlan.this.isFinishing()) {
                    new AlertDialog.Builder(BuyPlan.this)
                            .setMessage(inErrorMessage)
                            .setPositiveButton("Ok", (dialog, which) -> dialog.dismiss())
                            .setCancelable(false)
                            .show();
                    progressLayout.setVisibility(View.GONE);
                }
            }

            public void onErrorLoadingWebPage(int iniErrorCode, String inErrorMessage, String inFailingUrl) {
                if (!BuyPlan.this.isFinishing()) {
                    new AlertDialog.Builder(BuyPlan.this)
                            .setMessage(inErrorMessage)
                            .setPositiveButton("Ok", (dialog, which) -> dialog.dismiss())
                            .setCancelable(false)
                            .show();
                    progressLayout.setVisibility(View.GONE);
                }
            }

            public void onBackPressedCancelTransaction() {
                if (!BuyPlan.this.isFinishing()) {
                    new AlertDialog.Builder(BuyPlan.this)
                            .setMessage("Back button pressed. Transaction Cancelled")
                            .setPositiveButton("Ok", (dialog, which) -> dialog.dismiss())
                            .setCancelable(false)
                            .show();
                    progressLayout.setVisibility(View.GONE);
                }
            }

            public void onTransactionCancel(String inErrorMessage, Bundle inResponse) {
                if (!BuyPlan.this.isFinishing()) {
                    new AlertDialog.Builder(BuyPlan.this)
                            .setMessage(inErrorMessage)
                            .setPositiveButton("Ok", (dialog, which) -> dialog.dismiss())
                            .setCancelable(false)
                            .show();
                    progressLayout.setVisibility(View.GONE);
                }
            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        progressLayout.setVisibility(View.GONE);
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
