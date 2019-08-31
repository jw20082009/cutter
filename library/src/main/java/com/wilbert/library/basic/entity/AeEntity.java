package com.wilbert.library.basic.entity;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.ArrayList;
import java.util.List;

/**
 * 后期处理的结果实体，封装了所有后期特效相关参数
 */
public class AeEntity implements Parcelable {

    private String filePath;
    private String iframePath;
    private String outPath;
    private int videoVolume = 20;
    private MusicEntity musicEntity;
    private SpeedEntity speedEntity;
    private List<ActionEntity> actionEntities = new ArrayList<>();
    private List<StickerEntity> waterMarkers = new ArrayList<>();
    private List<StickerEntity> stickerEntities = new ArrayList<>();
    private List<StickerEntity> subTitleEntities = new ArrayList<>();

    public AeEntity() {
    }

    protected AeEntity(Parcel in) {
        filePath = in.readString();
        iframePath = in.readString();
        outPath = in.readString();
        videoVolume = in.readInt();
        musicEntity = in.readParcelable(MusicEntity.class.getClassLoader());
        speedEntity = in.readParcelable(SpeedEntity.class.getClassLoader());
        actionEntities = in.readArrayList(ActionEntity.class.getClassLoader());
        in.readTypedList(actionEntities, ActionEntity.CREATOR);
        in.readTypedList(stickerEntities, StickerEntity.CREATOR);
        in.readTypedList(subTitleEntities,StickerEntity.CREATOR);
        in.readTypedList(waterMarkers,StickerEntity.CREATOR);
    }

    public static final Creator<AeEntity> CREATOR = new Creator<AeEntity>() {
        @Override
        public AeEntity createFromParcel(Parcel in) {
            return new AeEntity(in);
        }

        @Override
        public AeEntity[] newArray(int size) {
            return new AeEntity[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(filePath);
        dest.writeString(iframePath);
        dest.writeString(outPath);
        dest.writeInt(videoVolume);
        dest.writeParcelable(musicEntity, flags);
        dest.writeParcelable(speedEntity, flags);
        dest.writeTypedList(actionEntities);
        dest.writeTypedList(stickerEntities);
        dest.writeTypedList(subTitleEntities);
        dest.writeTypedList(waterMarkers);
    }

    public String getFilePath() {
        return filePath;
    }

    /**
     * 设置输入视频地址
     */
    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }

    public String getOutPath() {
        return outPath;
    }

    /**
     * 设置输出地址
     */
    public void setOutPath(String outPath) {
        this.outPath = outPath;
    }

    public int getVideoVolume() {
        return videoVolume;
    }

    /**
     * 设置视频音量
     */
    public void setVideoVolume(int videoVolume) {
        this.videoVolume = videoVolume;
    }

    public MusicEntity getMusicEntity() {
        return musicEntity;
    }

    /**
     * 后期特效之——音乐特效
     */
    public void setMusicEntity(MusicEntity musicEntity) {
        this.musicEntity = musicEntity;
    }

    public String getIframePath() {
        return iframePath;
    }

    /**
     * 设置全关键帧视频地址
     */
    public void setIframePath(String iframePath) {
        this.iframePath = iframePath;
    }

    public SpeedEntity getSpeedEntity() {
        return speedEntity;
    }

    /**
     * 后期特效之——变速特效
     */
    public void setSpeedEntity(SpeedEntity speedEntity) {
        this.speedEntity = speedEntity;
    }

    public List<ActionEntity> getActionEntities() {
        return actionEntities;
    }

    /**
     * 后期特效之——动作列表
     */
    public void setActionEntities(List<ActionEntity> actionEntities) {
        this.actionEntities = actionEntities;
    }

    public List<StickerEntity> getStickerEntities() {
        return stickerEntities;
    }

    /**
     * 后期特效之——贴纸列表
     */
    public void setStickerEntities(List<StickerEntity> stickerEntities) {
        this.stickerEntities = stickerEntities;
    }

    public List<StickerEntity> getSubTitleEntities() {
        return subTitleEntities;
    }

    /**
     * 后期特效之——字幕列表
     */
    public void setSubTitleEntities(List<StickerEntity> subTitleEntities) {
        this.subTitleEntities = subTitleEntities;
    }

    public List<StickerEntity> getWaterMarkers() {
        return waterMarkers;
    }

    /**
     * 后期特效之——水印列表
     * @param waterMarkers
     */
    public void setWaterMarkers(List<StickerEntity> waterMarkers) {
        this.waterMarkers = waterMarkers;
    }
}
