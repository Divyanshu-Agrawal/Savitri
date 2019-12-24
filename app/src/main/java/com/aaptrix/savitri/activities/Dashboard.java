package com.aaptrix.savitri.activities;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.aaptrix.savitri.databeans.PlansData;
import com.aaptrix.savitri.session.URLs;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.navigation.NavigationView;
import com.squareup.picasso.Picasso;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import com.aaptrix.savitri.R;
import com.aaptrix.savitri.session.SharedPrefsManager;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.cardview.widget.CardView;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.gridlayout.widget.GridLayout;
import cz.msebera.android.httpclient.HttpEntity;
import cz.msebera.android.httpclient.HttpResponse;
import cz.msebera.android.httpclient.client.HttpClient;
import cz.msebera.android.httpclient.client.methods.HttpPost;
import cz.msebera.android.httpclient.entity.mime.HttpMultipartMode;
import cz.msebera.android.httpclient.entity.mime.MultipartEntityBuilder;
import cz.msebera.android.httpclient.impl.client.DefaultHttpClient;
import cz.msebera.android.httpclient.util.EntityUtils;
import de.hdodenhof.circleimageview.CircleImageView;

import static com.aaptrix.savitri.session.SharedPrefsNames.KEY_ORG_ID;
import static com.aaptrix.savitri.session.SharedPrefsNames.KEY_ORG_PLAN_TYPE;
import static com.aaptrix.savitri.session.SharedPrefsNames.KEY_PLAN_EXPIRE_DATE;
import static com.aaptrix.savitri.session.SharedPrefsNames.KEY_STORAGE_CYCLE;
import static com.aaptrix.savitri.session.SharedPrefsNames.KEY_USER_ID;
import static com.aaptrix.savitri.session.SharedPrefsNames.KEY_USER_IMAGE;
import static com.aaptrix.savitri.session.SharedPrefsNames.KEY_USER_NAME;
import static com.aaptrix.savitri.session.SharedPrefsNames.KEY_USER_ROLE;
import static com.aaptrix.savitri.session.SharedPrefsNames.USER_PREFS;
import static com.aaptrix.savitri.session.URLs.DATA_URL;

public class Dashboard extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {
	
	DrawerLayout drawer;
	ImageButton complianceBtn, renewalBtn, peopleBtn, tasksBtn, historyBtn, feedbackBtn;
	MaterialButton upgradePlanBtn;
	LinearLayout logoutBtn, appInfoBtn;
	CircleImageView profImage;
	CardView feedbackView, peopleView;
	GridLayout gridLayout;
	String planExpireDate;
	TextView planName;
	ArrayList<PlansData> plansArray = new ArrayList<>();
	String planId;
	
	@SuppressLint("SetTextI18n")
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_dashboard);
		Toolbar toolbar = findViewById(R.id.toolbar);
		setSupportActionBar(toolbar);
		
		drawer = findViewById(R.id.drawer_layout);
		ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
		drawer.addDrawerListener(toggle);
		toggle.syncState();
		
		NavigationView navigationView = findViewById(R.id.nav_view);
		navigationView.setNavigationItemSelectedListener(this);
		
		Menu menu = navigationView.getMenu();
		MenuItem people = menu.findItem(R.id.people);
		MenuItem feedback = menu.findItem(R.id.feedback);
		
		
		View headerView = navigationView.getHeaderView(0);
		
		complianceBtn = findViewById(R.id.compliances_btn);
		renewalBtn = findViewById(R.id.renewal_btn);
		peopleBtn = findViewById(R.id.people_btn);
		tasksBtn = findViewById(R.id.tasks_btn);
		historyBtn = findViewById(R.id.history_btn);
		feedbackBtn = findViewById(R.id.feedback_btn);
		upgradePlanBtn = findViewById(R.id.upgrade_plan_btn);
		logoutBtn = navigationView.findViewById(R.id.logout_btn);
		appInfoBtn = navigationView.findViewById(R.id.app_info_btn);
		feedbackView = findViewById(R.id.feedback_view);
		peopleView = findViewById(R.id.people_view);
		gridLayout = findViewById(R.id.grid_layout);
		
		profImage = headerView.findViewById(R.id.logged_user_image);
		TextView userName = headerView.findViewById(R.id.logged_user_name);
		TextView userRole = headerView.findViewById(R.id.logged_user_role);
		planName = headerView.findViewById(R.id.plan_name);
		fetchPlans();
		
		SharedPreferences sp = getSharedPreferences(USER_PREFS, Context.MODE_PRIVATE);
		planId = sp.getString(KEY_ORG_PLAN_TYPE, "");
		String url = DATA_URL + sp.getString(KEY_ORG_ID, "") + "/profile/" + sp.getString(KEY_USER_IMAGE, "");
		Picasso.with(this).load(url).placeholder(R.drawable.user_placeholder).into(profImage);
		userName.setText(sp.getString(KEY_USER_NAME, ""));
		userRole.setText(sp.getString(KEY_USER_ROLE, ""));
		
		if (!sp.getString(KEY_USER_ROLE, "").equals("Admin")) {
			gridLayout.removeView(peopleView);
			gridLayout.removeView(feedbackView);
			people.setVisible(false);
			feedback.setVisible(false);
			upgradePlanBtn.setVisibility(View.GONE);
		}
		
		headerView.setOnClickListener(v -> startActivity(new Intent(this, UserProfile.class)
				.putExtra("userId", sp.getString(KEY_USER_ID, ""))));
		
		logoutBtn.setOnClickListener(v -> new AlertDialog.Builder(this)
				.setMessage("Are you sure you want to logout?")
				.setPositiveButton("Yes", (dialog, which) -> {
					SharedPrefsManager.getInstance(this).logout();
					Intent intent = new Intent(this, AppLogin.class);
					intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
					startActivity(intent);
					finish();
				})
				.setNegativeButton("No", null)
				.show());
		
		if (sp.getString(KEY_ORG_PLAN_TYPE, "").equals("3")) {
			upgradePlanBtn.setText("Buy Plan Now");
		}
		
		try {
			planExpireDate = sp.getString(KEY_PLAN_EXPIRE_DATE, "");
			Calendar calendar = Calendar.getInstance();
			SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
			for (int i = 15; i > 0; i--) {
				calendar.setTime(sdf.parse(planExpireDate));
				int days = i;
				calendar.add(Calendar.DATE, -days);
				Date remainingDays = new Date(calendar.getTimeInMillis());
				Date todayDate = new Date(Calendar.getInstance().getTimeInMillis());
				if (remainingDays.compareTo(todayDate) == 0) {
					upgradePlanBtn.setText(days + " days left Renew Plan Now");
				}
			}
		} catch (ParseException e) {
			e.printStackTrace();
		}
		
		appInfoBtn.setOnClickListener(v -> startActivity(new Intent(this, AppInfo.class)));
		upgradePlanBtn.setOnClickListener(v -> startActivity(new Intent(this, PlansActivity.class)));
		complianceBtn.setOnClickListener(v -> startActivity(new Intent(this, CompliancesActivity.class)));
		peopleBtn.setOnClickListener(v -> startActivity(new Intent(this, PeopleActivity.class)));
		feedbackBtn.setOnClickListener(v -> startActivity(new Intent(this, FeedbackActivity.class)));
		renewalBtn.setOnClickListener(v -> startActivity(new Intent(this, RenewalActivity.class)));
		tasksBtn.setOnClickListener(v -> startActivity(new Intent(this, TasksActivity.class)));
		historyBtn.setOnClickListener(v -> {
			if (sp.getInt(KEY_STORAGE_CYCLE, 0) == 0) {
				new AlertDialog.Builder(this)
						.setMessage("Please upgrade your plan to access more..")
						.setPositiveButton("Upgrade", (dialog, which) -> startActivity(new Intent(this, PlansActivity.class)))
						.setNegativeButton("Cancel", null)
						.show();
			} else {
				startActivity(new Intent(this, HistoryActivity.class));
			}
		});
	}
	
	private void fetchPlans() {
		new Thread(() -> {
			try {
				HttpClient httpclient = new DefaultHttpClient();
				HttpPost httppost = new HttpPost(URLs.ALL_PLANS);
				MultipartEntityBuilder entityBuilder = MultipartEntityBuilder.create();
				entityBuilder.setMode(HttpMultipartMode.BROWSER_COMPATIBLE);
				HttpEntity entity = entityBuilder.build();
				httppost.setEntity(entity);
				HttpResponse response = httpclient.execute(httppost);
				HttpEntity httpEntity = response.getEntity();
				String result = EntityUtils.toString(httpEntity);
				Handler handler = new Handler(Looper.getMainLooper());
				handler.post(() -> {
					try {
						if (result.equals("null")) {
							planName.setVisibility(View.GONE);
						} else {
							JSONObject jsonObject = new JSONObject(result);
							JSONArray jsonArray = jsonObject.getJSONArray("allPlans");
							for (int i = 0; i < jsonArray.length(); i++) {
								JSONObject jObject = jsonArray.getJSONObject(i);
								PlansData data = new PlansData();
								data.setId(jObject.getString("plan_id"));
								data.setName(jObject.getString("plan_name"));
								plansArray.add(data);
							}
						}
					} catch (JSONException e) {
						e.printStackTrace();
					}
					for (int i = 0; i < plansArray.size(); i++) {
						if (plansArray.get(i).getId().equals(planId)) {
							planName.setText(plansArray.get(i).getName());
							break;
						}
					}
				});
			} catch (IOException e) {
				e.printStackTrace();
			}
		}).start();
	}
	
	@Override
	public void onResume() {
		super.onResume();
		SharedPreferences sp = getSharedPreferences(USER_PREFS, Context.MODE_PRIVATE);
		String url = DATA_URL + sp.getString(KEY_ORG_ID, "") + "/profile/" + sp.getString(KEY_USER_IMAGE, "");
		Picasso.with(this).load(url).placeholder(R.drawable.user_placeholder).into(profImage);
	}
	
	@Override
	public void onBackPressed() {
		if (drawer.isDrawerOpen(GravityCompat.START)) {
			drawer.closeDrawer(GravityCompat.START);
		} else {
			new AlertDialog.Builder(this)
					.setMessage("Are you sure you want to exit?")
					.setPositiveButton("Yes", (dialog, which) -> {
						Intent a = new Intent(Intent.ACTION_MAIN);
						a.addCategory(Intent.CATEGORY_HOME);
						a.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
						startActivity(a);
						finish();
						System.exit(0);
					})
					.setNegativeButton("No", null)
					.show();
		}
	}
	
	@SuppressWarnings("StatementWithEmptyBody")
	@Override
	public boolean onNavigationItemSelected(@NonNull MenuItem item) {
		int id = item.getItemId();
		if (id == R.id.faqs) {
			startActivity(new Intent(this, FaqActivity.class));
		} else if (id == R.id.compliance) {
			startActivity(new Intent(this, CompliancesActivity.class));
		} else if (id == R.id.renewal) {
			startActivity(new Intent(this, RenewalActivity.class));
		} else if (id == R.id.people) {
			startActivity(new Intent(this, PeopleActivity.class));
		} else if (id == R.id.tasks) {
			startActivity(new Intent(this, TasksActivity.class));
		} else if (id == R.id.history) {
			startActivity(new Intent(this, HistoryActivity.class));
		} else if (id == R.id.feedback) {
			startActivity(new Intent(this, FeedbackActivity.class));
		}
		drawer.closeDrawer(GravityCompat.START);
		return false;
	}
}
