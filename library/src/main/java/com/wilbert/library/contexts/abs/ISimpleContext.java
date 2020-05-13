package com.wilbert.library.contexts.abs;

import com.wilbert.library.contexts.abs.IOpenable;

import java.nio.ByteBuffer;

/**
 * author : Administrator
 * e-mail : jw20082009@qq.com
 * time   : 2020/04/26
 * desc   :
 */
public interface ISimpleContext extends IOpenable {
    void onFrameAvailable(ByteBuffer byteBuffer);
}
