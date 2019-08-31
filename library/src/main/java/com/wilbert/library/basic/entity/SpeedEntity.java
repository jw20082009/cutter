package com.wilbert.library.basic.entity;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * 后期变速特效
 */
public class SpeedEntity implements Parcelable {

    private Speeds speeds;
    private int startTime;
    private int duration;
    private String filePath;

    public SpeedEntity() {
    }

    protected SpeedEntity(Parcel in) {
        startTime = in.readInt();
        duration = in.readInt();
        speeds = Speeds.parseSpeeds(in.readString());
        filePath = in.readString();
    }

    public static final Creator<SpeedEntity> CREATOR = new Creator<SpeedEntity>() {
        @Override
        public SpeedEntity createFromParcel(Parcel in) {
            return new SpeedEntity(in);
        }

        @Override
        public SpeedEntity[] newArray(int size) {
            return new SpeedEntity[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeInt(startTime);
        parcel.writeInt(duration);
        parcel.writeString(speeds.name);
        parcel.writeString(filePath);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null || !(obj instanceof SpeedEntity)) {
            return false;
        }
        SpeedEntity objEntity = (SpeedEntity) obj;
        if (objEntity.speeds == speeds && objEntity.startTime == startTime && objEntity.duration == duration) {
            return true;
        }
        return super.equals(obj);
    }

    public Speeds getSpeeds() {
        return speeds;
    }

    /**
     * 速度
     */
    public void setSpeeds(Speeds speeds) {
        this.speeds = speeds;
    }

    public int getStartTime() {
        return startTime;
    }

    /**
     * 特效开始时间
     */
    public void setStartTime(int startTime) {
        this.startTime = startTime;
    }

    public int getDuration() {
        return duration;
    }

    /**
     * 特效时长
     */
    public void setDuration(int duration) {
        this.duration = duration;
    }

    public String getFilePath() {
        return filePath;
    }

    /**
     * 后期变速特效后的文件地址
     */
    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }
}
