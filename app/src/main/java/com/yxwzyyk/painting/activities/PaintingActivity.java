package com.yxwzyyk.painting.activities;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.SeekBar;

import com.yxwzyyk.painting.R;
import com.yxwzyyk.painting.bean.StrokeRecord;
import com.yxwzyyk.painting.utils.L;
import com.yxwzyyk.painting.utils.ScreenUtils;
import com.yxwzyyk.painting.utils.Tools;
import com.yxwzyyk.painting.views.SketchView;

import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

import static com.yxwzyyk.painting.app.ConstUtils.EXTENSION;
import static com.yxwzyyk.painting.app.ConstUtils.FILE_PATH;
import static com.yxwzyyk.painting.app.ConstUtils.TEMP_FILE_NAME;
import static com.yxwzyyk.painting.app.ConstUtils.TEMP_FILE_PATH;
import static com.yxwzyyk.painting.utils.MyColor.COLOR_BLACK;
import static com.yxwzyyk.painting.utils.MyColor.COLOR_BLUE;
import static com.yxwzyyk.painting.utils.MyColor.COLOR_GREEN;
import static com.yxwzyyk.painting.utils.MyColor.COLOR_ORANGE;
import static com.yxwzyyk.painting.utils.MyColor.COLOR_RED;

/**
 * Created by yyk on 29/09/2016.
 */

public class PaintingActivity extends BaseActivity {


    @BindView(R.id.painting_sketchView)
    SketchView mPaintingSketchView;
    @BindView(R.id.painting_menu_draw)
    ImageView mPaintingMenuDraw;
    @BindView(R.id.painting_menu_eraser)
    ImageView mPaintingMenuEraser;
    @BindView(R.id.painting_menu_delete)
    ImageView mPaintingMenuDelete;
    @BindView(R.id.painting_menu_save)
    ImageView mPaintingMenuSave;
    @BindView(R.id.painting_menu_share)
    ImageView mPaintingMenuShare;
    private PopupWindow strokePopupWindow, eraserPopupWindow;
    private View popupStrokeLayout, popupEraserLayout;

    private int pupWindowsDPWidth = 300;//弹窗宽度，单位DP
    private int strokePupWindowsDPHeight = 275;//画笔弹窗高度，单位DP
    private int eraserPupWindowsDPHeight = 100;

    private int mNowDraw = StrokeRecord.STROKE_TYPE_DRAW;
    private String mImgName;

    private long exitTime;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_painting);
        ButterKnife.bind(this);

        initView();
        initStrokePop();
        initEraserPop();
    }

    @Override
    public void onBackPressed() {
        if ((System.currentTimeMillis() - exitTime) > 2000) {
            toast(R.string.app_exit);
            exitTime = System.currentTimeMillis();
        } else {
            this.finish();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    private void initView() {
        if (getSupportActionBar() != null)
            getSupportActionBar().hide();

        // popupWindow布局
        LayoutInflater inflater = (LayoutInflater) getSystemService(Activity.LAYOUT_INFLATER_SERVICE);
        popupStrokeLayout = inflater.inflate(R.layout.popup_sketch_stroke, null);
        popupEraserLayout = inflater.inflate(R.layout.popup_sketch_eraser, null);

        mPaintingMenuDraw.setOnClickListener(v -> {
            strokePopupWindow.showAsDropDown(v);
            mPaintingSketchView.setStrokeType(mNowDraw);
        });
        mPaintingMenuEraser.setOnClickListener(v -> {
            eraserPopupWindow.showAsDropDown(v);
            mPaintingSketchView.setStrokeType(StrokeRecord.STROKE_TYPE_ERASER);
        });
        mPaintingMenuDelete.setOnClickListener(v -> mPaintingSketchView.clean());

        mPaintingMenuSave.setOnClickListener(v -> {
            toast(R.string.painting_save);
            if (save(FILE_PATH, mImgName) != null) {
                toast(R.string.painting_save_ok);
            } else {
                toast(R.string.painting_save_failure);
            }
        });

        mPaintingMenuShare.setOnClickListener(v -> {
            share();
        });

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            List<String> list = new ArrayList<>();

            if (checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                list.add(Manifest.permission.WRITE_EXTERNAL_STORAGE);
            }
            Tools.requestAlertWindowPermission(this, list, 0);
        }

        String file = getIntent().getStringExtra("file");
        if (file != null && !file.isEmpty()) {
            mImgName = file;
            mPaintingSketchView.post(() -> mPaintingSketchView.setBitmap(FILE_PATH + mImgName));
        } else {
            mImgName = System.currentTimeMillis() + EXTENSION;
        }
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        for (int i = 0; i < permissions.length; i++) {
            if (permissions[i].equals(Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                if (grantResults[i] != PackageManager.PERMISSION_GRANTED) {
                    toast(R.string.permission_storage);
                    finish();
                }
            }

        }
    }

    private void initStrokePop() {
        //画笔弹窗
        strokePopupWindow = new PopupWindow(mContext);
        strokePopupWindow.setContentView(popupStrokeLayout);//设置主体布局
        strokePopupWindow.setWidth(ScreenUtils.dip2px(mContext, pupWindowsDPWidth));//宽度
//        strokePopupWindow.setHeight(WindowManager.LayoutParams.WRAP_CONTENT);//高度自适应
        strokePopupWindow.setHeight(ScreenUtils.dip2px(mContext, strokePupWindowsDPHeight));//高度
        strokePopupWindow.setFocusable(true);
        strokePopupWindow.setBackgroundDrawable(new BitmapDrawable(getResources(), Bitmap.createBitmap(pupWindowsDPWidth, strokePupWindowsDPHeight, Bitmap.Config.ALPHA_8)));//设置空白背景
        strokePopupWindow.setAnimationStyle(R.style.mypopwindow_anim_style);//动画

        PopupStrokeViewHolder popupStrokeViewHolder = new PopupStrokeViewHolder(popupStrokeLayout);
        popupStrokeViewHolder.mStrokeTypeRadioGroup.setOnCheckedChangeListener((group, checkedId) -> {
            int resId = R.drawable.ic_create_cheched;
            mNowDraw = StrokeRecord.STROKE_TYPE_DRAW;
            switch (checkedId) {
                case R.id.stroke_type_rbtn_draw:
                    resId = R.drawable.ic_create_cheched;
                    mNowDraw = StrokeRecord.STROKE_TYPE_DRAW;
                    break;
                case R.id.stroke_type_rbtn_line:
                    resId = R.drawable.ic_line_checked;
                    mNowDraw = StrokeRecord.STROKE_TYPE_LINE;
                    break;
                case R.id.stroke_type_rbtn_circle:
                    resId = R.drawable.ic_circle_checked;
                    mNowDraw = StrokeRecord.STROKE_TYPE_CIRCLE;
                    break;
                case R.id.stroke_type_rbtn_rectangle:
                    resId = R.drawable.ic_rectangle_cheched;
                    mNowDraw = StrokeRecord.STROKE_TYPE_RECTANGLE;
                    break;
//                    case R.id.stroke_type_rbtn_text:
//                        resId = R.drawable.ic_text_checked;
//                        type = StrokeRecord.STROKE_TYPE_TEXT;
//                        break;
            }
            mPaintingSketchView.setStrokeType(mNowDraw);
            mPaintingMenuDraw.setImageResource(resId);
            strokePopupWindow.dismiss();
        });
        popupStrokeViewHolder.mStrokeColorRadioGroup.setOnCheckedChangeListener((group, checkedId) -> {
            int color = COLOR_BLACK;
            if (checkedId == R.id.stroke_color_black) {
                color = COLOR_BLACK;
            } else if (checkedId == R.id.stroke_color_red) {
                color = COLOR_RED;
            } else if (checkedId == R.id.stroke_color_green) {
                color = COLOR_GREEN;
            } else if (checkedId == R.id.stroke_color_orange) {
                color = COLOR_ORANGE;
            } else if (checkedId == R.id.stroke_color_blue) {
                color = COLOR_BLUE;
            }
            mPaintingSketchView.setStrokeColor(color);
        });
        popupStrokeViewHolder.mStrokeSeekbar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                int size = popupStrokeViewHolder.mStrokeCircle.getDrawable().getIntrinsicWidth();
                int calcProgress = progress > 1 ? progress : 1;
                int newSize = Math.round((size / 100f) * calcProgress);
                int offset = Math.round((size - newSize) / 2);
                LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(newSize, newSize);
                lp.setMargins(offset, offset, offset, offset);
                popupStrokeViewHolder.mStrokeCircle.setLayoutParams(lp);
                mPaintingSketchView.setStrokeSize(progress);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
        popupStrokeViewHolder.mStrokeSeekbar.setMax(100);
        popupStrokeViewHolder.mStrokeSeekbar.setProgress(SketchView.DEFAULT_STROKE_SIZE);

        popupStrokeViewHolder.mStrokeAlphaSeekbar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                mPaintingSketchView.setStrokeAlpha(progress);
                popupStrokeViewHolder.mStrokeAlphaCircle.setAlpha(progress);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
        popupStrokeViewHolder.mStrokeAlphaSeekbar.setMax(255);
        popupStrokeViewHolder.mStrokeAlphaSeekbar.setProgress(SketchView.DEFAULT_STROKE_ALPHA);

    }

    private void initEraserPop() {
        //橡皮擦弹窗
        eraserPopupWindow = new PopupWindow(mContext);
        eraserPopupWindow.setContentView(popupEraserLayout);//设置主体布局
        eraserPopupWindow.setWidth(ScreenUtils.dip2px(mContext, pupWindowsDPWidth));//宽度200dp
//        eraserPopupWindow.setHeight(WindowManager.LayoutParams.WRAP_CONTENT);//高度自适应
        eraserPopupWindow.setHeight(ScreenUtils.dip2px(mContext, eraserPupWindowsDPHeight));//高度自适应
        eraserPopupWindow.setFocusable(true);
        eraserPopupWindow.setBackgroundDrawable(new BitmapDrawable(getResources(), Bitmap.createBitmap(pupWindowsDPWidth, eraserPupWindowsDPHeight, Bitmap.Config.ALPHA_8)));//设置空白背景
        eraserPopupWindow.setAnimationStyle(R.style.mypopwindow_anim_style);//动画

        PopupEraserViewHolder popupEraserViewHolder = new PopupEraserViewHolder(popupEraserLayout);
        popupEraserViewHolder.mStrokeSeekbar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                int size = popupEraserViewHolder.mStrokeCircle.getDrawable().getIntrinsicWidth();
                int calcProgress = progress > 1 ? progress : 1;
                int newSize = Math.round((size / 100f) * calcProgress);
                int offset = Math.round((size - newSize) / 2);
                LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(newSize, newSize);
                lp.setMargins(offset, offset, offset, offset);
                popupEraserViewHolder.mStrokeCircle.setLayoutParams(lp);
                mPaintingSketchView.setEraserSize(progress);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
        popupEraserViewHolder.mStrokeSeekbar.setMax(100);
        popupEraserViewHolder.mStrokeSeekbar.setProgress(SketchView.DEFAULT_ERASER_SIZE);
    }

    private File save(String filePath, String imgName) {
        Bitmap bitmap = mPaintingSketchView.getBitmap();
        if (bitmap == null) {
            toast(R.string.painting_null);
        } else {
            try {
                File dir = new File(filePath);
                if (!dir.exists()) {
                    dir.mkdirs();
                }

                File f = new File(filePath, imgName);
                if (!f.exists()) {
                    f.createNewFile();
                } else {
                    f.delete();
                }
                FileOutputStream out = new FileOutputStream(f);
                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, out);
                out.close();
                return f;
            } catch (Exception e) {
                L.e(TAG, e.getMessage());
                return null;
            }
        }
        return null;
    }

    private void share() {
        File file = save(TEMP_FILE_PATH, TEMP_FILE_NAME);
        if (file != null) {
            Intent shareIntent = new Intent(Intent.ACTION_SEND);
            shareIntent.setType("image/*");
            Uri uri = Uri.fromFile(file);
            shareIntent.putExtra(Intent.EXTRA_STREAM, uri);
            startActivity(Intent.createChooser(shareIntent, getString(R.string.painting_share)));
        }
    }


    static class PopupStrokeViewHolder {
        @BindView(R.id.stroke_type_rbtn_draw)
        RadioButton mStrokeTypeRbtnDraw;
        @BindView(R.id.stroke_type_rbtn_line)
        RadioButton mStrokeTypeRbtnLine;
        @BindView(R.id.stroke_type_rbtn_circle)
        RadioButton mStrokeTypeRbtnCircle;
        @BindView(R.id.stroke_type_rbtn_rectangle)
        RadioButton mStrokeTypeRbtnRectangle;
        @BindView(R.id.stroke_type_rbtn_text)
        RadioButton mStrokeTypeRbtnText;
        @BindView(R.id.stroke_type_radio_group)
        RadioGroup mStrokeTypeRadioGroup;
        @BindView(R.id.stroke_circle)
        ImageView mStrokeCircle;
        @BindView(R.id.stroke_seekbar)
        SeekBar mStrokeSeekbar;
        @BindView(R.id.stroke_color_black)
        RadioButton mStrokeColorBlack;
        @BindView(R.id.stroke_color_red)
        RadioButton mStrokeColorRed;
        @BindView(R.id.stroke_color_green)
        RadioButton mStrokeColorGreen;
        @BindView(R.id.stroke_color_orange)
        RadioButton mStrokeColorOrange;
        @BindView(R.id.stroke_color_blue)
        RadioButton mStrokeColorBlue;
        @BindView(R.id.stroke_color_radio_group)
        RadioGroup mStrokeColorRadioGroup;
        @BindView(R.id.stroke_alpha_circle)
        ImageView mStrokeAlphaCircle;
        @BindView(R.id.stroke_alpha_seekbar)
        SeekBar mStrokeAlphaSeekbar;

        PopupStrokeViewHolder(View view) {
            ButterKnife.bind(this, view);
        }
    }

    static class PopupEraserViewHolder {
        @BindView(R.id.stroke_circle)
        ImageView mStrokeCircle;
        @BindView(R.id.stroke_seekbar)
        SeekBar mStrokeSeekbar;

        PopupEraserViewHolder(View view) {
            ButterKnife.bind(this, view);
        }
    }


}
