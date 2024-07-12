package com.example.appgroovy;

import android.content.Intent;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.provider.MediaStore;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import java.io.IOException;
import java.io.InputStream;

public class MainActivity extends AppCompatActivity {

    int cameraRequestCode = 001;
    Classifier classifier;
    AssetManager assetManager;
    TextView classificationResults;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        assetManager = getAssets();
        classifier = new Classifier(Utils.assetFilePath(this, "mobilenet-v2.pt"));

        Button capture = findViewById(R.id.capture);
        classificationResults = findViewById(R.id.classificationResults);

        capture.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                classifyImagesInAssets();
            }
        });
    }

    private void classifyImagesInAssets() {
        Log.d("Image Classification", "starting classification");

        try {
            String[] imageList = assetManager.list("images");
            String[] notWantedImages = new String[]{
                    "android-logo-mask.png",
                    "android-logo-shine.png",
                    "clock_font.png",
                    "progress_font.png"
            };

            boolean found;
            int imageCounter = 0;
            Long totalTime = 0L;

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
                        imageCounter++;
                        InputStream is = assetManager.open("images/" + imageName);
                        Bitmap bitmap = BitmapFactory.decodeStream(is);
                        is.close();

                        long startTime = System.currentTimeMillis();
                        String pred = classifier.predict(bitmap);
                        long endTime = System.currentTimeMillis();
                        long duration = endTime - startTime;
                        totalTime += duration;

//                        results.append("Image: ").append(imageName)
//                                .append(", Prediction: ").append(pred)
//                                .append(", Time: ").append(duration).append("ms\n");
                    }
                }

                results.append("Amount of pictures classified: ").append(imageCounter).append("\n");

                results.append("Total time taken: ").append(totalTime).append("ms\n");

                long averageTime = totalTime / imageCounter;
                results.append("Average time taken: ").append(averageTime).append("ms\n");

                classificationResults.setText(results.toString());
            } else {
                classificationResults.setText("No images in assets");
            }
        } catch (IOException e) {
            Log.d("Image Classification", "something", e);
        }

        Log.d("Image Classification", "Done with classification");
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == cameraRequestCode && resultCode == RESULT_OK) {
            Intent resultView = new Intent(this, Result.class);
            resultView.putExtra("imagedata", data.getExtras());

            Bitmap imageBitmap = (Bitmap) data.getExtras().get("data");
            String pred = classifier.predict(imageBitmap);
            resultView.putExtra("pred", pred);

            startActivity(resultView);
        }
    }
}
