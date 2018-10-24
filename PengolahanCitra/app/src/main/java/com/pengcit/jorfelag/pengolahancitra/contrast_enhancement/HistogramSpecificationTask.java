package com.pengcit.jorfelag.pengolahancitra.contrast_enhancement;

import android.app.ProgressDialog;
import android.graphics.Bitmap;
import android.os.AsyncTask;

import com.pengcit.jorfelag.pengolahancitra.histogram.HistogramSplineInterpolator;
import com.pengcit.jorfelag.pengolahancitra.util.LoopBody;
import com.pengcit.jorfelag.pengolahancitra.util.Parallel;

import java.lang.ref.WeakReference;

public class HistogramSpecificationTask extends AsyncTask<Bitmap, Void, Bitmap> {

    private WeakReference<HistogramSpecificationFragment> fragmentRef;
    private ProgressDialog dialog;

    private Integer[] histogramValues;

    public HistogramSpecificationTask(HistogramSpecificationFragment fr) {
        fragmentRef = new WeakReference<>(fr);
        dialog = new ProgressDialog(fr.getContext());
        histogramValues = fr.getHistogramValues();
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        dialog.setMessage("Processing image, please wait...");
        dialog.show();
    }

    /**
     * TBD (src: https://www.youtube.com/watch?v=YxZUnJ_Ok2w)
     *
     * @param args Original bitmap.
     * @return
     */
    @Override
    protected Bitmap doInBackground(Bitmap... args) {
        final Bitmap imageBitmap = args[0];

        Integer[] templateCumulative = histogramValues;

        final Integer[] redValuesFrequencies = new Integer[256];
        final Integer[] greenValuesFrequencies = new Integer[256];
        final Integer[] blueValuesFrequencies = new Integer[256];

        for (int i = 0; i < 256; i++) {
            redValuesFrequencies[i] = 0;
            greenValuesFrequencies[i] = 0;
            blueValuesFrequencies[i] = 0;
        }

        final Bitmap processedBitmap = imageBitmap.copy(Bitmap.Config.ARGB_8888, true);

        final int width = processedBitmap.getWidth();
        final int height = processedBitmap.getHeight();
        final int size = height * width;

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

                    redValuesFrequencies[red]++;
                    greenValuesFrequencies[green]++;
                    blueValuesFrequencies[blue]++;
                }
            }
        });

        // Cumulative Distribution
        for (int i = 1; i < 256; i++) {
            redValuesFrequencies[i] += redValuesFrequencies[i - 1];
        }
        for (int i = 1; i < 256; i++) {
            greenValuesFrequencies[i] += greenValuesFrequencies[i - 1];
        }
        for (int i = 1; i < 256; i++) {
            blueValuesFrequencies[i] += blueValuesFrequencies[i - 1];
        }
        for (int i = 1; i < 256; i++) {
            templateCumulative[i] += templateCumulative[i - 1];
        }

        // Normalize
        for (int i = 0; i < 256; i++) {
            redValuesFrequencies[i] = (255 * redValuesFrequencies[i]) / size;
        }
        for (int i = 0; i < 256; i++) {
            greenValuesFrequencies[i] = (255 * greenValuesFrequencies[i]) / size;
        }
        for (int i = 0; i < 256; i++) {
            blueValuesFrequencies[i] = (255 * blueValuesFrequencies[i]) / size;
        }
        for (int i = 0; i < 256; i++) {
            templateCumulative[i] = (255 * templateCumulative[i]) / templateCumulative[255];
        }

        final Integer[] Tred = new Integer[256];
        final Integer[] Tgreen = new Integer[256];
        final Integer[] Tblue = new Integer[256];

        // Histogram specification
        for (int i = 255; i >= 0; i--) {
            int j = (i < 255) ? Tred[i + 1] : 255;
            do {
                Tred[i] = j--;
            } while (j >= 0 && redValuesFrequencies[i] <= templateCumulative[j]);
        }

        for (int i = 255; i >= 0; i--) {
            int j = (i < 255) ? Tgreen[i + 1] : 255;
            do {
                Tgreen[i] = j--;
            } while (j >= 0 && greenValuesFrequencies[i] <= templateCumulative[j]);
        }

        for (int i = 255; i >= 0; i--) {
            int j = (i < 255) ? Tblue[i + 1] : 255;
            do {
                Tblue[i] = j--;
            } while (j >= 0 && blueValuesFrequencies[i] <= templateCumulative[j]);
        }

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

                    processedPixels[j] = (0xFF << 24) | (Tred[red] << 16) | (Tgreen[green] << 8) | Tblue[blue];
                }

                processedBitmap.setPixels(processedPixels, 0, width, 0, i, width, 1);
            }
        });

        return processedBitmap;
    }

    @Override
    protected void onPostExecute(Bitmap result) {
        HistogramSpecificationFragment fr = fragmentRef.get();
        if (fr == null
                || fr.getActivity() == null
                || fr.getActivity().isFinishing()) return;

        if (dialog.isShowing()) {
            dialog.dismiss();
        }

        fr.setResultImageView(result);
    }


}
