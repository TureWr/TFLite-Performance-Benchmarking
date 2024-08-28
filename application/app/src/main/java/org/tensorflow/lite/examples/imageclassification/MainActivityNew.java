package org.tensorflow.lite.examples.imageclassification;

import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.navigation.ui.AppBarConfiguration;

import org.tensorflow.lite.examples.imageclassification.databinding.ActivityMainBinding;

/**
 * Main Activity for the app.
 * Sets up navigation and handles basic activity lifecycle events.
 */
public class MainActivityNew extends AppCompatActivity {

    private static final String TAG = "MainActivity";
    private ActivityMainBinding binding;
    private AppBarConfiguration appBarConfiguration;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Initialize binding
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Set up Navigation
    }

}
