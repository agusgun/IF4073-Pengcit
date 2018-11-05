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
import android.widget.TableLayout;
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
    private final static int BLUR = 4;
    private final static int CUSTOM_KERNEL = 5;

    private ImageView originalImageView;
    private ImageView resultImageView;
    private TextView loadTextView;
    private EditText kernelSizeEditText;

    private LinearLayout edgeKernelLayout;
    private Spinner edgeKernelSpinner;

    private LinearLayout blurKernelLayout;
    private Spinner blurKernelSpinner;

    private TableLayout customKernelLayout;
    private EditText[][] customKernelEditText;

    private Button processButton;
    private Button commitButton;
    private Spinner filterSpinner;

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

        edgeKernelLayout = view.findViewById(R.id.preprocess_operator_layout_edge_kernel);
        edgeKernelSpinner = view.findViewById(R.id.preprocess_operator_spinner_edge_kernel);

        blurKernelLayout = view.findViewById(R.id.preprocess_operator_layout_blur_kernel);
        blurKernelSpinner = view.findViewById(R.id.preprocess_operator_spinner_blur_kernel);

        processButton = view.findViewById(R.id.preprocess_operator_fr_btn_process);
        commitButton = view.findViewById(R.id.preprocess_operator_fr_btn_commit);
        filterSpinner = view.findViewById(R.id.preprocess_operator_spinner);

        customKernelLayout = view.findViewById(R.id.preprocess_operator_layout_custom_kernel_table);
        customKernelEditText = new EditText[][] {
                {
                        view.findViewById(R.id.preprocess_operator_kernel_m00),
                        view.findViewById(R.id.preprocess_operator_kernel_m01),
                        view.findViewById(R.id.preprocess_operator_kernel_m02),
                },
                {
                        view.findViewById(R.id.preprocess_operator_kernel_m10),
                        view.findViewById(R.id.preprocess_operator_kernel_m11),
                        view.findViewById(R.id.preprocess_operator_kernel_m12),
                },
                {
                        view.findViewById(R.id.preprocess_operator_kernel_m20),
                        view.findViewById(R.id.preprocess_operator_kernel_m21),
                        view.findViewById(R.id.preprocess_operator_kernel_m22),
                },
        };

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
        filterSpinner.setAdapter(methodListAdapter);
        filterSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (position == MEDIAN_FILTER || position == BLUR) {
                    kernelSizeEditText.setVisibility(View.VISIBLE);
                } else {
                    kernelSizeEditText.setVisibility(View.GONE);
                }

                if (position == GRADIENT_OPERATOR) {
                    edgeKernelLayout.setVisibility(View.VISIBLE);
                } else {
                    edgeKernelLayout.setVisibility(View.GONE);
                }

                if (position == BLUR) {
                    blurKernelLayout.setVisibility(View.VISIBLE);
                } else {
                    blurKernelLayout.setVisibility(View.GONE);
                }

                if (position == CUSTOM_KERNEL) {
                    customKernelLayout.setVisibility(View.VISIBLE);
                } else {
                    customKernelLayout.setVisibility(View.GONE);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) { }
        });

        ArrayAdapter<CharSequence> edgeKernelListAdapter = ArrayAdapter.createFromResource(
                view.getContext(),
                R.array.gradient_operator_kernel,
                R.layout.support_simple_spinner_dropdown_item
        );
        edgeKernelListAdapter.setDropDownViewResource(R.layout.support_simple_spinner_dropdown_item);
        edgeKernelSpinner.setAdapter(edgeKernelListAdapter);

        ArrayAdapter<CharSequence> blurKernelListAdapter = ArrayAdapter.createFromResource(
                view.getContext(),
                R.array.blur_kernel,
                R.layout.support_simple_spinner_dropdown_item
        );
        blurKernelListAdapter.setDropDownViewResource(R.layout.support_simple_spinner_dropdown_item);
        blurKernelSpinner.setAdapter(blurKernelListAdapter);

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

                switch (filterSpinner.getSelectedItemPosition()) {
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
                        new GradientOperatorTask(fr, edgeKernelSpinner.getSelectedItemPosition()).execute(originalBitmap);
                        break;
                    case BLUR:
                        kernelSize = getKernelSize();
                        if (kernelSize == -1) {
                            return;
                        }

                        Toast.makeText(getContext(), "Blur", Toast.LENGTH_SHORT).show();
                        new BlurTask(fr, blurKernelSpinner.getSelectedItemPosition(), kernelSize).execute(originalBitmap);
                        break;
                    case CUSTOM_KERNEL:
                        try {
                            double[][] kernel = getCustomKernel();
                            Toast.makeText(getContext(), "Custom kernel", Toast.LENGTH_SHORT).show();
                            new SingleKernelTask(fr, kernel).execute(originalBitmap);
                            break;
                        } catch (NumberFormatException ignored) {}
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

    public double[][] getCustomKernel() {
        double[][] customKernel = new double[3][];
        try {
            for (int i = 0; i < 3; ++i) {
                customKernel[i] = new double[3];
                for (int j = 0; j < 3; ++j) {
                    customKernel[i][j] =
                            Double.parseDouble(customKernelEditText[i][j].getText().toString());
                }
            }
        } catch (NumberFormatException e) {
            Toast.makeText(getContext(),
                    "Invalid kernel input", Toast.LENGTH_SHORT).show();
            throw(e);
        }
        return customKernel;
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
