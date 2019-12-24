package com.aaptrix.savitri.activities;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.aaptrix.savitri.R;
import com.aaptrix.savitri.adapter.AssignedPeopleAdapter;
import com.aaptrix.savitri.dialogs.AssignTaskDialog;
import com.aaptrix.savitri.session.FormatDate;
import com.aaptrix.savitri.session.SharedPrefsManager;
import com.aaptrix.savitri.session.URLs;
import com.google.android.material.button.MaterialButton;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Locale;
import java.util.Objects;

import androidx.appcompat.app.AlertDialog;
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

import static com.aaptrix.savitri.session.SharedPrefsNames.KEY_ORG_ID;
import static com.aaptrix.savitri.session.SharedPrefsNames.KEY_SESSION_ID;
import static com.aaptrix.savitri.session.SharedPrefsNames.KEY_USER_ID;
import static com.aaptrix.savitri.session.SharedPrefsNames.KEY_USER_NAME;
import static com.aaptrix.savitri.session.SharedPrefsNames.KEY_USER_ROLE;
import static com.aaptrix.savitri.session.SharedPrefsNames.USER_PREFS;

public class TaskDetail extends AppCompatActivity {
	
	Toolbar toolbar;
	TextView name, desc, dueDate, assignedBy, assignByTitle, assignToTitle, status;
	ListView assignList;
	String strName, strId, strDesc, strDueDate, strAssignedTo, strAssignedBy, strUserId,
			strSessionId, strOrgId, strAssignByName, strStatus, strUserName;
	SharedPreferences sp;
	MaterialButton completeTask, assignBtn;
	ArrayList<String> array = new ArrayList<>();
	LinearLayout completeTaskLayout;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_task_detail);
		sp = getSharedPreferences(USER_PREFS, Context.MODE_PRIVATE);
		strUserId = sp.getString(KEY_USER_ID, "");
		strOrgId = sp.getString(KEY_ORG_ID, "");
		strSessionId = sp.getString(KEY_SESSION_ID, "");
		strUserName = sp.getString(KEY_USER_NAME, "");
		strAssignedBy = getIntent().getStringExtra("assignedBy");
		strAssignedTo = getIntent().getStringExtra("assignedTo");
		strAssignByName = getIntent().getStringExtra("assignName");
		strName = getIntent().getStringExtra("name");
		strDesc = getIntent().getStringExtra("desc");
		strDueDate = getIntent().getStringExtra("due_date");
		strId = getIntent().getStringExtra("id");
		strStatus = getIntent().getStringExtra("status");
		toolbar = findViewById(R.id.toolbar);
		setSupportActionBar(toolbar);
		Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
		getSupportActionBar().setDisplayShowHomeEnabled(true);
		getSupportActionBar().setTitle(strName);
		
		name = findViewById(R.id.task_name);
		desc = findViewById(R.id.task_details);
		dueDate = findViewById(R.id.task_due_date);
		assignList = findViewById(R.id.assigned_to_listview);
		completeTask = findViewById(R.id.complete_task_btn);
		assignedBy = findViewById(R.id.task_assigned_by);
		assignByTitle = findViewById(R.id.assign_by_title);
		assignBtn = findViewById(R.id.assign_task_btn);
		assignToTitle = findViewById(R.id.assign_title);
		status = findViewById(R.id.task_status);
		completeTaskLayout = findViewById(R.id.task_layout);
		
		if (!strAssignedBy.equals(strUserId)) {
			assignByTitle.setVisibility(View.VISIBLE);
			assignedBy.setVisibility(View.VISIBLE);
			assignedBy.setText(strAssignByName);
			assignBtn.setVisibility(View.GONE);
		}
		
		if (sp.getString(KEY_USER_ROLE, "").equals("Admin")) {
			try {
				JSONObject jsonObject = new JSONObject(strAssignedTo);
				for (int i = 0; i < 5; i++) {
					JSONObject jObject = jsonObject.getJSONObject("assign_users_list" + i);
					array.add(jObject.getString("users_name"));
				}
				AssignedPeopleAdapter adapter = new AssignedPeopleAdapter(this, R.layout.list_assign_people, array);
				assignList.setAdapter(adapter);
				assignList.setEnabled(false);
			} catch (JSONException e) {
				e.printStackTrace();
				AssignedPeopleAdapter adapter = new AssignedPeopleAdapter(this, R.layout.list_assign_people, array);
				assignList.setAdapter(adapter);
				assignList.setEnabled(false);
			}
			if (array.size() == 0) {
				assignToTitle.setVisibility(View.GONE);
				assignList.setVisibility(View.GONE);
			}
		} else {
			assignToTitle.setVisibility(View.GONE);
			assignByTitle.setVisibility(View.VISIBLE);
			assignedBy.setVisibility(View.VISIBLE);
			assignedBy.setText(strAssignByName);
			assignList.setVisibility(View.GONE);
		}
		
		if (strStatus.equals("Completed")) {
			completeTaskLayout.setVisibility(View.GONE);
		}
		
		name.setText(strName);
		desc.setText(strDesc);
		status.setText(strStatus);
		FormatDate date = new FormatDate(strDueDate, "yyyy-MM-dd", "dd-MM-yyyy");
		dueDate.setText(date.format());
		
		completeTask.setOnClickListener(v -> new AlertDialog.Builder(this).setTitle("Are you sure you want to complete this task?")
				.setPositiveButton("Yes", (dialog, which) -> completeTask(strUserId, strId, strSessionId, strUserName, strName))
				.setNegativeButton("No", null)
				.show());
		
		assignBtn.setOnClickListener(v -> {
			AssignTaskDialog dialog = new AssignTaskDialog(this, strId, "task");
			Objects.requireNonNull(dialog.getWindow()).setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
			dialog.show();
		});
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		if (strAssignedBy.equals(strUserId)) {
			MenuInflater inflater = getMenuInflater();
			inflater.inflate(R.menu.toolbar_menu, menu);
			if (strStatus.equals("Completed")) {
				menu.findItem(R.id.edit).setEnabled(false);
			}
			return true;
		} else {
			return true;
		}
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		if (item.getItemId() == android.R.id.home) {
			onBackPressed();
		} else if (item.getItemId() == R.id.delete) {
			new AlertDialog.Builder(this)
					.setTitle("Are you sure you want to delete this task?")
					.setPositiveButton("Yes", (dialog, which) -> deleteTask(strOrgId, strId, strSessionId))
					.setNegativeButton("No", null)
					.show();
		} else if (item.getItemId() == R.id.edit) {
			Intent intent = new Intent(this, AddTask.class);
			intent.putExtra("type", "update");
			intent.putStringArrayListExtra("assign", array);
			intent.putExtra("name", strName);
			intent.putExtra("id", strId);
			intent.putExtra("date", strDueDate);
			intent.putExtra("desc", strDesc);
			startActivity(intent);
		}
		return super.onOptionsItemSelected(item);
	}
	
	@Override
	public void onBackPressed() {
		super.onBackPressed();
	}
	
	private void deleteTask(String orgId, String taskId, String sessionId) {
		new Thread(() -> {
			try {
				HttpClient httpclient = new DefaultHttpClient();
				HttpPost httppost = new HttpPost(URLs.DELETE_TASK);
				MultipartEntityBuilder entityBuilder = MultipartEntityBuilder.create();
				entityBuilder.setMode(HttpMultipartMode.BROWSER_COMPATIBLE);
				entityBuilder.addTextBody("org_details_id", orgId);
				entityBuilder.addTextBody("tasks_details_id", taskId);
				entityBuilder.addTextBody("app_session_id", sessionId);
				
				HttpEntity entity = entityBuilder.build();
				httppost.setEntity(entity);
				HttpResponse response = httpclient.execute(httppost);
				HttpEntity httpEntity = response.getEntity();
				String result = EntityUtils.toString(httpEntity);
				Handler handler = new Handler(Looper.getMainLooper());
				handler.post(() -> {
					if (result.contains("{\"success\":true}")) {
						Toast.makeText(this, "Deleted Successfully", Toast.LENGTH_SHORT).show();
						startActivity(new Intent(this, TasksActivity.class).setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP));
					} else if (result.contains("\"success\":false,\"msg\":\"Session Expire\"")) {
						Toast.makeText(this, "Your Session is expired please login again", Toast.LENGTH_SHORT).show();
						SharedPrefsManager.getInstance(this).logout();
						Intent intent = new Intent(this, AppLogin.class);
						intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
						startActivity(intent);
						finish();
					} else {
						Toast.makeText(this, "Error Occured", Toast.LENGTH_SHORT).show();
					}
				});
			} catch (IOException e) {
				e.printStackTrace();
			}
		}).start();
	}
	
	private void completeTask(String userId, String taskId, String sessionId, String userName, String taskName) {
		new Thread(() -> {
			try {
				HttpClient httpclient = new DefaultHttpClient();
				HttpPost httppost = new HttpPost(URLs.COMPLETE_TASK);
				MultipartEntityBuilder entityBuilder = MultipartEntityBuilder.create();
				entityBuilder.setMode(HttpMultipartMode.BROWSER_COMPATIBLE);
				entityBuilder.addTextBody("users_details_id", userId);
				entityBuilder.addTextBody("tasks_details_id", taskId);
				entityBuilder.addTextBody("tasks_assign_status", "Completed");
				entityBuilder.addTextBody("tasks_completed_date", new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(System.currentTimeMillis()));
				entityBuilder.addTextBody("app_session_id", sessionId);
				entityBuilder.addTextBody("task_name", taskName);
				entityBuilder.addTextBody("users_name", userName);
				entityBuilder.addTextBody("tasks_created_by", strAssignedBy);
				
				HttpEntity entity = entityBuilder.build();
				httppost.setEntity(entity);
				HttpResponse response = httpclient.execute(httppost);
				HttpEntity httpEntity = response.getEntity();
				String result = EntityUtils.toString(httpEntity);
				Handler handler = new Handler(Looper.getMainLooper());
				handler.post(() -> {
					Log.e("res", result);
					if (result.contains("{\"success\":true}")) {
						Toast.makeText(this, "Completed Successfully", Toast.LENGTH_SHORT).show();
						startActivity(new Intent(this, TasksActivity.class).setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP));
					} else if (result.contains("\"success\":false,\"msg\":\"Session Expire\"")) {
						Toast.makeText(this, "Your Session is expired please login again", Toast.LENGTH_SHORT).show();
						SharedPrefsManager.getInstance(this).logout();
						Intent intent = new Intent(this, AppLogin.class);
						intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
						startActivity(intent);
						finish();
					} else {
						Toast.makeText(this, "Error Occured", Toast.LENGTH_SHORT).show();
					}
				});
			} catch (IOException e) {
				e.printStackTrace();
			}
		}).start();
	}
}
