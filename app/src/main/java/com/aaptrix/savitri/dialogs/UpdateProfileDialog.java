package com.aaptrix.savitri.dialogs;

import android.Manifest;
import android.app.Activity;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.PermissionChecker;

import com.aaptrix.savitri.R;
import com.aaptrix.savitri.activities.AppLogin;
import com.aaptrix.savitri.activities.UserProfile;
import com.aaptrix.savitri.session.FileUtil;
import com.aaptrix.savitri.session.SharedPrefsManager;
import com.aaptrix.savitri.session.URLs;
import com.google.android.material.button.MaterialButton;
import com.squareup.picasso.Picasso;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Objects;

import cz.msebera.android.httpclient.HttpEntity;
import cz.msebera.android.httpclient.HttpResponse;
import cz.msebera.android.httpclient.client.HttpClient;
import cz.msebera.android.httpclient.client.cache.Resource;
import cz.msebera.android.httpclient.client.methods.HttpPost;
import cz.msebera.android.httpclient.entity.mime.HttpMultipartMode;
import cz.msebera.android.httpclient.entity.mime.MultipartEntityBuilder;
import cz.msebera.android.httpclient.entity.mime.content.FileBody;
import cz.msebera.android.httpclient.impl.client.DefaultHttpClient;
import cz.msebera.android.httpclient.util.EntityUtils;
import de.hdodenhof.circleimageview.CircleImageView;
import id.zelory.compressor.Compressor;

import static android.app.Activity.RESULT_OK;
import static com.aaptrix.savitri.session.SharedPrefsNames.KEY_ORG_ID;
import static com.aaptrix.savitri.session.SharedPrefsNames.KEY_ORG_NAME;
import static com.aaptrix.savitri.session.SharedPrefsNames.KEY_SESSION_ID;
import static com.aaptrix.savitri.session.SharedPrefsNames.KEY_USER_ID;
import static com.aaptrix.savitri.session.SharedPrefsNames.KEY_USER_IMAGE;
import static com.aaptrix.savitri.session.SharedPrefsNames.USER_PREFS;
import static com.aaptrix.savitri.session.URLs.DATA_URL;

public class UpdateProfileDialog extends DialogFragment {

    private Context context;
    private CircleImageView image;
    private File imageFile;
    private String strOrgId;
    private String strSessionId;
    private String strUserId;
    private SharedPreferences sp;
    private ProgressBar progressBar;

    public UpdateProfileDialog() {

    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        this.context = context;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Dialog dialog = super.onCreateDialog(savedInstanceState);
        dialog.getWindow().requestFeature(Window.FEATURE_NO_TITLE);
        return dialog;
    }

    @Override
    public void onStart() {
        super.onStart();
        Dialog dialog = getDialog();
        if (dialog != null) {
            int w = Resources.getSystem().getDisplayMetrics().widthPixels;
            int width = w/10;
            dialog.getWindow().setLayout(w-width, ViewGroup.LayoutParams.WRAP_CONTENT);
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        }
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.dialog_update_profile, container, false);
        EditText phone = view.findViewById(R.id.update_phone);
        EditText mail = view.findViewById(R.id.update_email);
        MaterialButton close = view.findViewById(R.id.cancel_btn);
        MaterialButton update = view.findViewById(R.id.update_btn);
        progressBar = view.findViewById(R.id.progress_bar);
        image = view.findViewById(R.id.profile_image);

        sp = context.getSharedPreferences(USER_PREFS, Context.MODE_PRIVATE);
        strOrgId = sp.getString(KEY_ORG_ID, "");
        strSessionId = sp.getString(KEY_SESSION_ID, "");
        strUserId = sp.getString(KEY_USER_ID, "");

        Bundle bundle = getArguments();
        assert bundle != null;
        String strPhone = bundle.getString("phone");
        String strEmail = bundle.getString("email");
        String strUrl = bundle.getString("url");

        phone.setText(strPhone);
        mail.setText(strEmail);
        String url = DATA_URL + strOrgId + "/profile/" + strUrl;
        Picasso.with(context).load(url).placeholder(R.drawable.user_placeholder).into(image);

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
            if (PermissionChecker.checkSelfPermission(context, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                Intent intent = new Intent();
                intent.setType("image/*");
                intent.setAction(Intent.ACTION_GET_CONTENT);
                startActivityForResult(intent, 1);
            } else {
                isPermissionGranted();
            }
        });

        close.setOnClickListener(v -> dismiss());

        update.setOnClickListener(v -> {
            progressBar.setVisibility(View.VISIBLE);
            progressBar.bringToFront();
            updateProfile(phone.getText().toString(), mail.getText().toString());
        });


        return view;
    }

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
                        .start(getActivity());

            }
        }

        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if (resultCode == RESULT_OK) {
                try {
                    selectedImage = result.getUri();
                    String filename = FileUtil.getFileName(context, selectedImage);
                    String file_extn = filename.substring(filename.lastIndexOf(".") + 1);
                    if (file_extn.equals("jpg") || file_extn.equals("jpeg") || file_extn.equals("png")) {
                        imageFile = new Compressor(context)
                                .setMaxWidth(640)
                                .setMaxHeight(480)
                                .setQuality(75)
                                .setCompressFormat(Bitmap.CompressFormat.JPEG)
                                .compressToFile(FileUtil.from(context, selectedImage));
                        Picasso.with(context)
                                .load(imageFile)
                                .into(image);
                    } else {
                        FileNotFoundException fe = new FileNotFoundException();
                        Toast.makeText(context, "File not in required format.", Toast.LENGTH_SHORT).show();
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
                entityBuilder.addTextBody("org_details_id", strOrgId);
                entityBuilder.addTextBody("app_session_id", strSessionId);
                entityBuilder.addTextBody("users_details_id", strUserId);
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
                            Toast.makeText(context, "Your Session is expired please login again", Toast.LENGTH_SHORT).show();
                            SharedPrefsManager.getInstance(context).logout();
                            Intent intent = new Intent(context, AppLogin.class);
                            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                            startActivity(intent);
                        } else if (result.equals("null")) {
                            Toast.makeText(context, "Error Occured", Toast.LENGTH_SHORT).show();
                        }  else {
                            JSONObject jsonObject = new JSONObject(result);
                            SharedPreferences.Editor editor = sp.edit();
                            if (imageFile != null) {
                                editor.putString(KEY_USER_IMAGE, jsonObject.getString("img"));
                            }
                            editor.apply();
                            Toast.makeText(context, "Profile Updated Successfully", Toast.LENGTH_SHORT).show();
                            context.startActivity(new Intent(context, UserProfile.class).addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP));
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
        InputMethodManager inputMethodManager = (InputMethodManager) context.getSystemService(Activity.INPUT_METHOD_SERVICE);
        assert inputMethodManager != null;
        inputMethodManager.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }

    public void isPermissionGranted() {
        ActivityCompat.requestPermissions(Objects.requireNonNull(getActivity()),
                new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 1);
    }


    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == 1) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(context, "Permission granted", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(context, "Permission denied", Toast.LENGTH_SHORT).show();
            }
        }
    }
}
