package com.aaptrix.savitri.activities;

import android.app.FragmentTransaction;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.net.ConnectivityManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.aaptrix.savitri.dialogs.UpdateProfileDialog;
import com.github.chrisbanes.photoview.PhotoView;
import com.google.android.material.snackbar.Snackbar;
import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;
import java.util.Objects;

import com.aaptrix.savitri.R;
import com.aaptrix.savitri.session.FormatDate;
import com.aaptrix.savitri.session.SharedPrefsManager;
import com.aaptrix.savitri.session.URLs;
import com.squareup.picasso.Target;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import cz.msebera.android.httpclient.HttpEntity;
import cz.msebera.android.httpclient.HttpResponse;
import cz.msebera.android.httpclient.client.HttpClient;
import cz.msebera.android.httpclient.client.methods.HttpPost;
import cz.msebera.android.httpclient.entity.mime.HttpMultipartMode;
import cz.msebera.android.httpclient.entity.mime.MultipartEntityBuilder;
import cz.msebera.android.httpclient.impl.client.DefaultHttpClient;
import cz.msebera.android.httpclient.util.EntityUtils;
import static com.aaptrix.savitri.session.SharedPrefsNames.KEY_ORG_ID;
import static com.aaptrix.savitri.session.SharedPrefsNames.KEY_ORG_NAME;
import static com.aaptrix.savitri.session.SharedPrefsNames.KEY_SESSION_ID;
import static com.aaptrix.savitri.session.SharedPrefsNames.KEY_USER_ID;
import static com.aaptrix.savitri.session.SharedPrefsNames.USER_PREFS;
import static com.aaptrix.savitri.session.URLs.DATA_URL;
import static java.security.AccessController.getContext;

public class UserProfile extends AppCompatActivity {
	
	Toolbar toolbar;
	ImageView userProfile, editProfile;
	String userId, orgId, sessionId, strUrl;
	SharedPreferences sp;
	ProgressBar progressBar;
	TextView designation, gender, dob, regDate, email, number, type, name, org;
	RelativeLayout fullscrLayout;
	PhotoView fullscrImage;
	boolean isVisible = false;
	RelativeLayout layout;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_user_profile);
		toolbar = findViewById(R.id.toolbar);
		userProfile = findViewById(R.id.profile_image);
		editProfile = findViewById(R.id.edit_profile);
		progressBar = findViewById(R.id.progress_bar);
		name = findViewById(R.id.user_name);
		layout = findViewById(R.id.layout);
		setSupportActionBar(toolbar);
		toolbar.bringToFront();
		Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
		getSupportActionBar().setDisplayShowHomeEnabled(true);
		getSupportActionBar().setTitle("");
		sp = getSharedPreferences(USER_PREFS, Context.MODE_PRIVATE);
		designation = findViewById(R.id.user_designation);
		gender = findViewById(R.id.user_gender);
		dob = findViewById(R.id.user_dob);
		type = findViewById(R.id.user_type);
		email = findViewById(R.id.user_email);
		number = findViewById(R.id.user_contact);
		regDate = findViewById(R.id.user_reg_on);
		fullscrImage = findViewById(R.id.fullscr_user_profile);
		fullscrLayout = findViewById(R.id.fullscr_image);
		org = findViewById(R.id.user_org);
		
		userId = getIntent().getStringExtra("userId");
		orgId = sp.getString(KEY_ORG_ID, "");
		sessionId = sp.getString(KEY_SESSION_ID, "");
		org.setText(sp.getString(KEY_ORG_NAME, ""));
		if (checkConnection()) {
			fetchUserProfile(userId, orgId, sessionId);
		} else {
			Snackbar snackbar = Snackbar.make(layout, "No Internet Connection", Snackbar.LENGTH_INDEFINITE)
					.setActionTextColor(Color.WHITE)
					.setAction("Ok", null);
			snackbar.show();
			try {
				File directory = this.getFilesDir();
				ObjectInputStream in = new ObjectInputStream(new FileInputStream(new File(directory, "userProfile")));
				String json = in.readObject().toString();
				in.close();
				JSONObject jsonObject = new JSONObject(json);
				JSONArray jsonArray = jsonObject.getJSONArray("profileDetails");
				JSONObject jObject = jsonArray.getJSONObject(0);
				name.setText(jObject.getString("users_name"));
				designation.setText(jObject.getString("users_designation"));
				gender.setText(jObject.getString("users_gender"));
				number.setText(jObject.getString("users_mobileno"));
				email.setText(jObject.getString("users_email"));
				type.setText(jObject.getString("users_type"));
				FormatDate date = new FormatDate(jObject.getString("users_dob"), "yyyy-MM-dd", "dd-MM-yyyy");
				dob.setText(date.format());
				date = new FormatDate(jObject.getString("users_reg_date"), "yyyy-MM-dd", "dd-MM-yyyy");
				regDate.setText(date.format());
				File file = new File(directory, "userImg.jpg");

				Picasso.with(this).load(file).placeholder(R.drawable.user_placeholder).into(userProfile);

				userProfile.setOnClickListener(v -> {
					fullscrLayout.setVisibility(View.VISIBLE);
					isVisible = true;
					Picasso.with(this).load(file).placeholder(R.drawable.user_placeholder).into(fullscrImage);
				});
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		
		if (userId.equals(sp.getString(KEY_USER_ID, ""))) {
			editProfile.setVisibility(View.VISIBLE);
		} else {
			editProfile.setVisibility(View.GONE);
		}
		
		number.setOnClickListener(v -> {
			Intent intent = new Intent(Intent.ACTION_DIAL);
			intent.setData(Uri.parse("tel:" + "+91" + number.getText().toString()));
			startActivity(intent);
		});
		
		email.setOnClickListener(v -> {
			Intent intent = new Intent(Intent.ACTION_SENDTO);
			intent.setData(Uri.parse("mailto:" + email.getText().toString()));
			startActivity(Intent.createChooser(intent, "Send Mail..."));
		});

		editProfile.setOnClickListener(v -> {
			if (checkConnection()) {
				PopupMenu popup = new PopupMenu(v.getContext(), v);
				popup.getMenuInflater().inflate(R.menu.profile_menu, popup.getMenu());
				popup.show();
				popup.setOnMenuItemClickListener(item -> {
					switch (item.getItemId()) {
						case R.id.edit_profile:
							FragmentTransaction ft = getFragmentManager().beginTransaction();
							UpdateProfileDialog dialog = new UpdateProfileDialog();
							Bundle bundle = new Bundle();
							bundle.putString("phone", number.getText().toString());
							bundle.putString("email", email.getText().toString());
							bundle.putString("orgName", org.getText().toString());
							bundle.putString("url", strUrl);
							dialog.setArguments(bundle);
							dialog.show(ft, "dialog");
							break;
						case R.id.org_profile:
							startActivity(new Intent(this, OrgProfile.class));
							break;
						case R.id.payment_history:
							startActivity(new Intent(this, PaymentHistory.class));
							break;
						case R.id.logout:
							new AlertDialog.Builder(this)
									.setMessage("Are you sure you want to logout?")
									.setPositiveButton("Yes", (d, which) -> {
										SharedPrefsManager.getInstance(this).logout();
										Intent intent = new Intent(this, AppLogin.class);
										intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
										startActivity(intent);
										finish();
									})
									.setNegativeButton("No", null)
									.show();
							break;
					}
					return true;
				});
			} else {
				Snackbar snackbar = Snackbar.make(layout, "No Internet Connection", Snackbar.LENGTH_LONG)
						.setActionTextColor(Color.WHITE)
						.setAction("Ok", null);
				snackbar.show();
			}
		});
	}

	private void fetchUserProfile(String userId, String orgId, String sessionID) {
		progressBar.setVisibility(View.VISIBLE);
		new Thread(() -> {
			try {
				HttpClient httpclient = new DefaultHttpClient();
				HttpPost httppost = new HttpPost(URLs.ALL_PEOPLE);
				MultipartEntityBuilder entityBuilder = MultipartEntityBuilder.create();
				entityBuilder.setMode(HttpMultipartMode.BROWSER_COMPATIBLE);
				entityBuilder.addTextBody("org_details_id", orgId);
				entityBuilder.addTextBody("app_session_id", sessionID);
				entityBuilder.addTextBody("users_details_id", userId);
				entityBuilder.addTextBody("profile", "profile");
				HttpEntity entity = entityBuilder.build();
				httppost.setEntity(entity);
				HttpResponse response = httpclient.execute(httppost);
				HttpEntity httpEntity = response.getEntity();
				String result = EntityUtils.toString(httpEntity);
				Handler handler = new Handler(Looper.getMainLooper());
				handler.post(() -> {
					try {
						progressBar.setVisibility(View.GONE);
						if (result.contains("\"success\":false,\"msg\":\"Session Expire\"")){
							Toast.makeText(this, "Your Session is expired please login again", Toast.LENGTH_SHORT).show();
							SharedPrefsManager.getInstance(this).logout();
							Intent intent = new Intent(this, AppLogin.class);
							intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
							startActivity(intent);
							finish();
						} else if (result.equals("null")) {
							Toast.makeText(this, "Error Occured", Toast.LENGTH_SHORT).show();
						}  else {
							JSONObject jsonObject = new JSONObject(result);
							cacheJson(jsonObject);
							JSONArray jsonArray = jsonObject.getJSONArray("profileDetails");
							JSONObject jObject = jsonArray.getJSONObject(0);
							name.setText(jObject.getString("users_name"));
							designation.setText(jObject.getString("users_designation"));
							gender.setText(jObject.getString("users_gender"));
							number.setText(jObject.getString("users_mobileno"));
							email.setText(jObject.getString("users_email"));
							type.setText(jObject.getString("users_type"));
							FormatDate date = new FormatDate(jObject.getString("users_dob"), "yyyy-MM-dd", "dd-MM-yyyy");
							dob.setText(date.format());
							date = new FormatDate(jObject.getString("users_reg_date"), "yyyy-MM-dd", "dd-MM-yyyy");
							regDate.setText(date.format());
							strUrl = jObject.getString("users_profile_img");
							String url = DATA_URL + orgId + "/profile/" + strUrl;
							Picasso.with(this).load(url).placeholder(R.drawable.user_placeholder).into(userProfile);
							Picasso.with(this).load(url).into(saveFileCache());
							userProfile.setOnClickListener(v -> {
								fullscrLayout.setVisibility(View.VISIBLE);
								isVisible = true;
								Picasso.with(this).load(url).placeholder(R.drawable.user_placeholder).into(fullscrImage);
							});
						}
					} catch (JSONException e) {
						e.printStackTrace();
					}
				});
			} catch (IOException e) {
				e.printStackTrace();
			}
		}).start();
	}

	private void cacheJson(final JSONObject jsonObject) {
		new Thread(() -> {
			ObjectOutput out;
			String data = jsonObject.toString();
			try {
				if (getContext() != null) {
					File directory = this.getFilesDir();
					directory.mkdir();
					out = new ObjectOutputStream(new FileOutputStream(new File(directory, "userProfile")));
					out.writeObject(data);
					out.close();
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}).start();
	}

	private Target saveFileCache() {
		return new Target() {
			@Override
			public void onBitmapLoaded(final Bitmap bitmap, Picasso.LoadedFrom from) {
				new Thread(() -> {
					try {
						File directory = getFilesDir();
						File file = new File(directory, "userImg.jpg");
						FileOutputStream fos = new FileOutputStream(file);
						bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fos);
						fos.flush();
						fos.close();
					} catch (IOException e) {
						e.printStackTrace();
					}
				}).start();
			}

			@Override
			public void onBitmapFailed(Drawable errorDrawable) {

			}

			@Override
			public void onPrepareLoad(Drawable placeHolderDrawable) {

			}
		};
	}

	public boolean checkConnection() {
		ConnectivityManager connec;
		connec = (ConnectivityManager) getSystemService(CONNECTIVITY_SERVICE);
		assert connec != null;
		return connec.getActiveNetworkInfo() != null && connec.getActiveNetworkInfo().isAvailable() && connec.getActiveNetworkInfo().isConnectedOrConnecting();
	}
	
	@Override
	public void onBackPressed() {
		if (isVisible) {
			isVisible = false;
			fullscrLayout.setVisibility(View.GONE);
		} else {
			super.onBackPressed();
		}
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		if (item.getItemId() == android.R.id.home) {
			onBackPressed();
		}
		return super.onOptionsItemSelected(item);
	}
}
