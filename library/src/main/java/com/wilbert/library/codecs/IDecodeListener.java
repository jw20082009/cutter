package com.wilbert.library.codecs;

/**
 * author : wilbert
 * e-mail : jw20082009@qq.com
 * time   : 2020/04/27
 * desc   :
 */
public interface IDecodeListener {

    void onInputBufferAvailable(InputInfo inputInfo);

    void onPrepared(VideoDecoderWrapper decoder);

    void onReleasing(VideoDecoderWrapper decoder);

    void onError(VideoDecoderWrapper decoder, Throwable throwable);
}
