package com.pengcit.jorfelag.pengolahancitra;

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
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.github.chrisbanes.photoview.PhotoViewAttacher;

import Catalano.Imaging.FastBitmap;
import Catalano.Imaging.Filters.FourierTransform;

public class ImageBeautifierFragment extends Fragment {

    private ImageBeautifierFragment.OnFragmentInteractionListener mListener;

    private ImageView originalImageView;
    private ImageView resultImageView;
    private TextView loadTextView;

    private Button bradleyButton;
    private Button sauvolaButton;
    private Button niblackButton;
    private Button wolfButton;
    private Button fftButton;

    private Button commitButton;

    private SharedViewModel model;
    private Bitmap originalBitmap;
    private Bitmap resultBitmap;

    private PhotoViewAttacher mAttacher;

    private EditText threshold1;
    private EditText threshold2;



    public ImageBeautifierFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_image_beautifier, container, false);

        originalImageView = view.findViewById(R.id.image_beautifier_fr_iv_orig);
        resultImageView = view.findViewById(R.id.image_beautifier_fr_iv_result);
        mAttacher = new PhotoViewAttacher(resultImageView);
        mAttacher.update();
        loadTextView = view.findViewById(R.id.image_beautifier_fr_tv_load_first);

        threshold1 = view.findViewById(R.id.threshold_1);
        threshold2 = view.findViewById(R.id.threshold_2);
        bradleyButton = view.findViewById(R.id.image_beautifier_fr_btn_bradley);
        sauvolaButton = view.findViewById(R.id.image_beautifier_fr_btn_sauvola);
        niblackButton = view.findViewById(R.id.image_beautifier_fr_btn_niblack);
        wolfButton = view.findViewById(R.id.image_beautifier_fr_btn_wolf);
        fftButton = view.findViewById(R.id.image_beautifier_fr_btn_fft);

        commitButton = view.findViewById(R.id.image_beautifier_fr_btn_commit);

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
        final ImageBeautifierFragment fr = this;
        bradleyButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (originalBitmap == null) {
                    Toast.makeText(fr.getContext(),
                            R.string.ask_to_select_or_capture_an_image, Toast.LENGTH_SHORT).show();
                    return;
                }
                int threshold1Number = 0;
                int threshold2Number = 0;

                new ImageBeautifierTask(fr, "Bradley", threshold1Number, threshold2Number).execute(originalBitmap);
            }
        });
        sauvolaButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (originalBitmap == null) {
                    Toast.makeText(fr.getContext(),
                            R.string.ask_to_select_or_capture_an_image, Toast.LENGTH_SHORT).show();
                    return;
                }
                int threshold1Number = 0;
                int threshold2Number = 0;

                new ImageBeautifierTask(fr, "Sauvola", threshold1Number, threshold2Number).execute(originalBitmap);
            }
        });
        niblackButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (originalBitmap == null) {
                    Toast.makeText(fr.getContext(),
                            R.string.ask_to_select_or_capture_an_image, Toast.LENGTH_SHORT).show();
                    return;
                }
                int threshold1Number = 0;
                int threshold2Number = 0;

                new ImageBeautifierTask(fr, "Niblack", threshold1Number, threshold2Number).execute(originalBitmap);
            }
        });
        wolfButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (originalBitmap == null) {
                    Toast.makeText(fr.getContext(),
                            R.string.ask_to_select_or_capture_an_image, Toast.LENGTH_SHORT).show();
                    return;
                }
                int threshold1Number = 0;
                int threshold2Number = 0;

                new ImageBeautifierTask(fr, "Wolf", threshold1Number, threshold2Number).execute(originalBitmap);
            }
        });
        fftButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (originalBitmap == null) {
                    Toast.makeText(fr.getContext(),
                            R.string.ask_to_select_or_capture_an_image, Toast.LENGTH_SHORT).show();
                    return;
                }
                int threshold1Number = Integer.parseInt(threshold1.getText().toString());
                int threshold2Number = Integer.parseInt(threshold2.getText().toString());

                new ImageBeautifierTask(fr, "FFT", threshold1Number, threshold2Number).execute(originalBitmap);
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
        mAttacher.update();
        commitButton.setVisibility(View.VISIBLE);
    }

}