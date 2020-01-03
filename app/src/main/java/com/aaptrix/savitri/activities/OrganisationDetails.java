package com.aaptrix.savitri.activities;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.aaptrix.savitri.asyncclass.LoginUser;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Objects;

import com.aaptrix.savitri.R;
import com.aaptrix.savitri.databeans.StateData;
import com.aaptrix.savitri.session.URLs;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.iid.FirebaseInstanceId;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import cz.msebera.android.httpclient.HttpEntity;
import cz.msebera.android.httpclient.HttpResponse;
import cz.msebera.android.httpclient.client.HttpClient;
import cz.msebera.android.httpclient.client.methods.HttpPost;
import cz.msebera.android.httpclient.entity.mime.HttpMultipartMode;
import cz.msebera.android.httpclient.entity.mime.MultipartEntityBuilder;
import cz.msebera.android.httpclient.entity.mime.content.FileBody;
import cz.msebera.android.httpclient.impl.client.DefaultHttpClient;
import cz.msebera.android.httpclient.util.EntityUtils;

public class OrganisationDetails extends AppCompatActivity {
	
	Toolbar toolbar;
	EditText orgName, orgType, orgLandline, orgAddress, orgPincode, orgDetails, orgDistrict;
	Spinner orgCity, orgState;
	MaterialButton registerBtn, termsBtn, privacyBtn;
	String userName, userDob, userDesignation, userEmail, userGender, userPassword, userMobile;
	File userProfile;
	ProgressBar progressBar;
	CheckBox tncCheckbox;
	String strState, strCity;
	private StateData stateData;
	private ArrayList<StateData> stateArray = new ArrayList<>();
	private ArrayList<String> stateName = new ArrayList<>();
	private ArrayList<String> stateID = new ArrayList<>();
	private ArrayList<String> cityarray = new ArrayList<>();
	ArrayAdapter<String> cityAdapter;
	String token;
	View v;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_organisation_details);
		toolbar = findViewById(R.id.toolbar);
		setSupportActionBar(toolbar);
		Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
		getSupportActionBar().setDisplayShowHomeEnabled(true);
		orgName = findViewById(R.id.organisation_name);
		orgType = findViewById(R.id.organisation_type);
		orgLandline = findViewById(R.id.organisation_landline);
		orgAddress = findViewById(R.id.organisation_address);
		orgCity = findViewById(R.id.organisation_city);
		orgDistrict = findViewById(R.id.organisation_district);
		orgState = findViewById(R.id.organisation_state);
		orgPincode = findViewById(R.id.organisation_pincode);
		registerBtn = findViewById(R.id.register_org_btn);
		termsBtn = findViewById(R.id.terms_btn);
		privacyBtn = findViewById(R.id.privacy_btn);
		orgDetails = findViewById(R.id.organisation_details);
		progressBar = findViewById(R.id.progress_bar);
		progressBar.bringToFront();
		tncCheckbox = findViewById(R.id.tnc_checkbox);
		
		privacyBtn.setOnClickListener(v -> {
			Intent intent = new Intent(this, TnCActivity.class);
			intent.putExtra("type", "privacy");
			intent.putExtra("url", URLs.PRIVACY_URL);
			startActivity(intent);
		});
		
		termsBtn.setOnClickListener(v -> {
			Intent intent = new Intent(this, TnCActivity.class);
			intent.putExtra("type", "tnc");
			intent.putExtra("url", URLs.TnC_URL);
			startActivity(intent);
		});
		
		userName = getIntent().getStringExtra("user_name");
		userDob = getIntent().getStringExtra("user_dob");
		userDesignation = getIntent().getStringExtra("user_designation");
		userEmail = getIntent().getStringExtra("user_email");
		userGender = getIntent().getStringExtra("user_gender");
		if (getIntent().getSerializableExtra("profile_image") != null)
			userProfile = (File) getIntent().getSerializableExtra("profile_image");
		else
			userProfile = null;
		userPassword = getIntent().getStringExtra("user_password");
		userMobile = getIntent().getStringExtra("user_mobile");
		fetchAllState();
		
		FirebaseInstanceId.getInstance().getInstanceId().addOnSuccessListener(this, instanceIdResult -> token = instanceIdResult.getToken());
		
		orgDetails.setOnFocusChangeListener((v, hasFocus) -> {
			if (!hasFocus)
				hideKeyboard(v);
		});
		orgType.setOnFocusChangeListener((v, hasFocus) -> {
			if (!hasFocus)
				hideKeyboard(v);
		});
		orgState.setOnFocusChangeListener((v, hasFocus) -> {
			if (!hasFocus)
				hideKeyboard(v);
		});
		orgDistrict.setOnFocusChangeListener((v, hasFocus) -> {
			if (!hasFocus)
				hideKeyboard(v);
		});
		orgCity.setOnFocusChangeListener((v, hasFocus) -> {
			if (!hasFocus)
				hideKeyboard(v);
		});
		orgLandline.setOnFocusChangeListener((v, hasFocus) -> {
			if (!hasFocus)
				hideKeyboard(v);
		});
		orgPincode.setOnFocusChangeListener((v, hasFocus) -> {
			if (!hasFocus)
				hideKeyboard(v);
		});
		orgName.setOnFocusChangeListener((v, hasFocus) -> {
			if (!hasFocus)
				hideKeyboard(v);
		});
		orgAddress.setOnFocusChangeListener((v, hasFocus) -> {
			if (!hasFocus)
				hideKeyboard(v);
		});
		
		
		
		cityarray.add("Select City*");
		cityAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, cityarray);
		cityAdapter.notifyDataSetChanged();
		cityAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		orgCity.setAdapter(cityAdapter);
		orgCity.setEnabled(false);
		
		orgState.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
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
					cityarray.clear();
					orgCity.setEnabled(true);
					strState = stateID.get(position);
					fetchCity(stateID.get(position));
				} else {
					cityarray.clear();
					orgCity.setEnabled(false);
				}
			}
			
			@Override
			public void onNothingSelected(AdapterView<?> parent) {
			
			}
		});
		
		registerBtn.setOnClickListener(v -> {
			if (TextUtils.isEmpty(orgName.getText().toString())) {
				orgName.setError("Please Enter Organisation Name");
				orgName.requestFocus();
			} else if (TextUtils.isEmpty(orgType.getText().toString())) {
				orgType.setError("Please Enter Organisation Type");
				orgType.requestFocus();
			} else if (TextUtils.isEmpty(orgLandline.getText().toString())) {
				orgLandline.setError("Please Enter Organisation Number");
				orgLandline.requestFocus();
			} else if (TextUtils.isEmpty(orgDetails.getText().toString())) {
				orgDetails.setError("Please Enter Organisation Details");
				orgDetails.requestFocus();
			} else if (TextUtils.isEmpty(orgAddress.getText().toString())) {
				orgAddress.setError("Please Enter Organisation Address");
				orgAddress.requestFocus();
			} else if (TextUtils.isEmpty(strCity)) {
				Toast.makeText(this, "Please Select Organisation City", Toast.LENGTH_SHORT).show();
			} else if (TextUtils.isEmpty(orgDistrict.getText().toString())) {
				orgDistrict.setError("Please Enter District");
				orgDistrict.requestFocus();
			} else if (TextUtils.isEmpty(strState)) {
				Toast.makeText(this, "Please Select Organisation State", Toast.LENGTH_SHORT).show();
			} else if (TextUtils.isEmpty(orgPincode.getText().toString())) {
				orgPincode.setError("Please Enter Organisation Pincode");
				orgPincode.requestFocus();
			} else if (orgPincode.getText().toString().length() != 6) {
				orgPincode.setError("Please Enter Correct Pincode");
				orgPincode.requestFocus();
			} else if (orgLandline.getText().toString().length() != 11) {
				orgLandline.setError("Please Enter Correct Number");
				orgLandline.requestFocus();
			} else if (!tncCheckbox.isChecked()){
				Toast.makeText(this, "Please Accept Terms and Conditions, Privacy Policy", Toast.LENGTH_SHORT).show();
			} else {
				RegisterUser registerUser = new RegisterUser(this);
				registerUser.execute(orgName.getText().toString(),
						orgType.getText().toString(),
						orgAddress.getText().toString(),
						orgLandline.getText().toString(),
						strState, strCity, orgDistrict.getText().toString(),
						orgPincode.getText().toString(), orgDetails.getText().toString());
			}
		});
	}
	
	private void hideKeyboard(View view) {
		InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(Activity.INPUT_METHOD_SERVICE);
		inputMethodManager.hideSoftInputFromWindow(view.getWindowToken(), 0);
	}
	
	private void fetchAllState() {
		StringRequest stringRequest = new StringRequest(Request.Method.GET, URLs.ALL_STATE_URL, response -> {
			try {
				JSONObject jsonObject = new JSONObject(response);
				JSONArray jsonArray = jsonObject.getJSONArray("allState");
				for (int i = 0; i<jsonArray.length(); i++) {
					JSONObject jObject = jsonArray.getJSONObject(i);
					stateData = new StateData();
					stateData.setId(jObject.getString("id"));
					stateData.setName(jObject.getString("name"));
					stateArray.add(stateData);
				}
				setState(stateArray);
			} catch (JSONException e) {
				e.printStackTrace();
			}
		}, error -> Toast.makeText(this, "Something went wrong...", Toast.LENGTH_SHORT).show());
		
		RequestQueue requestQueue = Volley.newRequestQueue(this);
		requestQueue.add(stringRequest);
	}
	
	private void setState(ArrayList<StateData> stateArrayList) {
		stateName.add("Select State*");
		stateID.add("00");
		for (int i=0; i<stateArrayList.size(); i++) {
			stateName.add(stateArrayList.get(i).getName());
			stateID.add(stateArrayList.get(i).getId());
		}
		ArrayAdapter<String> stateAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, stateName);
		stateAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		stateAdapter.notifyDataSetChanged();
		orgState.setAdapter(stateAdapter);
	}
	
	private void fetchCity(String stateid) {
		String url = URLs.CITY_URL + "?stateId=" + stateid;
		StringRequest stringRequest = new StringRequest(Request.Method.GET, url, response -> {
			try {
				JSONObject jsonObject = new JSONObject(response);
				JSONArray jsonArray = jsonObject.getJSONArray("cityList");
				cityarray.add("Select City*");
				for (int i = 0; i<jsonArray.length(); i++) {
					JSONObject jObject = jsonArray.getJSONObject(i);
					cityarray.add(jObject.getString("name"));
				}
				setCity(cityarray);
			} catch (JSONException e) {
				e.printStackTrace();
			}
		}, error -> Toast.makeText(this, "Something went wrong...", Toast.LENGTH_SHORT).show());
		
		RequestQueue requestQueue = Volley.newRequestQueue(this);
		requestQueue.add(stringRequest);
	}
	
	private void setCity(final ArrayList<String> city) {
		cityAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, city);
		cityAdapter.notifyDataSetChanged();
		cityAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		orgCity.setAdapter(cityAdapter);
		
		orgCity.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
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
					strCity = city.get(position);
				} else {
					Toast.makeText(OrganisationDetails.this, "Please Select City", Toast.LENGTH_SHORT).show();
				}
			}
			
			@Override
			public void onNothingSelected(AdapterView<?> parent) {
			
			}
		});
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
			
			String name = params[0];
			String type = params[1];
			String address = params[2];
			String landline = params[3];
			String state = params[4];
			String city = params[5];
			String district = params[6];
			String pincode = params[7];
			String details = params[8];
			
			try {
				HttpClient httpclient = new DefaultHttpClient();
				HttpPost httppost = new HttpPost(URLs.ORG_REGISTER_URL);
				MultipartEntityBuilder entityBuilder = MultipartEntityBuilder.create();
				entityBuilder.setMode(HttpMultipartMode.BROWSER_COMPATIBLE);
				if (userProfile != null)
					entityBuilder.addPart("prof_img", new FileBody(userProfile));
				entityBuilder.addTextBody("users_name", userName);
				entityBuilder.addTextBody("users_mobileno", userMobile);
				entityBuilder.addTextBody("users_email", userEmail);
				entityBuilder.addTextBody("users_dob", userDob);
				entityBuilder.addTextBody("users_gender", userGender);
				entityBuilder.addTextBody("users_designation", userDesignation);
				entityBuilder.addTextBody("users_password", userPassword);
				entityBuilder.addTextBody("users_type", "Admin");
				entityBuilder.addTextBody("org_name", name);
				entityBuilder.addTextBody("org_details", details);
				entityBuilder.addTextBody("org_landline_no", landline);
				entityBuilder.addTextBody("org_state", state);
				entityBuilder.addTextBody("org_city", city);
				entityBuilder.addTextBody("org_pincode", pincode);
				entityBuilder.addTextBody("org_district", district);
				entityBuilder.addTextBody("org_address", address);
				entityBuilder.addTextBody("org_type", type);
				
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
				Log.e("result", result);
				try {
					JSONObject jsonObject = new JSONObject(result);
					if (jsonObject.getBoolean("success")) {
						Toast.makeText(context, "Regsitered Successfully", Toast.LENGTH_SHORT).show();
						LoginUser loginUser = new LoginUser(context, progressBar);
						loginUser.execute(userMobile, userPassword, token);
					} else {
						progressBar.setVisibility(View.GONE);
						Toast.makeText(context, "Error Occured", Toast.LENGTH_SHORT).show();
						recreate();
					}
				} catch (JSONException e) {
					e.printStackTrace();
				}
			}
			super.onPostExecute(result);
		}
	}
}
