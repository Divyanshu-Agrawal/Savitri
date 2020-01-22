package com.aaptrix.savitri.activities;

import com.aaptrix.savitri.R;
import com.aaptrix.savitri.session.FormatDate;
import com.aaptrix.savitri.session.SharedPrefsManager;
import com.aaptrix.savitri.session.URLs;
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

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.button.MaterialButton;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Objects;

import static com.aaptrix.savitri.session.SharedPrefsNames.KEY_ORG_ID;
import static com.aaptrix.savitri.session.SharedPrefsNames.KEY_SESSION_ID;
import static com.aaptrix.savitri.session.SharedPrefsNames.KEY_USER_ID;
import static com.aaptrix.savitri.session.SharedPrefsNames.USER_PREFS;

public class UpdateCompliance extends AppCompatActivity {
	
	EditText complianceName, complianceRefNo, complianceNotes, validFrom, validTo, otherIssueAuth;
	Spinner issuingAuth;
	String strIssueAuth, strOrgId, strUserId, strSessionId;
	Toolbar toolbar;
	RelativeLayout progressBar;
	MaterialButton updateCompliance;
	ArrayList<String> issueAuth = new ArrayList<>();
	ArrayAdapter<String> issueAuthAdapter;
	String strName, strNotes, strValidFrom, strValidTo, strRefno, strId;
	View v;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_update_compliance);
		toolbar = findViewById(R.id.toolbar);
		setSupportActionBar(toolbar);
		Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
		getSupportActionBar().setDisplayShowHomeEnabled(true);
		
		progressBar = findViewById(R.id.progress_bar);
		progressBar.bringToFront();
		
		complianceName = findViewById(R.id.update_compliance_name);
		complianceRefNo = findViewById(R.id.update_compliance_ref_no);
		complianceNotes = findViewById(R.id.update_compliance_note);
		validFrom = findViewById(R.id.update_compliance_valid_from);
		validTo = findViewById(R.id.update_compliance_valid_upto);
		issuingAuth = findViewById(R.id.update_compliance_issue_auth);
		updateCompliance = findViewById(R.id.update_compliance_btn);
		otherIssueAuth = findViewById(R.id.update_compliance_other_auth);

		progressBar.setOnClickListener(v1 -> {});
		progressBar.setOnTouchListener((v1, event) -> false);

		complianceName.setOnFocusChangeListener((v, hasFocus) -> {
			if (!hasFocus)
				hideKeyboard(v);
		});
		complianceRefNo.setOnFocusChangeListener((v, hasFocus) -> {
			if (!hasFocus)
				hideKeyboard(v);
		});
		complianceNotes.setOnFocusChangeListener((v, hasFocus) -> {
			if (!hasFocus)
				hideKeyboard(v);
		});
		validTo.setOnFocusChangeListener((v, hasFocus) -> {
			if (!hasFocus)
				hideKeyboard(v);
		});
		validFrom.setOnFocusChangeListener((v, hasFocus) -> {
			if (!hasFocus)
				hideKeyboard(v);
		});
		issuingAuth.setOnFocusChangeListener((v, hasFocus) -> {
			if (!hasFocus)
				hideKeyboard(v);
		});
		otherIssueAuth.setOnFocusChangeListener((v, hasFocus) -> {
			if (!hasFocus)
				hideKeyboard(v);
		});
		
		strName = getIntent().getStringExtra("name");
		strNotes = getIntent().getStringExtra("notes");
		strIssueAuth = getIntent().getStringExtra("issueAuth");
		strId = getIntent().getStringExtra("id");
		strValidFrom = getIntent().getStringExtra("validFrom");
		strValidTo = getIntent().getStringExtra("validTo");
		strRefno = getIntent().getStringExtra("refNo");
		
		complianceName.setText(strName);
		complianceNotes.setText(strNotes);
		otherIssueAuth.setText(strIssueAuth);
		FormatDate date = new FormatDate(strValidFrom, "yyyy-MM-dd", "dd-MM-yyyy");
		validFrom.setText(date.format());
		date = new FormatDate(strValidTo, "yyyy-MM-dd", "dd-MM-yyyy");
		validTo.setText(date.format());
		complianceRefNo.setText(strRefno);
		
		strOrgId = getSharedPreferences(USER_PREFS, Context.MODE_PRIVATE).getString(KEY_ORG_ID, "");
		strUserId = getSharedPreferences(USER_PREFS, Context.MODE_PRIVATE).getString(KEY_USER_ID, "");
		strSessionId = getSharedPreferences(USER_PREFS, Context.MODE_PRIVATE).getString(KEY_SESSION_ID, "");
		issueAuth.add("Select Issuing Authority");
		issueAuth.add("Other");
		issueAuthAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, issueAuth);
		issueAuthAdapter.notifyDataSetChanged();
		issueAuthAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		issuingAuth.setAdapter(issueAuthAdapter);
		fetchAuth();
		
		issuingAuth.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
			@Override
			public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
				if (view != null) {
					v = view;
					if (position == 0)
						((TextView) view).setTextColor(getResources().getColor(R.color.hintcolor));
					else
						((TextView) view).setTextColor(getResources().getColor(R.color.colorPrimaryDark));
				} else {
					if (position == 0)
						((TextView) v).setTextColor(getResources().getColor(R.color.hintcolor));
					else
						((TextView) v).setTextColor(getResources().getColor(R.color.colorPrimaryDark));
				}
				if (position == 0) {
					otherIssueAuth.setVisibility(View.GONE);
					strIssueAuth = "";
				} else if (issueAuth.get(position).equals("Other")) {
					otherIssueAuth.setVisibility(View.VISIBLE);
					strIssueAuth = issueAuth.get(position);
				} else {
					otherIssueAuth.setVisibility(View.GONE);
					strIssueAuth = issueAuth.get(position);
				}
			}
			
			@Override
			public void onNothingSelected(AdapterView<?> parent) {
			
			}
		});
		
		updateCompliance.setOnClickListener(v -> {
			if (TextUtils.isEmpty(complianceName.getText().toString())) {
				complianceName.setError("Please Enter Compliance Name");
				complianceName.requestFocus();
			} else if (TextUtils.isEmpty(complianceRefNo.getText().toString())) {
				complianceRefNo.setError("Please Enter Compliance Reference Number");
				complianceRefNo.requestFocus();
			} else if (TextUtils.isEmpty(strIssueAuth)) {
				Toast.makeText(this, "Please Select Issuing Authority", Toast.LENGTH_SHORT).show();
			} else if (strIssueAuth.equals("Other")) {
				if (TextUtils.isEmpty(otherIssueAuth.getText().toString())) {
					otherIssueAuth.setError("Please Enter Issuing Authority");
					otherIssueAuth.requestFocus();
				} else {
					updateCompliance.setEnabled(false);
					UpdateComp uploadCompliance = new UpdateComp(this);
					uploadCompliance.execute(strOrgId, complianceName.getText().toString(),
							complianceRefNo.getText().toString(), strIssueAuth,
							complianceNotes.getText().toString(), strUserId, otherIssueAuth.getText().toString());
				}
			} else {
				updateCompliance.setEnabled(false);
				UpdateComp uploadCompliance = new UpdateComp(this);
				uploadCompliance.execute(strOrgId, complianceName.getText().toString(),
						complianceRefNo.getText().toString(), strIssueAuth,
						complianceNotes.getText().toString(), strUserId, "");
			}
		});
	}

	private void hideKeyboard(View view) {
		InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(Activity.INPUT_METHOD_SERVICE);
		assert inputMethodManager != null;
		inputMethodManager.hideSoftInputFromWindow(view.getWindowToken(), 0);
	}
	
	private void fetchAuth() {
		new Thread(() -> {
			try {
				HttpClient httpclient = new DefaultHttpClient();
				HttpPost httppost = new HttpPost(URLs.ALL_ISSUE_AUTH);
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
						if (!result.contains("null")) {
							issueAuth.clear();
							issueAuth.add("Select Issuing Authority");
							Log.e("result", result);
							JSONObject jsonObject = new JSONObject(result);
							JSONArray jsonArray = jsonObject.getJSONArray("issuingAuthority");
							for (int i = 0; i < jsonArray.length(); i++) {
								JSONObject jObject = jsonArray.getJSONObject(i);
								issueAuth.add(jObject.getString("issuing_authority_name"));
							}
							issueAuth.add("Other");
							issueAuthAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, issueAuth);
							issueAuthAdapter.notifyDataSetChanged();
							issueAuthAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
							issuingAuth.setAdapter(issueAuthAdapter);
							
							for (int i = 0; i < issueAuth.size(); i++) {
								if (issueAuth.get(i).equals(strIssueAuth)) {
									issuingAuth.setSelection(i);
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
	public boolean onOptionsItemSelected(MenuItem item) {
		if (item.getItemId() == android.R.id.home) {
			onBackPressed();
		}
		return super.onOptionsItemSelected(item);
	}
	
	@Override
	public void onBackPressed() {
		super.onBackPressed();
	}
	
	@SuppressLint("StaticFieldLeak")
	class UpdateComp extends AsyncTask<String, String, String> {
		
		@SuppressLint("StaticFieldLeak")
		private Context context;
		
		UpdateComp(Context context) {
			this.context = context;
		}
		
		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			progressBar.setVisibility(View.VISIBLE);
			progressBar.bringToFront();
		}
		
		
		@Override
		protected String doInBackground(String... params) {
			
			String orgId = params[0];
			String name = params[1];
			String refNo = params[2];
			String issueAuth = params[3];
			String notes = params[4];
			String userId = params[5];
			String otherAuth = params[6];
			
			
			
			try {
				HttpClient httpclient = new DefaultHttpClient();
				HttpPost httppost = new HttpPost(URLs.UPDATE_COMPLIANCE);
				MultipartEntityBuilder entityBuilder = MultipartEntityBuilder.create();
				entityBuilder.setMode(HttpMultipartMode.BROWSER_COMPATIBLE);
				entityBuilder.addTextBody("compliance_id", strId);
				entityBuilder.addTextBody("org_details_id", orgId);
				entityBuilder.addTextBody("compliance_name", name);
				entityBuilder.addTextBody("compliance_reference_no", refNo);
				entityBuilder.addTextBody("compliance_notes", notes);
				entityBuilder.addTextBody("app_session_id", strSessionId);
				if (issueAuth.equals("Other")) {
					entityBuilder.addTextBody("compliance_issuing_auth", issueAuth);
					entityBuilder.addTextBody("compliance_issuing_auth_other", otherAuth);
				} else {
					entityBuilder.addTextBody("compliance_issuing_auth", issueAuth);
				}
				HttpEntity entity = entityBuilder.build();
				httppost.setEntity(entity);
				HttpResponse response = httpclient.execute(httppost);
				HttpEntity httpEntity = response.getEntity();
				String res = EntityUtils.toString(httpEntity);
				Log.e("res", res);
				return res;
			} catch (Exception e) {
				e.printStackTrace();
			}
			return null;
		}
		
		@Override
		protected void onPostExecute(String result) {
			if (result != null) {
				Log.e("result", result);
				try {
					JSONObject jsonObject = new JSONObject(result);
					if (jsonObject.getBoolean("success")) {
						Toast.makeText(context, "Updated Successfully", Toast.LENGTH_SHORT).show();
						startActivity(new Intent(context, CompliancesActivity.class).setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP));
					} else if (result.contains("\"success\":false,\"msg\":\"Session Expire\"")){
						progressBar.setVisibility(View.GONE);
						Toast.makeText(context, "Your Session is expired please login again", Toast.LENGTH_SHORT).show();
						SharedPrefsManager.getInstance(context).logout();
						Intent intent = new Intent(context, AppLogin.class);
						intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
						startActivity(intent);
						finish();
					} else {
						progressBar.setVisibility(View.GONE);
						Toast.makeText(context, "Error Occured. Please try again", Toast.LENGTH_SHORT).show();
					}
				} catch (JSONException e) {
					e.printStackTrace();
				}
			}
			super.onPostExecute(result);
		}
	}
}
