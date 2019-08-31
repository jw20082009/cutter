package com.wilbert.library.recorder.interfaces;

import android.opengl.EGLContext;

import java.nio.ByteBuffer;

/**
 * Created by Android Studio.
 * User: wilbert jw20082009@qq.com
 * Date: 2019/7/25 16:37
 */
public interface IVideoEncoder {
    int getEncodeType();
    ByteBuffer getBuffer(int width, int height);
    void setEglContext(final EGLContext shared_context, final int tex_id);
    boolean frameAvailableSoon(int textureId, final float[] mvp_matrix);
}
