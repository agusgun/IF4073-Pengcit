package com.pengcit.jorfelag.pengolahancitra.contrast.enhancement;

import android.app.ProgressDialog;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.util.MutableInt;
import android.widget.ImageView;

import com.annimon.stream.IntStream;
import com.annimon.stream.function.IntConsumer;

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
        final Bitmap imageBitmap = args[0];
        final Bitmap processedBitmap = imageBitmap.copy(Bitmap.Config.ARGB_8888, true);

        final int width = processedBitmap.getWidth();
        final int height = processedBitmap.getHeight();
        final int size = height * width;

        final MutableInt minRed = new MutableInt(255);
        final MutableInt maxRed = new MutableInt(0);
        final MutableInt minGreen = new MutableInt(255);
        final MutableInt maxGreen = new MutableInt(0);
        final MutableInt minBlue = new MutableInt(255);
        final MutableInt maxBlue = new MutableInt(0);

        IntStream.range(0, size).forEach(new IntConsumer() {
            public void accept(int value) {
                int x = value % width;
                int y = value / width;

                int pixelColor = processedBitmap.getPixel(x, y);

                int red = (pixelColor & 0x00FF0000) >> 16;
                int green = (pixelColor & 0x0000FF00) >> 8;
                int blue = (pixelColor & 0x000000FF);

                minRed.value = Math.min(minRed.value, red);
                maxRed.value = Math.max(maxRed.value, red);

                minGreen.value = Math.min(minGreen.value, green);
                maxGreen.value = Math.max(maxGreen.value, green);

                minBlue.value = Math.min(minBlue.value, blue);
                maxBlue.value = Math.max(maxBlue.value, blue);
            }
        });

        final Integer[] Tred = new Integer[256];
        final Integer[] Tgreen = new Integer[256];
        final Integer[] Tblue = new Integer[256];

        for (int i = minRed.value; i <= maxRed.value; i++) {
            Tred[i] = (i - minRed.value) * (255 / (maxRed.value - minRed.value));
        }

        for (int i = minGreen.value; i <= maxGreen.value; i++) {
            Tgreen[i] = (i - minGreen.value) * (255 / (maxGreen.value - minGreen.value));
        }

        for (int i = minBlue.value; i <= maxBlue.value; i++) {
            Tblue[i] = (i - minBlue.value) * (255 / (maxBlue.value - minBlue.value));
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
