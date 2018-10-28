package com.pengcit.jorfelag.pengolahancitra.preprocess;

import android.app.ProgressDialog;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.util.Log;
import android.util.Pair;

import com.pengcit.jorfelag.pengolahancitra.ocr.ImageSkeletonizer;
import com.pengcit.jorfelag.pengolahancitra.ocr.OCRFragment;
import com.pengcit.jorfelag.pengolahancitra.util.LoopBody;
import com.pengcit.jorfelag.pengolahancitra.util.Parallel;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Collections;

public class DifferenceOperatorTask extends AsyncTask<Bitmap, Void, Bitmap> {

    private WeakReference<PreprocessOperatorFragment> fragmentRef;
    private ProgressDialog dialog;

    // | -2 | -3 | -4 |
    // | -1 |    |  1 |
    // |  4 |  3 |  2 |

    private final static int[] X_TRANSLATION = {1, 1, 0, -1};
    private final static int[] Y_TRANSLATION = {0, 1, 1, 1};
    private final static int[] X_REVERSE = {-1, -1,  0, 1};
    private final static int[] Y_REVERSE = {0, -1, -1, -1};

    public DifferenceOperatorTask(PreprocessOperatorFragment fr) {
        fragmentRef = new WeakReference<>(fr);
        dialog = new ProgressDialog(fr.getContext());
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        dialog.setMessage("Processing image, please wait...");
        dialog.show();
    }

    @Override
    protected Bitmap doInBackground(Bitmap... args) {
        final Bitmap imageBitmap = args[0];

        Bitmap processedBitmap = imageBitmap.copy(Bitmap.Config.ARGB_8888, true);
        int width = processedBitmap.getWidth();
        int height = processedBitmap.getHeight();

        int[][] originMatrix = new int[height][width];

        // Copy Original Image Gray
        for (int i = 0; i < height; i++) {
            for (int j = 0; j < width; j++) {
                int pixelColor = processedBitmap.getPixel(j, i);
                int red = (pixelColor & 0x00FF0000) >> 16;
                int green = (pixelColor & 0x0000FF00) >> 8;
                int blue = (pixelColor & 0x000000FF);

                int gray = (int) (red * 0.3 + green * 0.59 + blue * 0.11);
                originMatrix[i][j] = gray;
            }
        }

        // Transform Original Matrix
        int[][] transformedMatrix = new int[height][width];
        for (int i = 0; i < height; i++) {
            for (int j = 0; j < width; j++) {
                ArrayList<Integer> differenceList = new ArrayList<>();
                for (int k = 0; k < 4; k++) {
                    differenceList.add(Math.abs(getValue(i + Y_TRANSLATION[k], j + X_TRANSLATION[k], height, width, originMatrix    ) -
                            getValue(i + Y_REVERSE[k], j + X_REVERSE[k], height, width, originMatrix)));
                }
                int maximum = Collections.max(differenceList);
                transformedMatrix[i][j] = maximum;
                int newColor = (0xFF << 24) | (maximum << 16) | (maximum << 8) | maximum;
                processedBitmap.setPixel(j, i, newColor);
            }
        }


        return processedBitmap;
    }

    private int getValue(int i, int j, int height, int width, int[][] matrix) {
        if (i == height || j == width || i == -1 || j == -1) {
            // Out of bound value change here if needed
            return 0;
        } else {
            return matrix[i][j];
        }
    }

    @Override
    protected void onPostExecute(Bitmap result) {
        PreprocessOperatorFragment fr = fragmentRef.get();
        if (fr == null
                || fr.getActivity() == null
                || fr.getActivity().isFinishing()) return;

        if (dialog.isShowing()) {
            dialog.dismiss();
        }

        Bitmap bitmap = result;

        fr.setResultImageView(bitmap);
    }
}
