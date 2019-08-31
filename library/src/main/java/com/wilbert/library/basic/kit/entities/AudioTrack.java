package com.wilbert.library.basic.kit.entities;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by Android Studio.
 * User: wilbert jw20082009@qq.com
 * Date: 2019/8/19 10:47
 */
public class AudioTrack implements Parcelable {
    private String filePath;
    private float volume;
    private float startTime;
    private float durationTime;

    protected AudioTrack(Parcel in) {
        filePath = in.readString();
        volume = in.readFloat();
        startTime = in.readFloat();
        durationTime = in.readFloat();
    }

    public static final Creator<AudioTrack> CREATOR = new Creator<AudioTrack>() {
        @Override
        public AudioTrack createFromParcel(Parcel in) {
            return new AudioTrack(in);
        }

        @Override
        public AudioTrack[] newArray(int size) {
            return new AudioTrack[size];
        }
    };

    public String getFilePath() {
        return filePath;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }

    public float getVolume() {
        return volume;
    }

    public void setVolume(float volume) {
        this.volume = volume;
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
        dest.writeFloat(volume);
        dest.writeFloat(startTime);
        dest.writeFloat(durationTime);
    }
}
