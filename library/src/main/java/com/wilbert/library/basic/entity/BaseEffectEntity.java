package com.wilbert.library.basic.entity;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * 后期特效基类，封装了时间参数
 */
public class BaseEffectEntity implements Parcelable {

    /**
     * 特效开始时间
     */
    public int startTime;

    /**
     * 特效时长
     */
    public int durationTime;

    public BaseEffectEntity() {
    }

    protected BaseEffectEntity(Parcel in) {
        startTime = in.readInt();
        durationTime = in.readInt();
    }

    public static final Creator<BaseEffectEntity> CREATOR = new Creator<BaseEffectEntity>() {
        @Override
        public BaseEffectEntity createFromParcel(Parcel in) {
            return new BaseEffectEntity(in);
        }

        @Override
        public BaseEffectEntity[] newArray(int size) {
            return new BaseEffectEntity[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(startTime);
        dest.writeInt(durationTime);
    }

    public int getStartTime() {
        return startTime;
    }

    public void setStartTime(int startTime) {
        this.startTime = startTime;
    }

    public int getDurationTime() {
        return durationTime;
    }

    public void setDurationTime(int durationTime) {
        this.durationTime = durationTime;
    }
}
