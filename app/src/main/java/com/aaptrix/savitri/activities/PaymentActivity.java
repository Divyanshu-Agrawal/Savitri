package com.aaptrix.savitri.activities;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.ColorStateList;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.aaptrix.savitri.R;
import com.aaptrix.savitri.databeans.PaymentData;
import com.aaptrix.savitri.databeans.PlansData;
import com.aaptrix.savitri.asyncclass.UploadPayment;
import com.aaptrix.savitri.session.FormatDate;
import com.squareup.picasso.Picasso;

import static com.aaptrix.savitri.session.SharedPrefsNames.KEY_ORG_ID;
import static com.aaptrix.savitri.session.SharedPrefsNames.KEY_PLAN_EXPIRE_DATE;
import static com.aaptrix.savitri.session.SharedPrefsNames.KEY_USER_ID;
import static com.aaptrix.savitri.session.SharedPrefsNames.USER_PREFS;
import static com.aaptrix.savitri.activities.SplashScreen.DATA_URL;

public class PaymentActivity extends AppCompatActivity {

    PlansData plansData;
    PaymentData paymentData;
    Bundle response;
    String type, mobileNo, email, orgId, userId, planExp;
    TextView paymentStatus, orderId, planName, paymentMode, transId, transDate, transAmount, transDetails, idTitle;
    LinearLayout transLayout, offlineLayout;
    ImageView statusImg, transDoc;
    SharedPreferences sp;
    Toolbar toolbar;

    @SuppressLint({"SetTextI18n", "NewApi"})
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_payment);
        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("");
        type = getIntent().getStringExtra("type");
        paymentStatus = findViewById(R.id.payment_status);
        orderId = findViewById(R.id.order_id);
        planName = findViewById(R.id.plan_name);
        paymentMode = findViewById(R.id.payment_mode);
        transId = findViewById(R.id.transaction_id);
        transDate = findViewById(R.id.transaction_date);
        transAmount = findViewById(R.id.transaction_amount);
        statusImg = findViewById(R.id.payment_img);
        offlineLayout = findViewById(R.id.offline_layout);
        transDetails = findViewById(R.id.transaction_details);
        idTitle = findViewById(R.id.id_title);
        transDoc = findViewById(R.id.transaction_documents);
        transLayout = findViewById(R.id.transaction_layout);
        sp = getSharedPreferences(USER_PREFS, Context.MODE_PRIVATE);
        orgId = sp.getString(KEY_ORG_ID, "");
        userId = sp.getString(KEY_USER_ID, "");
        planExp = sp.getString(KEY_PLAN_EXPIRE_DATE, "");

        if (type.equals("payment")) {
            plansData = (PlansData) getIntent().getSerializableExtra("plansdata");
            response = getIntent().getBundleExtra("response");
            mobileNo = getIntent().getStringExtra("mobile");
            email = getIntent().getStringExtra("email");

            UploadPayment uploadPayment = new UploadPayment(this, response);
            uploadPayment.execute(orgId, userId, mobileNo, email, plansData.getId(), planExp, plansData.getName());

            if (response.getString("STATUS").equals("TXN_SUCCESS")) {
                paymentStatus.setText("Payment Successfull");
                statusImg.setImageResource(R.drawable.payment_success);
                transId.setText(response.getString("TXNID"));
                transAmount.setText(response.getString("TXNAMOUNT"));
                String[] date = response.getString("TXNDATE").split(" ");
                FormatDate formatDate = new FormatDate(date[0], "yyyy-MM-dd", "dd-MM-yyyy");
                transDate.setText(formatDate.format());
            } else if (response.getString("STATUS").equals("PENDING")) {
                paymentStatus.setText("Payment Pending");
                statusImg.setImageResource(R.drawable.payment_failed);
                statusImg.setImageTintList(ColorStateList.valueOf(getResources().getColor(R.color.yellow)));
                transId.setText(response.getString("TXNID"));
                transAmount.setText(response.getString("TXNAMOUNT"));
                String[] date = response.getString("TXNDATE").split(" ");
                FormatDate formatDate = new FormatDate(date[0], "yyyy-MM-dd", "dd-MM-yyyy");
                transDate.setText(formatDate.format());
            } else {
                paymentStatus.setText("Payment Failed");
                statusImg.setImageResource(R.drawable.payment_failed);
                transLayout.setVisibility(View.GONE);
            }

            orderId.setText(response.getString("ORDERID"));
            planName.setText(plansData.getName());
            paymentMode.setText(response.getString("BANKNAME"));
        } else if (type.equals("history")) {
            paymentData = (PaymentData) getIntent().getSerializableExtra("paydata");
            if (paymentData.getType().equals("Offline")) {
                offlineLayout.setVisibility(View.VISIBLE);
                transLayout.setVisibility(View.GONE);
                idTitle.setText("Transaction ID");
                paymentStatus.setText("Payment Successfull");
                statusImg.setImageResource(R.drawable.payment_success);
                orderId.setText(paymentData.getTxnId());
                FormatDate formatDate = new FormatDate(paymentData.getDate(), "yyyy-MM-dd", "dd-MM-yyyy");
                transDate.setText(formatDate.format());
                transDetails.setText(paymentData.getDesc());
                planName.setText(paymentData.getPlanName());
                paymentMode.setText(paymentData.getMethod());
                String url = DATA_URL + orgId + "/offlinePayment/" + paymentData.getImage();
                Picasso.with(this).load(url).into(transDoc);
            } else if (paymentData.getType().equals("Online")) {
                orderId.setText(paymentData.getOrderId());
                planName.setText(paymentData.getPlanName());
                paymentMode.setText(paymentData.getMethod());

                if (paymentData.getStatus().equals("TXN_SUCCESS")) {
                    paymentStatus.setText("Payment Successfull");
                    statusImg.setImageResource(R.drawable.payment_success);
                    transId.setText(paymentData.getTxnId());
                    transAmount.setText(paymentData.getAmount());
                    FormatDate formatDate = new FormatDate(paymentData.getDate(), "yyyy-MM-dd", "dd-MM-yyyy");
                    transDate.setText(formatDate.format());
                } else if (paymentData.getStatus().equals("PENDING")) {
                    paymentStatus.setText("Payment Successfull");
                    statusImg.setImageResource(R.drawable.payment_failed);
                    statusImg.setImageTintList(ColorStateList.valueOf(getResources().getColor(R.color.yellow)));
                    transId.setText(paymentData.getTxnId());
                    transAmount.setText(paymentData.getAmount());
                    FormatDate formatDate = new FormatDate(paymentData.getDate(), "yyyy-MM-dd", "dd-MM-yyyy");
                    transDate.setText(formatDate.format());
                } else {
                    paymentStatus.setText("Payment Failed");
                    statusImg.setImageResource(R.drawable.payment_failed);
                    transLayout.setVisibility(View.GONE);
                }
            }
        }
    }

    @Override
    public void onBackPressed() {
        if (type.equals("payment"))
            startActivity(new Intent(this, Dashboard.class).setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP));
        else
            super.onBackPressed();
        finish();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
        }
        return super.onOptionsItemSelected(item);
    }
}
