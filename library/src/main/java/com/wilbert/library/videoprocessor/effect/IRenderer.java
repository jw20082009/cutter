package com.wilbert.library.videoprocessor.effect;

import android.graphics.SurfaceTexture;

public interface IRenderer {

    int getTextureId();

    void surfaceCreated();

    void onInputSizeChanged(int width, int height);
    void onSurfaceChanged(int outwidth, int outheight);
    void drawFrame(SurfaceTexture st, boolean invert);

    void checkGlError(String op);

    void release();
}
