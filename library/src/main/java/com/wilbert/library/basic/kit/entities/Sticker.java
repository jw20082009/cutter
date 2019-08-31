package com.wilbert.library.basic.kit.entities;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by Android Studio.
 * User: wilbert jw20082009@qq.com
 * Date: 2019/8/21 10:36
 */
public class Sticker extends BaseEffect implements Parcelable {
    private String name;
    private String stickerId;
    private int stickerFps;
    private int rotate;
    private float scale;
    private float offsetX;
    private float offsetY;
    private String filePath;
    private float startTime;
    private float durationTime;

    protected Sticker(Parcel in) {
        name = in.readString();
        stickerId = in.readString();
        stickerFps = in.readInt();
        rotate = in.readInt();
        scale = in.readFloat();
        offsetX = in.readFloat();
        offsetY = in.readFloat();
        filePath = in.readString();
        startTime = in.readFloat();
        durationTime = in.readFloat();
    }

    public static final Creator<Sticker> CREATOR = new Creator<Sticker>() {
        @Override
        public Sticker createFromParcel(Parcel in) {
            return new Sticker(in);
        }

        @Override
        public Sticker[] newArray(int size) {
            return new Sticker[size];
        }
    };

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getStickerId() {
        return stickerId;
    }

    public void setStickerId(String stickerId) {
        this.stickerId = stickerId;
    }

    public int getStickerFps() {
        return stickerFps;
    }

    public void setStickerFps(int stickerFps) {
        this.stickerFps = stickerFps;
    }

    public int getRotate() {
        return rotate;
    }

    public void setRotate(int rotate) {
        this.rotate = rotate;
    }

    public float getScale() {
        return scale;
    }

    public void setScale(float scale) {
        this.scale = scale;
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

    public String getFilePath() {
        return filePath;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
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
        dest.writeString(name);
        dest.writeString(stickerId);
        dest.writeInt(stickerFps);
        dest.writeInt(rotate);
        dest.writeFloat(scale);
        dest.writeFloat(offsetX);
        dest.writeFloat(offsetY);
        dest.writeString(filePath);
        dest.writeFloat(startTime);
        dest.writeFloat(durationTime);
    }

    @Override
    public int getType() {
        type = 1;
        return super.getType();
    }
}
