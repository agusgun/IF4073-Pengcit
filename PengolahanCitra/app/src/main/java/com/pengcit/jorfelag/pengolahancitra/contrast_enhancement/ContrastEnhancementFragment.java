package com.pengcit.jorfelag.pengolahancitra.contrast_enhancement;

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
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.pengcit.jorfelag.pengolahancitra.R;
import com.pengcit.jorfelag.pengolahancitra.SharedViewModel;

import org.apache.commons.math3.geometry.euclidean.twod.Line;

public class ContrastEnhancementFragment extends Fragment {

    private final static int LINEAR_STRETCHING = 0;
    private final static int HISTOGRAM_EQUALIZATION = 1;
    private final static int LOG_TRANSFORM = 2;
    private final static int POWER_LAW = 3;
    private final static int NEGATIVE = 4;

    private ImageView originalImageView;
    private ImageView resultImageView;
    private TextView loadTextView;
    private EditText weightEditText;
    private Button processButton;
    private Button commitButton;
    private Spinner spinner;

    SharedViewModel model;
    private Bitmap originalBitmap;
    private Bitmap resultBitmap;

    private OnFragmentInteractionListener mListener;

    public ContrastEnhancementFragment() {
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
        View view = inflater.inflate(R.layout.fragment_contrast_enhancement, container, false);

        originalImageView = view.findViewById(R.id.contrast_enhancement_fr_iv_orig);
        resultImageView = view.findViewById(R.id.contrast_enhancement_fr_iv_result);
        loadTextView = view.findViewById(R.id.contrast_enhancement_fr_tv_load_first);
        weightEditText = view.findViewById(R.id.contrast_enhancement_fr_tv_weight);
        processButton = view.findViewById(R.id.contrast_enhancement_fr_btn_process);
        commitButton = view.findViewById(R.id.contrast_enhancement_fr_btn_commit);
        spinner = view.findViewById(R.id.contrast_enhancement_spinner);

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

        // Spinner
        ArrayAdapter<CharSequence> methodListAdapter = ArrayAdapter.createFromResource(
                view.getContext(),
                R.array.contrast_enhancement_method,
                R.layout.support_simple_spinner_dropdown_item
        );
        methodListAdapter.setDropDownViewResource(R.layout.support_simple_spinner_dropdown_item);
        spinner.setAdapter(methodListAdapter);
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {

            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (position == HISTOGRAM_EQUALIZATION) {
                    weightEditText.setVisibility(View.VISIBLE);
                } else {
                    weightEditText.setVisibility(View.GONE);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        // Process button listener
        final ContrastEnhancementFragment fr = this;
        processButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (originalBitmap != null) {
                    switch (spinner.getSelectedItemPosition()) {
                        case LINEAR_STRETCHING:
                            new LinearStretchingTask(fr).execute(originalBitmap);
                            break;
                        case HISTOGRAM_EQUALIZATION:
                            float weight = Float.parseFloat(weightEditText.getText().toString());
                            new HistogramEqualizationTask(fr, weight).execute(originalBitmap);
                            break;
                        case LOG_TRANSFORM:
                            new LogTransformationTask(fr).execute(originalBitmap);
                            break;
                        case POWER_LAW:
                            new PowerLawTask(fr).execute(originalBitmap);
                            break;
                        case NEGATIVE:
                            new NegativeTask(fr).execute(originalBitmap);
                            break;
                    }
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

    public interface OnFragmentInteractionListener {
        void onFragmentInteraction(Uri uri);
    }

    public void setResultImageView(Bitmap bitmap) {
        resultBitmap = bitmap;
        resultImageView.setImageBitmap(bitmap);
        commitButton.setVisibility(View.VISIBLE);
    }
}
