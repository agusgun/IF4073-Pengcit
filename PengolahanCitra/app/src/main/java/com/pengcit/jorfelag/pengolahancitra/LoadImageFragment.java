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


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link LoadImageFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link LoadImageFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class LoadImageFragment extends Fragment {

    // Tag for logging
    private static final String TAG = "LoadImageFragment";

    // Request ID
    private static final int SELECT_IMAGE = 2;

    private Context ctx;
    private SharedViewModel model;
    private ImageView origImageView;
    private TextView textView;

    private Bitmap imageBitmap;
    private Uri imageBitmapURI;


    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    private OnFragmentInteractionListener mListener;

    public LoadImageFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment LoadImageFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static LoadImageFragment newInstance(String param1, String param2) {
        LoadImageFragment fragment = new LoadImageFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }

    }

    // TODO: Rename method, update argument and hook method into UI event
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

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
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
                imageBitmap = BitmapFactory.decodeStream(imageStream);

                model.setBitmap(imageBitmap);
                origImageView.setImageBitmap(imageBitmap);
            } catch (FileNotFoundException e) {
                Log.e(TAG, getString(R.string.file_not_found_for_image_uri) + imageBitmapURI);
                e.printStackTrace();
            }
        }
    }
}
