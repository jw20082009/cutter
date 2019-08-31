package com.wilbert.library.basic.kit.entities;

import android.os.Parcel;
import android.os.Parcelable;
import android.util.JsonReader;

/**
 * Created by Android Studio.
 * User: wilbert jw20082009@qq.com
 * Date: 2019/8/21 10:45
 */
public class Filter extends BaseEffect implements Parcelable {
    private float strength;
    private float startTime;
    private float durationTime;
    private String filePath;

    public Filter() {
    }

    protected Filter(Parcel in) {
        strength = in.readFloat();
        startTime = in.readFloat();
        durationTime = in.readFloat();
        filePath = in.readString();
    }

    public static final Creator<Filter> CREATOR = new Creator<Filter>() {
        @Override
        public Filter createFromParcel(Parcel in) {
            return new Filter(in);
        }

        @Override
        public Filter[] newArray(int size) {
            return new Filter[size];
        }
    };

    public float getStrength() {
        return strength;
    }

    public void setStrength(float strength) {
        this.strength = strength;
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

    public String getFilePath() {
        return filePath;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeFloat(strength);
        dest.writeFloat(startTime);
        dest.writeFloat(durationTime);
        dest.writeString(filePath);
    }

    @Override
    public int getType() {
        return type;
    }

    public static Filter parse(JsonReader jsonReader){
        Filter filter = null;
        if (jsonReader != null) {

        }
        return filter;
    }
}
