package com.aaptrix.savitri.adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebSettings;
import android.webkit.WebView;

import com.aaptrix.savitri.R;
import com.github.chrisbanes.photoview.PhotoView;
import com.squareup.picasso.Picasso;
import java.util.ArrayList;

import androidx.annotation.NonNull;
import androidx.viewpager.widget.PagerAdapter;

import static com.aaptrix.savitri.session.SharedPrefsNames.KEY_ORG_ID;
import static com.aaptrix.savitri.session.SharedPrefsNames.USER_PREFS;
import static com.aaptrix.savitri.session.URLs.DATA_URL;

public class FullscrAdapter extends PagerAdapter {

    private Context context;
    private ArrayList<String> certificate;

    public FullscrAdapter(Context context, ArrayList<String> certificate) {
        this.context = context;
        this.certificate = certificate;
    }

    @SuppressLint("SetJavaScriptEnabled")
    @NonNull
    @Override
    public Object instantiateItem(@NonNull final ViewGroup container, final int position) {
        LayoutInflater layoutInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        @SuppressLint("InflateParams") final View view = layoutInflater.inflate(R.layout.fullscr_view, null);
        PhotoView photoView = view.findViewById(R.id.fullscr_image);
        WebView webView = view.findViewById(R.id.fullscr_pdf);

        WebSettings settings = webView.getSettings();
        settings.setAllowFileAccessFromFileURLs(true);
        settings.setJavaScriptEnabled(true);

        SharedPreferences sp = context.getSharedPreferences(USER_PREFS, Context.MODE_PRIVATE);
        String strOrgId = sp.getString(KEY_ORG_ID, "");
        String url = DATA_URL + strOrgId + "/compliances/" + certificate.get(position);
        String fileExt = certificate.get(position).substring(certificate.get(position).lastIndexOf(".") + 1);
        switch (fileExt) {
            case "pdf":
                photoView.setVisibility(View.GONE);
                webView.setVisibility(View.VISIBLE);
                String pdfUrl = "https://docs.google.com/viewerng/viewer?url=" + url;
                webView.loadUrl(pdfUrl);
                break;
            case "doc":
                Picasso.with(context).load(R.drawable.doc).into(photoView);
                photoView.setVisibility(View.VISIBLE);
                webView.setVisibility(View.GONE);
                break;
            case "docx":
                Picasso.with(context).load(R.drawable.doc).into(photoView);
                photoView.setVisibility(View.VISIBLE);
                webView.setVisibility(View.GONE);
                break;
            case "xls":
                Picasso.with(context).load(R.drawable.xls).into(photoView);
                photoView.setVisibility(View.VISIBLE);
                webView.setVisibility(View.GONE);
                break;
            case "xlsx":
                Picasso.with(context).load(R.drawable.xls).into(photoView);
                photoView.setVisibility(View.VISIBLE);
                webView.setVisibility(View.GONE);
                break;
            case "ppt":
                Picasso.with(context).load(R.drawable.ppt).into(photoView);
                photoView.setVisibility(View.VISIBLE);
                webView.setVisibility(View.GONE);
                break;
            case "pptx":
                Picasso.with(context).load(R.drawable.ppt).into(photoView);
                photoView.setVisibility(View.VISIBLE);
                webView.setVisibility(View.GONE);
                break;
            case "png":
                Picasso.with(context).load(url).into(photoView);
                photoView.setVisibility(View.VISIBLE);
                webView.setVisibility(View.GONE);
                break;
            case "jpg":
                Picasso.with(context).load(url).into(photoView);
                photoView.setVisibility(View.VISIBLE);
                webView.setVisibility(View.GONE);
                break;
            case "jpeg":
                Picasso.with(context).load(url).into(photoView);
                photoView.setVisibility(View.VISIBLE);
                webView.setVisibility(View.GONE);
                break;
            default:
                Picasso.with(context).load(url).into(photoView);
                photoView.setVisibility(View.VISIBLE);
                webView.setVisibility(View.GONE);
                break;
        }
        container.addView(view);
        return view;
    }

    @Override
    public void destroyItem(@NonNull ViewGroup container, int position, @NonNull Object object) {
        container.removeView((View) object);
    }

    @Override
    public int getCount() {
        return certificate.size();
    }

    @Override
    public boolean isViewFromObject(@NonNull View view, @NonNull Object object) {
        return view == object;
    }
}
