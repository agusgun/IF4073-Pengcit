package com.pengcit.jorfelag.pengolahancitra;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.speech.tts.TextToSpeech;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.leinardi.android.speeddial.SpeedDialActionItem;
import com.leinardi.android.speeddial.SpeedDialView;
import com.pengcit.jorfelag.pengolahancitra.contrast.enhancement.ContrastEnhancementActivity;
import com.pengcit.jorfelag.pengolahancitra.histogram.CreateImageHistogramTask;
import com.pengcit.jorfelag.pengolahancitra.histogram.specification.HistogramSpecificationActivity;
import com.pengcit.jorfelag.pengolahancitra.image_skeletonization.ImageSkeletonizationActivity;
import com.pengcit.jorfelag.pengolahancitra.ocr.ChainCode;
import com.pengcit.jorfelag.pengolahancitra.ocr.OcrTask;
import com.pengcit.jorfelag.pengolahancitra.ocr.TrainOcrTask;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import static android.content.SharedPreferences.*;

public class MainActivity extends AppCompatActivity {

    private ImageView imageView;
    private TextView textView;
    private String currentImagePath;
    private Bitmap imageBitmap;
    private Uri imageBitmapURI;

    private SharedPreferences prefs;
    private TextToSpeech textToSpeech;
    private List<ChainCode> chainCodes;

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

    /**
     * Id to identify a skeletonization request
     */
    private static final int SKELETONIZATION_IMAGE = 3;

    private static final String PREFS_NAME = "ChainCode_Models";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        imageView = (ImageView) findViewById(R.id.imageView);
        textView = (TextView) findViewById(R.id.textView);

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
                    case R.id.action_histogram_spesification:
                        launchHistogramSpesification();
                        return false;
                    case R.id.action_train_ocr:
                        trainOcr();
                        return false;
                    case R.id.action_ocr:
                        launchOCR();
                        return false;
                    case R.id.action_skeletonize_image:
                        launchImageSkeletonization();
                        return false;
                    default:
                        return false;
                }
            }
        });

        textToSpeech = new TextToSpeech(getApplicationContext(), new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {
                if (status != TextToSpeech.ERROR) {
                    textToSpeech.setLanguage(new Locale("id","ID"));
                }
            }
        });

        textView.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                textToSpeech.speak(editable.toString(), TextToSpeech.QUEUE_FLUSH, null);
                Gson gson = new Gson();
                String json = gson.toJson(chainCodes);
                Log.i("B", json);
            }
        });

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) !=
                PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE},
                    REQUEST_CAMERA);
        }

        prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        Gson gson = new Gson();
        String json = prefs.getString("ChainCodes", "");
        chainCodes = gson.fromJson(json, new TypeToken<List<ChainCode>>(){}.getType());
        if (chainCodes == null) {
            chainCodes = new ArrayList<>();
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

    public void launchHistogramSpesification() {
        if (imageBitmap != null) {
            Intent intent = new Intent(this, HistogramSpecificationActivity.class);
            intent.putExtra("BitmapImageURI", imageBitmapURI.toString());
            startActivity(intent);
        } else {
            Toast.makeText(getApplicationContext(), R.string.ask_to_select_or_capture_an_image, Toast.LENGTH_SHORT).show();
        }
    }

    public void launchOCR() {
        if (imageBitmap != null) {
            new OcrTask(MainActivity.this, chainCodes, textView).execute(imageBitmap);
        } else {
            Toast.makeText(getApplicationContext(), R.string.ask_to_select_or_capture_an_image, Toast.LENGTH_SHORT).show();
        }
    }

    public void trainOcr() {
        if (imageBitmap != null) {
            // get ocr_label_prompt.xml view
            LayoutInflater li = LayoutInflater.from(this);
            View promptsView = li.inflate(R.layout.ocr_label_prompt, null);

            AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);

            // set ocr_label_prompt.xml to alertdialog builder
            alertDialogBuilder.setView(promptsView);

            final EditText userInput = (EditText) promptsView
                    .findViewById(R.id.editText);

            alertDialogBuilder
                    .setCancelable(false)
                    .setPositiveButton("OK",
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog,int id) {
                                    Editor prefsEditor = prefs.edit();
                                    new TrainOcrTask(MainActivity.this, chainCodes, userInput.getText().toString(), prefsEditor).execute(imageBitmap);
                                }
                            })
                    .setNegativeButton("Cancel",
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog,int id) {
                                    dialog.cancel();
                                }
                            });

            // create alert dialog
            AlertDialog alertDialog = alertDialogBuilder.create();

            // show it
            alertDialog.show();
        } else {
            Toast.makeText(getApplicationContext(), R.string.ask_to_select_or_capture_an_image, Toast.LENGTH_SHORT).show();
        }
    }

    public void launchImageSkeletonization() {
        if (imageBitmap != null) {
            Intent intent = new Intent(this, ImageSkeletonizationActivity.class);
            intent.putExtra("BitmapImageURI", imageBitmapURI.toString());
            startActivityForResult(intent, SKELETONIZATION_IMAGE);
        } else {
            Toast.makeText(getApplicationContext(), R.string.ask_to_select_or_capture_an_image, Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_OK) {
            textView.setText("");
            imageBitmapURI = null;
            if (requestCode == IMAGE_CAPTURE) {
                Log.e(TAG, currentImagePath);
                File imageFile = new File(currentImagePath);
                if (imageFile.exists()) {
                    imageBitmapURI = Uri.fromFile(imageFile);
                    imageView.setRotation(90);
                }
            } else if (requestCode == SELECT_IMAGE) {
                imageBitmapURI = data.getData();
                imageView.setRotation(0);
            } else if (requestCode == SKELETONIZATION_IMAGE) {

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

        currentImagePath = image.getAbsolutePath();
        return image;
    }
}
