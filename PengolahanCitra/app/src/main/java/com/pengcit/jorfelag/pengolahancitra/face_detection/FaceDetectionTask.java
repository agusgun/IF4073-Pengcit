package com.pengcit.jorfelag.pengolahancitra.face_detection;

import android.app.ProgressDialog;
import android.graphics.Bitmap;
import android.os.AsyncTask;

import java.lang.ref.WeakReference;
import java.util.List;

public class FaceDetectionTask extends AsyncTask<Bitmap, Void, FaceDetector> {

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
    protected FaceDetector doInBackground(Bitmap... bitmaps) {
        return new FaceDetector(bitmaps[0]);
    }

    @Override
    protected void onPostExecute(FaceDetector result) {
        FaceDetectionFragment fr = fragmentRef.get();
        if (fr == null
                || fr.getActivity() == null
                || fr.getActivity().isFinishing()) return;

        if (dialog.isShowing()) {
            dialog.dismiss();
        }
        StringBuilder sb = new StringBuilder();
        List<String> recognitionResult = result.getLabels();
        List<Double> deltas = result.getDeltas();

        if (recognitionResult.size() > 1) {
            for (int i = 0; i < recognitionResult.size(); i++) {
                sb.append(i + 1);
                sb.append(". ");
                sb.append(recognitionResult.get(i));

                // DEBUG
                sb.append(", error: ");
                sb.append(String.format("%.2f", deltas.get(i)));

                if (i != (recognitionResult.size() - 1)) {
                    sb.append("\n");
                }
            }
        } else if (recognitionResult.size() == 1) {
            sb.append(recognitionResult.get(0));
            // DEBUG
            sb.append(", error: ");
            sb.append(String.format("%.2f", deltas.get(0)));
        } else {
            sb.append("No face detected");
        }
        fr.setResultImageView(result.getBitmap());
        fr.setRecognitionResultTextView(sb.toString());
    }
}
