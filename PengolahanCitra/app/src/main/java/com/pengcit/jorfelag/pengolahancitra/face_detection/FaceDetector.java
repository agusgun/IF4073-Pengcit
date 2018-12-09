package com.pengcit.jorfelag.pengolahancitra.face_detection;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.Rect;
import android.util.Log;

import com.pengcit.jorfelag.pengolahancitra.util.LoopBody;
import com.pengcit.jorfelag.pengolahancitra.util.Parallel;

import java.util.ArrayList;

public class FaceDetector {

    private Bitmap originalBitmap;
    private Bitmap processedBitmap;
    private int[] bitmap;
    private int width, height;

    public FaceDetector(Bitmap inputBitmap) {
        originalBitmap = inputBitmap.copy(inputBitmap.getConfig(), true);

        height = originalBitmap.getHeight();
        width = originalBitmap.getWidth();

        bitmap = new int[height * width];
        Parallel.For(0, height, new LoopBody<Integer>() {
            @Override
            public void run(Integer y) {
                final int[] resultPixels = new int[width];
                originalBitmap.getPixels(resultPixels, 0, width, 0, y, width, 1);

                for (int x = 0; x < width; ++x) {
                    bitmap[y * width + x] = isSkinColor(resultPixels[x]) ? Color.WHITE : Color.BLACK;
                }
            }
        });

        processedBitmap = Bitmap.createBitmap(bitmap, width, height, originalBitmap.getConfig());
        Bitmap sobelBitmap = new Sobel(originalBitmap).process();

        Canvas canvas = new Canvas(originalBitmap);
        Paint framePaint = new Paint();
        framePaint.setColor(Color.RED);
        framePaint.setStyle(Paint.Style.STROKE);
        Paint pointsPaint = new Paint();
        pointsPaint.setColor(Color.GREEN);
        pointsPaint.setStyle(Paint.Style.STROKE);

        ArrayList<Point[]> faceCandidateBounds =
                new CandidateFaceDetector(bitmap, width, height).process();

        for (Point[] bound: faceCandidateBounds) {
            FaceCandidateProcessor fc = new FaceCandidateProcessor(sobelBitmap, bound);
            fc.process();
            if (fc.isFace()) {
                for (Point[] featureBoundary :fc.getFeaturesBoundary()) {
                    if (featureBoundary != null) {
                        Rect r = new Rect(featureBoundary[0].x, featureBoundary[0].y, featureBoundary[1].x, featureBoundary[1].y);
                        canvas.drawRect(r, framePaint);
                    }
                }

                Point[][] controlPoints = fc.getControlPoints();
                ArrayList<Float> f = new ArrayList<>();
                for (Point[] component: controlPoints) {
                    if (component != null) {
                        for (Point controlPoint: component) {
                            f.add((float) controlPoint.x);
                            f.add((float) controlPoint.y);
                        }
                    }
                }

                float[] f2 = new float[f.size()];
                for (int i = 0; i < f2.length; ++i) {
                    f2[i] = f.get(i);
                }

                canvas.drawPoints(f2, pointsPaint);
            }
        }
    }

    public Bitmap getBitmap() {
        return originalBitmap;
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
