package com.wilbert.library.codecs.abs;

import com.wilbert.library.codecs.VideoExtractorWrapper;

/**
 * author : wilbert
 * e-mail : jw20082009@qq.com
 * time   : 2020/04/27
 * desc   :
 */
public interface IExtractorListener {

    void onPrepared(VideoExtractorWrapper extractor);

    void onReleased(VideoExtractorWrapper extractor);

    void onError(VideoExtractorWrapper extractor, Throwable throwable);
}
