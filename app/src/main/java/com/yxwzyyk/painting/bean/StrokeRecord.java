package com.yxwzyyk.painting.bean;

import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PointF;
import android.graphics.RectF;
import android.text.TextPaint;

public class StrokeRecord {
    public static final int STROKE_TYPE_ERASER = 1;
    public static final int STROKE_TYPE_DRAW = 2;
    public static final int STROKE_TYPE_LINE = 3;
    public static final int STROKE_TYPE_CIRCLE = 4;
    public static final int STROKE_TYPE_RECTANGLE = 5;
    public static final int STROKE_TYPE_TEXT = 6;
    public static final int STROKE_TYPE_PHOTO = 7;

    public int type;//记录类型

    public StrokeRecord(int type) {
        this.type = type;
    }
}