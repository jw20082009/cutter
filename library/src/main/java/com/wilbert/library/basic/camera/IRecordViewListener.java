package com.wilbert.library.basic.camera;

import com.wilbert.library.basic.entity.VideoEntity;

import java.util.List;

/**
 * Created by Android Studio.
 * User: wilbert jw20082009@qq.com
 * Date: 2019/8/26 17:47
 */
public interface IRecordViewListener {
    void deleteLastVideo();

    void onStartRecord(List<VideoEntity> recordedVideos);

    void onStopRecord();
}
