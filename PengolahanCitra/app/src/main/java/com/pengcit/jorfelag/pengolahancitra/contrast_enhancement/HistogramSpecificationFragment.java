package com.pengcit.jorfelag.pengolahancitra.contrast_enhancement;

import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.series.BarGraphSeries;
import com.jjoe64.graphview.series.DataPoint;
import com.pengcit.jorfelag.pengolahancitra.R;
import com.pengcit.jorfelag.pengolahancitra.SharedViewModel;

public class HistogramSpecificationFragment extends Fragment {
    private final static int NUM_POINTS = 5;
    private SharedViewModel model;
    private SeekBar[] seekBars;
    private TextView[] seekBarLabels;
    private int[] seekBarValues;
    private ImageView originalImageView;
    private ImageView resultImageView;
    private TextView loadTextView;
    private Button processButton;
    private Button commitButton;
    private GraphView referencedHistogram;
    private Bitmap originalBitmap;
    private Bitmap resultBitmap;

    private OnFragmentInteractionListener mListener;

    public HistogramSpecificationFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_histogram_specification, container, false);

        seekBars = new SeekBar[]{
                view.findViewById(R.id.histogram_specification_fr_seekbar_1),
                view.findViewById(R.id.histogram_specification_fr_seekbar_2),
                view.findViewById(R.id.histogram_specification_fr_seekbar_3),
                view.findViewById(R.id.histogram_specification_fr_seekbar_4),
                view.findViewById(R.id.histogram_specification_fr_seekbar_5),
        };

        seekBarLabels = new TextView[]{
                view.findViewById(R.id.histogram_specification_fr_label_1),
                view.findViewById(R.id.histogram_specification_fr_label_2),
                view.findViewById(R.id.histogram_specification_fr_label_3),
                view.findViewById(R.id.histogram_specification_fr_label_4),
                view.findViewById(R.id.histogram_specification_fr_label_5),
        };

        seekBarValues = new int[NUM_POINTS];
        for (int i = 0; i < NUM_POINTS; ++i) {
            final Integer index = i;

            seekBars[i].setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                int progressChangedValue = 0;

                @Override
                public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                    progressChangedValue = progress;
                    seekBarLabels[index].setText(Integer.toString(progress));
                }

                @Override
                public void onStartTrackingTouch(SeekBar seekBar) {

                }

                @Override
                public void onStopTrackingTouch(SeekBar seekBar) {
                    seekBarValues[index] = progressChangedValue;
                }
            });
        }

        originalImageView = view.findViewById(R.id.histogram_specification_fr_iv_orig);
        resultImageView = view.findViewById(R.id.histogram_specification_fr_iv_result);
        loadTextView = view.findViewById(R.id.histogram_specification_fr_tv_load_first);
        processButton = view.findViewById(R.id.histogram_specification_fr_btn_process);
        commitButton = view.findViewById(R.id.histogram_specification_fr_btn_commit);
        referencedHistogram = view.findViewById(R.id.histogram_specification_fr_referenced_histogram);

        model = ViewModelProviders.of(getActivity()).get(SharedViewModel.class);
        model.getBitmapLiveData().observe(this, new Observer<Bitmap>() {
            @Override
            public void onChanged(@Nullable Bitmap bitmap) {
                originalBitmap = bitmap;
                if (bitmap != null) {
                    originalImageView.setImageBitmap(bitmap);
                    loadTextView.setVisibility(View.GONE);
                } else {
                    originalImageView.setImageResource(android.R.color.transparent);
                    loadTextView.setVisibility(View.VISIBLE);
                }
            }
        });

        // Process button listener
        final HistogramSpecificationFragment fr = this;
        processButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (originalBitmap != null) {
                    new HistogramSpecificationTask(fr).execute(originalBitmap);
                } else {
                    Toast.makeText(fr.getContext(),
                            R.string.ask_to_select_or_capture_an_image, Toast.LENGTH_SHORT).show();
                }
            }
        });

        // Commit button listener
        commitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                model.setBitmap(resultBitmap);
                resultImageView.setImageResource(android.R.color.transparent);
                fr.commitButton.setVisibility(View.GONE);
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

    public int[] getSeekBarValues() {
        return seekBarValues;
    }

    public void setResultImageView(Bitmap bitmap) {
        resultBitmap = bitmap;
        resultImageView.setImageBitmap(bitmap);
        commitButton.setVisibility(View.VISIBLE);
    }

    public void setUpHistogram(Integer[] data) {
        BarGraphSeries series = new BarGraphSeries<>(generateData(data));
        series.setColor(Color.BLACK);

        referencedHistogram.removeAllSeries();
        referencedHistogram.addSeries(series);
        referencedHistogram.setTitle("Referenced Histogram");
        referencedHistogram.getViewport().setXAxisBoundsManual(true);
        referencedHistogram.getViewport().setMinX(0);
        referencedHistogram.getViewport().setMaxX(255);

        // enable scaling and scrolling
        referencedHistogram.getViewport().setScalable(true);
        referencedHistogram.getViewport().setScalableY(true);
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
