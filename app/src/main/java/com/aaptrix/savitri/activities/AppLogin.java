package com.aaptrix.savitri.activities;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.aaptrix.savitri.BuildConfig;
import com.aaptrix.savitri.asyncclass.LoginUser;
import com.google.android.gms.tasks.TaskExecutors;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.FirebaseException;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthProvider;
import com.google.firebase.iid.FirebaseInstanceId;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import com.aaptrix.savitri.R;
import com.aaptrix.savitri.session.URLs;

import androidx.appcompat.app.AppCompatActivity;

import cz.msebera.android.httpclient.HttpEntity;
import cz.msebera.android.httpclient.HttpResponse;
import cz.msebera.android.httpclient.client.HttpClient;
import cz.msebera.android.httpclient.client.methods.HttpPost;
import cz.msebera.android.httpclient.entity.mime.HttpMultipartMode;
import cz.msebera.android.httpclient.entity.mime.MultipartEntityBuilder;
import cz.msebera.android.httpclient.impl.client.DefaultHttpClient;
import cz.msebera.android.httpclient.util.EntityUtils;

import static com.aaptrix.savitri.session.SharedPrefsNames.KEY_PASSWORD;
import static com.aaptrix.savitri.session.SharedPrefsNames.KEY_PHONE;
import static com.aaptrix.savitri.session.SharedPrefsNames.KEY_TOKEN;
import static com.aaptrix.savitri.session.SharedPrefsNames.USER_PREFS;

public class AppLogin extends AppCompatActivity {

    RelativeLayout initialLayout, loginLayout, otpLayout, registerLayout;
    EditText userPhone, userPassword, userOtp, userNewPassword, userConfirmPassword;
    MaterialButton proceedBtn, loginBtn, verifyBtn, registerBtn;
    Button resendOtpBtn, forgotPasswordBtn;
    String registerPhoneNumber, registerUserPassword, registerConfirmPassword, registerOTP;
    TextView resendTimer;
    ProgressBar progressBar;
    private FirebaseAuth mAuth;
    private String mVerificationId;
    String token, type;
    TextView version;
    Context context = this;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_app_login);

        FirebaseInstanceId.getInstance().getInstanceId().addOnSuccessListener(this, instanceIdResult -> token = instanceIdResult.getToken());
        mAuth = FirebaseAuth.getInstance();
        progressBar = findViewById(R.id.progress_bar);
        progressBar.bringToFront();
        initialLayout = findViewById(R.id.phone_number_layout);
        loginLayout = findViewById(R.id.login_layout);
        otpLayout = findViewById(R.id.otp_layout);
        registerLayout = findViewById(R.id.register_layout);
        version = findViewById(R.id.version);

        version.setText("Version " + BuildConfig.VERSION_NAME);

        userPhone = findViewById(R.id.user_phone);
        userPassword = findViewById(R.id.user_password);
        userOtp = findViewById(R.id.user_otp);
        userNewPassword = findViewById(R.id.user_new_password);
        userConfirmPassword = findViewById(R.id.user_confirm_password);

        proceedBtn = findViewById(R.id.proceed_btn);
        loginBtn = findViewById(R.id.login_btn);
        verifyBtn = findViewById(R.id.verify_btn);
        registerBtn = findViewById(R.id.register_btn);
        resendOtpBtn = findViewById(R.id.resend_otp_btn);
        resendTimer = findViewById(R.id.resend_timer);
        forgotPasswordBtn = findViewById(R.id.forgot_password_btn);

        proceedBtn.setOnClickListener(v -> {
            registerPhoneNumber = userPhone.getText().toString();
            if (registerPhoneNumber.length() == 10) {
                progressBar.setVisibility(View.VISIBLE);
                verifyNumber(registerPhoneNumber);
            } else {
                userPhone.setError("Please enter correct phone number");
                userPhone.requestFocus();
                userPhone.getText().clear();
            }
        });

        userPhone.setOnFocusChangeListener((v, hasFocus) -> {
            if (!hasFocus)
                hideKeyboard(v);
        });
        userPassword.setOnFocusChangeListener((v, hasFocus) -> {
            if (!hasFocus)
                hideKeyboard(v);
        });
        userNewPassword.setOnFocusChangeListener((v, hasFocus) -> {
            if (!hasFocus)
                hideKeyboard(v);
        });
        userConfirmPassword.setOnFocusChangeListener((v, hasFocus) -> {
            if (!hasFocus)
                hideKeyboard(v);
        });
        userOtp.setOnFocusChangeListener((v, hasFocus) -> {
            if (!hasFocus)
                hideKeyboard(v);
        });
    }

    private void verifyNumber(final String registerPhoneNumber) {

        new Thread(() -> {
            try {
                HttpClient httpclient = new DefaultHttpClient();
                HttpPost httppost = new HttpPost(URLs.VERIFY_PHONE_URL);
                MultipartEntityBuilder entityBuilder = MultipartEntityBuilder.create();
                entityBuilder.setMode(HttpMultipartMode.BROWSER_COMPATIBLE);
                entityBuilder.addTextBody("users_mobileno", registerPhoneNumber);

                HttpEntity entity = entityBuilder.build();
                httppost.setEntity(entity);
                HttpResponse response = httpclient.execute(httppost);
                HttpEntity httpEntity = response.getEntity();
                String result = EntityUtils.toString(httpEntity);
                Handler handler = new Handler(Looper.getMainLooper());
                handler.post(() -> {
                    try {
                        JSONObject jsonObject = new JSONObject(result);
                        JSONObject jObject = jsonObject.getJSONObject("result");
                        progressBar.setVisibility(View.GONE);
                        switch (jObject.getString("msg")) {
                            case "verified":
                                loginUser(registerPhoneNumber);
                                break;
                            case "notverified":
                                verifyUser(registerPhoneNumber, "verification");
                                break;
                            case "Mobile number not available":
                                verifyUser(registerPhoneNumber, "registration");
                                break;
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

    private void countDown() {
        resendTimer.setVisibility(View.VISIBLE);
        resendOtpBtn.setVisibility(View.GONE);
        new CountDownTimer(60000, 1000) {
            @SuppressLint({"SetTextI18n", "DefaultLocale"})
            public void onTick(long millisUntilFinished) {
                resendTimer.setText("00:" + String.format("%02d", millisUntilFinished / 1000));
            }

            @Override
            public void onFinish() {
                resendTimer.setVisibility(View.GONE);
                resendOtpBtn.setVisibility(View.VISIBLE);
            }
        }.start();
    }

    public void verifyUser(String phone, String type) {
        this.type = type;
        progressBar.setVisibility(View.GONE);
        sendVerificationCode(phone);
        initialLayout.setVisibility(View.GONE);
        loginLayout.setVisibility(View.GONE);
        registerLayout.setVisibility(View.GONE);
        otpLayout.setVisibility(View.VISIBLE);
        countDown();
        resendOtpBtn.setOnClickListener(v -> {
            sendVerificationCode(registerPhoneNumber);
            countDown();
        });
    }

    public void loginUser(final String phone) {
        progressBar.setVisibility(View.GONE);
        initialLayout.setVisibility(View.GONE);
        loginLayout.setVisibility(View.VISIBLE);
        otpLayout.setVisibility(View.GONE);
        registerLayout.setVisibility(View.GONE);

        forgotPasswordBtn.setOnClickListener(v -> forgotPassword(phone));

        loginBtn.setOnClickListener(v -> {
            if (TextUtils.isEmpty(userPassword.getText().toString())) {
                userPassword.setError("Please Enter Password");
                userPassword.requestFocus();
            } else {
                progressBar.setVisibility(View.VISIBLE);
                SharedPreferences sp = getSharedPreferences(USER_PREFS, Context.MODE_PRIVATE);
                SharedPreferences.Editor editor = sp.edit();
                editor.putString(KEY_PHONE, phone);
                editor.putString(KEY_PASSWORD, userPassword.getText().toString());
                editor.putString(KEY_TOKEN, token);
                editor.apply();
                LoginUser loginUser = new LoginUser(this, progressBar, "login");
                loginUser.execute(phone, userPassword.getText().toString(), token);
            }
        });

    }

    private void forgotPassword(String phone) {
        type = "forgot";
        progressBar.setVisibility(View.GONE);
        sendVerificationCode(phone);
        otpLayout.setVisibility(View.VISIBLE);
        loginLayout.setVisibility(View.GONE);
        registerLayout.setVisibility(View.GONE);
        initialLayout.setVisibility(View.GONE);
        countDown();
        resendOtpBtn.setOnClickListener(v -> {
            sendVerificationCode(registerPhoneNumber);
            countDown();
        });
    }

    private void sendVerificationCode(String mobile) {
        PhoneAuthProvider.getInstance().verifyPhoneNumber(
                "+91" + mobile,
                60,
                TimeUnit.SECONDS,
                TaskExecutors.MAIN_THREAD,
                mCallbacks);
    }

    private PhoneAuthProvider.OnVerificationStateChangedCallbacks mCallbacks = new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
        @Override
        public void onVerificationCompleted(PhoneAuthCredential phoneAuthCredential) {
            progressBar.setVisibility(View.GONE);
        }

        @Override
        public void onVerificationFailed(FirebaseException e) {
            Toast.makeText(context, e.getMessage(), Toast.LENGTH_LONG).show();
        }

        @Override
        public void onCodeSent(String s, PhoneAuthProvider.ForceResendingToken forceResendingToken) {
            super.onCodeSent(s, forceResendingToken);
            mVerificationId = s;
            Toast.makeText(context, "OTP sent successfully", Toast.LENGTH_SHORT).show();
            verifyVerificationCode();
        }
    };

    private void verifyVerificationCode() {
        verifyBtn.setOnClickListener(v -> {
            registerOTP = userOtp.getText().toString();
            if (registerOTP.length() == 6) {
                PhoneAuthCredential credential = PhoneAuthProvider.getCredential(mVerificationId, registerOTP);
                signInWithPhoneAuthCredential(credential);
            }
        });
    }

    @SuppressLint("SetTextI18n")
    private void signInWithPhoneAuthCredential(PhoneAuthCredential credential) {
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        progressBar.setVisibility(View.GONE);
                        otpLayout.setVisibility(View.GONE);
                        registerLayout.setVisibility(View.VISIBLE);
                        registerBtn.setOnClickListener(v -> {
                            registerUserPassword = userNewPassword.getText().toString();
                            registerConfirmPassword = userConfirmPassword.getText().toString();
                            if (TextUtils.isEmpty(registerUserPassword)) {
                                userNewPassword.setError("Please Enter Password");
                                userNewPassword.requestFocus();
                                userConfirmPassword.getText().clear();
                            } else if (TextUtils.isEmpty(registerConfirmPassword)) {
                                userConfirmPassword.setError("Please Re-enter Password");
                                userConfirmPassword.requestFocus();
                                userNewPassword.getText().clear();
                            } else if (!registerUserPassword.equals(registerConfirmPassword)) {
                                Toast.makeText(context, "Password does not match", Toast.LENGTH_SHORT).show();
                                userNewPassword.getText().clear();
                                userConfirmPassword.getText().clear();
                            } else if (registerUserPassword.length() < 8) {
                                Toast.makeText(context, "Password must have 8 characters", Toast.LENGTH_SHORT).show();
                            } else {
                                if (type.equals("forgot")) {
                                    SharedPreferences sp = getSharedPreferences(USER_PREFS, Context.MODE_PRIVATE);
                                    SharedPreferences.Editor editor = sp.edit();
                                    editor.putString(KEY_PHONE, registerPhoneNumber);
                                    editor.putString(KEY_PASSWORD, registerUserPassword);
                                    editor.apply();
                                    ForgotPassword forgotPassword = new ForgotPassword(this);
                                    forgotPassword.execute(registerPhoneNumber, registerUserPassword);
                                } else {
                                    startActivity(new Intent(context, UserDetails.class).putExtra("type", type)
                                            .putExtra("password", registerUserPassword)
                                            .putExtra("mobile", registerPhoneNumber));
                                }
                            }
                        });
                    } else {
                        if (task.getException() instanceof FirebaseAuthInvalidCredentialsException) {
                            String message = "Invalid code entered...";
                            progressBar.setVisibility(View.GONE);
                            Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
                        } else {
                            String message = "Something is wrong, we will fix it soon...";
                            progressBar.setVisibility(View.GONE);
                            Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }


    @Override
    public void onBackPressed() {
        finishAffinity();
        System.exit(0);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    @SuppressLint("StaticFieldLeak")
    class ForgotPassword extends AsyncTask<String, String, String> {

        @SuppressLint("StaticFieldLeak")
        private Context ctx;
        private String username, password;

        ForgotPassword(Context ctx) {
            this.ctx = ctx;
        }

        @Override
        protected void onPreExecute() {
            progressBar.setVisibility(View.VISIBLE);
            super.onPreExecute();
        }


        @Override
        protected String doInBackground(String... params) {

            username = params[0];
            password = params[1];

            try {
                HttpClient httpclient = new DefaultHttpClient();
                HttpPost httppost = new HttpPost(URLs.FORGOT_PASSWORD);
                MultipartEntityBuilder entityBuilder = MultipartEntityBuilder.create();
                entityBuilder.setMode(HttpMultipartMode.BROWSER_COMPATIBLE);
                entityBuilder.addTextBody("users_mobileno", username);

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
            Log.e("result", result);
            if (result != null) {
                try {
                    JSONObject jsonObject = new JSONObject(result);
                    if (jsonObject.getBoolean("success")) {
                        new Thread(() -> {
                            try {
                                HttpClient httpclient = new DefaultHttpClient();
                                HttpPost httppost = new HttpPost(URLs.SET_PASSWORD);
                                MultipartEntityBuilder entityBuilder = MultipartEntityBuilder.create();
                                entityBuilder.setMode(HttpMultipartMode.BROWSER_COMPATIBLE);
                                entityBuilder.addTextBody("users_details_id", jsonObject.getString("userId"));
                                entityBuilder.addTextBody("users_password", password);

                                HttpEntity entity = entityBuilder.build();
                                httppost.setEntity(entity);
                                HttpResponse response = httpclient.execute(httppost);
                                HttpEntity httpEntity = response.getEntity();
                                String res = EntityUtils.toString(httpEntity);
                                Handler handler = new Handler(Looper.getMainLooper());
                                handler.post(() -> {
                                    try {
                                        JSONObject jObject = new JSONObject(res);
                                        if (jObject.getBoolean("success")) {
                                            LoginUser loginUser = new LoginUser(ctx, progressBar, "login");
                                            loginUser.execute(username, password, token);
                                        }
                                    } catch (JSONException e) {
                                        e.printStackTrace();
                                    }
                                });
                            } catch (IOException e) {
                                e.printStackTrace();
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }).start();
                    } else {
                        Toast.makeText(ctx, jsonObject.getString("msg"), Toast.LENGTH_SHORT).show();
                        progressBar.setVisibility(View.GONE);
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
            super.onPostExecute(result);
        }
    }
}
