package com.wilbert.library.basic.aftereffect;

import android.graphics.SurfaceTexture;
import android.media.MediaPlayer;
import android.opengl.GLSurfaceView;
import android.os.Bundle;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Surface;
import android.view.View;
import android.view.ViewGroup;

import com.wilbert.library.basic.fragments.BaseThreadHandlerFragment;
import com.wilbert.library.basic.widgets.IAspectView;
import com.wilbert.library.basic.renderer.OesRenderer;
import com.wilbert.library.basic.widgets.AspectGLSurfaceView;

import java.io.File;
import java.io.IOException;

public abstract class BaseVideoFragment extends BaseThreadHandlerFragment {
    private final String TAG = "BaseVideoFragment";
    protected View mLayoutView;

    protected AspectGLSurfaceView mSurfaceView;

    protected IAspectView mAspectView;

    protected OesRenderer mRenderer;

    protected MediaPlayer mMediaPlayer;

    protected String mPlayingVideoPath;

    protected SurfaceTexture mSurfaceTexture;

    protected Surface mPreviewSurface;

    protected boolean mHasPlayerPrepared = false;

    protected int mVideoWidth, mVideoHeight;

    protected abstract AspectGLSurfaceView getSurfaceView();

    protected abstract int getLayoutId();

    protected abstract String getFilePath();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        mLayoutView = inflater.inflate(getLayoutId(), container, false);
        findViews();
        initSurfaceView();
        mAspectView = getAspectView();
        refreshView();
        return mLayoutView;
    }

    protected IAspectView getAspectView() {
        return mSurfaceView;
    }

    protected void findViews() {
    }

    protected void refreshView() {
        mPlayingVideoPath = getFilePath();
        tryInitMediaPlayer(mPlayingVideoPath);
        initView(mLayoutView);
    }

    protected void initView(View rootView) {
    }

    @Override
    public void onResume() {
        super.onResume();
        mSurfaceView.onResume();
        if (mVideoWidth > 0 && mVideoHeight > 0) {
            mRenderer.onInputSizeChanged(mVideoWidth, mVideoHeight);
        }
        startPlay();
    }

    protected void startPlay() {
        if (mMediaPlayer != null && mHasPlayerPrepared) {
            mMediaPlayer.start();
            onVideoStartPlay();
        }
    }

    protected void onVideoStartPlay() {
    }

    protected void pausePlay() {
        if (mMediaPlayer != null && mHasPlayerPrepared && mMediaPlayer.isPlaying()) {
            mMediaPlayer.pause();
            onVideoPausePlay();
        }
    }

    protected void onVideoPausePlay() {
    }

    protected void releasePlayer() {
        if (mMediaPlayer != null) {
            mMediaPlayer.release();
            mMediaPlayer = null;
            mVideoWidth = 0;
            mVideoHeight = 0;
            mHasPlayerPrepared = false;
            onVideoReleasePlay();
        }
    }

    protected void onVideoReleasePlay() {
    }

    @Override
    public void onPause() {
        super.onPause();
        mSurfaceView.onPause();
        pausePlay();
        mSurfaceView.queueEvent(new Runnable() {
            @Override
            public void run() {
                mRenderer.release();
            }
        });
    }

    protected OesRenderer getRenderer() {
        return new OesRenderer(mSurfaceView);
    }

    protected void initSurfaceView() {
        mSurfaceView = getSurfaceView();
        mSurfaceView.setEGLContextClientVersion(2);
        mRenderer = getRenderer();
        mRenderer.setTextureListener(mTextureListener);
        mSurfaceView.setRenderer(mRenderer);
        mSurfaceView.setRenderMode(GLSurfaceView.RENDERMODE_WHEN_DIRTY);
    }

    protected void tryInitMediaPlayer(String filePath) {
        releasePlayer();
        mMediaPlayer = new MediaPlayer();
        if (!TextUtils.isEmpty(filePath)) {
            if (new File(filePath).canRead()) {
                initMediaPlayer(filePath);
            } else {
                if (retryTimes <= 3) {
                    Log.i(TAG, "file can not Read retry times " + retryTimes + ";" + filePath);
                    retryTimes++;
                    sendEmptyUIMessageDelay(MSG_UI_RETRY_PLAY, 30);
                }
            }
        }
    }

    protected void initMediaPlayer(String filePath) {
        onInputFileReady(filePath);
        try {
            mMediaPlayer.setDataSource(filePath);
            mMediaPlayer.setOnPreparedListener(mPreparedListener);
            mMediaPlayer.setOnVideoSizeChangedListener(mVideoSizeListener);
            mMediaPlayer.setOnInfoListener(mVideoInfoListener);
            mMediaPlayer.setOnSeekCompleteListener(mSeekCompleteListener);
            mMediaPlayer.setOnCompletionListener(mCompleteListener);
            mMediaPlayer.setLooping(true);
            mMediaPlayer.prepareAsync();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    protected void onInputFileReady(String filePath) {

    }

    private final int MSG_UI_RETRY_PLAY = 0x01;

    private int retryTimes = 0;

    @Override
    protected void handleUIMessage(Message message) {
        super.handleUIMessage(message);
        switch (message.what) {
            case MSG_UI_RETRY_PLAY:
                tryInitMediaPlayer(mPlayingVideoPath);
                break;
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        releasePlayer();
    }

    protected void setMediaPlayerDisplay(MediaPlayer mediaPlayer) {
        if (mediaPlayer != null && mSurfaceTexture != null) {
            Log.i(TAG, "setMediaPlayerDisplay mMediaPlayer setSurface:" + mSurfaceTexture.hashCode());
            if (mPreviewSurface == null) {
                mPreviewSurface = new Surface(mSurfaceTexture);
            }
            mediaPlayer.setSurface(mPreviewSurface);
        }
    }

    protected MediaPlayer.OnCompletionListener mCompleteListener = new MediaPlayer.OnCompletionListener() {
        @Override
        public void onCompletion(MediaPlayer mp) {
            BaseVideoFragment.this.onCompletion(mp);
        }
    };

    protected void onCompletion(MediaPlayer mp) {
    }

    protected MediaPlayer.OnInfoListener mVideoInfoListener = new MediaPlayer.OnInfoListener() {
        @Override
        public boolean onInfo(MediaPlayer mediaPlayer, int what, int extra) {
            return BaseVideoFragment.this.onInfo(mediaPlayer, what, extra);
        }
    };

    protected boolean onInfo(MediaPlayer mediaPlayer, int what, int extra) {
        return false;
    }

    protected MediaPlayer.OnSeekCompleteListener mSeekCompleteListener = new MediaPlayer.OnSeekCompleteListener() {
        @Override
        public void onSeekComplete(MediaPlayer mediaPlayer) {
            BaseVideoFragment.this.onSeekComplete(mediaPlayer);
        }
    };

    protected void onSeekComplete(MediaPlayer mediaPlayer) {
    }

    protected MediaPlayer.OnVideoSizeChangedListener mVideoSizeListener = new MediaPlayer.OnVideoSizeChangedListener() {
        @Override
        public void onVideoSizeChanged(MediaPlayer mp, int width, int height) {
            Log.i(TAG, "onVideoSizeChanged:" + (mSurfaceTexture == null ? "mSurfaceTexture == null" : mSurfaceTexture.hashCode()));
            mVideoWidth = width;
            mVideoHeight = height;
            mAspectView.onInputSizeChanged(width, height);
            mRenderer.onInputSizeChanged(width, height);
        }
    };

    protected MediaPlayer.OnPreparedListener mPreparedListener = new MediaPlayer.OnPreparedListener() {
        @Override
        public void onPrepared(MediaPlayer mp) {
            mHasPlayerPrepared = true;
            onVideoPrepared(mp);
            if (mSurfaceTexture != null) {
                setMediaPlayerDisplay(mp);
            }
            startPlay();
        }
    };

    protected void onVideoPrepared(MediaPlayer mediaPlayer) {
    }

    protected OesRenderer.TextureListener mTextureListener = new OesRenderer.TextureListener() {
        @Override
        public void onSurfaceTextureCreated(SurfaceTexture surfaceTexture) {
            mSurfaceTexture = surfaceTexture;
            if (mHasPlayerPrepared) {
                setMediaPlayerDisplay(mMediaPlayer);
            }
        }
    };
}
