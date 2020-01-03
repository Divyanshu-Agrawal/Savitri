package com.aaptrix.savitri.adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.util.ArrayList;

import com.aaptrix.savitri.R;
import androidx.annotation.NonNull;

public class AssignedPeopleAdapter extends ArrayAdapter<String> {
	
	private Context context;
	private int resource;
	private ArrayList<String> nameArray;
	
	public AssignedPeopleAdapter(Context context, int resource, ArrayList<String> nameArray) {
		super(context, resource, nameArray);
		this.context = context;
		this.resource = resource;
		this.nameArray = nameArray;
	}
	
	@SuppressLint({"ViewHolder", "SetTextI18n"})
	@NonNull
	@Override
	public View getView(int position, View view, @NonNull ViewGroup parent) {
		LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		assert inflater != null;
		view = inflater.inflate(resource, null);
		TextView assignedPeople = view.findViewById(R.id.assigned_people);
		LinearLayout layout = view.findViewById(R.id.people_layout);
		RelativeLayout relativeLayout = view.findViewById(R.id.relative_layout);
		relativeLayout.setVisibility(View.GONE);
		layout.setVisibility(View.VISIBLE);
		assignedPeople.setText(nameArray.get(position));
		return view;
	}
}
