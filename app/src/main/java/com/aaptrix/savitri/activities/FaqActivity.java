package com.aaptrix.savitri.activities;

import com.aaptrix.savitri.R;
import com.aaptrix.savitri.adapter.FaqAdapter;
import com.aaptrix.savitri.databeans.FaqData;
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
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.snackbar.Snackbar;

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

import static com.aaptrix.savitri.session.SharedPrefsNames.KEY_SESSION_ID;
import static com.aaptrix.savitri.session.SharedPrefsNames.USER_PREFS;
import static java.security.AccessController.getContext;

public class FaqActivity extends AppCompatActivity {
	
	Toolbar toolbar;
	ProgressBar progressBar;
	TextView noFaq;
	ListView listView;
	FaqAdapter adapter;
	ArrayList<FaqData> faqArray = new ArrayList<>();
	String strSessionId;
	SwipeRefreshLayout swipeRefreshLayout;
	RelativeLayout relativeLayout;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_faq);
		toolbar = findViewById(R.id.toolbar);
		setSupportActionBar(toolbar);
		Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
		getSupportActionBar().setDisplayShowHomeEnabled(true);
		progressBar = findViewById(R.id.progress_bar);
		noFaq = findViewById(R.id.no_faq);
		listView = findViewById(R.id.faq_listview);
		relativeLayout = findViewById(R.id.faq_layout);
		swipeRefreshLayout = findViewById(R.id.swipe_refresh);
		SharedPreferences sp = getSharedPreferences(USER_PREFS, Context.MODE_PRIVATE);
		strSessionId = sp.getString(KEY_SESSION_ID, "");
		progressBar.setVisibility(View.VISIBLE);
		setFaq();
		swipeRefreshLayout.setOnRefreshListener(() -> {
			faqArray.clear();
			progressBar.setVisibility(View.VISIBLE);
			swipeRefreshLayout.setRefreshing(true);
			listView.setEnabled(false);
			setFaq();
		});
	}
	
	private void setFaq() {
		if (checkConnection()) {
			noFaq.setVisibility(View.GONE);
			fetchFaq();
		} else {
			Snackbar snackbar = Snackbar.make(relativeLayout, "No Internet Connection", Snackbar.LENGTH_INDEFINITE)
					.setActionTextColor(Color.WHITE)
					.setAction("Ok", null);
			snackbar.show();
			try {
				FileNotFoundException fe = new FileNotFoundException();
				File directory = this.getFilesDir();
				ObjectInputStream in = new ObjectInputStream(new FileInputStream(new File(directory, "faqData")));
				String json = in.readObject().toString();
				in.close();
				JSONObject jsonObject = new JSONObject(json);
				JSONArray jsonArray = jsonObject.getJSONArray("allFaq");
				for (int i = 0; i < jsonArray.length(); i++) {
					JSONObject jObject = jsonArray.getJSONObject(i);
					FaqData data = new FaqData();
					data.setQuestion(jObject.getString("faq_ques"));
					data.setAnswer(jObject.getString("faq_ans"));
					faqArray.add(data);
				}
				throw fe;
			} catch (Exception e) {
				e.printStackTrace();
			}
			if (faqArray.size() == 0) {
				noFaq.setVisibility(View.VISIBLE);
				progressBar.setVisibility(View.GONE);
			} else {
				listItem();
			}
		}
	}
	
	private void fetchFaq() {
		new Thread(() -> {
			try {
				HttpClient httpclient = new DefaultHttpClient();
				HttpPost httppost = new HttpPost(URLs.ALL_FAQ);
				MultipartEntityBuilder entityBuilder = MultipartEntityBuilder.create();
				entityBuilder.setMode(HttpMultipartMode.BROWSER_COMPATIBLE);
				entityBuilder.addTextBody("app_session_id", strSessionId);
				HttpEntity entity = entityBuilder.build();
				httppost.setEntity(entity);
				HttpResponse response = httpclient.execute(httppost);
				HttpEntity httpEntity = response.getEntity();
				String result = EntityUtils.toString(httpEntity);
				Handler handler = new Handler(Looper.getMainLooper());
				handler.post(() -> {
					try {
						Log.e("res", result);
						if (result.contains("\"success\":false,\"msg\":\"Session Expire\"")){
							progressBar.setVisibility(View.GONE);
							Toast.makeText(this, "Your Session is expired please login again", Toast.LENGTH_SHORT).show();
							SharedPrefsManager.getInstance(this).logout();
							Intent intent = new Intent(this, AppLogin.class);
							intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
							startActivity(intent);
							finish();
						} else if (result.contains("{\"allFaq\":null}")) {
							noFaq.setVisibility(View.VISIBLE);
							progressBar.setVisibility(View.GONE);
						} else {
							JSONObject jsonObject = new JSONObject(result);
							cacheJson(jsonObject);
							JSONArray jsonArray = jsonObject.getJSONArray("allFaq");
							for (int i = 0; i < jsonArray.length(); i++) {
								JSONObject jObject = jsonArray.getJSONObject(i);
								FaqData data = new FaqData();
								data.setQuestion(jObject.getString("faq_ques"));
								data.setAnswer(jObject.getString("faq_ans"));
								faqArray.add(data);
							}
						}
					} catch (JSONException e) {
						e.printStackTrace();
						try {
							FileNotFoundException fe = new FileNotFoundException();
							File directory = this.getFilesDir();
							ObjectInputStream in = new ObjectInputStream(new FileInputStream(new File(directory, "faqData")));
							String json = in.readObject().toString();
							in.close();
							JSONObject jsonObject = new JSONObject(json);
							JSONArray jsonArray = jsonObject.getJSONArray("allFaq");
							for (int i = 0; i < jsonArray.length(); i++) {
								JSONObject jObject = jsonArray.getJSONObject(i);
								FaqData data = new FaqData();
								data.setQuestion(jObject.getString("faq_ques"));
								data.setAnswer(jObject.getString("faq_ans"));
								faqArray.add(data);
							}
							Snackbar snackbar = Snackbar.make(relativeLayout, "Network error showing offline data", Snackbar.LENGTH_LONG)
									.setActionTextColor(Color.WHITE)
									.setAction("Ok", null);
							snackbar.show();
							throw fe;
						} catch (Exception e1) {
							e1.printStackTrace();
						}
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
		adapter = new FaqAdapter(this, R.layout.list_faq, faqArray);
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
					out = new ObjectOutputStream(new FileOutputStream(new File(directory, "faqData")));
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
		connec = (ConnectivityManager)getSystemService(CONNECTIVITY_SERVICE);
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
