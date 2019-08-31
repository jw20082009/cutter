package com.wilbert.library.basic.kit.entities;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by Android Studio.
 * User: wilbert jw20082009@qq.com
 * Date: 2019/8/21 10:43
 */
public class Transition extends BaseEffect implements Parcelable {
    private int transitionId;
    private int durationTime;
    private int startTime;

    protected Transition(Parcel in) {
        transitionId = in.readInt();
        durationTime = in.readInt();
        startTime = in.readInt();
    }

    public static final Creator<Transition> CREATOR = new Creator<Transition>() {
        @Override
        public Transition createFromParcel(Parcel in) {
            return new Transition(in);
        }

        @Override
        public Transition[] newArray(int size) {
            return new Transition[size];
        }
    };

    public int getTransitionId() {
        return transitionId;
    }

    public void setTransitionId(int transitionId) {
        this.transitionId = transitionId;
    }

    public int getDurationTime() {
        return durationTime;
    }

    public void setDurationTime(int durationTime) {
        this.durationTime = durationTime;
    }

    public int getStartTime() {
        return startTime;
    }

    public void setStartTime(int startTime) {
        this.startTime = startTime;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(transitionId);
        dest.writeInt(durationTime);
        dest.writeInt(startTime);
    }

    @Override
    public int getType() {
        type = 3;
        return super.getType();
    }
}
