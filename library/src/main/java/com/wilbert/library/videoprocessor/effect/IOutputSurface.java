package com.wilbert.library.videoprocessor.effect;

import android.view.Surface;

public interface IOutputSurface {
    void awaitNewImage();

    void drawImage(long presentationTimeMs, boolean invert);

    Surface getSurface();

    void release();
}
