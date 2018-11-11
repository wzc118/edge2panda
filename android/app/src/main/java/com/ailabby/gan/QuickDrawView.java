package com.ailabby.gan;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.util.Log;


import android.support.v7.widget.AppCompatImageView;




public class QuickDrawView extends AppCompatImageView{
    private Path mPath;
    private Paint mPaint, mCanvasPaint;
    private Canvas mCanvas;
    private Bitmap mBitmap;
    private MainActivity mActivity;

    public QuickDrawView(Context context, AttributeSet attrs) {
        super(context, attrs);
        mActivity = (MainActivity) context;

        setPathPaint();

    }

    private void setPathPaint() {
        mPath = new Path();
        mPaint = new Paint();
        mPaint.setColor(0xFF000000);
        mPaint.setAntiAlias(true);
        mPaint.setStrokeWidth(18);
        mPaint.setStyle(Paint.Style.STROKE);
        mPaint.setStrokeJoin(Paint.Join.ROUND);
        mCanvasPaint = new Paint(Paint.DITHER_FLAG);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        mBitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
        mCanvas = new Canvas(mBitmap);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        canvas.drawBitmap(mBitmap, 0, 0, mCanvasPaint);
        canvas.drawPath(mPath, mPaint);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (!mActivity.canDraw()) return true;

        float x = event.getX();
        float y = event.getY();

        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                mPath.moveTo(x, y);
                break;
            case MotionEvent.ACTION_MOVE:
                mPath.lineTo(x, y);
                break;
            case MotionEvent.ACTION_UP:
                mCanvas.drawPath(mPath, mPaint);
                mPath.reset();
                Thread thread = new Thread(mActivity);
                thread.start();
                break;
            default:
                return false;
        }
        invalidate();
        return true;
    }


    public void clearRedraw(){
        mBitmap = Bitmap.createBitmap(mBitmap.getWidth(),mBitmap.getHeight(),Bitmap.Config.ARGB_8888);
        mCanvas = new Canvas(mBitmap);
        mCanvasPaint = new Paint(Paint.DITHER_FLAG);
        mCanvas.drawBitmap(mBitmap,0,0,mCanvasPaint);

        setPathPaint();

        invalidate();
    }

    public Bitmap getBitmap(){
        this.setDrawingCacheEnabled(true);
        this.buildDrawingCache();
        Bitmap bmp = Bitmap.createBitmap(this.getDrawingCache());
        if (this.getDrawingCache() == null){
            Log.w("bitmap","bad playing!");
        }
        this.setDrawingCacheEnabled(false);
        this.destroyDrawingCache();
        return bmp;
    }

    public void refresh(Bitmap bmp) {
        mBitmap = Bitmap.createBitmap(mBitmap.getWidth(),mBitmap.getHeight(),Bitmap.Config.ARGB_8888);
        mCanvasPaint = new Paint(Paint.DITHER_FLAG);
        mCanvas = new Canvas(mBitmap);
        mCanvas.drawBitmap(bmp,0,0,mCanvasPaint);

        invalidate();

    }

}