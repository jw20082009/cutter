package com.wilbert.library.contexts.abs;

import com.wilbert.library.contexts.abs.IOpenable;

/**
 * author : Administrator
 * e-mail : jw20082009@qq.com
 * time   : 2020/04/26
 * desc   :
 */
public interface IVideoContext extends IOpenable {
    void onFrameAvailable(int textureId);
}
