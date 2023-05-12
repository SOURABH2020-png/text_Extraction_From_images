    package com.example.google_play_services_test;

    import static android.content.ContentValues.TAG;

    import androidx.annotation.NonNull;
    import androidx.appcompat.app.AppCompatActivity;
    import androidx.core.app.ActivityCompat;
    import androidx.core.content.ContextCompat;

    import android.Manifest;
    import android.annotation.SuppressLint;
    import android.content.ClipData;
    import android.content.ClipboardManager;
    import android.content.Context;
    import android.content.Intent;
    import android.content.pm.PackageManager;
    import android.graphics.Bitmap;
    import android.graphics.BitmapFactory;
    import android.os.Bundle;
    import android.speech.RecognitionListener;
    import android.speech.RecognizerIntent;
    import android.speech.SpeechRecognizer;
    import android.util.Log;
    import android.util.SparseArray;
    import android.view.SurfaceHolder;
    import android.view.SurfaceView;
    import android.view.View;
    import android.widget.Button;
    import android.widget.ImageView;
    import android.widget.TextView;
    import android.widget.Toast;

    import com.google.android.gms.vision.CameraSource;
    import com.google.android.gms.vision.Detector;
    import com.google.android.gms.vision.Frame;
    import com.google.android.gms.vision.text.TextBlock;
    import com.google.android.gms.vision.text.TextRecognizer;

    import java.io.IOException;
    import java.util.ArrayList;
    import java.util.List;
    import java.util.Locale;

    import android.speech.RecognizerIntent;

    public class MainActivity extends AppCompatActivity {

        private static final int REQUEST_IMAGE_CAPTURE = 1;
        private SurfaceView surfaceView;
        private CameraSource cameraSource;
        private Button btnCapture, btn_copy, btnSpeech, btnReOpenCamera;
        private TextView camera_Image_Text, speech_text;
        private ImageView ivImagePreview;

        private static final int REQUEST_CAMERA_PERMISSIONS = 1001;
        private static final int REQUEST_RECORD_AUDIO_PERMISSION = 200;

        private boolean permissionToRecordAccepted = false;
        private String[] permissions = {Manifest.permission.RECORD_AUDIO};

        private SpeechRecognizer speechRecognizer;
        private Intent speechRecognizerIntent;
        private boolean isListening = false;

        @SuppressLint("MissingInflatedId")
        @Override
        protected void onCreate(Bundle savedInstanceState) {

            super.onCreate(savedInstanceState);
            setContentView(R.layout.activity_main);

            surfaceView = findViewById(R.id.surface_view);
            btnCapture = findViewById(R.id.btn_capture);
            camera_Image_Text = findViewById(R.id.camera_Image_Text);
            speech_text = findViewById(R.id.speech_text);
            btn_copy = findViewById(R.id.btn_copy);
            ivImagePreview = findViewById(R.id.ivImagePreview);
            btnSpeech = findViewById(R.id.btnSpeech);
            btnReOpenCamera = findViewById(R.id.btnReOpenCamera);

            // Create a new SpeechRecognizer instance
            speechRecognizer = SpeechRecognizer.createSpeechRecognizer(this);

            btnCapture.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    captureImage();
                }
            });



            // Set the recognition listener
            speechRecognizer.setRecognitionListener(new RecognitionListener() {
                @Override
                public void onReadyForSpeech(Bundle params) {
                    // Called when the listener is ready to start receiving speech input
                    Toast.makeText(getApplicationContext(), "Ready For Speech", Toast.LENGTH_SHORT).show();

                }

                @Override
                public void onBeginningOfSpeech() {
                    // Called when the user starts speaking
                    Toast.makeText(getApplicationContext(), "Beginning Of Speech", Toast.LENGTH_SHORT).show();

                }

                @Override
                public void onRmsChanged(float rmsdB) {
                    // Called when the volume of the input speech changes
    //                Toast.makeText(getApplicationContext(), "RMS changed " + rmsdB, Toast.LENGTH_SHORT).show();
                }

                @Override
                public void onBufferReceived(byte[] buffer) {
                    // Called when partial recognition results are available
                    Toast.makeText(getApplicationContext(), "Buffer Received", Toast.LENGTH_SHORT).show();
                }

                @Override
                public void onEndOfSpeech() {
                    // Called when the user stops speaking
                    Toast.makeText(getApplicationContext(), "End of Speech", Toast.LENGTH_SHORT).show();
                }

                @Override
                public void onError(int error) {
                    // Called when there is an error in speech recognition
                    Toast.makeText(getApplicationContext(), "ERROR", Toast.LENGTH_SHORT).show();
                }

                @Override
                public void onResults(Bundle results) {
                    // Called when recognition results are ready
                    ArrayList<String> matches = results.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
    //                String text = matches.get(0); // get the recognized text
    //                Log.d(TAG, "onResults: " + text); // this is for console
                    if (matches != null && matches.size() > 0) {
                        // Update the text view with the recognized speech
                        speech_text.setText(matches.get(0));
                    }
                }

                @Override
                public void onPartialResults(Bundle partialResults) {
                    // Called when partial recognition results are available
                }

                @Override
                public void onEvent(int eventType, Bundle params) {
                    // Called when a recognition event occurs
                }
            });

            // Set click listener on the speech button
            btnSpeech.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    // Start listening for speech input
                    Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
                    intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
                    intent.putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 1);
                    speechRecognizer.startListening(intent);

                    Toast.makeText(getApplicationContext(), "listening", Toast.LENGTH_SHORT).show();
                    // Show the "Open Camera" button
                    btnReOpenCamera.setVisibility(View.VISIBLE);
                }
            });

            btn_copy.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    String textToCopy = camera_Image_Text.getText().toString() + "" + speech_text.getText().toString();
                    ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
                    ClipData clip = ClipData.newPlainText("text", textToCopy);
                    clipboard.setPrimaryClip(clip);
                    Toast.makeText(getApplicationContext(), "Text copied to clipboard", Toast.LENGTH_SHORT).show();
                }
            });

            btnReOpenCamera.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    // Clear All Text Field
                    camera_Image_Text.setText("");
                    speech_text.setText("");

                    startCameraPreview();
                    // Show the "Open Camera" button
                    btnReOpenCamera.setVisibility(View.GONE);
                }
            });
            createCameraSource();
        }


        private void createCameraSource() {
            TextRecognizer textRecognizer = new TextRecognizer.Builder(getApplicationContext()).build();

            if (!textRecognizer.isOperational()) {
                Toast.makeText(getApplicationContext(), "Text Recognition not available", Toast.LENGTH_SHORT).show();
                return;
            }

            cameraSource = new CameraSource.Builder(getApplicationContext(), textRecognizer)
                    .setAutoFocusEnabled(true)
                    .setRequestedPreviewSize(1280, 1024)
                    .build();

            surfaceView.getHolder().addCallback(new SurfaceHolder.Callback() {
                @Override
                public void surfaceCreated(SurfaceHolder surfaceHolder) {
                    try {
                        if (checkCameraPermissions()) {
                            if (ActivityCompat.checkSelfPermission(MainActivity.this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                                return;
                            }
                            cameraSource.start(surfaceView.getHolder());
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }

                @Override
                public void surfaceChanged(SurfaceHolder surfaceHolder, int i, int i1, int i2) {
                }

                @Override
                public void surfaceDestroyed(SurfaceHolder surfaceHolder) {
                    cameraSource.stop();
                }
            });

            textRecognizer.setProcessor(new Detector.Processor<TextBlock>() {
                @Override
                public void release() {
                }

                @Override
                public void receiveDetections(Detector.Detections<TextBlock> detections) {
                    SparseArray<TextBlock> items = detections.getDetectedItems();
                    if (items.size() != 0) {
                        camera_Image_Text.post(new Runnable() {
                            @Override
                            public void run() {
                                StringBuilder stringBuilder = new StringBuilder();
                                for (int i = 0; i < items.size(); i++) {
                                    TextBlock item = items.valueAt(i);
                                    stringBuilder.append(item.getValue());
                                    stringBuilder.append("\n");
                                }
                                camera_Image_Text.setText(stringBuilder.toString());
                            }
                        });
                    }
                }
            });
        }

        private boolean checkCameraPermissions() {
            String[] permissions = {Manifest.permission.CAMERA};

            List<String> missingPermissions = new ArrayList<>();
            for (String permission : permissions) {
                if (ContextCompat.checkSelfPermission(getApplicationContext(), permission)
                        != PackageManager.PERMISSION_GRANTED) {
                    missingPermissions.add(permission);
                }
            }

            if (!missingPermissions.isEmpty()) {
                ActivityCompat.requestPermissions(this, missingPermissions.toArray(new String[0]), REQUEST_CAMERA_PERMISSIONS);
                return false;
            }

            return true;
        }

        private void startCameraPreview() {
            try {
                if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                    return;
                }
                cameraSource.start(surfaceView.getHolder());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        @Override
        public void onActivityResult(int requestCode, int resultCode, Intent data) {
            super.onActivityResult(requestCode, resultCode, data);

            if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
                Bitmap photo = (Bitmap) data.getExtras().get("data");
                ivImagePreview.setImageBitmap(photo);

                // Start text recognition
    //            speechRecognizer(photo);

                // Stop camera preview
                cameraSource.stop();

                // Call startCameraPreview() to re-open the camera preview
                startCameraPreview();
            }
            Toast.makeText(getApplicationContext(), "onActivityResult", Toast.LENGTH_SHORT).show();
        }




        private void captureImage() {
            // Stop speech recognition if it's active
            speechRecognizer.stopListening();

            cameraSource.takePicture(null, new CameraSource.PictureCallback() {
                @Override
                public void onPictureTaken(byte[] bytes) {
                    Bitmap bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);

                    // Display the captured image in the ImageView
                    ivImagePreview.setImageBitmap(bitmap);

                    TextRecognizer textRecognizer = new TextRecognizer.Builder(getApplicationContext()).build();
                    if (!textRecognizer.isOperational()) {
                        Toast.makeText(getApplicationContext(), "Text Recognition not available", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    Frame frame = new Frame.Builder().setBitmap(bitmap).build();
                    SparseArray<TextBlock> items = textRecognizer.detect(frame);
                    if (items.size() != 0) {
                        StringBuilder stringBuilder = new StringBuilder();
                        for (int i = 0; i < items.size(); i++) {
                            TextBlock item = items.valueAt(i);
                            stringBuilder.append(item.getValue());
                            stringBuilder.append("\n");
                        }
                        final String detectedText = stringBuilder.toString().trim();
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                camera_Image_Text.setText(detectedText);
                            }
                        });
                    }
                }
            });
            Toast.makeText(getApplicationContext(), "Capture Photo", Toast.LENGTH_SHORT).show();
            cameraSource.stop();
            // Show the "Open Camera" button
            btnReOpenCamera.setVisibility(View.VISIBLE);
        }


        @Override
        public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
            super.onRequestPermissionsResult(requestCode, permissions, grantResults);

            if (requestCode == REQUEST_CAMERA_PERMISSIONS) {
                boolean allPermissionsGranted = true;
                for (int result : grantResults) {
                    if (result != PackageManager.PERMISSION_GRANTED) {
                        allPermissionsGranted = false;
                        break;
                    }
                }

                if (allPermissionsGranted) {
                    try {
                        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                            return;
                        }
                        cameraSource.start(surfaceView.getHolder());
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                } else {
                    Toast.makeText(getApplicationContext(), "Camera permissions not granted", Toast.LENGTH_SHORT).show();
                }
            };

            if (requestCode == REQUEST_RECORD_AUDIO_PERMISSION) {
                permissionToRecordAccepted = grantResults[0] == PackageManager.PERMISSION_GRANTED;
            }
            if (!permissionToRecordAccepted ) finish();

        }

        // Release the resources used by the speech recognizer
        @Override
        protected void onDestroy() {
            super.onDestroy();
            if (speechRecognizer != null) {
                speechRecognizer.destroy();
                Toast.makeText(getApplicationContext(), "destroy", Toast.LENGTH_SHORT).show();
            }
        }
    }

