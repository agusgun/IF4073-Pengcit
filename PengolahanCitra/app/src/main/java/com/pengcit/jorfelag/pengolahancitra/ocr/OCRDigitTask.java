package com.pengcit.jorfelag.pengolahancitra.ocr;

import android.app.ProgressDialog;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.widget.TextView;


public class OCRDigitTask extends AsyncTask<Bitmap, Void, String> {

    private ProgressDialog dialog;
    OCRActivity activity;
    TextView resultTextView;

    public OCRDigitTask(OCRActivity activity, TextView resultTextView) {
        dialog = new ProgressDialog(activity);
        this.resultTextView = resultTextView;
    }

    @Override
    protected void onPreExecute() {
        dialog.setMessage("Creating new image, please wait...");
        dialog.show();
    }


    protected String doInBackground(Bitmap... args) {
        Bitmap imageBitmap = args[0];

        ChainCode chainCode = new ChainCode(imageBitmap, Integer.toString(0));
        String code = chainCode.getCode();

        return code;
    }

    /**
     * Display image histogram.
     * @param result RGB and grayscale values' frequencies.
     */
    protected void onPostExecute(String result) {
        if (dialog.isShowing()) {
            dialog.dismiss();
        }
        resultTextView.setText("HEHE");
    }
}
