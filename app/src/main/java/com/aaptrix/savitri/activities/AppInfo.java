package com.aaptrix.savitri.activities;

import com.aaptrix.savitri.BuildConfig;
import com.aaptrix.savitri.R;
import com.aaptrix.savitri.session.URLs;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ShareCompat;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.util.Objects;

public class AppInfo extends AppCompatActivity {
	
	String versionName;
	TextView version, webUrl, contactNumber, contactEmail, privacyPolicy, tnc;
	Toolbar toolbar;
	WebView webView;
	LinearLayout relativeLayout;
	TextView rate, share;
	
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
		rate = findViewById(R.id.rate_app);
		share = findViewById(R.id.share_app);
		tnc = findViewById(R.id.terms_condition);
		
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

		rate.setOnClickListener(v -> {
			Intent i = new Intent(Intent.ACTION_VIEW);
			i.setData(Uri.parse("market://details?id=com.aaptrix.savitri"));
			startActivity(i);
		});

		share.setOnClickListener(v -> {
			String msg = "Install Savitri: Your compliance tracking assistant, and never miss any compliance renewals." + " " + "\nhttp://play.google.com/store/apps/details?id=com.aaptrix.savitri";
			ShareCompat.IntentBuilder.from(this)
					.setType("text/plain")
					.setChooserTitle("Share via...")
					.setText(msg)
					.startChooser();
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
