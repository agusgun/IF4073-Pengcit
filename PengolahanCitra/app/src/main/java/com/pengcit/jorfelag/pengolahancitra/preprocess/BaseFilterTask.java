package com.pengcit.jorfelag.pengolahancitra.preprocess;

import android.app.ProgressDialog;
import android.graphics.Bitmap;
import android.os.AsyncTask;

import java.lang.ref.WeakReference;

public abstract class BaseFilterTask extends AsyncTask<Bitmap, Void, Bitmap> {

    public static int MAX_WIDTH = 1080;
    public static int MAX_HEIGHT = 1080;

    private WeakReference<PreprocessOperatorFragment> fragmentRef;
    private ProgressDialog dialog;

    public BaseFilterTask(PreprocessOperatorFragment fr) {
        fragmentRef = new WeakReference<>(fr);
        dialog = new ProgressDialog(fr.getContext());
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        dialog.setMessage("Processing image, please wait...");
        dialog.show();
    }

    @Override
    protected void onPostExecute(Bitmap result) {
        PreprocessOperatorFragment fr = fragmentRef.get();
        if (fr == null
                || fr.getActivity() == null
                || fr.getActivity().isFinishing()) return;

        if (dialog.isShowing()) {
            dialog.dismiss();
        }

        fr.setResultImageView(result);
    }

    protected Bitmap resizeBitmap(Bitmap bitmap) {
        int width = bitmap.getWidth();
        int height = bitmap.getHeight();
        int resizedWidth, resizedHeight;

        if (width >= height) {   // Landscape
            resizedWidth = Math.min(width, MAX_WIDTH);
            resizedHeight = resizedWidth * height / width;
        } else {    // Portrait
            resizedHeight = Math.min(height, MAX_HEIGHT);
            resizedWidth = resizedHeight *  width / height;
        }

        return Bitmap.createScaledBitmap(bitmap, resizedWidth, resizedHeight, true);
    }
}
