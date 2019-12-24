package com.aaptrix.savitri.activities;

import com.aaptrix.savitri.BuildConfig;
import com.aaptrix.savitri.R;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.util.Objects;

public class AppInfo extends AppCompatActivity {
	
	String versionName;
	TextView version, webUrl, contactNumber, contactEmail, privacyPolicy;
	Toolbar toolbar;
	WebView webView;
	RelativeLayout relativeLayout;
	
	@SuppressLint("SetJavaScriptEnabled")
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_app_info);
		versionName = "Version " + BuildConfig.VERSION_NAME;
		version = findViewById(R.id.version);
		relativeLayout = findViewById(R.id.relative_layout);
		privacyPolicy = findViewById(R.id.privacy_policy);
		version.setText(versionName);
		webView = findViewById(R.id.privacy_policy_webview);
		WebSettings webSettings = webView.getSettings();
		webSettings.setJavaScriptEnabled(true);
		webSettings.setBuiltInZoomControls(false);
		
		toolbar = findViewById(R.id.app_info_toolbar);
		setSupportActionBar(toolbar);
		getSupportActionBar().setDisplayHomeAsUpEnabled(true);
		getSupportActionBar().setDisplayShowHomeEnabled(true);
		toolbar.setTitleTextColor(getResources().getColor(android.R.color.white));
		setTitle("");
		
		webUrl = findViewById(R.id.web_url);
		
		webUrl.setOnClickListener(v -> {
			Intent browserIntent = new Intent(Intent.ACTION_VIEW);
			browserIntent.setData(Uri.parse(webUrl.getText().toString()));
			startActivity(browserIntent);
		});
		
		contactNumber = findViewById(R.id.contact_number);
		contactNumber.setOnClickListener(v -> {
			Intent phoneIntent = new Intent(Intent.ACTION_DIAL);
			String phone = "tel:" + contactNumber.getText().toString().trim();
			phoneIntent.setData(Uri.parse(phone));
			startActivity(phoneIntent);
		});
		
		contactEmail = findViewById(R.id.contact_email);
		contactEmail.setOnClickListener(v -> {
			Intent mailIntent = new Intent(Intent.ACTION_VIEW);
			Uri data = Uri.parse("mailto:?to=hello@apptrix.com");
			mailIntent.setData(data);
			startActivity(mailIntent);
		});
		
		privacyPolicy.setOnClickListener(v -> {
			webView.loadUrl("http://www.aaptrix.com/app-policies/privacy_policy.html");
			webView.setVisibility(View.VISIBLE);
			relativeLayout.setVisibility(View.GONE);
			getSupportActionBar().setTitle("Privacy Policy");
		});
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
		if (webView.getVisibility() == View.VISIBLE) {
			webView.setVisibility(View.GONE);
			relativeLayout.setVisibility(View.VISIBLE);
			Objects.requireNonNull(getSupportActionBar()).setTitle("");
		} else {
			super.onBackPressed();
			finish();
		}
	}
}
