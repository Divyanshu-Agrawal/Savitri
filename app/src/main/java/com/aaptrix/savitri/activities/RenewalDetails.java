package com.aaptrix.savitri.activities;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.aaptrix.savitri.adapter.CertificateAdapter;
import com.google.android.material.button.MaterialButton;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Objects;

import com.aaptrix.savitri.R;
import com.aaptrix.savitri.adapter.AssignedPeopleAdapter;
import com.aaptrix.savitri.dialogs.AssignTaskDialog;
import com.aaptrix.savitri.session.FormatDate;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import static com.aaptrix.savitri.session.SharedPrefsNames.KEY_USER_ID;
import static com.aaptrix.savitri.session.SharedPrefsNames.KEY_USER_ROLE;
import static com.aaptrix.savitri.session.SharedPrefsNames.USER_PREFS;

public class RenewalDetails extends AppCompatActivity {
	
	Toolbar toolbar;
	String strName, strNotes, strIssueAuth, strValidFrom, strValidTo, strRefno, strId, strCertificate;
	String strUserId, strUserrole, strAssignedTo;
	SharedPreferences sp;
	MaterialButton renewCompliance, assignRenew;
	TextView name, notes, issueAuth, validFrom, validTo, refNo;
	TextView assignToTitle;
	TextView assignTo;
	ListView certificateList;
	CertificateAdapter adaptor;
	ArrayList<String> certUrl = new ArrayList<>();
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_renewal_details);
		toolbar = findViewById(R.id.toolbar);
		setSupportActionBar(toolbar);
		Objects.requireNonNull(getSupportActionBar()).setDisplayShowHomeEnabled(true);
		getSupportActionBar().setDisplayHomeAsUpEnabled(true);
		
		strName = getIntent().getStringExtra("name");
		strNotes = getIntent().getStringExtra("notes");
		strIssueAuth = getIntent().getStringExtra("issueAuth");
		strId = getIntent().getStringExtra("id");
		strValidFrom = getIntent().getStringExtra("validFrom");
		strValidTo = getIntent().getStringExtra("validTo");
		strRefno = getIntent().getStringExtra("refNo");
		strAssignedTo = getIntent().getStringExtra("assignedTo");
		strCertificate = getIntent().getStringExtra("certificate");
		
		name = findViewById(R.id.compliance_name);
		notes = findViewById(R.id.compliance_notes);
		issueAuth = findViewById(R.id.compliance_issue_auth);
		validFrom = findViewById(R.id.compliance_valid_from);
		validTo = findViewById(R.id.compliance_valid_upto);
		refNo = findViewById(R.id.compliance_ref_no);
		renewCompliance = findViewById(R.id.renew_btn);
		assignRenew = findViewById(R.id.assign_renew_btn);
		assignTo = findViewById(R.id.assigned_to);
		certificateList = findViewById(R.id.certificate_listview);
		assignToTitle = findViewById(R.id.assign_title);
		
		sp = getSharedPreferences(USER_PREFS, Context.MODE_PRIVATE);
		strUserrole = sp.getString(KEY_USER_ROLE, "");
		strUserId = sp.getString(KEY_USER_ID, "");
		
		if (strUserrole.equals("Admin")) {
			assignRenew.setVisibility(View.VISIBLE);
		} else {
			assignRenew.setVisibility(View.GONE);
		}

		for (String aStrUrl : strCertificate.split(",")) {
			certUrl.add(aStrUrl.replace("[", "")
					.replace("]", "")
					.replace("\"", "")
					.replace(" ", "")
					.replace("\\", ""));
		}

		RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
		layoutParams.height = (int) (getResources().getDimension(R.dimen._220sdp)) * certUrl.size();
		certificateList.setLayoutParams(layoutParams);
		certificateList.setPadding(0, 0, 0, 50);
		adaptor = new CertificateAdapter(this, R.layout.list_certificate, certUrl, this);
		certificateList.setAdapter(adaptor);
		adaptor.notifyDataSetChanged();
		
		name.setText(strName);
		notes.setText(strNotes);
		issueAuth.setText(strIssueAuth);
		FormatDate date = new FormatDate(strValidFrom, "yyyy-MM-dd", "dd-MM-yyyy");
		validFrom.setText(date.format());
		date = new FormatDate(strValidTo, "yyyy-MM-dd", "dd-MM-yyyy");
		validTo.setText(date.format());
		refNo.setText(strRefno);
		
		assignTo.setText(strAssignedTo);
		
		renewCompliance.setOnClickListener(v -> {
			Intent intent = new Intent(this, RenewCompliance.class);
			intent.putExtra("name", strName);
			intent.putExtra("refNo", strRefno);
			intent.putExtra("issueAuth", strIssueAuth);
			intent.putExtra("id", strId);
			intent.putExtra("validFrom", strValidFrom);
			intent.putExtra("validTo", strValidTo);
			intent.putExtra("notes", strNotes);
			startActivity(intent);
		});
		
		assignRenew.setOnClickListener(v -> {
			AssignTaskDialog dialog = new AssignTaskDialog(this, strId, "renewal");
			Objects.requireNonNull(dialog.getWindow()).setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
			dialog.show();
		});
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
