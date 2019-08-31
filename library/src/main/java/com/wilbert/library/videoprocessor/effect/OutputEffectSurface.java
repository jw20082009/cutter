package com.wilbert.library.videoprocessor.effect;

import android.content.Context;
import android.graphics.SurfaceTexture;
import android.view.Surface;

import com.wilbert.library.basic.entity.ActionEntity;
import com.wilbert.library.basic.entity.StickerEntity;

import java.util.List;

public class OutputEffectSurface implements IOutputSurface, SurfaceTexture.OnFrameAvailableListener {

    private Context mContext;
    private int mVideoWidth, mVideoHeight, mSurfaceWidth, mSurfaceHeight;
    private IRenderer mTextureRender;
    private SurfaceTexture mSurfaceTexture;
    private Surface mSurface;
    private final Object mFrameSyncObject = new Object();
    private boolean mFrameAvailable;
    private int mType = 0;

    public OutputEffectSurface(Context context, int videoWidth, int videoHeight) {
        super();
        this.mContext = context;
        this.mVideoWidth = videoWidth;
        this.mVideoHeight = videoHeight;
        setup();
        if (mTextureRender != null) {
            mTextureRender.onInputSizeChanged(mVideoWidth, mVideoHeight);
        }
    }

    public OutputEffectSurface(Context context, int videoWidth, int videoHeight, int resultWidth, int resultHeight) {
        super();
        this.mContext = context;
        this.mVideoWidth = videoWidth;
        this.mVideoHeight = videoHeight;
        this.mSurfaceWidth = resultWidth;
        this.mSurfaceHeight = resultHeight;
        setup();
        if (mTextureRender != null) {
            mTextureRender.onSurfaceChanged(resultWidth, resultHeight);
            mTextureRender.onInputSizeChanged(mVideoWidth, mVideoHeight);
        }
    }

    /**
     * @param context
     * @param videoWidth   源视频宽度
     * @param videoHeight  源视频高度
     * @param resultWidth  输出视频宽度
     * @param resultHeight 输出视频高度
     * @param type         1.使用native渲染方式 0：使用surface方式
     */
    public OutputEffectSurface(Context context, int videoWidth, int videoHeight, int resultWidth, int resultHeight, int type) {
        super();
        this.mContext = context;
        this.mVideoWidth = videoWidth;
        this.mVideoHeight = videoHeight;
        this.mSurfaceWidth = resultWidth;
        this.mSurfaceHeight = resultHeight;
        this.mType = type;
        setup();
        if (mTextureRender != null) {
            mTextureRender.onSurfaceChanged(resultWidth, resultHeight);
            mTextureRender.onInputSizeChanged(mVideoWidth, mVideoHeight);
        }
    }

    public void initDecodeTime(int duration, int currentTime) {
        if (mTextureRender != null && mTextureRender instanceof ActionRenderer) {
            ((ActionRenderer) mTextureRender).setTime(duration, currentTime);
        }
    }

    public void setActionEntities(List<ActionEntity> actionEntities) {
        if (mTextureRender != null && mTextureRender instanceof ActionRenderer) {
            ((ActionRenderer) mTextureRender).setActionEntities(actionEntities);
        }
    }

    public void setStickerEntities(List<StickerEntity> stickerEntities) {
        if (mTextureRender != null && mTextureRender instanceof ActionRenderer) {
            ((ActionRenderer) mTextureRender).setStickerEntities(stickerEntities);
        }
    }

    @Override
    public void awaitNewImage() {
        final int TIMEOUT_MS = 5000;
        synchronized (mFrameSyncObject) {
            while (!mFrameAvailable) {
                try {
                    mFrameSyncObject.wait(TIMEOUT_MS);
                    if (!mFrameAvailable) {
                        throw new RuntimeException("Surface frame wait timed out");
                    }
                } catch (InterruptedException ie) {
                    throw new RuntimeException(ie);
                }
            }
            mFrameAvailable = false;
        }
        mTextureRender.checkGlError("before updateTexImage");
        mSurfaceTexture.updateTexImage();
    }

    @Override
    public void drawImage(long timems, boolean invert) {
        if (mTextureRender instanceof ActionRenderer) {
            ((ActionRenderer) mTextureRender).tickTime((int) timems);
        }
        mTextureRender.drawFrame(mSurfaceTexture, invert);
    }

    @Override
    public Surface getSurface() {
        return mSurface;
    }

    @Override
    public void release() {
        mSurface.release();
        mTextureRender.release();
        mTextureRender = null;
        mSurface = null;
        mSurfaceTexture = null;
    }

    private void setup() {
        mTextureRender = getRenderer();
        mTextureRender.surfaceCreated();
        mSurfaceTexture = new SurfaceTexture(mTextureRender.getTextureId());
        mSurfaceTexture.setOnFrameAvailableListener(this);
        mSurface = new Surface(mSurfaceTexture);
    }

    private IRenderer getRenderer() {
        return new ActionRenderer(mContext);
    }

    public int getInputFrameBuffer() {
        return mTextureRender.getTextureId();
    }

    @Override
    public void onFrameAvailable(SurfaceTexture surfaceTexture) {
        synchronized (mFrameSyncObject) {
            if (mFrameAvailable) {
                throw new RuntimeException("mFrameAvailable already set, frame could be dropped");
            }
            mFrameAvailable = true;
            mFrameSyncObject.notifyAll();
        }
    }
}
