package com.wilbert.library.codecs.abs;

import android.media.MediaFormat;

import com.wilbert.library.codecs.VideoExtractor;

import java.io.FileDescriptor;

/**
 * author : wilbert
 * e-mail : jw20082009@qq.com
 * time   : 2020/05/09
 * desc   :
 */
public interface IExtractor {
    void prepare(String filePath, VideoExtractor.Type type);

    void prepare(FileDescriptor descriptor, VideoExtractor.Type type);

    FrameInfo getNextFrameBuffer();

    void seekTo(long timeUs);

    MediaFormat getMediaFormat();

    void setListener(IExtractorListener listener);

    void release();
}
