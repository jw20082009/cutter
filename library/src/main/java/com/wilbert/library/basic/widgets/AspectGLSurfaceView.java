
package com.wilbert.library.basic.widgets;

import android.content.Context;
import android.opengl.GLSurfaceView;
import android.util.AttributeSet;

public class AspectGLSurfaceView extends GLSurfaceView implements IAspectView{

    public static final int MODE_WIDTH = 0x00;

    public static final int MODE_HEIGHT = 0x01;

    int mode = MODE_WIDTH;
    int mInputWidth, mInputHeight;

    public AspectGLSurfaceView(Context context) {
        this(context, null);
    }

    public AspectGLSurfaceView(Context context, AttributeSet attrs) {
        super(context, attrs);
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

    public void setMode(int mode) {
        this.mode = mode;
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
