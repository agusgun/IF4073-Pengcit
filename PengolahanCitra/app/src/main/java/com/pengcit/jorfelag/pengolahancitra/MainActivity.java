package com.pengcit.jorfelag.pengolahancitra;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;
import android.widget.Toast;

import com.leinardi.android.speeddial.SpeedDialActionItem;
import com.leinardi.android.speeddial.SpeedDialView;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Date;

public class MainActivity extends AppCompatActivity {

    private ImageView imageView;
    String mCurrentImagePath;
    Bitmap imageBitmap;
    Uri imageBitmapURI;

    /**
     * Tag for logging.
     */
    private static final String TAG = "MainActivity";

    /**
     * Id to identify a camera permission request.
     */
    private static final int REQUEST_CAMERA = 0;


    /**
     * Id to identify a image capture request.
     */
    private static final int IMAGE_CAPTURE = 1;


    /**
     * Id to identify a select image request.
     */
    private static final int SELECT_IMAGE = 2;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        imageView = (ImageView) findViewById(R.id.imageView);

        SpeedDialView speedDialView = findViewById(R.id.speedDial);
        speedDialView.inflate(R.menu.menu_speed_dial);
        speedDialView.setOnActionSelectedListener(new SpeedDialView.OnActionSelectedListener() {
            @Override
            public boolean onActionSelected(SpeedDialActionItem speedDialActionItem) {
                switch (speedDialActionItem.getId()) {
                    case R.id.action_camera:
                        takePicture();
                        return false; // true to keep the Speed Dial open
                    case R.id.action_select_picture:
                        selectPicture();
                        return false;
                    case R.id.action_show_histogram:
                        showHistogram();
                        return false;
                    case R.id.action_constrast_enhancement:
                        launchContrastEnhancement();
                        return false;
                    default:
                        return false;
                }
            }
        });

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) !=
                PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE},
                    REQUEST_CAMERA);
        }
    }

    /**
     * Called when the 'take a picture' button is clicked.
     * Open the camera app to take the picture to be processed.
     */
    public void takePicture() {
        Log.i(TAG, getString(R.string.taking_a_picture));
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA) !=
                PackageManager.PERMISSION_GRANTED) {
            Log.i(TAG, getString(R.string.camera_permission_not_granted));
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.CAMERA},
                    REQUEST_CAMERA);
        } else {
            Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            if (intent.resolveActivity(getPackageManager()) != null) {
                File imageFile = null;
                try {
                    imageFile = createImageFile();
                } catch (IOException ex) {
                    Log.e(TAG, getString(R.string.fail_to_create_image));
                }

                if (imageFile != null) {
                    Uri imageURI = FileProvider.getUriForFile(this,
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

    /**
     * Called when the 'select a picture' button is clicked.
     * Open file manager to choose the picture to be processed.
     */
    public void selectPicture() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("image/*");
        startActivityForResult(Intent.createChooser(intent, getString(R.string.select_picture)), SELECT_IMAGE);
    }

    /**
     * Called when the 'show histogram' button is clicked.
     * Launch a new activity that show the image histogram for RGB and grayscale values.
     */
    public void showHistogram() {
        if (imageBitmap != null) {
            new CreateImageHistogramTask(this).execute(imageBitmap);
        } else {
            Toast.makeText(getApplicationContext(), R.string.select_or_capture_image, Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Called when the 'contrast enhancement' button is clicked.
     * Launch contrast enhancement activity.
     */
    public void launchContrastEnhancement() {
        if (imageBitmap != null) {
            Intent intent = new Intent(this, ContrastEnhancementActivity.class);
            intent.putExtra("BitmapImageURI", imageBitmapURI.toString());
            startActivity(intent);
        } else {
            Toast.makeText(getApplicationContext(), R.string.ask_to_select_or_capture_an_image, Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_OK) {
            imageBitmapURI = null;
            if (requestCode == IMAGE_CAPTURE) {
                Log.e(TAG, mCurrentImagePath);
                File imageFile = new File(mCurrentImagePath);
                if (imageFile.exists()) {
                    imageBitmapURI = Uri.fromFile(imageFile);
                    imageView.setRotation(90);
                }
            } else if (requestCode == SELECT_IMAGE) {
                imageBitmapURI = data.getData();
                imageView.setRotation(0);
            }
            if (imageBitmapURI != null) {
                imageView.setImageURI(imageBitmapURI);
                try {
                    InputStream imageStream = getContentResolver().openInputStream(imageBitmapURI);
                    imageBitmap = BitmapFactory.decodeStream(imageStream);
                } catch (FileNotFoundException e) {
                    Log.e(TAG, getString(R.string.file_not_found_for_image_uri) + imageBitmapURI);
                    e.printStackTrace();
                }
            }
        }
    }

    /**
     * Create a new image file.
     * @return The created image file.
     * @throws IOException thrown when failed to create file.
     */
    private File createImageFile() throws IOException {
        // Create an image file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(
                imageFileName,
                ".jpg",
                storageDir
        );

        mCurrentImagePath = image.getAbsolutePath();
        return image;
    }

}
