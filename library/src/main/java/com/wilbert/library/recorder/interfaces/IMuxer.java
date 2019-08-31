package com.wilbert.library.recorder.interfaces;

import android.media.MediaCodec;
import android.media.MediaFormat;

import java.nio.ByteBuffer;

/**
 * Created by Android Studio.
 * User: wilbert jw20082009@qq.com
 * Date: 2019/7/24 20:31
 */
public interface IMuxer {
    /**
     * 获取Mux后的输出文件路径
     *
     * @return
     */
    String getOutputPath();

    /**
     * 添加轨道（视频/音频/字幕）
     *
     * @param format
     * @return
     */
    int addTrack(final MediaFormat format);

    /**
     * 添加编码器
     *
     * @param encoder
     */
    void addEncoder(IEncoder encoder);

    boolean start();

    boolean isStarted();

    void writeSampleData(final int trackIndex, final ByteBuffer byteBuf, final MediaCodec.BufferInfo bufferInfo);

    void stop();
}
