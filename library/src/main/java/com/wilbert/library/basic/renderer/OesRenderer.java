
package com.wilbert.library.basic.renderer;

import android.graphics.SurfaceTexture;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.util.Log;

import com.wilbert.library.frameprocessor.gles.GLImageOESInputFilter;
import com.wilbert.library.frameprocessor.gles.OpenGLUtils;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

/**
 * created by jw200 at 2019/3/14 16:27
 **/
public class OesRenderer extends BaseRenderer implements SurfaceTexture.OnFrameAvailableListener {
    private final String TAG = "OesRenderer";

    protected SurfaceTexture mSurfaceTexture;

    protected final float[] mSTMatrix = new float[16];

    protected boolean mHasTextureCreated = false;

    public OesRenderer(GLSurfaceView context) {
        super(context);
    }

    @Override
    public void onSurfaceCreated(GL10 gl, EGLConfig config) {
        super.onSurfaceCreated(gl, config);
        synchronized (mLock) {
            mInputTexture = createInputTexture();
            if (mInputTexture != OpenGLUtils.GL_NOT_TEXTURE) {
                mSurfaceTexture = new SurfaceTexture(mInputTexture);
                mHasTextureCreated = true;
                mSurfaceTexture.setOnFrameAvailableListener(this);
                onSurfaceTextureCreated(mSurfaceTexture);
            } else {
                Log.i(TAG, "createInputTexture failed");
            }
        }
    }

    @Override
    public void onDrawFrame(GL10 gl) {
        Log.i("releaseCheck", "onVideoPlayCompleted4");
        if (needRefreshFrame()) {
            mSurfaceTexture.updateTexImage();
        }
        super.onDrawFrame(gl);
    }

    protected boolean needRefreshFrame() {
        return true;
    }

    @Override
    protected void beforeDrawFrame() {
        super.beforeDrawFrame();
        mSurfaceTexture.getTransformMatrix(mSTMatrix);
        ((GLImageOESInputFilter) mInputFilter).setTextureTransformMatrix(mSTMatrix);
    }

    @Override
    public int createInputTexture() {
        mInputTexture = OpenGLUtils.createOESTexture();
        return mInputTexture;
    }

    @Override
    protected GLImageOESInputFilter initInputFilter() {
        if (mInputFilter == null) {
            mInputFilter = new GLImageOESInputFilter(mContext);
        } else {
            mInputFilter.initProgramHandle();
        }
        return (GLImageOESInputFilter) mInputFilter;
    }

    protected void onSurfaceTextureCreated(SurfaceTexture surfaceTexture) {
        notifyTextureCreated(surfaceTexture);
    }

    @Override
    protected void onChildFilterSizeChanged() {
        super.onChildFilterSizeChanged();
    }

    @Override
    protected void _release() {
        Log.e("OesRenderer", "release");
        synchronized (mLock) {
            mHasTextureCreated = false;
            if (mInputTexture != OpenGLUtils.GL_NOT_TEXTURE) {
                Log.e("OesRenderer", "release1:" + Thread.currentThread().getName());
                GLES20.glDeleteTextures(1, new int[]{
                        mInputTexture
                }, 0);
                Log.e("OesRenderer", "release2" + Thread.currentThread().getName());
                mInputTexture = OpenGLUtils.GL_NOT_TEXTURE;
            }
        }
        super._release();
    }

    @Override
    public void onFrameAvailable(SurfaceTexture surfaceTexture) {
        if (mSurfaceView != null) {
            mSurfaceView.requestRender();
        }
    }

    public enum ScaleType {
        CENTER_INSIDE, CENTER_CROP, FIT_XY
    }

    public void notifyTextureCreated(SurfaceTexture surfaceTexture) {
        if (mTextureListener != null) {
            mTextureListener.onSurfaceTextureCreated(surfaceTexture);
        }
    }

    TextureListener mTextureListener;

    public void setTextureListener(TextureListener listener) {
        this.mTextureListener = listener;
        if (mHasTextureCreated && mSurfaceTexture != null) {
            notifyTextureCreated(mSurfaceTexture);
        }
    }

    public interface TextureListener {
        void onSurfaceTextureCreated(SurfaceTexture surfaceTexture);
    }
}
