package com.wilbert.library.codecs;

/**
 * author : wilbert
 * e-mail : jw20082009@qq.com
 * time   : 2020/04/27
 * desc   :
 */
public interface IExtractorListener {
    void onInputBufferAvailable(InputInfo inputInfo);

    void onPrepared(VideoExtractorWrapper extractor);

    void onRelease(VideoExtractorWrapper extractor);

    void onError(VideoExtractorWrapper extractor, Throwable throwable);
}
