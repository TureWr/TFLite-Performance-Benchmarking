/*
 * Copyright 2022 The TensorFlow Authors. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *             http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.tensorflow.lite.examples.imageclassification.fragments;

import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.camera.core.AspectRatio;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.ImageAnalysis;
import androidx.camera.core.ImageProxy;
import androidx.camera.core.Preview;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.google.common.util.concurrent.ListenableFuture;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.tensorflow.lite.Interpreter;
import org.tensorflow.lite.examples.imageclassification.ImageClassifierHelper;
import org.tensorflow.lite.examples.imageclassification.databinding.FragmentCameraBinding;
import org.tensorflow.lite.task.vision.classifier.Classifications;

// TURES IMPORTS
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.io.FileInputStream;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.io.IOException;
import org.tensorflow.lite.support.tensorbuffer.TensorBuffer;

import android.content.res.AssetFileDescriptor;


/** Fragment for displaying and controlling the device camera and other UI */
public class CameraFragment extends Fragment
        implements ImageClassifierHelper.ClassifierListener {
    private static final String TAG = "Image Classifier";

    private FragmentCameraBinding fragmentCameraBinding;
    private ImageClassifierHelper imageClassifierHelper;
    private Bitmap bitmapBuffer;
    //private ClassificationResultAdapter classificationResultsAdapter;
    private ImageAnalysis imageAnalyzer;
    private ProcessCameraProvider cameraProvider;
    private final Object task = new Object();
    AssetManager assetManager;

    private Interpreter tfliteInterpreter;


    /**
     * Blocking camera operations are performed using this executor
     */
    private ExecutorService cameraExecutor;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        fragmentCameraBinding = FragmentCameraBinding
                .inflate(inflater, container, false);

        assetManager = requireContext().getAssets();

        // Load the TFLite model for anomaly detection
        try {
            tfliteInterpreter = new Interpreter(loadModelFile(assetManager));

        } catch (IOException e) {
            e.printStackTrace();
        }

        return fragmentCameraBinding.getRoot();
    }

    // Load the TFLite model file
    private MappedByteBuffer loadModelFile(AssetManager assetManager) throws IOException {
        AssetFileDescriptor fileDescriptor = assetManager.openFd("tcnae40_.tflite");
        FileInputStream inputStream = new FileInputStream(fileDescriptor.getFileDescriptor());
        FileChannel fileChannel = inputStream.getChannel();
        long startOffset = fileDescriptor.getStartOffset();
        long declaredLength = fileDescriptor.getDeclaredLength();
        return fileChannel.map(FileChannel.MapMode.READ_ONLY, startOffset, declaredLength);
    }

    // Method to load .npy files from assets and convert to float array
    public float[] loadNumpyFile(AssetManager assetManager, String fileName) throws IOException {
        InputStream is = assetManager.open(fileName);
        byte[] buffer = new byte[is.available()];
        is.read(buffer);
        is.close();

        // .npy files have a 128-byte header. We skip it.
        ByteBuffer byteBuffer = ByteBuffer.wrap(buffer, 128, buffer.length - 128).order(ByteOrder.nativeOrder());

        // Assuming the data is of type float32
        FloatBuffer floatBuffer = byteBuffer.asFloatBuffer();
        float[] floatArray = new float[floatBuffer.remaining()];
        floatBuffer.get(floatArray);

        return floatArray;
    }

    // Update the benchmarkAnomalyDetection method to use the loaded TFLite model
    private void benchmarkAnomalyDetection() throws IOException {
        //TODO ACTUALLY IMPLEMENT THIS, IT CRASHES AT THE MOMENT
        double threshold = 0.5;
        Log.d(TAG, "ENTERED ANOMALY DETECTION BENCHMARK");


        // Example input data for the anomaly detection model
        //float[] inputData = new float[]{/* Fill with your actual data */};
        float[] inputData = loadNumpyFile(assetManager, "timeSeriesData/mg3.npy");

        // Output array (assuming model output is a single float value)
        //float[][] outputData = new float[1][1];
        float[] outputData = loadNumpyFile(assetManager, "timeSeriesData/anomaly_labels3.npy");


        // Run the model
        if (tfliteInterpreter != null) {
            tfliteInterpreter.run(inputData, outputData);

            // Handle the output for anomaly detection
//            float prediction = outputData[0][0];
//            Log.d("AnomalyDetection", "Prediction: " + prediction);
//
//            // You can also update the UI or other components with the result
//            // For example:
//            if (prediction > threshold) { // Define your threshold value
//                Log.d("AnomalyDetection", "Anomaly Detected");
//            } else {
//                Log.d("AnomalyDetection", "Normal");
//            }

            fragmentCameraBinding.bottomSheetLayout.anomalyResults.setText("DETECTION DONE!");

            //fragmentCameraBinding.bottomSheetLayout.benchmarkResults.setText("TIME TAKEN: " + totalTime + "ms" + "   AMOUNT IMAGES: " + amountImages);

        } else {
            Log.e("AnomalyDetection", "TFLite Interpreter is not initialized.");
        }


    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();

        // Shut down our background executor
        cameraExecutor.shutdown();
        synchronized (task) {
            imageClassifierHelper.clearImageClassifier();
        }

        // Close the TFLite interpreter
        if (tfliteInterpreter != null) {
            tfliteInterpreter.close();
        }
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        cameraExecutor = Executors.newSingleThreadExecutor();
        imageClassifierHelper = ImageClassifierHelper.create(requireContext()
                , this);

        initBottomSheetControls();
    }


    private void initBottomSheetControls() {

        // When clicked, change the underlying hardware used for inference.
        // Current options are CPU,GPU, and NNAPI
        fragmentCameraBinding.bottomSheetLayout.spinnerDelegate
                .setSelection(0, false);
        fragmentCameraBinding.bottomSheetLayout.spinnerDelegate
                .setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                    @Override
                    public void onItemSelected(AdapterView<?> adapterView,
                                               View view,
                                               int position,
                                               long id) {
                        imageClassifierHelper.setCurrentDelegate(position);
                        updateControlsUi();
                    }

                    @Override
                    public void onNothingSelected(AdapterView<?> adapterView) {
                        // no-op
                    }
                });

        // When clicked, change the underlying model used for object
        // classification
        fragmentCameraBinding.bottomSheetLayout.spinnerModel
                .setSelection(0, false);
        fragmentCameraBinding.bottomSheetLayout.spinnerModel
                .setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                    @Override
                    public void onItemSelected(AdapterView<?> adapterView,
                                               View view,
                                               int position,
                                               long id) {
                        imageClassifierHelper.setCurrentModel(position);
                        updateControlsUi();
                    }

                    @Override
                    public void onNothingSelected(AdapterView<?> adapterView) {
                        // no-op
                    }
                });

        fragmentCameraBinding.bottomSheetLayout.benchmarkButton
                .setOnClickListener(view -> {
                    benchmarkClassification();
                });

        // Set up the OnClickListener for the new anomaly detection benchmark button
        fragmentCameraBinding.bottomSheetLayout.anomalyBenchmarkButton
                .setOnClickListener(view -> {
                    boolean activated = false;
                    if (activated) {
                        try {
                            benchmarkAnomalyDetection();
                        }
                        catch (IOException e) {
                            throw new RuntimeException(e);
                        }
                    } else {
                        Log.d(TAG, "CLICKED ANOMALY DETECTION BUTTON");

                    }

                });
    }


    // Update the values displayed in the bottom sheet. Reset classifier.
    private void updateControlsUi() {

        // Needs to be cleared instead of reinitialized because the GPU
        // delegate needs to be initialized on the thread using it when
        // applicable
        synchronized (task) {
            imageClassifierHelper.clearImageClassifier();
        }
    }

    // Initialize CameraX, and prepare to bind the camera use cases
    private void setUpCamera() {
        ListenableFuture<ProcessCameraProvider> cameraProviderFuture =
                ProcessCameraProvider.getInstance(requireContext());
        cameraProviderFuture.addListener(() -> {
            try {
                cameraProvider = cameraProviderFuture.get();

                // Build and bind the camera use cases
                bindCameraUseCases();
            } catch (ExecutionException | InterruptedException e) {
                e.printStackTrace();
            }
        }, ContextCompat.getMainExecutor(requireContext()));
    }

    // Declare and bind preview, capture and analysis use cases
    private void bindCameraUseCases() {
        // CameraSelector - makes assumption that we're only using the back
        // camera
        CameraSelector.Builder cameraSelectorBuilder = new CameraSelector.Builder();
        CameraSelector cameraSelector = cameraSelectorBuilder
                .requireLensFacing(CameraSelector.LENS_FACING_BACK).build();

        // Preview. Only using the 4:3 ratio because this is the closest to
        // our model
        Preview preview = new Preview.Builder()
                .setTargetAspectRatio(AspectRatio.RATIO_4_3)
                .setTargetRotation(
                        fragmentCameraBinding.viewFinder
                                .getDisplay().getRotation()
                )
                .build();

        // ImageAnalysis. Using RGBA 8888 to match how our models work
        imageAnalyzer = new ImageAnalysis.Builder()
                .setTargetAspectRatio(AspectRatio.RATIO_4_3)
                .setTargetRotation(fragmentCameraBinding.viewFinder.getDisplay().getRotation())
                .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                .setOutputImageFormat(ImageAnalysis.OUTPUT_IMAGE_FORMAT_RGBA_8888)
                .build();

        // The analyzer can then be assigned to the instance
        imageAnalyzer.setAnalyzer(cameraExecutor, image -> {
            if (bitmapBuffer == null) {
                bitmapBuffer = Bitmap.createBitmap(
                        image.getWidth(),
                        image.getHeight(),
                        Bitmap.Config.ARGB_8888);
            }

            long startTime = System.currentTimeMillis();

            classifyImage(image);
            long endTime = System.currentTimeMillis();
            long duration = endTime - startTime;


        });

        // Must unbind the use-cases before rebinding them
        cameraProvider.unbindAll();

        try {
            // A variable number of use-cases can be passed here -
            // camera provides access to CameraControl & CameraInfo
            cameraProvider.bindToLifecycle(
                    this,
                    cameraSelector,
                    preview,
                    imageAnalyzer
            );

            // Attach the viewfinder's surface provider to preview use case
            preview.setSurfaceProvider(
                    fragmentCameraBinding.viewFinder.getSurfaceProvider()
            );
        } catch (Exception exc) {
            Log.e(TAG, "Use case binding failed", exc);
        }
    }

    private void benchmarkClassification(){
        Log.d(TAG, "Image Classification button pressed");
        classifyAssetsImages();
    }

    private Bitmap loadBitmapFromAsset(String filePath) {
//        AssetManager assetManager = getAssets(); // Assuming this is in an Activity or Context
        InputStream inputStream = null;
        Bitmap bitmap = null;
        try {
            inputStream = assetManager.open(filePath);
            bitmap = BitmapFactory.decodeStream(inputStream);
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (inputStream != null) {
                try {
                    inputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return bitmap;
    }


    // Method that reads all images from assets/images and predicts them for benchmarking. Keeps track of time taken and displays it when done
    public void classifyAssetsImages() {
        String imagesFolder = "images"; // Path within assets folder

        try {
            String[] imageList = assetManager.list(imagesFolder);
            String[] notWantedImages = new String[]{
                    "android-logo-mask.png",
                    "android-logo-shine.png",
                    "clock_font.png",
                    "progress_font.png"
            };

            boolean found;
            int amountImages = 0;
            long totalTime = 0;

            StringBuilder results = new StringBuilder();

            if (imageList != null) {

                for (String imageName : imageList) {
                    found = false;

                    for (String item : notWantedImages) {
                        if (item.equals(imageName)) {
                            found = true;
                            break;
                        }
                    }

                    if (!found) {
                        String imagePath = imagesFolder + "/" + imageName;
                        Bitmap bitmap = loadBitmapFromAsset(imagePath);
                        if (bitmap != null) {
                            int rotation = 0;
                            synchronized (task) {
                                amountImages++;
                                long startTime = System.currentTimeMillis();
                                imageClassifierHelper.classify(bitmap, rotation);
                                long endTime = System.currentTimeMillis();
                                long duration = endTime - startTime;
                                totalTime += duration;
                            }
                        }
                    }
                }
            }
            fragmentCameraBinding.bottomSheetLayout.benchmarkResults.setText("TIME TAKEN: " + totalTime + "ms" + "   AMOUNT IMAGES: " + amountImages);


        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    private void classifyImage(@NonNull ImageProxy image) {
        // Copy out RGB bits to the shared bitmap buffer
        bitmapBuffer.copyPixelsFromBuffer(image.getPlanes()[0].getBuffer());

        int imageRotation = image.getImageInfo().getRotationDegrees();
        image.close();
        synchronized (task) {
            // Pass Bitmap and rotation to the image classifier helper for
            // processing and classification
            imageClassifierHelper.classify(bitmapBuffer, imageRotation);
        }
    }

    @Override
    public void onError(String error) {
        requireActivity().runOnUiThread(() -> {
            Toast.makeText(requireContext(), error, Toast.LENGTH_SHORT).show();
            //classificationResultsAdapter.updateResults(new ArrayList<>());
        });
    }

    @Override
    public void onResults(List<Classifications> results, long inferenceTime) {
//        requireActivity().runOnUiThread(() -> {
//            classificationResultsAdapter.updateResults(results.get(0).getCategories());
//            fragmentCameraBinding.bottomSheetLayout.inferenceTimeVal
//                    .setText(String.format(Locale.US, "%d ms", inferenceTime));
//        });
    }
}

