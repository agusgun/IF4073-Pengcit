package com.pengcit.jorfelag.pengolahancitra.contrast_enhancement;

import android.graphics.Bitmap;
import android.util.MutableInt;

import com.pengcit.jorfelag.pengolahancitra.util.LoopBody;
import com.pengcit.jorfelag.pengolahancitra.util.Parallel;

public class LogTransformationTask extends BaseContrastEnhancementTask {

    public LogTransformationTask(ContrastEnhancementFragment fr) {
        super(fr);
    }

    //TODO: refactor

    /**
     * TBD (src: http://terminalcoders.blogspot.com/2017/02/histogram-equalisation-in-java.html)
     *
     * @param args The image bitmap to be extracted.
     * @return
     */
    protected Bitmap doInBackground(Bitmap... args) {
        Bitmap imageBitmap = args[0];

        final Bitmap processedBitmap = imageBitmap.copy(Bitmap.Config.ARGB_8888, true);

        final Integer[] T = new Integer[256];
        for (int i = 0; i < 256; i++) {
            T[i] = (int) (Math.log(1 + i) * 255 / Math.log(256));
        }

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

                    processedPixels[j] = (0xFF << 24) | (T[red] << 16) | (T[green] << 8) | T[blue];
                }

                processedBitmap.setPixels(processedPixels, 0, width, 0, i, width, 1);
            }
        });

        return processedBitmap;
    }
}
