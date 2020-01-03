package com.aaptrix.savitri.activities;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.DatePickerDialog;
import android.content.ClipData;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.ConnectivityManager;
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

import com.aaptrix.savitri.asyncclass.UploadCompliance;
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
import com.aaptrix.savitri.session.URLs;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;

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
import cz.msebera.android.httpclient.impl.client.DefaultHttpClient;
import cz.msebera.android.httpclient.util.EntityUtils;
import id.zelory.compressor.Compressor;

import static com.aaptrix.savitri.session.SharedPrefsNames.COM_ADDED;
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
import static com.aaptrix.savitri.session.SharedPrefsNames.KEY_ORG_ID;
import static com.aaptrix.savitri.session.SharedPrefsNames.KEY_SESSION_ID;
import static com.aaptrix.savitri.session.SharedPrefsNames.KEY_USER_ID;
import static com.aaptrix.savitri.session.SharedPrefsNames.USER_PREFS;

public class AddNewCompliance extends AppCompatActivity {
	
	EditText complianceName, complianceRefNo, complianceNotes, validFrom, validTo, otherIssueAuth;
	Spinner issuingAuth;
	String strIssueAuth, strValidFrom = "", strValidUpto = "", strOrgId, strUserId, strSessionId;
	Toolbar toolbar;
	ProgressBar progressBar;
	MaterialButton addCompliance;
	Button uploadCertificate;
	TextView certCount;
	ArrayList<String> issueAuth = new ArrayList<>();
	ArrayList<File> filepath = new ArrayList<>();
	Calendar fromCalendar = Calendar.getInstance();
	Calendar uptoCalendar = Calendar.getInstance();
	DatePickerDialog.OnDateSetListener validFromDate, validUptoDate;
	ArrayAdapter<String> issueAuthAdapter;
	View v;
	
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
		SharedPreferences preferences = getSharedPreferences(USER_PREFS, Context.MODE_PRIVATE);
		strOrgId = preferences.getString(KEY_ORG_ID, "");
		strUserId = preferences.getString(KEY_USER_ID, "");
		strSessionId = preferences.getString(KEY_SESSION_ID, "");
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
			fromCalendar.set(Calendar.YEAR, year);
			fromCalendar.set(Calendar.MONTH, monthOfYear);
			fromCalendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
			String myFormat = "dd-MM-yyyy"; //In which you need put here
			SimpleDateFormat sdf = new SimpleDateFormat(myFormat, Locale.US);
			validFrom.setText(sdf.format(fromCalendar.getTime()));
			myFormat = "yyyy-MM-dd";
			sdf = new SimpleDateFormat(myFormat, Locale.getDefault());
			strValidFrom = sdf.format(fromCalendar.getTime());
		};
		
		validFrom.setOnClickListener(v -> {
			DatePickerDialog datePickerDialog = new DatePickerDialog(this, validFromDate, fromCalendar
					.get(Calendar.YEAR), fromCalendar.get(Calendar.MONTH),
					fromCalendar.get(Calendar.DAY_OF_MONTH));
			if (!strValidUpto.isEmpty()) {
				try {
					datePickerDialog.getDatePicker().setMaxDate(new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).parse(strValidUpto).getTime());
				} catch (ParseException e) {
					e.printStackTrace();
				}
			}
			datePickerDialog.show();
		});
		
		validUptoDate = (view, year, monthOfYear, dayOfMonth) -> {
			uptoCalendar.set(Calendar.YEAR, year);
			uptoCalendar.set(Calendar.MONTH, monthOfYear);
			uptoCalendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
			String myFormat = "dd-MM-yyyy"; //In which you need put here
			SimpleDateFormat sdf = new SimpleDateFormat(myFormat, Locale.US);
			validTo.setText(sdf.format(uptoCalendar.getTime()));
			myFormat = "yyyy-MM-dd";
			sdf = new SimpleDateFormat(myFormat, Locale.getDefault());
			strValidUpto = sdf.format(uptoCalendar.getTime());
		};
		
		validTo.setOnClickListener(v -> {
			DatePickerDialog datePickerDialog = new DatePickerDialog(this, validUptoDate, uptoCalendar
					.get(Calendar.YEAR), uptoCalendar.get(Calendar.MONTH),
					uptoCalendar.get(Calendar.DAY_OF_MONTH));
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
					if (checkConnection()) {
						UploadCompliance uploadCompliance = new UploadCompliance(this, progressBar, filepath, "online");
						uploadCompliance.execute(strOrgId, complianceName.getText().toString(),
								complianceRefNo.getText().toString(), strIssueAuth,
								complianceNotes.getText().toString(), strValidFrom, strValidUpto,
								strUserId, otherIssueAuth.getText().toString(), strSessionId);
					} else {
						Toast.makeText(this, "Internet connection not available details will be saved when device is connected to internet", Toast.LENGTH_SHORT).show();
						SharedPreferences sp = getSharedPreferences(COM_PREFS, Context.MODE_PRIVATE);
						SharedPreferences.Editor editor = sp.edit();
						editor.putBoolean(FLAG, true);
						Gson gson = new GsonBuilder().create();
						JsonArray array = gson.toJsonTree(filepath).getAsJsonArray();
						editor.putString(COM_NAME, complianceName.getText().toString());
						editor.putString(COM_CERT, array.toString());
						editor.putString(COM_REF, complianceRefNo.getText().toString());
						editor.putString(COM_AUTH, strIssueAuth);
						editor.putString(COM_NOTES, complianceNotes.getText().toString());
						editor.putString(COM_OTHER_AUTH, otherIssueAuth.getText().toString());
						editor.putString(COM_VALID_FROM, strValidFrom);
						editor.putString(COM_VALID_TO, strValidUpto);
						editor.putString(COM_ADDED, new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
								.format(Calendar.getInstance().getTime()));
						editor.apply();
						startActivity(new Intent(this, CompliancesActivity.class).setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP));
					}
				}
			} else {
				if (checkConnection()) {
					UploadCompliance uploadCompliance = new UploadCompliance(this, progressBar, filepath, "online");
					uploadCompliance.execute(strOrgId, complianceName.getText().toString(),
							complianceRefNo.getText().toString(), strIssueAuth,
							complianceNotes.getText().toString(), strValidFrom, strValidUpto, strUserId, "", strSessionId);
				} else {
					Toast.makeText(this, "Internet connection not available details will be saved when device is connected to internet", Toast.LENGTH_SHORT).show();
					SharedPreferences sp = getSharedPreferences(COM_PREFS, Context.MODE_PRIVATE);
					SharedPreferences.Editor editor = sp.edit();
					editor.putBoolean(FLAG, true);
					Gson gson = new GsonBuilder().create();
					JsonArray array = gson.toJsonTree(filepath).getAsJsonArray();
					editor.putString(COM_NAME, complianceName.getText().toString());
					editor.putString(COM_CERT, array.toString());
					editor.putString(COM_REF, complianceRefNo.getText().toString());
					editor.putString(COM_AUTH, strIssueAuth);
					editor.putString(COM_NOTES, complianceNotes.getText().toString());
					editor.putString(COM_OTHER_AUTH, "");
					editor.putString(COM_VALID_FROM, strValidFrom);
					editor.putString(COM_ADDED, new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
							.format(Calendar.getInstance().getTime()));
					editor.putString(COM_VALID_TO, strValidUpto);
					editor.apply();
					finish();
				}
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
								int length = 0;
								for (int j = 0; j < clipData.getItemCount(); j++) {
									length = length + (int) FileUtil.from(this, clipData.getItemAt(j).getUri()).length();
								}
								for (int j = 0; j < filepath.size(); j++) {
									length = length + (int) filepath.get(j).length();
								}
								String fileExt = clipData.getItemAt(i).getUri().toString().substring(clipData.getItemAt(i).getUri().toString().lastIndexOf(".") + 1);
								if (length <= 1024000) {
									if (fileExt.equals("jpg") || fileExt.equals("jpeg") || fileExt.equals("png")) {
										filepath.add(new Compressor(this)
												.setMaxWidth(1280)
												.setMaxHeight(720)
												.setQuality(75)
												.setCompressFormat(Bitmap.CompressFormat.JPEG)
												.compressToFile(FileUtil.from(this, clipData.getItemAt(i).getUri())));
									} else {
										filepath.add(FileUtil.from(this, clipData.getItemAt(i).getUri()));
									}
								} else {
									Toast.makeText(this, "File size cannot be greater than 200KB", Toast.LENGTH_SHORT).show();
									break;
								}
							} catch (IOException e) {
								e.printStackTrace();
							}
						}
						certCount.setText(filepath.size() + " Files Selected");
					} else {
						Toast.makeText(this, "You can only select upto 5 certificates", Toast.LENGTH_SHORT).show();
					}
				} else {
					try {
						if (filepath.size() <= 5) {
							int length = (int) FileUtil.from(this, data.getData()).length();
							for (int j = 0; j < filepath.size(); j++) {
								length = length + (int) filepath.get(j).length();
							}
							String fileExt = data.getData().toString().substring(data.getData().toString().lastIndexOf(".") + 1);
							if (length <= 1024000) {
								if (fileExt.equals("jpg") || fileExt.equals("jpeg") || fileExt.equals("png")) {
									filepath.add(new Compressor(this)
											.setMaxWidth(1280)
											.setMaxHeight(720)
											.setQuality(75)
											.setCompressFormat(Bitmap.CompressFormat.JPEG)
											.compressToFile(FileUtil.from(this, data.getData())));
									certCount.setText(filepath.size() + " Files Selected");
								} else {
									filepath.add(FileUtil.from(this, data.getData()));
									certCount.setText(filepath.size() + " Files Selected");
								}
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
										   @NonNull String[] permissions, @NonNull int[] grantResults) {
		if (requestCode == 1) {
			if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
				Toast.makeText(this, "Permission granted", Toast.LENGTH_SHORT).show();
			} else {
				Toast.makeText(this, "Permission denied", Toast.LENGTH_SHORT).show();
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


	private boolean checkConnection() {
		ConnectivityManager connec;
		connec = (ConnectivityManager) getSystemService(CONNECTIVITY_SERVICE);
		assert connec != null;
		return connec.getActiveNetworkInfo() != null && connec.getActiveNetworkInfo().isAvailable() && connec.getActiveNetworkInfo().isConnectedOrConnecting();
	}
}
