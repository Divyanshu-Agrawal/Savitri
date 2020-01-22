package com.aaptrix.savitri.adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.TextView;

import java.util.ArrayList;

import com.aaptrix.savitri.R;
import com.aaptrix.savitri.activities.RenewDetails;
import com.aaptrix.savitri.databeans.ComplianceData;
import com.aaptrix.savitri.session.FormatDate;
import androidx.annotation.NonNull;

import static com.aaptrix.savitri.session.SharedPrefsNames.KEY_USER_ROLE;
import static com.aaptrix.savitri.session.SharedPrefsNames.USER_PREFS;

public class TasksAdapter extends ArrayAdapter<ComplianceData> {
	
	private Context context;
	private int resource;
	private ArrayList<ComplianceData> object;
	
	class ViewHolder {
		
		TextView taskName, assignedTo, severity, dueDate, dueMonth;
		ImageButton more;
		
		@SuppressLint("NewApi")
		ViewHolder(@NonNull View view) {
			taskName = view.findViewById(R.id.renewal_compliance_name);
			assignedTo = view.findViewById(R.id.renewal_assigned_people);
			severity = view.findViewById(R.id.severity);
			severity.setVisibility(View.GONE);
			dueDate = view.findViewById(R.id.due_date);
			dueMonth = view.findViewById(R.id.due_month_year);
			more = view.findViewById(R.id.renewal_more);
			more.setImageResource(R.drawable.app_info_icon);
			more.setImageTintList(ColorStateList.valueOf(context.getResources().getColor(R.color.yellow)));
		}
	}
	
	public TasksAdapter(Context context, int resource, ArrayList<ComplianceData> object) {
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
		if (object != null) {
			ComplianceData data = object.get(position);
			holder.taskName.setText(data.getName());
			SharedPreferences sp = context.getSharedPreferences(USER_PREFS, Context.MODE_PRIVATE);
			if (sp.getString(KEY_USER_ROLE, "").equals("Admin")) {
				holder.assignedTo.setText(data.getAssignedTo());
				if (data.getMarkReview().equals("1")) {
					holder.more.setVisibility(View.VISIBLE);
				} else {
					holder.more.setVisibility(View.GONE);
				}
			} else {
				if (data.getMarkReview().equals("1")) {
					holder.assignedTo.setText("Marked for review");
					holder.more.setVisibility(View.VISIBLE);
				} else {
					holder.assignedTo.setText(data.getStatus());
					holder.more.setVisibility(View.GONE);
				}
			}
			
			FormatDate date = new FormatDate(data.getValidTo(), "yyyy-MM-dd", "dd-MMM-yyyy");
			String[] ddmmmyyyy = date.format().split("-");
			holder.dueDate.setText(ddmmmyyyy[0]);
			holder.dueMonth.setText(ddmmmyyyy[1] + ", " + ddmmmyyyy[2]);

			view.setOnClickListener(v -> {
				Intent intent = new Intent(context, RenewDetails.class);
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
				intent.putExtra("assignedTo", data.getAssignedTo());
				intent.putExtra("certificate", data.getCertificate());
				intent.putExtra("status", data.getStatus());
				intent.putExtra("markReview", data.getMarkReview());
				context.startActivity(intent);
			});

		}
		return view;
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
