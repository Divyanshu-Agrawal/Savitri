package com.aaptrix.savitri.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.PermissionChecker;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.DatePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.aaptrix.savitri.R;
import com.aaptrix.savitri.databeans.PlansData;
import com.aaptrix.savitri.session.FileUtil;
import com.aaptrix.savitri.session.URLs;
import com.google.android.material.button.MaterialButton;
import com.squareup.picasso.Picasso;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

import cz.msebera.android.httpclient.HttpEntity;
import cz.msebera.android.httpclient.HttpResponse;
import cz.msebera.android.httpclient.client.HttpClient;
import cz.msebera.android.httpclient.client.methods.HttpPost;
import cz.msebera.android.httpclient.entity.mime.HttpMultipartMode;
import cz.msebera.android.httpclient.entity.mime.MultipartEntityBuilder;
import cz.msebera.android.httpclient.entity.mime.content.FileBody;
import cz.msebera.android.httpclient.impl.client.DefaultHttpClient;
import cz.msebera.android.httpclient.util.EntityUtils;
import id.zelory.compressor.Compressor;

import static com.aaptrix.savitri.session.SharedPrefsNames.KEY_ORG_ID;
import static com.aaptrix.savitri.session.SharedPrefsNames.KEY_SESSION_ID;
import static com.aaptrix.savitri.session.SharedPrefsNames.KEY_USER_ID;
import static com.aaptrix.savitri.session.SharedPrefsNames.USER_PREFS;

public class SubmitPayment extends AppCompatActivity {

    Toolbar toolbar;
    EditText tranId, messge, date;
    MaterialButton submit;
    ProgressBar progressBar;
    ImageView certificate;
    File imageFile;
    String strDate, strOrgId, strUserId, strSessionId;
    Calendar myCalendar = Calendar.getInstance();
    DatePickerDialog.OnDateSetListener dateSetListener;
    PlansData plansData;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_submit_payment);
        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        tranId = findViewById(R.id.trans_id);
        messge = findViewById(R.id.message);
        submit = findViewById(R.id.send_details_btn);
        progressBar = findViewById(R.id.progress_bar);
        certificate = findViewById(R.id.add_certificate);
        date = findViewById(R.id.trans_date);
        plansData = (PlansData) getIntent().getSerializableExtra("plandata");

        SharedPreferences sp = getSharedPreferences(USER_PREFS, Context.MODE_PRIVATE);
        strSessionId = sp.getString(KEY_SESSION_ID, "");
        strOrgId = sp.getString(KEY_ORG_ID, "");
        strUserId = sp.getString(KEY_USER_ID, "");

        tranId.setOnFocusChangeListener((v, hasFocus) -> {
            if (!hasFocus)
                hideKeyboard(v);
        });

        messge.setOnFocusChangeListener((v, hasFocus) -> {
            if (!hasFocus)
                hideKeyboard(v);
        });

        date.setOnFocusChangeListener((v, hasFocus) -> {
            if (!hasFocus) {
                hideKeyboard(v);
            }
        });

        dateSetListener = (view, year, monthOfYear, dayOfMonth) -> {
            myCalendar.set(Calendar.YEAR, year);
            myCalendar.set(Calendar.MONTH, monthOfYear);
            myCalendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
            String myFormat = "dd-MM-yyyy"; //In which you need put here
            SimpleDateFormat sdf = new SimpleDateFormat(myFormat, Locale.US);
            date.setText(sdf.format(myCalendar.getTime()));
            myFormat = "yyyy-MM-dd";
            sdf = new SimpleDateFormat(myFormat, Locale.getDefault());
            strDate = sdf.format(myCalendar.getTime());
        };

        date.setOnClickListener(v -> {
            DatePickerDialog datePickerDialog = new DatePickerDialog(this, dateSetListener, myCalendar
                    .get(Calendar.YEAR), myCalendar.get(Calendar.MONTH),
                    myCalendar.get(Calendar.DAY_OF_MONTH));
            datePickerDialog.show();
        });

        certificate.setOnClickListener(v -> {
            if (PermissionChecker.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                Intent photoPickerIntent = new Intent();
                photoPickerIntent.setType("image/*");
                photoPickerIntent.setAction(Intent.ACTION_GET_CONTENT);
                startActivityForResult(photoPickerIntent, 1);
            } else {
                isPermissionGranted();
            }
        });

        submit.setOnClickListener(v -> {
            if (TextUtils.isEmpty(tranId.getText().toString())) {
                tranId.setError("Please enter transaction Id");
                tranId.requestFocus();
            } else if (TextUtils.isEmpty(messge.getText().toString())) {
                messge.setError("Please enter message");
                messge.requestFocus();
            } else if (TextUtils.isEmpty(strDate)) {
                date.setError("Please select date");
                date.requestFocus();
            } else if (imageFile == null) {
                Toast.makeText(this, "Please upload document", Toast.LENGTH_SHORT).show();
            } else {
                SendPayment sendPayment = new SendPayment(this);
                sendPayment.execute(tranId.getText().toString(), messge.getText().toString(), strDate);
            }
        });
    }

    private void hideKeyboard(View view) {
        InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(Activity.INPUT_METHOD_SERVICE);
        inputMethodManager.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Uri selectedImage;

        if (requestCode == 1) {
            if (resultCode == RESULT_OK) {
                try {
                    selectedImage = data.getData();
                    assert selectedImage != null;
                    String filename = FileUtil.getFileName(this, selectedImage);
                    String file_extn = filename.substring(filename.lastIndexOf(".") + 1);
                    if (file_extn.equals("jpg") || file_extn.equals("jpeg") || file_extn.equals("png")) {
                        imageFile = new Compressor(this)
                                .setMaxWidth(640)
                                .setMaxHeight(480)
                                .setQuality(75)
                                .setCompressFormat(Bitmap.CompressFormat.JPEG)
                                .compressToFile(FileUtil.from(this, selectedImage));
                        Picasso.with(this).load(imageFile).into(certificate);
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

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
        }
        return super.onOptionsItemSelected(item);
    }

    public void isPermissionGranted() {
        ActivityCompat.requestPermissions(this,
                new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 1);
    }


    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == 1) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "Permission granted", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Permission denied", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @SuppressLint("StaticFieldLeak")
    class SendPayment extends AsyncTask<String, String, String> {

        @SuppressLint("StaticFieldLeak")
        private Context context;

        SendPayment(Context context) {
            this.context = context;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressBar.setVisibility(View.VISIBLE);
        }


        @Override
        protected String doInBackground(String... params) {

            String transId = params[0];
            String message = params[1];
            String date = params[2];

            try {
                HttpClient httpclient = new DefaultHttpClient();
                HttpPost httppost = new HttpPost(URLs.OFFLINE_PAYMENT);
                MultipartEntityBuilder entityBuilder = MultipartEntityBuilder.create();
                entityBuilder.setMode(HttpMultipartMode.BROWSER_COMPATIBLE);

                if (imageFile != null) {
                    entityBuilder.addPart("offline_payment_images", new FileBody(imageFile));
                }
                entityBuilder.addTextBody("offline_payment_transection_id", transId);
                entityBuilder.addTextBody("offline_payment_desc", message);
                entityBuilder.addTextBody("offline_payment_date", date);
                entityBuilder.addTextBody("org_details_id",strOrgId);
                entityBuilder.addTextBody("app_session_id", strSessionId);
                entityBuilder.addTextBody("users_details_id", strUserId);
                entityBuilder.addTextBody("plan_name", plansData.getName());
                entityBuilder.addTextBody("planId", plansData.getId());

                HttpEntity entity = entityBuilder.build();
                httppost.setEntity(entity);
                HttpResponse response = httpclient.execute(httppost);
                HttpEntity httpEntity = response.getEntity();
                return EntityUtils.toString(httpEntity);
            } catch (IOException e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(String result) {
            if (result != null) {
                Log.e("result", result);
                try {
                    progressBar.setVisibility(View.GONE);
                    JSONObject jsonObject = new JSONObject(result);
                    if (jsonObject.getBoolean("success")) {
                        Toast.makeText(context, jsonObject.getString("msg"), Toast.LENGTH_SHORT).show();
                        finish();
                    } else {
                        Toast.makeText(context, "Error Occured", Toast.LENGTH_SHORT).show();
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
            super.onPostExecute(result);
        }
    }
}
