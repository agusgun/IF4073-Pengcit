package com.pengcit.jorfelag.pengolahancitra.contrast.enhancement;

import android.app.ProgressDialog;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.widget.ImageView;

import com.pengcit.jorfelag.pengolahancitra.contrast.enhancement.ContrastEnhancementActivity;

public class HistogramEqualizationTask extends AsyncTask<Bitmap, Void, Bitmap> {

    private ProgressDialog dialog;
    ContrastEnhancementActivity activity;
    ImageView resultImageView;

    public HistogramEqualizationTask(ContrastEnhancementActivity activity, ImageView resultImageView) {
        dialog = new ProgressDialog(activity);
        this.resultImageView = resultImageView;
    }

    @Override
    protected void onPreExecute() {
        dialog.setMessage("Creating new image, please wait...");
        dialog.show();
    }

    //TODO: add weighting
    //TODO: local histogram equalization
    //TODO:
    //TODO: refactor
    /**
     * TBD (src: http://terminalcoders.blogspot.com/2017/02/histogram-equalisation-in-java.html)
     * @param args The image bitmap to be extracted.
     * @return
     */
    protected Bitmap doInBackground(Bitmap... args) {
        Bitmap imageBitmap = args[0];

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

                redValuesFrequencies[red]++;
                greenValuesFrequencies[green]++;
                blueValuesFrequencies[blue]++;
            }
        }

        // Create cumulative frequencies
        for (int i = 1; i < 256; i++) {
            redValuesFrequencies[i] += redValuesFrequencies[i - 1];
            greenValuesFrequencies[i] += greenValuesFrequencies[i - 1];
            blueValuesFrequencies[i] += blueValuesFrequencies[i - 1];
        }

        Integer[] Tred = new Integer[256];
        Integer[] Tgreen = new Integer[256];
        Integer[] Tblue = new Integer[256];

        int size = processedBitmap.getHeight() * processedBitmap.getWidth();

        for (int i = 0; i < 256; i++) {
            Tred[i] = (255 * redValuesFrequencies[i]) / size;
            Tgreen[i] = (255 * greenValuesFrequencies[i]) / size;
            Tblue[i] = (255 * blueValuesFrequencies[i]) / size;
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
