
package com.wilbert.library.basic.aftereffect;

import android.content.Context;

public interface IFaceDetector {

    void init(Context context);

    void onInputSizeChanged(int width, int height, int cameraOrientation, int cameraId);

    Object[] onDrawFrame(final byte[] mDataBuffer);

    void onPause();

    void onDestroy();
}
