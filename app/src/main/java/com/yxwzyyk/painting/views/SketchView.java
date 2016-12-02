package com.yxwzyyk.painting.views;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import com.yxwzyyk.painting.bean.StrokeRecord;
import com.yxwzyyk.painting.utils.MyColor;

import java.io.ByteArrayOutputStream;
import java.lang.ref.WeakReference;

/**
 * Created by yyk on 30/09/2016.
 */

public class SketchView extends View implements View.OnTouchListener {

    private final String TAG = this.getClass().getSimpleName();

    public static final int DEFAULT_STROKE_SIZE = 7;
    public static final int DEFAULT_STROKE_ALPHA = 255;
    public static final int DEFAULT_ERASER_SIZE = 50;

    private float mStrokeSize = DEFAULT_STROKE_SIZE;
    private int mStrokeRealColor = Color.BLACK;//画笔实际颜色
    private int mStrokeColor = Color.BLACK;//画笔颜色
    private int mStrokeAlpha = DEFAULT_STROKE_ALPHA;//画笔透明度
    private float mEraserSize = DEFAULT_ERASER_SIZE;
    private boolean mClean = false;

    private int mStrokeType = StrokeRecord.STROKE_TYPE_DRAW;

    private Context mContext;

    private float downX, downY, preX, preY, curX, curY;
    private int mWidth, mHeight;

    private RectF mStrokeRect;
    private Path mStrokePath;
    private Paint mStrokePaint;

    private Bitmap mBitmap;
    private Bitmap mBaseBitmap;
    private int[] mColors;

    public SketchView(Context context) {
        this(context, null);
    }

    public SketchView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public SketchView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        this.mContext = context;
        initParams();
    }

    public void initParams() {
        //设置背景
        setBackgroundColor(Color.WHITE);
        //设置监听
        setOnTouchListener(this);
        //初始化画笔
        mStrokePaint = new Paint();
        mStrokePaint.setAntiAlias(true);//抗锯齿
        mStrokePaint.setStyle(Paint.Style.STROKE);
        mStrokePaint.setColor(mStrokeRealColor);
        mStrokePaint.setStrokeWidth(mStrokeSize);
        //初始化路径
        mStrokePath = new Path();
        //初始化原和长方形
        mStrokeRect = new RectF();
    }

    //设置画笔透明度
    public void setStrokeAlpha(int mAlpha) {
        mStrokeAlpha = mAlpha;
        calculColor();
    }

    //设置画笔颜色
    public void setStrokeColor(int color) {
        mStrokeColor = color;
        calculColor();
    }

    //计算颜色混合
    private void calculColor() {
        mStrokeRealColor = Color.argb(mStrokeAlpha, Color.red(mStrokeColor), Color.green(mStrokeColor), Color.blue(mStrokeColor));
    }

    //设置橡皮擦大小
    public void setEraserSize(int size) {
        mEraserSize = size;
    }

    //设置画笔类型
    public void setStrokeType(int type) {
        mStrokeType = type;
    }

    //设置画笔大小
    public void setStrokeSize(int size) {
        mStrokeSize = size;
    }

    //清空画板
    public void clean() {
        mClean = true;
        invalidate();
    }

    //获取图片
    public Bitmap getBitmap() {
        return mBitmap;
    }

    //设置图片
    public void setBitmap(String path) {
        Bitmap bitmap = convertToBitmap(path, mWidth, mHeight);
        mBitmap = bitmap.copy(bitmap.getConfig(), true);
        invalidate();
    }


    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        mWidth = MeasureSpec.getSize(widthMeasureSpec);
        mHeight = MeasureSpec.getSize(heightMeasureSpec);
        setMeasuredDimension(mWidth, mHeight);

        mColors = new int[mWidth * mHeight];
        for (int i = 0; i < mColors.length; i++) {
            mColors[i] = Color.WHITE;
        }
        mBaseBitmap = Bitmap.createBitmap(mColors, mWidth, mHeight, Bitmap.Config.ARGB_8888);

    }

    @Override
    protected void onDraw(Canvas canvas) {
        if (mClean) {
            mBitmap = mBaseBitmap.copy(mBaseBitmap.getConfig(), true);
            canvas.drawBitmap(mBitmap, 0, 0, null);
            mClean = false;
        } else {
            if (mBitmap == null) {
                //初始化bitmap
                mBitmap = mBaseBitmap.copy(mBaseBitmap.getConfig(), true);
            } else {
                canvas.drawBitmap(mBitmap, 0, 0, null);
            }
            painting(canvas);
        }
    }


    @Override
    public boolean onTouch(View v, MotionEvent event) {
        curX = event.getX();
        curY = event.getY();
        switch (event.getAction() & MotionEvent.ACTION_MASK) {
            case MotionEvent.ACTION_POINTER_DOWN:
                break;
            case MotionEvent.ACTION_DOWN:
                touchDown(event);
                break;
            case MotionEvent.ACTION_MOVE:
                touchMove(event);
                break;
            case MotionEvent.ACTION_UP:
                touchUp(event);
                break;
        }
        invalidate();
        //改变当前坐标
        preX = curX;
        preY = curY;
        return true;
    }

    private void painting(Canvas canvas) {
        if (mStrokeType == StrokeRecord.STROKE_TYPE_DRAW || mStrokeType == StrokeRecord.STROKE_TYPE_LINE || mStrokeType == StrokeRecord.STROKE_TYPE_ERASER) {
            canvas.drawPath(mStrokePath, mStrokePaint);
        } else if (mStrokeType == StrokeRecord.STROKE_TYPE_RECTANGLE) {
            canvas.drawRect(mStrokeRect, mStrokePaint);
        } else if (mStrokeType == StrokeRecord.STROKE_TYPE_CIRCLE) {
            canvas.drawOval(mStrokeRect, mStrokePaint);
        }
    }

    private void touchUp(MotionEvent event) {
        Canvas newCanvas = new Canvas(mBitmap);
        painting(newCanvas);
    }

    private void touchMove(MotionEvent event) {
        if (mStrokeType == StrokeRecord.STROKE_TYPE_DRAW || mStrokeType == StrokeRecord.STROKE_TYPE_ERASER) {
            mStrokePath.quadTo(preX, preY, (curX + preX) / 2, (curY + preY) / 2);
        } else if (mStrokeType == StrokeRecord.STROKE_TYPE_LINE) {
            mStrokePath.reset();
            mStrokePath.moveTo(downX, downY);
            mStrokePath.lineTo(curX, curY);
        } else if (mStrokeType == StrokeRecord.STROKE_TYPE_CIRCLE || mStrokeType == StrokeRecord.STROKE_TYPE_RECTANGLE) {
            mStrokeRect.set(downX < curX ? downX : curX, downY < curY ? downY : curY, downX > curX ? downX : curX, downY > curY ? downY : curY);
        }
        preX = curX;
        preY = curY;
    }

    private void touchDown(MotionEvent event) {
        downX = event.getX();
        downY = event.getY();

        //设置画笔颜色和粗细
        mStrokePaint.setColor(mStrokeRealColor);
        mStrokePaint.setStrokeWidth(mStrokeSize);

        if (mStrokeType == StrokeRecord.STROKE_TYPE_ERASER) {
            mStrokePaint.setColor(Color.WHITE);
            mStrokePaint.setStrokeWidth(mEraserSize);
            mStrokePath.reset();
            mStrokePath.moveTo(downX, downY);
        } else if (mStrokeType == StrokeRecord.STROKE_TYPE_DRAW || mStrokeType == StrokeRecord.STROKE_TYPE_LINE) {
            mStrokePath.reset();
            mStrokePath.moveTo(downX, downY);
        } else if (mStrokeType == StrokeRecord.STROKE_TYPE_CIRCLE || mStrokeType == StrokeRecord.STROKE_TYPE_RECTANGLE) {
            mStrokeRect.set(downX, downY, downX, downY);
        }
    }

    private Bitmap convertToBitmap(String path, int w, int h) {
        BitmapFactory.Options opts = new BitmapFactory.Options();
        // 设置为ture只获取图片大小
        opts.inJustDecodeBounds = true;
        opts.inPreferredConfig = Bitmap.Config.ARGB_8888;
        // 返回为空
        BitmapFactory.decodeFile(path, opts);
        int width = opts.outWidth;
        int height = opts.outHeight;
        float scaleWidth = 0.f, scaleHeight = 0.f;
        if (width > w || height > h) {
            // 缩放
            scaleWidth = ((float) width) / w;
            scaleHeight = ((float) height) / h;
        }
        opts.inJustDecodeBounds = false;
        float scale = Math.max(scaleWidth, scaleHeight);
        opts.inSampleSize = (int) scale;
        WeakReference<Bitmap> weak = new WeakReference<Bitmap>(BitmapFactory.decodeFile(path, opts));
        return Bitmap.createScaledBitmap(weak.get(), w, h, true);
    }

}
