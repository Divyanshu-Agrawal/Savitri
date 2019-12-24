package com.aaptrix.savitri.activities;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ProgressBar;

import com.aaptrix.savitri.R;

public class TnCActivity extends AppCompatActivity {
	
	Toolbar toolbar;
	WebView webView;
	ProgressBar progressBar;
	String type, url;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_tnc);
		toolbar = findViewById(R.id.toolbar);
		progressBar = findViewById(R.id.progress_bar);
		webView = findViewById(R.id.webview);
		type = getIntent().getStringExtra("type");
		url = getIntent().getStringExtra("url");
		setSupportActionBar(toolbar);
		if (type.equals("tnc")) {
			getSupportActionBar().setTitle("Terms and Condition");
		} else {
			getSupportActionBar().setTitle("Privacy Policy");
		}
		getSupportActionBar().setDisplayShowHomeEnabled(true);
		getSupportActionBar().setDisplayHomeAsUpEnabled(true);
		progressBar.setVisibility(View.VISIBLE);
		progressBar.bringToFront();
		webView.setWebViewClient(new WebViewClient(){
			
			@Override
			public void onLoadResource(WebView webView, String url) {
			
			}
			
			@Override
			public void onPageFinished(WebView view, String url) {
				progressBar.setVisibility(View.GONE);
			}
		});
		WebSettings webSettings = webView.getSettings();
		webSettings.setJavaScriptEnabled(true);
		webSettings.setBuiltInZoomControls(false);
		webSettings.setLayoutAlgorithm(WebSettings.LayoutAlgorithm.SINGLE_COLUMN);
		webView.loadUrl(url);
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
		super.onBackPressed();
	}
}
