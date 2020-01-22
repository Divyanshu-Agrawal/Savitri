package com.aaptrix.savitri.adapter;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.DownloadManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Environment;
import android.os.StrictMode;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.aaptrix.savitri.activities.FullscreenView;
import com.squareup.picasso.Picasso;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;

import com.aaptrix.savitri.R;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.PermissionChecker;

import static com.aaptrix.savitri.session.SharedPrefsNames.KEY_ORG_ID;
import static com.aaptrix.savitri.session.SharedPrefsNames.USER_PREFS;
import static com.aaptrix.savitri.activities.SplashScreen.DATA_URL;
import static android.content.Context.DOWNLOAD_SERVICE;

public class CertificateAdapter extends ArrayAdapter<String> implements ActivityCompat.OnRequestPermissionsResultCallback {

    private Context context;
    private int resource;
    private ArrayList<String> objects;
    private String strOrgId;
    private long downloadID;
    private Activity activity;
    private File file;

    public CertificateAdapter(Context context, int resource, ArrayList<String> objects, Activity activity) {
        super(context, resource, objects);
        this.context = context;
        this.resource = resource;
        this.objects = objects;
        this.activity = activity;
    }

    @SuppressLint({"ViewHolder", "ClickableViewAccessibility"})
    @NonNull
    @Override
    public View getView(int position, View view, @NonNull ViewGroup parent) {
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        assert inflater != null;
        view = inflater.inflate(resource, null);
        if (objects != null) {
            TextView title = view.findViewById(R.id.title);
            ImageView icon = view.findViewById(R.id.file_icon);
            ImageView download = view.findViewById(R.id.download_icon);
            ImageView previewImage = view.findViewById(R.id.certificate_preview_image);
            WebView previewPdf = view.findViewById(R.id.certificate_preview_pdf);
            ImageView share = view.findViewById(R.id.share_icon);

            String fileExt = objects.get(position).substring(objects.get(position).lastIndexOf(".") + 1);
            StrictMode.VmPolicy.Builder builder = new StrictMode.VmPolicy.Builder();
            StrictMode.setVmPolicy(builder.build());

            title.setText(objects.get(position));
            SharedPreferences sp = context.getSharedPreferences(USER_PREFS, Context.MODE_PRIVATE);
            strOrgId = sp.getString(KEY_ORG_ID, "");
            context.registerReceiver(onDownloadComplete, new IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE));
            String url = DATA_URL + strOrgId + "/compliances/" + objects.get(position);

            share.setOnClickListener(v -> {
                if (PermissionChecker.checkSelfPermission(context, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                    File directory = Environment.getExternalStorageDirectory();
                    file = new File(directory, "temp." + fileExt);
                    new Thread(() -> {
                        try {
                            URL u = new URL(url);
                            URLConnection conn = u.openConnection();
                            int contentLength = conn.getContentLength();
                            DataInputStream stream = new DataInputStream(u.openStream());
                            byte[] buffer = new byte[contentLength];
                            stream.readFully(buffer);
                            stream.close();
                            DataOutputStream fos = new DataOutputStream(new FileOutputStream(file));
                            fos.write(buffer);
                            fos.flush();
                            fos.close();
                            Intent intent = new Intent(Intent.ACTION_SEND);
                            intent.setDataAndType(Uri.fromFile(file), "application/octet-stream");
                            intent.putExtra(Intent.EXTRA_STREAM, Uri.parse("file://" + file));
                            context.startActivity(Intent.createChooser(intent, "Share via..."));
                        } catch (FileNotFoundException e) {
                            e.printStackTrace();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }).start();
                } else {
                    isPermissionGranted();
                }
            });

            title.setOnClickListener(v -> {
                if (PermissionChecker.checkSelfPermission(context, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                    downloadFile(objects.get(position));
                } else {
                    isPermissionGranted();
                }
            });

            icon.setOnClickListener(v -> {
                if (PermissionChecker.checkSelfPermission(context, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                    downloadFile(objects.get(position));
                } else {
                    isPermissionGranted();
                }
            });

            download.setOnClickListener(v -> {
                if (PermissionChecker.checkSelfPermission(context, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                    downloadFile(objects.get(position));
                } else {
                    isPermissionGranted();
                }
            });

            previewImage.setOnClickListener(v -> {
                Intent intent = new Intent(context, FullscreenView.class);
                intent.putStringArrayListExtra("certificate", objects);
                intent.putExtra("position", position);
                context.startActivity(intent);
            });

            previewPdf.setOnTouchListener((v, event) -> {
                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    Intent intent = new Intent(context, FullscreenView.class);
                    intent.putStringArrayListExtra("certificate", objects);
                    intent.putExtra("position", position);
                    context.startActivity(intent);
                }
                return false;
            });

            switch (fileExt) {
                case "pdf":
                    Picasso.with(context).load(R.drawable.pdf).into(icon);
                    previewImage.setVisibility(View.GONE);
                    previewPdf.setVisibility(View.VISIBLE);
                    String pdfUrl = "https://docs.google.com/viewerng/viewer?url=" + url;
                    previewPdf.loadUrl(pdfUrl);
                    break;
                case "doc":
                    Picasso.with(context).load(R.drawable.doc).into(icon);
                    Picasso.with(context).load(R.drawable.doc).into(previewImage);
                    previewImage.setVisibility(View.VISIBLE);
                    previewPdf.setVisibility(View.GONE);
                    break;
                case "docx":
                    Picasso.with(context).load(R.drawable.doc).into(icon);
                    Picasso.with(context).load(R.drawable.doc).into(previewImage);
                    previewImage.setVisibility(View.VISIBLE);
                    previewPdf.setVisibility(View.GONE);
                    break;
                case "xls":
                    Picasso.with(context).load(R.drawable.xls).into(icon);
                    Picasso.with(context).load(R.drawable.xls).into(previewImage);
                    previewImage.setVisibility(View.VISIBLE);
                    previewPdf.setVisibility(View.GONE);
                    break;
                case "xlsx":
                    Picasso.with(context).load(R.drawable.xls).into(icon);
                    Picasso.with(context).load(R.drawable.xls).into(previewImage);
                    previewImage.setVisibility(View.VISIBLE);
                    previewPdf.setVisibility(View.GONE);
                    break;
                case "ppt":
                    Picasso.with(context).load(R.drawable.ppt).into(icon);
                    Picasso.with(context).load(R.drawable.ppt).into(previewImage);
                    previewImage.setVisibility(View.VISIBLE);
                    previewPdf.setVisibility(View.GONE);
                    break;
                case "pptx":
                    Picasso.with(context).load(R.drawable.ppt).into(icon);
                    Picasso.with(context).load(R.drawable.ppt).into(previewImage);
                    previewImage.setVisibility(View.VISIBLE);
                    previewPdf.setVisibility(View.GONE);
                    break;
                case "png":
                    Picasso.with(context).load(R.drawable.png).into(icon);
                    Picasso.with(context).load(url).into(previewImage);
                    previewImage.setVisibility(View.VISIBLE);
                    previewPdf.setVisibility(View.GONE);
                    break;
                case "jpg":
                    Picasso.with(context).load(R.drawable.jpg).into(icon);
                    Picasso.with(context).load(url).into(previewImage);
                    previewImage.setVisibility(View.VISIBLE);
                    previewPdf.setVisibility(View.GONE);
                    break;
                case "jpeg":
                    Picasso.with(context).load(R.drawable.jpg).into(icon);
                    Picasso.with(context).load(url).into(previewImage);
                    previewImage.setVisibility(View.VISIBLE);
                    previewPdf.setVisibility(View.GONE);
                    break;
                default:
                    Picasso.with(context).load(R.drawable.file).into(icon);
                    Picasso.with(context).load(url).into(previewImage);
                    previewImage.setVisibility(View.VISIBLE);
                    previewPdf.setVisibility(View.GONE);
                    break;
            }
        }
        return view;
    }

    private void downloadFile(String url) {
        String path = Environment.DIRECTORY_DOWNLOADS;
        String downloadUrl = DATA_URL + strOrgId + "/compliances/" + url;
        DownloadManager.Request request = new DownloadManager.Request(Uri.parse(downloadUrl))
                .setTitle(url)
                .setDescription("Downloading")
                .setMimeType("application/octet-stream")
                .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
                .setDestinationInExternalPublicDir(path, url);
        request.allowScanningByMediaScanner();
        DownloadManager downloadManager = (DownloadManager) context.getSystemService(DOWNLOAD_SERVICE);
        assert downloadManager != null;
        downloadID = downloadManager.enqueue(request);
    }

    private BroadcastReceiver onDownloadComplete = new BroadcastReceiver() {
        @Override
        public void onReceive(Context ctx, Intent intent) {
            long id = intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1);
            if (downloadID == id) {
                Toast.makeText(context, "Download Completed", Toast.LENGTH_SHORT).show();
            }
        }
    };

    private void isPermissionGranted() {
        ActivityCompat.requestPermissions(activity,
                new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
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

    public void deleteFile() {
        if (file != null) {
            file.delete();
        }
    }
}
