package com.wilbert.library.videoprocessor.effect;

import android.content.Context;
import android.media.MediaExtractor;
import android.support.annotation.Nullable;

import com.wilbert.library.basic.entity.ActionEntity;
import com.wilbert.library.basic.entity.StickerEntity;
import com.wilbert.library.videoprocessor.IVideoEncodeThread;
import com.wilbert.library.videoprocessor.VideoDecodeThread;

import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

public class VideoDecodeWithEffectThread extends VideoDecodeThread {

    Context context;
    int videoWidth;
    int videoHeight;
    int resultWidth;
    int resultHeight;
    int duration;
    int currentTime;

    OutputEffectSurface effectSurface;
    List<ActionEntity> actionEntities = null;
    List<StickerEntity> stickerEntities = null;

    public VideoDecodeWithEffectThread(IVideoEncodeThread videoEncodeThread, MediaExtractor extractor, @Nullable Integer startTimeMs, @Nullable Integer endTimeMs, @Nullable Integer srcFrameRate, @Nullable Integer dstFrameRate, @Nullable Float speed, boolean dropFrames, int videoIndex, AtomicBoolean decodeDone) {
        super(videoEncodeThread, extractor, startTimeMs, endTimeMs, srcFrameRate, dstFrameRate, speed, dropFrames, videoIndex, decodeDone);
    }

    public VideoDecodeWithEffectThread(Context context, int videoWidth, int videoHeight, int resultWidth, int resultHeight, IVideoEncodeThread videoEncodeThread, MediaExtractor extractor, @Nullable Integer startTimeMs, @Nullable Integer endTimeMs, @Nullable Integer srcFrameRate, @Nullable Integer dstFrameRate, @Nullable Float speed, boolean dropFrames, int videoIndex, AtomicBoolean decodeDone) {
        super(videoEncodeThread, extractor, startTimeMs, endTimeMs, srcFrameRate, dstFrameRate, speed, dropFrames, videoIndex, decodeDone);
        this.context = context;
        this.videoWidth = videoWidth;
        this.videoHeight = videoHeight;
        this.resultWidth = resultWidth;
        this.resultHeight = resultHeight;
    }

    public void setDecodeTime(int duration, int currentTime) {
        this.duration = duration;
        this.currentTime = currentTime;
        if (effectSurface != null) {
            effectSurface.initDecodeTime(duration, currentTime);
        }
    }

    public void setActionEntities(List<ActionEntity> actions) {
        this.actionEntities = actions;
        if (effectSurface != null) {
            effectSurface.setActionEntities(actionEntities);
        }
    }

    public void setStickerEntities(List<StickerEntity> stickerEntities) {
        this.stickerEntities = stickerEntities;
        if (effectSurface != null) {
            effectSurface.setStickerEntities(stickerEntities);
        }
    }

    @Override
    protected OutputEffectSurface getOutputSurface() {
        if (effectSurface == null) {
            effectSurface = new OutputEffectSurface(context, videoWidth, videoHeight, resultWidth, resultHeight);
            effectSurface.initDecodeTime(duration, currentTime);
            effectSurface.setActionEntities(actionEntities);
            effectSurface.setStickerEntities(stickerEntities);
        }
        return effectSurface;
    }
}
