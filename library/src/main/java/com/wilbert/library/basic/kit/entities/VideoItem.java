package com.wilbert.library.basic.kit.entities;

import android.os.Parcel;
import android.os.Parcelable;
import android.text.TextUtils;

/**
 * Created by Android Studio.
 * User: wilbert jw20082009@qq.com
 * Date: 2019/8/21 10:39
 */
public class VideoItem implements Parcelable {
    private String filePath;
    private float videoVolume;
    private float videoScale;
    private float offsetX;
    private float offsetY;
    private float startTime;
    private float durationTime;

    public VideoItem() {
    }

    protected VideoItem(Parcel in) {
        filePath = in.readString();
        videoVolume = in.readFloat();
        videoScale = in.readFloat();
        offsetX = in.readFloat();
        offsetY = in.readFloat();
        startTime = in.readFloat();
        durationTime = in.readFloat();
    }

    public boolean isImage() {
        if (TextUtils.isEmpty(filePath)) {
            if (filePath.endsWith("jpg") || filePath.endsWith("jpeg") || filePath.endsWith("png") || filePath.endsWith("bmp")) {
                return true;
            } else {

            }
        }
        return false;
    }

    public static final Creator<VideoItem> CREATOR = new Creator<VideoItem>() {
        @Override
        public VideoItem createFromParcel(Parcel in) {
            return new VideoItem(in);
        }

        @Override
        public VideoItem[] newArray(int size) {
            return new VideoItem[size];
        }
    };

    public String getFilePath() {
        return filePath;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }

    public float getVideoVolume() {
        return videoVolume;
    }

    public void setVideoVolume(float videoVolume) {
        this.videoVolume = videoVolume;
    }

    public float getVideoScale() {
        return videoScale;
    }

    public void setVideoScale(float videoScale) {
        this.videoScale = videoScale;
    }

    public float getOffsetX() {
        return offsetX;
    }

    public void setOffsetX(float offsetX) {
        this.offsetX = offsetX;
    }

    public float getOffsetY() {
        return offsetY;
    }

    public void setOffsetY(float offsetY) {
        this.offsetY = offsetY;
    }

    public float getStartTime() {
        return startTime;
    }

    public void setStartTime(float startTime) {
        this.startTime = startTime;
    }

    public float getDurationTime() {
        return durationTime;
    }

    public void setDurationTime(float durationTime) {
        this.durationTime = durationTime;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(filePath);
        dest.writeFloat(videoVolume);
        dest.writeFloat(videoScale);
        dest.writeFloat(offsetX);
        dest.writeFloat(offsetY);
        dest.writeFloat(startTime);
        dest.writeFloat(durationTime);
    }
}
