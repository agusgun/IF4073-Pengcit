package com.pengcit.jorfelag.pengolahancitra.face_detection;

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
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.pengcit.jorfelag.pengolahancitra.R;
import com.pengcit.jorfelag.pengolahancitra.SharedViewModel;

import java.util.concurrent.TimeoutException;

public class FaceDetectionFragment extends Fragment {
    private OnFragmentInteractionListener mListener;

    private ImageView originalImageView;
    private ImageView resultImageView;
    private TextView loadTextView;

    private Button processButton;
    private Button commitButton;

    private SharedViewModel model;
    private Bitmap originalBitmap;
    private Bitmap resultBitmap;

    private TextView alisKiriTextView;
    private TextView alisKananTextView;
    private TextView mataKiriTextView;
    private TextView mataKananTextView;
    private TextView hidungTextView;
    private TextView mulutTextView;
    private TextView resultTextView;

    public FaceDetectionFragment() {
        // Required empty public constructor
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_face_detection, container, false);

        originalImageView = view.findViewById(R.id.face_detection_fr_iv_orig);
        resultImageView = view.findViewById(R.id.face_detection_fr_iv_result);
        loadTextView = view.findViewById(R.id.face_detection_fr_tv_load_first);

        processButton = view.findViewById(R.id.face_detection_fr_btn_process);
        commitButton = view.findViewById(R.id.face_detection_fr_btn_commit);

        resultTextView = view.findViewById(R.id.face_detection_fr_tv_result);

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
        final FaceDetectionFragment fr = this;
        processButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (originalBitmap == null) {
                    Toast.makeText(fr.getContext(),
                            R.string.ask_to_select_or_capture_an_image, Toast.LENGTH_SHORT).show();
                    return;
                }
                new FaceDetectionTask(fr).execute(originalBitmap);
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

    public void setRecognitionResultTextView(String text) {
        resultTextView.setText(text);
    }
}
