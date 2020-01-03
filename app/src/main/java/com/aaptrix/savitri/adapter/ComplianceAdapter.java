package com.aaptrix.savitri.adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.util.ArrayList;

import com.aaptrix.savitri.R;
import com.aaptrix.savitri.activities.AppLogin;
import com.aaptrix.savitri.activities.ComplianceDetails;
import com.aaptrix.savitri.activities.CompliancesActivity;
import com.aaptrix.savitri.activities.UpdateCompliance;
import com.aaptrix.savitri.databeans.ComplianceData;
import com.aaptrix.savitri.session.FormatDate;
import com.aaptrix.savitri.session.SharedPrefsManager;
import com.aaptrix.savitri.session.URLs;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
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
import static com.aaptrix.savitri.session.SharedPrefsNames.KEY_USER_ROLE;
import static com.aaptrix.savitri.session.SharedPrefsNames.USER_PREFS;

public class ComplianceAdapter extends ArrayAdapter<ComplianceData> {
	
	private Context context;
	private int resource;
	private ArrayList<ComplianceData> object;
	
	class ViewHolder {
		
		TextView name, issueAuth, validity;
		ImageButton more;
		
		ViewHolder(@NonNull View view) {
			name = view.findViewById(R.id.compliance_name);
			issueAuth = view.findViewById(R.id.compliance_issue_authority);
			validity = view.findViewById(R.id.compliance_validity);
			more = view.findViewById(R.id.compliance_more);
		}
	}
	
	public ComplianceAdapter(Context context, int resource, ArrayList<ComplianceData> object) {
		super(context, resource, object);
		this.context = context;
		this.resource = resource;
		this.object = object;
	}
	
	@SuppressLint({"ViewHolder", "SetTextI18n"})
	@NonNull
	@Override
	public View getView(int position, View view, @NonNull ViewGroup parent) {
		LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		assert inflater != null;
		view = inflater.inflate(resource, null);
		ViewHolder holder = new ViewHolder(view);
		ComplianceData data = object.get(position);
		
		if (data != null) {
			holder.name.setText(data.getName());
			
			if (data.getIssueAuth().equals("Other")) {
				holder.issueAuth.setText(data.getOtherAuth());
			} else {
				holder.issueAuth.setText(data.getIssueAuth());
			}
			
			FormatDate formatDate = new FormatDate(data.getValidTo(), "yyyy-MM-dd", "dd-MM-yyyy");
			String validityDate = formatDate.format();
			holder.validity.setText("Valid Till : " + validityDate);
			
			SharedPreferences sp = context.getSharedPreferences(USER_PREFS, Context.MODE_PRIVATE);
			String orgId = sp.getString(KEY_ORG_ID, "");
			String userRole = sp.getString(KEY_USER_ROLE, "");
			String sessionId = sp.getString(KEY_SESSION_ID, "");
			
			if (userRole.equals("Admin")) {
				holder.more.setVisibility(View.VISIBLE);
			} else {
				holder.more.setVisibility(View.GONE);
			}
			
			view.setOnClickListener(v -> {
				if (data.getId() != null) {
					Intent intent = new Intent(context, ComplianceDetails.class);
					intent.putExtra("name", data.getName());
					intent.putExtra("refNo", data.getRefNo());
					if (data.getIssueAuth().equals("Other")) {
						intent.putExtra("issueAuth", data.getOtherAuth());
					} else {
						intent.putExtra("issueAuth", data.getIssueAuth());
					}
					intent.putExtra("id", data.getId());
					intent.putExtra("validFrom", data.getValidfrom());
					intent.putExtra("validTo", data.getValidTo());
					intent.putExtra("addedOn", data.getAddedDate());
					intent.putExtra("certificate", data.getCertificate());
					intent.putExtra("notes", data.getNotes());
					context.startActivity(intent);
				} else {
					Toast.makeText(context, "Compliance not uploaded yet please connect to internet", Toast.LENGTH_SHORT).show();
				}
			});
			
			holder.more.setOnClickListener(v -> {
				PopupMenu popup = new PopupMenu(v.getContext(), v);
				popup.getMenuInflater().inflate(R.menu.overflow_menu, popup.getMenu());
				popup.show();
				popup.setOnMenuItemClickListener(item -> {
					switch (item.getItemId()) {
						case R.id.edit_compliance:
							if (data.getId() != null) {
								Intent intent = new Intent(context, UpdateCompliance.class);
								intent.putExtra("name", data.getName());
								intent.putExtra("refNo", data.getRefNo());
								if (data.getIssueAuth().equals("Other")) {
									intent.putExtra("issueAuth", data.getOtherAuth());
								} else {
									intent.putExtra("issueAuth", data.getIssueAuth());
								}
								intent.putExtra("id", data.getId());
								intent.putExtra("validFrom", data.getValidfrom());
								intent.putExtra("validTo", data.getValidTo());
								intent.putExtra("notes", data.getNotes());
								context.startActivity(intent);
							} else {
								Toast.makeText(context, "Compliance not uploaded yet please connect to internet", Toast.LENGTH_SHORT).show();
							}
							break;
						case R.id.delete_compliance:
							if (data.getId() != null) {
								new AlertDialog.Builder(context)
										.setMessage("Are you sure you want to delete this compliance?")
										.setPositiveButton("Yes", (dialog, which) -> deleteCompliance(orgId, data.getId(), sessionId))
										.setNegativeButton("No", null)
										.show();
							} else {
								Toast.makeText(context, "Compliance not uploaded yet please connect to internet", Toast.LENGTH_SHORT).show();
							}
							break;
					}
					return true;
				});
			});
		}
		
		return view;
	}
	
	private void deleteCompliance(String orgId, String complianceId, String sessionId) {
		new Thread(() -> {
			try {
				HttpClient httpclient = new DefaultHttpClient();
				HttpPost httppost = new HttpPost(URLs.DELETE_COMPLIANCE);
				MultipartEntityBuilder entityBuilder = MultipartEntityBuilder.create();
				entityBuilder.setMode(HttpMultipartMode.BROWSER_COMPATIBLE);
				entityBuilder.addTextBody("org_details_id", orgId);
				entityBuilder.addTextBody("compliance_id", complianceId);
				entityBuilder.addTextBody("app_session_id", sessionId);
				
				HttpEntity entity = entityBuilder.build();
				httppost.setEntity(entity);
				HttpResponse response = httpclient.execute(httppost);
				HttpEntity httpEntity = response.getEntity();
				String result = EntityUtils.toString(httpEntity);
				Handler handler = new Handler(Looper.getMainLooper());
				handler.post(() -> {
					if (result.contains("{\"success\":true}")) {
						Toast.makeText(context, "Deleted Successfully", Toast.LENGTH_SHORT).show();
						context.startActivity(new Intent(context, CompliancesActivity.class).setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP));
					} else if (result.contains("\"success\":false,\"msg\":\"Session Expire\"")){
						Toast.makeText(context, "Your Session is expired please login again", Toast.LENGTH_SHORT).show();
						SharedPrefsManager.getInstance(context).logout();
						Intent intent = new Intent(context, AppLogin.class);
						intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
						context.startActivity(intent);
					} else {
						Toast.makeText(context, "Error Occured", Toast.LENGTH_SHORT).show();
					}
				});
			} catch (IOException e) {
				e.printStackTrace();
			}
		}).start();
	}
	
	@Override
	public int getViewTypeCount() {
		return 1;
	}
	@Override
	public int getItemViewType(int position) {
		return position;
	}
}
