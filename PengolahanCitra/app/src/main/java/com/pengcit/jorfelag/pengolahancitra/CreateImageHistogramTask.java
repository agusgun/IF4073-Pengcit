package com.pengcit.jorfelag.pengolahancitra;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.AsyncTask;

import java.util.HashMap;
import java.util.Map;

public class CreateImageHistogramTask extends AsyncTask<Bitmap, Void, HashMap<String, Integer[]>> {

    private ProgressDialog dialog;
    private Activity activity;

    public CreateImageHistogramTask(MainActivity activity) {
        dialog = new ProgressDialog(activity);
        this.activity = activity;
    }

    @Override
    protected void onPreExecute() {
        dialog.setMessage("Creating image histogram, please wait...");
        dialog.show();
    }

    /**
     * Extract RGB and grayscale values' frequencies from the given image bitmap.
     * @param args The image bitmap to be extracted.
     * @return RGB and grayscale values' frequencies.
     */
    protected HashMap<String, Integer[]> doInBackground(Bitmap... args) {
        Bitmap imageBitmap = args[0];

        Integer[] redValuesFrequencies = new Integer[256];
        Integer[] greenValuesFrequencies = new Integer[256];
        Integer[] blueValuesFrequencies = new Integer[256];
        Integer[] grayValuesFrequencies = new Integer[256];

        for (int i = 0; i < 256; i++) {
            redValuesFrequencies[i] = 0;
            greenValuesFrequencies[i] = 0;
            blueValuesFrequencies[i] = 0;
            grayValuesFrequencies[i] = 0;
        }

        Bitmap processedBitmap = imageBitmap.copy(Bitmap.Config.ARGB_8888, true);
        for (int x = 0; x < processedBitmap.getWidth(); x++) {
            for (int y = 0; y < processedBitmap.getHeight(); y++) {
                int pixelColor = processedBitmap.getPixel(x, y);

                int red = (pixelColor & 0x00FF0000) >> 16;
                int green = (pixelColor & 0x0000FF00) >> 8;
                int blue = (pixelColor & 0x000000FF);
                int gray = (red + green + blue) / 3;

                redValuesFrequencies[red]++;
                greenValuesFrequencies[green]++;
                blueValuesFrequencies[blue]++;
                grayValuesFrequencies[gray]++;
            }
        }

        HashMap<String, Integer[]> results = new HashMap<>();
        results.put("red", redValuesFrequencies);
        results.put("green", greenValuesFrequencies);
        results.put("blue", blueValuesFrequencies);
        results.put("gray", grayValuesFrequencies);
        return results;
    }

    /**
     * Display image histogram.
     * @param result RGB and grayscale values' frequencies.
     */
    protected void onPostExecute(HashMap<String, Integer[]> result) {
        if (dialog.isShowing()) {
            dialog.dismiss();
        }
        Intent intent = new Intent(activity, HistogramActivity.class);
        intent.putExtra("colorMap", result);
        activity.startActivity(intent);
    }
}