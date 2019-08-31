
package com.wilbert.library.basic.aftereffect;

import android.graphics.SurfaceTexture;
import android.media.MediaPlayer;
import android.opengl.GLSurfaceView;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.view.Surface;
import android.view.Window;
import android.view.WindowManager;

import com.wilbert.library.basic.activity.BaseThreadHandlerActivity;
import com.wilbert.library.basic.renderer.OesRenderer;
import com.wilbert.library.basic.widgets.AspectGLSurfaceView;

import java.io.IOException;

public abstract class BaseVideoActivity extends BaseThreadHandlerActivity {

    protected AspectGLSurfaceView mSurfaceView;

    protected OesRenderer mRenderer;

    protected MediaPlayer mMediaPlayer;

    protected String mFilePath;

    protected SurfaceTexture mSurfaceTexture;

    protected boolean mHasPlayerPrepared = false;

    protected int mVideoWidth, mVideoHeight;

    protected abstract AspectGLSurfaceView getSurfaceView();

    protected abstract int getLayoutId();

    protected abstract String getFilePath();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(getLayoutId());
        initSurfaceView();
        mFilePath = getFilePath();
        initMediaPlayer();
    }

    @Override
    protected void onResume() {
        super.onResume();
        mSurfaceView.onResume();
        if (mVideoWidth > 0 && mVideoHeight > 0) {
            mRenderer.onInputSizeChanged(mVideoWidth, mVideoHeight);
        }
        if (mHasPlayerPrepared) {
            mMediaPlayer.start();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        mSurfaceView.onPause();
        if (mHasPlayerPrepared) {
            mMediaPlayer.pause();
        }
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

    protected void initMediaPlayer() {
        releasePlayer();
        mMediaPlayer = new MediaPlayer();
        if (!TextUtils.isEmpty(mFilePath)) {
            try {
                mMediaPlayer.setDataSource(mFilePath);
                mMediaPlayer.setOnPreparedListener(mPreparedListener);
                mMediaPlayer.setOnVideoSizeChangedListener(mVideoSizeListener);
                mMediaPlayer.setLooping(true);
                mMediaPlayer.prepareAsync();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    protected void releasePlayer() {
        if (mMediaPlayer != null) {
            mMediaPlayer.release();
            mMediaPlayer = null;
            mVideoWidth = 0;
            mVideoHeight = 0;
            mHasPlayerPrepared = false;
        }
    }

    protected void setMediaPlayerDisplay() {
        if (mMediaPlayer != null && mSurfaceTexture != null) {
            mMediaPlayer.setSurface(new Surface(mSurfaceTexture));
        }
    }

    protected MediaPlayer.OnVideoSizeChangedListener mVideoSizeListener = new MediaPlayer.OnVideoSizeChangedListener() {
        @Override
        public void onVideoSizeChanged(MediaPlayer mp, int width, int height) {
            mVideoWidth = width;
            mVideoHeight = height;
            mSurfaceView.onInputSizeChanged(width, height);
            mRenderer.onInputSizeChanged(width, height);
        }
    };

    protected MediaPlayer.OnPreparedListener mPreparedListener = new MediaPlayer.OnPreparedListener() {
        @Override
        public void onPrepared(MediaPlayer mp) {
            mHasPlayerPrepared = true;
            if (mSurfaceTexture != null) {
                setMediaPlayerDisplay();
            }
            mMediaPlayer.start();
        }
    };

    protected OesRenderer.TextureListener mTextureListener = new OesRenderer.TextureListener() {
        @Override
        public void onSurfaceTextureCreated(SurfaceTexture surfaceTexture) {
            mSurfaceTexture = surfaceTexture;
            if (mHasPlayerPrepared) {
                setMediaPlayerDisplay();
            }
        }
    };
}
