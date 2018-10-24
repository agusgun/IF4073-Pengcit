package com.pengcit.jorfelag.pengolahancitra.ocr;

import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Context;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.pengcit.jorfelag.pengolahancitra.R;
import com.pengcit.jorfelag.pengolahancitra.SharedViewModel;

public class OCRFragment extends Fragment {

    private SharedViewModel model;
    private ImageView originalImageView;
    private ImageView resultImageView;
    private TextView loadTextView;
    private TextView resultTextView;
    private Button processButton;
    private Button commitButton;
    private Bitmap originalBitmap;
    private Bitmap resultBitmap;
    private SeekBar seekBar;
    private EditText distanceEditText;
    private EditText counterEditText;

    private int threshold;
    private int distanceThreshold;
    private int counterThreshold;

    private OnFragmentInteractionListener mListener;

    public OCRFragment() {
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
        View view = inflater.inflate(R.layout.fragment_ocr, container, false);

        originalImageView = view.findViewById(R.id.ocr_fr_iv_orig);
        resultImageView = view.findViewById(R.id.ocr_fr_iv_result);
        loadTextView = view.findViewById(R.id.ocr_fr_tv_load_first);
        resultTextView = view.findViewById(R.id.ocr_fr_tv_result);
        processButton = view.findViewById(R.id.ocr_fr_btn_process);
        commitButton = view.findViewById(R.id.ocr_fr_btn_commit);
        seekBar = view.findViewById(R.id.ocr_fr_seekbar);
        distanceEditText = view.findViewById(R.id.ocr_fr_edittext_distance);
        counterEditText = view.findViewById(R.id.ocr_fr_edittext_counter);

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

        final OCRFragment fr = this;
        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                threshold = i;
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                Toast.makeText(fr.getContext(), Integer.toString(threshold), Toast.LENGTH_SHORT).show();
            }
        });

        // Process button listener
        processButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (originalBitmap != null) {
                    counterThreshold = Integer.parseInt(counterEditText.getText().toString());
                    distanceThreshold = Integer.parseInt(distanceEditText.getText().toString());
                    new OCRTask(fr).execute(originalBitmap);
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

    public int getThreshold() {
        return threshold;
    }

    public int getDistanceThreshold() { return distanceThreshold; }

    public int getCounterThreshold() { return counterThreshold; }

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

    public void setResultImage(Bitmap result) {
        resultBitmap = result;
        resultImageView.setImageBitmap(result);
        commitButton.setVisibility(View.VISIBLE);
    }

    public void setResultText(String result) {
        resultTextView.setText(result);
    }

    public interface OnFragmentInteractionListener {
        void onFragmentInteraction(Uri uri);
    }
}
