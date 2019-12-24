package com.aaptrix.savitri.activities;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;

import com.aaptrix.savitri.R;
import com.aaptrix.savitri.databeans.PlansData;

import static com.aaptrix.savitri.session.SharedPrefsNames.KEY_ORG_ID;
import static com.aaptrix.savitri.session.SharedPrefsNames.KEY_ORG_NAME;
import static com.aaptrix.savitri.session.SharedPrefsNames.USER_PREFS;

public class BuyPlan extends AppCompatActivity {

    PlansData plansData;
    Toolbar toolbar;
    LinearLayout progressLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_buy_plan);
        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        progressLayout = findViewById(R.id.progress_layout);
        plansData = (PlansData) getIntent().getSerializableExtra("data");
        SharedPreferences sp = getSharedPreferences(USER_PREFS, Context.MODE_PRIVATE);
        String transRefId = sp.getString(KEY_ORG_ID, "") + sp.getString(KEY_ORG_NAME, "");
        String transNote = "PAY%20" + plansData.getPlanCost() + "%20TO%20SAVITRI";
        String uri = "upi://pay?pa=" + "" + "&pn=" + ""
                + "&tr=" + transRefId + "&tn=" + transNote + "&am="+ "" +"&mam=null&cu=INR";
        Intent intent = new Intent();
        intent.setData(Uri.parse(uri));
        Intent chooser = Intent.createChooser(intent, "Pay with...");
        startActivityForResult(chooser, 1, null);
    }

    @SuppressLint("SetTextI18n")
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1) {
            if (resultCode == Activity.RESULT_OK) {
                progressLayout.setVisibility(View.GONE);
                String trnId = data.getStringExtra("txnId");
                String resCode = data.getStringExtra("responseCode");
                String status = data.getStringExtra("Status");
                String refNo = data.getStringExtra("txnRef");

                Log.e("trn Id", String.valueOf(trnId));
                Log.e("res code", String.valueOf(resCode));
                Log.e("status", String.valueOf(status));
            }
        }
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
