package com.pengcit.jorfelag.pengolahancitra;

import android.app.Activity;
import android.app.ProgressDialog;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.TextView;

import com.pengcit.jorfelag.pengolahancitra.ocr.ChainCode;
import com.pengcit.jorfelag.pengolahancitra.ocr.OCRActivity;
import com.pengcit.jorfelag.pengolahancitra.util.MonochromeBitmap;

import java.util.List;

import static com.pengcit.jorfelag.pengolahancitra.util.MonochromeBitmap.BLACK;

public class OcrExtendedTask extends AsyncTask<Bitmap, Void, String> {

    private ProgressDialog dialog;
    private TextView textView;

    public OcrExtendedTask(Activity activity, TextView textView) {
        dialog = new ProgressDialog(activity);
        this.textView = textView;
    }

    @Override
    protected void onPreExecute() {
        dialog.setMessage("Scanning image...");
        dialog.show();
    }

    protected String doInBackground(Bitmap... args) {
        return "HEHE";
    }

    protected void onPostExecute(String result) {
        textView.setText(result);
        if (dialog.isShowing()) {
            dialog.dismiss();
        }
        dialog = null;
        textView = null;
    }
}
