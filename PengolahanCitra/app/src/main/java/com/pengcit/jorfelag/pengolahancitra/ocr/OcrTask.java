package com.pengcit.jorfelag.pengolahancitra.ocr;

import android.app.ProgressDialog;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.util.Log;
import android.util.Pair;

import java.lang.ref.WeakReference;

public class OCRTask extends AsyncTask<Bitmap, Void, Pair<Bitmap, String>> {

    private WeakReference<OCRFragment> fragmentRef;
    private ProgressDialog dialog;

    private int threshold;
    private int distanceThreshold;
    private int counterThreshold;

    public OCRTask(OCRFragment fr) {
        fragmentRef = new WeakReference<>(fr);
        dialog = new ProgressDialog(fr.getContext());
        threshold = fr.getThreshold();
        distanceThreshold = fr.getDistanceThreshold();
        counterThreshold = fr.getCounterThreshold();

        Log.d("Threshold:", Integer.toString(distanceThreshold) + " " + Integer.toString(counterThreshold));
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        dialog.setMessage("Processing image, please wait...");
        dialog.show();
    }

    @Override
    protected Pair<Bitmap, String> doInBackground(Bitmap... bitmaps) {
        ImageSkeletonizer imageSkeletonizer = new ImageSkeletonizer(bitmaps[0], threshold);
        imageSkeletonizer.process(distanceThreshold, counterThreshold);

        return new Pair<>(imageSkeletonizer.getBitmap(), imageSkeletonizer.predict());
    }

    @Override
    protected void onPostExecute(Pair<Bitmap, String> result) {
        OCRFragment fr = fragmentRef.get();
        if (fr == null
                || fr.getActivity() == null
                || fr.getActivity().isFinishing()) return;

        if (dialog.isShowing()) {
            dialog.dismiss();
        }

        Bitmap bitmap = result.first;
        String prediction = result.second;

        fr.setResultImage(bitmap);
        fr.setResultText(prediction);
    }
}
