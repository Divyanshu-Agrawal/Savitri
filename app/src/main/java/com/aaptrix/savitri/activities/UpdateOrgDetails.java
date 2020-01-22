package com.aaptrix.savitri.activities;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.aaptrix.savitri.R;
import com.aaptrix.savitri.activities.OrgProfile;
import com.aaptrix.savitri.databeans.StateData;
import com.aaptrix.savitri.session.URLs;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.material.button.MaterialButton;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Objects;

import cz.msebera.android.httpclient.HttpEntity;
import cz.msebera.android.httpclient.HttpResponse;
import cz.msebera.android.httpclient.client.HttpClient;
import cz.msebera.android.httpclient.client.methods.HttpPost;
import cz.msebera.android.httpclient.entity.mime.HttpMultipartMode;
import cz.msebera.android.httpclient.entity.mime.MultipartEntityBuilder;
import cz.msebera.android.httpclient.impl.client.DefaultHttpClient;
import cz.msebera.android.httpclient.util.EntityUtils;

import static com.aaptrix.savitri.session.SharedPrefsNames.KEY_ORG_ID;
import static com.aaptrix.savitri.session.SharedPrefsNames.KEY_SESSION_ID;
import static com.aaptrix.savitri.session.SharedPrefsNames.USER_PREFS;

public class UpdateOrgDetails extends AppCompatActivity {

    Toolbar toolbar;
    EditText orgName, orgType, orgLandline, orgAddress, orgPincode, orgDetails;
    Spinner orgCity, orgState, orgDistrict;
    String name, type, phone, address, pincode, details, state, city, district;
    String strOrgId, strSessionId;
    MaterialButton registerBtn;
    RelativeLayout progressBar;
    String strState, strCity, strDistrict;
    private StateData stateData;
    private ArrayList<StateData> stateArray = new ArrayList<>();
    private ArrayList<String> stateName = new ArrayList<>();
    private ArrayList<String> stateID = new ArrayList<>();
    private ArrayList<String> cityarray = new ArrayList<>();
    private ArrayList<String> districtArray = new ArrayList<>();
    ArrayAdapter<String> cityAdapter;
    ArrayAdapter<String> districtAdapter;
    View v;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_update_org_details);
        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        orgName = findViewById(R.id.organisation_name);
        orgType = findViewById(R.id.organisation_type);
        orgLandline = findViewById(R.id.organisation_landline);
        orgAddress = findViewById(R.id.organisation_address);
        orgCity = findViewById(R.id.organisation_city);
        orgDistrict = findViewById(R.id.organisation_district);
        orgState = findViewById(R.id.organisation_state);
        orgPincode = findViewById(R.id.organisation_pincode);
        registerBtn = findViewById(R.id.update_org_btn);
        orgDetails = findViewById(R.id.organisation_details);
        progressBar = findViewById(R.id.progress_bar);
        progressBar.bringToFront();

        progressBar.setOnTouchListener((v1, event) -> false);
        progressBar.setOnClickListener(v1 -> {});

        SharedPreferences sp = getSharedPreferences(USER_PREFS, Context.MODE_PRIVATE);
        strOrgId = sp.getString(KEY_ORG_ID, "");
        strSessionId = sp.getString(KEY_SESSION_ID, "");

        name = getIntent().getStringExtra("name");
        phone = getIntent().getStringExtra("phone");
        type = getIntent().getStringExtra("type");
        address = getIntent().getStringExtra("address");
        city = getIntent().getStringExtra("city");
        strCity = getIntent().getStringExtra("city");
        district = getIntent().getStringExtra("district");
        strDistrict = getIntent().getStringExtra("district");
        state = getIntent().getStringExtra("state");
        strState = getIntent().getStringExtra("state");
        pincode = getIntent().getStringExtra("pincode");
        details = getIntent().getStringExtra("details");
        fetchAllState();

        orgName.setText(name);
        orgType.setText(type);
        orgPincode.setText(pincode);
        orgLandline.setText(phone);
        orgDetails.setText(details);
        orgAddress.setText(address);

        orgDetails.setOnFocusChangeListener((v, hasFocus) -> {
            if (!hasFocus)
                hideKeyboard(v);
        });
        orgType.setOnFocusChangeListener((v, hasFocus) -> {
            if (!hasFocus)
                hideKeyboard(v);
        });
        orgState.setOnFocusChangeListener((v, hasFocus) -> {
            if (!hasFocus)
                hideKeyboard(v);
        });
        orgDistrict.setOnFocusChangeListener((v, hasFocus) -> {
            if (!hasFocus)
                hideKeyboard(v);
        });
        orgCity.setOnFocusChangeListener((v, hasFocus) -> {
            if (!hasFocus)
                hideKeyboard(v);
        });
        orgLandline.setOnFocusChangeListener((v, hasFocus) -> {
            if (!hasFocus)
                hideKeyboard(v);
        });
        orgPincode.setOnFocusChangeListener((v, hasFocus) -> {
            if (!hasFocus)
                hideKeyboard(v);
        });
        orgName.setOnFocusChangeListener((v, hasFocus) -> {
            if (!hasFocus)
                hideKeyboard(v);
        });
        orgAddress.setOnFocusChangeListener((v, hasFocus) -> {
            if (!hasFocus)
                hideKeyboard(v);
        });

        cityarray.add("Select City*");
        cityarray.add(city);
        cityAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, cityarray);
        cityAdapter.notifyDataSetChanged();
        cityAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        orgCity.setAdapter(cityAdapter);
        orgCity.setSelection(1);
        orgCity.setEnabled(false);

        districtArray.add("Select District*");
        districtArray.add(district);
        districtAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, districtArray);
        districtAdapter.notifyDataSetChanged();
        districtAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        orgDistrict.setAdapter(districtAdapter);
        orgDistrict.setSelection(1);
        orgDistrict.setEnabled(false);

        orgState.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (view != null) {
                    v = view;
                    if (position == 0)
                        ((TextView) view).setTextColor(getResources().getColor(R.color.hintcolor));
                    else
                        ((TextView) view).setTextColor(getResources().getColor(R.color.colorPrimaryDark));
                } else {
                    if (position == 0)
                        ((TextView) v).setTextColor(getResources().getColor(R.color.hintcolor));
                    else
                        ((TextView) v).setTextColor(getResources().getColor(R.color.colorPrimaryDark));
                }
                if (position != 0) {
                    cityarray.clear();
                    districtArray.clear();
                    orgCity.setEnabled(true);
                    orgDistrict.setEnabled(true);
                    strState = stateID.get(position);
                    fetchCity(stateID.get(position));
                } else {
                    cityarray.clear();
                    districtArray.clear();
                    orgDistrict.setEnabled(false);
                    orgCity.setEnabled(false);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        registerBtn.setOnClickListener(v -> {
            if (TextUtils.isEmpty(orgName.getText().toString())) {
                orgName.setError("Please Enter Organisation Name");
                orgName.requestFocus();
            } else if (TextUtils.isEmpty(orgType.getText().toString())) {
                orgType.setError("Please Enter Organisation Type");
                orgType.requestFocus();
            } else if (TextUtils.isEmpty(orgLandline.getText().toString())) {
                orgLandline.setError("Please Enter Organisation Number");
                orgLandline.requestFocus();
            } else if (TextUtils.isEmpty(orgDetails.getText().toString())) {
                orgDetails.setError("Please Enter Organisation Details");
                orgDetails.requestFocus();
            } else if (TextUtils.isEmpty(orgAddress.getText().toString())) {
                orgAddress.setError("Please Enter Organisation Address");
                orgAddress.requestFocus();
            } else if (TextUtils.isEmpty(strCity)) {
                Toast.makeText(this, "Please Select Organisation City", Toast.LENGTH_SHORT).show();
            } else if (TextUtils.isEmpty(strDistrict)) {
                Toast.makeText(this, "Please Select Organisation District", Toast.LENGTH_SHORT).show();
            } else if (TextUtils.isEmpty(strState)) {
                Toast.makeText(this, "Please Select Organisation State", Toast.LENGTH_SHORT).show();
            } else if (TextUtils.isEmpty(orgPincode.getText().toString())) {
                orgPincode.setError("Please Enter Organisation Pincode");
                orgPincode.requestFocus();
            } else if (orgPincode.getText().toString().length() != 6) {
                orgPincode.setError("Please Enter Correct Pincode");
                orgPincode.requestFocus();
            } else if (orgLandline.getText().toString().length() != 11) {
                orgLandline.setError("Please Enter Correct Number");
                orgLandline.requestFocus();
            } else {
                registerBtn.setEnabled(false);
                UpdateOrg updateOrg = new UpdateOrg(this);
                updateOrg.execute(orgName.getText().toString(),
                        orgType.getText().toString(),
                        orgAddress.getText().toString(),
                        orgLandline.getText().toString(),
                        strState, strCity, strDistrict,
                        orgPincode.getText().toString(), orgDetails.getText().toString());
            }
        });
    }

    private void hideKeyboard(View view) {
        InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(Activity.INPUT_METHOD_SERVICE);
        inputMethodManager.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }

    private void fetchAllState() {
        StringRequest stringRequest = new StringRequest(Request.Method.GET, URLs.ALL_STATE_URL, response -> {
            try {
                JSONObject jsonObject = new JSONObject(response);
                JSONArray jsonArray = jsonObject.getJSONArray("allState");
                for (int i = 0; i<jsonArray.length(); i++) {
                    JSONObject jObject = jsonArray.getJSONObject(i);
                    stateData = new StateData();
                    stateData.setId(jObject.getString("id"));
                    stateData.setName(jObject.getString("name"));
                    stateArray.add(stateData);
                }
                setState(stateArray);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }, error -> Toast.makeText(this, "Something went wrong...", Toast.LENGTH_SHORT).show());

        RequestQueue requestQueue = Volley.newRequestQueue(this);
        requestQueue.add(stringRequest);
    }

    private void setState(ArrayList<StateData> stateArrayList) {
        stateName.add("Select State*");
        stateID.add("00");
        for (int i=0; i<stateArrayList.size(); i++) {
            stateName.add(stateArrayList.get(i).getName());
            stateID.add(stateArrayList.get(i).getId());
        }
        ArrayAdapter<String> stateAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, stateName);
        stateAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        stateAdapter.notifyDataSetChanged();
        orgState.setAdapter(stateAdapter);
        for (int i = 0; i < stateName.size(); i++) {
            if (stateName.get(i).equals(state)) {
                orgState.setSelection(i);
                break;
            }
        }
    }

    private void fetchCity(String stateid) {
        String url = URLs.CITY_URL + "?stateId=" + stateid;
        StringRequest stringRequest = new StringRequest(Request.Method.GET, url, response -> {
            try {
                JSONObject jsonObject = new JSONObject(response);
                JSONArray jsonArray = jsonObject.getJSONArray("cityList");
                cityarray.add("Select City*");
                for (int i = 0; i<jsonArray.length(); i++) {
                    JSONObject jObject = jsonArray.getJSONObject(i);
                    cityarray.add(jObject.getString("name"));
                }
                JSONArray disArray = jsonObject.getJSONArray("districtList");
                districtArray.add("Select District");
                for (int i = 0; i < disArray.length(); i++) {
                    JSONObject jObject = jsonArray.getJSONObject(i);
                    districtArray.add(jObject.getString("name"));
                }
                setCity(cityarray, districtArray);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }, error -> Toast.makeText(this, "Something went wrong...", Toast.LENGTH_SHORT).show());

        RequestQueue requestQueue = Volley.newRequestQueue(this);
        requestQueue.add(stringRequest);
    }

    private void setCity(final ArrayList<String> city, final ArrayList<String> district) {
        cityAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, city);
        cityAdapter.notifyDataSetChanged();
        cityAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        orgCity.setAdapter(cityAdapter);

        for (int i = 0; i < city.size(); i++) {
            if (city.get(i).equals(this.city)) {
                orgCity.setSelection(i);
                break;
            }
        }

        districtAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, district);
        districtAdapter.notifyDataSetChanged();
        districtAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        orgDistrict.setAdapter(districtAdapter);

        for (int i = 0; i < district.size(); i++) {
            if (district.get(i).equals(this.district)) {
                orgDistrict.setSelection(i);
                break;
            }
        }

        orgCity.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (view != null) {
                    v = view;
                    if (position == 0)
                        ((TextView) view).setTextColor(getResources().getColor(R.color.hintcolor));
                    else
                        ((TextView) view).setTextColor(getResources().getColor(R.color.colorPrimaryDark));
                } else {
                    if (position == 0)
                        ((TextView) v).setTextColor(getResources().getColor(R.color.hintcolor));
                    else
                        ((TextView) v).setTextColor(getResources().getColor(R.color.colorPrimaryDark));
                }
                if (position != 0) {
                    strCity = city.get(position);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        orgDistrict.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (view != null) {
                    v = view;
                    if (position == 0)
                        ((TextView) view).setTextColor(getResources().getColor(R.color.hintcolor));
                    else
                        ((TextView) view).setTextColor(getResources().getColor(R.color.colorPrimaryDark));
                } else {
                    if (position == 0)
                        ((TextView) v).setTextColor(getResources().getColor(R.color.hintcolor));
                    else
                        ((TextView) v).setTextColor(getResources().getColor(R.color.colorPrimaryDark));
                }
                if (position != 0) {
                    strDistrict = district.get(position);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
    }

    @SuppressLint("StaticFieldLeak")
    class UpdateOrg extends AsyncTask<String, String, String> {

        @SuppressLint("StaticFieldLeak")
        private Context context;

        UpdateOrg(Context context) {
            this.context = context;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressBar.setVisibility(View.VISIBLE);
        }


        @Override
        protected String doInBackground(String... params) {

            String name = params[0];
            String type = params[1];
            String address = params[2];
            String landline = params[3];
            String state = params[4];
            String city = params[5];
            String district = params[6];
            String pincode = params[7];
            String details = params[8];

            try {
                HttpClient httpclient = new DefaultHttpClient();
                HttpPost httppost = new HttpPost(URLs.UPDATE_ORG);
                MultipartEntityBuilder entityBuilder = MultipartEntityBuilder.create();
                entityBuilder.setMode(HttpMultipartMode.BROWSER_COMPATIBLE);
                entityBuilder.addTextBody("org_details_id", strOrgId);
                entityBuilder.addTextBody("app_session_id", strSessionId);
                entityBuilder.addTextBody("org_name", name);
                entityBuilder.addTextBody("org_details", details);
                entityBuilder.addTextBody("org_landline_no", landline);
                entityBuilder.addTextBody("org_state", state);
                entityBuilder.addTextBody("org_city", city);
                entityBuilder.addTextBody("org_pincode", pincode);
                entityBuilder.addTextBody("org_district", district);
                entityBuilder.addTextBody("org_address", address);
                entityBuilder.addTextBody("org_type", type);

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
            progressBar.setVisibility(View.GONE);
            if (result != null) {
                try {
                    JSONObject jsonObject = new JSONObject(result);
                    if (jsonObject.getBoolean("success")) {
                        Toast.makeText(context, "Updated Successfully", Toast.LENGTH_SHORT).show();
                        startActivity(new Intent(context, OrgProfile.class).addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP));
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

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }
}
