package com.ailabby.gan;

import java.io.IOException;
import android.graphics.Bitmap;
import org.tensorflow.contrib.android.TensorFlowInferenceInterface;
import android.content.res.AssetManager;
import android.support.v4.os.TraceCompat;

public class TensorFlowTransformer {

    private TensorFlowInferenceInterface inferenceInterface;
    private boolean runStats = false;
    private String INPUT_NODE2;
    private String OUTPUT_NODE2;
    private int WANTED_WIDTH;
    private int[] intValues = new int[WANTED_WIDTH * WANTED_WIDTH];
    private float[] outputValues = new float[WANTED_WIDTH * WANTED_WIDTH * 3];
    private TensorFlowTransformer(){}

    public static TensorFlowTransformer create(
            AssetManager assetManager,
            String modelFilename,
            int WANTED_WIDTH,
            String INPUT_NODE2,
            String OUTPUT_NODE2)
            throws IOException{
        TensorFlowTransformer c = new TensorFlowTransformer();
        c.INPUT_NODE2 = INPUT_NODE2;
        c.OUTPUT_NODE2 = OUTPUT_NODE2;
        c.inferenceInterface = new TensorFlowInferenceInterface(assetManager,modelFilename);
        c.WANTED_WIDTH = WANTED_WIDTH;
        c.outputValues = new float[WANTED_WIDTH * WANTED_WIDTH * 3];
        c.intValues = new int[WANTED_WIDTH * WANTED_WIDTH];
        return c;
    }


    public int[] transformImage(float[] floatValues){
        TraceCompat.beginSection("feed");
        inferenceInterface.feed(INPUT_NODE2,floatValues,1,WANTED_WIDTH,WANTED_WIDTH,3);
        TraceCompat.endSection();

        TraceCompat.beginSection("run");
        inferenceInterface.run(new String[] {OUTPUT_NODE2},runStats);
        TraceCompat.endSection();

        TraceCompat.beginSection("fetch");
        inferenceInterface.fetch(OUTPUT_NODE2, outputValues);
        TraceCompat.endSection();

        for (int i = 0; i < intValues.length; ++i) {
            intValues[i] = 0xFF000000
                    | (((int) (outputValues[i * 3] * 255)) << 16)
                    | (((int) (outputValues[i * 3 + 1] * 255)) << 8)
                    | ((int) (outputValues[i * 3 + 2] * 255));
        }

        TraceCompat.endSection();
        return intValues;
    }

    public  String getStstString(){
        return inferenceInterface.getStatString();
    }

    public void close(){
        inferenceInterface.close();
    }

}
