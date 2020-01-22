package com.aaptrix.savitri.activities;

import com.aaptrix.savitri.R;
import com.aaptrix.savitri.asyncclass.LoginUser;
import com.aaptrix.savitri.session.SharedPrefsManager;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;

import static com.aaptrix.savitri.session.SharedPrefsNames.KEY_PASSWORD;
import static com.aaptrix.savitri.session.SharedPrefsNames.KEY_PHONE;
import static com.aaptrix.savitri.session.SharedPrefsNames.KEY_TOKEN;
import static com.aaptrix.savitri.session.SharedPrefsNames.KEY_USER_ROLE;
import static com.aaptrix.savitri.session.SharedPrefsNames.USER_PREFS;

public class SplashScreen extends AppCompatActivity {
	
	CountDownTimer mTimer;
	int options;
	SharedPreferences sp;
	public static String ROOT_URL, DATA_URL;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		View decorView = getWindow().getDecorView();
		options = View.SYSTEM_UI_FLAG_HIDE_NAVIGATION;
		decorView.setSystemUiVisibility(options);
		setContentView(R.layout.activity_splash_screen);
		sp = getSharedPreferences(USER_PREFS, Context.MODE_PRIVATE);

		if (Build.VERSION.SDK_INT > Build.VERSION_CODES.N) {
			ROOT_URL = "https://www.aaptrix.com/savitri/savitriapis/";
			DATA_URL = "https://www.aaptrix.com/savitri/storage/app/public/org/org_";
		} else {
			ROOT_URL = "http://www.aaptrix.com/savitri/savitriapis/";
			DATA_URL = "http://www.aaptrix.com/savitri/storage/app/public/org/org_";
		}

		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
			Window window = getWindow();
			window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
			window.setStatusBarColor(Color.TRANSPARENT);
		}
		refreshPlan();
		init();
	}


	private void refreshPlan() {
		String userPhone = sp.getString(KEY_PHONE, "");
		String userPass = sp.getString(KEY_PASSWORD, "");
		String token = sp.getString(KEY_TOKEN, " ");
		if (userPhone != null && userPass != null && !userPhone.isEmpty() && !userPass.isEmpty()) {
			LoginUser loginUser = new LoginUser(this, null, "dashboard");
			loginUser.execute(userPhone, userPass, token);
		}
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
			if (sp.getString(KEY_USER_ROLE, "").equals("Admin")) {
				startActivity(new Intent(this, Dashboard.class));
			} else {
				startActivity(new Intent(this, MemberDashboard.class));
			}
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
