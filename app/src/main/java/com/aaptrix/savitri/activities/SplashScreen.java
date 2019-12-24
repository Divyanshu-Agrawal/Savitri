package com.aaptrix.savitri.activities;

import com.aaptrix.savitri.R;
import com.aaptrix.savitri.session.SharedPrefsManager;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.view.View;

public class SplashScreen extends AppCompatActivity {
	
	CountDownTimer mTimer;
	int options;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		View decorView = getWindow().getDecorView();
		options = View.SYSTEM_UI_FLAG_FULLSCREEN | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION;
		decorView.setSystemUiVisibility(options);
		setContentView(R.layout.activity_splash_screen);
		init();
	}
	
	private void init() {
		mTimer = new CountDownTimer(3000, 1000) {
			@Override
			public void onTick(long millisUntilFinished) {
			
			}
			
			@Override
			public void onFinish() {
				navigate();
				getWindow().clearFlags(options);
				finish();
			}
		};
		mTimer.start();
	}
	
	private void navigate() {
		if (SharedPrefsManager.getInstance(this).isLoggedIn()) {
			startActivity(new Intent(this, Dashboard.class));
		} else {
			startActivity(new Intent(this, AppLogin.class));
		}
	}
	
	@Override
	protected void onDestroy() {
		super.onDestroy();
		if (mTimer != null) {
			mTimer.cancel();
			mTimer = null;
		}
	}
}
