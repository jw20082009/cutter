package com.wilbert.library.recorder.interfaces;

/**
 * Created by Android Studio.
 * User: wilbert jw20082009@qq.com
 * Date: 2019/7/24 20:31
 */
public interface IMediaEncoderListener {
    void onPrepared(IEncoder encoder);
    void onStopped(IEncoder encoder);
}
