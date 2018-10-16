package com.pengcit.jorfelag.pengolahancitra.histogram;

import android.app.ProgressDialog;
import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.net.Uri;
import android.os.AsyncTask;
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
import com.pengcit.jorfelag.pengolahancitra.util.LoopBody;
import com.pengcit.jorfelag.pengolahancitra.util.Parallel;

import java.lang.ref.WeakReference;
import java.util.HashMap;

public class ShowHistogramFragment extends Fragment {

    private GraphView redHistogram;
    private GraphView greenHistogram;
    private GraphView blueHistogram;
    private GraphView grayHistogram;

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

        redHistogram = view.findViewById(R.id.show_histogram_fr_red_histogram);
        greenHistogram = view.findViewById(R.id.show_histogram_fr_green_histogram);
        blueHistogram = view.findViewById(R.id.show_histogram_fr_blue_histogram);
        grayHistogram = view.findViewById(R.id.show_histogram_fr_gray_histogram);

        SharedViewModel model = ViewModelProviders.of(getActivity()).get(SharedViewModel.class);
        final ShowHistogramFragment fr = this;
        model.getBitmapLiveData().observe(this, new Observer<Bitmap>() {
            @Override
            public void onChanged(@Nullable Bitmap bitmap) {
                if (bitmap != null) {
                    new CreateHistogramTask(fr).execute(bitmap);
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

    public interface OnFragmentInteractionListener {
        void onFragmentInteraction(Uri uri);
    }

    private void setUpHistogram(GraphView g, Integer[] data, String title, int color) {
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

    private static class CreateHistogramTask extends AsyncTask<Bitmap, Void, HashMap<String, Integer[]>> {
        private WeakReference<ShowHistogramFragment> fragmentRef;
        private ProgressDialog dialog;

        CreateHistogramTask(ShowHistogramFragment fr) {
            fragmentRef = new WeakReference<>(fr);
            dialog = new ProgressDialog(fr.getContext());
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            dialog.setMessage("Creating image histogram, please wait...");
            dialog.show();
        }

        @Override
        protected HashMap<String, Integer[]> doInBackground(Bitmap... params) {
            final Bitmap imageBitmap = params[0];

            final Integer[] redValuesFrequencies = new Integer[256];
            final Integer[] greenValuesFrequencies = new Integer[256];
            final Integer[] blueValuesFrequencies = new Integer[256];
            final Integer[] grayValuesFrequencies = new Integer[256];

            for (int i = 0; i < 256; i++) {
                redValuesFrequencies[i] = 0;
                greenValuesFrequencies[i] = 0;
                blueValuesFrequencies[i] = 0;
                grayValuesFrequencies[i] = 0;
            }

            final Bitmap processedBitmap = imageBitmap.copy(Bitmap.Config.ARGB_8888, true);

            final int width = processedBitmap.getWidth();
            final int height = processedBitmap.getHeight();

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
                        int gray = (red + green + blue) / 3;

                        redValuesFrequencies[red]++;
                        greenValuesFrequencies[green]++;
                        blueValuesFrequencies[blue]++;
                        grayValuesFrequencies[gray]++;
                    }
                }
            });

            HashMap<String, Integer[]> results = new HashMap<>();
            results.put("red", redValuesFrequencies);
            results.put("green", greenValuesFrequencies);
            results.put("blue", blueValuesFrequencies);
            results.put("gray", grayValuesFrequencies);
            return results;
        }

        @Override
        protected void onPostExecute(HashMap<String, Integer[]> result) {
            ShowHistogramFragment fr = fragmentRef.get();
            if (fr == null
                    || fr.getActivity() == null
                    || fr.getActivity().isFinishing()) return;

            if (dialog.isShowing()) {
                dialog.dismiss();
            }

            fr.setUpHistogram(fr.redHistogram, result.get("red"),
                    fr.getString(R.string.red_histogram_title), Color.RED);
            fr.setUpHistogram(fr.greenHistogram, result.get("green"),
                    fr.getString(R.string.green_histogram_title), Color.GREEN);
            fr.setUpHistogram(fr.blueHistogram, result.get("blue"),
                    fr.getString(R.string.blue_histogram_title), Color.BLUE);
            fr.setUpHistogram(fr.grayHistogram, result.get("gray"),
                    fr.getString(R.string.grayscale_histogram_title), Color.GRAY);
        }
    }
}
