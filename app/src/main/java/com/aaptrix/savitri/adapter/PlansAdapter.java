package com.aaptrix.savitri.adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.aaptrix.savitri.activities.BuyPlan;
import com.google.android.material.button.MaterialButton;

import java.util.ArrayList;

import com.aaptrix.savitri.R;
import com.aaptrix.savitri.databeans.PlansData;
import androidx.annotation.NonNull;

import static com.aaptrix.savitri.session.SharedPrefsNames.KEY_ORG_PLAN_TYPE;
import static com.aaptrix.savitri.session.SharedPrefsNames.USER_PREFS;

public class PlansAdapter extends ArrayAdapter<PlansData> {
	
	private Context context;
	private int resource;
	private ArrayList<PlansData> object;
	
	public PlansAdapter(@NonNull Context context, int resource, ArrayList<PlansData> object) {
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
		TextView complianceLimit = view.findViewById(R.id.compliance_limit);
		TextView userLimit = view.findViewById(R.id.user_limit);
		TextView storageCycle = view.findViewById(R.id.storage_limit);
		TextView planCost = view.findViewById(R.id.plan_cost);
		TextView planName = view.findViewById(R.id.plan_name);
		ImageView alertByApp = view.findViewById(R.id.alert_by_app);
		ImageView alertBySms = view.findViewById(R.id.alert_by_sms);
		ImageView alertByEmail = view.findViewById(R.id.alert_by_email);
		ImageView dataDownload = view.findViewById(R.id.data_download);
		MaterialButton buyPlan = view.findViewById(R.id.buy_plan_btn);
		SharedPreferences sp = context.getSharedPreferences(USER_PREFS, Context.MODE_PRIVATE);
		String userPlan = sp.getString(KEY_ORG_PLAN_TYPE, "");
		PlansData data = object.get(position);
		
		if (data.getId().equals("3")) {
			buyPlan.setVisibility(View.INVISIBLE);
		}
		
		if (userPlan.equals(data.getId())) {
			buyPlan.setVisibility(View.INVISIBLE);
		}

		buyPlan.setOnClickListener(v -> {
			Intent intent = new Intent(context, BuyPlan.class);
			intent.putExtra("data", object.get(position));
			context.startActivity(intent);
		});
		
		planName.setText(data.getName());
		complianceLimit.setText(data.getComplianceLimit());
		userLimit.setText(data.getUserLimit());
		storageCycle.setText(data.getStorageCycle());
		planCost.setText("₹ " + data.getPlanCost() + " (Per Year)");
		
		if (data.getAlertByApp().equals("1")) {
			alertByApp.setImageDrawable(context.getResources().getDrawable(R.drawable.tick));
		} else {
			alertByApp.setImageDrawable(context.getResources().getDrawable(R.drawable.cross));
		}
		
		if (data.getAlertByEmail().equals("1")) {
			alertByEmail.setImageDrawable(context.getResources().getDrawable(R.drawable.tick));
		} else {
			alertByEmail.setImageDrawable(context.getResources().getDrawable(R.drawable.cross));
		}
		
		if (data.getAlertBySms().equals("1")) {
			alertBySms.setImageDrawable(context.getResources().getDrawable(R.drawable.tick));
		} else {
			alertBySms.setImageDrawable(context.getResources().getDrawable(R.drawable.cross));
		}
		
		if (data.getDataDownload().equals("1")) {
			dataDownload.setImageDrawable(context.getResources().getDrawable(R.drawable.tick));
		} else {
			dataDownload.setImageDrawable(context.getResources().getDrawable(R.drawable.cross));
		}
		
		return view;
	}
}
