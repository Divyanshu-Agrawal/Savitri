package com.aaptrix.savitri.activities;

import com.aaptrix.savitri.R;
import com.aaptrix.savitri.asyncclass.LoginUser;
import com.aaptrix.savitri.session.FileUtil;
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
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Patterns;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.iid.FirebaseInstanceId;
import com.squareup.picasso.Picasso;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Locale;
import java.util.Objects;

public class UserDetails extends AppCompatActivity {
	
	EditText userName, userDob, userDesignation, userEmail;
	Spinner userGender;
	ImageView userProfileImage;
	Button submitBtn;
	ArrayList<String> genderArray = new ArrayList<>();
	Toolbar toolbar;
	String type, strGender, strDob, strPassword, strNumber;
	Calendar myCalendar = Calendar.getInstance();
	DatePickerDialog.OnDateSetListener date;
	File imageFile = null;
	ProgressBar progressBar;
	String token;
	View v;
	
	@SuppressLint("SetTextI18n")
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_user_details);
		toolbar = findViewById(R.id.toolbar);
		setSupportActionBar(toolbar);
		Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
		getSupportActionBar().setDisplayShowHomeEnabled(true);
		userName = findViewById(R.id.user_name);
		userDob = findViewById(R.id.user_dob);
		userDesignation = findViewById(R.id.user_designation);
		userEmail = findViewById(R.id.user_email);
		userGender = findViewById(R.id.user_gender);
		userProfileImage = findViewById(R.id.user_profile);
		submitBtn = findViewById(R.id.user_detail_next_btn);
		progressBar = findViewById(R.id.progress_bar);
		progressBar.bringToFront();
		type = getIntent().getStringExtra("type");
		strPassword = getIntent().getStringExtra("password");
		strNumber = getIntent().getStringExtra("mobile");
		
		FirebaseInstanceId.getInstance().getInstanceId().addOnSuccessListener(this, instanceIdResult -> token = instanceIdResult.getToken());
		
		userName.setOnFocusChangeListener((v, hasFocus) -> {
			if (!hasFocus)
				hideKeyboard(v);
		});
		userDob.setOnFocusChangeListener((v, hasFocus) -> {
			if (!hasFocus)
				hideKeyboard(v);
		});
		userEmail.setOnFocusChangeListener((v, hasFocus) -> {
			if (!hasFocus)
				hideKeyboard(v);
		});
		userDesignation.setOnFocusChangeListener((v, hasFocus) -> {
			if (!hasFocus)
				hideKeyboard(v);
		});
		userGender.setOnFocusChangeListener((v, hasFocus) -> {
			if (!hasFocus)
				hideKeyboard(v);
		});
		
		switch (type) {
			case "verification":
				submitBtn.setText("Register");
				break;
			case "registration" :
				submitBtn.setText("Next");
				break;
		}
		
		genderArray.add("Select Gender*");
		genderArray.add("Male");
		genderArray.add("Female");
		genderArray.add("Other");
		
		ArrayAdapter<String> typeAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, genderArray);
		typeAdapter.notifyDataSetChanged();
		typeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		userGender.setAdapter(typeAdapter);
		
		userProfileImage.setOnClickListener(v -> {
			if (PermissionChecker.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
				Intent photoPickerIntent = new Intent();
				photoPickerIntent.setType("image/*");
				photoPickerIntent.setAction(Intent.ACTION_GET_CONTENT);
				startActivityForResult(photoPickerIntent, 1);
			} else {
				isPermissionGranted();
			}
		});

		userGender.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
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
				if (position != 0) {
					strGender = genderArray.get(position);
				}
			}
			
			@Override
			public void onNothingSelected(AdapterView<?> parent) {
			
			}
		});
		
		date = (view, year, monthOfYear, dayOfMonth) -> {
			myCalendar.set(Calendar.YEAR, year);
			myCalendar.set(Calendar.MONTH, monthOfYear);
			myCalendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
			String myFormat = "dd-MM-yyyy"; //In which you need put here
			SimpleDateFormat sdf = new SimpleDateFormat(myFormat, Locale.US);
			userDob.setText(sdf.format(myCalendar.getTime()));
			myFormat = "yyyy-MM-dd";
			sdf = new SimpleDateFormat(myFormat, Locale.getDefault());
			strDob = sdf.format(myCalendar.getTime());
		};
		
		userDob.setOnClickListener(v -> {
			DatePickerDialog datePickerDialog = new DatePickerDialog(this, date, myCalendar
					.get(Calendar.YEAR), myCalendar.get(Calendar.MONTH),
					myCalendar.get(Calendar.DAY_OF_MONTH));
			datePickerDialog.getDatePicker().setMaxDate(System.currentTimeMillis());
			datePickerDialog.show();
		});
		
		submitBtn.setOnClickListener(v -> {
			if (TextUtils.isEmpty(userName.getText().toString())) {
				userName.setError("Please Enter Your Name");
				userName.requestFocus();
				return;
			} if (TextUtils.isEmpty(userDob.getText().toString())) {
				userDob.setError("Please Enter Your DOB");
				userDob.requestFocus();
				return;
			} if (TextUtils.isEmpty(userDesignation.getText().toString())) {
				userDesignation.setError("Please Enter Your Designation");
				userDesignation.requestFocus();
				return;
			} if (TextUtils.isEmpty(userEmail.getText().toString())) {
				userEmail.setError("Please Enter Your Email");
				userEmail.requestFocus();
				return;
			} if (!Patterns.EMAIL_ADDRESS.matcher(userEmail.getText().toString()).matches()) {
				userEmail.setError("Please Enter Correct Email");
				userEmail.requestFocus();
				return;
			} if (TextUtils.isEmpty(strGender)) {
				Toast.makeText(this, "Please Select Gender", Toast.LENGTH_SHORT).show();
				return;
			} if (type.equals("verification")) {
				RegisterUser registerUser = new RegisterUser(this);
				registerUser.execute(userName.getText().toString(), strNumber, userEmail.getText().toString(),
						strDob, strGender, userDesignation.getText().toString(), strPassword);
			} else if (type.equals("registration")) {
				Intent intent = new Intent(this, OrganisationDetails.class);
				intent.putExtra("profile_image", imageFile);
				intent.putExtra("user_name", userName.getText().toString());
				intent.putExtra("user_dob", strDob);
				intent.putExtra("user_gender", strGender);
				intent.putExtra("user_designation", userDesignation.getText().toString());
				intent.putExtra("user_email", userEmail.getText().toString());
				intent.putExtra("user_password", strPassword);
				intent.putExtra("user_mobile", strNumber);
				startActivity(intent);
			}
		});
	}
	
	private void hideKeyboard(View view) {
		InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(Activity.INPUT_METHOD_SERVICE);
		inputMethodManager.hideSoftInputFromWindow(view.getWindowToken(), 0);
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
								.into(userProfileImage);
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
	class RegisterUser extends AsyncTask<String, String, String> {
		
		@SuppressLint("StaticFieldLeak")
		private Context context;
		private String userMobile, userPassword;
		
		RegisterUser(Context context) {
			this.context = context;
		}
		
		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			progressBar.setVisibility(View.VISIBLE);
		}
		
		
		@Override
		protected String doInBackground(String... params) {
			
			String userName = params[0];
			userMobile = params[1];
			String userEmail = params[2];
			String userDob = params[3];
			String userGender = params[4];
			String userDesignation = params[5];
			userPassword = params[6];
			
			try {
				HttpClient httpclient = new DefaultHttpClient();
				HttpPost httppost = new HttpPost(URLs.MEMBER_REGISTER_URL);
				MultipartEntityBuilder entityBuilder = MultipartEntityBuilder.create();
				entityBuilder.setMode(HttpMultipartMode.BROWSER_COMPATIBLE);

				if (imageFile != null)
				    entityBuilder.addPart("prof_img", new FileBody(imageFile));

				entityBuilder.addTextBody("users_name", userName);
				entityBuilder.addTextBody("users_mobileno", userMobile);
				entityBuilder.addTextBody("users_email", userEmail);
				entityBuilder.addTextBody("users_dob", userDob);
				entityBuilder.addTextBody("users_gender", userGender);
				entityBuilder.addTextBody("users_designation", userDesignation);
				entityBuilder.addTextBody("users_password", userPassword);
				
				HttpEntity entity = entityBuilder.build();
				httppost.setEntity(entity);
				HttpResponse response = httpclient.execute(httppost);
				HttpEntity httpEntity = response.getEntity();
				return EntityUtils.toString(httpEntity);
			} catch (IOException e) {
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
						Toast.makeText(context, "Regsitered Successfully", Toast.LENGTH_SHORT).show();
						LoginUser loginUser = new LoginUser(context, progressBar);
						loginUser.execute(userMobile, userPassword, token);
					} else {
						progressBar.setVisibility(View.GONE);
						Toast.makeText(context, "Error Occured", Toast.LENGTH_SHORT).show();
					}
				} catch (JSONException e) {
					e.printStackTrace();
				}
			}
			super.onPostExecute(result);
		}
	}
}
