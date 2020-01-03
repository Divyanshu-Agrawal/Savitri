package com.aaptrix.savitri.adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.aaptrix.savitri.R;
import com.aaptrix.savitri.activities.PaymentActivity;
import com.aaptrix.savitri.databeans.PaymentData;
import com.aaptrix.savitri.session.FormatDate;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

import androidx.annotation.NonNull;

public class PaymentHistoryAdapter extends ArrayAdapter<PaymentData> {

    private Context context;
    private int resource;
    private ArrayList<PaymentData> objects;

    public PaymentHistoryAdapter(@NonNull Context context, int resource, @NonNull ArrayList<PaymentData> objects) {
        super(context, resource, objects);
        this.context = context;
        this.resource = resource;
        this.objects = objects;
    }

    @SuppressLint({"ViewHolder", "SetTextI18n"})
    @NonNull
    @Override
    public View getView(int position, View view, @NonNull ViewGroup parent) {
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        assert inflater != null;
        view = inflater.inflate(resource, null);
        if (objects != null) {
            PaymentData data = objects.get(position);
            TextView orderid = view.findViewById(R.id.order_id);
            TextView planName = view.findViewById(R.id.plan_name);
            TextView amount = view.findViewById(R.id.amount);
            TextView date = view.findViewById(R.id.date);
            ImageView status = view.findViewById(R.id.status);

            if (data.getType().equals("Online")) {
                if (data.getStatus().contains("SUCCESS")) {
                    status.setImageResource(R.drawable.payment_success);
                    amount.setText(data.getMethod());
                } else {
                    status.setImageResource(R.drawable.payment_failed);
                    amount.setText("Failed");
                }
                planName.setText(data.getPlanName());
                orderid.setText(data.getOrderId());
            } else {
                status.setImageResource(R.drawable.payment_success);
                amount.setText(data.getMethod());
                planName.setText(data.getPlanName());
                orderid.setText(data.getTxnId());
            }

            FormatDate formatDate = new FormatDate(data.getDate(), "yyyy-MM-dd", "dd-MM-yyyy");
            date.setText(formatDate.format());

            view.setOnClickListener(v -> {
                Intent intent = new Intent(context, PaymentActivity.class);
                intent.putExtra("type", "history");
                intent.putExtra("paydata", data);
                context.startActivity(intent);
            });

        }
        return view;
    }
}
