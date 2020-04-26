package com.wilbert.library.codecs;

import android.media.MediaCodec;

import java.nio.ByteBuffer;

/**
 * author : wilbert
 * e-mail : jw20082009@qq.com
 * time   : 2020/04/26
 * desc   :
 */
public class OutputInfo {

    public int outputIndex;
    public MediaCodec.BufferInfo bufferInfo;

    public OutputInfo(int outIndex, MediaCodec.BufferInfo info) {
        this.outputIndex = outIndex;
        this.bufferInfo = info;
    }
}
