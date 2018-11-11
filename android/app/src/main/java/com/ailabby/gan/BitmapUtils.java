package com.ailabby.gan;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;
import android.util.Log;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * Created by Administrator on 2016/6/21.
 */
public class BitmapUtils {
    public static Bitmap getBitmapFromFile(File dst, int width, int height) {
        if (null != dst && dst.exists()) {
            BitmapFactory.Options opts = null;
            if (width > 0 && height > 0) {
                opts = new BitmapFactory.Options();           //设置inJustDecodeBounds为true后，decodeFile并不分配空间，此时计算原始图片的长度和宽度
                opts.inJustDecodeBounds = true;
                BitmapFactory.decodeFile(dst.getPath(), opts);
                // 计算图片缩放比例
                final int minSideLength = Math.min(width, height);
                opts.inSampleSize = computeSampleSize(opts, minSideLength,
                        width * height);            //这里一定要将其设置回false，因为之前我们将其设置成了true
                opts.inJustDecodeBounds = false;
                opts.inInputShareable = true;
                opts.inPurgeable = true;
            }
            try {
                return BitmapFactory.decodeFile(dst.getPath(), opts);
            } catch (OutOfMemoryError e) {
                e.printStackTrace();
            }
        }
        return null;
    }
    public static  int computeSampleSize(BitmapFactory.Options options,
                                         int minSideLength, int maxNumOfPixels) {
        int initialSize = computeInitialSampleSize(options, minSideLength,
                maxNumOfPixels);

        int roundedSize;
        if (initialSize <= 8) {
            roundedSize = 1;
            while (roundedSize < initialSize) {
                roundedSize <<= 1;
            }
        } else {
            roundedSize = (initialSize + 7) / 8 * 8;
        }

        return roundedSize;
    }

    private  static int computeInitialSampleSize(BitmapFactory.Options options,
                                                 int minSideLength, int maxNumOfPixels) {
        double w = options.outWidth;
        double h = options.outHeight;

        int lowerBound = (maxNumOfPixels == -1) ? 1 : (int) Math.ceil(Math
                .sqrt(w * h / maxNumOfPixels));
        int upperBound = (minSideLength == -1) ? 128 : (int) Math.min(Math
                .floor(w / minSideLength), Math.floor(h / minSideLength));

        if (upperBound < lowerBound) {
            // return the larger one when there is no overlapping zone.
            return lowerBound;
        }

        if ((maxNumOfPixels == -1) && (minSideLength == -1)) {
            return 1;
        } else if (minSideLength == -1) {
            return lowerBound;
        } else {
            return upperBound;
        }
    }

    public static Bitmap toRoundCorner(Bitmap bitmap, int pixels) {

        Bitmap output = Bitmap.createBitmap(bitmap.getWidth(), bitmap.getHeight(), Bitmap.Config.ARGB_8888);

        Canvas canvas = new Canvas(output);

        final int color = 0xff424242;

        final Paint paint = new Paint();

        final Rect rect = new Rect(0, 0, bitmap.getWidth(), bitmap.getHeight());

        final RectF rectF = new RectF(rect);

        final float roundPx = pixels;

        paint.setAntiAlias(true);

        canvas.drawARGB(0, 0, 0, 0);

        paint.setColor(color);

        canvas.drawRoundRect(rectF, roundPx, roundPx, paint);

        paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));

        canvas.drawBitmap(bitmap, rect, rect, paint);


        return output;

    }


    //把图片压缩到1280*720
    public static Bitmap getImageForUpload(String srcPath) {
        BitmapFactory.Options newOpts = new BitmapFactory.Options();
        //开始读入图片，此时把options.inJustDecodeBounds 设回true了
        newOpts.inJustDecodeBounds = true;
        Bitmap bitmap = BitmapFactory.decodeFile(srcPath,newOpts);//此时返回bm为空

        newOpts.inJustDecodeBounds = false;
        int w = newOpts.outWidth;
        int h = newOpts.outHeight;
        //现在主流手机比较多是800*480分辨率，所以高和宽我们设置为
        float hh = 1280f;//这里设置高度为800f
        float ww = 720f;//这里设置宽度为480f
        //缩放比。由于是固定比例缩放，只用高或者宽其中一个数据进行计算即可
        int be = 1;//be=1表示不缩放
        if (w > h && w > ww) {//如果宽度大的话根据宽度固定大小缩放
            be = (int) (newOpts.outWidth / ww);
        } else if (w < h && h > hh) {//如果高度高的话根据宽度固定大小缩放
            be = (int) (newOpts.outHeight / hh);
        }
        if (be <= 0)
            be = 1;
        newOpts.inSampleSize = be;//设置缩放比例

        //重新读入图片，注意此时已经把options.inJustDecodeBounds 设回false了
        bitmap = BitmapFactory.decodeFile(srcPath, newOpts);

        int width  = w;
        int height = h;
        // 若图片过宽, 则保持长宽比缩放图片
        if (w > ww) {
            width = (int)ww;

            double i = w * 1.0 / ww;
            height = (int) Math.floor(h / i);

            bitmap = Bitmap.createScaledBitmap(bitmap, width, height, false);
        }

        // 若图片过长, 则从上端截取
        if (height > hh) {
            height = (int)hh;
            bitmap = Bitmap.createBitmap(bitmap, 0, 0, width, height);
        }
        return  bitmap;
        // return compressImage(bitmap);//压缩好比例大小后再进行质量压缩
    }

    //压缩到200k以下
    private static Bitmap compressImage(Bitmap image) {

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        image.compress(Bitmap.CompressFormat.JPEG, 100, baos);//质量压缩方法，这里100表示不压缩，把压缩后的数据存放到baos中
        int options = 100;
        while ( baos.toByteArray().length / 1024>200) {  //循环判断如果压缩后图片是否大于200kb,大于继续压缩
            baos.reset();//重置baos即清空baos
            image.compress(Bitmap.CompressFormat.JPEG, options, baos);//这里压缩options%，把压缩后的数据存放到baos中
            options -= 10;//每次都减少10
        }
        ByteArrayInputStream isBm = new ByteArrayInputStream(baos.toByteArray());//把压缩后的数据baos存放到ByteArrayInputStream中
        Bitmap bitmap = BitmapFactory.decodeStream(isBm, null, null);//把ByteArrayInputStream数据生成图片
        return bitmap;
    }
    //压缩到..k以下
    private static Bitmap compressImage(Bitmap image,int k) {

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        image.compress(Bitmap.CompressFormat.JPEG, 100, baos);//质量压缩方法，这里100表示不压缩，把压缩后的数据存放到baos中
        int options = 100;
        while ( baos.toByteArray().length / 1024>k) {  //循环判断如果压缩后图片是否大于200kb,大于继续压缩
            baos.reset();//重置baos即清空baos
            image.compress(Bitmap.CompressFormat.JPEG, options, baos);//这里压缩options%，把压缩后的数据存放到baos中
            options -= 10;//每次都减少10
        }
        ByteArrayInputStream isBm = new ByteArrayInputStream(baos.toByteArray());//把压缩后的数据baos存放到ByteArrayInputStream中
        Bitmap bitmap = BitmapFactory.decodeStream(isBm, null, null);//把ByteArrayInputStream数据生成图片
        return bitmap;
    }
    public static File saveBitmap(Bitmap image,String filepath) {
        File f=new File(filepath);
        if (f.exists()) {
            f.delete();
        }
        FileOutputStream out = null;
        try {
            out = new FileOutputStream(f);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        try {
            image.compress(Bitmap.CompressFormat.JPEG, 100, out);
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        try {
            out.flush();
            out.close();
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return f;
    }
    public static File saveBitmapWithError(Bitmap image,String filepath) throws Exception {
        File f=new File(filepath);
        if (f.exists()) {
            f.delete();
        }
        FileOutputStream out  = new FileOutputStream(f);
        image.compress(Bitmap.CompressFormat.JPEG, 100, out);
        image.compress(Bitmap.CompressFormat.JPEG, 100, out);
        out.flush();
        out.close();
        return f;
    }

    public static byte[] bitmapToByte(Bitmap thumbBitmap){
        if(thumbBitmap==null)return  null;
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        thumbBitmap.compress(Bitmap.CompressFormat.PNG, 100, stream);
        byte[] byteArray = stream.toByteArray();
        return  byteArray;
    }

    //把图片压缩到1280*720
    public static Bitmap getImageForShare(Bitmap bitmap) {
        int w = bitmap.getWidth();
        int h = bitmap.getHeight();
        //现在主流手机比较多是800*480分辨率，所以高和宽我们设置为
        float hh = 200f;//这里设置高度为800f
        float ww = 200f;//这里设置宽度为480f
        //缩放比。由于是固定比例缩放，只用高或者宽其中一个数据进行计算即可
        int be = 1;//be=1表示不缩放
        if (w > h && w > ww) {//如果宽度大的话根据宽度固定大小缩放
            be = (int) (w / ww);
        } else if (w < h && h > hh) {//如果高度高的话根据宽度固定大小缩放
            be = (int) (h / hh);
        }
        if (be <= 0)
            be = 1;


        int width  = w;
        int height = h;
        // 若图片过宽, 则保持长宽比缩放图片
        if (w > ww) {
            width = (int)ww;

            double i = w * 1.0 / ww;
            height = (int) Math.floor(h / i);

            bitmap = Bitmap.createScaledBitmap(bitmap, width, height, false);
        }

        // 若图片过长, 则从上端截取
        if (height > hh) {
            height = (int)hh;
            bitmap = Bitmap.createBitmap(bitmap, 0, 0, width, height);
        }


        return  compressImage(bitmap,32);
        // return compressImage(bitmap);//压缩好比例大小后再进行质量压缩
    }

    ///获取压缩到..k以下的包
    public static byte[] getCompressByte(Bitmap image,int k){
        if(image==null)return  null;
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        image.compress(Bitmap.CompressFormat.JPEG, 100, baos);//质量压缩方法，这里100表示不压缩，把压缩后的数据存放到baos中
        int options = 100;
        while ( baos.toByteArray().length / 1024>k) {  //循环判断如果压缩后图片是否大于200kb,大于继续压缩
            baos.reset();//重置baos即清空baos
            image.compress(Bitmap.CompressFormat.JPEG, options, baos);//这里压缩options%，把压缩后的数据存放到baos中
            options -= 10;//每次都减少10
        }
        byte[] result=baos.toByteArray();
        try {
            baos.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return  result;
    }
}
