package com.aaptrix.savitri.activities;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;

import com.aaptrix.savitri.R;
import com.aaptrix.savitri.adapter.CertificateAdapter;
import com.aaptrix.savitri.databeans.ComplianceData;
import com.aaptrix.savitri.session.FormatDate;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

public class HistoryDetails extends AppCompatActivity {
	
	Toolbar toolbar;
	ProgressBar progressBar;
	String strComplianceId, strName, strNotes, strRef, strAuth;
	LinearLayout historyLayout;
	TextView comName, comRef, comAuth, comNotes;
	ArrayList<ComplianceData> complianceArray;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_history_details);
		toolbar = findViewById(R.id.toolbar);
		setSupportActionBar(toolbar);
		getSupportActionBar().setDisplayHomeAsUpEnabled(true);
		getSupportActionBar().setDisplayShowHomeEnabled(true);
		strComplianceId = getIntent().getStringExtra("id");
		strName = getIntent().getStringExtra("name");
		strRef = getIntent().getStringExtra("refno");
		strNotes = getIntent().getStringExtra("notes");
		strAuth = getIntent().getStringExtra("issueAuth");
		GsonBuilder gsonBuilder = new GsonBuilder();
		Type type = new TypeToken<ArrayList<ComplianceData>>() {}.getType();
		complianceArray = gsonBuilder.create().fromJson(getIntent().getStringExtra("complianceArray"), type);
		getSupportActionBar().setTitle(strName);
		
		historyLayout = findViewById(R.id.history_layout);
		comName = findViewById(R.id.compliance_name);
		comAuth = findViewById(R.id.compliance_issue_auth);
		comNotes = findViewById(R.id.compliance_notes);
		comRef = findViewById(R.id.compliance_ref_no);
		progressBar = findViewById(R.id.progress_bar);
		
		comRef.setText(strRef);
		comNotes.setText(strNotes);
		comAuth.setText(strAuth);
		comName.setText(strName);
		listItem();
	}
	
	@SuppressLint("SetTextI18n")
	private void listItem() {
		int padding = getResources().getDimensionPixelSize(R.dimen.padding);
		LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
		LinearLayout.LayoutParams textViewParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
		textViewParams.weight = 1f;
		for (int i = complianceArray.size()-1; i >= 0; i--) {
			LinearLayout layout = new LinearLayout(this);
			layout.setOrientation(LinearLayout.HORIZONTAL);
			layout.setLayoutParams(params);
			TextView from = new TextView(this);
			from.setLayoutParams(textViewParams);
			from.setBackgroundResource(android.R.color.white);
			from.setPadding(padding, 1, padding, 10);
			from.setText("Valid From");
			layout.addView(from);
			TextView upto = new TextView(this);
			upto.setLayoutParams(textViewParams);
			upto.setBackgroundResource(android.R.color.white);
			upto.setPadding(padding, 1, padding, 10);
			upto.setText("Valid Upto");
			layout.addView(upto);
			historyLayout.addView(layout);
			
			LinearLayout dateLayout = new LinearLayout(this);
			dateLayout.setOrientation(LinearLayout.HORIZONTAL);
			dateLayout.setLayoutParams(params);
			TextView fromDate = new TextView(this);
			fromDate.setLayoutParams(textViewParams);
			FormatDate date = new FormatDate(complianceArray.get(i).getValidfrom(), "yyyy-MM-dd", "dd-MM-yyyy");
			fromDate.setText(date.format());
			fromDate.setBackgroundResource(android.R.color.white);
			fromDate.setPadding(padding, 1, padding, 10);
			fromDate.setTextColor(getResources().getColor(android.R.color.black));
			fromDate.setTextSize(18);
			dateLayout.addView(fromDate);
			TextView uptoDate = new TextView(this);
			uptoDate.setLayoutParams(textViewParams);
			date = new FormatDate(complianceArray.get(i).getValidTo(), "yyyy-MM-dd", "dd-MM-yyyy");
			uptoDate.setText(date.format());
			uptoDate.setBackgroundResource(android.R.color.white);
			uptoDate.setPadding(padding, 1, padding, 10);
			uptoDate.setTextColor(getResources().getColor(android.R.color.black));
			uptoDate.setTextSize(18);
			dateLayout.addView(uptoDate);
			historyLayout.addView(dateLayout);
			
			LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
			
			ListView listView = new ListView(this);
			listView.setDivider(null);
			listView.setDividerHeight(0);
			
			ArrayList<String> certUrl = new ArrayList<>();
			for (String aStrUrl : complianceArray.get(i).getCertificate().split(",")) {
				certUrl.add(aStrUrl.replace("[", "")
						.replace("]", "")
						.replace("\"", "")
						.replace(" ", "")
						.replace("\\", ""));
			}
			
			layoutParams.height = (int) (getResources().getDimension(R.dimen._70sdp)) * certUrl.size();
			listView.setLayoutParams(layoutParams);
			
			CertificateAdapter adaptor = new CertificateAdapter(this, R.layout.list_certificate, certUrl, this);
			listView.setAdapter(adaptor);
			adaptor.notifyDataSetChanged();
			historyLayout.addView(listView);
		}
		progressBar.setVisibility(View.GONE);
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
