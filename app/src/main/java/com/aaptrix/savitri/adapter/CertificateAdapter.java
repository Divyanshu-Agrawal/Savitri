package com.aaptrix.savitri.adapter;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.DownloadManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Environment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.squareup.picasso.Picasso;

import java.util.ArrayList;

import com.aaptrix.savitri.R;
import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.PermissionChecker;

import static com.aaptrix.savitri.session.SharedPrefsNames.KEY_ORG_ID;
import static com.aaptrix.savitri.session.SharedPrefsNames.USER_PREFS;
import static com.aaptrix.savitri.session.URLs.DATA_URL;
import static android.content.Context.DOWNLOAD_SERVICE;

public class CertificateAdapter extends ArrayAdapter<String> implements ActivityCompat.OnRequestPermissionsResultCallback {
	
	private Context context;
	private int resource;
	private ArrayList<String> objects;
	private String strOrgId;
	private long downloadID;
	private Activity activity;
	
	public CertificateAdapter(Context context, int resource, ArrayList<String> objects, Activity activity) {
		super(context, resource, objects);
		this.context = context;
		this.resource = resource;
		this.objects = objects;
		this.activity = activity;
	}
	
	@SuppressLint("ViewHolder")
	@NonNull
	@Override
	public View getView(int position, View view, @NonNull ViewGroup parent) {
		LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		assert inflater != null;
		view = inflater.inflate(resource, null);
		if (objects != null) {
			TextView title = view.findViewById(R.id.title);
			ImageView icon = view.findViewById(R.id.file_icon);
			title.setText(objects.get(position));
			SharedPreferences sp = context.getSharedPreferences(USER_PREFS, Context.MODE_PRIVATE);
			strOrgId = sp.getString(KEY_ORG_ID, "");
			context.registerReceiver(onDownloadComplete,new IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE));
			
			view.setOnClickListener(v -> {
				if (PermissionChecker.checkSelfPermission(context, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
					downloadFile(objects.get(position));
				} else {
					isPermissionGranted();
				}
			});
			
			String fileExt = objects.get(position).substring(objects.get(position).lastIndexOf(".") + 1);
			switch (fileExt) {
				case "pdf":
					Picasso.with(context).load(R.drawable.pdf).into(icon);
					break;
				case "doc":
					Picasso.with(context).load(R.drawable.doc).into(icon);
					break;
				case "docx":
					Picasso.with(context).load(R.drawable.doc).into(icon);
					break;
				case "xls":
					Picasso.with(context).load(R.drawable.xls).into(icon);
					break;
				case "xlsx":
					Picasso.with(context).load(R.drawable.xls).into(icon);
					break;
				case "ppt":
					Picasso.with(context).load(R.drawable.ppt).into(icon);
					break;
				case "pptx":
					Picasso.with(context).load(R.drawable.ppt).into(icon);
					break;
				case "png":
					Picasso.with(context).load(R.drawable.png).into(icon);
					break;
				case "jpg":
					Picasso.with(context).load(R.drawable.jpg).into(icon);
					break;
				case "jpeg":
					Picasso.with(context).load(R.drawable.jpg).into(icon);
					break;
				default:
					Picasso.with(context).load(R.drawable.file).into(icon);
					break;
			}
		}
		return view;
	}
	
	private void downloadFile(String url) {
		String path = Environment.DIRECTORY_DOWNLOADS;
		String downloadUrl = DATA_URL + strOrgId + "/compliances/" + url;
		DownloadManager.Request request = new DownloadManager.Request(Uri.parse(downloadUrl))
				.setTitle(url)
				.setDescription("Downloading")
				.setMimeType("application/octet-stream")
				.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
				.setDestinationInExternalPublicDir(path, url);
		request.allowScanningByMediaScanner();
		DownloadManager downloadManager= (DownloadManager) context.getSystemService(DOWNLOAD_SERVICE);
		assert downloadManager != null;
		downloadID = downloadManager.enqueue(request);
	}
	
	private BroadcastReceiver onDownloadComplete = new BroadcastReceiver() {
		@Override
		public void onReceive(Context ctx, Intent intent) {
			long id = intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1);
			if (downloadID == id) {
				Toast.makeText(context, "Download Completed", Toast.LENGTH_SHORT).show();
			}
		}
	};
	
	private void isPermissionGranted() {
		ActivityCompat.requestPermissions(activity,
				new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
	}
	
	
	@Override
	public void onRequestPermissionsResult(int requestCode,
										   @NonNull String permissions[], @NonNull int[] grantResults) {
		switch (requestCode) {
			case 1: {
				if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
					Toast.makeText(context, "Permission granted", Toast.LENGTH_SHORT).show();
				} else {
					Toast.makeText(context, "Permission denied", Toast.LENGTH_SHORT).show();
				}
				
			}
		}
	}
}
