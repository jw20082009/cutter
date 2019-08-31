
package com.wilbert.library.basic.widgets;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.widget.FrameLayout;

public class AspectFrameLayout extends FrameLayout implements IAspectView {
    public static final int MODE_WIDTH = 0x00;

    public static final int MODE_HEIGHT = 0x01;

    int mode = MODE_WIDTH;
    int mInputWidth=720, mInputHeight=1280, mScreenWidth, mScreenHeight;

    public AspectFrameLayout(@NonNull Context context) {
        this(context, null);
    }

    public AspectFrameLayout(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public AspectFrameLayout(@NonNull Context context, @Nullable AttributeSet attrs,
                             int defStyleAttr) {
        this(context, attrs, defStyleAttr, 0);
    }

    public AspectFrameLayout(@NonNull Context context, @Nullable AttributeSet attrs,
                             int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        DisplayMetrics metrics = context.getResources().getDisplayMetrics();
        mScreenWidth = metrics.widthPixels;
        mScreenHeight = metrics.heightPixels;
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        if (mInputWidth > 0 && mInputHeight > 0) {
            int width = MeasureSpec.getSize(widthMeasureSpec);
            int height = MeasureSpec.getSize(heightMeasureSpec);
            float aspect = 1.0f * width / height;
            float aspectVideo = 1.0f * mInputWidth / mInputHeight;
            if (aspectVideo > aspect) {
                mode = MODE_WIDTH;
            } else {
                mode = MODE_HEIGHT;
            }
            if (mode == MODE_HEIGHT) {
                width = (int) (1.0f * height * mInputWidth / mInputHeight);
                super.onMeasure(MeasureSpec.makeMeasureSpec(width, MeasureSpec.EXACTLY), heightMeasureSpec);
            } else {
                height = (int) (1.0f * width * mInputHeight / mInputWidth);
                super.onMeasure(widthMeasureSpec,
                        MeasureSpec.makeMeasureSpec(height, MeasureSpec.EXACTLY));
            }
        } else {
            super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        }
    }

    @Override
    public void onInputSizeChanged(int width, int height) {
        this.mInputWidth = width;
        this.mInputHeight = height;
        post(new Runnable() {
            @Override
            public void run() {
                requestLayout();
            }
        });
    }
}
