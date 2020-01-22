package com.aaptrix.savitri.activities;

import android.Manifest;
import android.app.Activity;
import android.app.FragmentTransaction;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.ConnectivityManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.provider.ContactsContract;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.button.MaterialButton;
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
import com.google.android.material.snackbar.Snackbar;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
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

import static com.aaptrix.savitri.session.SharedPrefsNames.KEY_MEMBER_COUNT;
import static com.aaptrix.savitri.session.SharedPrefsNames.KEY_ORG_ID;
import static com.aaptrix.savitri.session.SharedPrefsNames.KEY_ORG_NAME;
import static com.aaptrix.savitri.session.SharedPrefsNames.KEY_ORG_PLAN_TYPE;
import static com.aaptrix.savitri.session.SharedPrefsNames.KEY_PEOPLE_COUNT;
import static com.aaptrix.savitri.session.SharedPrefsNames.KEY_SESSION_ID;
import static com.aaptrix.savitri.session.SharedPrefsNames.KEY_USER_ID;
import static com.aaptrix.savitri.session.SharedPrefsNames.KEY_USER_NAME;
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
	String strOrgId, strSessionId, strUserId, strPlanType;
	SharedPreferences sp;
	int memberCount;
	RelativeLayout layout;
	AlertDialog alertDialog;
	AlertDialog.Builder alert;
	private EditText memberPhone, memberName;
	private String senderName, senderId, strOrgName;
	
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
		layout = findViewById(R.id.layout);
		sp = getSharedPreferences(USER_PREFS, Context.MODE_PRIVATE);
		strOrgId = sp.getString(KEY_ORG_ID, "");
		strSessionId = sp.getString(KEY_SESSION_ID, "");
		strUserId = sp.getString(KEY_USER_ID, "");
		memberCount = sp.getInt(KEY_MEMBER_COUNT, 0);
		strPlanType = sp.getString(KEY_ORG_PLAN_TYPE, "");
		
		if (sp.getString(KEY_USER_ROLE, "").equals("Admin")) {
			addPeople.setVisibility(View.VISIBLE);
		} else {
			addPeople.setVisibility(View.GONE);
		}
		progressBar.setVisibility(View.VISIBLE);
		
		addPeople.setOnClickListener(v -> {
			if (sp.getInt(KEY_PEOPLE_COUNT, 0) < memberCount) {
//				FragmentTransaction ft = getFragmentManager().beginTransaction();
//				AddMembersDialog dialog = new AddMembersDialog();
//				dialog.show(ft, "dialog");
				addMember();
			} else {
				DialogInterface.OnClickListener onClickListener = (dialog, which) -> startActivity(new Intent(this, PlansActivity.class));
					new AlertDialog.Builder(this)
							.setMessage("Please upgrade your plan to add more members.")
							.setPositiveButton("Upgrade", onClickListener)
							.setNegativeButton("Close", null)
							.show();
			}
		});
		
		if (checkConnection()) {
			noPeople.setVisibility(View.GONE);
			fetchPeople();
		} else {
			try {
				Snackbar snackbar = Snackbar.make(layout, "No Internet Connection", Snackbar.LENGTH_INDEFINITE)
						.setActionTextColor(Color.WHITE)
						.setAction("Ok", null);
				snackbar.show();
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

	private void addMember() {
		LayoutInflater factory = LayoutInflater.from(this);
		final View view = factory.inflate(R.layout.dialog_add_member, null);

		memberPhone = view.findViewById(R.id.add_member_phone);
		Switch makeAdmin = view.findViewById(R.id.make_admin_switch);
		MaterialButton cancel = view.findViewById(R.id.cancel_btn);
		MaterialButton add = view.findViewById(R.id.add_btn);
		ImageButton pickContact = view.findViewById(R.id.pick_btn);

		senderId = sp.getString(KEY_USER_ID, "");
		senderName = sp.getString(KEY_USER_NAME, "");
		strOrgName = sp.getString(KEY_ORG_NAME, "");
		makeAdmin.setChecked(false);
		memberName = view.findViewById(R.id.add_member_name);
		ProgressBar progressBar = view.findViewById(R.id.progress_bar);

		memberPhone.setOnFocusChangeListener((v, hasFocus) -> {
			if (!hasFocus)
				hideKeyboard(v);
		});
		memberName.setOnFocusChangeListener((v, hasFocus) -> {
			if (!hasFocus)
				hideKeyboard(v);
		});

		pickContact.setOnClickListener(v -> {
			if (PermissionChecker.checkSelfPermission(this, Manifest.permission.READ_CONTACTS) == PackageManager.PERMISSION_GRANTED) {
				Intent intent = new Intent(Intent.ACTION_PICK, ContactsContract.Contacts.CONTENT_URI);
				startActivityForResult(intent, 1);
			} else {
				isPermissionGranted();
			}
		});

		add.setOnClickListener(v -> {
			if (TextUtils.isEmpty(memberPhone.getText().toString())) {
				memberPhone.setError("Please Enter Phone Number");
				memberPhone.requestFocus();
			} else if (memberPhone.getText().toString().length() < 10) {
				memberPhone.setError("Please Enter Correct Phone Number");
				memberPhone.requestFocus();
				memberPhone.getText().clear();
			} else if (TextUtils.isEmpty(memberName.getText().toString())) {
				memberName.setError("Please Enter Member Name");
				memberName.requestFocus();
			} else {
//                makeAdminState = makeAdmin.isChecked();
				progressBar.setVisibility(View.VISIBLE);
//                add.setEnabled(false);
//                if (makeAdminState) {
//                    addMember(memberPhone.getText().toString(), "Admin", strOrgId, memberName.getText().toString(), strOrgName);
//                } else {
				addMember(memberPhone.getText().toString(), "Team Member", strOrgId, memberName.getText().toString(), strOrgName);
//                }
			}
		});

		alert = new AlertDialog.Builder(this);

		alert.setView(view);
		alertDialog = alert.create();
		alertDialog.show();
		Objects.requireNonNull(alertDialog.getWindow()).setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

		cancel.setOnClickListener(v -> alertDialog.dismiss());

		alertDialog.getButton(DialogInterface.BUTTON_POSITIVE).setVisibility(View.GONE);
		alertDialog.getButton(DialogInterface.BUTTON_NEGATIVE).setVisibility(View.GONE);
	}

	private void hideKeyboard(View view) {
		InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(Activity.INPUT_METHOD_SERVICE);
		inputMethodManager.hideSoftInputFromWindow(view.getWindowToken(), 0);
	}

	public void onActivityResult(int reqCode, int resultCode, Intent data) {
		super.onActivityResult(reqCode, resultCode, data);

		if (reqCode == 1) {
			if (resultCode == Activity.RESULT_OK) {
				Uri contactData = data.getData();
				Cursor c = this.managedQuery(contactData, null, null, null, null);
				if (c.moveToFirst()) {
					String id = c.getString(c.getColumnIndexOrThrow(ContactsContract.Contacts._ID));
					String hasPhone = c.getString(c.getColumnIndex(ContactsContract.Contacts.HAS_PHONE_NUMBER));

					if (hasPhone.equalsIgnoreCase("1")) {
						Cursor phones = this.getContentResolver().query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null,
								ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " = " + id, null, null);
						phones.moveToFirst();
						String number = phones.getString(phones.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
						String name = phones.getString(phones.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME));
						number = number.replace(" ", "").replace("-", "");
						memberPhone.setText(number);
						memberName.setText(name);
					}
				}
			}
		}
	}

	private void addMember(String phone, String makeAdmin, String orgId, String name, String orgName) {
		new Thread(() -> {
			try {
				HttpClient httpclient = new DefaultHttpClient();
				HttpPost httppost = new HttpPost(URLs.MEMBER_REGISTER_URL);
				MultipartEntityBuilder entityBuilder = MultipartEntityBuilder.create();
				entityBuilder.setMode(HttpMultipartMode.BROWSER_COMPATIBLE);
				entityBuilder.addTextBody("org_details_id", orgId);
				entityBuilder.addTextBody("org_details_name", orgName);
				entityBuilder.addTextBody("users_mobileno", phone);
				entityBuilder.addTextBody("users_type", makeAdmin);
				entityBuilder.addTextBody("users_name", name);
				entityBuilder.addTextBody("sender_nm", senderName);
				entityBuilder.addTextBody("sender_id", senderId);
				HttpEntity entity = entityBuilder.build();
				httppost.setEntity(entity);
				HttpResponse response = httpclient.execute(httppost);
				HttpEntity httpEntity = response.getEntity();
				String result = EntityUtils.toString(httpEntity);
				Handler handler = new Handler(Looper.getMainLooper());
				handler.post(() -> {
					progressBar.setVisibility(View.GONE);
					if (result.contains("{\"success\":true}")) {
						Toast.makeText(this, "Added Successfully", Toast.LENGTH_SHORT).show();
						alertDialog.dismiss();
						fetchPeople();
					} else if (result.contains("Already Exist")) {
						Toast.makeText(this, "Already Exist", Toast.LENGTH_SHORT).show();
					} else {
						Toast.makeText(this, "Error Occured", Toast.LENGTH_SHORT).show();
					}
				});
			} catch (IOException e) {
				e.printStackTrace();
			}
		}).start();
	}

	public void isPermissionGranted() {
		ActivityCompat.requestPermissions(Objects.requireNonNull(this),
				new String[]{Manifest.permission.READ_CONTACTS}, 1);
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
		sp.edit().putInt(KEY_PEOPLE_COUNT, peopleArray.size()).apply();
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
