package com.aaptrix.savitri.activities;

import com.aaptrix.savitri.R;
import com.aaptrix.savitri.adapter.PlansAdapter;
import com.aaptrix.savitri.databeans.PlansData;
import com.aaptrix.savitri.fragment.FreePlanFragment;
import com.aaptrix.savitri.fragment.PlanFragment;
import com.aaptrix.savitri.session.SharedPrefsManager;
import com.aaptrix.savitri.session.URLs;
import com.google.android.material.tabs.TabLayout;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentStatePagerAdapter;
import androidx.viewpager.widget.ViewPager;
import cz.msebera.android.httpclient.HttpEntity;
import cz.msebera.android.httpclient.HttpResponse;
import cz.msebera.android.httpclient.client.HttpClient;
import cz.msebera.android.httpclient.client.methods.HttpPost;
import cz.msebera.android.httpclient.entity.mime.HttpMultipartMode;
import cz.msebera.android.httpclient.entity.mime.MultipartEntityBuilder;
import cz.msebera.android.httpclient.impl.client.DefaultHttpClient;
import cz.msebera.android.httpclient.util.EntityUtils;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ListView;
import android.widget.ProgressBar;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;

public class PlansActivity extends AppCompatActivity {
	
	Toolbar toolbar;
	TabLayout tabLayout;
	ViewPager pager;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_plans);
		toolbar = findViewById(R.id.toolbar);
		setSupportActionBar(toolbar);
		getSupportActionBar().setDisplayShowHomeEnabled(true);
		getSupportActionBar().setDisplayHomeAsUpEnabled(true);
		pager = findViewById(R.id.viewpager);
		tabLayout = findViewById(R.id.tablayout);
		tabLayout.setupWithViewPager(pager);
		tabLayout.setTabGravity(TabLayout.GRAVITY_FILL);
		tabLayout.addTab(tabLayout.newTab().setText("Paid"));
		tabLayout.addTab(tabLayout.newTab().setText("Free"));
		ViewPagerAdapter adapter = new ViewPagerAdapter(getSupportFragmentManager());
		pager.setAdapter(adapter);
		adapter.notifyDataSetChanged();
		tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
			@Override
			public void onTabSelected(TabLayout.Tab tab) {
				pager.setCurrentItem(tab.getPosition(), true);
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
					return new PlanFragment();
				case 1:
					return new FreePlanFragment();
				default:
					return null;
			}
		}

		@Override
		public int getCount() {
			return 2;
		}

		@Override
		public CharSequence getPageTitle(int position) {
			String title = null;
			if (position == 0) {
				title = "Paid";
			} else if (position == 1) {
				title = "Free";
			}
			return title;
		}

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
