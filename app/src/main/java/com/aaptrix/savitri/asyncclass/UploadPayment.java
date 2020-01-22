package com.aaptrix.savitri.asyncclass;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;

import androidx.annotation.Nullable;

import com.aaptrix.savitri.session.URLs;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import cz.msebera.android.httpclient.HttpEntity;
import cz.msebera.android.httpclient.HttpResponse;
import cz.msebera.android.httpclient.client.HttpClient;
import cz.msebera.android.httpclient.client.methods.HttpPost;
import cz.msebera.android.httpclient.entity.mime.HttpMultipartMode;
import cz.msebera.android.httpclient.entity.mime.MultipartEntityBuilder;
import cz.msebera.android.httpclient.impl.client.DefaultHttpClient;
import cz.msebera.android.httpclient.util.EntityUtils;

import static com.aaptrix.savitri.session.SharedPrefsNames.KEY_AMOUNT;
import static com.aaptrix.savitri.session.SharedPrefsNames.KEY_BANKTRANS;
import static com.aaptrix.savitri.session.SharedPrefsNames.KEY_COMPLIANCE_COUNT;
import static com.aaptrix.savitri.session.SharedPrefsNames.KEY_DATA_DOWNLOAD;
import static com.aaptrix.savitri.session.SharedPrefsNames.KEY_DATE;
import static com.aaptrix.savitri.session.SharedPrefsNames.KEY_EMAIL;
import static com.aaptrix.savitri.session.SharedPrefsNames.KEY_MEMBER_COUNT;
import static com.aaptrix.savitri.session.SharedPrefsNames.KEY_METHOD;
import static com.aaptrix.savitri.session.SharedPrefsNames.KEY_MOBILE;
import static com.aaptrix.savitri.session.SharedPrefsNames.KEY_ORDER_ID;
import static com.aaptrix.savitri.session.SharedPrefsNames.KEY_ORG_ID;
import static com.aaptrix.savitri.session.SharedPrefsNames.KEY_ORG_PLAN_TYPE;
import static com.aaptrix.savitri.session.SharedPrefsNames.KEY_PLANNAME;
import static com.aaptrix.savitri.session.SharedPrefsNames.KEY_PLAN_EXP;
import static com.aaptrix.savitri.session.SharedPrefsNames.KEY_PLAN_EXPIRE_DATE;
import static com.aaptrix.savitri.session.SharedPrefsNames.KEY_PLAN_NAME;
import static com.aaptrix.savitri.session.SharedPrefsNames.KEY_PLAN_PURCHASE;
import static com.aaptrix.savitri.session.SharedPrefsNames.KEY_RESMSG;
import static com.aaptrix.savitri.session.SharedPrefsNames.KEY_SERVER_STATUS;
import static com.aaptrix.savitri.session.SharedPrefsNames.KEY_STATUS;
import static com.aaptrix.savitri.session.SharedPrefsNames.KEY_STORAGE_CYCLE;
import static com.aaptrix.savitri.session.SharedPrefsNames.KEY_TRANS_ID;
import static com.aaptrix.savitri.session.SharedPrefsNames.KEY_USER_ID;
import static com.aaptrix.savitri.session.SharedPrefsNames.PAYMENT_PREFS;
import static com.aaptrix.savitri.session.SharedPrefsNames.USER_PREFS;

public class UploadPayment extends AsyncTask<String, String, String> {

    @SuppressLint("StaticFieldLeak")
    private Context context;
    private String orgId, userId, orderId, mobNo, email, amount, status,
            date, method, transId, banktransId, resMsg, planId, planExp, planName;
    private Bundle response;

    public UploadPayment(Context context, @Nullable Bundle response) {
        this.context = context;
        this.response = response;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
    }


    @Override
    protected String doInBackground(String... params) {

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        orgId = params[0];
        userId = params[1];
        mobNo = params[2];
        email = params[3];
        planId = params[4];
        planExp = params[5];
        planName = params[6];
        Date d;
        try {
            d = sdf.parse(params[5]);
            planExp = sdf.format(d);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        if (response != null) {
            amount = response.getString("TXNAMOUNT");
            orderId = response.getString("ORDERID");
            status = response.getString("STATUS");
            resMsg = response.getString("RESPMSG");
            if (status.equals("TXN_SUCCESS")) {
                date = response.getString("TXNDATE").split(" ")[0];
                method = response.getString("BANKNAME");
                transId = response.getString("TXNID");
                banktransId = response.getString("BANKTXNID");
            } else {
                date = sdf.format(Calendar.getInstance().getTimeInMillis());
                method = "null";
                transId = "null";
                banktransId = "null";
            }
        } else {
            SharedPreferences sp = context.getSharedPreferences(PAYMENT_PREFS, Context.MODE_PRIVATE);
            amount = sp.getString(KEY_AMOUNT, "");
            orderId = sp.getString(KEY_ORDER_ID, "");
            status = sp.getString(KEY_STATUS, "");
            resMsg = sp.getString(KEY_RESMSG, "");
            if (status.equals("TXN_SUCCESS")) {
                date = sp.getString(KEY_DATE, "");
                method = sp.getString(KEY_METHOD, "");
                transId = sp.getString(KEY_TRANS_ID, "");
                banktransId = sp.getString(KEY_BANKTRANS, "");
            } else {
                date = sdf.format(Calendar.getInstance().getTimeInMillis());
                method = "null";
                transId = "null";
                banktransId = "null";
            }
        }

        try {
            HttpClient httpclient = new DefaultHttpClient();
            HttpPost httppost = new HttpPost(URLs.UPDATE_PAYMENT);
            MultipartEntityBuilder entityBuilder = MultipartEntityBuilder.create();
            entityBuilder.setMode(HttpMultipartMode.BROWSER_COMPATIBLE);
            entityBuilder.addTextBody("org_details_id", orgId);
            entityBuilder.addTextBody("users_details_id", userId);
            entityBuilder.addTextBody("orderId", orderId);
            entityBuilder.addTextBody("mobileNo", mobNo);
            entityBuilder.addTextBody("email", email);
            entityBuilder.addTextBody("txnAmount", amount);
            entityBuilder.addTextBody("payment_status", status);
            entityBuilder.addTextBody("txnDate", date);
            entityBuilder.addTextBody("payment_method", method);
            entityBuilder.addTextBody("txn_id", transId);
            entityBuilder.addTextBody("bank_txn_id", banktransId);
            entityBuilder.addTextBody("response_msg", resMsg);
            entityBuilder.addTextBody("planId", planId);
            entityBuilder.addTextBody("org_plan_expire_date", planExp);
            entityBuilder.addTextBody("plan_name", planName);

            HttpEntity entity = entityBuilder.build();
            httppost.setEntity(entity);
            HttpResponse response = httpclient.execute(httppost);
            HttpEntity httpEntity = response.getEntity();
            return EntityUtils.toString(httpEntity);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    protected void onPostExecute(String result) {
        if (result != null) {
            try {
                Log.e("res", result);
                JSONObject jsonObject = new JSONObject(result);
                if (status.equals("TXN_SUCCESS")) {
                    if (jsonObject.getBoolean("success")) {
                        SharedPreferences preferences = context.getSharedPreferences(PAYMENT_PREFS, Context.MODE_PRIVATE);
                        preferences.edit().clear().putBoolean(KEY_SERVER_STATUS, false).apply();
                        JSONArray jsonArray = jsonObject.getJSONArray("result");
                        JSONObject jObject = jsonArray.getJSONObject(0);
                        SharedPreferences sp = context.getSharedPreferences(USER_PREFS, Context.MODE_PRIVATE);
                        SharedPreferences.Editor editor = sp.edit();
                        editor.putString(KEY_PLAN_EXPIRE_DATE, jObject.getString("org_plan_expire_date"));
                        editor.putInt(KEY_COMPLIANCE_COUNT, Integer.parseInt(jObject.getString("plan_compliance_limit")));
                        editor.putInt(KEY_MEMBER_COUNT, Integer.parseInt(jObject.getString("plan_user_assign_limit")));
                        editor.putInt(KEY_DATA_DOWNLOAD, Integer.parseInt(jObject.getString("plan_data_download")));
                        editor.putInt(KEY_STORAGE_CYCLE, Integer.parseInt(jObject.getString("plan_data_storage_cycle")));
                        editor.putString(KEY_ORG_PLAN_TYPE, planId);
                        editor.putString(KEY_PLAN_NAME, planName);
                        editor.apply();
                    } else {
                        savePreference();
                    }
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        } else {
            savePreference();
        }
        super.onPostExecute(result);
    }

    private void savePreference() {
        SharedPreferences sp = context.getSharedPreferences(PAYMENT_PREFS, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sp.edit();
        editor.putBoolean(KEY_SERVER_STATUS, true);
        editor.putString(KEY_PLAN_PURCHASE, planId);
        editor.putString(KEY_PLAN_EXP, planExp);
        editor.putString(KEY_ORG_ID, orgId);
        editor.putString(KEY_USER_ID, userId);
        editor.putString(KEY_TRANS_ID, transId);
        editor.putString(KEY_BANKTRANS, banktransId);
        editor.putString(KEY_STATUS, status);
        editor.putString(KEY_ORDER_ID, orderId);
        editor.putString(KEY_METHOD, method);
        editor.putString(KEY_MOBILE, mobNo);
        editor.putString(KEY_EMAIL, email);
        editor.putString(KEY_AMOUNT, amount);
        editor.putString(KEY_DATE, date);
        editor.putString(KEY_RESMSG, resMsg);
        editor.putString(KEY_PLANNAME, planName);
        editor.apply();
    }
}
