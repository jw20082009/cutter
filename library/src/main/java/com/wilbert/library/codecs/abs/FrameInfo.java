package com.wilbert.library.codecs.abs;

import android.media.MediaCodec;

import java.nio.ByteBuffer;

/**
 * author : wilbert
 * e-mail : jw20082009@qq.com
 * time   : 2020/04/26
 * desc   :
 */
public class FrameInfo {

    public int outputIndex;
    public ByteBuffer outputBuffer;
    public MediaCodec.BufferInfo bufferInfo;

    public FrameInfo(int outIndex, ByteBuffer buffer, MediaCodec.BufferInfo info) {
        this.outputIndex = outIndex;
        this.outputBuffer = buffer;
        this.bufferInfo = info;
    }
}
