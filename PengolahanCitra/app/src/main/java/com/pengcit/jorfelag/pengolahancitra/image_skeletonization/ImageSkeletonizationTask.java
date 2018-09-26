package com.pengcit.jorfelag.pengolahancitra.image_skeletonization;

import android.app.Activity;
import android.app.ProgressDialog;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.widget.ImageView;
import android.widget.TextView;

import com.pengcit.jorfelag.pengolahancitra.ocr.ChainCode;

import java.util.List;

public class ImageSkeletonizationTask extends AsyncTask<Bitmap, Void, Bitmap> {

    private ProgressDialog dialog;
    private ImageView imageView;

    public ImageSkeletonizationTask(Activity activity, ImageView imageView) {
        dialog = new ProgressDialog(activity);
        this.imageView = imageView;
    }

    @Override
    protected void onPreExecute() {
        dialog.setMessage("Skeletonizing image...");
        dialog.show();
    }

    @Override
    protected Bitmap doInBackground(Bitmap... args) {
        ImageSkeletonizer imageSkeletonizer = new ImageSkeletonizer(args[0]);
        imageSkeletonizer.process();

        return imageSkeletonizer.getBitmap();
    }

    @Override
    protected void onPostExecute(Bitmap bitmap) {
        imageView.setImageBitmap(bitmap);
        if (dialog.isShowing()) {
            dialog.dismiss();
        }
        dialog = null;
        imageView = null;
    }
}
