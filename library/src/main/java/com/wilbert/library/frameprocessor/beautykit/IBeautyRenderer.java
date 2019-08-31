
package com.wilbert.library.frameprocessor.beautykit;

import android.content.Context;

import com.wilbert.library.basic.aftereffect.IFaceDetector;
import com.wilbert.library.basic.entity.BeautifyEntity;

public interface IBeautyRenderer {
    void init(Context context, IFaceDetector faceDetector);

    void onResume();

    void onPause();

    void onDestroy();

    void onInputSizeChanged(int imageWidth, int imageHeight, final int cameraOrientation,
                            final int cameraId);

    void setBeautifyEffect(final BeautifyEntity effect);

    int onDrawFrame(int textureId, final byte[] mDataBuffer);
}
