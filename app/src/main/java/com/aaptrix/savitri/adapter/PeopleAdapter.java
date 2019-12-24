package com.aaptrix.savitri.adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

import com.aaptrix.savitri.R;
import com.aaptrix.savitri.activities.PeopleActivity;
import com.aaptrix.savitri.activities.UserProfile;
import com.aaptrix.savitri.databeans.PeopleData;
import com.aaptrix.savitri.session.URLs;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import cz.msebera.android.httpclient.HttpEntity;
import cz.msebera.android.httpclient.HttpResponse;
import cz.msebera.android.httpclient.NameValuePair;
import cz.msebera.android.httpclient.client.HttpClient;
import cz.msebera.android.httpclient.client.entity.UrlEncodedFormEntity;
import cz.msebera.android.httpclient.client.methods.HttpPost;
import cz.msebera.android.httpclient.impl.client.DefaultHttpClient;
import cz.msebera.android.httpclient.message.BasicNameValuePair;
import cz.msebera.android.httpclient.util.EntityUtils;

import static com.aaptrix.savitri.session.SharedPrefsNames.KEY_ORG_ID;
import static com.aaptrix.savitri.session.SharedPrefsNames.KEY_SESSION_ID;
import static com.aaptrix.savitri.session.SharedPrefsNames.USER_PREFS;
import static com.aaptrix.savitri.session.URLs.DATA_URL;

public class PeopleAdapter extends ArrayAdapter<PeopleData> {
	
	private Context context;
	private int resource;
	private ArrayList<PeopleData> object;
	
	class ViewHolder {
		
		TextView name, type, number, invited;
		ImageButton call, message, delete;
		ImageView profileImage;
		
		ViewHolder(@NonNull View view) {
			name = view.findViewById(R.id.member_name);
			type = view.findViewById(R.id.member_type);
			call = view.findViewById(R.id.call_member);
			message = view.findViewById(R.id.message_member);
			delete = view.findViewById(R.id.delete_member);
			number = view.findViewById(R.id.member_number);
			invited = view.findViewById(R.id.invited);
			profileImage = view.findViewById(R.id.member_prof_image);
		}
	}
	
	public PeopleAdapter(Context context, int resource, ArrayList<PeopleData> object) {
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
		PeopleData data = object.get(position);
		SharedPreferences sp = context.getSharedPreferences(USER_PREFS, Context.MODE_PRIVATE);
		if (data != null) {
			
			if (data.getPassword().contains("null")) {
				view.setEnabled(false);
				holder.invited.setVisibility(View.VISIBLE);
			}
			
			holder.name.setText(data.getName());
			holder.type.setText(data.getType());
			holder.number.setText(data.getNumber());
			String url = DATA_URL + sp.getString(KEY_ORG_ID, "") + "/profile/" + data.getImage();
			Picasso.with(context).load(url).placeholder(R.drawable.user_placeholder).into(holder.profileImage);
			
			view.setOnClickListener(v -> {
				Intent intent = new Intent(context, UserProfile.class);
				intent.putExtra("userId", data.getUsers_details_id());
				context.startActivity(intent);
			});
			
			holder.call.setOnClickListener(v -> {
				Intent intent = new Intent(Intent.ACTION_DIAL);
				intent.setData(Uri.parse("tel:" + "+91" + data.getNumber()));
				context.startActivity(intent);
			});
			
			holder.message.setOnClickListener(v -> {
				Intent intentsms = new Intent(Intent.ACTION_VIEW);
				intentsms.setData(Uri.parse("sms:" + data.getNumber()));
				context.startActivity(intentsms);
			});
			
			holder.delete.setOnClickListener(v -> new AlertDialog.Builder(context)
					.setMessage("Are you sure you want to delete this member?")
					.setPositiveButton("Yes", (dialog, which) -> deleteMember(data.getUsers_details_id(),
							sp.getString(KEY_SESSION_ID, "")))
					.setNegativeButton("No", null)
					.show());
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
	
	private void deleteMember(String id, String sessionId) {
		new Thread(() -> {
			HttpClient httpclient = new DefaultHttpClient();
			HttpPost httppost = new HttpPost(URLs.DELETE_MEMBER);
			
			try {
				List<NameValuePair> nameValuePairs = new ArrayList<>(3);
				nameValuePairs.add(new BasicNameValuePair("users_details_id", id));
				nameValuePairs.add(new BasicNameValuePair("app_session_id", sessionId));
				httppost.setEntity(new UrlEncodedFormEntity(nameValuePairs));
				
				HttpResponse response = httpclient.execute(httppost);
				HttpEntity httpEntity = response.getEntity();
				final String result = EntityUtils.toString(httpEntity);
				Log.e("result", result);
				
				Handler handler = new Handler(Looper.getMainLooper());
				handler.post(() -> {
					if (result.contains("\"success\":true")) {
						context.startActivity(new Intent(context, PeopleActivity.class).setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP));
						Toast.makeText(context, "Member Removed", Toast.LENGTH_SHORT).show();
					} else {
						Toast.makeText(context, "Error occured", Toast.LENGTH_SHORT).show();
					}
					
				});
				
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}).start();
	}
	
}
