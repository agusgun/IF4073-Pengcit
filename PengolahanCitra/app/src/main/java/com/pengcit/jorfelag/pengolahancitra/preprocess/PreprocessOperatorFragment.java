package com.pengcit.jorfelag.pengolahancitra.preprocess;

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
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.pengcit.jorfelag.pengolahancitra.R;
import com.pengcit.jorfelag.pengolahancitra.SharedViewModel;

public class PreprocessOperatorFragment extends Fragment {

    private OnFragmentInteractionListener mListener;

    private final static int MEDIAN_FILTER = 0;
    private final static int DIFFERENCE_OPERATOR = 1;
    private final static int DIFFERENCE_HOMOGEN_OPERATOR = 2;
    private final static int GRADIENT_OPERATOR = 3;

    private ImageView originalImageView;
    private ImageView resultImageView;
    private TextView loadTextView;
    private EditText kernelSizeEditText;
    private LinearLayout kernelLinearLayout;
    private Button processButton;
    private Button commitButton;
    private Spinner spinner;
    private Spinner kernelSpinner;
    private SharedViewModel model;
    private Bitmap originalBitmap;
    private Bitmap resultBitmap;

    public PreprocessOperatorFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_preprocess_operator, container, false);

        originalImageView = view.findViewById(R.id.preprocess_operator_fr_iv_orig);
        resultImageView = view.findViewById(R.id.preprocess_operator_fr_iv_result);
        loadTextView = view.findViewById(R.id.preprocess_operator_fr_tv_load_first);
        kernelSizeEditText = view.findViewById(R.id.preprocess_operator_fr_tv_kernel_size);
        kernelLinearLayout = view.findViewById(R.id.preprocess_operator_layout_kernel);
        processButton = view.findViewById(R.id.preprocess_operator_fr_btn_process);
        commitButton = view.findViewById(R.id.preprocess_operator_fr_btn_commit);
        spinner = view.findViewById(R.id.preprocess_operator_spinner);
        kernelSpinner = view.findViewById(R.id.preprocess_operator_spinner_kernel);

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
                R.array.preprocess_operator_method,
                R.layout.support_simple_spinner_dropdown_item
        );
        methodListAdapter.setDropDownViewResource(R.layout.support_simple_spinner_dropdown_item);
        spinner.setAdapter(methodListAdapter);
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (position == MEDIAN_FILTER) {
                    kernelSizeEditText.setVisibility(View.VISIBLE);
                } else {
                    kernelSizeEditText.setVisibility(View.INVISIBLE);
                }

                if (position == GRADIENT_OPERATOR) {
                    kernelLinearLayout.setVisibility(View.VISIBLE);
                } else {
                    kernelLinearLayout.setVisibility(View.INVISIBLE);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) { }
        });

        ArrayAdapter<CharSequence> kernelListAdapter = ArrayAdapter.createFromResource(
                view.getContext(),
                R.array.gradient_operator_kernel,
                R.layout.support_simple_spinner_dropdown_item
        );
        kernelListAdapter.setDropDownViewResource(R.layout.support_simple_spinner_dropdown_item);
        kernelSpinner.setAdapter(kernelListAdapter);

        // Process button listener
        final PreprocessOperatorFragment fr = this;
        processButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (originalBitmap == null) {
                    Toast.makeText(fr.getContext(),
                            R.string.ask_to_select_or_capture_an_image, Toast.LENGTH_SHORT).show();
                    return;
                }

                switch (spinner.getSelectedItemPosition()) {
                    case MEDIAN_FILTER:
                        int kernelSize = getKernelSize();
                        if (kernelSize == -1) {
                            return;
                        }

                        Toast.makeText(getContext(), "Median Filter", Toast.LENGTH_SHORT).show();
                        new MedianFilterTask(fr, kernelSize).execute(originalBitmap);
                        break;
                    case DIFFERENCE_OPERATOR:
                        Toast.makeText(getContext(), "Difference", Toast.LENGTH_SHORT).show();
                        new DifferenceOperatorTask(fr).execute(originalBitmap);
                        break;
                    case DIFFERENCE_HOMOGEN_OPERATOR:
                        Toast.makeText(getContext(), "Difference Homogen", Toast.LENGTH_SHORT).show();
                        new DifferenceHomogenOperatorTask(fr).execute(originalBitmap);
                        break;
                    case GRADIENT_OPERATOR:
                        Toast.makeText(getContext(), "Gradient", Toast.LENGTH_SHORT).show();
                        new GradientOperatorTask(fr, kernelSpinner.getSelectedItemPosition()).execute(originalBitmap);
                        break;
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

    public int getKernelSize() {
        int kernelSize;
        try {
            kernelSize = Integer.parseInt(kernelSizeEditText.getText().toString());
        } catch (NumberFormatException e) {
            Toast.makeText(getContext(),
                    "Invalid weight input", Toast.LENGTH_SHORT).show();
            return -1;
        }

        if (kernelSize % 2 == 0 || kernelSize <= 1) {
            Toast.makeText(getContext(),
                    "Kernel size must be an odd number larger than 1", Toast.LENGTH_SHORT).show();
            return -1;
        }
        return kernelSize;
    }

    public void setResultImageView(Bitmap bitmap) {
        resultBitmap = bitmap;
        resultImageView.setImageBitmap(bitmap);
        commitButton.setVisibility(View.VISIBLE);
    }

    public interface OnFragmentInteractionListener {
        void onFragmentInteraction(Uri uri);
    }
}
