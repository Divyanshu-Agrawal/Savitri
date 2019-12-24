package com.aaptrix.savitri.fragment;


import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.aaptrix.savitri.activities.Dashboard;
import com.aaptrix.savitri.activities.PlansActivity;
import com.google.android.material.snackbar.Snackbar;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Objects;

import com.aaptrix.savitri.R;
import com.aaptrix.savitri.activities.AppLogin;
import com.aaptrix.savitri.activities.HistoryDetails;
import com.aaptrix.savitri.adapter.HistoryAdapter;
import com.aaptrix.savitri.databeans.ComplianceData;
import com.aaptrix.savitri.session.SharedPrefsManager;
import com.aaptrix.savitri.session.URLs;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
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
import static com.aaptrix.savitri.session.SharedPrefsNames.KEY_STORAGE_CYCLE;
import static com.aaptrix.savitri.session.SharedPrefsNames.USER_PREFS;
import static android.content.Context.CONNECTIVITY_SERVICE;

/**
 * A simple {@link Fragment} subclass.
 */
public class HistoryFragment extends Fragment {
	
	private ListView listView;
	private TextView noHistory;
	private ProgressBar progressBar;
	private String strOrgId, strSessionId, cycleCount;
	private ArrayList<ComplianceData> complianceArray = new ArrayList<>();
	private ArrayList<ComplianceData> secondArray = new ArrayList<>();
	private ArrayList<ArrayList<ComplianceData>> sortedArray = new ArrayList<>();
	private ArrayList<ComplianceData> viewArray = new ArrayList<>();
	private Context context;
	private FrameLayout frameLayout;
	
	public HistoryFragment() {
		// Required empty public constructor
	}
	
	@Override
	public void onAttach(@NonNull Context context) {
		super.onAttach(context);
		this.context = context;
	}
	
	@Override
	public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
							 Bundle savedInstanceState) {
		Bundle bundle = getArguments();
		assert bundle != null;
		if (bundle.getString("visibility").equals("yes")) {
			View view = inflater.inflate(R.layout.fragment_history, container, false);
			listView = view.findViewById(R.id.history_listview);
			noHistory = view.findViewById(R.id.no_history);
			progressBar = view.findViewById(R.id.progress_bar);
			frameLayout = view.findViewById(R.id.history_layout);
			SharedPreferences sp = Objects.requireNonNull(context).getSharedPreferences(USER_PREFS, Context.MODE_PRIVATE);
			strOrgId = sp.getString(KEY_ORG_ID, "");
			strSessionId = sp.getString(KEY_SESSION_ID, "");
			cycleCount = bundle.getString("cycleCount");
			if (checkConnection()) {
				fetchHistory();
			} else {
				Snackbar snackbar = Snackbar.make(frameLayout, "No Internet Connection", Snackbar.LENGTH_LONG)
						.setActionTextColor(Color.WHITE)
						.setAction("Ok", null);
				snackbar.show();
			}
			return view;
		} else {
			return null;
		}
	}
	
	private void fetchHistory() {
		progressBar.setVisibility(View.VISIBLE);
		new Thread(() -> {
			try {
				HttpClient httpclient = new DefaultHttpClient();
				HttpPost httppost = new HttpPost(URLs.ALL_HISTORY);
				MultipartEntityBuilder entityBuilder = MultipartEntityBuilder.create();
				entityBuilder.setMode(HttpMultipartMode.BROWSER_COMPATIBLE);
				entityBuilder.addTextBody("org_details_id", strOrgId);
				entityBuilder.addTextBody("app_session_id", strSessionId);
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
							Toast.makeText(context, "Your Session is expired please login again", Toast.LENGTH_SHORT).show();
							SharedPrefsManager.getInstance(context).logout();
							Intent intent = new Intent(context, AppLogin.class);
							intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
							startActivity(intent);
						} else if (result.equals("null")) {
							noHistory.setVisibility(View.VISIBLE);
							progressBar.setVisibility(View.GONE);
						} else {
							Log.e("res", result);
							JSONObject jsonObject = new JSONObject(result);
							JSONArray jsonArray = jsonObject.getJSONArray("complianceHistory");
							for (int i = 0; i < jsonArray.length(); i++) {
								JSONObject jObject = jsonArray.getJSONObject(i);
								ComplianceData data = new ComplianceData();
								data.setId(jObject.getString("compliance_id"));
								data.setName(jObject.getString("compliance_name"));
								data.setIssueAuth(jObject.getString("compliance_issuing_auth"));
								data.setNotes(jObject.getString("compliance_notes"));
								data.setOtherAuth(jObject.getString("compliance_issuing_auth_other"));
								data.setRefNo(jObject.getString("compliance_reference_no"));
								data.setRenewCount(jObject.getString("compliance_renew_count"));
								data.setValidfrom(jObject.getString("compliance_valid_from"));
								data.setValidTo(jObject.getString("compliance_valid_upto"));
								data.setCertificate(jObject.getString("compliance_certificates"));
								complianceArray.add(data);
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
		secondArray.addAll(complianceArray);
		for (int i = 0; i < complianceArray.size(); i++) {
			ArrayList<ComplianceData> arrayList = new ArrayList<>();
			if (complianceArray.get(i).getRenewCount().equals("1")) {
				for (int j = 0; j < secondArray.size(); j++) {
					if (complianceArray.get(i).getId().equals(secondArray.get(j).getId())) {
						arrayList.add(secondArray.get(j));
					}
				}
			}
			if (!arrayList.isEmpty()) {
				sortedArray.add(arrayList);
			}
		}
		
		for (int i = 0; i < sortedArray.size(); i++) {
			if (sortedArray.get(i).size() == Integer.parseInt(cycleCount)) {
				for (int j = 0; j < sortedArray.get(i).size(); j++) {
					if (sortedArray.get(i).get(j).getRenewCount().equals("1")) {
						viewArray.add(sortedArray.get(i).get(j));
					}
				}
				HistoryAdapter adapter = new HistoryAdapter(context, R.layout.list_compliance_history, viewArray);
				listView.setAdapter(adapter);
				listView.setEnabled(true);
				adapter.notifyDataSetChanged();
				noHistory.setVisibility(View.GONE);
			}
		}
		
		progressBar.setVisibility(View.GONE);
		
		listView.setOnItemClickListener((parent, view, position, id) -> {
			ArrayList<ComplianceData> comArray = new ArrayList<>();
			Intent intent = new Intent(getContext(), HistoryDetails.class);
			intent.putExtra("id", viewArray.get(position).getId());
			intent.putExtra("name", viewArray.get(position).getName());
			intent.putExtra("refno", viewArray.get(position).getRefNo());
			intent.putExtra("notes", viewArray.get(position).getNotes());
			if (viewArray.get(position).getIssueAuth().equals("Other")) {
				intent.putExtra("issueAuth", viewArray.get(position).getOtherAuth());
			} else {
				intent.putExtra("issueAuth", viewArray.get(position).getIssueAuth());
			}
			for (int i = 0; i < sortedArray.size(); i++) {
				for (int j = 0; j < sortedArray.get(i).size(); j++) {
					if (sortedArray.get(i).get(j).getId().equals(viewArray.get(position).getId())) {
						comArray.add(sortedArray.get(i).get(j));
					}
				}
			}
			Gson gson = new GsonBuilder().create();
			JsonArray array = gson.toJsonTree(comArray).getAsJsonArray();
			Log.e("array", array.toString());
			intent.putExtra("complianceArray", array.toString());
			intent.putExtra("count", cycleCount);
			startActivity(intent);
		});
	}
	
	public boolean checkConnection() {
		ConnectivityManager connec;
		connec = (ConnectivityManager) context.getSystemService(CONNECTIVITY_SERVICE);
		assert connec != null;
		return connec.getActiveNetworkInfo() != null && connec.getActiveNetworkInfo().isAvailable() && connec.getActiveNetworkInfo().isConnectedOrConnecting();
	}
	
}
