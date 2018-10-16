package com.pengcit.jorfelag.pengolahancitra.contrast_enhancement;

import android.graphics.Bitmap;
import android.util.MutableInt;

import com.pengcit.jorfelag.pengolahancitra.util.LoopBody;
import com.pengcit.jorfelag.pengolahancitra.util.Parallel;

public class HistogramEqualizationTask extends BaseContrastEnhancementTask {

    private float weight;

    public HistogramEqualizationTask(ContrastEnhancementFragment fr, float weight) {
        super(fr);
        this.weight = weight;
    }

    //TODO: local histogram equalization
    //TODO: refactor

    /**
     * TBD (src: http://terminalcoders.blogspot.com/2017/02/histogram-equalisation-in-java.html)
     *
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
        final int size = width * height;

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
}
