package com.wilbert.library.basic.renderer;

import com.wilbert.library.recorder.interfaces.IVideoEncoder;
public interface IRecordablePreviewRenderer extends IBeautyPreviewRenderer {
    void setVideoEncoder(IVideoEncoder encoder);
}
