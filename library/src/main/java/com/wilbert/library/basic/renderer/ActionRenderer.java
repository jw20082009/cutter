package com.wilbert.library.basic.renderer;

import android.media.MediaPlayer;
import android.opengl.GLSurfaceView;
import android.util.Log;

import com.wilbert.library.basic.entity.ActionEntity;
import com.wilbert.library.basic.entity.Actions;
import com.wilbert.library.frameprocessor.gles.GLImageFilter;
import com.wilbert.library.frameprocessor.gles.OpenGLUtils;
import com.wilbert.library.frameprocessor.gles.effect.GLImageEffectIllusionFilter;
import com.wilbert.library.frameprocessor.gles.effect.GLImageEffectMultiNineFilter;
import com.wilbert.library.frameprocessor.gles.effect.GLImageEffectShakeFilter;
import com.wilbert.library.frameprocessor.gles.effect.GLImageEffectSoulStuffFilter;

import java.nio.FloatBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.LinkedBlockingQueue;

public class ActionRenderer extends FilterRenderer {
    private final String TAG = "ActionRenderer";
    private GLImageEffectSoulStuffFilter effectSoulStuffFilter;
    private GLImageEffectMultiNineFilter multiFilter;
    private GLImageEffectIllusionFilter illusionFilter;
    private GLImageEffectShakeFilter shakeFilter;
    private ActionEntity soulStuffEntity;
    private ActionEntity multiEntity;
    private ActionEntity illusionEntity;
    private ActionEntity shakeEntity;
    protected MediaPlayer mediaPlayer;

    private Queue<GLImageFilter> actionFilters = new LinkedBlockingQueue<>();

    public ActionRenderer(GLSurfaceView context) {
        super(context);
    }

    public void setMediaPlayer(MediaPlayer player) {
        this.mediaPlayer = player;
    }

    @Override
    protected void onChildFilterSizeChanged() {
        super.onChildFilterSizeChanged();
        for (GLImageFilter filter : actionFilters) {
            filter.onInputSizeChanged(mIncommingWidth, mIncommingHeight);
            filter.initFrameBuffer(mIncommingWidth, mIncommingHeight);
        }
    }

    @Override
    protected void onDisplaySizeChanged(int width, int height) {
        super.onDisplaySizeChanged(width, height);
        for (GLImageFilter filter : actionFilters) {
            filter.onDisplaySizeChanged(width, height);
        }
    }

    @Override
    protected int onDrawFrameBuffer(int textureId, FloatBuffer vertexBuffer, FloatBuffer textureBuffer) {
        textureId = super.onDrawFrameBuffer(textureId, vertexBuffer, textureBuffer);
        if (textureId != OpenGLUtils.GL_NOT_TEXTURE && mediaPlayer != null) {
            long currentTime = 0;
            try {
                currentTime = mediaPlayer.getCurrentPosition();
            } catch (IllegalStateException e) {
                e.printStackTrace();
            }
            if (checkCanAction(soulStuffEntity, currentTime)) {
                if (effectSoulStuffFilter == null) {
                    effectSoulStuffFilter = new GLImageEffectSoulStuffFilter();
                    effectSoulStuffFilter.onDisplaySizeChanged(mSurfaceWidth, mSurfaceHeight);
                    effectSoulStuffFilter.onInputSizeChanged(mIncommingWidth, mIncommingHeight);
                    effectSoulStuffFilter.initFrameBuffer(mIncommingWidth, mIncommingHeight);
                    actionFilters.offer(effectSoulStuffFilter);
                }
                effectSoulStuffFilter.setCurrentPosition(currentTime);
                textureId = effectSoulStuffFilter.drawFrameBuffer(textureId, vertexBuffer, textureBuffer);
            } else if (checkCanAction(illusionEntity, currentTime)) {
                if (illusionFilter == null) {
                    illusionFilter = new GLImageEffectIllusionFilter();
                    illusionFilter.onDisplaySizeChanged(mSurfaceWidth, mSurfaceHeight);
                    illusionFilter.onInputSizeChanged(mIncommingWidth, mIncommingHeight);
                    illusionFilter.initFrameBuffer(mIncommingWidth, mIncommingHeight);
                    actionFilters.offer(illusionFilter);
                }
                textureId = illusionFilter.drawFrameBuffer(textureId, vertexBuffer, textureBuffer);
            } else if (checkCanAction(shakeEntity, currentTime)) {
                if (shakeFilter == null) {
                    shakeFilter = new GLImageEffectShakeFilter(mContext);
                    shakeFilter.onDisplaySizeChanged(mSurfaceWidth, mSurfaceHeight);
                    shakeFilter.onInputSizeChanged(mIncommingWidth, mIncommingHeight);
                    shakeFilter.initFrameBuffer(mIncommingWidth, mIncommingHeight);
                    actionFilters.offer(shakeFilter);
                }
                shakeFilter.setCurrentPosition(currentTime);
                textureId = shakeFilter.drawFrameBuffer(textureId, vertexBuffer, textureBuffer);
            }
            if (checkCanAction(multiEntity, currentTime)) {
                if (multiFilter == null) {
                    multiFilter = new GLImageEffectMultiNineFilter();
                    multiFilter.onDisplaySizeChanged(mSurfaceWidth, mSurfaceHeight);
                    multiFilter.onInputSizeChanged(mIncommingWidth, mIncommingHeight);
                    multiFilter.initFrameBuffer(mIncommingWidth, mIncommingHeight);
                    actionFilters.offer(multiFilter);
                }
                textureId = multiFilter.drawFrameBuffer(textureId, vertexBuffer, textureBuffer);
            }
        }
        return textureId;
    }

    public boolean checkCanAction(ActionEntity actionEntity, long currentTime) {
        Log.i(TAG, "checkCanAction " + currentTime);
        if (actionEntity != null && currentTime >= actionEntity.startTime && currentTime < (actionEntity.startTime + actionEntity.durationTime)) {
            return true;
        }
        return false;
    }

    public void setActionEntities(List<ActionEntity> actionEntities) {
        if (actionEntities != null) {
            for (ActionEntity actionEntity : actionEntities) {
                setActionEntity(actionEntity);
            }
        }
    }

    public void clearActions() {
        soulStuffEntity = null;
        shakeEntity = null;
        illusionEntity = null;
        multiEntity = null;
    }

    public void setActionEntity(ActionEntity actionEntity) {
        if (actionEntity != null) {
            Actions actions = actionEntity.action;
            switch (actions) {
                case LingHunChuQiao:
                    soulStuffEntity = actionEntity;
                    shakeEntity = null;
                    illusionEntity = null;
                    break;
                case FenLie:
                    multiEntity = actionEntity;
                    break;
                case GuangBo:
                    soulStuffEntity = null;
                    shakeEntity = actionEntity;
                    illusionEntity = null;
                    break;
                case HuanJue:
                    soulStuffEntity = null;
                    shakeEntity = null;
                    illusionEntity = actionEntity;
                    break;
            }
        }
    }

    public List<ActionEntity> getActionEntities() {
        List<ActionEntity> actionEntities = new ArrayList<>();
        if (soulStuffEntity != null)
            actionEntities.add(soulStuffEntity);
        if (multiEntity != null) {
            actionEntities.add(multiEntity);
        }
        if (shakeEntity != null) {
            actionEntities.add(shakeEntity);
        }
        if (illusionEntity != null) {
            actionEntities.add(illusionEntity);
        }
        return actionEntities;
    }

    @Override
    protected void _release() {
        mediaPlayer = null;
        super._release();
        GLImageFilter filter = null;
        while ((filter = actionFilters.poll()) != null) {
            filter.release();
        }
        effectSoulStuffFilter = null;
        multiFilter = null;
        illusionFilter = null;
        shakeFilter = null;
    }
}
