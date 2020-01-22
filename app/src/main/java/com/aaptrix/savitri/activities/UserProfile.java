package com.aaptrix.savitri.activities;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.net.ConnectivityManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.aaptrix.savitri.session.FileUtil;
import com.github.chrisbanes.photoview.PhotoView;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.snackbar.Snackbar;
import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
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
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.PermissionChecker;

import cz.msebera.android.httpclient.HttpEntity;
import cz.msebera.android.httpclient.HttpResponse;
import cz.msebera.android.httpclient.client.HttpClient;
import cz.msebera.android.httpclient.client.methods.HttpPost;
import cz.msebera.android.httpclient.entity.mime.HttpMultipartMode;
import cz.msebera.android.httpclient.entity.mime.MultipartEntityBuilder;
import cz.msebera.android.httpclient.entity.mime.content.FileBody;
import cz.msebera.android.httpclient.impl.client.DefaultHttpClient;
import cz.msebera.android.httpclient.util.EntityUtils;
import de.hdodenhof.circleimageview.CircleImageView;
import id.zelory.compressor.Compressor;

import static com.aaptrix.savitri.session.SharedPrefsNames.KEY_ORG_ID;
import static com.aaptrix.savitri.session.SharedPrefsNames.KEY_ORG_NAME;
import static com.aaptrix.savitri.session.SharedPrefsNames.KEY_SESSION_ID;
import static com.aaptrix.savitri.session.SharedPrefsNames.KEY_USER_ID;
import static com.aaptrix.savitri.session.SharedPrefsNames.KEY_USER_IMAGE;
import static com.aaptrix.savitri.session.SharedPrefsNames.KEY_USER_ROLE;
import static com.aaptrix.savitri.session.SharedPrefsNames.USER_PREFS;
import static com.aaptrix.savitri.activities.SplashScreen.DATA_URL;
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
	File imageFile;
	CircleImageView image;
	boolean isVisible = false;
	RelativeLayout layout;
	AlertDialog alertDialog;
	AlertDialog.Builder alert;
	
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
				if (!sp.getString(KEY_USER_ROLE, "").equals("Admin")) {
					popup.getMenu().findItem(R.id.payment_history).setVisible(false);
				}
				popup.show();
				popup.setOnMenuItemClickListener(item -> {
					switch (item.getItemId()) {
						case R.id.edit_profile:
							editUserProfile(number.getText().toString(), email.getText().toString(), strUrl);
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

	private void editUserProfile(String number, String email, String strUrl) {
		LayoutInflater factory = LayoutInflater.from(this);
		final View view = factory.inflate(R.layout.dialog_update_profile, null);

		EditText phone = view.findViewById(R.id.update_phone);
		EditText mail = view.findViewById(R.id.update_email);
		MaterialButton close = view.findViewById(R.id.cancel_btn);
		MaterialButton update = view.findViewById(R.id.update_btn);
		ProgressBar progressBar = view.findViewById(R.id.progress_bar);
		image = view.findViewById(R.id.profile_image);

		phone.setText(number);
		mail.setText(email);
		String url = DATA_URL + orgId + "/profile/" + strUrl;
		Picasso.with(this).load(url).placeholder(R.drawable.user_placeholder).into(image);

		phone.setOnFocusChangeListener((v, hasFocus) -> {
			if (!hasFocus) {
				hideKeyboard(v);
			}
		});

		mail.setOnFocusChangeListener((v, hasFocus) -> {
			if (!hasFocus) {
				hideKeyboard(v);
			}
		});

		image.setOnClickListener(v -> {
			if (PermissionChecker.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
				Intent intent = new Intent();
				intent.setType("image/*");
				intent.setAction(Intent.ACTION_GET_CONTENT);
				startActivityForResult(intent, 1);
			} else {
				isPermissionGranted();
			}
		});

		update.setOnClickListener(v -> {
			progressBar.setVisibility(View.VISIBLE);
			progressBar.bringToFront();
			update.setEnabled(false);
			updateProfile(phone.getText().toString(), mail.getText().toString());
		});

		alert = new AlertDialog.Builder(this);

		alert.setView(view);
		alertDialog = alert.create();
		alertDialog.show();
		Objects.requireNonNull(alertDialog.getWindow()).setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

		close.setOnClickListener(v -> alertDialog.dismiss());

		alertDialog.getButton(DialogInterface.BUTTON_POSITIVE).setVisibility(View.GONE);
		alertDialog.getButton(DialogInterface.BUTTON_NEGATIVE).setVisibility(View.GONE);
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		Uri selectedImage;
		if (requestCode == 1) {
			if (resultCode == RESULT_OK) {
				CropImage.activity(Objects.requireNonNull(data.getData()))
						.setGuidelines(CropImageView.Guidelines.ON)
						.setAspectRatio(150, 150)
						.setGuidelines(CropImageView.Guidelines.ON)
						.setCropShape(CropImageView.CropShape.RECTANGLE)
						.start(this);

			}
		}

		if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
			CropImage.ActivityResult result = CropImage.getActivityResult(data);
			if (resultCode == RESULT_OK) {
				try {
					selectedImage = result.getUri();
					String filename = FileUtil.getFileName(this, selectedImage);
					String file_extn = filename.substring(filename.lastIndexOf(".") + 1);
					if (file_extn.equals("jpg") || file_extn.equals("jpeg") || file_extn.equals("png")) {
						imageFile = new Compressor(this)
								.setMaxWidth(640)
								.setMaxHeight(480)
								.setQuality(75)
								.setCompressFormat(Bitmap.CompressFormat.JPEG)
								.compressToFile(FileUtil.from(this, selectedImage));
						Picasso.with(this)
								.load(imageFile)
								.into(image);
					} else {
						FileNotFoundException fe = new FileNotFoundException();
						Toast.makeText(this, "File not in required format.", Toast.LENGTH_SHORT).show();
						throw fe;
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
	}

	private void updateProfile(String phone, String mail) {
		new Thread(() -> {
			try {
				HttpClient httpclient = new DefaultHttpClient();
				HttpPost httppost = new HttpPost(URLs.UPDATE_PROFILE);
				MultipartEntityBuilder entityBuilder = MultipartEntityBuilder.create();
				entityBuilder.setMode(HttpMultipartMode.BROWSER_COMPATIBLE);
				entityBuilder.addTextBody("org_details_id", orgId);
				entityBuilder.addTextBody("app_session_id", sessionId);
				entityBuilder.addTextBody("users_details_id", userId);
				entityBuilder.addTextBody("users_mobileno", phone);
				entityBuilder.addTextBody("users_email", mail);
				if (imageFile != null)
					entityBuilder.addPart("prof_img", new FileBody(imageFile));
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
						} else if (result.equals("null")) {
							Toast.makeText(this, "Error Occured", Toast.LENGTH_SHORT).show();
						}  else {
							JSONObject jsonObject = new JSONObject(result);
							SharedPreferences.Editor editor = sp.edit();
							if (imageFile != null) {
								editor.putString(KEY_USER_IMAGE, jsonObject.getString("img"));
							}
							editor.apply();
							Toast.makeText(this, "Profile Updated Successfully", Toast.LENGTH_SHORT).show();
							alertDialog.dismiss();
							fetchUserProfile(userId, orgId, sessionId);
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

	private void hideKeyboard(View view) {
		InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(Activity.INPUT_METHOD_SERVICE);
		assert inputMethodManager != null;
		inputMethodManager.hideSoftInputFromWindow(view.getWindowToken(), 0);
	}

	public void isPermissionGranted() {
		ActivityCompat.requestPermissions(Objects.requireNonNull(this),
				new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 1);
	}


	@Override
	public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
		if (requestCode == 1) {
			if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
				Toast.makeText(this, "Permission granted", Toast.LENGTH_SHORT).show();
			} else {
				Toast.makeText(this, "Permission denied", Toast.LENGTH_SHORT).show();
			}
		}
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
	public boolean onOptionsItemSelected(@NonNull MenuItem item) {
		if (item.getItemId() == android.R.id.home) {
			onBackPressed();
		}
		return super.onOptionsItemSelected(item);
	}
}
