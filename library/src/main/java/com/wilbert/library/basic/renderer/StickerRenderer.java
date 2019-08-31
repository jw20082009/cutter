package com.wilbert.library.basic.renderer;

import android.opengl.GLSurfaceView;
import android.util.Log;

import com.wilbert.library.basic.entity.StickerEntity;
import com.wilbert.library.frameprocessor.sticker.StickerFilter;

import java.util.List;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

public class StickerRenderer extends ActionRenderer {
    private final String TAG = "StickerRenderer";
    private boolean needDrawLog = true;
    StickerFilter stickerFilter;
    List<StickerEntity> stickerEntities;

    public StickerRenderer(GLSurfaceView context) {
        super(context);
    }

    public void setStickerEntities(List<StickerEntity> stickerEntities) {
        this.stickerEntities = stickerEntities;
        if (stickerFilter != null) {
            stickerFilter.setStickerEntities(stickerEntities);
        }
    }

    @Override
    public void onSurfaceCreated(GL10 gl, EGLConfig config) {
        super.onSurfaceCreated(gl, config);
        stickerFilter = new StickerFilter(mContext);
        stickerFilter.setStickerEntities(stickerEntities);
        stickerFilter.onInputSizeChanged(mIncommingWidth, mIncommingHeight);
    }

    @Override
    public void onInputSizeChanged(int width, int height) {
        super.onInputSizeChanged(width, height);
        Log.i(TAG, "onInputSizeChanged，width:" + width + ";height:" + height + ";" + hashCode());
        if (stickerFilter != null)
            stickerFilter.onInputSizeChanged(width, height);
    }

    @Override
    protected void onDisplaySizeChanged(int width, int height) {
        super.onDisplaySizeChanged(width, height);
        Log.i(TAG, "onDisplaySizeChanged，width:" + width + ";height:" + height + ";" + hashCode());
        if (stickerFilter != null)
            stickerFilter.onDisplaySizeChanged(width, height);
    }

    @Override
    public void onDrawFrame(GL10 gl) {
        super.onDrawFrame(gl);
        if (mediaPlayer != null) {
            int time = 0;
            try {
                time = mediaPlayer.getCurrentPosition();
            } catch (IllegalStateException e) {
                e.printStackTrace();
            }
            if (needDrawLog) {
                needDrawLog = false;
                Log.i(TAG, "onDrawFrame" + ";" + hashCode());
            }
            stickerFilter.drawFrame(mVertexBuffer, mTextureBuffer, time);
        }
    }

    public void clearStickers() {
        if (stickerEntities != null) {
            stickerEntities.clear();
            stickerEntities = null;
        }
    }

    @Override
    protected void _release() {
        super._release();
        if (stickerFilter != null)
            stickerFilter.release();
    }
}
