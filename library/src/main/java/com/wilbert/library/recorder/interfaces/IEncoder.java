package com.wilbert.library.recorder.interfaces;

import java.io.IOException;
import java.nio.ByteBuffer;

/**
 * Created by Android Studio.
 * User: wilbert jw20082009@qq.com
 * Date: 2019/7/24 20:31
 */
public interface IEncoder {
    void prepare() throws IOException;

    void startEncode();

    void encode(final ByteBuffer buffer, final int length);

    void stopEncode();

    void signalEndOfInputStream();

    boolean frameAvailableSoon();

    void drain();

    void release();
}
