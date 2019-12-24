package com.aaptrix.savitri.adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Build;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;

import com.aaptrix.savitri.R;
import com.aaptrix.savitri.databeans.FaqData;
import androidx.annotation.NonNull;

public class FaqAdapter extends ArrayAdapter<FaqData> {
	
	private Context context;
	private int resource;
	private ArrayList<FaqData> object;
	private boolean isExpanded = false;
	
	public FaqAdapter(Context context, int resource, ArrayList<FaqData> object) {
		super(context, resource, object);
		this.context = context;
		this.resource = resource;
		this.object = object;
	}
	
	@SuppressLint("ViewHolder")
	@NonNull
	@Override
	public View getView(int position, View view, @NonNull ViewGroup parent) {
		LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		assert inflater != null;
		view = inflater.inflate(resource, null);
		if (object != null) {
			FaqData data = object.get(position);
			TextView question = view.findViewById(R.id.faq_question);
			ImageView expand = view.findViewById(R.id.expand_btn);
			TextView answer = view.findViewById(R.id.faq_answer);
			TextView seperater = view.findViewById(R.id.seperater);
			
			if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
				question.setText(Html.fromHtml(data.getQuestion(), Html.FROM_HTML_MODE_COMPACT));
				answer.setText(Html.fromHtml(data.getAnswer(), Html.FROM_HTML_MODE_COMPACT));
			} else {
				question.setText(Html.fromHtml(data.getQuestion()));
				answer.setText(Html.fromHtml(data.getAnswer()));
			}
			
			view.setOnClickListener(v -> {
				if (isExpanded) {
					answer.setVisibility(View.GONE);
					seperater.setVisibility(View.GONE);
					isExpanded = false;
					expand.setRotation(0);
				} else {
					isExpanded = true;
					answer.setVisibility(View.VISIBLE);
					seperater.setVisibility(View.VISIBLE);
					expand.setRotation(90);
				}
			});
		}
		return view;
	}
}
