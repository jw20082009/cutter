package com.wilbert.library.basic.utils;

import com.wilbert.library.basic.entity.AeEntity;
import com.wilbert.library.basic.entity.StickerEntity;

import java.util.ArrayList;
import java.util.List;

public class AeResultHelper {

    public static List<StickerEntity> getResultStickers(AeEntity entity) {
        if (entity == null)
            return null;
        List<StickerEntity> resultStickers = new ArrayList<>();
        List<StickerEntity> stickers = entity.getStickerEntities();
        List<StickerEntity> subtitles = entity.getSubTitleEntities();
        List<StickerEntity> watermarkers = entity.getWaterMarkers();
        if (stickers != null) {
            resultStickers.addAll(stickers);
        }
        if (subtitles != null) {
            resultStickers.addAll(subtitles);
        }
        if (watermarkers != null) {
            resultStickers.addAll(watermarkers);
        }
        return resultStickers;
    }
}
