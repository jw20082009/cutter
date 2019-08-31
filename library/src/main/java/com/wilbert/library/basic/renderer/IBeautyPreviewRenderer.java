package com.wilbert.library.basic.renderer;

import com.wilbert.library.frameprocessor.beautykit.IBeautyRenderer;
import com.wilbert.library.basic.entity.BeautifyEntity;

public interface IBeautyPreviewRenderer extends IPreviewRenderer {

    void setBeautyRenderer(IBeautyRenderer mBeautyRenderer);

    void setBeautifyEffect(BeautifyEntity entity);
}
