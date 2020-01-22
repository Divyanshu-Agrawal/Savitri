package com.aaptrix.savitri.activities;

import com.aaptrix.savitri.R;
import com.aaptrix.savitri.session.FileUtil;
import com.aaptrix.savitri.session.FormatDate;
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
import id.zelory.compressor.Compressor;

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
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.button.MaterialButton;

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

import static com.aaptrix.savitri.session.SharedPrefsNames.KEY_ORG_ID;
import static com.aaptrix.savitri.session.SharedPrefsNames.KEY_SESSION_ID;
import static com.aaptrix.savitri.session.SharedPrefsNames.KEY_USER_ID;
import static com.aaptrix.savitri.session.SharedPrefsNames.KEY_USER_ROLE;
import static com.aaptrix.savitri.session.SharedPrefsNames.USER_PREFS;

public class RenewCompliance extends AppCompatActivity {
	
	EditText validFrom, validTo;
	String setValidFrom = "", setValidUpto = "", strOrgId, strUserId, strSessionId, strUserRole;
	Toolbar toolbar;
	RelativeLayout progressBar;
	MaterialButton renewCompliance;
	Button uploadCertificate;
	TextView certCount;
	ArrayList<File> filepath = new ArrayList<>();
	Calendar fromCalendar = Calendar.getInstance();
	Calendar uptoCalender = Calendar.getInstance();
	DatePickerDialog.OnDateSetListener validFromDate, validUptoDate;
	TextView name, notes, issueAuth, refNo;
	String strName, strNotes, strIssueAuth, strValidFrom, strValidTo, strRefno, strId;
	SharedPreferences sp;
	
	@SuppressLint("SetTextI18n")
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_renew_compliance);
		toolbar = findViewById(R.id.toolbar);
		setSupportActionBar(toolbar);
		Objects.requireNonNull(getSupportActionBar()).setDisplayShowHomeEnabled(true);
		getSupportActionBar().setDisplayHomeAsUpEnabled(true);
		name = findViewById(R.id.compliance_name);
		notes = findViewById(R.id.compliance_notes);
		issueAuth = findViewById(R.id.compliance_issue_auth);
		refNo = findViewById(R.id.compliance_ref_no);
		progressBar = findViewById(R.id.progress_bar);
		progressBar.bringToFront();
		validFrom = findViewById(R.id.compliance_valid_from);
		validTo = findViewById(R.id.compliance_valid_upto);
		renewCompliance = findViewById(R.id.add_compliance_btn);
		uploadCertificate = findViewById(R.id.compliance_certificate);
		certCount = findViewById(R.id.certificate_count);

		SharedPreferences sp = getSharedPreferences(USER_PREFS, Context.MODE_PRIVATE);
		strOrgId = sp.getString(KEY_ORG_ID, "");
		strUserId = sp.getString(KEY_USER_ID, "");
		strSessionId = sp.getString(KEY_SESSION_ID, "");
		strUserRole = sp.getString(KEY_USER_ROLE, "");
		
		strName = getIntent().getStringExtra("name");
		strNotes = getIntent().getStringExtra("notes");
		strIssueAuth = getIntent().getStringExtra("issueAuth");
		strId = getIntent().getStringExtra("id");
		strValidFrom = getIntent().getStringExtra("validFrom");
		strValidTo = getIntent().getStringExtra("validTo");
		strRefno = getIntent().getStringExtra("refNo");

		progressBar.setOnTouchListener((v, event) -> false);
		progressBar.setOnClickListener(v -> {});
		
		name.setText(strName);
		notes.setText(strNotes);
		issueAuth.setText(strIssueAuth);
		FormatDate date = new FormatDate(strValidFrom, "yyyy-MM-dd", "dd-MM-yyyy");
		validFrom.setText(date.format());
		date = new FormatDate(strValidTo, "yyyy-MM-dd", "dd-MM-yyyy");
		validTo.setText(date.format());
		refNo.setText(strRefno);

		if (strUserRole.equals("Admin")) {
			renewCompliance.setText("Renew Compliance");
		} else {
			renewCompliance.setText("Mark For Review");
		}
		
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
			setValidFrom = sdf.format(fromCalendar.getTime());
		};
		
		validFrom.setOnClickListener(v -> {
			DatePickerDialog datePickerDialog = new DatePickerDialog(this, validFromDate, fromCalendar
					.get(Calendar.YEAR), fromCalendar.get(Calendar.MONTH),
					fromCalendar.get(Calendar.DAY_OF_MONTH));
			datePickerDialog.show();
		});
		
		validUptoDate = (view, year, monthOfYear, dayOfMonth) -> {
			uptoCalender.set(Calendar.YEAR, year);
			uptoCalender.set(Calendar.MONTH, monthOfYear);
			uptoCalender.set(Calendar.DAY_OF_MONTH, dayOfMonth);
			String myFormat = "dd-MM-yyyy"; //In which you need put here
			SimpleDateFormat sdf = new SimpleDateFormat(myFormat, Locale.US);
			validTo.setText(sdf.format(uptoCalender.getTime()));
			myFormat = "yyyy-MM-dd";
			sdf = new SimpleDateFormat(myFormat, Locale.getDefault());
			setValidUpto = sdf.format(uptoCalender.getTime());
		};
		
		validTo.setOnClickListener(v -> {
			DatePickerDialog datePickerDialog = new DatePickerDialog(this, validUptoDate, uptoCalender
					.get(Calendar.YEAR), uptoCalender.get(Calendar.MONTH),
					uptoCalender.get(Calendar.DAY_OF_MONTH));
			datePickerDialog.show();
		});
		
		renewCompliance.setOnClickListener(v -> {
			if (TextUtils.isEmpty(setValidFrom)) {
				validFrom.setError("Please Select Date");
				validFrom.requestFocus();
			} else if (TextUtils.isEmpty(setValidUpto)) {
				validTo.setError("Please Select Date");
				validTo.requestFocus();
			} else if (filepath.size() == 0) {
				Toast.makeText(this, "Please Upload Certificate", Toast.LENGTH_SHORT).show();
			} else {
				renewCompliance.setEnabled(false);
				progressBar.setVisibility(View.VISIBLE);
				if (strUserRole.equals("Admin")) {
					Renew renew = new Renew(this);
					renew.execute(strOrgId, strSessionId, setValidFrom, setValidUpto, strId, URLs.RENEW_COMPLIANCE);
				} else {
					Renew renew = new Renew(this);
					renew.execute(strOrgId, strSessionId, setValidFrom, setValidUpto, strId, URLs.COMPLIANCE_REVIEW);
				}
			}
		});
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
									String fileExt = FileUtil.from(this, clipData.getItemAt(j).getUri()).toString().substring(FileUtil.from(this, clipData.getItemAt(j).getUri()).toString().lastIndexOf(".") + 1);
									if (fileExt.equals("jpg") || fileExt.equals("jpeg") || fileExt.equals("png")) {
										length = (int) new Compressor(this)
												.setMaxWidth(1280)
												.setMaxHeight(720)
												.setQuality(25)
												.setCompressFormat(Bitmap.CompressFormat.JPEG)
												.compressToFile(FileUtil.from(this, clipData.getItemAt(j).getUri())).length();
									} else {
										length = (int) FileUtil.from(this, data.getData()).length();
									}
									Log.e("length", "" + length);
								}
								for (int j = 0; j < filepath.size(); j++) {
									length = length + (int) filepath.get(j).length();
								}
								String fileExt = FileUtil.from(this, clipData.getItemAt(i).getUri()).toString().substring(FileUtil.from(this, clipData.getItemAt(i).getUri()).toString().lastIndexOf(".") + 1);
								if (length <= 1024000) {
									if (fileExt.equals("jpg") || fileExt.equals("jpeg") || fileExt.equals("png")) {
										filepath.add(new Compressor(this)
												.setMaxWidth(1280)
												.setMaxHeight(720)
												.setQuality(25)
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
							int length;
							String fileExt = FileUtil.from(this, data.getData()).toString().substring(FileUtil.from(this, data.getData()).toString().lastIndexOf(".") + 1);
							if (fileExt.equals("jpg") || fileExt.equals("jpeg") || fileExt.equals("png")) {
								length = (int) new Compressor(this)
										.setMaxWidth(1280)
										.setMaxHeight(720)
										.setQuality(25)
										.setCompressFormat(Bitmap.CompressFormat.JPEG)
										.compressToFile(FileUtil.from(this, data.getData())).length();
								Log.e("length", ""+length);
							} else {
								length = (int) FileUtil.from(this, data.getData()).length();
							}
							for (int j = 0; j < filepath.size(); j++) {
								length = length + (int) filepath.get(j).length();
							}
							if (length <= 1024000) {
								if (fileExt.equals("jpg") || fileExt.equals("jpeg") || fileExt.equals("png")) {
									filepath.add(new Compressor(this)
											.setMaxWidth(1280)
											.setMaxHeight(720)
											.setQuality(25)
											.setCompressFormat(Bitmap.CompressFormat.JPEG)
											.compressToFile(FileUtil.from(this, data.getData())));
									certCount.setText(filepath.size() + " Files Selected");
								} else {
									if((int)FileUtil.from(this, data.getData()).length() < 1024000)

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
	class Renew extends AsyncTask<String, String, String> {
		
		@SuppressLint("StaticFieldLeak")
		private Context context;
		
		Renew(Context context) {
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
			String sessionId = params[1];
			String validFrom = params[2];
			String validTo = params[3];
			String compliance_id = params[4];
			String url = params[5];
			
			
			try {
				ArrayList<String> fileNames = new ArrayList<>();
				for (int i = 0; i < filepath.size(); i++) {
					try {
						HttpClient httpclient = new DefaultHttpClient();
						HttpPost httppost = new HttpPost(url);
						MultipartEntityBuilder entityBuilder = MultipartEntityBuilder.create();
						entityBuilder.setMode(HttpMultipartMode.BROWSER_COMPATIBLE);
						FileBody image = new FileBody(filepath.get(i));
						entityBuilder.addPart("image", image);
						entityBuilder.addTextBody("org_details_id", orgId);
						entityBuilder.addTextBody("app_session_id", sessionId);
						HttpEntity entity = entityBuilder.build();
						httppost.setEntity(entity);
						HttpResponse response = httpclient.execute(httppost);
						HttpEntity httpEntity = response.getEntity();
						String result = EntityUtils.toString(httpEntity);
						Log.e("json", result);
						JSONObject jsonObject = new JSONObject(result);
						fileNames.add("\"" + jsonObject.getString("imageNm") + "\"");
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
				try {
					HttpClient httpclient = new DefaultHttpClient();
					HttpPost httppost = new HttpPost(url);
					MultipartEntityBuilder entityBuilder = MultipartEntityBuilder.create();
					entityBuilder.setMode(HttpMultipartMode.BROWSER_COMPATIBLE);
					entityBuilder.addTextBody("compliance_certificates", fileNames.toString());
					entityBuilder.addTextBody("org_details_id", orgId);
					entityBuilder.addTextBody("app_session_id", sessionId);
					entityBuilder.addTextBody("compliance_id", compliance_id);
					entityBuilder.addTextBody("compliance_valid_from", validFrom);
					entityBuilder.addTextBody("compliance_valid_upto", validTo);
					entityBuilder.addTextBody("users_details_id", strUserId);
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
						if (strUserRole.equals("Admin")) {
							Toast.makeText(context, "Renewed Successfully", Toast.LENGTH_SHORT).show();
//							startActivity(new Intent(context, RenewalActivity.class).setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP));
						} else {
							Toast.makeText(context, "Marked for review", Toast.LENGTH_SHORT).show();
//							startActivity(new Intent(context, MemberDashboard.class).setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP));
						}
						finish();
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
