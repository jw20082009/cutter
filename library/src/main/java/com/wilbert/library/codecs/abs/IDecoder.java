package com.wilbert.library.codecs.abs;

import android.media.MediaFormat;

import java.io.IOException;

/**
 * author : wilbert
 * e-mail : jw20082009@qq.com
 * time   : 2020/05/09
 * desc   :
 */
public interface IDecoder {
    boolean prepare(MediaFormat format) throws IOException;

    InputInfo dequeueInputBuffer();

    void queueInputBuffer(InputInfo inputInfo);

    FrameInfo dequeueOutputBuffer();

    void queueOutputBuffer(FrameInfo frameInfo);

    boolean flush();

    void release();
}
