package com.pengcit.jorfelag.pengolahancitra.image_skeletonization;

import android.app.Activity;
import android.app.ProgressDialog;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.Environment;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.pengcit.jorfelag.pengolahancitra.ocr.ChainCode;
import com.pengcit.jorfelag.pengolahancitra.util.ImageSaver;

import java.io.File;
import java.io.FileOutputStream;
import java.util.List;

public class ImageSkeletonizationTask extends AsyncTask<Bitmap, Void, Bitmap> {

    private ProgressDialog dialog;
    private ImageView imageView;
    private int seekBarValue;

    public ImageSkeletonizationTask(Activity activity, ImageView imageView, int seekBarValue) {
        dialog = new ProgressDialog(activity);
        this.imageView = imageView;

        this.seekBarValue = seekBarValue;
        Log.d("HEHEHEHE", Integer.toString(seekBarValue));
    }

    @Override
    protected void onPreExecute() {
        dialog.setMessage("Skeletonizing image...");
        dialog.show();
    }

    @Override
    protected Bitmap doInBackground(Bitmap... args) {
        ImageSkeletonizer imageSkeletonizer = new ImageSkeletonizer(args[0], seekBarValue);
        imageSkeletonizer.process();

        return imageSkeletonizer.getBitmap();
    }

    @Override
    protected void onPostExecute(Bitmap bitmap) {
        imageView.setImageBitmap(bitmap);

        ImageSaver imageSaver = new ImageSaver();
        imageSaver.saveImage(bitmap, "skeletonization");

        if (dialog.isShowing()) {
            dialog.dismiss();
        }
        dialog = null;
        imageView = null;
    }
}
