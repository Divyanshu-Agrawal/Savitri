package com.aaptrix.savitri.activities;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.Button;

import com.aaptrix.savitri.R;
import com.aaptrix.savitri.session.URLs;
import com.google.android.material.button.MaterialButton;

public class PoliciesActivity extends AppCompatActivity {

    Toolbar toolbar;
    MaterialButton tnc, privacy, refund;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_policies);
        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        tnc = findViewById(R.id.tnc_btn);
        privacy = findViewById(R.id.privacy_btn);
        refund = findViewById(R.id.refund_btn);

        privacy.setOnClickListener(v -> {
            Intent intent = new Intent(this, TnCActivity.class);
            intent.putExtra("type", "privacy");
            intent.putExtra("url", URLs.PRIVACY_URL);
            startActivity(intent);
        });

        tnc.setOnClickListener(v -> {
            Intent intent = new Intent(this, TnCActivity.class);
            intent.putExtra("type", "tnc");
            intent.putExtra("url", URLs.TnC_URL);
            startActivity(intent);
        });

        refund.setOnClickListener(v -> {
            Intent intent = new Intent(this, TnCActivity.class);
            intent.putExtra("type", "refund");
            intent.putExtra("url", URLs.REFUND_POLICY_URL);
            startActivity(intent);
        });
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
        }
        return super.onOptionsItemSelected(item);
    }
}
