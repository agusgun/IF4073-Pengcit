package com.pengcit.jorfelag.pengolahancitra.face_detection;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.Rect;
import android.support.constraint.solver.widgets.Rectangle;
import android.util.Log;

import com.pengcit.jorfelag.pengolahancitra.util.LoopBody;
import com.pengcit.jorfelag.pengolahancitra.util.Parallel;

import java.util.ArrayList;
import java.util.List;

import Catalano.Imaging.FastBitmap;
import Catalano.Imaging.Filters.FourierTransform;
import Catalano.Imaging.Filters.FrequencyFilter;
import Catalano.Imaging.Filters.Threshold;


public class FaceDetector {

    private Bitmap originalBitmap;
    private Bitmap processedBitmap;
    private int[] bitmap;
    private int width, height;
    private List<String> labels;
    private List<Double> deltas;


    public FaceDetector(Bitmap inputBitmap) {
        labels = new ArrayList<>();
        deltas = new ArrayList<>();

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

        Log.d("Candidates", "num: " + faceCandidateBounds.size());

        int index = 0;
        for (Point[] bound: faceCandidateBounds) {
            FaceCandidateProcessor fc = new FaceCandidateProcessor(sobelBitmap, bound);
            fc.process();

            //DEBUG
//            originalBitmap = fc.getBitmap();

            if (fc.isFace()) {
                index += 1;
                Rect rFace = new Rect(bound[0].x, bound[0].y, bound[1].x, bound[1].y);
                canvas.drawRect(rFace, framePaint);

                setTextSizeForWidth(pointsPaint, 20, Integer.toString(index));

                canvas.drawText(Integer.toString(index), bound[0].x + 5, bound[0].y + 5, pointsPaint);
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
                labels.add(recognize(controlPoints));
            }
        }
    }

    private static void setTextSizeForWidth(Paint paint, float desiredWidth,
                                            String text) {

        // Pick a reasonably large value for the test. Larger values produce
        // more accurate results, but may cause problems with hardware
        // acceleration. But there are workarounds for that, too; refer to
        // http://stackoverflow.com/questions/6253528/font-size-too-large-to-fit-in-cache
        final float testTextSize = 48f;

        // Get the bounds of the text, using our testTextSize.
        paint.setTextSize(testTextSize);
        Rect bounds = new Rect();
        paint.getTextBounds(text, 0, text.length(), bounds);

        // Calculate the desired size as a proportion of our testTextSize.
        float desiredTextSize = testTextSize * desiredWidth / bounds.width();

        // Set the paint for that size.
        paint.setTextSize(desiredTextSize);
    }

    public Bitmap getBitmap() {
        return originalBitmap;
    }

    public List<String> getLabels() {
        return labels;
    }

    public List<Double> getDeltas() {
        return deltas;
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

    private double gradient(Point p1, Point p2) {
        if (p1.x == p2.x) {
            return Double.MAX_VALUE;
        }
        return (double)(p2.y - p1.y)/(double)(p2.x - p1.x);
    }

    private double compare(Point[][] p1, Point[][] p2) {
        double delta = 0;

        for (int i = 0; i < p1.length; i++) {
            if (p1[i] != null && p2[i] != null) {
                for (int j = 0; j < p1[i].length - 1; j++) {
                    delta += Math.abs(gradient(p1[i][j], p1[i][j + 1]) - gradient(p2[i][j], p2[i][j + 1]));
                }
                delta += Math.abs(gradient(p1[i][p1[i].length - 1], p1[i][0]) - gradient(p2[i][p2[i].length - 1], p2[i][0]));
            }
        }

        return delta;
    }

    private String recognize(Point[][] controlPoints) {
        int index = 0;
        double delta = Double.MAX_VALUE;
        double newDelta;
        for (int i = 0; i < FaceDataset.LABELS.length; i++) {
            newDelta = compare(controlPoints, FaceDataset.CONTROL_POINTS[i]);
            if (newDelta < delta) {
                index = i;
                delta = newDelta;
            }
        }
        deltas.add(delta);
        return FaceDataset.LABELS[index];
    }
}
