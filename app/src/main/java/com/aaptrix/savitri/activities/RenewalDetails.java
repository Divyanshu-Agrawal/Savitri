package com.aaptrix.savitri.activities;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.View;
import android.widget.ListView;
import android.widget.TextView;

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
	String strName, strNotes, strIssueAuth, strValidFrom, strValidTo, strRefno, strId;
	String strAssignedBy, strUserId, strAssignByName, strUserrole, strAssignedTo;
	SharedPreferences sp;
	MaterialButton renewCompliance, assignRenew;
	TextView name, notes, issueAuth, validFrom, validTo, refNo;
	TextView assignedBy, assignByTitle, assignToTitle;
	ListView assignList;
	ArrayList<String> array = new ArrayList<>();
	
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
		strAssignedBy = getIntent().getStringExtra("assignedBy");
		strAssignedTo = getIntent().getStringExtra("assignedTo");
		strAssignByName = getIntent().getStringExtra("assignName");
		
		name = findViewById(R.id.compliance_name);
		notes = findViewById(R.id.compliance_notes);
		issueAuth = findViewById(R.id.compliance_issue_auth);
		validFrom = findViewById(R.id.compliance_valid_from);
		validTo = findViewById(R.id.compliance_valid_upto);
		refNo = findViewById(R.id.compliance_ref_no);
		renewCompliance = findViewById(R.id.renew_btn);
		assignRenew = findViewById(R.id.assign_renew_btn);
		assignList = findViewById(R.id.assigned_to_listview);
		assignedBy = findViewById(R.id.task_assigned_by);
		assignByTitle = findViewById(R.id.assign_by_title);
		assignToTitle = findViewById(R.id.assign_title);
		
		sp = getSharedPreferences(USER_PREFS, Context.MODE_PRIVATE);
		strUserrole = sp.getString(KEY_USER_ROLE, "");
		strUserId = sp.getString(KEY_USER_ID, "");
		
		if (strUserrole.equals("Admin")) {
			assignRenew.setVisibility(View.VISIBLE);
		} else {
			assignRenew.setVisibility(View.GONE);
		}
		
		if (strAssignedBy != null && !strAssignedBy.equals(strUserId)) {
			assignByTitle.setVisibility(View.VISIBLE);
			assignedBy.setVisibility(View.VISIBLE);
			assignedBy.setText(strAssignByName);
		}
		
		name.setText(strName);
		notes.setText(strNotes);
		issueAuth.setText(strIssueAuth);
		FormatDate date = new FormatDate(strValidFrom, "yyyy-MM-dd", "dd-MM-yyyy");
		validFrom.setText(date.format());
		date = new FormatDate(strValidTo, "yyyy-MM-dd", "dd-MM-yyyy");
		validTo.setText(date.format());
		refNo.setText(strRefno);
		
		if (strAssignedTo != null) {
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
		} else {
			assignToTitle.setVisibility(View.GONE);
			assignList.setVisibility(View.GONE);
		}
		if (array.size() == 0) {
			assignToTitle.setVisibility(View.GONE);
			assignList.setVisibility(View.GONE);
		}
		
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
}
