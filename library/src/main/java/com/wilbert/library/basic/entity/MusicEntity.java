
package com.wilbert.library.basic.entity;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * 后期音乐特效
 */
public class MusicEntity implements Parcelable {

    /**
     * 音乐文件总时长
     */
    public int fileDuration;

    /**
     * 音乐文件地址
     */
    public String filePath;

    /**
     * 专辑名
     */
    public String album;

    /**
     * 歌手名
     */
    public String artist;

    /**
     * 音乐选中部分的开始，单位：ms
     */
    public int selectedStartMs;

    /**
     * 音乐音量
     */
    public int volume = 100;

    /**
     * 视频音量
     */
    public int videoVolume = 20;

    public MusicEntity() {
    }

    protected MusicEntity(Parcel in) {
        fileDuration = in.readInt();
        filePath = in.readString();
        album = in.readString();
        artist = in.readString();
        selectedStartMs = in.readInt();
        volume = in.readInt();
        videoVolume = in.readInt();
    }

    public static final Creator<MusicEntity> CREATOR = new Creator<MusicEntity>() {
        @Override
        public MusicEntity createFromParcel(Parcel in) {
            return new MusicEntity(in);
        }

        @Override
        public MusicEntity[] newArray(int size) {
            return new MusicEntity[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(fileDuration);
        dest.writeString(filePath);
        dest.writeString(album);
        dest.writeString(artist);
        dest.writeInt(selectedStartMs);
        dest.writeInt(volume);
        dest.writeInt(videoVolume);
    }
}
