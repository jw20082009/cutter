
package com.wilbert.library.basic.entity;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.List;

/**
 * 预览模块的结果实体，依据该实体内容可完成：1.视频音轨更换，2.视频变速，3.视频多段合并
 */
public class ResultEntity implements Parcelable {

    private List<VideoEntity> videoEntities;

    private MusicEntity musicEntity;

    public ResultEntity() {
    }

    protected ResultEntity(Parcel in) {
        videoEntities = in.createTypedArrayList(VideoEntity.CREATOR);
        musicEntity = in.readParcelable(MusicEntity.class.getClassLoader());
    }

    public static final Creator<ResultEntity> CREATOR = new Creator<ResultEntity>() {
        @Override
        public ResultEntity createFromParcel(Parcel in) {
            return new ResultEntity(in);
        }

        @Override
        public ResultEntity[] newArray(int size) {
            return new ResultEntity[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeTypedList(videoEntities);
        dest.writeParcelable(musicEntity, flags);
    }

    public List<VideoEntity> getVideoEntities() {
        return videoEntities;
    }

    public void setVideoEntities(List<VideoEntity> videoEntities) {
        this.videoEntities = videoEntities;
    }

    public MusicEntity getMusicEntity() {
        return musicEntity;
    }

    public void setMusicEntity(MusicEntity musicEntity) {
        this.musicEntity = musicEntity;
    }
}
