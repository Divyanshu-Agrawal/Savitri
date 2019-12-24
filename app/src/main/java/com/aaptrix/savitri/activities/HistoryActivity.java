package com.aaptrix.savitri.activities;

import com.aaptrix.savitri.R;
import com.aaptrix.savitri.fragment.HistoryFragment;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentStatePagerAdapter;
import androidx.viewpager.widget.ViewPager;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.MenuItem;

import com.google.android.material.tabs.TabLayout;

import java.util.Objects;

import static com.aaptrix.savitri.session.SharedPrefsNames.KEY_STORAGE_CYCLE;
import static com.aaptrix.savitri.session.SharedPrefsNames.USER_PREFS;

public class HistoryActivity extends AppCompatActivity {
	
	Toolbar toolbar;
	TabLayout tabLayout;
	ViewPager pager;
	int cycleLimit;
	
	@SuppressLint("ClickableViewAccessibility")
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_history);
		toolbar = findViewById(R.id.toolbar);
		setSupportActionBar(toolbar);
		Objects.requireNonNull(getSupportActionBar()).setTitle("History");
		getSupportActionBar().setDisplayShowHomeEnabled(true);
		getSupportActionBar().setDisplayHomeAsUpEnabled(true);
		pager = findViewById(R.id.viewpager);
		tabLayout = findViewById(R.id.history_tab);
		tabLayout.setupWithViewPager(pager);
		tabLayout.setTabGravity(TabLayout.GRAVITY_FILL);
		tabLayout.addTab(tabLayout.newTab().setText("Cycle 1"));
		tabLayout.addTab(tabLayout.newTab().setText("Cycle 2"));
		tabLayout.addTab(tabLayout.newTab().setText("Cycle 3"));
		tabLayout.addTab(tabLayout.newTab().setText("Cycle 4"));
		tabLayout.addTab(tabLayout.newTab().setText("Cycle 5"));
		ViewPagerAdapter adapter = new ViewPagerAdapter(getSupportFragmentManager());
		pager.setAdapter(adapter);
		adapter.notifyDataSetChanged();
		tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
			@Override
			public void onTabSelected(TabLayout.Tab tab) {
				if (tab.getPosition() < cycleLimit) {
					pager.setCurrentItem(tab.getPosition(), true);
				} else {
					new AlertDialog.Builder(HistoryActivity.this)
							.setTitle("Please upgrade your plan to access more..")
							.setPositiveButton("Upgrade", (dialog, which) -> startActivity(new Intent(HistoryActivity.this, PlansActivity.class)))
							.setNegativeButton("Cancel", (dialog, which) -> pager.setCurrentItem(cycleLimit-1, true))
							.setCancelable(false)
							.show();
				}
			}
			
			@Override
			public void onTabUnselected(TabLayout.Tab tab) {
			
			}
			
			@Override
			public void onTabReselected(TabLayout.Tab tab) {
			
			}
		});
		
		SharedPreferences sp = getSharedPreferences(USER_PREFS, Context.MODE_PRIVATE);
		cycleLimit = sp.getInt(KEY_STORAGE_CYCLE, 0);
		
//		LinearLayout tab = ((LinearLayout)tabLayout.getChildAt(0));
//		for (int i = cycleLimit; i < tabLayout.getTabCount(); i++) {
//			tab.getChildAt(i).setOnTouchListener((v, event) -> true);
//		}
	}
	
	private class ViewPagerAdapter extends FragmentStatePagerAdapter {
		
		ViewPagerAdapter(FragmentManager manager) {
			super(manager);
		}
		
		@NonNull
		@Override
		public Fragment getItem(int position) {
			Fragment fragment;
			Bundle bundle;
			switch (position) {
				case 0:
					fragment = new HistoryFragment();
					bundle = new Bundle();
					bundle.putString("cycleCount", "1");
					if (position >= cycleLimit)
						bundle.putString("visibility", "no");
					else
						bundle.putString("visibility", "yes");
					fragment.setArguments(bundle);
					return fragment;
				case 1:
					fragment = new HistoryFragment();
					bundle = new Bundle();
					bundle.putString("cycleCount", "2");
					if (position >= cycleLimit)
						bundle.putString("visibility", "no");
					else
						bundle.putString("visibility", "yes");
					fragment.setArguments(bundle);
					return fragment;
				case 2:
					fragment = new HistoryFragment();
					bundle = new Bundle();
					bundle.putString("cycleCount", "3");
					if (position >= cycleLimit)
						bundle.putString("visibility", "no");
					else
						bundle.putString("visibility", "yes");
					fragment.setArguments(bundle);
					return fragment;
				case 3:
					fragment = new HistoryFragment();
					bundle = new Bundle();
					bundle.putString("cycleCount", "4");
					if (position >= cycleLimit)
						bundle.putString("visibility", "no");
					else
						bundle.putString("visibility", "yes");
					fragment.setArguments(bundle);
					return fragment;
				case 4:
					fragment = new HistoryFragment();
					bundle = new Bundle();
					bundle.putString("cycleCount", "5");
					if (position >= cycleLimit)
						bundle.putString("visibility", "no");
					else
						bundle.putString("visibility", "yes");
					fragment.setArguments(bundle);
					return fragment;
				default:
					return null;
			}
		}
		
		@Override
		public int getCount() {
			return 5;
		}
		
		@Override
		public CharSequence getPageTitle(int position) {
			String title = null;
			if (position == 0) {
				title = "Cycle 1";
			} else if (position == 1) {
				title = "Cycle 2";
			} else if (position == 2) {
				title = "Cycle 3";
			} else if (position == 3) {
				title = "Cycle 4";
			} else if (position == 4) {
				title = "Cycle 5";
			}
			return title;
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
