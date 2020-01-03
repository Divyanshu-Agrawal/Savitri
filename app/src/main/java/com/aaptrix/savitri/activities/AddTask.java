package com.aaptrix.savitri.activities;

import com.aaptrix.savitri.R;
import com.aaptrix.savitri.adapter.AssignTaskPeopleAdapter;
import com.aaptrix.savitri.asyncclass.UploadTask;
import com.aaptrix.savitri.databeans.PeopleData;
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
import android.app.DatePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.button.MaterialButton;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.lang.reflect.Type;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Locale;
import java.util.Objects;

import static com.aaptrix.savitri.session.SharedPrefsNames.FLAG;
import static com.aaptrix.savitri.session.SharedPrefsNames.KEY_ORG_ID;
import static com.aaptrix.savitri.session.SharedPrefsNames.KEY_PEOPLE_ARRAY;
import static com.aaptrix.savitri.session.SharedPrefsNames.KEY_SESSION_ID;
import static com.aaptrix.savitri.session.SharedPrefsNames.KEY_USER_ID;
import static com.aaptrix.savitri.session.SharedPrefsNames.PEOPLE_PREFS;
import static com.aaptrix.savitri.session.SharedPrefsNames.TASK_ASSIGN;
import static com.aaptrix.savitri.session.SharedPrefsNames.TASK_DATE;
import static com.aaptrix.savitri.session.SharedPrefsNames.TASK_DETAIL;
import static com.aaptrix.savitri.session.SharedPrefsNames.TASK_NAME;
import static com.aaptrix.savitri.session.SharedPrefsNames.TASK_PEOPLE;
import static com.aaptrix.savitri.session.SharedPrefsNames.TASK_PREFS;
import static com.aaptrix.savitri.session.SharedPrefsNames.USER_PREFS;

public class AddTask extends AppCompatActivity {
	
	Toolbar toolbar;
	ProgressBar progressBar;
	TextView nameTitle, dateTitle, detailTitle;
	EditText name, dueDate, details;
	ListView listView;
	ArrayList<PeopleData> peopleArray = new ArrayList<>();
	AssignTaskPeopleAdapter adapter;
	Calendar myCalendar = Calendar.getInstance();
	DatePickerDialog.OnDateSetListener date;
	String strDate, strOrgId, strSessionId, strUserId;
	TextView noPeople;
	MaterialButton addTask;
	String type, taskName, taskDetail, taskDueDate, taskId;
	ArrayList<String> taskAssign;
	
	@SuppressLint("SetTextI18n")
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_add_task);
		toolbar = findViewById(R.id.toolbar);
		setSupportActionBar(toolbar);
		Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
		getSupportActionBar().setDisplayShowHomeEnabled(true);
		
		progressBar = findViewById(R.id.progress_bar);
		progressBar.bringToFront();
		
		name = findViewById(R.id.add_task_name);
		dueDate = findViewById(R.id.add_task_due_date);
		details = findViewById(R.id.add_task_details);
		listView = findViewById(R.id.add_task_listview);
		addTask = findViewById(R.id.add_task_btn);
		noPeople = findViewById(R.id.add_task_no_people);
		nameTitle = findViewById(R.id.name_title);
		dateTitle = findViewById(R.id.date_title);
		detailTitle = findViewById(R.id.detail_title);
		SharedPreferences sp = getSharedPreferences(USER_PREFS, Context.MODE_PRIVATE);
		strOrgId = sp.getString(KEY_ORG_ID, "");
		strSessionId = sp.getString(KEY_SESSION_ID, "");
		strUserId = sp.getString(KEY_USER_ID, "");
		if (checkConnection()) {
			fetchPeople(strOrgId, strSessionId, strUserId);
		} else {
			SharedPreferences preferences = getSharedPreferences(TASK_PREFS, Context.MODE_PRIVATE);
			GsonBuilder gsonBuilder = new GsonBuilder();
			Type type = new TypeToken<ArrayList<PeopleData>>() {}.getType();
			peopleArray = gsonBuilder.create().fromJson(preferences.getString(TASK_PEOPLE, ""), type);
			listItems();
		}
		
		type = getIntent().getStringExtra("type");
		
		name.setOnFocusChangeListener((v, hasFocus) -> {
			if (!hasFocus)
				hideKeyboard(v);
		});
		dueDate.setOnFocusChangeListener((v, hasFocus) -> {
			if (!hasFocus)
				hideKeyboard(v);
		});
		details.setOnFocusChangeListener((v, hasFocus) -> {
			if (!hasFocus)
				hideKeyboard(v);
		});
		
		if (type.equals("update")) {
			getSupportActionBar().setTitle("Update Task");
			taskId = getIntent().getStringExtra("id");
			taskName = getIntent().getStringExtra("name");
			taskDetail = getIntent().getStringExtra("desc");
			taskDueDate = getIntent().getStringExtra("date");
			taskAssign = getIntent().getStringArrayListExtra("assign");
			strDate = taskDueDate;
			name.setText(taskName);
			details.setText(taskDetail);
			FormatDate formatDate = new FormatDate(taskDueDate, "yyyy-MM-dd", "dd-MM-yyyy");
			String validityDate = formatDate.format();
			dueDate.setText(validityDate);
			addTask.setText("Update Task");
		}
		
		date = (view, year, monthOfYear, dayOfMonth) -> {
			myCalendar.set(Calendar.YEAR, year);
			myCalendar.set(Calendar.MONTH, monthOfYear);
			myCalendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
			String myFormat = "dd-MM-yyyy"; //In which you need put here
			SimpleDateFormat sdf = new SimpleDateFormat(myFormat, Locale.US);
			dueDate.setText(sdf.format(myCalendar.getTime()));
			myFormat = "yyyy-MM-dd";
			sdf = new SimpleDateFormat(myFormat, Locale.getDefault());
			strDate = sdf.format(myCalendar.getTime());
		};
		
		dueDate.setOnClickListener(v -> {
			DatePickerDialog datePickerDialog = new DatePickerDialog(this, date, myCalendar
					.get(Calendar.YEAR), myCalendar.get(Calendar.MONTH),
					myCalendar.get(Calendar.DAY_OF_MONTH));
			datePickerDialog.getDatePicker().setMinDate(Calendar.getInstance().getTimeInMillis() - 1000);
			datePickerDialog.show();
		});
		
		addTask.setOnClickListener(v -> {
			SharedPreferences preferences = getSharedPreferences(PEOPLE_PREFS, Context.MODE_PRIVATE);
			String peopleArray = preferences.getString(KEY_PEOPLE_ARRAY, "NA");
			if (TextUtils.isEmpty(name.getText().toString())) {
				name.setError("Please Enter Task Name");
				name.requestFocus();
			} else if (TextUtils.isEmpty(details.getText().toString())) {
				details.setError("Please Enter Task Details");
				details.requestFocus();
			} else if (TextUtils.isEmpty(strDate)) {
				dueDate.requestFocus();
				dueDate.setError("Please Select Due Date");
			} else {
				if (type.equals("add")) {
					if (checkConnection()) {
						UploadTask uploadTask = new UploadTask(this, progressBar, "online");
						uploadTask.execute(strOrgId, strUserId, strSessionId,
								name.getText().toString(), details.getText().toString(), strDate, peopleArray);
					} else {
						Toast.makeText(this, "Internet connection not available details will be saved when device is connected to internet", Toast.LENGTH_SHORT).show();
						SharedPreferences task_sp = getSharedPreferences(TASK_PREFS, Context.MODE_PRIVATE);
						SharedPreferences.Editor editor = task_sp.edit();
						editor.putBoolean(FLAG, true);
						editor.putString(TASK_NAME, name.getText().toString());
						editor.putString(TASK_DETAIL, details.getText().toString());
						editor.putString(TASK_DATE, strDate);
						editor.putString(TASK_ASSIGN, peopleArray);
						editor.apply();
						startActivity(new Intent(this, TasksActivity.class).setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP));
					}
				} else if (type.equals("update")) {
					UpdateTask uploadTask = new UpdateTask(this);
					uploadTask.execute(strOrgId, strUserId, strSessionId, name.getText().toString(),
							details.getText().toString(), strDate, peopleArray, taskId);
				}
			}
		});
	}

	private boolean checkConnection() {
		ConnectivityManager connec;
		connec = (ConnectivityManager) getSystemService(CONNECTIVITY_SERVICE);
		assert connec != null;
		return connec.getActiveNetworkInfo() != null && connec.getActiveNetworkInfo().isAvailable() && connec.getActiveNetworkInfo().isConnectedOrConnecting();
	}
	
	private void hideKeyboard(View view) {
		InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(Activity.INPUT_METHOD_SERVICE);
		inputMethodManager.hideSoftInputFromWindow(view.getWindowToken(), 0);
	}
	
	private void fetchPeople(String strOrgId, String  strSessionId, String strUserId) {
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
							Toast.makeText(this, "Your Session is expired please login again", Toast.LENGTH_SHORT).show();
							SharedPrefsManager.getInstance(this).logout();
							Intent intent = new Intent(this, AppLogin.class);
							intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
							startActivity(intent);
							finish();
						} else if (result.contains("{\"allMembers\":null}")) {
							noPeople.setVisibility(View.VISIBLE);
						} else {
							JSONObject jsonObject = new JSONObject(result);
							JSONArray jsonArray = jsonObject.getJSONArray("allMembers");
							for (int i = 0; i < jsonArray.length(); i++) {
								JSONObject jObject = jsonArray.getJSONObject(i);
								PeopleData data = new PeopleData();
								data.setUsers_details_id(jObject.getString("users_details_id"));
								data.setName(jObject.getString("users_name"));
								peopleArray.add(data);
							}
						}
					} catch (JSONException e) {
						e.printStackTrace();
					}
					listItems();
				});
			} catch (IOException e) {
				e.printStackTrace();
			}
		}).start();
	}
	
	private void listItems() {
		adapter = new AssignTaskPeopleAdapter(this, R.layout.list_assign_people, peopleArray);
		listView.setAdapter(adapter);
		listView.setEnabled(true);
		adapter.notifyDataSetChanged();
	}
	

	
	@SuppressLint("StaticFieldLeak")
	class UpdateTask extends AsyncTask<String, String, String> {
		
		@SuppressLint("StaticFieldLeak")
		private Context context;
		
		UpdateTask(Context context) {
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
			String userId = params[1];
			String sessionId = params[2];
			String name = params[3];
			String desc = params[4];
			String dueDate = params[5];
			String assignPeople = params[6];
			String taskId = params[7];
			
			Log.e("assign", assignPeople);
			
			try {
				HttpClient httpclient = new DefaultHttpClient();
				HttpPost httppost = new HttpPost(URLs.UPDATE_TASK);
				MultipartEntityBuilder entityBuilder = MultipartEntityBuilder.create();
				entityBuilder.setMode(HttpMultipartMode.BROWSER_COMPATIBLE);
				entityBuilder.addTextBody("org_details_id", orgId);
				entityBuilder.addTextBody("tasks_details_name", name);
				entityBuilder.addTextBody("tasks_details_desc", desc);
				entityBuilder.addTextBody("tasks_details_due_date", dueDate);
				entityBuilder.addTextBody("users_details_id", userId);
				entityBuilder.addTextBody("assign_users", assignPeople);
				entityBuilder.addTextBody("app_session_id", sessionId);
				entityBuilder.addTextBody("tasks_details_id", taskId);
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
						startActivity(new Intent(context, TasksActivity.class).setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP));
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
	
	@Override
	public void onBackPressed() {
		super.onBackPressed();
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		if (item.getItemId() == android.R.id.home) {
			onBackPressed();
		}
		return super.onOptionsItemSelected(item);
	}
}
