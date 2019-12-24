package com.aaptrix.savitri.activities;

import com.aaptrix.savitri.R;
import com.aaptrix.savitri.adapter.CertificateAdapter;
import com.aaptrix.savitri.session.FormatDate;
import com.aaptrix.savitri.session.SharedPrefsManager;
import com.aaptrix.savitri.session.URLs;
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

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Objects;

import static com.aaptrix.savitri.session.SharedPrefsNames.KEY_ORG_ID;
import static com.aaptrix.savitri.session.SharedPrefsNames.KEY_SESSION_ID;
import static com.aaptrix.savitri.session.SharedPrefsNames.KEY_USER_ROLE;
import static com.aaptrix.savitri.session.SharedPrefsNames.USER_PREFS;

public class ComplianceDetails extends AppCompatActivity {
	
	TextView name, notes, issueAuth, addedOn, validFrom, validTo, refNo;
	ListView certificateList;
	Toolbar toolbar;
	ArrayList<String> certUrl = new ArrayList<>();
	String strName, strNotes, strIssueAuth, strAddedOn, strValidFrom, strValidTo, strRefno, strCertificate, strId;
	SharedPreferences sp;
	String orgId, sessionId;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_compliance_details);
		toolbar = findViewById(R.id.toolbar);
		setSupportActionBar(toolbar);
		Objects.requireNonNull(getSupportActionBar()).setDisplayShowHomeEnabled(true);
		getSupportActionBar().setDisplayHomeAsUpEnabled(true);
		name = findViewById(R.id.compliance_name);
		notes = findViewById(R.id.compliance_notes);
		issueAuth = findViewById(R.id.compliance_issue_auth);
		addedOn = findViewById(R.id.compliance_added_on);
		validFrom = findViewById(R.id.compliance_valid_from);
		validTo = findViewById(R.id.compliance_valid_upto);
		refNo = findViewById(R.id.compliance_ref_no);
		certificateList = findViewById(R.id.certificate_listview);
		sp = getSharedPreferences(USER_PREFS, Context.MODE_PRIVATE);
		orgId = sp.getString(KEY_ORG_ID, "");
		sessionId = sp.getString(KEY_SESSION_ID, "");
		
		strName = getIntent().getStringExtra("name");
		strNotes = getIntent().getStringExtra("notes");
		strIssueAuth = getIntent().getStringExtra("issueAuth");
		strId = getIntent().getStringExtra("id");
		strAddedOn = getIntent().getStringExtra("addedOn");
		strValidFrom = getIntent().getStringExtra("validFrom");
		strValidTo = getIntent().getStringExtra("validTo");
		strRefno = getIntent().getStringExtra("refNo");
		strCertificate = getIntent().getStringExtra("certificate");
		
		for (String aStrUrl : strCertificate.split(",")) {
			certUrl.add(aStrUrl.replace("[", "")
					.replace("]", "")
					.replace("\"", "")
					.replace(" ", "")
					.replace("\\", ""));
		}
		
		name.setText(strName);
		notes.setText(strNotes);
		issueAuth.setText(strIssueAuth);
		FormatDate date = new FormatDate(strAddedOn, "yyyy-MM-dd", "dd-MM-yyyy");
		addedOn.setText(date.format());
		date = new FormatDate(strValidFrom, "yyyy-MM-dd", "dd-MM-yyyy");
		validFrom.setText(date.format());
		date = new FormatDate(strValidTo, "yyyy-MM-dd", "dd-MM-yyyy");
		validTo.setText(date.format());
		refNo.setText(strRefno);
		
		CertificateAdapter adaptor = new CertificateAdapter(this, R.layout.list_certificate, certUrl, this);
		certificateList.setAdapter(adaptor);
		adaptor.notifyDataSetChanged();

	}
	
	@Override
	public void onPause() {
		super.onPause();
	}
	
	@Override
	public void onResume() {
		super.onResume();
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		if (item.getItemId() == android.R.id.home) {
			onBackPressed();
		} else if (item.getItemId() == R.id.delete) {
			new AlertDialog.Builder(this)
					.setTitle("Are you sure you want to delete this compliance?")
					.setPositiveButton("Yes", (dialog, which) -> deleteCompliance(orgId, strId, sessionId))
					.setNegativeButton("No", null)
			.show();
		} else if (item.getItemId() == R.id.edit) {
			Intent intent = new Intent(this, UpdateCompliance.class);
			intent.putExtra("name", strName);
			intent.putExtra("refNo", strRefno);
			intent.putExtra("issueAuth", strIssueAuth);
			intent.putExtra("id", strId);
			intent.putExtra("validFrom", strValidFrom);
			intent.putExtra("validTo", strValidTo);
			intent.putExtra("notes", strNotes);
			startActivity(intent);
		}
		return super.onOptionsItemSelected(item);
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
						Toast.makeText(this, "Deleted Successfully", Toast.LENGTH_SHORT).show();
						startActivity(new Intent(this, CompliancesActivity.class).setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP));
					} else if (result.contains("\"success\":false,\"msg\":\"Session Expire\"")){
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
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		if (sp.getString(KEY_USER_ROLE, "").equals("Admin")) {
			MenuInflater inflater = getMenuInflater();
			inflater.inflate(R.menu.toolbar_menu, menu);
			return true;
		} else {
			return true;
		}
	}
	
	@Override
	public void onBackPressed() {
		super.onBackPressed();
		finish();
	}
}
