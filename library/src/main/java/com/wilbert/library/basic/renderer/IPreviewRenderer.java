package com.wilbert.library.basic.renderer;

import android.opengl.GLSurfaceView;

import java.util.List;

public interface IPreviewRenderer extends GLSurfaceView.Renderer {

    void setGLSurfaceView(GLSurfaceView surfaceView);

    void onResume();

    void onPause();

    void switchCamera();

    int getCameraID();

    void changePreviewAspect(float aspect);

    List<Float> getAspects();

    void saveImage();

    void setPreviewListener(IPreviewListener listener);

    void setMeteringArea(float touchX, float touchY);

    void setExposureCompensation(int progress);

    void handleZoom(boolean isZoomOut);

    int getPreviewWidth();

    int getPreviewHeight();

    void onDestroy();
}
