package com.pengcit.jorfelag.pengolahancitra.ocr;

import android.app.Activity;
import android.app.ProgressDialog;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.TextView;

import com.pengcit.jorfelag.pengolahancitra.util.MonochromeBitmap;

import java.util.List;

import static com.pengcit.jorfelag.pengolahancitra.util.MonochromeBitmap.BLACK;

public class OcrTask extends AsyncTask<Bitmap, Void, String> {

    private ProgressDialog dialog;
    private List<ChainCode> chainCodes;
    private TextView textView;

    public OcrTask(Activity activity, List<ChainCode> chainCodes, TextView textView) {
        dialog = new ProgressDialog(activity);
        this.chainCodes = chainCodes;
        this.textView = textView;
    }

    @Override
    protected void onPreExecute() {
        dialog.setMessage("Scanning image...");
        dialog.show();
    }

    protected String doInBackground(Bitmap... args) {
        final Bitmap imageBitmap = args[0];
        final Bitmap monochromeBitmap = MonochromeBitmap.createMonochromeBitmap(imageBitmap);

        StringBuilder result = new StringBuilder();

        for (int x = 0; x < monochromeBitmap.getWidth(); x++) {
            for (int y = 0; y < monochromeBitmap.getHeight(); y++) {
                if ((monochromeBitmap.getPixel(x, y) & 0x000000FF) == BLACK) {
                    ChainCode chainCode = new ChainCode(monochromeBitmap, x, y, "");
                    double dissimilarity = 1000;
                    double temp;
                    for (ChainCode c : chainCodes) {
                        temp = ChainCode.calculateDissimilarity(chainCode, c);
                        Log.i("A", "C " +  temp + " " + c.getCode());
                        if (temp < dissimilarity) {
                            dissimilarity = temp;
                            chainCode.setLabel(c.getLabel());
                        }
                    }
                    result.append(chainCode.getLabel());
                    MonochromeBitmap.removeObject(monochromeBitmap, x, y);
                }
            }
        }

        if(result.length() == 0) {
            return "There's nothing to read";
        } else {
            return result.toString();
        }
    }

    protected void onPostExecute(String result) {
        textView.setText(result);
        if (dialog.isShowing()) {
            dialog.dismiss();
        }
        dialog = null;
        chainCodes = null;
        textView = null;
    }
}
