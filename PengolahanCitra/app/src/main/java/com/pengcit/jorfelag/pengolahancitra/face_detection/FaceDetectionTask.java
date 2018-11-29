package com.pengcit.jorfelag.pengolahancitra.face_detection;

import android.app.ProgressDialog;
import android.graphics.Bitmap;
import android.os.AsyncTask;

import java.lang.ref.WeakReference;

public class FaceDetectionTask extends AsyncTask<Bitmap, Void, Bitmap> {

    private WeakReference<FaceDetectionFragment> fragmentRef;
    private ProgressDialog dialog;

    protected int offset;

    public FaceDetectionTask(FaceDetectionFragment fr) {
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
    protected Bitmap doInBackground(Bitmap... bitmaps) {
        return new FaceDetector(bitmaps[0]).getBitmap();
    }

    @Override
    protected void onPostExecute(Bitmap result) {
        FaceDetectionFragment fr = fragmentRef.get();
        if (fr == null
                || fr.getActivity() == null
                || fr.getActivity().isFinishing()) return;

        if (dialog.isShowing()) {
            dialog.dismiss();
        }

        fr.setResultImageView(result);
    }
}
