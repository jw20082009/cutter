package com.wilbert.library.contexts.yuv;

import com.wilbert.library.codecs.abs.FrameInfo;

/**
 * author : wilbert
 * e-mail : jw20082009@qq.com
 * time   : 2020/05/15
 * desc   :
 */
public interface IYuvRenderer {
    void onSurfaceCreated();

    void onSurfaceChanged(int width, int height);

    int yuv2Rgb(FrameInfo frameInfo);

    void destroyFrameBuffers();

    void release();
}
