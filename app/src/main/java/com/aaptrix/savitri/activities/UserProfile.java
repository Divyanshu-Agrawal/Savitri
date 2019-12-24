package com.aaptrix.savitri.activities;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.app.Activity;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.github.chrisbanes.photoview.PhotoView;
import com.squareup.picasso.Picasso;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Objects;

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

import static com.aaptrix.savitri.session.SharedPrefsNames.KEY_ORG_ID;
import static com.aaptrix.savitri.session.SharedPrefsNames.KEY_SESSION_ID;
import static com.aaptrix.savitri.session.SharedPrefsNames.KEY_USER_ID;
import static com.aaptrix.savitri.session.SharedPrefsNames.KEY_USER_IMAGE;
import static com.aaptrix.savitri.session.SharedPrefsNames.USER_PREFS;
import static com.aaptrix.savitri.session.URLs.DATA_URL;

public class UserProfile extends AppCompatActivity {
	
	Toolbar toolbar;
	ImageView userProfile, editProfile;
	File imageFile;
	String userId, orgId, sessionId;
	SharedPreferences sp;
	ProgressBar progressBar;
	TextView designation, gender, dob, regDate, email, number, type, name;
	RelativeLayout fullscrLayout;
	PhotoView fullscrImage;
	boolean isVisible = false;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_user_profile);
		toolbar = findViewById(R.id.toolbar);
		userProfile = findViewById(R.id.profile_image);
		editProfile = findViewById(R.id.edit_profile);
		progressBar = findViewById(R.id.progress_bar);
		name = findViewById(R.id.user_name);
		setSupportActionBar(toolbar);
		toolbar.bringToFront();
		Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
		getSupportActionBar().setDisplayShowHomeEnabled(true);
		getSupportActionBar().setTitle("");
		sp = getSharedPreferences(USER_PREFS, Context.MODE_PRIVATE);
		designation = findViewById(R.id.user_designation);
		gender = findViewById(R.id.user_gender);
		dob = findViewById(R.id.user_dob);
		type = findViewById(R.id.user_type);
		email = findViewById(R.id.user_email);
		number = findViewById(R.id.user_contact);
		regDate = findViewById(R.id.user_reg_on);
		fullscrImage = findViewById(R.id.fullscr_user_profile);
		fullscrLayout = findViewById(R.id.fullscr_image);
		
		userId = getIntent().getStringExtra("userId");
		orgId = sp.getString(KEY_ORG_ID, "");
		sessionId = sp.getString(KEY_SESSION_ID, "");
		fetchUserProfile(userId, orgId, sessionId);
		
		if (userId.equals(sp.getString(KEY_USER_ID, ""))) {
			editProfile.setVisibility(View.VISIBLE);
		} else {
			editProfile.setVisibility(View.GONE);
		}
		
		number.setOnClickListener(v -> {
			Intent intent = new Intent(Intent.ACTION_DIAL);
			intent.setData(Uri.parse("tel:" + "+91" + number.getText().toString()));
			startActivity(intent);
		});
		
		email.setOnClickListener(v -> {
			Intent intent = new Intent(Intent.ACTION_SENDTO);
			intent.setData(Uri.parse("mailto:" + email.getText().toString()));
			startActivity(Intent.createChooser(intent, "Send Mail..."));
		});
		
		editProfile.setOnClickListener(v -> {
			if (PermissionChecker.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
				Intent intent = new Intent();
				intent.setType("image/*");
				intent.setAction(Intent.ACTION_GET_CONTENT);
				startActivityForResult(intent, 1);
			} else {
				isPermissionGranted();
			}
		});
	}
	
	private void fetchUserProfile(String userId, String orgId, String sessionID) {
		progressBar.setVisibility(View.VISIBLE);
		new Thread(() -> {
			try {
				HttpClient httpclient = new DefaultHttpClient();
				HttpPost httppost = new HttpPost(URLs.ALL_PEOPLE);
				MultipartEntityBuilder entityBuilder = MultipartEntityBuilder.create();
				entityBuilder.setMode(HttpMultipartMode.BROWSER_COMPATIBLE);
				entityBuilder.addTextBody("org_details_id", orgId);
				entityBuilder.addTextBody("app_session_id", sessionID);
				entityBuilder.addTextBody("users_details_id", userId);
				entityBuilder.addTextBody("profile", "profile");
				HttpEntity entity = entityBuilder.build();
				httppost.setEntity(entity);
				HttpResponse response = httpclient.execute(httppost);
				HttpEntity httpEntity = response.getEntity();
				String result = EntityUtils.toString(httpEntity);
				Handler handler = new Handler(Looper.getMainLooper());
				handler.post(() -> {
					try {
						progressBar.setVisibility(View.GONE);
						if (result.contains("\"success\":false,\"msg\":\"Session Expire\"")){
							Toast.makeText(this, "Your Session is expired please login again", Toast.LENGTH_SHORT).show();
							SharedPrefsManager.getInstance(this).logout();
							Intent intent = new Intent(this, AppLogin.class);
							intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
							startActivity(intent);
							finish();
						} else if (result.equals("null")) {
							Toast.makeText(this, "Error Occured", Toast.LENGTH_SHORT).show();
						}  else {
							Log.e("res", result);
							JSONObject jsonObject = new JSONObject(result);
							JSONArray jsonArray = jsonObject.getJSONArray("profileDetails");
							JSONObject jObject = jsonArray.getJSONObject(0);
							name.setText(jObject.getString("users_name"));
							designation.setText(jObject.getString("users_designation"));
							gender.setText(jObject.getString("users_gender"));
							number.setText(jObject.getString("users_mobileno"));
							email.setText(jObject.getString("users_email"));
							type.setText(jObject.getString("users_type"));
							FormatDate date = new FormatDate(jObject.getString("users_dob"), "yyyy-MM-dd", "dd MMM yyyy");
							dob.setText(date.format());
							date = new FormatDate(jObject.getString("users_reg_date"), "yyyy-MM-dd", "dd-MM-yyyy");
							regDate.setText(date.format());
							String url = DATA_URL + orgId + "/profile/" + jObject.getString("users_profile_img");
							Picasso.with(this).load(url).placeholder(R.drawable.user_placeholder).into(userProfile);
							
							userProfile.setOnClickListener(v -> {
								fullscrLayout.setVisibility(View.VISIBLE);
								isVisible = true;
								Picasso.with(this).load(url).placeholder(R.drawable.user_placeholder).into(fullscrImage);
							});
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
	
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		Uri selectedImage;
		if (requestCode == 1) {
			if (resultCode == Activity.RESULT_OK) {
				CropImage.activity(Objects.requireNonNull(data.getData()))
						.setGuidelines(CropImageView.Guidelines.ON)
						.setAspectRatio(150, 150)
						.setGuidelines(CropImageView.Guidelines.ON)
						.setCropShape(CropImageView.CropShape.RECTANGLE)
						.start(this);
				
			}
		}
		
		if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
			CropImage.ActivityResult result = CropImage.getActivityResult(data);
			if (resultCode == RESULT_OK) {
				try {
					selectedImage = result.getUri();
					String filename = FileUtil.getFileName(this, selectedImage);
					String file_extn = filename.substring(filename.lastIndexOf(".") + 1);
					if (file_extn.equals("jpg") || file_extn.equals("jpeg") || file_extn.equals("png")) {
						imageFile = new Compressor(this)
								.setMaxWidth(640)
								.setMaxHeight(480)
								.setQuality(75)
								.setCompressFormat(Bitmap.CompressFormat.JPEG)
								.compressToFile(FileUtil.from(this, selectedImage));
						Picasso.with(this)
								.load(imageFile)
								.into(userProfile);
						updateProfile(imageFile);
					} else {
						FileNotFoundException fe = new FileNotFoundException();
						Toast.makeText(this, "File not in required format.", Toast.LENGTH_SHORT).show();
						throw fe;
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
	}
	
	private void updateProfile(File imageFile) {
		progressBar.setVisibility(View.VISIBLE);
		new Thread(() -> {
			try {
				HttpClient httpclient = new DefaultHttpClient();
				HttpPost httppost = new HttpPost(URLs.UPDATE_PROFILE);
				MultipartEntityBuilder entityBuilder = MultipartEntityBuilder.create();
				entityBuilder.setMode(HttpMultipartMode.BROWSER_COMPATIBLE);
				entityBuilder.addTextBody("org_details_id", orgId);
				entityBuilder.addTextBody("app_session_id", sessionId);
				entityBuilder.addTextBody("users_details_id", userId);
				entityBuilder.addPart("prof_img", new FileBody(imageFile));
				HttpEntity entity = entityBuilder.build();
				httppost.setEntity(entity);
				HttpResponse response = httpclient.execute(httppost);
				HttpEntity httpEntity = response.getEntity();
				String result = EntityUtils.toString(httpEntity);
				Handler handler = new Handler(Looper.getMainLooper());
				handler.post(() -> {
					try {
						progressBar.setVisibility(View.GONE);
						if (result.contains("\"success\":false,\"msg\":\"Session Expire\"")){
							Toast.makeText(this, "Your Session is expired please login again", Toast.LENGTH_SHORT).show();
							SharedPrefsManager.getInstance(this).logout();
							Intent intent = new Intent(this, AppLogin.class);
							intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
							startActivity(intent);
							finish();
						} else if (result.equals("null")) {
							Toast.makeText(this, "Error Occured", Toast.LENGTH_SHORT).show();
						}  else {
							Log.e("res", result);
							JSONObject jsonObject = new JSONObject(result);
							sp.edit().putString(KEY_USER_IMAGE, jsonObject.getString("img")).apply();
							Toast.makeText(this, "Profile Updated Successfully", Toast.LENGTH_SHORT).show();
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
	public void onBackPressed() {
		if (isVisible) {
			isVisible = false;
			fullscrLayout.setVisibility(View.GONE);
		} else {
			super.onBackPressed();
		}
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		if (item.getItemId() == android.R.id.home) {
			onBackPressed();
		}
		return super.onOptionsItemSelected(item);
	}
}
