package com.wilbert.library.codecs;

import java.nio.ByteBuffer;

/**
 * author : wilbert
 * e-mail : jw20082009@qq.com
 * time   : 2020/04/26
 * desc   :
 */
public class InputInfo {
    public int inputIndex;
    public ByteBuffer buffer;

    public InputInfo(int inputIndex, ByteBuffer buffer) {
        this.inputIndex = inputIndex;
        this.buffer = buffer;
    }
}
