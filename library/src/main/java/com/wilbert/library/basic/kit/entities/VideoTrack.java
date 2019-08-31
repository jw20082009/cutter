package com.wilbert.library.basic.kit.entities;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.List;

/**
 * Created by Android Studio.
 * User: wilbert jw20082009@qq.com
 * Date: 2019/8/19 10:47
 */
public class VideoTrack implements Parcelable {

    private String headerVideoFilePath;
    private String tailerVideoFilePath;
    private int outWidth;
    private int outHeight;
    private List<VideoItem> videoItems;

    protected VideoTrack(Parcel in) {
        headerVideoFilePath = in.readString();
        tailerVideoFilePath = in.readString();
        outWidth = in.readInt();
        outHeight = in.readInt();
        videoItems = in.createTypedArrayList(VideoItem.CREATOR);
    }

    public static final Creator<VideoTrack> CREATOR = new Creator<VideoTrack>() {
        @Override
        public VideoTrack createFromParcel(Parcel in) {
            return new VideoTrack(in);
        }

        @Override
        public VideoTrack[] newArray(int size) {
            return new VideoTrack[size];
        }
    };

    public String getHeaderVideoFilePath() {
        return headerVideoFilePath;
    }

    public void setHeaderVideoFilePath(String headerVideoFilePath) {
        this.headerVideoFilePath = headerVideoFilePath;
    }

    public String getTailerVideoFilePath() {
        return tailerVideoFilePath;
    }

    public void setTailerVideoFilePath(String tailerVideoFilePath) {
        this.tailerVideoFilePath = tailerVideoFilePath;
    }

    public int getOutWidth() {
        return outWidth;
    }

    public void setOutWidth(int outWidth) {
        this.outWidth = outWidth;
    }

    public int getOutHeight() {
        return outHeight;
    }

    public void setOutHeight(int outHeight) {
        this.outHeight = outHeight;
    }

    public List<VideoItem> getVideoItems() {
        return videoItems;
    }

    public void setVideoItems(List<VideoItem> videoItems) {
        this.videoItems = videoItems;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(headerVideoFilePath);
        dest.writeString(tailerVideoFilePath);
        dest.writeInt(outWidth);
        dest.writeInt(outHeight);
        dest.writeTypedList(videoItems);
    }
}
