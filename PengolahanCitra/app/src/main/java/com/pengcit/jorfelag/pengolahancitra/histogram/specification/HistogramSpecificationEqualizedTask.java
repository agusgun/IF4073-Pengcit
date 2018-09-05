package com.pengcit.jorfelag.pengolahancitra.histogram.specification;

import android.app.ProgressDialog;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.ImageView;

public class HistogramSpecificationEqualizedTask extends AsyncTask<Bitmap, Void, Bitmap> {

    private ProgressDialog dialog;
    HistogramSpecificationActivity activity;
    ImageView resultImageView;
    int seekBarValue1, seekBarValue2, seekBarValue3;

    public HistogramSpecificationEqualizedTask(HistogramSpecificationActivity activity, ImageView resultImageView, int seekBarValue1, int seekBarValue2, int seekBarValue3) {
        dialog = new ProgressDialog(activity);
        this.resultImageView = resultImageView;
        this.seekBarValue1 = seekBarValue1;
        this.seekBarValue2 = seekBarValue2;
        this.seekBarValue3 = seekBarValue3;
    }

    @Override
    protected void onPreExecute() {
        dialog.setMessage("Creating new image, please wait...");
        dialog.show();
    }

    /**
     * TBD (src: http://terminalcoders.blogspot.com/2017/02/histogram-equalisation-in-java.html)
     * @param args The image bitmap to be extracted.
     * @return
     */
    @Override
    protected Bitmap doInBackground(Bitmap... args) {
        Bitmap imageBitmap = args[0];

        int minRed, maxRed;
        int minGreen, maxGreen;
        int minBlue, maxBlue;

        minRed = minGreen = minBlue = 255;
        maxRed = maxGreen = maxBlue = 0;

        Integer[] redValuesFrequencies = new Integer[256];
        Integer[] greenValuesFrequencies = new Integer[256];
        Integer[] blueValuesFrequencies = new Integer[256];

        for (int i = 0; i < 256; i++) {
            redValuesFrequencies[i] = 0;
            greenValuesFrequencies[i] = 0;
            blueValuesFrequencies[i] = 0;
        }

        Bitmap processedBitmap = imageBitmap.copy(Bitmap.Config.ARGB_8888, true);
        for (int x = 0; x < processedBitmap.getWidth(); x++) {
            for (int y = 0; y < processedBitmap.getHeight(); y++) {
                int pixelColor = processedBitmap.getPixel(x, y);

                int red = (pixelColor & 0x00FF0000) >> 16;
                int green = (pixelColor & 0x0000FF00) >> 8;
                int blue = (pixelColor & 0x000000FF);

                minRed = Math.min(minRed, red);
                maxRed = Math.max(maxRed, red);

                minGreen = Math.min(minGreen, green);
                maxGreen = Math.max(maxGreen, green);

                minBlue = Math.min(minBlue, blue);
                maxBlue = Math.max(maxBlue, blue);

                redValuesFrequencies[red]++;
                greenValuesFrequencies[green]++;
                blueValuesFrequencies[blue]++;
            }
        }

        Integer[] interpolationRed = new Integer[256];
        Integer[] interpolationGreen = new Integer[256];
        Integer[] interpolationBlue = new Integer[256];

        //Create Control Point using Normalization
        interpolationRed[0] = Math.round(((redValuesFrequencies[0] - minRed) / (maxRed - minRed)) * (seekBarValue1));
        interpolationRed[127] = Math.round(((redValuesFrequencies[127] - minRed) / (maxRed - minRed)) * (seekBarValue2));
        interpolationRed[255] = Math.round(((redValuesFrequencies[255] - minRed) / (maxRed - minRed)) * (seekBarValue3));

        interpolationGreen[0] = Math.round(((greenValuesFrequencies[0] - minGreen) / (maxGreen - minGreen)) * (seekBarValue1));
        interpolationGreen[127] = Math.round(((greenValuesFrequencies[127] - minGreen) / (maxGreen - minGreen)) * (seekBarValue2));
        interpolationGreen[255] = Math.round(((greenValuesFrequencies[255] - minGreen) / (maxGreen - minGreen)) * (seekBarValue3));

        interpolationBlue[0] = Math.round(((blueValuesFrequencies[0] - minBlue) / (maxBlue - minBlue)) * (seekBarValue1));
        interpolationBlue[127] = Math.round(((blueValuesFrequencies[127] - minBlue) / (maxBlue - minBlue)) * (seekBarValue2));
        interpolationBlue[255] = Math.round(((blueValuesFrequencies[255] - minBlue) / (maxBlue - minBlue)) * (seekBarValue3));


        Log.d("BUGGGInter", String.valueOf(redValuesFrequencies[0]));
        Log.d("BUGGGInter", String.valueOf(redValuesFrequencies[127]));
        Log.d("BUGGGInter", String.valueOf(redValuesFrequencies[255]));

        for (int i = 0; i < 256; i++) {
            if (i == 0 || i == 127 || i == 255) {
                // do nothing
            } else if (i > 0 && i < 127) {
                interpolationRed[i] = ((interpolationRed[0] * (127 - i) + interpolationRed[127] * (i - 0)) / (127 - 0));
                interpolationGreen[i] = ((interpolationGreen[0] * (127 - i) + interpolationGreen[127] * (i - 0)) / (127 - 0));
                interpolationBlue[i] = ((interpolationBlue[0] * (127 - i) + interpolationBlue[127] * (i - 0)) / (127 - 0));
            } else { // (> 128) (< 255)
                interpolationRed[i] = ((interpolationRed[127] * (255 - i) + interpolationRed[255] * (i - 127)) / (255 - 127));
                interpolationGreen[i] = ((interpolationGreen[127] * (255 - i) + interpolationGreen[255] * (i - 127)) / (255 - 127));
                interpolationBlue[i] = ((interpolationBlue[127] * (255 - i) + interpolationBlue[255] * (i - 127)) / (255 - 127));
            }
        }

        // Transformation
        Integer[] Tred = new Integer[256];
        Integer[] Tgreen = new Integer[256];
        Integer[] Tblue = new Integer[256];

        int size = processedBitmap.getHeight() * processedBitmap.getWidth();

        //Todo: Check here maybe wrong
        for (int i = 0; i < 256; i++) {
            Tred[i] = (255 * interpolationRed[i]) / size;
            Tgreen[i] = (255 * interpolationGreen[i]) / size;
            Tblue[i] = (255 * interpolationBlue[i]) / size;
            Log.d("BUGGG" + String.valueOf(i) + " " + interpolationRed[i] + " " + interpolationGreen[i] + " " + interpolationBlue[i], "R" + Tred[i] + " G" + Tgreen[i] + " B" + Tblue[i]);
        }

        Bitmap result = Bitmap.createBitmap(imageBitmap.getWidth(), imageBitmap.getHeight(), Bitmap.Config.ARGB_8888);
        for (int x = 0; x < processedBitmap.getWidth(); x++) {
            for (int y = 0; y < processedBitmap.getHeight(); y++) {
                int pixelColor = processedBitmap.getPixel(x, y);

                int red = (pixelColor & 0x00FF0000) >> 16;
                int green = (pixelColor & 0x0000FF00) >> 8;
                int blue = (pixelColor & 0x000000FF);

                int newPixelColor = (0xFF<<24) | (Tred[red]<<16) | (Tgreen[green]<<8) | Tblue[blue];
                result.setPixel(x, y, newPixelColor);
            }
        }

        return result;

    }

    /**
     * Display image histogram.
     * @param result RGB and grayscale values' frequencies.
     */
    protected void onPostExecute(Bitmap result) {
        if (dialog.isShowing()) {
            dialog.dismiss();
        }
        resultImageView.setImageBitmap(result);
    }
}
