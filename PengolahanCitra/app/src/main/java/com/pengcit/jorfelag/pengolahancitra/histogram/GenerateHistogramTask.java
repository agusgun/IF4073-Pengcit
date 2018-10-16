package com.pengcit.jorfelag.pengolahancitra.histogram;

import android.app.ProgressDialog;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.AsyncTask;

import com.jjoe64.graphview.GraphView;
import com.pengcit.jorfelag.pengolahancitra.R;
import com.pengcit.jorfelag.pengolahancitra.util.LoopBody;
import com.pengcit.jorfelag.pengolahancitra.util.Parallel;

import java.lang.ref.WeakReference;
import java.util.HashMap;
import java.util.Map;

public class GenerateHistogramTask extends AsyncTask<Bitmap, Void, HashMap<String, Integer[]>> {
    private WeakReference<ShowHistogramFragment> fragmentRef;
    private ProgressDialog dialog;

    GenerateHistogramTask(ShowHistogramFragment fr) {
        fragmentRef = new WeakReference<>(fr);
        dialog = new ProgressDialog(fr.getContext());
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        dialog.setMessage("Creating image histogram, please wait...");
        dialog.show();
    }

    @Override
    protected HashMap<String, Integer[]> doInBackground(Bitmap... params) {
        final Bitmap imageBitmap = params[0];

        final Integer[] redValuesFrequencies = new Integer[256];
        final Integer[] greenValuesFrequencies = new Integer[256];
        final Integer[] blueValuesFrequencies = new Integer[256];
        final Integer[] grayValuesFrequencies = new Integer[256];

        for (int i = 0; i < 256; i++) {
            redValuesFrequencies[i] = 0;
            greenValuesFrequencies[i] = 0;
            blueValuesFrequencies[i] = 0;
            grayValuesFrequencies[i] = 0;
        }

        final Bitmap processedBitmap = imageBitmap.copy(Bitmap.Config.ARGB_8888, true);

        final int width = processedBitmap.getWidth();
        final int height = processedBitmap.getHeight();

        Parallel.For(0, height, new LoopBody<Integer>() {
            @Override
            public void run(Integer i) {
                int[] processedPixels = new int[width];
                processedBitmap.getPixels(processedPixels, 0, width, 0, i, width, 1);

                for (int j = 0; j < width; ++j) {
                    int pixelColor = processedPixels[j];

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
        });

        HashMap<String, Integer[]> results = new HashMap<>();
        results.put("red", redValuesFrequencies);
        results.put("green", greenValuesFrequencies);
        results.put("blue", blueValuesFrequencies);
        results.put("gray", grayValuesFrequencies);
        return results;
    }

    @Override
    protected void onPostExecute(HashMap<String, Integer[]> result) {
        ShowHistogramFragment fr = fragmentRef.get();
        if (fr == null
                || fr.getActivity() == null
                || fr.getActivity().isFinishing()) return;

        if (dialog.isShowing()) {
            dialog.dismiss();
        }

        Map<String, GraphView> histogram = fr.getHistogram();

        fr.setUpHistogram(histogram.get("red"), result.get("red"),
                fr.getString(R.string.red_histogram_title), Color.RED);
        fr.setUpHistogram(histogram.get("green"), result.get("green"),
                fr.getString(R.string.green_histogram_title), Color.GREEN);
        fr.setUpHistogram(histogram.get("blue"), result.get("blue"),
                fr.getString(R.string.blue_histogram_title), Color.BLUE);
        fr.setUpHistogram(histogram.get("gray"), result.get("gray"),
                fr.getString(R.string.grayscale_histogram_title), Color.GRAY);
    }

}
