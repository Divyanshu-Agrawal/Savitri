package com.aaptrix.savitri.adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Objects;

import com.aaptrix.savitri.R;
import com.aaptrix.savitri.activities.RenewCompliance;
import com.aaptrix.savitri.activities.RenewalDetails;
import com.aaptrix.savitri.databeans.ComplianceData;
import com.aaptrix.savitri.dialogs.AssignTaskDialog;
import com.aaptrix.savitri.session.FormatDate;
import androidx.annotation.NonNull;
import androidx.appcompat.widget.PopupMenu;

import static com.aaptrix.savitri.session.SharedPrefsNames.KEY_USER_ROLE;
import static com.aaptrix.savitri.session.SharedPrefsNames.USER_PREFS;

public class RenewalStatusAdapter extends ArrayAdapter<ComplianceData> {
	
	private Context context;
	private int resource;
	private ArrayList<ComplianceData> object;
	private String type;
	
	class ViewHolder {
		
		TextView complianceName, assignedTo, severity, dueDate, dueMonth;
		ImageButton more;
		
		ViewHolder(@NonNull View view) {
			complianceName = view.findViewById(R.id.renewal_compliance_name);
			assignedTo = view.findViewById(R.id.renewal_assigned_people);
			severity = view.findViewById(R.id.severity);
			dueDate = view.findViewById(R.id.due_date);
			dueMonth = view.findViewById(R.id.due_month_year);
			more = view.findViewById(R.id.renewal_more);
		}
	}
	
	public RenewalStatusAdapter(Context context, int resource, ArrayList<ComplianceData> object, String type) {
		super(context, resource, object);
		this.context = context;
		this.resource = resource;
		this.object = object;
		this.type = type;
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
		holder.complianceName.setText(data.getName());
		
		FormatDate date = new FormatDate(data.getValidTo(), "yyyy-MM-dd", "dd-MMM-yyyy");
		String[] ddmmmyyyy = date.format().split("-");
		holder.dueDate.setText(ddmmmyyyy[0]);
		holder.dueMonth.setText(ddmmmyyyy[1] + ", " + ddmmmyyyy[2]);
		if (type.equals("new")) {
			holder.severity.setBackgroundColor(Color.parseColor(data.getAddedDate()));
		} else {
			LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(-1, -1);
			params.height = (int)context.getResources().getDimension(R.dimen._20sdp);
			holder.severity.setLayoutParams(params);
			holder.severity.setGravity(Gravity.CENTER);
			holder.severity.setText("Expired");
			holder.severity.setBackgroundColor(Color.parseColor("#FF0000"));
			holder.severity.setTextColor(Color.parseColor("#FFFFFF"));
		}
		
		SharedPreferences sp = context.getSharedPreferences(USER_PREFS, Context.MODE_PRIVATE);
		String userRole = sp.getString(KEY_USER_ROLE, "");
		
		if (userRole.equals("Admin")) {
			holder.more.setVisibility(View.VISIBLE);
		} else {
			holder.more.setVisibility(View.GONE);
		}

		holder.assignedTo.setText(data.getAssignedTo());
		
		holder.more.setOnClickListener(v -> {
			PopupMenu popup = new PopupMenu(v.getContext(), v);
			popup.getMenuInflater().inflate(R.menu.renewal_menu, popup.getMenu());
			popup.show();
			popup.setOnMenuItemClickListener(item -> {
				switch (item.getItemId()) {
					case R.id.renew_compliance:
						Intent intent = new Intent(context, RenewCompliance.class);
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
						context.startActivity(intent);
						break;
					case R.id.assign_compliance:
						AssignTaskDialog dialog = new AssignTaskDialog(context, data.getId(), "renewal");
						Objects.requireNonNull(dialog.getWindow()).setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
						dialog.show();
						break;
				}
				return true;
			});
		});
		
		view.setOnClickListener(v -> {
			Intent intent = new Intent(context, RenewalDetails.class);
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
			context.startActivity(intent);
		});
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
