package com.aaptrix.savitri.adapter;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;

import com.aaptrix.savitri.R;
import com.aaptrix.savitri.databeans.CommentData;
import com.aaptrix.savitri.session.FormatDate;

import java.util.ArrayList;

public class CommentAdapter extends ArrayAdapter<CommentData> {

    private Context context;
    private int resource;
    private ArrayList<CommentData> objects;
    private Activity activity;

    public CommentAdapter(@NonNull Context context, int resource, ArrayList<CommentData> objects, Activity activity) {
        super(context, resource, objects);
        this.context = context;
        this.resource = resource;
        this.objects = objects;
        this.activity = activity;
    }

    @SuppressLint({"ViewHolder", "SetTextI18n"})
    @NonNull
    @Override
    public View getView(int position, View view, @NonNull ViewGroup parent) {
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        assert inflater != null;
        view = inflater.inflate(resource, null);

        if (objects != null) {
            CommentData data = objects.get(position);
            TextView username = view.findViewById(R.id.commented_by);
            TextView comment = view.findViewById(R.id.comment);
            TextView date = view.findViewById(R.id.date);

            username.setText("By : " + data.getName());
            comment.setText(data.getComment());

            FormatDate formatDate = new FormatDate(data.getDate(), "yyyy-MM-dd hh:mm:ss", "dd-MM-yyyy hh:mm");
            date.setText(formatDate.format());

            CardView cardView = view.findViewById(R.id.cardview);

            DisplayMetrics metrics = new DisplayMetrics();
            activity.getWindowManager().getDefaultDisplay().getMetrics(metrics);
            int width = metrics.widthPixels;

            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(-1, -1);
            params.width = (width/2) + (width/4);
            int right = (int) context.getResources().getDimension(R.dimen._10sdp);
            int left = (int) context.getResources().getDimension(R.dimen._10sdp);
            int top = (int) context.getResources().getDimension(R.dimen._5sdp);
            int bottom = (int) context.getResources().getDimension(R.dimen._5sdp);
            params.setMargins(left, top, right, bottom);
            cardView.setLayoutParams(params);
        }


        return view;
    }
}
