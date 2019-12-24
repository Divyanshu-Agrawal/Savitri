package com.aaptrix.savitri.activities;

import com.aaptrix.savitri.R;
import com.aaptrix.savitri.adapter.PlansAdapter;
import com.aaptrix.savitri.databeans.PlansData;
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

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ListView;
import android.widget.ProgressBar;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;

public class PlansActivity extends AppCompatActivity {
	
	Toolbar toolbar;
	ProgressBar progressBar;
	ListView listView;
	PlansAdapter adapter;
	ArrayList<PlansData> plansArray = new ArrayList<>();
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_plans);
		toolbar = findViewById(R.id.toolbar);
		progressBar = findViewById(R.id.progress_bar);
		listView = findViewById(R.id.plans_listview);
		setSupportActionBar(toolbar);
		getSupportActionBar().setDisplayShowHomeEnabled(true);
		getSupportActionBar().setDisplayHomeAsUpEnabled(true);
		progressBar.setVisibility(View.VISIBLE);
		fetchPlans();
	}
	
	private void fetchPlans() {
		new Thread(() -> {
			try {
				HttpClient httpclient = new DefaultHttpClient();
				HttpPost httppost = new HttpPost(URLs.ALL_PLANS);
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
						if (result.equals("null")) {
							progressBar.setVisibility(View.GONE);
						} else {
							Log.e("res", result);
							JSONObject jsonObject = new JSONObject(result);
							JSONArray jsonArray = jsonObject.getJSONArray("allPlans");
							for (int i = 0; i < jsonArray.length(); i++) {
								JSONObject jObject = jsonArray.getJSONObject(i);
								PlansData data = new PlansData();
								data.setId(jObject.getString("plan_id"));
								data.setName(jObject.getString("plan_name"));
								data.setComplianceLimit(jObject.getString("plan_compliance_limit"));
								data.setDataDownload(jObject.getString("plan_data_download"));
								data.setStorageCycle(jObject.getString("plan_data_storage_cycle"));
								data.setAlertByApp(jObject.getString("plan_alert_by_app"));
								data.setAlertByEmail(jObject.getString("plan_alert_by_email"));
								data.setAlertBySms(jObject.getString("plan_alert_by_sms"));
								data.setPlanCost(jObject.getString("plan_cost"));
								data.setUserLimit(jObject.getString("plan_user_assign_limit"));
								plansArray.add(data);
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
		adapter = new PlansAdapter(this, R.layout.list_plans, plansArray);
		listView.setAdapter(adapter);
		adapter.notifyDataSetChanged();
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
