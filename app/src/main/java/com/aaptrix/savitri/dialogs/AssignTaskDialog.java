package com.aaptrix.savitri.dialogs;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.button.MaterialButton;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Objects;

import com.aaptrix.savitri.R;
import com.aaptrix.savitri.activities.AppLogin;
import com.aaptrix.savitri.activities.RenewalActivity;
import com.aaptrix.savitri.activities.TasksActivity;
import com.aaptrix.savitri.adapter.AssignTaskPeopleAdapter;
import com.aaptrix.savitri.databeans.PeopleData;
import com.aaptrix.savitri.session.SharedPrefsManager;
import com.aaptrix.savitri.session.URLs;
import androidx.annotation.NonNull;
import cz.msebera.android.httpclient.HttpEntity;
import cz.msebera.android.httpclient.HttpResponse;
import cz.msebera.android.httpclient.client.HttpClient;
import cz.msebera.android.httpclient.client.methods.HttpPost;
import cz.msebera.android.httpclient.entity.mime.HttpMultipartMode;
import cz.msebera.android.httpclient.entity.mime.MultipartEntityBuilder;
import cz.msebera.android.httpclient.impl.client.DefaultHttpClient;
import cz.msebera.android.httpclient.util.EntityUtils;

import static com.aaptrix.savitri.session.SharedPrefsNames.KEY_ORG_ID;
import static com.aaptrix.savitri.session.SharedPrefsNames.KEY_PEOPLE_ARRAY;
import static com.aaptrix.savitri.session.SharedPrefsNames.KEY_SESSION_ID;
import static com.aaptrix.savitri.session.SharedPrefsNames.KEY_USER_ID;
import static com.aaptrix.savitri.session.SharedPrefsNames.KEY_USER_NAME;
import static com.aaptrix.savitri.session.SharedPrefsNames.PEOPLE_PREFS;
import static com.aaptrix.savitri.session.SharedPrefsNames.USER_PREFS;

public class AssignTaskDialog extends Dialog {
	
	private Context context;
	private ProgressBar progressBar;
	private ListView listView;
	private ArrayList<PeopleData> peopleArray = new ArrayList<>();
	private TextView noPeople;
//	private ArrayList<String> taskAssign;
	private String strSession;
	private String taskId, type;
	
	public AssignTaskDialog(@NonNull Context context/*, ArrayList<String> taskAssign*/, String taskId, String type) {
		super(context);
		this.context = context;
//		this.taskAssign = taskAssign;
		this.taskId = taskId;
		this.type = type;
	}
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.dialog_assign_people);
		Objects.requireNonNull(this.getWindow()).setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
		
		listView = findViewById(R.id.assign_people_listview);
		noPeople = findViewById(R.id.no_people);
		progressBar = findViewById(R.id.progress_bar);
		MaterialButton assign = findViewById(R.id.assign_btn);
		MaterialButton cancel = findViewById(R.id.cancel_btn);
		SharedPreferences sp = context.getSharedPreferences(USER_PREFS, Context.MODE_PRIVATE);
		String strOrgId = sp.getString(KEY_ORG_ID, "");
		strSession = sp.getString(KEY_SESSION_ID, "");
		String strUserId = sp.getString(KEY_USER_ID, "");
		String strUserName = sp.getString(KEY_USER_NAME, "");
		fetchPeople(strOrgId, strSession, strUserId);
		
		assign.setOnClickListener(v -> {
			SharedPreferences preferences = context.getSharedPreferences(PEOPLE_PREFS, Context.MODE_PRIVATE);
			String peopleArray = preferences.getString(KEY_PEOPLE_ARRAY, "NA");
			if (type.equals("task")) {
				AssignTask assignTask = new AssignTask(context);
				assignTask.execute(strSession, taskId, peopleArray);
			} else if (type.equals("renewal")) {
				AssignRenewal assignRenewal = new AssignRenewal(context);
				assignRenewal.execute(strSession, taskId, peopleArray, strUserId, strUserName);
			}
		});
		
		cancel.setOnClickListener(v -> dismiss());
		
	}
	
	private void fetchPeople(String strOrgId, String  strSessionId, String strUserId) {
		progressBar.setVisibility(View.VISIBLE);
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
						progressBar.setVisibility(View.GONE);
						if (result.contains("\"success\":false,\"msg\":\"Session Expire\"")){
							Toast.makeText(context, "Your Session is expired please login again", Toast.LENGTH_SHORT).show();
							SharedPrefsManager.getInstance(context).logout();
							Intent intent = new Intent(context, AppLogin.class);
							intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
							context.startActivity(intent);
						} else if (result.contains("{\"allMembers\":null}")) {
							noPeople.setVisibility(View.VISIBLE);
						} else {
							Log.e("res", result);
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
		AssignTaskPeopleAdapter adapter = new AssignTaskPeopleAdapter(context, R.layout.list_assign_people, peopleArray);
		listView.setAdapter(adapter);
		listView.setEnabled(true);
		adapter.notifyDataSetChanged();
	}
	
	@SuppressLint("StaticFieldLeak")
	class AssignTask extends AsyncTask<String, String, String> {
		
		@SuppressLint("StaticFieldLeak")
		private Context context;
		
		AssignTask(Context context) {
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
			
			String sessionId = params[0];
			String id = params[1];
			String assignPeople = params[2];
			
			try {
				HttpClient httpclient = new DefaultHttpClient();
				HttpPost httppost = new HttpPost(URLs.ASSIGN_TASK);
				MultipartEntityBuilder entityBuilder = MultipartEntityBuilder.create();
				entityBuilder.setMode(HttpMultipartMode.BROWSER_COMPATIBLE);
				entityBuilder.addTextBody("tasks_details_id", id);
				entityBuilder.addTextBody("assign_users", assignPeople);
				entityBuilder.addTextBody("app_session_id", sessionId);
				HttpEntity entity = entityBuilder.build();
				httppost.setEntity(entity);
				HttpResponse response = httpclient.execute(httppost);
				HttpEntity httpEntity = response.getEntity();
				return EntityUtils.toString(httpEntity);
			} catch (Exception e) {
				e.printStackTrace();
			}
			return null;
		}
		
		@Override
		protected void onPostExecute(String result) {
			Log.e("result", String.valueOf(result));
			if (result != null) {
				try {
					JSONObject jsonObject = new JSONObject(result);
					if (jsonObject.getBoolean("success")) {
						Toast.makeText(context, "Assigned Successfully", Toast.LENGTH_SHORT).show();
						context.startActivity(new Intent(context, TasksActivity.class).setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP));
					} else if (result.contains("\"success\":false,\"msg\":\"Session Expire\"")) {
						progressBar.setVisibility(View.GONE);
						Toast.makeText(context, "Your Session is expired please login again", Toast.LENGTH_SHORT).show();
						SharedPrefsManager.getInstance(context).logout();
						Intent intent = new Intent(context, AppLogin.class);
						intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
						context.startActivity(intent);
					} else {
						progressBar.setVisibility(View.GONE);
						Toast.makeText(context, "Error Occured. Please try again", Toast.LENGTH_SHORT).show();
					}
				} catch (JSONException e) {
					e.printStackTrace();
				}
			} else {
				progressBar.setVisibility(View.GONE);
				Toast.makeText(context, "Error Occured. Please try again", Toast.LENGTH_SHORT).show();
			}
			super.onPostExecute(result);
		}
	}
	
	@SuppressLint("StaticFieldLeak")
	class AssignRenewal extends AsyncTask<String, String, String> {
		
		@SuppressLint("StaticFieldLeak")
		private Context context;
		
		AssignRenewal(Context context) {
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
			
			String sessionId = params[0];
			String id = params[1];
			String assignPeople = params[2];
			String userId = params[3];
			String name = params[4];
			
			Log.e("assign", assignPeople);
			Log.e("session id", sessionId);
			Log.e("task id", id);
			Log.e("userid", userId);
			
			try {
				HttpClient httpclient = new DefaultHttpClient();
				HttpPost httppost = new HttpPost(URLs.ASSIGN_RENEWAL);
				MultipartEntityBuilder entityBuilder = MultipartEntityBuilder.create();
				entityBuilder.setMode(HttpMultipartMode.BROWSER_COMPATIBLE);
				entityBuilder.addTextBody("compliance_id", id);
				entityBuilder.addTextBody("assign_users", assignPeople);
				entityBuilder.addTextBody("app_session_id", sessionId);
				entityBuilder.addTextBody("users_details_id", userId);
				entityBuilder.addTextBody("users_name", name);
				HttpEntity entity = entityBuilder.build();
				httppost.setEntity(entity);
				HttpResponse response = httpclient.execute(httppost);
				HttpEntity httpEntity = response.getEntity();
				return EntityUtils.toString(httpEntity);
			} catch (Exception e) {
				e.printStackTrace();
			}
			return null;
		}
		
		@Override
		protected void onPostExecute(String result) {
			Log.e("result", String.valueOf(result));
			if (result != null) {
				try {
					JSONObject jsonObject = new JSONObject(result);
					if (jsonObject.getBoolean("success")) {
						Toast.makeText(context, "Assigned Successfully", Toast.LENGTH_SHORT).show();
						context.startActivity(new Intent(context, RenewalActivity.class).setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP));
					} else if (result.contains("\"success\":false,\"msg\":\"Session Expire\"")) {
						progressBar.setVisibility(View.GONE);
						Toast.makeText(context, "Your Session is expired please login again", Toast.LENGTH_SHORT).show();
						SharedPrefsManager.getInstance(context).logout();
						Intent intent = new Intent(context, AppLogin.class);
						intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
						context.startActivity(intent);
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