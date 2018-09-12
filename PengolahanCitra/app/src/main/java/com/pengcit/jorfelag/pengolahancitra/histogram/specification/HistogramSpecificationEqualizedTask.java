package com.pengcit.jorfelag.pengolahancitra.histogram.specification;

import android.app.ProgressDialog;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.AsyncTask;
import android.widget.ImageView;

import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.series.BarGraphSeries;
import com.jjoe64.graphview.series.DataPoint;
import com.pengcit.jorfelag.pengolahancitra.util.LoopBody;
import com.pengcit.jorfelag.pengolahancitra.util.Parallel;

import java.util.HashMap;

public class HistogramSpecificationEqualizedTask extends AsyncTask<Bitmap, Void, HashMap<String, Integer[]>> {

    ImageView resultImageView;
    int[] seekBarValues;
    GraphView referencedHistogramView;
    Bitmap imageBitmap;
    private ProgressDialog dialog;

    public HistogramSpecificationEqualizedTask(HistogramSpecificationActivity activity, ImageView resultImageView, int[] seekBarValues, GraphView referencedHistogramView) {
        dialog = new ProgressDialog(activity);
        this.resultImageView = resultImageView;
        this.seekBarValues = seekBarValues;
        this.referencedHistogramView = referencedHistogramView;
    }

    @Override
    protected void onPreExecute() {
        dialog.setMessage("Creating new image, please wait...");
        dialog.show();
    }

    /**
     * TBD (src: https://www.youtube.com/watch?v=YxZUnJ_Ok2w)
     *
     * @param args Original bitmap.
     * @return
     */
    @Override
    protected HashMap<String, Integer[]> doInBackground(Bitmap... args) {
        imageBitmap = args[0];

        int[] controlPoints = seekBarValues;

        // Handle all zero values
        int sum = 0;
        for (int point : controlPoints) {
            sum += point;
        }
        if (sum == 0) {
            for (int i = 0; i < controlPoints.length; ++i) {
                controlPoints[i] = 1;
            }
        }

        // Generate histogram
        Integer[] templateCumulative = HistogramSplineInterpolator.interpolate(controlPoints);
        Integer[] templateForView = templateCumulative.clone();

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
            templateCumulative[i] += templateCumulative[i - 1];
            redValuesFrequencies[i] += redValuesFrequencies[i - 1];
            greenValuesFrequencies[i] += greenValuesFrequencies[i - 1];
            blueValuesFrequencies[i] += blueValuesFrequencies[i - 1];
        }

        // Normalize
        for (int i = 0; i < 256; i++) {
            redValuesFrequencies[i] = (255 * redValuesFrequencies[i]) / size;
            greenValuesFrequencies[i] = (255 * greenValuesFrequencies[i]) / size;
            blueValuesFrequencies[i] = (255 * blueValuesFrequencies[i]) / size;
            templateCumulative[i] = (255 * templateCumulative[i]) / templateCumulative[255];
//            Log.d("BEHE " + i, " " + templateCumulative[i] + " " + redValuesFrequencies[i] + " " + greenValuesFrequencies[i] + " " + blueValuesFrequencies[i]);
        }

        Integer[] Tred = new Integer[256];
        Integer[] Tgreen = new Integer[256];
        Integer[] Tblue = new Integer[256];

        // Histogram specification
        for (int i = 0; i < 256; i++) {
            int j;

            //Tred
            j = 255;
            while (true) {
                Tred[i] = j;
                j = j - 1;
                if (j < 0 || redValuesFrequencies[i] > templateCumulative[j]) {
                    break;
                }
            }

            //Tgreen
            j = 255;
            while (true) {
                Tgreen[i] = j;
                j = j - 1;
                if (j < 0 || greenValuesFrequencies[i] > templateCumulative[j]) {
                    break;
                }
            }

            //Tblue
            j = 255;
            while (true) {
                Tblue[i] = j;
                j = j - 1;
                if (j < 0 || blueValuesFrequencies[i] > templateCumulative[j]) {
                    break;
                }
            }
        }

        HashMap<String, Integer[]> result = new HashMap<>();
        result.put("templateForView", templateForView);
        result.put("Tred", Tred);
        result.put("Tgreen", Tgreen);
        result.put("Tblue", Tblue);

        return result;
    }

    /**
     * Display image histogram.
     *
     * @param result referenced histogram.
     */
    protected void onPostExecute(HashMap<String, Integer[]> result) {
        Integer[] template = result.get("templateForView");

        BarGraphSeries series = new BarGraphSeries<>(generateData(template));
        series.setColor(Color.BLACK);

        referencedHistogramView.addSeries(series);
        referencedHistogramView.setTitle("Referenced Histogram");
        referencedHistogramView.getViewport().setXAxisBoundsManual(true);
        referencedHistogramView.getViewport().setMinX(0);
        referencedHistogramView.getViewport().setMaxX(255);
        referencedHistogramView.getViewport().setYAxisBoundsManual(true);
        referencedHistogramView.getViewport().setMinY(0);
        referencedHistogramView.getViewport().setMaxY(100);

        // Enable scaling and scrolling
        referencedHistogramView.getViewport().setScalable(true);
        referencedHistogramView.getViewport().setScalableY(true);

        final Integer[] Tred = result.get("Tred");
        final Integer[] Tgreen = result.get("Tgreen");
        final Integer[] Tblue = result.get("Tblue");

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

                    processedPixels[j] = (0xFF<<24) | (Tred[red]<<16) | (Tgreen[green]<<8) | Tblue[blue];
                }

                processedBitmap.setPixels(processedPixels, 0, width, 0, i, width, 1);
            }
        });

        resultImageView.setImageBitmap(processedBitmap);

        if (dialog.isShowing()) {
            dialog.dismiss();
        }
    }

    private DataPoint[] generateData(Integer[] colorValuesFrequencies) {
        DataPoint[] values = new DataPoint[colorValuesFrequencies.length];
        for (int i = 0; i < values.length; i++) {
            DataPoint v = new DataPoint(i, colorValuesFrequencies[i]);
            values[i] = v;
        }
        return values;
    }
}
