package com.aaptrix.savitri.activities;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
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

import com.aaptrix.savitri.R;
import com.aaptrix.savitri.adapter.PeopleAdapter;
import com.aaptrix.savitri.databeans.PeopleData;
import com.aaptrix.savitri.dialogs.AddMembersDialog;
import com.aaptrix.savitri.session.SharedPrefsManager;
import com.aaptrix.savitri.session.URLs;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import cz.msebera.android.httpclient.HttpEntity;
import cz.msebera.android.httpclient.HttpResponse;
import cz.msebera.android.httpclient.client.HttpClient;
import cz.msebera.android.httpclient.client.methods.HttpPost;
import cz.msebera.android.httpclient.entity.mime.HttpMultipartMode;
import cz.msebera.android.httpclient.entity.mime.MultipartEntityBuilder;
import cz.msebera.android.httpclient.impl.client.DefaultHttpClient;
import cz.msebera.android.httpclient.util.EntityUtils;

import static com.aaptrix.savitri.session.SharedPrefsNames.KEY_MEMBER_COUNT;
import static com.aaptrix.savitri.session.SharedPrefsNames.KEY_ORG_ID;
import static com.aaptrix.savitri.session.SharedPrefsNames.KEY_SESSION_ID;
import static com.aaptrix.savitri.session.SharedPrefsNames.KEY_USER_ID;
import static com.aaptrix.savitri.session.SharedPrefsNames.KEY_USER_ROLE;
import static com.aaptrix.savitri.session.SharedPrefsNames.USER_PREFS;
import static java.security.AccessController.getContext;

public class PeopleActivity extends AppCompatActivity {
	
	Toolbar toolbar;
	ListView listView;
	FloatingActionButton addPeople;
	TextView noPeople;
	ProgressBar progressBar;
	PeopleAdapter adapter;
	ArrayList<PeopleData> peopleArray = new ArrayList<>();
	String strOrgId, strSessionId, strUserId;
	SharedPreferences sp;
	int memberCount;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_people);
		toolbar = findViewById(R.id.toolbar);
		setSupportActionBar(toolbar);
		Objects.requireNonNull(getSupportActionBar()).setDisplayShowHomeEnabled(true);
		getSupportActionBar().setDisplayHomeAsUpEnabled(true);
		
		listView = findViewById(R.id.people_listview);
		addPeople = findViewById(R.id.add_people);
		noPeople = findViewById(R.id.no_people);
		progressBar = findViewById(R.id.progress_bar);
		sp = getSharedPreferences(USER_PREFS, Context.MODE_PRIVATE);
		strOrgId = sp.getString(KEY_ORG_ID, "");
		strSessionId = sp.getString(KEY_SESSION_ID, "");
		strUserId = sp.getString(KEY_USER_ID, "");
		memberCount = sp.getInt(KEY_MEMBER_COUNT, 0);
		
		if (sp.getString(KEY_USER_ROLE, "").equals("Admin")) {
			addPeople.setVisibility(View.VISIBLE);
		} else {
			addPeople.setVisibility(View.GONE);
		}
		progressBar.setVisibility(View.VISIBLE);
		
		addPeople.setOnClickListener(v -> {
			if (peopleArray.size() < memberCount) {
				AddMembersDialog dialog = new AddMembersDialog(this);
				Objects.requireNonNull(dialog.getWindow()).setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
				dialog.show();
			} else {
				new AlertDialog.Builder(this)
						.setMessage("Please upgrade your plan to add more members.")
						.setPositiveButton("Upgrade", (dialog, which) -> startActivity(new Intent(this, PlansActivity.class)))
						.setNegativeButton("Close", null)
						.show();
			}
		});
		
		if (checkConnection()) {
			noPeople.setVisibility(View.GONE);
			fetchPeople();
		} else {
			try {
				FileNotFoundException fe = new FileNotFoundException();
				File directory = this.getFilesDir();
				ObjectInputStream in = new ObjectInputStream(new FileInputStream(new File(directory, "peopleData")));
				String json = in.readObject().toString();
				in.close();
				JSONObject jsonObject = new JSONObject(json);
				JSONArray jsonArray = jsonObject.getJSONArray("allMembers");
				for (int i = 0; i < jsonArray.length(); i++) {
					JSONObject jObject = jsonArray.getJSONObject(i);
					PeopleData data = new PeopleData();
					data.setUsers_details_id(jObject.getString("users_details_id"));
					data.setName(jObject.getString("users_name"));
					data.setImage(jObject.getString("users_profile_img"));
					data.setDesignation(jObject.getString("users_designation"));
					data.setDob(jObject.getString("users_dob"));
					data.setEmail(jObject.getString("users_email"));
					data.setGender(jObject.getString("users_gender"));
					data.setNumber(jObject.getString("users_mobileno"));
					data.setPassword(jObject.getString("users_password"));
					data.setType(jObject.getString("users_type"));
					data.setRegDate(jObject.getString("users_reg_date"));
					peopleArray.add(data);
				}
				throw fe;
			} catch (Exception e) {
				e.printStackTrace();
			}
			if (peopleArray.size() == 0) {
				noPeople.setVisibility(View.VISIBLE);
				progressBar.setVisibility(View.GONE);
			} else {
				listItem();
			}
		}
	}
	
	private void fetchPeople() {
		new Thread(() -> {
			try {
				HttpClient httpclient = new DefaultHttpClient();
				HttpPost httppost = new HttpPost(URLs.ALL_PEOPLE);
				MultipartEntityBuilder entityBuilder = MultipartEntityBuilder.create();
				entityBuilder.setMode(HttpMultipartMode.BROWSER_COMPATIBLE);
				entityBuilder.addTextBody("org_details_id", strOrgId);
				entityBuilder.addTextBody("app_session_id", strSessionId);
				entityBuilder.addTextBody("users_details_id", strUserId);
				HttpEntity entity = entityBuilder.build();
				httppost.setEntity(entity);
				HttpResponse response = httpclient.execute(httppost);
				HttpEntity httpEntity = response.getEntity();
				String result = EntityUtils.toString(httpEntity);
				Handler handler = new Handler(Looper.getMainLooper());
				handler.post(() -> {
					try {
						if (result.contains("\"success\":false,\"msg\":\"Session Expire\"")){
							progressBar.setVisibility(View.GONE);
							Toast.makeText(this, "Your Session is expired please login again", Toast.LENGTH_SHORT).show();
							SharedPrefsManager.getInstance(this).logout();
							Intent intent = new Intent(this, AppLogin.class);
							intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
							startActivity(intent);
							finish();
						} else if (result.contains("{\"allMembers\":null}")) {
							noPeople.setVisibility(View.VISIBLE);
							progressBar.setVisibility(View.GONE);
						} else {
							Log.e("res", result);
							JSONObject jsonObject = new JSONObject(result);
							cacheJson(jsonObject);
							JSONArray jsonArray = jsonObject.getJSONArray("allMembers");
							for (int i = 0; i < jsonArray.length(); i++) {
								JSONObject jObject = jsonArray.getJSONObject(i);
								PeopleData data = new PeopleData();
								data.setUsers_details_id(jObject.getString("users_details_id"));
								data.setName(jObject.getString("users_name"));
								data.setImage(jObject.getString("users_profile_img"));
								data.setDesignation(jObject.getString("users_designation"));
								data.setDob(jObject.getString("users_dob"));
								data.setEmail(jObject.getString("users_email"));
								data.setGender(jObject.getString("users_gender"));
								data.setNumber(jObject.getString("users_mobileno"));
								data.setPassword(jObject.getString("users_password"));
								data.setType(jObject.getString("users_type"));
								data.setRegDate(jObject.getString("users_reg_date"));
								peopleArray.add(data);
							}
						}
					} catch (JSONException e) {
						e.printStackTrace();
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
		adapter = new PeopleAdapter(this, R.layout.list_people, peopleArray);
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
					out = new ObjectOutputStream(new FileOutputStream(new File(directory, "peopleData")));
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
		connec = (ConnectivityManager) getSystemService(CONNECTIVITY_SERVICE);
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
