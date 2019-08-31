
package com.wilbert.library.frameprocessor.beautykit;

import android.content.Context;

import com.wilbert.library.basic.aftereffect.IFaceDetector;
import com.wilbert.library.basic.entity.BeautifyEntity;
import com.wilbert.library.basic.utils.FilterIniter;

public class BeautyRenderer implements IBeautyRenderer {

    protected BeautifyEntity beautifyEffect = null;

    protected Context mContext;

    protected boolean hasBeautifyChanged = false;

    protected Object beautifyLock = new Object();

    protected int mImageWidth, mImageHeight;

    protected boolean mHasWrapperInited = false;

    IFaceDetector faceDetector;

    @Override
    public void init(Context context, IFaceDetector faceDetector) {
        this.mContext = context;
        this.faceDetector = faceDetector;
        FilterIniter.init(mContext, null);
        if(faceDetector != null){
            faceDetector.init(mContext);
        }
    }

    @Override
    public void setBeautifyEffect(final BeautifyEntity effect) {
        if (mContext == null || !checkNeedRefreshEffect(effect)) {
            return;
        }
        synchronized (beautifyLock) {
            hasBeautifyChanged = true;
        }
        this.beautifyEffect = effect;
    }

    protected boolean checkNeedRefreshEffect(BeautifyEntity beautifyEffect) {
        if (beautifyEffect == null)
            return false;
        return true;
    }

    @Override
    public void onPause() {
        unInitWrapper();
        if(faceDetector != null){
            faceDetector.onPause();
        }
    }

    @Override
    public void onDestroy() {
        if(faceDetector != null){
            faceDetector.onDestroy();
        }
    }

    @Override
    public void onResume() {
        synchronized (beautifyLock) {
            hasBeautifyChanged = true;
        }
    }

    @Override
    public void onInputSizeChanged(int imageWidth, int imageHeight, final int cameraOrientation,
                                   final int cameraId) {
        mImageWidth = imageWidth;
        mImageHeight = imageHeight;
        if(faceDetector != null){
            faceDetector.onInputSizeChanged(imageWidth,imageHeight,cameraOrientation,cameraId);
        }
    }

    protected void initWrapper() {
            mHasWrapperInited = true;
    }

    protected void unInitWrapper() {
        mHasWrapperInited = false;
        hasBeautifyChanged = true;
    }

    @Override
    public int onDrawFrame(int textureId, final byte[] mDataBuffer) {
        return textureId;
    }

}
