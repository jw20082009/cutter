package com.wilbert.library.basic.kit.entities;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by Android Studio.
 * User: wilbert jw20082009@qq.com
 * Date: 2019/8/21 15:49
 */
public class Action extends BaseEffect implements Parcelable {
    private int actionId;
    private float startTime;
    private float durationTime;

    protected Action(Parcel in) {
        actionId = in.readInt();
        startTime = in.readFloat();
        durationTime = in.readFloat();
    }

    public static final Creator<Action> CREATOR = new Creator<Action>() {
        @Override
        public Action createFromParcel(Parcel in) {
            return new Action(in);
        }

        @Override
        public Action[] newArray(int size) {
            return new Action[size];
        }
    };

    public int getActionId() {
        return actionId;
    }

    public void setActionId(int actionId) {
        this.actionId = actionId;
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
        dest.writeInt(actionId);
        dest.writeFloat(startTime);
        dest.writeFloat(durationTime);
    }

    @Override
    public int getType() {
        type = 2;
        return super.getType();
    }
}
