package com.aaptrix.savitri.adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.ArrayList;

import com.aaptrix.savitri.R;
import com.aaptrix.savitri.databeans.ComplianceData;
import androidx.annotation.NonNull;

public class HistoryAdapter extends ArrayAdapter<ComplianceData> {
	
	private Context context;
	private int resource;
	private ArrayList<ComplianceData> object;
	
	public HistoryAdapter(Context context, int resource, ArrayList<ComplianceData> object) {
		super(context, resource, object);
		this.context = context;
		this.resource = resource;
		this.object = object;
	}
	
	@SuppressLint("ViewHolder")
	@NonNull
	@Override
	public View getView(int position, View view, @NonNull ViewGroup parent) {
		LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		assert inflater != null;
		view = inflater.inflate(resource, null);
		ComplianceData data = object.get(position);
		TextView complianceName = view.findViewById(R.id.compliance_name);
		TextView refNo = view.findViewById(R.id.compliance_ref_no);
		TextView issuingAuth = view.findViewById(R.id.compliance_issue_authority);
		
		complianceName.setText(data.getName());
		refNo.setText(data.getRefNo());
		
		if (data.getIssueAuth().equals("Other")) {
			issuingAuth.setText(data.getOtherAuth());
		} else {
			issuingAuth.setText(data.getIssueAuth());
		}
		
		return view;
	}
}
