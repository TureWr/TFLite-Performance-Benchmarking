//package org.tensorflow.lite.examples.imageclassification.fragments;
//
//import android.content.res.AssetManager;
//import android.graphics.Bitmap;
//import android.graphics.BitmapFactory;
//import android.os.Bundle;
//import android.util.Log;
//import android.view.LayoutInflater;
//import android.view.View;
//import android.view.ViewGroup;
//import android.widget.TextView;
//
//import androidx.annotation.NonNull;
//import androidx.annotation.Nullable;
//import androidx.fragment.app.Fragment;
//
//import org.tensorflow.lite.examples.imageclassification.ImageClassifierHelper;
//import org.tensorflow.lite.examples.imageclassification.R;
//
//import java.io.IOException;
//import java.io.InputStream;
//import java.util.Locale;
//
//public class BenchmarkFragment extends Fragment {
//
//    private static final String TAG = "BenchmarkFragment";
//
//    private AssetManager assetManager;
//    private ImageClassifierHelper imageClassifierHelper;
//
//    private TextView benchmarkResultsTextView;
//
//    @Nullable
//    @Override
//    public View onCreateView(@NonNull LayoutInflater inflater,
//                             @Nullable ViewGroup container,
//                             @Nullable Bundle savedInstanceState) {
//        View view = inflater.inflate(R.layout.fragment_benchmark, container, false);
//        benchmarkResultsTextView = view.findViewById(R.id.benchmarkResultsTextView);
//        assetManager = requireContext().getAssets();
//        imageClassifierHelper = ImageClassifierHelper.create(requireContext(), null);
//
//        view.findViewById(R.id.benchmarkButton).setOnClickListener(v -> benchmarkClassification());
//
//        return view;
//    }
//
//    private void benchmarkClassification() {
//        Log.d(TAG, "Benchmarking started");
//        Log.d("myInfo", "CLASSIFYING STARTED");
//        classifyAssetsImages();
//    }
//
//    private Bitmap loadBitmapFromAsset(String filePath) {
//        InputStream inputStream = null;
//        Bitmap bitmap = null;
//        try {
//            inputStream = assetManager.open(filePath);
//            bitmap = BitmapFactory.decodeStream(inputStream);
//        } catch (IOException e) {
//            e.printStackTrace();
//        } finally {
//            if (inputStream != null) {
//                try {
//                    inputStream.close();
//                } catch (IOException e) {
//                    e.printStackTrace();
//                }
//            }
//        }
//        return bitmap;
//    }
//
//    public void classifyAssetsImages() {
//        String imagesFolder = "images"; // Path within assets folder
//
//        try {
//            String[] imageList = assetManager.list(imagesFolder);
//            String[] notWantedImages = new String[]{
//                    "android-logo-mask.png",
//                    "android-logo-shine.png",
//                    "clock_font.png",
//                    "progress_font.png"
//            };
//
//            boolean found;
//            int amountImages = 0;
//            long totalTime = 0;
//
//            StringBuilder results = new StringBuilder();
//
//            if (imageList != null) {
//                for (String imageName : imageList) {
//                    found = false;
//
//                    for (String item : notWantedImages) {
//                        if (item.equals(imageName)) {
//                            found = true;
//                            break;
//                        }
//                    }
//
//                    if (!found) {
//                        String imagePath = imagesFolder + "/" + imageName;
//                        Bitmap bitmap = loadBitmapFromAsset(imagePath);
//                        if (bitmap != null) {
//                            int rotation = 0;
//                            long startTime = System.currentTimeMillis();
//                            imageClassifierHelper.classify(bitmap, rotation);
//                            long endTime = System.currentTimeMillis();
//                            long duration = endTime - startTime;
//                            totalTime += duration;
//                            amountImages++;
//                        }
//                    }
//                }
//            }
//
//            benchmarkResultsTextView.setText("TIME TAKEN: " + totalTime + "ms" + "   AMOUNT IMAGES: " + amountImages);
//
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//    }
//}
