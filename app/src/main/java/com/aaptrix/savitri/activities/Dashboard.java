package com.aaptrix.savitri.activities;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.aaptrix.savitri.asyncclass.UploadCompliance;
import com.aaptrix.savitri.asyncclass.UploadPayment;
import com.aaptrix.savitri.session.URLs;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.navigation.NavigationView;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Type;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.Objects;

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
import androidx.core.app.ShareCompat;
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

import static com.aaptrix.savitri.session.SharedPrefsNames.COM_AUTH;
import static com.aaptrix.savitri.session.SharedPrefsNames.COM_CERT;
import static com.aaptrix.savitri.session.SharedPrefsNames.COM_NAME;
import static com.aaptrix.savitri.session.SharedPrefsNames.COM_NOTES;
import static com.aaptrix.savitri.session.SharedPrefsNames.COM_OTHER_AUTH;
import static com.aaptrix.savitri.session.SharedPrefsNames.COM_PREFS;
import static com.aaptrix.savitri.session.SharedPrefsNames.COM_REF;
import static com.aaptrix.savitri.session.SharedPrefsNames.COM_VALID_FROM;
import static com.aaptrix.savitri.session.SharedPrefsNames.COM_VALID_TO;
import static com.aaptrix.savitri.session.SharedPrefsNames.FLAG;
import static com.aaptrix.savitri.session.SharedPrefsNames.KEY_EMAIL;
import static com.aaptrix.savitri.session.SharedPrefsNames.KEY_GRACE;
import static com.aaptrix.savitri.session.SharedPrefsNames.KEY_MOBILE;
import static com.aaptrix.savitri.session.SharedPrefsNames.KEY_ORG_ID;
import static com.aaptrix.savitri.session.SharedPrefsNames.KEY_ORG_PLAN_TYPE;
import static com.aaptrix.savitri.session.SharedPrefsNames.KEY_PLAN_EXP;
import static com.aaptrix.savitri.session.SharedPrefsNames.KEY_PLAN_EXPIRE_DATE;
import static com.aaptrix.savitri.session.SharedPrefsNames.KEY_PLAN_NAME;
import static com.aaptrix.savitri.session.SharedPrefsNames.KEY_PLAN_PURCHASE;
import static com.aaptrix.savitri.session.SharedPrefsNames.KEY_SERVER_STATUS;
import static com.aaptrix.savitri.session.SharedPrefsNames.KEY_SESSION_ID;
import static com.aaptrix.savitri.session.SharedPrefsNames.KEY_STORAGE_CYCLE;
import static com.aaptrix.savitri.session.SharedPrefsNames.KEY_USER_ID;
import static com.aaptrix.savitri.session.SharedPrefsNames.KEY_USER_IMAGE;
import static com.aaptrix.savitri.session.SharedPrefsNames.KEY_USER_NAME;
import static com.aaptrix.savitri.session.SharedPrefsNames.KEY_USER_ROLE;
import static com.aaptrix.savitri.session.SharedPrefsNames.PAYMENT_PREFS;
import static com.aaptrix.savitri.session.SharedPrefsNames.USER_PREFS;
import static com.aaptrix.savitri.activities.SplashScreen.DATA_URL;

public class Dashboard extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {
	
	DrawerLayout drawer;
	ImageButton complianceBtn, renewalBtn, peopleBtn, tasksBtn, historyBtn, feedbackBtn;
	MaterialButton upgradePlanBtn, upPlanBtn;
	CircleImageView profImage;
	GridLayout gridLayout;
	String planExpireDate;
	TextView planName;
	String planId, strOrgId, strUserId, strSessionId;
	SharedPreferences sp;
	Intent intent;

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


		intent = new Intent(this, PlansActivity.class);
		View headerView = navigationView.getHeaderView(0);
		
		complianceBtn = findViewById(R.id.compliances_btn);
		renewalBtn = findViewById(R.id.renewal_btn);
		peopleBtn = findViewById(R.id.people_btn);
		tasksBtn = findViewById(R.id.tasks_btn);
		historyBtn = findViewById(R.id.history_btn);
		feedbackBtn = findViewById(R.id.feedback_btn);
		upgradePlanBtn = findViewById(R.id.upgrade_plan_btn);
		gridLayout = findViewById(R.id.grid_layout);
		
		profImage = headerView.findViewById(R.id.logged_user_image);
		TextView userName = headerView.findViewById(R.id.logged_user_name);
		TextView userRole = headerView.findViewById(R.id.logged_user_role);
		planName = headerView.findViewById(R.id.plan_name);
		
		sp = getSharedPreferences(USER_PREFS, Context.MODE_PRIVATE);
		planId = sp.getString(KEY_ORG_PLAN_TYPE, "");
		strOrgId = sp.getString(KEY_ORG_ID, "");
		strSessionId = sp.getString(KEY_SESSION_ID, "");
		strUserId = sp.getString(KEY_USER_ID, "");
		String strPlanName = sp.getString(KEY_PLAN_NAME, "");
		String url = DATA_URL + sp.getString(KEY_ORG_ID, "") + "/profile/" + sp.getString(KEY_USER_IMAGE, "");
		Picasso.with(this).load(url).placeholder(R.drawable.user_placeholder).into(profImage);
		userName.setText(sp.getString(KEY_USER_NAME, ""));
		userRole.setText(sp.getString(KEY_USER_ROLE, ""));

		if (strPlanName != null && !strPlanName.isEmpty()) {
			planName.setText(strPlanName);
		} else {
			fetchPlans();
		}

		RelativeLayout profLayout = headerView.findViewById(R.id.profile_layout);
		profLayout.setOnClickListener(v -> startActivity(new Intent(this, UserProfile.class)
				.putExtra("userId", sp.getString(KEY_USER_ID, ""))));

		upPlanBtn = headerView.findViewById(R.id.upgrade_plan_btn);
		
		if (Objects.equals(sp.getString(KEY_ORG_PLAN_TYPE, ""), "3")) {
			upgradePlanBtn.setText("Buy Plan Now");
			upPlanBtn.setText("Buy Plan Now");
		}

		upPlanBtn.setOnClickListener(v -> {
			startActivity(new Intent(this, PlansActivity.class));
			drawer.closeDrawers();
		});

		checkValidity();

		upgradePlanBtn.setOnClickListener(v -> startActivity(new Intent(this, PlansActivity.class)));
		complianceBtn.setOnClickListener(v -> startActivity(new Intent(this, CompliancesActivity.class)));
		peopleBtn.setOnClickListener(v -> {
			if (planId.equals("3")) {
				new AlertDialog.Builder(this)
						.setMessage("Please upgrade your plan to access more.")
						.setPositiveButton("Upgrade", (dialog, which) -> startActivity(intent))
						.setNegativeButton("Cancel", null)
						.show();
			} else {
				startActivity(new Intent(this, PeopleActivity.class));
			}
		});

		feedbackBtn.setOnClickListener(v -> startActivity(new Intent(this, FeedbackActivity.class)));
		renewalBtn.setOnClickListener(v -> startActivity(new Intent(this, RenewalActivity.class)));
		tasksBtn.setOnClickListener(v -> {
			if (planId.equals("3")) {
				new AlertDialog.Builder(this)
						.setMessage("Please upgrade your plan to access more.")
						.setPositiveButton("Upgrade", (dialog, which) -> startActivity(intent))
						.setNegativeButton("Cancel", null)
						.show();
			} else {
				startActivity(new Intent(this, TasksActivity.class));
			}
		});

		historyBtn.setOnClickListener(v -> {
			if (sp.getInt(KEY_STORAGE_CYCLE, 0) == 0) {
				new AlertDialog.Builder(this)
						.setMessage("Please upgrade your plan to access more.")
						.setPositiveButton("Upgrade", (dialog, which) -> startActivity(intent))
						.setNegativeButton("Cancel", null)
						.show();
			} else {
				startActivity(new Intent(this, HistoryActivity.class));
			}
		});

		SharedPreferences comPrefs = getSharedPreferences(COM_PREFS, Context.MODE_PRIVATE);
		if (checkConnection()) {
			if (comPrefs.getBoolean(FLAG, false)) {
				GsonBuilder gsonBuilder = new GsonBuilder();
				Type type = new TypeToken<ArrayList<File>>() {}.getType();
				ArrayList<File> filepath = gsonBuilder.create().fromJson(comPrefs.getString(COM_CERT, ""), type);
				UploadCompliance uploadCompliance = new UploadCompliance(this, null, filepath, "offline");
				uploadCompliance.execute(strOrgId, comPrefs.getString(COM_NAME, ""),
						comPrefs.getString(COM_REF, ""), comPrefs.getString(COM_AUTH, "Other"),
						comPrefs.getString(COM_NOTES, ""), comPrefs.getString(COM_VALID_FROM, ""),
						comPrefs.getString(COM_VALID_TO, ""),
						strUserId, comPrefs.getString(COM_OTHER_AUTH, ""), strSessionId);
			}
		}
	}

	@SuppressLint("SetTextI18n")
	private void checkValidity() {
		try {
			planExpireDate = sp.getString(KEY_PLAN_EXPIRE_DATE, "");
			Calendar calendar = Calendar.getInstance();
			SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
            calendar.setTime(sdf.parse(planExpireDate));
            calendar.add(Calendar.DATE, 1);
            Date remainingDays = new Date(calendar.getTimeInMillis());
            Date todayDate = new Date(Calendar.getInstance().getTimeInMillis());
            int difference= ((int)((remainingDays.getTime()/(24*60*60*1000))
                            -(int)(todayDate.getTime()/(24*60*60*1000))));
            if (difference > 0 && difference < 16) {
				upgradePlanBtn.setText(difference + " days left Renew Plan Now");
				upPlanBtn.setText(difference + " days left");
			} else if (difference == 0) {
				upgradePlanBtn.setText("Plan Expires Today Renew Now");
				upPlanBtn.setText("Renew Now");
			} else if (difference < 0){
				Calendar c = Calendar.getInstance();
				c.setTime(sdf.parse(planExpireDate));
				c.add(Calendar.DATE, sp.getInt(KEY_GRACE, 0)+1);
				Date graceExp = new Date(c.getTimeInMillis());
				Date planExp = sdf.parse(planExpireDate);
				int diff = ((int)((graceExp.getTime()/(24*60*60*1000))
						-(int)(planExp.getTime()/(24*60*60*1000))));
				if (diff > 0) {
					sdf = new SimpleDateFormat("dd-MM-yyyy", Locale.getDefault());
					String msg = "Your plan has expired on " + sdf.parse(planExpireDate) + ". You have grace period of "
							+ diff + " days, after that you will lose all of your data.";
					new AlertDialog.Builder(this).setMessage(msg)
							.setPositiveButton("Buy Plan", (dialog, which) -> startActivity(new Intent(this, PlansActivity.class)))
							.setNegativeButton("Exit", (dialog, which) -> System.exit(0))
							.setCancelable(false)
							.show();
				} else {
					sdf = new SimpleDateFormat("dd-MM-yyyy", Locale.getDefault());
					String msg = "Your plan has expired on " + sdf.parse(planExpireDate) + ". You have exhausted your grace period on "
							+ sdf.format(c.getTimeInMillis()) + ". Please contact us for more information";
					new AlertDialog.Builder(this).setMessage(msg)
							.setPositiveButton("Contact Us", (dialog, which) -> startActivity(new Intent(this, FeedbackActivity.class)))
							.setNegativeButton("Exit", (dialog, which) -> System.exit(0))
							.setCancelable(false)
							.show();
				}
			} else if (difference > 16) {
            	upPlanBtn.setText("View Plans");
            	upgradePlanBtn.setText("View Plans");
			}
		} catch (ParseException e) {
			e.printStackTrace();
		}
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
								if (jObject.getString("plan_id").equals(planId)) {
									planName.setText(jObject.getString("plan_name"));
									sp.edit().putString(KEY_PLAN_NAME, jObject.getString("plan_name")).apply();
									break;
								}
							}
						}
					} catch (JSONException e) {
						e.printStackTrace();
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
		checkValidity();
		SharedPreferences sp = getSharedPreferences(USER_PREFS, Context.MODE_PRIVATE);
		String url = DATA_URL + sp.getString(KEY_ORG_ID, "") + "/profile/" + sp.getString(KEY_USER_IMAGE, "");
		Picasso.with(this).load(url).placeholder(R.drawable.user_placeholder).into(profImage);

		SharedPreferences comPrefs = getSharedPreferences(COM_PREFS, Context.MODE_PRIVATE);
		if (checkConnection()) {
			if (comPrefs.getBoolean(FLAG, false)) {
				GsonBuilder gsonBuilder = new GsonBuilder();
				Type type = new TypeToken<ArrayList<File>>() {}.getType();
				ArrayList<File> filepath = gsonBuilder.create().fromJson(comPrefs.getString(COM_CERT, ""), type);
				UploadCompliance uploadCompliance = new UploadCompliance(this, null, filepath, "offline");
				uploadCompliance.execute(strOrgId, comPrefs.getString(COM_NAME, ""),
						comPrefs.getString(COM_REF, ""), comPrefs.getString(COM_AUTH, "Other"),
						comPrefs.getString(COM_NOTES, ""), comPrefs.getString(COM_VALID_FROM, ""),
						comPrefs.getString(COM_VALID_TO, ""),
						strUserId, comPrefs.getString(COM_OTHER_AUTH, ""), strSessionId);
			}
		}

		SharedPreferences payPrefs = getSharedPreferences(PAYMENT_PREFS, Context.MODE_PRIVATE);
		if (checkConnection()) {
			if (payPrefs.getBoolean(KEY_SERVER_STATUS, false)) {
				UploadPayment payment = new UploadPayment(this, null);
				payment.execute(payPrefs.getString(KEY_ORG_ID, ""), payPrefs.getString(KEY_USER_ID, ""),
						payPrefs.getString(KEY_MOBILE, ""), payPrefs.getString(KEY_EMAIL, ""), payPrefs.getString(KEY_PLAN_PURCHASE, ""),
						payPrefs.getString(KEY_PLAN_EXP, ""), payPrefs.getString(KEY_PLAN_NAME, ""));
			}
		}
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
			if (planId.equals("3")) {
				new AlertDialog.Builder(this)
						.setMessage("Please upgrade your plan to access more.")
						.setPositiveButton("Upgrade", (dialog, which) -> startActivity(intent))
						.setNegativeButton("Cancel", null)
						.show();
			} else {
				startActivity(new Intent(this, PeopleActivity.class));
			}
		} else if (id == R.id.tasks) {
			if (planId.equals("3")) {
				new AlertDialog.Builder(this)
						.setMessage("Please upgrade your plan to access more.")
						.setPositiveButton("Upgrade", (dialog, which) -> startActivity(intent))
						.setNegativeButton("Cancel", null)
						.show();
			} else {
				startActivity(new Intent(this, TasksActivity.class));
			}
		} else if (id == R.id.history) {
			if (getSharedPreferences(USER_PREFS, 0).getInt(KEY_STORAGE_CYCLE, 0) == 0) {
				new AlertDialog.Builder(this)
						.setMessage("Please upgrade your plan to access more.")
						.setPositiveButton("Upgrade", (dialog, which) -> startActivity(intent))
						.setNegativeButton("Cancel", null)
						.show();
			} else {
				startActivity(new Intent(this, HistoryActivity.class));
			}
		} else if (id == R.id.feedback) {
			startActivity(new Intent(this, FeedbackActivity.class));
		} else if (id == R.id.rate_us) {
			Intent i = new Intent(Intent.ACTION_VIEW);
			i.setData(Uri.parse("market://details?id=com.aaptrix.savitri"));
			startActivity(i);
		} else if (id == R.id.share_us) {
			String msg = "Install Savitri: Your compliance tracking assistant, and never miss any compliance renewals." + " " + "\nhttp://play.google.com/store/apps/details?id=com.aaptrix.savitri";
			ShareCompat.IntentBuilder.from(this)
					.setType("text/plain")
					.setChooserTitle("Share via...")
					.setText(msg)
					.startChooser();
		} else if (id == R.id.app_info) {
			startActivity(new Intent(this, AppInfo.class));
		} else if (id == R.id.logout) {
			new AlertDialog.Builder(this)
					.setMessage("Are you sure you want to logout?")
					.setPositiveButton("Yes", (dialog, which) -> {
						SharedPrefsManager.getInstance(this).logout();
						Intent intent = new Intent(this, AppLogin.class);
						intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
						startActivity(intent);
						finish();
					})
					.setNegativeButton("No", null)
					.show();
		} else if (id == R.id.pay_history) {
			startActivity(new Intent(this, PaymentHistory.class));
		} else if (id == R.id.about_savitri) {
			startActivity(new Intent(this, AboutSavitri.class));
		}
		drawer.closeDrawer(GravityCompat.START);
		return false;
	}

	private boolean checkConnection() {
		ConnectivityManager connec;
		connec = (ConnectivityManager) getSystemService(CONNECTIVITY_SERVICE);
		assert connec != null;
		return connec.getActiveNetworkInfo() != null && connec.getActiveNetworkInfo().isAvailable() && connec.getActiveNetworkInfo().isConnectedOrConnecting();
	}
}
