package com.pengcit.jorfelag.pengolahancitra;

import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import java.io.FileNotFoundException;
import java.io.InputStream;


public class LoadImageFragment extends Fragment {

    // Tag for logging
    private static final String TAG = "LoadImageFragment";

    // Request ID
    private static final int SELECT_IMAGE = 2;

    private Context ctx;
    private SharedViewModel model;
    private ImageView origImageView;
    private TextView textView;

    private Uri imageBitmapURI;

    private OnFragmentInteractionListener mListener;

    public LoadImageFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    public void onButtonPressed(Uri uri) {
        if (mListener != null) {
            mListener.onFragmentInteraction(uri);
        }
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_load_image, container, false);

        ctx = view.getContext();
        origImageView = view.findViewById(R.id.load_image_fr_iv_orig);
        textView = view.findViewById(R.id.load_image_fr_tv);

        Button loadImageBtn = view.findViewById(R.id.load_image_fr_btn_load_image);
        loadImageBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                intent.setType("image/*");
                startActivityForResult(Intent.createChooser(intent, getString(R.string.select_picture)), SELECT_IMAGE);
            }
        });

        model = ViewModelProviders.of(getActivity()).get(SharedViewModel.class);
        model.getBitmapLiveData().observe(this, new Observer<Bitmap>() {
            @Override
            public void onChanged(@Nullable Bitmap bitmap) {
                if (bitmap != null) {
                    origImageView.setImageBitmap(bitmap);
                    textView.setText("");
                } else {
                    origImageView.setImageResource(android.R.color.transparent);
                    textView.setText(getResources().getString(R.string.load_image_fr_tv_text));
                }
            }
        });

        return view;
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

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        try {
            if (requestCode == SELECT_IMAGE) {
                imageBitmapURI = data.getData();
                origImageView.setRotation(0);
            }
        } catch (NullPointerException npe) {
            npe.printStackTrace();
        }

        if (imageBitmapURI != null) {
            origImageView.setImageURI(imageBitmapURI);
            try {
                InputStream imageStream = ctx.getContentResolver().openInputStream(imageBitmapURI);
                Bitmap imageBitmap = BitmapFactory.decodeStream(imageStream);
                model.setBitmap(imageBitmap);
            } catch (FileNotFoundException e) {
                Log.e(TAG, getString(R.string.file_not_found_for_image_uri) + imageBitmapURI);
                e.printStackTrace();
            }
        }
    }
}
