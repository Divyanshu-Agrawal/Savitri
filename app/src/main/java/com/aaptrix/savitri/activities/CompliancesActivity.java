package com.aaptrix.savitri.activities;

import com.aaptrix.savitri.R;
import com.aaptrix.savitri.adapter.ComplianceAdapter;
import com.aaptrix.savitri.databeans.ComplianceData;
import com.aaptrix.savitri.session.SharedPrefsManager;
import com.aaptrix.savitri.session.URLs;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import cz.msebera.android.httpclient.HttpEntity;
import cz.msebera.android.httpclient.HttpResponse;
import cz.msebera.android.httpclient.client.HttpClient;
import cz.msebera.android.httpclient.client.methods.HttpPost;
import cz.msebera.android.httpclient.entity.mime.HttpMultipartMode;
import cz.msebera.android.httpclient.entity.mime.MultipartEntityBuilder;
import cz.msebera.android.httpclient.impl.client.DefaultHttpClient;
import cz.msebera.android.httpclient.util.EntityUtils;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

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
import java.util.Objects;

import static com.aaptrix.savitri.session.SharedPrefsNames.KEY_COMPLIANCE_COUNT;
import static com.aaptrix.savitri.session.SharedPrefsNames.KEY_ORG_ID;
import static com.aaptrix.savitri.session.SharedPrefsNames.KEY_SESSION_ID;
import static com.aaptrix.savitri.session.SharedPrefsNames.KEY_USER_ROLE;
import static com.aaptrix.savitri.session.SharedPrefsNames.USER_PREFS;
import static java.security.AccessController.getContext;

public class CompliancesActivity extends AppCompatActivity {
	
	Toolbar toolbar;
	ListView listView;
	FloatingActionButton addCompliance;
	ComplianceAdapter adapter;
	ArrayList<ComplianceData> complianceArray = new ArrayList<>();
	TextView noCompliance;
	ProgressBar progressBar;
	String strUserType, strOrgId, strSessionId;
	int complianceCount;
	SwipeRefreshLayout swipeRefreshLayout;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_compliances);
		toolbar = findViewById(R.id.toolbar);
		setSupportActionBar(toolbar);
		Objects.requireNonNull(getSupportActionBar()).setDisplayShowHomeEnabled(true);
		getSupportActionBar().setDisplayHomeAsUpEnabled(true);
		
		listView = findViewById(R.id.compliance_listview);
		addCompliance = findViewById(R.id.add_compliance);
		noCompliance = findViewById(R.id.no_compliance);
		progressBar = findViewById(R.id.progress_bar);
		swipeRefreshLayout = findViewById(R.id.swipe_refresh);
		
		SharedPreferences sp = getSharedPreferences(USER_PREFS, Context.MODE_PRIVATE);
		strUserType = sp.getString(KEY_USER_ROLE, "");
		complianceCount = sp.getInt(KEY_COMPLIANCE_COUNT, 0);
		strOrgId = sp.getString(KEY_ORG_ID, "");
		strSessionId = sp.getString(KEY_SESSION_ID, "");
		progressBar.setVisibility(View.VISIBLE);
		setCompliance();
		
		swipeRefreshLayout.setOnRefreshListener(() -> {
			complianceArray.clear();
			swipeRefreshLayout.setRefreshing(true);
			listView.setEnabled(false);
			setCompliance();
		});
		
		if (strUserType.equals("Admin")) {
			addCompliance.setVisibility(View.VISIBLE);
		} else {
			addCompliance.setVisibility(View.GONE);
		}
		
		addCompliance.setOnClickListener(v -> {
			if (complianceArray.size() < complianceCount) {
				startActivity(new Intent(this, AddNewCompliance.class));
			} else {
				new AlertDialog.Builder(this)
						.setMessage("Please upgrade your plan to add more compliances.")
						.setPositiveButton("Upgrade", (dialog, which) -> startActivity(new Intent(this, PlansActivity.class)))
						.setNegativeButton("Close", null)
						.show();
			}
		});
	}
	
	private void setCompliance() {
		if (checkConnection()) {
			noCompliance.setVisibility(View.GONE);
			fetchCompliance();
		} else {
			Toast.makeText(this, "Please connect to internet for better experience", Toast.LENGTH_SHORT).show();
			try {
				FileNotFoundException fe = new FileNotFoundException();
				File directory = this.getFilesDir();
				ObjectInputStream in = new ObjectInputStream(new FileInputStream(new File(directory, "complianceData")));
				String json = in.readObject().toString();
				in.close();
				JSONObject jsonObject = new JSONObject(json);
				JSONArray jsonArray = jsonObject.getJSONArray("complianceList");
				for (int i = 0; i < jsonArray.length(); i++) {
					JSONObject jObject = jsonArray.getJSONObject(i);
					ComplianceData data = new ComplianceData();
					data.setId(jObject.getString("compliance_id"));
					data.setName(jObject.getString("compliance_name"));
					data.setIssueAuth(jObject.getString("compliance_issuing_auth"));
					data.setAddedDate(jObject.getString("compliance_added_date"));
					data.setCertificate(jObject.getString("compliance_certificates"));
					data.setNotes(jObject.getString("compliance_notes"));
					data.setOtherAuth(jObject.getString("compliance_issuing_auth_other"));
					data.setRefNo(jObject.getString("compliance_reference_no"));
					data.setValidfrom(jObject.getString("compliance_valid_from"));
					data.setValidTo(jObject.getString("compliance_valid_upto"));
					complianceArray.add(data);
				}
				throw fe;
			} catch (Exception e) {
				e.printStackTrace();
			}
			if (complianceArray.size() == 0) {
				noCompliance.setVisibility(View.VISIBLE);
				progressBar.setVisibility(View.GONE);
			} else {
				listItem();
			}
		}
	}
	
	private void fetchCompliance() {
		new Thread(() -> {
			try {
				HttpClient httpclient = new DefaultHttpClient();
				HttpPost httppost = new HttpPost(URLs.ALL_COMPLIANCE);
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
					try {
						Log.e("res", result);
						if (result.contains("\"success\":false,\"msg\":\"Session Expire\"")){
							progressBar.setVisibility(View.GONE);
							Toast.makeText(this, "Your Session is expired please login again", Toast.LENGTH_SHORT).show();
							SharedPrefsManager.getInstance(this).logout();
							Intent intent = new Intent(this, AppLogin.class);
							intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
							startActivity(intent);
							finish();
						} else if (result.equals("{\"complianceList\":null}")) {
							noCompliance.setVisibility(View.VISIBLE);
							progressBar.setVisibility(View.GONE);
						} else {
							JSONObject jsonObject = new JSONObject(result);
							cacheJson(jsonObject);
							JSONArray jsonArray = jsonObject.getJSONArray("complianceList");
							for (int i = 0; i < jsonArray.length(); i++) {
								JSONObject jObject = jsonArray.getJSONObject(i);
								ComplianceData data = new ComplianceData();
								data.setId(jObject.getString("compliance_id"));
								data.setName(jObject.getString("compliance_name"));
								data.setIssueAuth(jObject.getString("compliance_issuing_auth"));
								data.setAddedDate(jObject.getString("compliance_added_date"));
								data.setCertificate(jObject.getString("compliance_certificates"));
								data.setNotes(jObject.getString("compliance_notes"));
								data.setOtherAuth(jObject.getString("compliance_issuing_auth_other"));
								data.setRefNo(jObject.getString("compliance_reference_no"));
								data.setValidfrom(jObject.getString("compliance_valid_from"));
								data.setValidTo(jObject.getString("compliance_valid_upto"));
								complianceArray.add(data);
							}
						}
					} catch (JSONException e) {
						e.printStackTrace();
						noCompliance.setVisibility(View.VISIBLE);
						progressBar.setVisibility(View.GONE);
					}
					listItem();
				});
			} catch (IOException e) {
				e.printStackTrace();
			}
		}).start();
	}
	
	private void listItem() {
		progressBar.setVisibility(View.GONE);
		swipeRefreshLayout.setRefreshing(false);
		adapter = new ComplianceAdapter(this, R.layout.list_compliances, complianceArray);
		listView.setAdapter(adapter);
		listView.setEnabled(true);
		adapter.notifyDataSetChanged();
	}
	
	private void cacheJson(final JSONObject jsonObject) {
		new Thread(() -> {
			ObjectOutput out;
			String data = jsonObject.toString();
			try {
				if (getContext() != null) {
					File directory = this.getFilesDir();
					directory.mkdir();
					out = new ObjectOutputStream(new FileOutputStream(new File(directory, "complianceData")));
					out.writeObject(data);
					out.close();
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}).start();
	}
	
	public boolean checkConnection() {
		ConnectivityManager connec;
		connec = (ConnectivityManager)getSystemService(CONNECTIVITY_SERVICE);
		assert connec != null;
		return connec.getActiveNetworkInfo() != null && connec.getActiveNetworkInfo().isAvailable() && connec.getActiveNetworkInfo().isConnectedOrConnecting();
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
}
