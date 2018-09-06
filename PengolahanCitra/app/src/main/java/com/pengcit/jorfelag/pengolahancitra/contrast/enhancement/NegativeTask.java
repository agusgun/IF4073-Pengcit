package com.pengcit.jorfelag.pengolahancitra.contrast.enhancement;

import android.app.ProgressDialog;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.widget.ImageView;

import com.annimon.stream.IntStream;
import com.annimon.stream.function.IntConsumer;

public class NegativeTask extends AsyncTask<Bitmap, Void, Bitmap> {

    private ProgressDialog dialog;
    ContrastEnhancementActivity activity;
    ImageView resultImageView;

    public NegativeTask(ContrastEnhancementActivity activity, ImageView resultImageView) {
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

        final Bitmap processedBitmap = imageBitmap.copy(Bitmap.Config.ARGB_8888, true);

        final Integer[] T = new Integer[256];
        for (int i = 0; i < 256; i++) {
            T[i] = 255 - i;
        }

        final Bitmap result = Bitmap.createBitmap(imageBitmap.getWidth(), imageBitmap.getHeight(), Bitmap.Config.ARGB_8888);

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

                int newPixelColor = (0xFF<<24) | (T[red]<<16) | (T[green]<<8) | T[blue];
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
