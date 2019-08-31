package com.wilbert.library.basic.kit.entities;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by Android Studio.
 * User: wilbert jw20082009@qq.com
 * Date: 2019/8/21 10:33
 */
public class SubTitle extends BaseEffect implements Parcelable {
    private String text;
    private int textSize;
    private int maxLines;
    private int maxWordsPerLine;
    private String textColor;
    private int rotate;
    private float scale;
    private float offsetX;
    private float offsetY;
    private float startTime;
    private float durationTime;

    protected SubTitle(Parcel in) {
        text = in.readString();
        textSize = in.readInt();
        maxLines = in.readInt();
        maxWordsPerLine = in.readInt();
        textColor = in.readString();
        rotate = in.readInt();
        scale = in.readFloat();
        offsetX = in.readFloat();
        offsetY = in.readFloat();
        startTime = in.readFloat();
        durationTime = in.readFloat();
    }

    public static final Creator<SubTitle> CREATOR = new Creator<SubTitle>() {
        @Override
        public SubTitle createFromParcel(Parcel in) {
            return new SubTitle(in);
        }

        @Override
        public SubTitle[] newArray(int size) {
            return new SubTitle[size];
        }
    };

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public int getTextSize() {
        return textSize;
    }

    public void setTextSize(int textSize) {
        this.textSize = textSize;
    }

    public int getMaxLines() {
        return maxLines;
    }

    public void setMaxLines(int maxLines) {
        this.maxLines = maxLines;
    }

    public int getMaxWordsPerLine() {
        return maxWordsPerLine;
    }

    public void setMaxWordsPerLine(int maxWordsPerLine) {
        this.maxWordsPerLine = maxWordsPerLine;
    }

    public String getTextColor() {
        return textColor;
    }

    public void setTextColor(String textColor) {
        this.textColor = textColor;
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
        dest.writeString(text);
        dest.writeInt(textSize);
        dest.writeInt(maxLines);
        dest.writeInt(maxWordsPerLine);
        dest.writeString(textColor);
        dest.writeInt(rotate);
        dest.writeFloat(scale);
        dest.writeFloat(offsetX);
        dest.writeFloat(offsetY);
        dest.writeFloat(startTime);
        dest.writeFloat(durationTime);
    }

    @Override
    public int getType() {
        type = 4;
        return super.getType();
    }
}
