package com.pengcit.jorfelag.pengolahancitra.contrast.enhancement;

import android.app.ProgressDialog;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.widget.ImageView;

import com.annimon.stream.IntStream;
import com.annimon.stream.function.IntConsumer;
import com.pengcit.jorfelag.pengolahancitra.contrast.enhancement.ContrastEnhancementActivity;

public class HistogramEqualizationTask extends AsyncTask<Bitmap, Void, Bitmap> {

    private ProgressDialog dialog;
    private ContrastEnhancementActivity activity;
    private ImageView resultImageView;
    private float weight;

    public HistogramEqualizationTask(ContrastEnhancementActivity activity, ImageView resultImageView, float weight) {
        dialog = new ProgressDialog(activity);
        this.resultImageView = resultImageView;
        this.weight = weight;
    }

    @Override
    protected void onPreExecute() {
        dialog.setMessage("Creating new image, please wait...");
        dialog.show();
    }

    //TODO: local histogram equalization
    //TODO:
    //TODO: refactor
    /**
     * TBD (src: http://terminalcoders.blogspot.com/2017/02/histogram-equalisation-in-java.html)
     * @param args The image bitmap to be extracted.
     * @return
     */
    protected Bitmap doInBackground(Bitmap... args) {
        final Bitmap imageBitmap = args[0];

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

        IntStream.range(0, size).forEach(new IntConsumer() {
            public void accept(int value) {
                int x = value % width;
                int y = value / width;

                int pixelColor = processedBitmap.getPixel(x, y);

                int red = (pixelColor & 0x00FF0000) >> 16;
                int green = (pixelColor & 0x0000FF00) >> 8;
                int blue = (pixelColor & 0x000000FF);

                redValuesFrequencies[red]++;
                greenValuesFrequencies[green]++;
                blueValuesFrequencies[blue]++;
            }
        });

        // Create cumulative frequencies
        for (int i = 1; i < 256; i++) {
            redValuesFrequencies[i] = Math.round(weight * redValuesFrequencies[i] + weight * redValuesFrequencies[i - 1]);
            greenValuesFrequencies[i] = Math.round(weight * greenValuesFrequencies[i] + weight * greenValuesFrequencies[i - 1]);
            blueValuesFrequencies[i] = Math.round(weight * blueValuesFrequencies[i] + weight * blueValuesFrequencies[i - 1]);
        }

        final Integer[] Tred = new Integer[256];
        final Integer[] Tgreen = new Integer[256];
        final Integer[] Tblue = new Integer[256];

        for (int i = 0; i < 256; i++) {
            Tred[i] = (255 * redValuesFrequencies[i]) / size;
            Tgreen[i] = (255 * greenValuesFrequencies[i]) / size;
            Tblue[i] = (255 * blueValuesFrequencies[i]) / size;
        }

        final Bitmap result = Bitmap.createBitmap(imageBitmap.getWidth(), imageBitmap.getHeight(), Bitmap.Config.ARGB_8888);

        IntStream.range(0, size).forEach(new IntConsumer() {
            public void accept(int value) {
                int x = value % width;
                int y = value / width;

                int pixelColor = processedBitmap.getPixel(x, y);

                int red = (pixelColor & 0x00FF0000) >> 16;
                int green = (pixelColor & 0x0000FF00) >> 8;
                int blue = (pixelColor & 0x000000FF);

                int newPixelColor = (0xFF<<24) | (Tred[red]<<16) | (Tgreen[green]<<8) | Tblue[blue];
                result.setPixel(x, y, newPixelColor);
            }
        });

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
