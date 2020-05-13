package com.wilbert.library.videoprocessor.effect;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.SurfaceTexture;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.opengl.Matrix;
import android.util.Log;

import com.wilbert.library.basic.entity.BaseEffectEntity;
import com.wilbert.library.frameprocessor.gles.GLImageFilter;
import com.wilbert.library.frameprocessor.gles.GLImageInputFilter;
import com.wilbert.library.frameprocessor.gles.GLImageOESInputFilter;
import com.wilbert.library.frameprocessor.gles.OpenGLUtils;
import com.wilbert.library.frameprocessor.gles.TextureRotationUtils;

import java.nio.FloatBuffer;

public class BaseRenderer implements SurfaceTexture.OnFrameAvailableListener, IRenderer {
    protected final String TAG = "BaseRenderer2";
    protected Context mContext;
    protected GLSurfaceView mSurfaceView;
    protected GLImageFilter mInputFilter;
    protected GLImageFilter mOutputFilter;
    protected FloatBuffer mVertexBuffer;
    protected FloatBuffer mTextureBuffer;
    protected int mInputTexture = OpenGLUtils.GL_NOT_TEXTURE;
    protected int mImageTexture = OpenGLUtils.GL_NOT_TEXTURE;
    protected int mIncommingWidth, mIncommingHeight;
    protected int mSurfaceWidth, mSurfaceHeight;
    protected boolean mHasInputSizeChanged = false, mHasSurfaceChanged = false;
    protected Object mLock = new Object();
    protected boolean mHasFrameBufferInited = false;
    protected boolean mHasTextureCreated = false;
    protected final float[] mSTMatrix = new float[16];
    protected long currentTime = 0;
    protected long duration = 0;
    protected Bitmap bitmap;
    protected boolean hasBitmapChanged = false;
    protected GLImageInputFilter mImageInputFilter;

    public BaseRenderer(Context context) {
        this.mContext = context;
        this.mHasSurfaceChanged = false;
        Matrix.setIdentityM(mSTMatrix, 0);
    }

    @Override
    public int getTextureId() {
        return mInputTexture;
    }

    @Override
    public void surfaceCreated() {
        mInputFilter = initInputFilter();
        mOutputFilter = initOutputFilter();
        mVertexBuffer = OpenGLUtils.createFloatBuffer(TextureRotationUtils.CubeVertices);
        mTextureBuffer = OpenGLUtils.createFloatBuffer(TextureRotationUtils.TextureVertices);
        mInputTexture = createInputTexture();
    }

    private long getCurrentTime() {
        return currentTime;
    }

    public void setTime(int duration, int currentTime) {
        this.currentTime = currentTime;
        this.duration = duration;
    }

    public void tickTime(int currentTime) {
        this.currentTime = currentTime;
    }

    public boolean checkCanAction(BaseEffectEntity effectEntity, long currentTime) {
        Log.i(TAG, "checkCanAction " + currentTime);
        if (effectEntity != null && currentTime >= effectEntity.startTime && currentTime < (effectEntity.startTime + effectEntity.durationTime)) {
            return true;
        }
        return false;
    }

    @Override
    public void onInputSizeChanged(int width, int height) {
        synchronized (mLock) {
            mHasInputSizeChanged = true;
            mIncommingWidth = width;
            mIncommingHeight = height;
        }
    }

    @Override
    public void drawFrame(SurfaceTexture st, boolean invert) {
        if ((mInputFilter == null && mImageInputFilter == null) || mOutputFilter == null)
            return;
        st.updateTexImage();

        int currentTexture = mInputTexture;
        if (hasBitmapChanged && bitmap != null) {
            hasBitmapChanged = false;
            if (mImageInputFilter == null) {
                mImageInputFilter = new GLImageInputFilter(mContext);
                mImageInputFilter.onDisplaySizeChanged(mSurfaceWidth, mSurfaceHeight);
            }
            if (mImageTexture != OpenGLUtils.GL_NOT_TEXTURE) {
                OpenGLUtils.deleteTexture(mImageTexture);
                mImageTexture = OpenGLUtils.GL_NOT_TEXTURE;
            }
            mImageTexture = OpenGLUtils.createTexture(bitmap, mImageTexture);
        }
        if (mHasInputSizeChanged && mImageInputFilter != null) {
            mImageInputFilter.onInputSizeChanged(mIncommingWidth, mIncommingHeight);
            mImageInputFilter.initFrameBuffer(mIncommingWidth, mIncommingHeight);
        }
        if (bitmap != null) {
            currentTexture = mImageTexture;
        }

        onFilterSizeChanged();
        beforeDrawFrame();
        st.getTransformMatrix(mSTMatrix);
        if (mInputFilter instanceof GLImageOESInputFilter)
            ((GLImageOESInputFilter) mInputFilter).setTextureTransformMatrix(mSTMatrix);
        if (mInputTexture != OpenGLUtils.GL_NOT_TEXTURE && mHasFrameBufferInited) {
            if (bitmap != null) {
                currentTexture = mImageInputFilter.drawFrameBuffer(currentTexture, mVertexBuffer, mTextureBuffer);
            } else if (mInputFilter != null) {
                currentTexture = mInputFilter.drawFrameBuffer(currentTexture, mVertexBuffer,
                        mTextureBuffer);
            }
            int nextTextureId = onDrawFrameBuffer(currentTexture, mVertexBuffer, mTextureBuffer);
            if (nextTextureId != OpenGLUtils.GL_NOT_TEXTURE) {
                currentTexture = nextTextureId;
            }
            mOutputFilter.drawFrame(currentTexture, mVertexBuffer, mTextureBuffer);
        } else {
            Log.e(TAG, "onDrawFrame GL_NOT_TEXTURE");
        }
    }

    public void onSurfaceChanged(int width, int height) {
        GLES20.glViewport(0, 0, width, height);
        synchronized (mLock) {
            mHasSurfaceChanged = true;
        }
        mSurfaceWidth = width;
        mSurfaceHeight = height;
        onDisplaySizeChanged(width, height);
        onFilterSizeChanged();
    }

    protected int onDrawFrameBuffer(int textureId, FloatBuffer vertexBuffer,
                                    FloatBuffer textureBuffer) {
        return textureId;
    }

    protected void beforeDrawFrame() {
    }

    protected int createInputTexture() {
        mInputTexture = OpenGLUtils.createOESTexture();
        return mInputTexture;
    }

    protected GLImageFilter initInputFilter() {
        if (mInputFilter == null) {
            mInputFilter = new GLImageOESInputFilter(mContext);
        } else {
            mInputFilter.initProgramHandle();
        }
        return mInputFilter;
    }

    protected GLImageFilter initOutputFilter() {
        if (mOutputFilter == null) {
            mOutputFilter = new GLImageFilter(mContext);
        } else {
            mOutputFilter.initProgramHandle();
        }
        return mOutputFilter;
    }

    protected void onFilterSizeChanged() {
        if (mHasInputSizeChanged) {
            synchronized (mLock) {
                if (mHasInputSizeChanged) {
                    mHasInputSizeChanged = false;
                    mHasFrameBufferInited = true;
                    onChildFilterSizeChanged();
                    if (mInputFilter != null) {
                        mInputFilter.onInputSizeChanged(mIncommingWidth, mIncommingHeight);
                        mInputFilter.initFrameBuffer(mIncommingWidth, mIncommingHeight);
                    }
                    if (mOutputFilter != null) {
                        mOutputFilter.onInputSizeChanged(mIncommingWidth, mIncommingHeight);
                    }
                }
            }
        }
    }

    protected void onChildFilterSizeChanged() {
    }

    protected void onDisplaySizeChanged(int width, int height) {
        if (mOutputFilter != null) {
            mOutputFilter.onDisplaySizeChanged(width, height);
        }
        if (mInputFilter != null) {
            mInputFilter.onDisplaySizeChanged(width, height);
        }
    }

    public void release() {
        synchronized (mLock) {
            mHasInputSizeChanged = false;
            mHasSurfaceChanged = false;
            mHasFrameBufferInited = false;
            mInputTexture = OpenGLUtils.GL_NOT_TEXTURE;
            synchronized (mLock) {
                mHasTextureCreated = false;
                if (mInputTexture != OpenGLUtils.GL_NOT_TEXTURE) {
                    GLES20.glDeleteTextures(1, new int[]{
                            mInputTexture
                    }, 0);
                    mInputTexture = OpenGLUtils.GL_NOT_TEXTURE;
                }
            }
        }
        if (bitmap != null) {
            bitmap = null;
        }
        if (mImageTexture != OpenGLUtils.GL_NOT_TEXTURE) {
            OpenGLUtils.deleteTexture(mImageTexture);
            mImageTexture = OpenGLUtils.GL_NOT_TEXTURE;
        }
        if (mImageInputFilter != null) {
            mImageInputFilter.release();
            mImageInputFilter = null;
        }
        if (mInputFilter != null) {
            mInputFilter.release();
            mInputFilter = null;
        }
        if (mOutputFilter != null) {
            mOutputFilter.release();
            mOutputFilter = null;
        }
        if (mVertexBuffer != null) {
            mVertexBuffer.clear();
            mVertexBuffer = null;
        }
        if (mTextureBuffer != null) {
            mTextureBuffer.clear();
            mTextureBuffer = null;
        }
    }

    public void setBitmap(Bitmap bitmap) {
        Log.i(TAG, "setBitmap");
        if (bitmap != this.bitmap) {
            this.bitmap = bitmap;
            hasBitmapChanged = true;
        }
    }

    public void clearBitmap() {
        Log.i(TAG, "clearBitmap");
        if (this.bitmap != null) {
            this.bitmap = null;
        }
    }

    @Override
    public void onFrameAvailable(SurfaceTexture surfaceTexture) {

    }

    @Override
    public void checkGlError(String op) {
        int error;
        if ((error = GLES20.glGetError()) != GLES20.GL_NO_ERROR) {
            throw new RuntimeException(op + ": glError " + error);
        }
    }
}
