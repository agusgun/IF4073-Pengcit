package com.pengcit.jorfelag.pengolahancitra.util.classifier;

import edu.drexel.cs.jah473.distance.KDPoint;

public class LabeledKDPoint extends KDPoint {
    public String label;

    public LabeledKDPoint(double[] features, String label) {
        super(features);
        this.label = label;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (!super.equals(obj))
            return false;
        if (getClass() != obj.getClass())
            return false;

        LabeledKDPoint other = (LabeledKDPoint) obj;
        return this.label.equals(other.label);
    }
}
