package com.wilbert.library.contexts;

import android.opengl.GLSurfaceView;
import android.view.Surface;

import com.wilbert.library.codecs.ISurfaceObtainer;

/**
 * author : Administrator
 * e-mail : jw20082009@qq.com
 * time   : 2020/04/26
 * desc   :
 */
public class VideoContext implements ISurfaceObtainer {

    GLSurfaceView mSurfaceView;

    public VideoContext(GLSurfaceView surfaceView) {
        mSurfaceView = surfaceView;
    }

    @Override
    public Surface getSurface() {
        return mSurfaceView.getHolder().getSurface();
    }
}
