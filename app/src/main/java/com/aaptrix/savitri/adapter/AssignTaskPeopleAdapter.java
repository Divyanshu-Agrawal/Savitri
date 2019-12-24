package com.aaptrix.savitri.adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;

import java.util.ArrayList;

import com.aaptrix.savitri.R;
import com.aaptrix.savitri.databeans.PeopleData;
import androidx.annotation.NonNull;

import static com.aaptrix.savitri.session.SharedPrefsNames.KEY_PEOPLE_ARRAY;
import static com.aaptrix.savitri.session.SharedPrefsNames.PEOPLE_PREFS;

public class AssignTaskPeopleAdapter extends ArrayAdapter<PeopleData> {
	
	private Context context;
	private int resource;
	private ArrayList<PeopleData> object;
	private ArrayList<String> stringArray = new ArrayList<>();
	private ArrayList<PeopleData> peopleArray = new ArrayList<>();
	
	public AssignTaskPeopleAdapter(Context context, int resource, ArrayList<PeopleData> object) {
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
		PeopleData data = object.get(position);
		CheckBox checkBox = view.findViewById(R.id.assign_checkbox);
		checkBox.setText(data.getName());
		
		checkBox.setOnCheckedChangeListener((compoundButton, b) -> {
			String value = object.get(position).getName();
			PeopleData peopleData = new PeopleData();
			if (b) {
				if (stringArray.contains(value)) {
					Toast.makeText(context, "Already added", Toast.LENGTH_SHORT).show();
				} else {
					stringArray.add(data.getName());
					peopleData.setName(data.getName());
					peopleData.setUsers_details_id(data.getUsers_details_id());
					peopleArray.add(peopleData);
					saveDataInSP(peopleArray);
				}
			} else {
				if (stringArray.contains(value)) {
					peopleArray.remove(position);
					saveDataInSP(peopleArray);
					stringArray.remove(value);
				} else {
					Toast.makeText(context, "Already Removed", Toast.LENGTH_SHORT).show();
				}
			}
		});
		return view;
	}
	
	private void saveDataInSP(ArrayList<PeopleData> peopleArray) {
		Gson gson = new GsonBuilder().create();
		JsonArray array = gson.toJsonTree(peopleArray).getAsJsonArray();
		SharedPreferences sp = context.getSharedPreferences(PEOPLE_PREFS, 0);
		SharedPreferences.Editor se = sp.edit();
		se.clear();
		se.putString(KEY_PEOPLE_ARRAY, array.toString());
		se.apply();
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
