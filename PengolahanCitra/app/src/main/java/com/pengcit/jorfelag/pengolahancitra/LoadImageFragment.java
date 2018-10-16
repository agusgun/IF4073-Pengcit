package com.pengcit.jorfelag.pengolahancitra;

import android.Manifest;
import android.annotation.SuppressLint;
import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.content.FileProvider;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Date;

import static android.app.Activity.RESULT_OK;


public class LoadImageFragment extends Fragment {

    // Tag for logging
    private static final String TAG = "LoadImageFragment";

    // Request ID
    private static final int REQUEST_CAMERA = 0;
    private static final int IMAGE_CAPTURE = 1;
    private static final int SELECT_IMAGE = 2;

    private Context ctx;
    private SharedViewModel model;
    private ImageView origImageView;
    private TextView textView;

    private String currentImagePath;

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

        Button takeImageBtn = view.findViewById(R.id.load_image_fr_btn_take_image);
        takeImageBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                takeImage();
            }
        });

        Button loadImageBtn = view.findViewById(R.id.load_image_fr_btn_load_image);
        loadImageBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loadImage();
            }
        });

        Button saveImageBtn = view.findViewById(R.id.load_image_fr_btn_save_image);
        saveImageBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveImage();
            }
        });

        model = ViewModelProviders.of(getActivity()).get(SharedViewModel.class);
        model.getBitmapLiveData().observe(this, new Observer<Bitmap>() {
            @Override
            public void onChanged(@Nullable Bitmap bitmap) {
                if (bitmap != null) {
                    origImageView.setImageBitmap(bitmap);
                    textView.setVisibility(View.GONE);
                } else {
                    origImageView.setImageResource(android.R.color.transparent);
                    textView.setVisibility(View.VISIBLE);
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

    private void takeImage() {
        Log.i(TAG, getString(R.string.taking_a_picture));
        if (ActivityCompat.checkSelfPermission(ctx, Manifest.permission.CAMERA) !=
                PackageManager.PERMISSION_GRANTED) {
            Log.i(TAG, getString(R.string.camera_permission_not_granted));
            ActivityCompat.requestPermissions(this.getActivity(),
                    new String[]{Manifest.permission.CAMERA},
                    REQUEST_CAMERA);
        } else {
            Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            if (intent.resolveActivity(ctx.getPackageManager()) != null) {
                File imageFile = null;
                try {
                    imageFile = createImageFile();
                } catch (IOException ex) {
                    Log.e(TAG, getString(R.string.fail_to_create_image));
                }

                if (imageFile != null) {
                    Uri imageURI = FileProvider.getUriForFile(ctx,
                            "com.pengcit.jorfelag.pengolahancitra.provider",
                            imageFile);
                    intent.putExtra(MediaStore.EXTRA_OUTPUT, imageURI);
                    startActivityForResult(intent, IMAGE_CAPTURE);
                }
            } else {
                Log.i(TAG, getString(R.string.no_activity_available_to_resolve_intent));
            }
        }
    }

    private void loadImage() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("image/*");
        startActivityForResult(Intent.createChooser(intent, getString(R.string.select_picture)), SELECT_IMAGE);
    }

    private void saveImage() {
        // TODO: implement
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode != RESULT_OK) {
            return;
        }

        try {
            if (requestCode == IMAGE_CAPTURE) {
                Log.e(TAG, currentImagePath);
                File imageFile = new File(currentImagePath);
                if (imageFile.exists()) {
                    imageBitmapURI = Uri.fromFile(imageFile);
                    origImageView.setRotation(0);
                }
            } else if (requestCode == SELECT_IMAGE) {
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

    private File createImageFile() throws IOException {
        // Create an image file name
        @SuppressLint("SimpleDateFormat") String timeStamp =
                new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = ctx.getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(
                imageFileName,
                ".jpg",
                storageDir
        );

        currentImagePath = image.getAbsolutePath();
        return image;
    }

    public interface OnFragmentInteractionListener {
        void onFragmentInteraction(Uri uri);
    }
}
