package com.wilbert.library.basic.aftereffect;

import android.media.MediaPlayer;
import android.text.TextUtils;

import com.wilbert.library.basic.entity.AeEntity;
import com.wilbert.library.basic.entity.MusicEntity;
import com.wilbert.library.basic.renderer.FilterRenderer;
import com.wilbert.library.basic.renderer.OesRenderer;
import com.wilbert.library.basic.widgets.stickerview.AnimateStickerView;

import java.io.IOException;

public abstract class BaseAeVideoFragment extends BaseVideoFragment {
    protected AeEntity mAeEntity;
    protected MediaPlayer mMusicPlayer;
    protected boolean mHasMusicPlayerPrepared = false;

    @Override
    protected String getFilePath() {
        mAeEntity = getAeEntity();
        String filePath = obtainFilePath();
        if (mAeEntity.getSpeedEntity() != null) {
            filePath = mAeEntity.getSpeedEntity().getFilePath();
        }
        initRenderer();
        return filePath;
    }

    protected void initRenderer(){
    }

    @Override
    protected OesRenderer getRenderer() {
        return new FilterRenderer(mSurfaceView);
    }

    @Override
    protected void onVideoPrepared(MediaPlayer mediaPlayer) {
        super.onVideoPrepared(mediaPlayer);
        if (getStickerView() != null) {
            getStickerView().setMediaPlayer(mediaPlayer);
        }
    }

    @Override
    protected void onVideoReleasePlay() {
        super.onVideoReleasePlay();
        if (getStickerView() != null)
            getStickerView().setMediaPlayer(null);
    }

    /**
     * 获取全关键帧视频，主要用于预览帧序列
     *
     * @return
     */
    protected String obtainFilePath() {
        if (!TextUtils.isEmpty(mAeEntity.getIframePath())) {
            return mAeEntity.getIframePath();
        } else {
            return mAeEntity.getFilePath();
        }
    }

    @Override
    protected void refreshView() {
        super.refreshView();
        if (mAeEntity != null && mAeEntity.getMusicEntity() != null)
            initMusicPlayer(mAeEntity.getMusicEntity());
    }

    protected abstract AeEntity getAeEntity();

    protected abstract AnimateStickerView getStickerView();

    protected void initMusicPlayer(final MusicEntity musicEntity) {
        releaseMusicPlayer();
        mMusicPlayer = new MediaPlayer();
        try {
            mMusicPlayer.setDataSource(musicEntity.filePath);
            mMusicPlayer.prepareAsync();
            mMusicPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                @Override
                public void onPrepared(MediaPlayer mp) {
                    if (mMusicPlayer != null) {
                        mMusicPlayer.seekTo(musicEntity.selectedStartMs);
                        float volume = 1.0f * musicEntity.volume / 100;
                        mMusicPlayer.setVolume(volume, volume);
                        mMusicPlayer.start();
                        mHasMusicPlayerPrepared = true;
                    }
                }
            });
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    protected void releaseMusicPlayer() {
        if (mMusicPlayer != null) {
            mMusicPlayer.release();
            mMusicPlayer = null;
            mHasMusicPlayerPrepared = false;
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if (mMusicPlayer != null && mHasMusicPlayerPrepared) {
            mMusicPlayer.start();
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        if (mMusicPlayer != null && mHasMusicPlayerPrepared) {
            mMusicPlayer.pause();
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        releaseMusicPlayer();
        if (getStickerView() != null) {
            getStickerView().stopRunning();
            getStickerView().clear();
        }
    }
}
