package com.example.demoui.activites;
//package com.example.demoui.activites;
//
//import com.example.demoui.R;
//import android.Manifest;
//import android.annotation.SuppressLint;
//import android.content.pm.PackageManager;
//import android.graphics.Bitmap;
//import android.graphics.BitmapFactory;
//import android.graphics.Matrix;
//import android.graphics.SurfaceTexture;
//import android.os.Bundle;
//import android.util.Log;
//import android.util.Size;
//import android.util.SparseArray;
//import android.view.TextureView;
//import android.view.View;
//import android.widget.Button;
//import android.widget.TextView;
//import android.widget.Toast;
//
//import androidx.annotation.NonNull;
//import androidx.appcompat.app.AppCompatActivity;
//import androidx.camera.core.Camera;
//import androidx.camera.core.CameraSelector;
//import androidx.camera.core.CameraX;
//import androidx.camera.core.ImageAnalysis;
//import androidx.camera.core.impl.ImageAnalysisConfig;
//import androidx.camera.core.ImageCapture;
//import androidx.camera.core.ImageCaptureException;
//import androidx.camera.core.ImageCapture.Metadata;
//import androidx.camera.core.ImageProxy;
//import androidx.camera.core.Preview;
//import androidx.camera.core.impl.PreviewConfig;
//import androidx.camera.lifecycle.ProcessCameraProvider;
//import androidx.camera.view.PreviewView;
//import androidx.core.app.ActivityCompat;
//import androidx.core.content.ContextCompat;
//import androidx.lifecycle.LifecycleOwner;
//
//import com.google.android.gms.tasks.OnCompleteListener;
//import com.google.android.gms.tasks.OnFailureListener;
//import com.google.android.gms.tasks.OnSuccessListener;
//import com.google.android.gms.tasks.Task;
//import com.google.android.gms.vision.Frame;
//import com.google.common.util.concurrent.ListenableFuture;
//import com.google.mlkit.vision.common.InputImage;
//import com.google.mlkit.vision.text.Text;
//import com.google.mlkit.vision.text.TextRecognition;
//import com.google.mlkit.vision.text.TextRecognizer;
//import com.google.mlkit.vision.text.latin.TextRecognizerOptions;
//
//import java.io.File;
//import java.text.SimpleDateFormat;
//import java.util.Date;
//import java.util.Locale;
//import java.util.concurrent.ExecutionException;
//import java.util.concurrent.Executor;
//import java.util.concurrent.Executors;
//
//public class MainActivity extends AppCompatActivity implements TextureView.SurfaceTextureListener {
//
//    private static final String TAG = "MainActivity";
//    private static final int REQUEST_CAMERA_PERMISSION = 200;
//
//    private PreviewView previewView;
//    private TextView textView;
//    private Button captureButton;
//
//    private ListenableFuture<ProcessCameraProvider> cameraProviderFuture;
//    private ImageCapture imageCapture;
//    private Executor executor = Executors.newSingleThreadExecutor();
//
//    @SuppressLint("MissingInflatedId")
//    @Override
//    protected void onCreate(Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//        setContentView(R.layout.activity_main);
//
//        previewView = findViewById(R.id.previewView);
//        textView = findViewById(R.id.textView);
//        captureButton = findViewById(R.id.captureButton);
//
//        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
//                != PackageManager.PERMISSION_GRANTED) {
//            ActivityCompat.requestPermissions(this,
//                    new String[]{Manifest.permission.CAMERA}, REQUEST_CAMERA_PERMISSION);
//        } else {
//            startCamera();
//        }
//
//        captureButton.setOnClickListener(v -> takePhoto());
//    }
//
//    private void startCamera() {
//        cameraProviderFuture = ProcessCameraProvider.getInstance(this);
//        cameraProviderFuture.addListener(new Runnable() {
//            @Override
//            public void run() {
//                try {
//                    ProcessCameraProvider cameraProvider = cameraProviderFuture.get();
//                    Preview preview = new Preview.Builder()
//                            .setTargetResolution(new Size(640, 480))
//                            .build();
//                    preview.setSurfaceProvider(previewView.getSurfaceProvider());
//
//                    ImageAnalysis imageAnalysis = new ImageAnalysis.Builder()
//                            .setTargetResolution(new Size(640, 480))
//                            .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
//                            .build();
//
//                    imageAnalysis.setAnalyzer(executor, new ImageAnalysis.Analyzer() {
//                        @Override
//                        public void analyze(@NonNull ImageProxy imageProxy) {
//                            // Get the bitmap from the preview view
//                            previewView.post(new Runnable() {
//                                @Override
//                                public void run() {
//                                    Bitmap bitmap = previewView.getBitmap();
//                                    // Process the bitmap using ML Kit
//                                    InputImage inputImage = InputImage.fromBitmap(bitmap, 0);
//                                    TextRecognizer recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS);
//                                    recognizer.process(inputImage)
//                                            .addOnSuccessListener(new OnSuccessListener<Text>() {
//                                                @Override
//                                                public void onSuccess(Text text) {
//                                                    // Display the text in the UI
//                                                    if (textView != null) {
//                                                        textView.setText(text.getText());
//                                                    }
//                                                }
//                                            })
//                                            .addOnFailureListener(new OnFailureListener() {
//                                                @Override
//                                                public void onFailure(@NonNull Exception e) {
//                                                    Log.e(TAG, "Text recognition failed: " + e.getMessage());
//                                                }
//                                            })
//                                            .addOnCompleteListener(new OnCompleteListener<Text>() {
//                                                @Override
//                                                public void onComplete(@NonNull Task<Text> task) {
//                                                    // Release the image proxy
//                                                    imageProxy.close();
//                                                }
//                                            });
//                                }
//                            });
//                        }
//                    });
//
//                    imageCapture = new ImageCapture.Builder()
//                            .setCaptureMode(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY)
//                            .setFlashMode(ImageCapture.FLASH_MODE_AUTO)
//                            .build();
//
//                    CameraSelector cameraSelector = new CameraSelector.Builder()
//                            .requireLensFacing(CameraSelector.LENS_FACING_BACK)
//                            .build();
//
//                    Camera camera = cameraProvider.bindToLifecycle((LifecycleOwner) MainActivity.this,
//                            cameraSelector, preview, imageAnalysis, imageCapture);
//
//                } catch (ExecutionException | InterruptedException e) {
//                    Log.e(TAG, "Error starting camera", e);
//                }
//            }
//        }, ContextCompat.getMainExecutor(this));
//    }
//
//
//
//    private void takePhoto() {
//        ImageCapture.OutputFileOptions outputFileOptions =
//                new ImageCapture.OutputFileOptions.Builder(new File(getOutputFile()))
//                        .build();
//
//        try {
//            imageCapture.takePicture(outputFileOptions, executor,
//                    new ImageCapture.OnImageSavedCallback() {
//                        @Override
//                        public void onImageSaved(@NonNull ImageCapture.OutputFileResults outputFileResults) {
//                            Toast.makeText(MainActivity.this,
//                                    "Image saved successfully", Toast.LENGTH_SHORT).show();
//                        }
//
//                        @Override
//                        public void onError(@NonNull ImageCaptureException error) {
//                            Log.e(TAG, "Error capturing image", error);
//                            Toast.makeText(MainActivity.this,
//                                    "Error capturing image", Toast.LENGTH_SHORT).show();
//                        }
//                    });
//        } catch (Exception e) {
//            e.printStackTrace();
//            Toast.makeText(MainActivity.this,
//                    "Error capturing image", Toast.LENGTH_SHORT).show();
//        }
//    }
//
//    private String getOutputFile() {
//        return getExternalMediaDirs()[0].getAbsolutePath() + "/photo.jpg";
//    }
//
//    @Override
//    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
//                                           @NonNull int[] grantResults) {
//        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
//        if (requestCode == REQUEST_CAMERA_PERMISSION) {
//            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
//                startCamera();
//            } else {
//                Toast.makeText(this, "Camera permission required", Toast.LENGTH_SHORT).show();
//            }
//        }
//    }
//
//    @Override
//    public void onSurfaceTextureAvailable(@NonNull SurfaceTexture surface, int width, int height) {
//        startCamera();
//    }
//
//    @Override
//    public void onSurfaceTextureSizeChanged(@NonNull SurfaceTexture surface, int width, int height) {
//    }
//
//    @Override
//    public boolean onSurfaceTextureDestroyed(@NonNull SurfaceTexture surface) {
//        return true;
//    }
//
//    @Override
//    public void onSurfaceTextureUpdated(@NonNull SurfaceTexture surface) {
//    }
//
//}


import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.demoui.R;
import com.google.android.gms.tasks.OnFailureListener;
//import com.google.android.gms.tasks.OnSuccessListener;
//import com.google.firebase.ml.vision.FirebaseVision;
//import com.google.firebase.ml.vision.common.FirebaseVisionImage;
//import com.google.firebase.ml.vision.text.FirebaseVisionText;
//import com.google.firebase.ml.vision.text.FirebaseVisionTextRecognizer;


public class MainActivity extends AppCompatActivity {

    private static final int REQUEST_IMAGE_CAPTURE = 1;

    private ImageView imageView;
    private TextView textView;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        imageView = findViewById(R.id.image_view);
        textView = findViewById(R.id.text_view);

        Button captureButton = findViewById(R.id.capture_button);
        captureButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dispatchTakePictureIntent();
            }
        });
    }

    private void dispatchTakePictureIntent() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
            Bundle extras = data.getExtras();
            Bitmap imageBitmap = (Bitmap) extras.get("data");
            imageView.setImageBitmap(imageBitmap);
//            recognizeTextFromImage(imageBitmap);
        }
    }


//    private void recognizeTextFromImage(Bitmap imageBitmap) {
//        FirebaseVisionImage image = FirebaseVisionImage.fromBitmap(imageBitmap);
//        FirebaseVisionTextRecognizer recognizer = FirebaseVision.getInstance()
//                .getOnDeviceTextRecognizer();
//
//        recognizer.processImage(image)
//                .addOnSuccessListener(new OnSuccessListener<FirebaseVisionText>() {
//                    @Override
//                    public void onSuccess(FirebaseVisionText firebaseVisionText) {
//                        String recognizedText = firebaseVisionText.getText();
//                        textView.setText(recognizedText);
//                    }
//                })
//                .addOnFailureListener(new OnFailureListener() {
//                    @Override
//                    public void onFailure(@NonNull Exception e) {
//                        Log.e("OCR", "Error recognizing text from image", e);
//                        textView.setText(getString(R.string.recognition_error_message));
//                    }
//                });
//    }
}
