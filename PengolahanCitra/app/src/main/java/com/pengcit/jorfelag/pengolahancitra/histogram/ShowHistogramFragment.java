package com.pengcit.jorfelag.pengolahancitra.histogram;

import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Context;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.series.BarGraphSeries;
import com.jjoe64.graphview.series.DataPoint;
import com.pengcit.jorfelag.pengolahancitra.R;
import com.pengcit.jorfelag.pengolahancitra.SharedViewModel;

import java.util.HashMap;
import java.util.Map;

public class ShowHistogramFragment extends Fragment {

    private Map<String, GraphView> histogram;

    private OnFragmentInteractionListener mListener;

    public ShowHistogramFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_show_histogram, container, false);

        histogram = new HashMap<>();
        histogram.put("red", (GraphView) view.findViewById(R.id.show_histogram_fr_red_histogram));
        histogram.put("green", (GraphView) view.findViewById(R.id.show_histogram_fr_green_histogram));
        histogram.put("blue", (GraphView) view.findViewById(R.id.show_histogram_fr_blue_histogram));
        histogram.put("gray", (GraphView) view.findViewById(R.id.show_histogram_fr_gray_histogram));

        SharedViewModel model = ViewModelProviders.of(getActivity()).get(SharedViewModel.class);
        final ShowHistogramFragment fr = this;
        model.getBitmapLiveData().observe(this, new Observer<Bitmap>() {
            @Override
            public void onChanged(@Nullable Bitmap bitmap) {
                if (bitmap != null) {
                    new GenerateHistogramTask(fr).execute(bitmap);
                }
            }
        });

        return view;
    }

    public void onButtonPressed(Uri uri) {
        if (mListener != null) {
            mListener.onFragmentInteraction(uri);
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnFragmentInteractionListener) {
            mListener = (OnFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    public Map<String, GraphView> getHistogram() {
        return histogram;
    }

    public void setUpHistogram(GraphView g, Integer[] data, String title, int color) {
        BarGraphSeries series = new BarGraphSeries<>(generateData(data));
        series.setColor(color);

        g.removeAllSeries();
        g.addSeries(series);
        g.setTitle(title);
        g.getViewport().setXAxisBoundsManual(true);
        g.getViewport().setMinX(0);
        g.getViewport().setMaxX(255);

        // enable scaling and scrolling
        g.getViewport().setScalable(true);
        g.getViewport().setScalableY(true);
    }

    private DataPoint[] generateData(Integer[] colorValuesFrequencies) {
        DataPoint[] values = new DataPoint[colorValuesFrequencies.length];
        for (int i = 0; i < values.length; i++) {
            DataPoint v = new DataPoint(i, colorValuesFrequencies[i]);
            values[i] = v;
        }
        return values;
    }

    public interface OnFragmentInteractionListener {
        void onFragmentInteraction(Uri uri);
    }
}
