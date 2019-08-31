
package com.wilbert.library.basic.entity;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * 视频实体，封装了视频路径和变速信息
 */
public class VideoEntity implements Parcelable {

    /**
     * 视频文件路径
     */
    private String filePath;

    /**
     * 视频速度 {@link Speed}
     */
    private Speed mSpeed;

    public VideoEntity() {
    }

    protected VideoEntity(Parcel in) {
        filePath = in.readString();
        mSpeed = Speed.parseFromIndex(in.readInt());
    }

    public static final Creator<VideoEntity> CREATOR = new Creator<VideoEntity>() {
        @Override
        public VideoEntity createFromParcel(Parcel in) {
            return new VideoEntity(in);
        }

        @Override
        public VideoEntity[] newArray(int size) {
            return new VideoEntity[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(filePath);
        dest.writeInt(mSpeed.ordinal());
    }

    public String getFilePath() {
        return filePath;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }

    public Speed getSpeed() {
        return mSpeed;
    }

    public void setSpeed(Speed mSpeed) {
        this.mSpeed = mSpeed;
    }
}
