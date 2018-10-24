package com.pengcit.jorfelag.pengolahancitra.image_skeletonization;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ProgressDialog;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.util.Pair;
import android.widget.ImageView;
import android.widget.TextView;

import com.pengcit.jorfelag.pengolahancitra.util.ImageSaver;

public class ImageSkeletonizationTask extends AsyncTask<Bitmap, Void, Pair<Bitmap, String>> {

    private ProgressDialog dialog;
    private ImageView imageView;
    private TextView textView;
    private int seekBarValue;

    public ImageSkeletonizationTask(Activity activity, ImageView imageView, TextView textView, int seekBarValue) {
        dialog = new ProgressDialog(activity);
        this.imageView = imageView;
        this.textView = textView;

        this.seekBarValue = seekBarValue;
    }

    @Override
    protected void onPreExecute() {
        dialog.setMessage("Skeletonizing image...");
        dialog.show();
    }

    @Override
    protected Pair<Bitmap, String> doInBackground(Bitmap... args) {
        ImageSkeletonizer imageSkeletonizer = new ImageSkeletonizer(args[0], seekBarValue);
        imageSkeletonizer.process(12, 50);

        return new Pair<>(imageSkeletonizer.getBitmap(), imageSkeletonizer.predict());
    }

    @SuppressLint("SetTextI18n")
    @Override
    protected void onPostExecute(Pair<Bitmap, String> result) {
        Bitmap bitmap = result.first;
        String prediction = result.second;

        imageView.setImageBitmap(bitmap);
        textView.setText(prediction);

        ImageSaver imageSaver = new ImageSaver();
        imageSaver.saveImage(bitmap, "skeletonization");

        if (dialog.isShowing()) {
            dialog.dismiss();
        }
        dialog = null;
        imageView = null;
    }
}
