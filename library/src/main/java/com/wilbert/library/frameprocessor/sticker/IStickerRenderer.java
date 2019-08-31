package com.wilbert.library.frameprocessor.sticker;

import android.content.Context;

import com.wilbert.library.basic.entity.StickerEntity;

public interface IStickerRenderer {

    void init(Context context, StickerEntity stickerEntity);

    void onInputSizeChanged(int imageWidth, int imageHeight);

    int onDrawFrame(int textureId, long currentTime);

    void onDestroy();
}
