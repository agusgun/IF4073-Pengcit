package com.pengcit.jorfelag.pengolahancitra.contrast.enhancement;

import android.app.ProgressDialog;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.widget.ImageView;

public class LinearStretchingTask extends AsyncTask<Bitmap, Void, Bitmap> {

    private ProgressDialog dialog;
    ContrastEnhancementActivity activity;
    ImageView resultImageView;

    public LinearStretchingTask(ContrastEnhancementActivity activity, ImageView resultImageView) {
        dialog = new ProgressDialog(activity);
        this.resultImageView = resultImageView;
    }

    @Override
    protected void onPreExecute() {
        dialog.setMessage("Creating new image, please wait...");
        dialog.show();
    }

    //TODO: refactor
    /**
     * TBD (src: http://terminalcoders.blogspot.com/2017/02/histogram-equalisation-in-java.html)
     * @param args The image bitmap to be extracted.
     * @return
     */
    protected Bitmap doInBackground(Bitmap... args) {
        Bitmap imageBitmap = args[0];

        int minRed, maxRed;
        int minGreen, maxGreen;
        int minBlue, maxBlue;

        minRed = minGreen = minBlue = 255;
        maxRed = maxGreen = maxBlue = 0;

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
            }
        }

        Integer[] Tred = new Integer[256];
        Integer[] Tgreen = new Integer[256];
        Integer[] Tblue = new Integer[256];

        for (int i = minRed; i <= maxRed; i++) {
            Tred[i] = (i - minRed) * (255 / (maxRed - minRed));
        }

        for (int i = minGreen; i <= maxGreen; i++) {
            Tgreen[i] = (i - minGreen) * (255 / (maxGreen - minGreen));
        }

        for (int i = minBlue; i <= maxBlue; i++) {
            Tblue[i] = (i - minBlue) * (255 / (maxBlue - minBlue));
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
