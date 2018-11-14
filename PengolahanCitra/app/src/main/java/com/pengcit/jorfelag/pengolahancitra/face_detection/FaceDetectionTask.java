package com.pengcit.jorfelag.pengolahancitra.face_detection;

import android.app.ProgressDialog;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.os.AsyncTask;
import android.util.Log;

import com.pengcit.jorfelag.pengolahancitra.util.LoopBody;
import com.pengcit.jorfelag.pengolahancitra.util.Parallel;

import java.lang.ref.WeakReference;

public class FaceDetectionTask extends AsyncTask<Bitmap, Void, Bitmap> {

    public static int MAX_WIDTH = 300;
    public static int MAX_HEIGHT = 400;

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
        final Bitmap originalBitmap = resizeBitmap(bitmaps[0], MAX_WIDTH, MAX_HEIGHT);
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

        int[] boundary = findFace(processedBitmap);
        Log.d("Boundary", Integer.toString(boundary[0]) + " " + Integer.toString(boundary[1]) + " " + Integer.toString(boundary[2]) + " " + Integer.toString(boundary[3]));

        Bitmap result = Bitmap.createBitmap(processedBitmap, boundary[0], boundary[1], boundary[2] - boundary[0], boundary[3] - boundary[1]);
        Bitmap resizedFace = Bitmap.createScaledBitmap(result, 75, 100, true);
        Log.d("Resized Face", Integer.toString(resizedFace.getWidth()) + " " + Integer.toString(resizedFace.getHeight()));

        int[] mouthPosition = findMouth(resizedFace);
        Log.d("Mouth", Integer.toString(mouthPosition[0]) + " " + Integer.toString(mouthPosition[1]) + " " + Integer.toString(mouthPosition[2]) + " " + Integer.toString(mouthPosition[3]));

        int[] nosePosition = findNose(resizedFace, mouthPosition[1], mouthPosition[0], mouthPosition[2]);
        Log.d("Nose", Integer.toString(nosePosition[0]) + " " + Integer.toString(nosePosition[1]) + " " + Integer.toString(nosePosition[2]) + " " + Integer.toString(nosePosition[3]));

        int[] leftEyePosition = findLeftEye(resizedFace, nosePosition[1], nosePosition[0]);
        Log.d("Left Eye", Integer.toString(leftEyePosition[0]) + " " + Integer.toString(leftEyePosition[1]) + " " + Integer.toString(leftEyePosition[2]) + " " + Integer.toString(leftEyePosition[3]));

        int[] rightEyePosition = findRightEye(resizedFace, leftEyePosition[1], leftEyePosition[3], leftEyePosition[2]);
        Log.d("Right Eye", Integer.toString(rightEyePosition[0]) + " " + Integer.toString(rightEyePosition[1]) + " " + Integer.toString(rightEyePosition[2]) + " " + Integer.toString(rightEyePosition[3]));

        int[] leftEyelashPosition = findLeftEyelash(resizedFace, leftEyePosition[0], leftEyePosition[1], leftEyePosition[2]);
        Log.d("Left Eyelash", Integer.toString(leftEyelashPosition[0]) + " " + Integer.toString(leftEyelashPosition[1]) + " " + Integer.toString(leftEyelashPosition[2]) + " " + Integer.toString(leftEyelashPosition[3]));

        int[] rightEyelashPosition = findRightEyelash(resizedFace, rightEyePosition[0], rightEyePosition[1], rightEyePosition[2]);
        Log.d("Right Eyelash", Integer.toString(rightEyelashPosition[0]) + " " + Integer.toString(rightEyelashPosition[1]) + " " + Integer.toString(rightEyelashPosition[2]) + " " + Integer.toString(rightEyelashPosition[3]));

        setBoundingBox(resizedFace, Color.RED, mouthPosition[0], mouthPosition[1], mouthPosition[2], mouthPosition[3]);
        setBoundingBox(resizedFace, Color.GREEN, nosePosition[0], nosePosition[1], nosePosition[2], nosePosition[3]);
        setBoundingBox(resizedFace, Color.BLUE, leftEyePosition[0], leftEyePosition[1], leftEyePosition[2], leftEyePosition[3]);
        setBoundingBox(resizedFace, Color.BLUE, rightEyePosition[0], rightEyePosition[1], rightEyePosition[2], rightEyePosition[3]);
        setBoundingBox(resizedFace, Color.YELLOW, leftEyelashPosition[0], leftEyelashPosition[1], leftEyelashPosition[2], leftEyelashPosition[3]);
        setBoundingBox(resizedFace, Color.YELLOW, rightEyelashPosition[0], rightEyelashPosition[1], rightEyelashPosition[2], rightEyelashPosition[3]);

        return resizedFace;
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

    //set bounding box
    private void setBoundingBox(Bitmap resizedBitmap, int color, int left, int top, int right, int bottom) {
        for (int i = top; i <= bottom; i++) {
            resizedBitmap.setPixel(left, i, color);
            resizedBitmap.setPixel(right, i, color);
        }
        for (int j = left; j <= right; j++) {
            resizedBitmap.setPixel(j, top, color);
            resizedBitmap.setPixel(j, bottom, color);
        }
    }

    //find rightEyelash
    private int[] findRightEyelash(Bitmap faceBitmap, int rightEyeLeft, int rightEyeTop, int rightEyeRight) {
        int rightEyelashWidth = 20;
        int rightEyelashHeight = 5;

        int maks = 0;
        int rightEyelashPosition[] = new int[4];

        int bitmapWidth = faceBitmap.getWidth();
        int bitmapHeight = faceBitmap.getHeight();

        for (int i = rightEyeTop - 10; i < rightEyeTop - rightEyelashHeight; i++) {
            for (int j = rightEyeLeft - 5; j < rightEyeRight - rightEyelashWidth + 5; j++) {
                if (j + rightEyelashWidth >= bitmapWidth) break;

                int white = sumRegion(faceBitmap, j, i, j + rightEyelashWidth, i + rightEyelashHeight / 2);
                int black = sumRegion(faceBitmap, j, i + rightEyelashHeight / 2, j + rightEyelashWidth, i + rightEyelashHeight);
                if (black - white >= maks) {
                    maks = black - white;
                    rightEyelashPosition[0] = j;
                    rightEyelashPosition[1] = i + rightEyelashHeight / 2;
                    rightEyelashPosition[2] = j + rightEyelashWidth;
                    rightEyelashPosition[3] = i + rightEyelashHeight;
                }
            }
            if (i + rightEyelashHeight >= bitmapHeight) break;
        }
        return rightEyelashPosition;

    }

    //find leftEyeLash
    private int[] findLeftEyelash(Bitmap faceBitmap, int leftEyeLeft, int leftEyeTop, int leftEyeRight) {
        int leftEyelashWidth = 20;
        int leftEyelashHeight = 5;

        int maks = 0;
        int leftEyelashPosition[] = new int[4];

        int bitmapWidth = faceBitmap.getWidth();
        int bitmapHeight = faceBitmap.getHeight();

        for (int i = leftEyeTop - 10; i < leftEyeTop - leftEyelashHeight; i++) {
            for (int j = leftEyeLeft - 5; j < leftEyeRight - leftEyelashWidth + 5; j++) {
                if (j + leftEyelashWidth >= bitmapWidth) break;

                int white = sumRegion(faceBitmap, j, i, j + leftEyelashWidth, i + leftEyelashHeight / 2);
                int black = sumRegion(faceBitmap, j, i + leftEyelashHeight / 2, j + leftEyelashWidth, i + leftEyelashHeight);
                if (black - white >= maks) {
                    maks = black - white;
                    leftEyelashPosition[0] = j;
                    leftEyelashPosition[1] = i + leftEyelashHeight / 2;
                    leftEyelashPosition[2] = j + leftEyelashWidth;
                    leftEyelashPosition[3] = i + leftEyelashHeight;
                }
            }
            if (i + leftEyelashHeight >= bitmapHeight) break;
        }
        return leftEyelashPosition;
    }

    //find right eye
    private int[] findRightEye(Bitmap faceBitmap, int leftEyeTop, int leftEyeBottom, int leftEyeRight) {
        int rightEyeWidth = 20;
        int rightEyeHeight = 12;

        int maks = 0;
        int rightEyePosition[] = new int[4];

        int bitmapWidth = faceBitmap.getWidth();
        int bitmapHeight = faceBitmap.getHeight();

        for (int i = leftEyeTop; i < leftEyeBottom; i++) {
            for (int j = leftEyeRight; j < bitmapWidth; j++) {
                if (j + rightEyeWidth >= bitmapWidth) break;

                int black = sumRegion(faceBitmap, j, i, j + rightEyeWidth, i + rightEyeHeight / 2);
                int white = sumRegion(faceBitmap, j, i + rightEyeHeight / 2, j + rightEyeWidth, i + rightEyeHeight);
                if (black - white >= maks) {
                    maks = black - white;
                    rightEyePosition[0] = j;
                    rightEyePosition[1] = i;
                    rightEyePosition[2] = j + rightEyeWidth;
                    rightEyePosition[3] = i + rightEyeHeight / 2;
                }
            }
            if (i + rightEyeHeight >= bitmapHeight) break;
        }
        return rightEyePosition;

    }

    //find left eye
    private int[] findLeftEye(Bitmap faceBitmap, int noseTopPosition, int noseLeftPosition) {
        int leftEyeWidth = 20;
        int leftEyeHeight = 12;

        int maks = 0;
        int leftEyePosition[] = new int[4];

        int bitmapWidth = faceBitmap.getWidth();
        int bitmapHeight = faceBitmap.getHeight();

        for (int i = noseTopPosition - 10; i < noseTopPosition; i++) {
            for (int j = 0; j < noseLeftPosition + 5; j++) {
                if (j + leftEyeWidth >= bitmapWidth) break;

                int black = sumRegion(faceBitmap, j, i, j + leftEyeWidth, i + leftEyeHeight / 2);
                int white = sumRegion(faceBitmap, j, i + leftEyeHeight / 2, j + leftEyeWidth, i + leftEyeHeight);
                if (black - white >= maks) {
                    maks = black - white;
                    leftEyePosition[0] = j;
                    leftEyePosition[1] = i;
                    leftEyePosition[2] = j + leftEyeWidth;
                    leftEyePosition[3] = i + leftEyeHeight / 2;
                }
            }
            if (i + leftEyeHeight >= bitmapHeight) break;
        }
        return leftEyePosition;
    }

    //find nose
    private int[] findNose(Bitmap faceBitmap, int mouthTopPosition, int mouthLeftPosition, int mouthRightPosition) {
        int noseWidth = 18;
        int noseHeight = 20;

        int maks = 0;
        int nosePosition[] = new int[4];

        int bitmapWidth = faceBitmap.getWidth();
        int bitmapHeight = faceBitmap.getHeight();

        for (int i = mouthTopPosition - 22; i < mouthTopPosition - noseHeight; i++) {
            for (int j = mouthLeftPosition; j < mouthRightPosition - noseWidth; j++) {
                if (j + noseWidth >= bitmapWidth) break;

                int black1 = sumRegion(faceBitmap, j, i, j + noseWidth / 3, i + noseHeight);
                int white = sumRegion(faceBitmap, j + noseWidth / 3, i, j + (noseWidth * 2) / 3, i + noseHeight);
                int black2 = sumRegion(faceBitmap, j + (noseWidth * 2) / 3, i, j + noseWidth, i + noseHeight);
                if ((black1 + black2) - white >= maks) {
                    maks = black1 + black2 - white;
                    nosePosition[0] = j;
                    nosePosition[1] = i;
                    nosePosition[2] = j + noseWidth;
                    nosePosition[3] = i + noseHeight;
                }
            }
            if (i + noseHeight >= bitmapHeight) break;
        }
        return nosePosition;
    }

    //find mouth
    private int[] findMouth(Bitmap faceBitmap) {
        int mouthWidth = 30;
        int mouthHeight = 12;

        int maks = 0;
        int mouthPosition[] = new int[4];

        int bitmapWidth = faceBitmap.getWidth();
        int bitmapHeight = faceBitmap.getHeight();

        for (int i = 0; i < bitmapHeight; i++) {
            for (int j = 0; j < bitmapWidth; j++) {
                if (j + mouthWidth >= bitmapWidth) break;
                int white = sumRegion(faceBitmap, j, i, j + mouthWidth, i + mouthHeight / 2);
                int black = sumRegion(faceBitmap, j, i + mouthHeight / 2, j + mouthWidth, i + mouthHeight);
                if (black - white >= maks) {
                    maks = black - white;
                    mouthPosition[0] = j;
                    mouthPosition[1] = i + mouthHeight / 2;
                    mouthPosition[2] = j + mouthWidth;
                    mouthPosition[3] = i + mouthHeight;
                }
            }
            if (i + mouthHeight >= bitmapHeight) break;
        }
        return mouthPosition;
    }

    //sum region
    private int sumRegion(Bitmap regionBitmap, int left, int top, int right, int bottom) {
        int sumResult = 0;
        for (int i = top; i < bottom; i++) {
            for (int j = left; j < right; j++) {
                if (regionBitmap.getPixel(j, i) == Color.BLACK) {
                    sumResult++;
                }
            }
        }
        return sumResult;
    }

    // return left,top,right,bottom boundary of a face
    private int[] findFace(Bitmap fullBodyBitmap) {
        int minRow = 9999;
        int maxRow = -9999;
        int minCol = 9999;
        int maxCol= -9999;
        boolean foundWhite = false;

        int height = fullBodyBitmap.getHeight();
        int width = fullBodyBitmap.getWidth();

        for (int i = 0; i < height; i++) {
            boolean allBlack = true;
            for (int j = 0; j < width; j++) {
                if (fullBodyBitmap.getPixel(j, i) == Color.WHITE) {
                    if (j > maxCol) {
                        maxCol = j;
                    }
                    if (j < minCol) {
                        minCol = j;
                    }
                    if (i > maxRow) {
                        maxRow = i;
                    }
                    if (i < minRow) {
                        minRow = i;
                    }
                    allBlack = false;
                    foundWhite = true;
                }
            }
            if (allBlack && foundWhite) {
                break;
            }
        }
        int result[] = {minCol, minRow, maxCol, maxRow};
        return result;
    }

    private Bitmap resizeBitmap(Bitmap bitmap, int maxWidth, int maxHeight) {
        int width = bitmap.getWidth();
        int height = bitmap.getHeight();
        int resizedWidth, resizedHeight;

        if (width >= height) {   // Landscape
            resizedWidth = Math.min(width, maxWidth);
            resizedHeight = resizedWidth * height / width;
        } else {    // Portrait
            resizedHeight = Math.min(height, maxHeight);
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
