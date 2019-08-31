package com.wilbert.library.basic.aftereffect;

import com.wilbert.library.basic.entity.StickerEntity;

import java.util.List;

public interface IStickerListener {
    void onLoadSuccess(List<StickerEntity> stickerEntities);
}
