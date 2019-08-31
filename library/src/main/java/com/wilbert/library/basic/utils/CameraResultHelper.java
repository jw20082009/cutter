package com.wilbert.library.basic.utils;

import android.content.Context;
import android.media.MediaMetadataRetriever;

import com.wilbert.library.basic.base.IProcessListener;
import com.wilbert.library.basic.entity.MusicEntity;
import com.wilbert.library.basic.entity.ResultEntity;
import com.wilbert.library.basic.entity.VideoEntity;
import com.wilbert.library.videoprocessor.NoDecodeVideoProcessor;
import com.wilbert.library.videoprocessor.VideoProcessor;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class CameraResultHelper {

    public static void handleResult(final Context context, final ResultEntity resultEntity, final int codecType, final IProcessListener listener) {
        if (resultEntity == null || resultEntity.getVideoEntities() == null)
            return;
        new Thread(new Runnable() {
            @Override
            public void run() {
                ArrayList<String> mp4list = new ArrayList<>();
                List<VideoEntity> mVideoEntities = resultEntity.getVideoEntities();
                MusicEntity mMusicEntity = resultEntity.getMusicEntity();
                for (VideoEntity videoEntity : mVideoEntities) {
                    if (videoEntity.getSpeed() != null && videoEntity.getSpeed().speed != 1.0f) {
                        String speedFile = OutFileGenerator.generateSpeedFile(context, videoEntity.getFilePath(),
                                videoEntity.getSpeed().speed);
                        try {
                            if (codecType == 1) {
                                MediaMetadataRetriever retriever = new MediaMetadataRetriever();
                                retriever.setDataSource(videoEntity.getFilePath());
                                String durationStr = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION);
                                int duration = Integer.parseInt(durationStr);
                                VideoProcessor.speedVideoNoDecode(videoEntity.getFilePath(), speedFile, 0, duration * 1000, videoEntity.getSpeed().speed, null);
                            } else {
                                VideoProcessor.changeVideoSpeed(context, videoEntity.getFilePath(), speedFile,
                                        videoEntity.getSpeed().speed);
                            }
                            mp4list.add(speedFile);
                        } catch (Exception e) {
                            e.printStackTrace();
                            mp4list.add(videoEntity.getFilePath());
                        }
                    } else {
                        mp4list.add(videoEntity.getFilePath());
                    }
                }
                String result = null;
                if (mp4list != null && mp4list.size() > 1) {
                    String mergeFile = OutFileGenerator.generateMergeFile(context,
                            mp4list);
                    result = mergeFile;
                } else if (mp4list != null && mp4list.size() == 1) {
                    result = mp4list.get(0);
                }
                if (mMusicEntity != null) {
                    String remuxfile = OutFileGenerator.generateMusicFile(context, result);
                    try {
                        VideoProcessor.mixAudioTrack(context, result,
                                mMusicEntity.filePath, remuxfile, null, null,
                                mMusicEntity.selectedStartMs, 0, mMusicEntity.volume, 1,
                                1);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    result = remuxfile;
                }
                if (listener != null) {
                    listener.onSuccess(result);
                } else {
                    listener.onFailed("");
                }
            }
        }).start();
    }
}
