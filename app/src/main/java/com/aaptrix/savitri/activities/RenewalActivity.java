package com.aaptrix.savitri.activities;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

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
import java.util.ArrayList;

import com.aaptrix.savitri.R;
import com.aaptrix.savitri.adapter.RenewalStatusAdapter;
import com.aaptrix.savitri.databeans.ComplianceData;
import com.aaptrix.savitri.fragment.ExpiredRenewalsFragment;
import com.aaptrix.savitri.fragment.NewRenewalFragment;
import com.aaptrix.savitri.session.SharedPrefsManager;
import com.aaptrix.savitri.session.URLs;
import com.google.android.material.tabs.TabLayout;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentStatePagerAdapter;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import androidx.viewpager.widget.ViewPager;

import cz.msebera.android.httpclient.HttpEntity;
import cz.msebera.android.httpclient.HttpResponse;
import cz.msebera.android.httpclient.client.HttpClient;
import cz.msebera.android.httpclient.client.methods.HttpPost;
import cz.msebera.android.httpclient.entity.mime.HttpMultipartMode;
import cz.msebera.android.httpclient.entity.mime.MultipartEntityBuilder;
import cz.msebera.android.httpclient.impl.client.DefaultHttpClient;
import cz.msebera.android.httpclient.util.EntityUtils;

import static com.aaptrix.savitri.session.SharedPrefsNames.KEY_ORG_ID;
import static com.aaptrix.savitri.session.SharedPrefsNames.KEY_SESSION_ID;
import static com.aaptrix.savitri.session.SharedPrefsNames.USER_PREFS;
import static java.security.AccessController.getContext;

public class RenewalActivity extends AppCompatActivity {
	
	Toolbar toolbar;
	TabLayout tabLayout;
	ViewPager pager;
	String strOrgId, strSessionId;
	ImageView expiredImage;
	TextView expiredText;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_renewal);
		toolbar = findViewById(R.id.toolbar);
		setSupportActionBar(toolbar);
		getSupportActionBar().setDisplayShowHomeEnabled(true);
		getSupportActionBar().setDisplayHomeAsUpEnabled(true);
		SharedPreferences sp = getSharedPreferences(USER_PREFS, Context.MODE_PRIVATE);
		strOrgId = sp.getString(KEY_ORG_ID, "");
		strSessionId = sp.getString(KEY_SESSION_ID, "");
		pager = findViewById(R.id.viewpager);
		tabLayout = findViewById(R.id.tablayout);
		tabLayout.setupWithViewPager(pager);
		tabLayout.setTabGravity(TabLayout.GRAVITY_FILL);

		View renewalView = ((LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE))
				.inflate(R.layout.custom_tab, null, false);

		View expiredView = ((LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE))
				.inflate(R.layout.custom_tab, null, false);

		TextView renewalText = renewalView.findViewById(R.id.text);
		renewalText.setText("Renewals");

		expiredText = expiredView.findViewById(R.id.text);
		expiredImage = expiredView.findViewById(R.id.image);
		expiredText.setText("Expired");

		ViewPagerAdapter adapter = new ViewPagerAdapter(getSupportFragmentManager());
		pager.setAdapter(adapter);
		adapter.notifyDataSetChanged();
		tabLayout.getTabAt(0).setCustomView(renewalView);
		tabLayout.getTabAt(1).setCustomView(expiredView);
		if (checkConnection()) {
			fetchRenewalStatus();
		} else {
			try {
				File directory = getFilesDir();
				ObjectInputStream in = new ObjectInputStream(new FileInputStream(new File(directory, "expiredRenewalData")));
				String json = in.readObject().toString();
				in.close();
				if (!json.isEmpty()) {
					expiredImage.setVisibility(View.VISIBLE);
					expiredText.setGravity(Gravity.START | Gravity.CENTER_VERTICAL);
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
			@Override
			public void onTabSelected(TabLayout.Tab tab) {
				pager.setCurrentItem(tab.getPosition(), true);
				if (tab.getPosition() == 1) {
					expiredImage.setVisibility(View.GONE);
					expiredText.setGravity(Gravity.CENTER);
				}
			}

			@Override
			public void onTabUnselected(TabLayout.Tab tab) {

			}

			@Override
			public void onTabReselected(TabLayout.Tab tab) {

			}
		});

	}

	private class ViewPagerAdapter extends FragmentStatePagerAdapter {

		ViewPagerAdapter(FragmentManager manager) {
			super(manager);
		}

		@NonNull
		@Override
		public Fragment getItem(int position) {
			switch (position) {
				case 0:
					return new NewRenewalFragment();
				case 1:
					return new ExpiredRenewalsFragment();
				default:
					return null;
			}
		}

		@Override
		public int getCount() {
			return 2;
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
		return true;
	}

    private void fetchRenewalStatus() {
		new Thread(() -> {
			try {
				HttpClient httpclient = new DefaultHttpClient();
				HttpPost httppost = new HttpPost(URLs.EXP_RENEWAL_STATUS);
				MultipartEntityBuilder entityBuilder = MultipartEntityBuilder.create();
				entityBuilder.setMode(HttpMultipartMode.BROWSER_COMPATIBLE);
				entityBuilder.addTextBody("org_details_id", strOrgId);
				entityBuilder.addTextBody("app_session_id", strSessionId);
				HttpEntity entity = entityBuilder.build();
				httppost.setEntity(entity);
				HttpResponse response = httpclient.execute(httppost);
				HttpEntity httpEntity = response.getEntity();
				String result = EntityUtils.toString(httpEntity);
				Handler handler = new Handler(Looper.getMainLooper());
				handler.post(() -> {
					if (!result.contains("{\"allRenews\":null}") || !result.contains("\"success\":false,\"msg\":\"Session Expire\"")) {
						expiredImage.setVisibility(View.VISIBLE);
						expiredText.setGravity(Gravity.START | Gravity.CENTER_VERTICAL);
					}
				});
			} catch (IOException e) {
				e.printStackTrace();
			}
		}).start();
	}

	private boolean checkConnection() {
		ConnectivityManager connec;
		connec = (ConnectivityManager) getSystemService(CONNECTIVITY_SERVICE);
		assert connec != null;
		return connec.getActiveNetworkInfo() != null && connec.getActiveNetworkInfo().isAvailable() && connec.getActiveNetworkInfo().isConnectedOrConnecting();
	}
}
