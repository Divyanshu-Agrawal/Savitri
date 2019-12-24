package com.aaptrix.savitri.activities;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.DatePickerDialog;
import android.content.ClipData;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.button.MaterialButton;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Locale;
import java.util.Objects;

import com.aaptrix.savitri.R;
import com.aaptrix.savitri.session.FileUtil;
import com.aaptrix.savitri.session.SharedPrefsManager;
import com.aaptrix.savitri.session.URLs;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.PermissionChecker;
import cz.msebera.android.httpclient.HttpEntity;
import cz.msebera.android.httpclient.HttpResponse;
import cz.msebera.android.httpclient.client.HttpClient;
import cz.msebera.android.httpclient.client.methods.HttpPost;
import cz.msebera.android.httpclient.entity.mime.HttpMultipartMode;
import cz.msebera.android.httpclient.entity.mime.MultipartEntityBuilder;
import cz.msebera.android.httpclient.entity.mime.content.FileBody;
import cz.msebera.android.httpclient.impl.client.DefaultHttpClient;
import cz.msebera.android.httpclient.util.EntityUtils;

import static com.aaptrix.savitri.session.SharedPrefsNames.KEY_ORG_ID;
import static com.aaptrix.savitri.session.SharedPrefsNames.KEY_SESSION_ID;
import static com.aaptrix.savitri.session.SharedPrefsNames.KEY_USER_ID;
import static com.aaptrix.savitri.session.SharedPrefsNames.USER_PREFS;

public class AddNewCompliance extends AppCompatActivity {
	
	EditText complianceName, complianceRefNo, complianceNotes, validFrom, validTo, otherIssueAuth;
	Spinner issuingAuth;
	String strIssueAuth, strValidFrom = "", strValidUpto = "", strOrgId, strUserId;
	Toolbar toolbar;
	ProgressBar progressBar;
	MaterialButton addCompliance;
	Button uploadCertificate;
	TextView certCount;
	ArrayList<String> issueAuth = new ArrayList<>();
	ArrayList<File> filepath = new ArrayList<>();
	Calendar myCalendar = Calendar.getInstance();
	DatePickerDialog.OnDateSetListener validFromDate, validUptoDate;
	ArrayAdapter<String> issueAuthAdapter;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_add_new_compliance);
		toolbar = findViewById(R.id.toolbar);
		setSupportActionBar(toolbar);
		Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
		getSupportActionBar().setDisplayShowHomeEnabled(true);
		
		progressBar = findViewById(R.id.progress_bar);
		progressBar.bringToFront();
		
		complianceName = findViewById(R.id.add_compliance_name);
		complianceRefNo = findViewById(R.id.add_compliance_ref_no);
		complianceNotes = findViewById(R.id.add_compliance_note);
		validFrom = findViewById(R.id.add_compliance_valid_from);
		validTo = findViewById(R.id.add_compliance_valid_upto);
		issuingAuth = findViewById(R.id.add_compliance_issue_auth);
		addCompliance = findViewById(R.id.add_compliance_btn);
		uploadCertificate = findViewById(R.id.add_compliance_certificate);
		certCount = findViewById(R.id.certificate_count);
		otherIssueAuth = findViewById(R.id.add_compliance_other_auth);
		strOrgId = getSharedPreferences(USER_PREFS, Context.MODE_PRIVATE).getString(KEY_ORG_ID, "");
		strUserId = getSharedPreferences(USER_PREFS, Context.MODE_PRIVATE).getString(KEY_USER_ID, "");
		issueAuth.add("Select Issuing Authority");
		issueAuth.add("Other");
		issueAuthAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, issueAuth);
		issueAuthAdapter.notifyDataSetChanged();
		issueAuthAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		issuingAuth.setAdapter(issueAuthAdapter);
		fetchAuth();
		
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
		
		issuingAuth.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
			@Override
			public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
				((TextView) parent.getChildAt(0)).setTextColor(getResources().getColor(R.color.hintcolor));
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
		
		uploadCertificate.setOnClickListener(v -> {
			if (PermissionChecker.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
				Intent intent = new Intent();
				intent.setType("*/*");
				intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
				intent.setAction(Intent.ACTION_GET_CONTENT);
				startActivityForResult(intent, 1);
			} else {
				isPermissionGranted();
			}
		});
		
		validFromDate = (view, year, monthOfYear, dayOfMonth) -> {
			myCalendar.set(Calendar.YEAR, year);
			myCalendar.set(Calendar.MONTH, monthOfYear);
			myCalendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
			String myFormat = "dd-MM-yyyy"; //In which you need put here
			SimpleDateFormat sdf = new SimpleDateFormat(myFormat, Locale.US);
			validFrom.setText(sdf.format(myCalendar.getTime()));
			myFormat = "yyyy-MM-dd";
			sdf = new SimpleDateFormat(myFormat, Locale.getDefault());
			strValidFrom = sdf.format(myCalendar.getTime());
		};
		
		validFrom.setOnClickListener(v -> {
			DatePickerDialog datePickerDialog = new DatePickerDialog(this, validFromDate, myCalendar
					.get(Calendar.YEAR), myCalendar.get(Calendar.MONTH),
					myCalendar.get(Calendar.DAY_OF_MONTH));
			datePickerDialog.show();
		});
		
		validUptoDate = (view, year, monthOfYear, dayOfMonth) -> {
			myCalendar.set(Calendar.YEAR, year);
			myCalendar.set(Calendar.MONTH, monthOfYear);
			myCalendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
			String myFormat = "dd-MM-yyyy"; //In which you need put here
			SimpleDateFormat sdf = new SimpleDateFormat(myFormat, Locale.US);
			validTo.setText(sdf.format(myCalendar.getTime()));
			myFormat = "yyyy-MM-dd";
			sdf = new SimpleDateFormat(myFormat, Locale.getDefault());
			strValidUpto = sdf.format(myCalendar.getTime());
		};
		
		validTo.setOnClickListener(v -> {
			DatePickerDialog datePickerDialog = new DatePickerDialog(this, validUptoDate, myCalendar
					.get(Calendar.YEAR), myCalendar.get(Calendar.MONTH),
					myCalendar.get(Calendar.DAY_OF_MONTH));
			try {
				datePickerDialog.getDatePicker().setMinDate(new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).parse(strValidFrom).getTime() + 1000);
			} catch (ParseException e) {
				e.printStackTrace();
			}
			datePickerDialog.show();
		});
		
		addCompliance.setOnClickListener(v -> {
			if (TextUtils.isEmpty(complianceName.getText().toString())) {
				complianceName.setError("Please Enter Compliance Name");
				complianceName.requestFocus();
			} else if (TextUtils.isEmpty(complianceRefNo.getText().toString())) {
				complianceRefNo.setError("Please Enter Compliance Reference Number");
				complianceRefNo.requestFocus();
			} else if (TextUtils.isEmpty(strIssueAuth)) {
				Toast.makeText(this, "Please Select Issuing Authority", Toast.LENGTH_SHORT).show();
			} else if (TextUtils.isEmpty(strValidFrom)) {
				validFrom.setError("Please Select Date");
				validFrom.requestFocus();
			} else if (TextUtils.isEmpty(strValidUpto)) {
				validTo.setError("Please Select Date");
				validTo.requestFocus();
			} else if (filepath.size() == 0) {
				Toast.makeText(this, "Please Upload Certificate", Toast.LENGTH_SHORT).show();
			} else if (strIssueAuth.equals("Other")) {
				if (TextUtils.isEmpty(otherIssueAuth.getText().toString())) {
					otherIssueAuth.setError("Please Enter Issuing Authority");
					otherIssueAuth.requestFocus();
				} else {
					UploadCompliance uploadCompliance = new UploadCompliance(this);
					uploadCompliance.execute(strOrgId, complianceName.getText().toString(),
							complianceRefNo.getText().toString(), strIssueAuth,
							complianceNotes.getText().toString(), strValidFrom, strValidUpto, strUserId, otherIssueAuth.getText().toString());
				}
			} else {
				UploadCompliance uploadCompliance = new UploadCompliance(this);
				uploadCompliance.execute(strOrgId, complianceName.getText().toString(),
						complianceRefNo.getText().toString(), strIssueAuth,
						complianceNotes.getText().toString(), strValidFrom, strValidUpto, strUserId, "");
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
	
	@SuppressLint("SetTextI18n")
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if (requestCode == 1) {
			if (resultCode == Activity.RESULT_OK) {
				ClipData clipData = data.getClipData();
				if (clipData != null) {
					if (filepath.size() <= 5) {
						for (int i = 0; i < clipData.getItemCount(); i++) {
							try {
								if (FileUtil.from(this, clipData.getItemAt(i).getUri()).length() / 1024 <= 200) {
									filepath.add(FileUtil.from(this, clipData.getItemAt(i).getUri()));
								} else {
									Toast.makeText(this, "File size cannot be greater than 200KB", Toast.LENGTH_SHORT).show();
									break;
								}
							} catch (IOException e) {
								e.printStackTrace();
							}
						}
						certCount.setText(String.valueOf(filepath.size()) + " Files Selected");
					} else {
						Toast.makeText(this, "You can only select upto 5 certificates", Toast.LENGTH_SHORT).show();
					}
				} else {
					try {
						if (filepath.size() <= 5) {
							if (FileUtil.from(this, data.getData()).length() / 1024 <= 200) {
								filepath.add(FileUtil.from(this, data.getData()));
								certCount.setText(String.valueOf(filepath.size()) + " Files Selected");
							} else {
								Toast.makeText(this, "File size cannot be greater than 200KB", Toast.LENGTH_SHORT).show();
							}
						} else {
							Toast.makeText(this, "You can only select upto 5 certificates", Toast.LENGTH_SHORT).show();
						}
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			}
		}
	}
	
	public void isPermissionGranted() {
		ActivityCompat.requestPermissions(this,
				new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 1);
	}
	
	
	@Override
	public void onRequestPermissionsResult(int requestCode,
										   @NonNull String permissions[], @NonNull int[] grantResults) {
		switch (requestCode) {
			case 1: {
				if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
					Toast.makeText(this, "Permission granted", Toast.LENGTH_SHORT).show();
				} else {
					Toast.makeText(this, "Permission denied", Toast.LENGTH_SHORT).show();
				}
				
			}
		}
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
	class UploadCompliance extends AsyncTask<String, String, String> {
		
		@SuppressLint("StaticFieldLeak")
		private Context context;
		
		UploadCompliance(Context context) {
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
			String validFrom = params[5];
			String validTo = params[6];
			String userId = params[7];
			String otherAuth = params[8];
			
			try {
				ArrayList<String> fileNames = new ArrayList<>();
				for (int i = 0; i < filepath.size(); i++) {
					try {
						HttpClient httpclient = new DefaultHttpClient();
						HttpPost httppost = new HttpPost(URLs.ADD_COMPLIANCE);
						MultipartEntityBuilder entityBuilder = MultipartEntityBuilder.create();
						entityBuilder.setMode(HttpMultipartMode.BROWSER_COMPATIBLE);
						FileBody image = new FileBody(filepath.get(i));
						entityBuilder.addPart("image", image);
						entityBuilder.addTextBody("org_details_id", orgId);
						entityBuilder.addTextBody("app_session_id", getSharedPreferences(USER_PREFS, Context.MODE_PRIVATE).getString(KEY_SESSION_ID, ""));
						HttpEntity entity = entityBuilder.build();
						httppost.setEntity(entity);
						HttpResponse response = httpclient.execute(httppost);
						HttpEntity httpEntity = response.getEntity();
						String result = EntityUtils.toString(httpEntity);
						if (result.contains("\"success\":false,\"msg\":\"Session Expire\"")) {
							Toast.makeText(context, "Your Session is expired please login again", Toast.LENGTH_SHORT).show();
							SharedPrefsManager.getInstance(context).logout();
							Intent intent = new Intent(context, AppLogin.class);
							intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
							startActivity(intent);
							finish();
						} else {
							JSONObject jsonObject = new JSONObject(result);
							fileNames.add("\"" + jsonObject.getString("imageNm") + "\"");
						}
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
				try {
					HttpClient httpclient = new DefaultHttpClient();
					HttpPost httppost = new HttpPost(URLs.ADD_COMPLIANCE);
					MultipartEntityBuilder entityBuilder = MultipartEntityBuilder.create();
					entityBuilder.setMode(HttpMultipartMode.BROWSER_COMPATIBLE);
					entityBuilder.addTextBody("compliance_certificates", fileNames.toString());
					entityBuilder.addTextBody("org_details_id", orgId);
					entityBuilder.addTextBody("compliance_name", name);
					entityBuilder.addTextBody("compliance_reference_no", refNo);
					entityBuilder.addTextBody("compliance_notes", notes);
					entityBuilder.addTextBody("users_details_id", userId);
					entityBuilder.addTextBody("app_session_id", getSharedPreferences(USER_PREFS, Context.MODE_PRIVATE).getString(KEY_SESSION_ID, ""));
					if (issueAuth.equals("Other")) {
						entityBuilder.addTextBody("compliance_issuing_auth", issueAuth);
						entityBuilder.addTextBody("compliance_issuing_auth_other", otherAuth);
					} else {
						entityBuilder.addTextBody("compliance_issuing_auth", issueAuth);
					}
					entityBuilder.addTextBody("compliance_valid_from", validFrom);
					entityBuilder.addTextBody("compliance_valid_upto", validTo);
					HttpEntity entity = entityBuilder.build();
					httppost.setEntity(entity);
					HttpResponse response = httpclient.execute(httppost);
					HttpEntity httpEntity = response.getEntity();
					String res = EntityUtils.toString(httpEntity);
					return res;
				} catch (Exception e) {
					e.printStackTrace();
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
			return null;
		}
		
		@Override
		protected void onPostExecute(String result) {
			if (result != null) {
				try {
					JSONObject jsonObject = new JSONObject(result);
					if (jsonObject.getBoolean("success")) {
						Toast.makeText(context, "Added Successfully", Toast.LENGTH_SHORT).show();
						startActivity(new Intent(context, CompliancesActivity.class).setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP));
					} else if (result.contains("\"success\":false,\"msg\":\"Session Expire\"")) {
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
