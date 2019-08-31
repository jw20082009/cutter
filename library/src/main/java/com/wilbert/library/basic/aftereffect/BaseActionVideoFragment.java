package com.wilbert.library.basic.aftereffect;

import android.util.Log;

import com.wilbert.library.basic.entity.ActionEntity;
import com.wilbert.library.basic.entity.StickerEntity;
import com.wilbert.library.basic.utils.AeResultHelper;
import com.wilbert.library.basic.renderer.ActionRenderer;
import com.wilbert.library.basic.renderer.StickerRenderer;

import java.util.List;

public abstract class BaseActionVideoFragment extends BaseAeVideoFragment {

    @Override
    protected StickerRenderer getRenderer() {
        return new StickerRenderer(mSurfaceView);
    }

    @Override
    protected void initRenderer() {
        super.initRenderer();
        List<ActionEntity> actionEntities = mAeEntity.getActionEntities();
        if (actionEntities != null) {
            ((ActionRenderer) mRenderer).setActionEntities(actionEntities);
        } else {
            ((ActionRenderer) mRenderer).clearActions();
        }
        List<StickerEntity> stickerEntities = AeResultHelper.getResultStickers(mAeEntity);
        if (stickerEntities != null) {
            ((StickerRenderer) mRenderer).setStickerEntities(stickerEntities);
        } else {
            ((StickerRenderer) mRenderer).clearStickers();
        }
    }

    @Override
    protected void onVideoStartPlay() {
        super.onVideoStartPlay();
        Log.i("playcheck", "BaseActionVideoFragment onVideoStartPlay " + (mMediaPlayer == null) + ";" + (mRenderer == null));
        if (mMediaPlayer != null && mRenderer != null) {
            ((ActionRenderer) mRenderer).setMediaPlayer(mMediaPlayer);
        }
    }

    @Override
    protected void onVideoReleasePlay() {
        super.onVideoReleasePlay();
        Log.i("playcheck", "BaseActionVideoFragment onVideoReleasePlay " + (mMediaPlayer == null) + ";" + (mRenderer == null));
        if (mRenderer != null) {
            ((ActionRenderer) mRenderer).setMediaPlayer(null);
        }
    }
}
