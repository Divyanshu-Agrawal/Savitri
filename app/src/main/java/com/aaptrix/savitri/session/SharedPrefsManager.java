package com.aaptrix.savitri.session;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;

import com.aaptrix.savitri.activities.AppLogin;
import static com.aaptrix.savitri.session.SharedPrefsNames.KEY_USER_NAME;
import static com.aaptrix.savitri.session.SharedPrefsNames.USER_PREFS;

public class SharedPrefsManager {
	
	@SuppressLint("StaticFieldLeak")
	private static SharedPrefsManager sharedPrefsManager;
	@SuppressLint("StaticFieldLeak")
	private static Context mContext;
	
	private SharedPrefsManager(Context context) {
		mContext = context;
	}
	
	public static synchronized SharedPrefsManager getInstance(Context context) {
		if (sharedPrefsManager == null) {
			sharedPrefsManager = new SharedPrefsManager(context);
		}
		return sharedPrefsManager;
	}
	
	public boolean isLoggedIn() {
		SharedPreferences sharedPreferences = mContext.getSharedPreferences(USER_PREFS, Context.MODE_PRIVATE);
		return sharedPreferences.getString(KEY_USER_NAME, null) != null;
	}
	
	public void logout() {
		SharedPreferences sharedPreferences = mContext.getSharedPreferences(USER_PREFS, Context.MODE_PRIVATE);
		SharedPreferences.Editor editor = sharedPreferences.edit();
		editor.clear();
		editor.apply();
		mContext.startActivity(new Intent(mContext, AppLogin.class));
	}
}
