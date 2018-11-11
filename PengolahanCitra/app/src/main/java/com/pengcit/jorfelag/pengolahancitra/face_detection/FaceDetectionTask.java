package com.pengcit.jorfelag.pengolahancitra.face_detection;

import android.app.ProgressDialog;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.AsyncTask;

import com.pengcit.jorfelag.pengolahancitra.util.LoopBody;
import com.pengcit.jorfelag.pengolahancitra.util.Parallel;

import java.lang.ref.WeakReference;

public class FaceDetectionTask extends AsyncTask<Bitmap, Void, Bitmap> {

    public static int MAX_WIDTH = 1080;
    public static int MAX_HEIGHT = 1080;

    private WeakReference<FaceDetectionFragment> fragmentRef;
    private ProgressDialog dialog;

    protected int offset;

    public FaceDetectionTask(FaceDetectionFragment fr) {
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
    protected Bitmap doInBackground(Bitmap... bitmaps) {
        final Bitmap originalBitmap = resizeBitmap(bitmaps[0]);
        final Bitmap processedBitmap = originalBitmap.copy(Bitmap.Config.ARGB_8888, true);

        final int height = originalBitmap.getHeight();
        final int width = originalBitmap.getWidth();

        Parallel.For(0, height, new LoopBody<Integer>() {
            @Override
            public void run(Integer y) {
                final int[] resultPixels = new int[width];
                originalBitmap.getPixels(resultPixels, 0, width, 0, y, width, 1);

                for (int x = 0; x < width; ++x) {
                    resultPixels[x] = isSkinColor(resultPixels[x]) ? Color.WHITE : Color.BLACK;
                }

                processedBitmap.setPixels(resultPixels, 0, width, 0, y, width, 1);
            }
        });

        return processedBitmap;
    }

    @Override
    protected void onPostExecute(Bitmap result) {
        FaceDetectionFragment fr = fragmentRef.get();
        if (fr == null
                || fr.getActivity() == null
                || fr.getActivity().isFinishing()) return;

        if (dialog.isShowing()) {
            dialog.dismiss();
        }

        fr.setResultImageView(result);
    }

    private Bitmap resizeBitmap(Bitmap bitmap) {
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

    private int[] getRGB(int pixel) {
        int[] RGB = new int[3];

        RGB[0] = (pixel & 0x00FF0000) >> 16;
        RGB[1] = (pixel & 0x0000FF00) >> 8;
        RGB[2] = (pixel & 0x000000FF);

        return RGB;
    }

    private int[] getYCbCr(int pixel) {
        int[] YCbCr = new int[3];

        int[] RGB = getRGB(pixel);
        YCbCr[0] = (int) (0.299 * RGB[0] + 0.587 * RGB[1] + 0.114 * RGB[2]);
        YCbCr[1] = (int) (128 - 0.169 * RGB[0] - 0.331 * RGB[1] + 0.5 * RGB[2]);
        YCbCr[2] = (int) (128 + 0.5 * RGB[0] - 0.419 * RGB[1] - 0.081 * RGB[2]);
        return YCbCr;
    }

    private boolean isSkinColor(int pixel) {
        int[] YCbCr = getYCbCr(pixel);
        return (YCbCr[0] > 80 && YCbCr[1] > 85 && YCbCr[1] < 135 &&
                YCbCr[2] > 135 && YCbCr[2] < 180);
    }
}
