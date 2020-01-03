package com.aaptrix.savitri.activities;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.content.Intent;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.Html;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;
import android.widget.Toast;

import com.aaptrix.savitri.R;
import com.aaptrix.savitri.databeans.PlansData;
import com.aaptrix.savitri.session.URLs;
import com.google.android.material.button.MaterialButton;

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

import cz.msebera.android.httpclient.HttpEntity;
import cz.msebera.android.httpclient.HttpResponse;
import cz.msebera.android.httpclient.client.HttpClient;
import cz.msebera.android.httpclient.client.methods.HttpPost;
import cz.msebera.android.httpclient.entity.mime.HttpMultipartMode;
import cz.msebera.android.httpclient.entity.mime.MultipartEntityBuilder;
import cz.msebera.android.httpclient.impl.client.DefaultHttpClient;
import cz.msebera.android.httpclient.util.EntityUtils;

import static java.security.AccessController.getContext;

public class OfflinePurchase extends AppCompatActivity {

    Toolbar toolbar;
    String strText;
    TextView details;
    MaterialButton submitPurchase;
    PlansData plansData;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_offline_purchase);
        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        details = findViewById(R.id.payment_details);
        submitPurchase = findViewById(R.id.submit_purchase);
        plansData = (PlansData) getIntent().getSerializableExtra("plandata");
        submitPurchase.setOnClickListener(v -> startActivity(new Intent(this, SubmitPayment.class).putExtra("plandata", plansData)));
        setData();
    }

    private void setData() {
        if (checkConnection()) {
            fetchData();
        } else {
            Toast.makeText(this, "Please connect to internet", Toast.LENGTH_SHORT).show();
            try {
                FileNotFoundException fe = new FileNotFoundException();
                File directory = this.getFilesDir();
                ObjectInputStream in = new ObjectInputStream(new FileInputStream(new File(directory, "purchaseData")));
                String json = in.readObject().toString();
                in.close();
                JSONObject jsonObject = new JSONObject(json);
                JSONArray jsonArray = jsonObject.getJSONArray("appSetting");
                for (int i = 0; i < jsonArray.length(); i++) {
                    JSONObject jObject = jsonArray.getJSONObject(i);
                    strText = jObject.getString("offline_payment_details");
                    details.setText(Html.fromHtml(strText));
                }
                throw fe;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private void fetchData() {
        new Thread(() -> {
            try {
                HttpClient httpclient = new DefaultHttpClient();
                HttpPost httppost = new HttpPost(URLs.OFFLINE_PURCHASE_DETAIL);
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
                        JSONObject jsonObject = new JSONObject(result);
                        cacheJson(jsonObject);
                        JSONArray jsonArray = jsonObject.getJSONArray("appSetting");
                        for (int i = 0; i < jsonArray.length(); i++) {
                            JSONObject jObject = jsonArray.getJSONObject(i);
                            strText = jObject.getString("offline_payment_details");
                            details.setText(Html.fromHtml(strText));
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                        try {
                            FileNotFoundException fe = new FileNotFoundException();
                            File directory = this.getFilesDir();
                            ObjectInputStream in = new ObjectInputStream(new FileInputStream(new File(directory, "purchaseData")));
                            String json = in.readObject().toString();
                            in.close();
                            JSONObject jsonObject = new JSONObject(json);
                            JSONArray jsonArray = jsonObject.getJSONArray("appSetting");
                            for (int i = 0; i < jsonArray.length(); i++) {
                                JSONObject jObject = jsonArray.getJSONObject(i);
                                strText = jObject.getString("offline_payment_details");
                                details.setText(Html.fromHtml(strText));
                            }

                            throw fe;
                        } catch (Exception e1) {
                            e1.printStackTrace();
                        }
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
                if (getContext() != null) {
                    File directory = this.getFilesDir();
                    directory.mkdir();
                    out = new ObjectOutputStream(new FileOutputStream(new File(directory, "purchaseData")));
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
        connec = (ConnectivityManager)getSystemService(CONNECTIVITY_SERVICE);
        assert connec != null;
        return connec.getActiveNetworkInfo() != null && connec.getActiveNetworkInfo().isAvailable() && connec.getActiveNetworkInfo().isConnectedOrConnecting();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.share_menu, menu);
        menu.findItem(R.id.download).setVisible(false);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
        } else if (item.getItemId() == R.id.share) {
            Intent intent = new Intent(Intent.ACTION_SEND);
            intent.putExtra(Intent.EXTRA_TEXT, details.getText().toString());
            intent.setType("text/plain");
            startActivity(Intent.createChooser(intent, "Share via..."));
        }
        return super.onOptionsItemSelected(item);
    }
}
