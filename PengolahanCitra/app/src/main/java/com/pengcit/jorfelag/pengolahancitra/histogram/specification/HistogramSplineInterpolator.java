package com.pengcit.jorfelag.pengolahancitra.histogram.specification;

import org.apache.commons.math3.analysis.interpolation.SplineInterpolator;
import org.apache.commons.math3.analysis.polynomials.PolynomialSplineFunction;

public class HistogramSplineInterpolator {
    public static final int MAX_VALUE = 100;
    public static final int MIN_VALUE = 0;

    public static Integer[] interpolate(int[] controlPoints) {
        double[] x = new double[controlPoints.length];
        double[] y = new double[controlPoints.length];
        Integer[] histogram = new Integer[256];

        // Divide histogram to equal parts
        for (int i = 0; i < x.length; ++i) {
            x[i] = Math.max(0, Math.round(256 / (float) (x.length - 1) * i) - 1);
            y[i] = controlPoints[i];
        }

        PolynomialSplineFunction f = new SplineInterpolator().interpolate(x, y);

        // Get frequency value for each pixel value
        for (int i = 0; i < histogram.length; ++i) {
            int value = (int) Math.round(f.value((double) i));

            // Constraint value between [MIN_VALUE, MAX_VALUE]
            histogram[i] = Math.min(MAX_VALUE, Math.max(MIN_VALUE, value));
        }

        return histogram;
    }
}
