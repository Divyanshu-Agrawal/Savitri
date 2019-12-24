package com.aaptrix.savitri.activities;

import com.aaptrix.savitri.R;
import com.aaptrix.savitri.adapter.TasksAdapter;
import com.aaptrix.savitri.databeans.TasksData;
import com.aaptrix.savitri.session.SharedPrefsManager;
import com.aaptrix.savitri.session.URLs;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import cz.msebera.android.httpclient.HttpEntity;
import cz.msebera.android.httpclient.HttpResponse;
import cz.msebera.android.httpclient.client.HttpClient;
import cz.msebera.android.httpclient.client.methods.HttpPost;
import cz.msebera.android.httpclient.entity.mime.HttpMultipartMode;
import cz.msebera.android.httpclient.entity.mime.MultipartEntityBuilder;
import cz.msebera.android.httpclient.impl.client.DefaultHttpClient;
import cz.msebera.android.httpclient.util.EntityUtils;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
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

import static com.aaptrix.savitri.session.SharedPrefsNames.KEY_ORG_ID;
import static com.aaptrix.savitri.session.SharedPrefsNames.KEY_SESSION_ID;
import static com.aaptrix.savitri.session.SharedPrefsNames.KEY_USER_ID;
import static com.aaptrix.savitri.session.SharedPrefsNames.KEY_USER_ROLE;
import static com.aaptrix.savitri.session.SharedPrefsNames.USER_PREFS;
import static java.security.AccessController.getContext;

public class TasksActivity extends AppCompatActivity {
	
	Toolbar toolbar;
	ProgressBar progressBar;
	TextView noTasks;
	ListView listView;
	SwipeRefreshLayout swipeRefreshLayout;
	TasksAdapter adapter;
	ArrayList<TasksData> tasksArray = new ArrayList<>();
	String strOrgId, strSessionId, strUserId, strUserRole;
	FloatingActionButton addTasks;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_tasks);
		toolbar = findViewById(R.id.toolbar);
		setSupportActionBar(toolbar);
		getSupportActionBar().setDisplayShowHomeEnabled(true);
		getSupportActionBar().setDisplayHomeAsUpEnabled(true);
		
		progressBar = findViewById(R.id.progress_bar);
		noTasks = findViewById(R.id.no_tasks);
		listView = findViewById(R.id.tasks_listview);
		swipeRefreshLayout = findViewById(R.id.swipe_refresh);
		addTasks = findViewById(R.id.add_tasks);
		
		SharedPreferences sp = getSharedPreferences(USER_PREFS, Context.MODE_PRIVATE);
		strOrgId = sp.getString(KEY_ORG_ID, "");
		strSessionId = sp.getString(KEY_SESSION_ID, "");
		strUserId = sp.getString(KEY_USER_ID, "");
		strUserRole = sp.getString(KEY_USER_ROLE, "");
		progressBar.setVisibility(View.VISIBLE);
		setTasks();
		swipeRefreshLayout.setOnRefreshListener(() -> {
			swipeRefreshLayout.setRefreshing(true);
			listView.setEnabled(false);
			tasksArray.clear();
			setTasks();
		});
		
		if (sp.getString(KEY_USER_ROLE, "").equals("Admin")) {
			addTasks.setVisibility(View.VISIBLE);
		} else {
			addTasks.setVisibility(View.GONE);
		}
		
		addTasks.setOnClickListener(v -> startActivity(new Intent(this, AddTask.class).putExtra("type", "add")));
	}
	
	private void setTasks() {
		if (checkConnection()) {
			noTasks.setVisibility(View.GONE);
			fetchTasks();
		} else {
			Toast.makeText(this, "Please connect to internet for better experience", Toast.LENGTH_SHORT).show();
			try {
				FileNotFoundException fe = new FileNotFoundException();
				File directory = this.getFilesDir();
				ObjectInputStream in = new ObjectInputStream(new FileInputStream(new File(directory, "tasksData")));
				String json = in.readObject().toString();
				in.close();
				JSONObject jsonObject = new JSONObject(json);
				JSONArray jsonArray = jsonObject.getJSONArray("alltasks");
				for (int i = 0; i < jsonArray.length(); i++) {
					JSONObject jObject = jsonArray.getJSONObject(i);
					TasksData data = new TasksData();
					data.setTaskId(jObject.getString("tasks_details_id"));
					data.setTaskName(jObject.getString("tasks_details_name"));
					data.setTaskDesc(jObject.getString("tasks_details_desc"));
					data.setTaskDueDate(jObject.getString("tasks_details_due_date"));
					data.setAssignedBy(jObject.getString("users_details_id"));
					data.setStatus(jObject.getString("tasks_assign_status"));
					tasksArray.add(data);
				}
				throw fe;
			} catch (Exception e) {
				e.printStackTrace();
			}
			if (tasksArray.size() == 0) {
				noTasks.setVisibility(View.VISIBLE);
				progressBar.setVisibility(View.GONE);
			} else {
				listItem();
			}
		}
	}
	
	private void fetchTasks() {
		new Thread(() -> {
			try {
				HttpClient httpclient = new DefaultHttpClient();
				HttpPost httppost = new HttpPost(URLs.ALL_TASKS);
				MultipartEntityBuilder entityBuilder = MultipartEntityBuilder.create();
				entityBuilder.setMode(HttpMultipartMode.BROWSER_COMPATIBLE);
				entityBuilder.addTextBody("org_details_id", strOrgId);
				entityBuilder.addTextBody("app_session_id", strSessionId);
				entityBuilder.addTextBody("users_type", strUserRole);
				entityBuilder.addTextBody("users_details_id", strUserId);
				HttpEntity entity = entityBuilder.build();
				httppost.setEntity(entity);
				HttpResponse response = httpclient.execute(httppost);
				HttpEntity httpEntity = response.getEntity();
				String result = EntityUtils.toString(httpEntity);
				Handler handler = new Handler(Looper.getMainLooper());
				handler.post(() -> {
					try {
						Log.e("res", result);
						if (result.contains("\"success\":false,\"msg\":\"Session Expire\"")) {
							progressBar.setVisibility(View.GONE);
							Toast.makeText(this, "Your Session is expired please login again", Toast.LENGTH_SHORT).show();
							SharedPrefsManager.getInstance(this).logout();
							Intent intent = new Intent(this, AppLogin.class);
							intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
							startActivity(intent);
							finish();
						} else if (result.contains("{\"allTasks\":null}")) {
							noTasks.setVisibility(View.VISIBLE);
							progressBar.setVisibility(View.GONE);
						} else {
							JSONObject jsonObject = new JSONObject(result);
							cacheJson(jsonObject);
							JSONArray jsonArray = jsonObject.getJSONArray("allTasks");
							for (int i = 0; i < jsonArray.length(); i++) {
								JSONObject jObject = jsonArray.getJSONObject(i);
								TasksData data = new TasksData();
								data.setTaskId(jObject.getString("tasks_details_id"));
								data.setTaskName(jObject.getString("tasks_details_name"));
								data.setTaskDesc(jObject.getString("tasks_details_desc"));
								data.setTaskDueDate(jObject.getString("tasks_details_due_date"));
								data.setAssignedBy(jObject.getString("users_details_id"));
								data.setStatus(jObject.getString("tasks_assign_status"));
								data.setAssignedByName(jObject.getString("users_name"));
								data.setAssignedTo(jObject.toString());
								tasksArray.add(data);
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
		swipeRefreshLayout.setRefreshing(false);
		adapter = new TasksAdapter(this, R.layout.list_renewal_status, tasksArray);
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
					out = new ObjectOutputStream(new FileOutputStream(new File(directory, "tasksData")));
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
		return super.onOptionsItemSelected(item);
	}
}
