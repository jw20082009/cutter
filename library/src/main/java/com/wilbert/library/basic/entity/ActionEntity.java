package com.wilbert.library.basic.entity;

import android.os.Parcel;

/**
 * 后期动作{@link Actions}：egg:灵魂出窍，视频分裂，动感光波，幻觉
 */
public class ActionEntity extends BaseEffectEntity {

    public Actions action;

    public ActionEntity() {
    }

    protected ActionEntity(Parcel in) {
        super(in);
        action = Actions.parseActions(in.readInt());
    }

    public static final Creator<ActionEntity> CREATOR = new Creator<ActionEntity>() {
        @Override
        public ActionEntity createFromParcel(Parcel in) {
            return new ActionEntity(in);
        }

        @Override
        public ActionEntity[] newArray(int size) {
            return new ActionEntity[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        super.writeToParcel(dest, flags);
        if (action != null)
            dest.writeInt(action.ordinal());
    }
}
