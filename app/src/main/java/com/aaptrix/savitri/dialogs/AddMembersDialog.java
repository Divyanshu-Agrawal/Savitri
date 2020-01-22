package com.aaptrix.savitri.dialogs;

import android.Manifest;
import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.provider.ContactsContract;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.Switch;
import android.widget.Toast;

import com.aaptrix.savitri.activities.PeopleActivity;
import com.google.android.material.button.MaterialButton;

import java.io.IOException;
import java.util.Objects;

import com.aaptrix.savitri.R;
import com.aaptrix.savitri.session.URLs;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.PermissionChecker;

import android.app.DialogFragment;

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
import static com.aaptrix.savitri.session.SharedPrefsNames.KEY_USER_ID;
import static com.aaptrix.savitri.session.SharedPrefsNames.KEY_USER_NAME;
import static com.aaptrix.savitri.session.SharedPrefsNames.USER_PREFS;

public class AddMembersDialog extends DialogFragment {

    private EditText memberPhone, memberName;
    private Switch makeAdmin;
    private boolean makeAdminState;
    private Context context;
    private ProgressBar progressBar;
    private String senderName, senderId;

    public AddMembersDialog() {

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
            int width = w / 10;
            dialog.getWindow().setLayout(w - width, ViewGroup.LayoutParams.WRAP_CONTENT);
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        }
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.dialog_add_member, container, false);
        memberPhone = view.findViewById(R.id.add_member_phone);
        makeAdmin = view.findViewById(R.id.make_admin_switch);
        MaterialButton cancel = view.findViewById(R.id.cancel_btn);
        MaterialButton add = view.findViewById(R.id.add_btn);
        ImageButton pickContact = view.findViewById(R.id.pick_btn);

        SharedPreferences sp = context.getSharedPreferences(USER_PREFS, Context.MODE_PRIVATE);
        String strOrgId = sp.getString(KEY_ORG_ID, "");
        String strOrgName = sp.getString(KEY_ORG_NAME, "");
        senderId = sp.getString(KEY_USER_ID, "");
        senderName = sp.getString(KEY_USER_NAME, "");
        makeAdmin.setChecked(false);
        memberName = view.findViewById(R.id.add_member_name);
        progressBar = view.findViewById(R.id.progress_bar);

        cancel.setOnClickListener(v -> dismiss());

        memberPhone.setOnFocusChangeListener((v, hasFocus) -> {
            if (!hasFocus)
                hideKeyboard(v);
        });
        memberName.setOnFocusChangeListener((v, hasFocus) -> {
            if (!hasFocus)
                hideKeyboard(v);
        });

        pickContact.setOnClickListener(v -> {
            if (PermissionChecker.checkSelfPermission(context, Manifest.permission.READ_CONTACTS) == PackageManager.PERMISSION_GRANTED) {
                Intent intent = new Intent(Intent.ACTION_PICK, ContactsContract.Contacts.CONTENT_URI);
                startActivityForResult(intent, 1);
            } else {
                isPermissionGranted();
            }
        });

        add.setOnClickListener(v -> {
            if (TextUtils.isEmpty(memberPhone.getText().toString())) {
                memberPhone.setError("Please Enter Phone Number");
                memberPhone.requestFocus();
            } else if (memberPhone.getText().toString().length() < 10) {
                memberPhone.setError("Please Enter Correct Phone Number");
                memberPhone.requestFocus();
                memberPhone.getText().clear();
            } else if (TextUtils.isEmpty(memberName.getText().toString())) {
                memberName.setError("Please Enter Member Name");
                memberName.requestFocus();
            } else {
//                makeAdminState = makeAdmin.isChecked();
                progressBar.setVisibility(View.VISIBLE);
//                add.setEnabled(false);
//                if (makeAdminState) {
//                    addMember(memberPhone.getText().toString(), "Admin", strOrgId, memberName.getText().toString(), strOrgName);
//                } else {
                    addMember(memberPhone.getText().toString(), "Team Member", strOrgId, memberName.getText().toString(), strOrgName);
//                }
            }
        });
        return view;
    }

    private void hideKeyboard(View view) {
        InputMethodManager inputMethodManager = (InputMethodManager) context.getSystemService(Activity.INPUT_METHOD_SERVICE);
        inputMethodManager.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }

    public void onActivityResult(int reqCode, int resultCode, Intent data) {
        super.onActivityResult(reqCode, resultCode, data);

        if (reqCode == 1) {
            if (resultCode == Activity.RESULT_OK) {
                Uri contactData = data.getData();
                Cursor c = getActivity().managedQuery(contactData, null, null, null, null);
                if (c.moveToFirst()) {
                    String id = c.getString(c.getColumnIndexOrThrow(ContactsContract.Contacts._ID));
                    String hasPhone = c.getString(c.getColumnIndex(ContactsContract.Contacts.HAS_PHONE_NUMBER));

                    if (hasPhone.equalsIgnoreCase("1")) {
                        Cursor phones = getActivity().getContentResolver().query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null,
                                ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " = " + id, null, null);
                        phones.moveToFirst();
                        String number = phones.getString(phones.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
                        String name = phones.getString(phones.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME));
                        number = number.replace(" ", "").replace("-", "");
                        memberPhone.setText(number);
                        memberName.setText(name);
                    }
                }
            }
        }
    }

    private void addMember(String phone, String makeAdmin, String orgId, String name, String orgName) {
        new Thread(() -> {
            try {
                HttpClient httpclient = new DefaultHttpClient();
                HttpPost httppost = new HttpPost(URLs.MEMBER_REGISTER_URL);
                MultipartEntityBuilder entityBuilder = MultipartEntityBuilder.create();
                entityBuilder.setMode(HttpMultipartMode.BROWSER_COMPATIBLE);
                entityBuilder.addTextBody("org_details_id", orgId);
                entityBuilder.addTextBody("org_details_name", orgName);
                entityBuilder.addTextBody("users_mobileno", phone);
                entityBuilder.addTextBody("users_type", makeAdmin);
                entityBuilder.addTextBody("users_name", name);
                entityBuilder.addTextBody("sender_nm", senderName);
                entityBuilder.addTextBody("sender_id", senderId);
                HttpEntity entity = entityBuilder.build();
                httppost.setEntity(entity);
                HttpResponse response = httpclient.execute(httppost);
                HttpEntity httpEntity = response.getEntity();
                String result = EntityUtils.toString(httpEntity);
                Handler handler = new Handler(Looper.getMainLooper());
                handler.post(() -> {
                    progressBar.setVisibility(View.GONE);
                    if (result.contains("{\"success\":true}")) {
                        Toast.makeText(context, "Added Successfully", Toast.LENGTH_SHORT).show();
                        context.startActivity(new Intent(context, PeopleActivity.class).addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP));
                        dismiss();
                    } else if (result.contains("Already Exist")) {
                        Toast.makeText(context, "Already Exist", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(context, "Error Occured", Toast.LENGTH_SHORT).show();
                    }
                });
            } catch (IOException e) {
                e.printStackTrace();
            }
        }).start();
    }

    public void isPermissionGranted() {
        ActivityCompat.requestPermissions(Objects.requireNonNull(getActivity()),
                new String[]{Manifest.permission.READ_CONTACTS}, 1);
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
