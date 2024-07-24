package com.example.appgroovy;

import android.graphics.Bitmap;
import org.pytorch.Tensor;
import org.pytorch.Module;
import org.pytorch.IValue;
import org.pytorch.torchvision.TensorImageUtils;
import org.pytorch.Device;

public class Classifier {

    Module model;
    float[] mean = {0.485f, 0.456f, 0.406f};
    float[] std = {0.229f, 0.224f, 0.225f};
    boolean useGpu;

    public Classifier(String modelPath, boolean useGpu) {
        this.useGpu = useGpu;
        // Load model
        model = Module.load(modelPath);
    }

    public void setUseGpu(boolean useGpu) {
        this.useGpu = useGpu;
    }

    public void setMeanAndStd(float[] mean, float[] std) {
        this.mean = mean;
        this.std = std;
    }

    public Tensor preprocess(Bitmap bitmap, int size) {
        bitmap = Bitmap.createScaledBitmap(bitmap, size, size, false);
        return TensorImageUtils.bitmapToFloat32Tensor(bitmap, this.mean, this.std);
    }

    public int argMax(float[] inputs) {
        int maxIndex = -1;
        float maxvalue = 0.0f;
        for (int i = 0; i < inputs.length; i++) {
            if (inputs[i] > maxvalue) {
                maxIndex = i;
                maxvalue = inputs[i];
            }
        }
        return maxIndex;
    }

    public String predict(Bitmap bitmap) {
        Tensor tensor = preprocess(bitmap, 224);
        IValue inputs = IValue.from(tensor);

        Tensor outputs;
//        if (useGpu) {
//            outputs = model.forward(inputs).toTensor(Device.VULKAN);
//        } else {
//            outputs = model.forward(inputs).toTensor(Device.CPU);
//        }
        outputs = model.forward(inputs).toTensor();


        float[] scores = outputs.getDataAsFloatArray();
        int classIndex = argMax(scores);
        return Constants.IMAGENET_CLASSES[classIndex];
    }
}
