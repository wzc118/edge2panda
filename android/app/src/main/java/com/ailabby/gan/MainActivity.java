package com.ailabby.gan;

import android.Manifest;
import android.content.pm.PackageManager;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;
import android.content.Intent;
import android.net.Uri;




import org.tensorflow.contrib.android.TensorFlowInferenceInterface;

import java.util.UUID;
import java.io.File;
import java.io.FileNotFoundException;


public class MainActivity extends AppCompatActivity implements Runnable {

    private static final String MODEL_FILE2 = "file:///android_asset/frozen_model_android_quantized.pb";


    private static final String INPUT_NODE2 = "image_tensor";
    private static final String OUTPUT_NODE2 = "generator/deprocess/div";
    private static final String IMAGE_NAME = "test.png";


    private static final int WANTED_WIDTH = 256;
    private static final int WANTED_HEIGHT = 256;
    private static final int SAVED_WIDTH = 256;
    private static final int SAVED_HEIGHT = 256;
    private static final int IMAGE_MEAN = 128;
    private static final float IMAGE_STD = 128;


    private boolean mCanDraw = true;
    private QuickDrawView mDrawView;
    private Button mButtonPix2Pix;
    private Button mButtonClear;
    private Button mButtonSave;
    private Bitmap mGeneratedBitmap;

    private TensorFlowInferenceInterface mInferenceInterface;

    public boolean canDraw(){
        return mCanDraw;
    }

    private static final int REQUEST_EXTERNAL_STORAGE = 1;
    private static String[] PERMISSIONS_STORAGE = {
            "android.permission.READ_EXTERNAL_STORAGE",
            "android.permission.WRITE_EXTERNAL_STORAGE" };

    public static void verifyStoragePermissions(AppCompatActivity activity) {

        try {
            //检测是否有写的权限
            int permission = ActivityCompat.checkSelfPermission(activity,
                    "android.permission.WRITE_EXTERNAL_STORAGE");
            if (permission != PackageManager.PERMISSION_GRANTED) {
                // 没有写的权限，去申请写的权限，会弹出对话框
                ActivityCompat.requestPermissions(activity, PERMISSIONS_STORAGE,REQUEST_EXTERNAL_STORAGE);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mDrawView = findViewById(R.id.drawview);
        mButtonClear = findViewById(R.id.clearbutton);
        mButtonPix2Pix = findViewById(R.id.pix2pixbutton);
        mButtonSave = findViewById(R.id.savebutton);



        mButtonClear.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                mCanDraw = true;
                mButtonClear.setText("CLear");
                mDrawView.clearRedraw();
            }
        });

        mButtonPix2Pix.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                /*Thread thread = new Thread(MainActivity.this);
                thread.start();*/
                runPix2PixBlurryModel(mInferenceInterface);
                mCanDraw = false;
            }
        });

        mButtonSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mCanDraw = false;
                try{
                    /**AssetManager am = getAssets();
                    Bitmap bitmap = BitmapFactory.decodeStream(am.open(IMAGE_NAME));**/
                    Bitmap bitmap = mDrawView.getBitmap();
                    /**String imgSaved = MediaStore.Images.Media.insertImage(MainActivity.this.getContentResolver(),bitmap, UUID.randomUUID().toString()+".png","drawing");**/
                    syncAlbum(bitmap);
                    /**if(imgSaved!= null){
                        Toast savedToast = Toast.makeText(getApplicationContext(),
                                "Drawing saved to Gallery!",Toast.LENGTH_SHORT);
                        savedToast.show();
                    }
                    else{
                        Toast unsavedToast = Toast.makeText(getApplicationContext(),
                                "Oops! Image could not be saved.",Toast.LENGTH_SHORT);
                        unsavedToast.show();
                    }**/

                }
                catch (Exception e){
                    e.printStackTrace();
                }

            }
        });

        Thread thread = new Thread(MainActivity.this);
        thread.start();
    }

    private void syncAlbum(Bitmap bitmap){
        verifyStoragePermissions(this);
        String savepath = "";
        String dir = "";
        /**if(new File("/sdcard/maskstyle").exists()){
            dir = "/sdcard/maskstyle";
        }else{
            File maskdir = new File("/sdcard/maskstyle");
            try{
                maskdir.mkdir();
                dir = "/sdcard/maskstyle";
            }catch (Exception e){
                e.printStackTrace();
            }
        }**/
        File sdCard = Environment.getExternalStorageDirectory();
        File maskdir = new File(sdCard.getAbsolutePath()+"/maskstyle");
        dir = maskdir.getAbsolutePath();
        if (!maskdir.exists()){
            maskdir.mkdir();
        }
        savepath = dir + File.separator + System.currentTimeMillis() + ".png";
        File file = BitmapUtils.saveBitmap(bitmap,savepath);
        try{
            MediaStore.Images.Media.insertImage(MainActivity.this.getContentResolver(),savepath,null,null);
            this.sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE,Uri.fromFile(file)));
            Toast savedToast = Toast.makeText(getApplicationContext(),
                    "Drawing saved to Gallery!",Toast.LENGTH_SHORT);
            savedToast.show();

        }catch(Exception e){
            e.printStackTrace();
        }

    }


    @Override
    public void run() {
        AssetManager assetManager = getAssets();
        mInferenceInterface = new TensorFlowInferenceInterface(assetManager, MODEL_FILE2);
    }

    void runPix2PixBlurryModel(TensorFlowInferenceInterface mInferenceInterface) {
        int[] intValues = new int[WANTED_WIDTH * WANTED_HEIGHT];
        float[] floatValues = new float[WANTED_WIDTH * WANTED_HEIGHT * 3];
        float[] outputValues = new float[WANTED_WIDTH * WANTED_HEIGHT * 3];

        try {
            /**mDrawView.setDrawingCacheEnabled(true);**/
            Bitmap bitmap = mDrawView.getBitmap();
            /**AssetManager am = getAssets();
            Bitmap bitmap = BitmapFactory.decodeStream(am.open(IMAGE_NAME));**/
            Bitmap scaledBitmap = Bitmap.createScaledBitmap(bitmap, WANTED_WIDTH, WANTED_HEIGHT, true);
            scaledBitmap.getPixels(intValues, 0, scaledBitmap.getWidth(), 0, 0, scaledBitmap.getWidth(), scaledBitmap.getHeight());
            Log.d("****", ""+scaledBitmap.getWidth() +","+scaledBitmap.getHeight());
            for (int i = 0; i < intValues.length; ++i) {
                final int val = intValues[i];

                floatValues[i * 3 + 0] = (((val >> 16) & 0xFF) - IMAGE_MEAN) / IMAGE_STD;
                floatValues[i * 3 + 1] = (((val >> 8) & 0xFF) - IMAGE_MEAN) / IMAGE_STD;
                floatValues[i * 3 + 2] = ((val & 0xFF) - IMAGE_MEAN) / IMAGE_STD;
            }

            /*AssetManager assetManager = getAssets();
            mInferenceInterface = new TensorFlowInferenceInterface(assetManager, MODEL_FILE2);*/


            mInferenceInterface.feed(INPUT_NODE2, floatValues, 1, WANTED_HEIGHT, WANTED_WIDTH, 3);
            mInferenceInterface.run(new String[] {OUTPUT_NODE2}, false);
            mInferenceInterface.fetch(OUTPUT_NODE2, outputValues);

            for (int i = 0; i < intValues.length; ++i) {
                intValues[i] = 0xFF000000
                        | (((int) (outputValues[i * 3] * 255)) << 16)
                        | (((int) (outputValues[i * 3 + 1] * 255)) << 8)
                        | ((int) (outputValues[i * 3 + 2] * 255));
            }


            Bitmap outputBitmap = scaledBitmap.copy( scaledBitmap.getConfig() , true);
            outputBitmap.setPixels(intValues, 0, outputBitmap.getWidth(), 0, 0, outputBitmap.getWidth(), outputBitmap.getHeight());
            mGeneratedBitmap = Bitmap.createScaledBitmap(outputBitmap, bitmap.getWidth(), bitmap.getHeight(), true);
            /*mGeneratedBitmap = Bitmap.createScaledBitmap(outputBitmap, SAVED_WIDTH, SAVED_HEIGHT, true);*/
            Log.d("generate", ""+mGeneratedBitmap.getWidth() +","+mGeneratedBitmap.getHeight());
            mDrawView.refresh(mGeneratedBitmap);


        }
        catch (Exception e) {
            e.printStackTrace();
        }

        /*runOnUiThread(
                new Runnable() {
                    @Override
                    public void run() {
                        mDrawView.refresh(mGeneratedBitmap);
                        Log.d("showimg", "success");
                    }
                });*/


    }
}
