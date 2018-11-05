package com.pengcit.jorfelag.pengolahancitra.preprocess;

import android.app.ProgressDialog;
import android.graphics.Bitmap;
import android.os.AsyncTask;

import java.lang.ref.WeakReference;

public abstract class BaseFilterTask extends AsyncTask<Bitmap, Void, Bitmap> {

    public static int MAX_WIDTH = 1080;
    public static int MAX_HEIGHT = 1080;

    private WeakReference<PreprocessOperatorFragment> fragmentRef;
    private ProgressDialog dialog;

    protected int kernelSize;
    protected int offset;

    public BaseFilterTask(PreprocessOperatorFragment fr) {
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
    protected void onPostExecute(Bitmap result) {
        PreprocessOperatorFragment fr = fragmentRef.get();
        if (fr == null
                || fr.getActivity() == null
                || fr.getActivity().isFinishing()) return;

        if (dialog.isShowing()) {
            dialog.dismiss();
        }

        fr.setResultImageView(result);
    }

    protected Bitmap resizeBitmap(Bitmap bitmap) {
        int width = bitmap.getWidth();
        int height = bitmap.getHeight();
        int resizedWidth, resizedHeight;

        if (width >= height) {   // Landscape
            resizedWidth = Math.min(width, MAX_WIDTH);
            resizedHeight = resizedWidth * height / width;
        } else {    // Portrait
            resizedHeight = Math.min(height, MAX_HEIGHT);
            resizedWidth = resizedHeight *  width / height;
        }

        return Bitmap.createScaledBitmap(bitmap, resizedWidth, resizedHeight, true);
    }

    protected int[] getProcessedPixels(Bitmap originalBitmap) {
        final int height = originalBitmap.getHeight();
        final int width = originalBitmap.getWidth();
        final int paddedWidth = width + 2 * offset;
        final int paddedHeight = height + 2 * offset;
        final int startOffset = offset + offset * paddedWidth;

        final int[] processedPixels = new int[paddedHeight * paddedWidth];
        originalBitmap.getPixels(processedPixels, startOffset, paddedWidth,
                0, 0, width, height);

        // Left and right padding
        for (int i = 0; i < paddedHeight; ++i) {
            for (int j = 0; j < offset; ++j) {
                processedPixels[i * paddedWidth + j] = processedPixels[i * paddedWidth + offset];
            }
            for (int j = width + offset; j < paddedWidth; ++j) {
                processedPixels[i * paddedWidth + j] = processedPixels[i * paddedWidth + width + offset - 1];
            }
        }

        // Top and bottom padding
        for (int i = 0; i < offset; ++i) {
            System.arraycopy(processedPixels, offset * paddedWidth,
                    processedPixels, i * paddedWidth, paddedWidth);
        }
        for (int i = height + offset; i < paddedHeight; ++i) {
            System.arraycopy(processedPixels, (height + offset - 1) * paddedWidth,
                    processedPixels, i * paddedWidth, paddedWidth);
        }

        return processedPixels;
    }
}
