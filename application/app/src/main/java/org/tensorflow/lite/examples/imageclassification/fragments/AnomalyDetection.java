package org.tensorflow.lite.examples.imageclassification.fragments;

import org.tensorflow.lite.Interpreter;
import android.content.res.AssetFileDescriptor;
import android.content.res.AssetManager;

import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.io.FileInputStream;
import java.io.IOException;

public class AnomalyDetection {

    private Interpreter tflite;

    public AnomalyDetection(AssetManager assetManager, String modelPath) throws IOException {
        tflite = new Interpreter(loadModelFile(assetManager, modelPath));
    }

    private MappedByteBuffer loadModelFile(AssetManager assetManager, String modelPath) throws IOException {
        AssetFileDescriptor fileDescriptor = assetManager.openFd(modelPath);
        FileInputStream inputStream = new FileInputStream(fileDescriptor.getFileDescriptor());
        FileChannel fileChannel = inputStream.getChannel();
        long startOffset = fileDescriptor.getStartOffset();
        long declaredLength = fileDescriptor.getDeclaredLength();
        return fileChannel.map(FileChannel.MapMode.READ_ONLY, startOffset, declaredLength);
    }

    public float[] predict(float[] inputData) {
        float[][] outputData = new float[1][1]; // Assuming your model outputs a single value.
        tflite.run(inputData, outputData);
        return outputData[0];
    }

    public void close() {
        tflite.close();
    }
}
