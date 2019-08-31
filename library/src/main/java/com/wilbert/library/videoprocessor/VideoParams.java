package com.wilbert.library.videoprocessor;

import android.content.Context;
import android.support.annotation.Nullable;

import com.wilbert.library.basic.entity.AeEntity;
import com.wilbert.library.videoprocessor.util.VideoProgressListener;

/**
 * Created by Android Studio.
 * User: wilbert jw20082009@qq.com
 * Date: 2019/8/7 11:22
 */
public class VideoParams {

    private Context context;

    private String input;

    private String output;

    @Nullable
    private Integer outWidth;

    @Nullable
    private Integer outHeight;

    @Nullable
    private Integer startTimeMs;

    @Nullable
    private Integer endTimeMs;

    @Nullable
    private Float speed;

    @Nullable
    private Boolean changeAudioSpeed;

    @Nullable
    private Integer bitrate;

    @Nullable
    private Integer frameRate;

    @Nullable
    private Integer iFrameInterval;

    @Nullable
    private VideoProgressListener listener;

    /**
     * 帧率超过指定帧率时是否丢帧
     */
    private boolean dropFrames = true;

    private AeEntity aeEntity;

    /**
     * 编码类型，0：硬编码;1：软编码
     */
    private Integer codecType;

    public Context getContext() {
        return context;
    }

    public void setContext(Context context) {
        this.context = context;
    }

    public String getInput() {
        return input;
    }

    public void setInput(String input) {
        this.input = input;
    }

    public String getOutput() {
        return output;
    }

    public void setOutput(String output) {
        this.output = output;
    }

    @Nullable
    public Integer getOutWidth() {
        return outWidth;
    }

    public void setOutWidth(@Nullable Integer outWidth) {
        this.outWidth = outWidth;
    }

    @Nullable
    public Integer getOutHeight() {
        return outHeight;
    }

    public void setOutHeight(@Nullable Integer outHeight) {
        this.outHeight = outHeight;
    }

    @Nullable
    public Integer getStartTimeMs() {
        return startTimeMs;
    }

    public void setStartTimeMs(@Nullable Integer startTimeMs) {
        this.startTimeMs = startTimeMs;
    }

    @Nullable
    public Integer getEndTimeMs() {
        return endTimeMs;
    }

    public void setEndTimeMs(@Nullable Integer endTimeMs) {
        this.endTimeMs = endTimeMs;
    }

    @Nullable
    public Float getSpeed() {
        return speed;
    }

    public void setSpeed(@Nullable Float speed) {
        this.speed = speed;
    }

    @Nullable
    public Boolean getChangeAudioSpeed() {
        return changeAudioSpeed;
    }

    public void setChangeAudioSpeed(@Nullable Boolean changeAudioSpeed) {
        this.changeAudioSpeed = changeAudioSpeed;
    }

    @Nullable
    public Integer getBitrate() {
        return bitrate;
    }

    public void setBitrate(@Nullable Integer bitrate) {
        this.bitrate = bitrate;
    }

    @Nullable
    public Integer getFrameRate() {
        return frameRate;
    }

    public void setFrameRate(@Nullable Integer frameRate) {
        this.frameRate = frameRate;
    }

    @Nullable
    public Integer getiFrameInterval() {
        return iFrameInterval;
    }

    public void setiFrameInterval(@Nullable Integer iFrameInterval) {
        this.iFrameInterval = iFrameInterval;
    }

    @Nullable
    public VideoProgressListener getListener() {
        return listener;
    }

    public void setListener(@Nullable VideoProgressListener listener) {
        this.listener = listener;
    }

    public boolean isDropFrames() {
        return dropFrames;
    }

    public void setDropFrames(boolean dropFrames) {
        this.dropFrames = dropFrames;
    }

    public AeEntity getAeEntity() {
        return aeEntity;
    }

    public void setAeEntity(AeEntity aeEntity) {
        this.aeEntity = aeEntity;
    }

    public Integer getCodecType() {
        return codecType;
    }

    public void setCodecType(Integer codecType) {
        this.codecType = codecType;
    }
}
