package com.pengcit.jorfelag.pengolahancitra.ocr;

import android.app.ProgressDialog;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.util.Log;
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

        int begin[];
        begin = getFirstPixel(imageBitmap);
        int x = begin[0];
        int y = begin[1];
        ChainCode chainCode = new ChainCode(imageBitmap, x, y, Integer.toString(0));
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

    /**
     *
     * @param imageBitmap the bitmap of the image
     * @return array of integer with size 2 (x,y)
     */
    private int[] getFirstPixel(Bitmap imageBitmap) {
        final Bitmap processedBitmap = imageBitmap.copy(Bitmap.Config.ARGB_8888, true);
        final int width = processedBitmap.getWidth();
        final int height = processedBitmap.getHeight();

        //x,y
        int result[] = new int[2];

        //Don't Parallalize
        int i = 0;
        int j = 0;
        boolean flag = false;
        while (i < width && !flag) {
            j = 0;
            while (j < height && !flag) {
                int pixelColor = processedBitmap.getPixel(i, j);

                int red = (pixelColor & 0x00FF0000) >> 16;
                int green = (pixelColor & 0x0000FF00) >> 8;
                int blue = (pixelColor & 0x000000FF);
                int gray = (red + green + blue) / 3;

                // <= 128 then black
                if (gray <= 128) {
                    Log.d("x,y", Integer.toString(i) + " " + Integer.toString(j));
                    result[0] = i;
                    result[1] = j;
                    flag = true;
                }
                j++;
            }
            i++;
        }
        return result;
    }
}
