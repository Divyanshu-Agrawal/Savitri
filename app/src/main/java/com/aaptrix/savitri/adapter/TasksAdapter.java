package com.aaptrix.savitri.adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.TextView;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import com.aaptrix.savitri.R;
import com.aaptrix.savitri.activities.TaskDetail;
import com.aaptrix.savitri.databeans.TasksData;
import com.aaptrix.savitri.session.FormatDate;
import androidx.annotation.NonNull;

import static com.aaptrix.savitri.session.SharedPrefsNames.KEY_USER_ROLE;
import static com.aaptrix.savitri.session.SharedPrefsNames.USER_PREFS;

public class TasksAdapter extends ArrayAdapter<TasksData> {
	
	private Context context;
	private int resource;
	private ArrayList<TasksData> object;
	
	class ViewHolder {
		
		TextView taskName, assignedTo, severity, dueDate, dueMonth;
		ImageButton more;
		
		ViewHolder(@NonNull View view) {
			taskName = view.findViewById(R.id.renewal_compliance_name);
			assignedTo = view.findViewById(R.id.renewal_assigned_people);
			severity = view.findViewById(R.id.severity);
			severity.setVisibility(View.GONE);
			dueDate = view.findViewById(R.id.due_date);
			dueMonth = view.findViewById(R.id.due_month_year);
			more = view.findViewById(R.id.renewal_more);
			more.setVisibility(View.GONE);
		}
	}
	
	public TasksAdapter(Context context, int resource, ArrayList<TasksData> object) {
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
			TasksData data = object.get(position);
			holder.taskName.setText(data.getTaskName());
			holder.assignedTo.setText(data.getTaskDesc());
			SharedPreferences sp = context.getSharedPreferences(USER_PREFS, Context.MODE_PRIVATE);
			ArrayList<String> array = new ArrayList<>();
			if (sp.getString(KEY_USER_ROLE, "").equals("Admin")) {
				try {
					JSONObject jsonObject = new JSONObject(data.getAssignedTo());
					for (int i = 0; i < 5; i++) {
						JSONObject jObject = jsonObject.getJSONObject("assign_users_list" + i);
						array.add(jObject.getString("users_name"));
					}
				} catch (JSONException e) {
					e.printStackTrace();
				}
				
				if (array.size() > 1) {
					int size = array.size() - 1;
					holder.assignedTo.setText(array.get(0) + " +" + size);
				} else if (array.size() == 1) {
					holder.assignedTo.setText(array.get(0));
				} else {
					holder.assignedTo.setText("Not Assigned");
				}
			} else {
				holder.assignedTo.setText(data.getStatus());
			}
			
			view.setOnClickListener(v -> {
				Intent intent = new Intent(context, TaskDetail.class);
				intent.putExtra("assignedBy", data.getAssignedBy());
				intent.putExtra("assignName", data.getAssignedByName());
				intent.putExtra("assignedTo", data.getAssignedTo());
				intent.putExtra("name", data.getTaskName());
				intent.putExtra("desc", data.getTaskDesc());
				intent.putExtra("due_date", data.getTaskDueDate());
				intent.putExtra("id", data.getTaskId());
				intent.putExtra("status", data.getStatus());
				context.startActivity(intent);
			});
			
			FormatDate date = new FormatDate(data.getTaskDueDate(), "yyyy-MM-dd", "dd-MMM-yyyy");
			String[] ddmmmyyyy = date.format().split("-");
			holder.dueDate.setText(ddmmmyyyy[0]);
			holder.dueMonth.setText(ddmmmyyyy[1] + ", " + ddmmmyyyy[2]);
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
