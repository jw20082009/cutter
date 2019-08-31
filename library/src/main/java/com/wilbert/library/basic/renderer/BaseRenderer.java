
package com.wilbert.library.basic.renderer;

import android.content.Context;
import android.graphics.Bitmap;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.util.Log;

import com.wilbert.library.frameprocessor.gles.GLImageFilter;
import com.wilbert.library.frameprocessor.gles.GLImageInputFilter;
import com.wilbert.library.frameprocessor.gles.OpenGLUtils;
import com.wilbert.library.frameprocessor.gles.TextureRotationUtils;

import java.nio.FloatBuffer;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

public abstract class BaseRenderer implements GLSurfaceView.Renderer {

    private final String TAG = "BaseRenderer2";

    protected Context mContext;

    protected GLSurfaceView mSurfaceView;

    protected GLImageFilter mInputFilter;

    protected GLImageFilter mOutputFilter;

    protected FloatBuffer mVertexBuffer;

    protected FloatBuffer mTextureBuffer;

    protected FloatBuffer mAnotherTextureBuffer;

    protected int mInputTexture = OpenGLUtils.GL_NOT_TEXTURE;

    protected int mImageTexture = OpenGLUtils.GL_NOT_TEXTURE;

    protected int mIncommingWidth, mIncommingHeight;

    protected int mSurfaceWidth, mSurfaceHeight;

    protected boolean mHasInputSizeChanged = false, mHasSurfaceChanged = false;

    protected Object mLock = new Object();

    protected boolean mHasFrameBufferInited = false;

    protected Bitmap bitmap;

    protected boolean hasBitmapChanged = false;

    protected GLImageInputFilter mImageInputFilter;

    protected boolean mReleaseing = false;

    public Context getContext() {
        return mContext;
    }

    public void refreshFrame() {
        if (mSurfaceView != null) {
            mSurfaceView.requestRender();
        }
    }

    public BaseRenderer(GLSurfaceView surfaceView) {
        this.mSurfaceView = surfaceView;
        this.mContext = surfaceView.getContext();
        this.mHasSurfaceChanged = false;
    }

    @Override
    public void onSurfaceCreated(GL10 gl, EGLConfig config) {
        mInputFilter = initInputFilter();
        mOutputFilter = initOutputFilter();
        mVertexBuffer = OpenGLUtils.createFloatBuffer(TextureRotationUtils.CubeVertices);
        mTextureBuffer = OpenGLUtils.createFloatBuffer(TextureRotationUtils.TextureVertices);
    }

    @Override
    public void onSurfaceChanged(GL10 gl, int width, int height) {
        GLES20.glViewport(0, 0, width, height);
        Log.e(TAG, "onSurfaceChanged:" + width + ";" + height);
        synchronized (mLock) {
            mHasSurfaceChanged = true;
        }
        mSurfaceWidth = width;
        mSurfaceHeight = height;
        onDisplaySizeChanged(width, height);
        onFilterSizeChanged();
    }

    @Override
    public void onDrawFrame(GL10 gl) {
        if (mReleaseing) {
            _release();
        } else {
            int currentTexture = mInputTexture;
            FloatBuffer currentTextureBuffer = mAnotherTextureBuffer == null ? mTextureBuffer : mAnotherTextureBuffer;
            boolean hasImageFilterInited = false;
            if (hasBitmapChanged && bitmap != null) {
                hasBitmapChanged = false;
                if (mImageInputFilter == null) {
                    mImageInputFilter = new GLImageInputFilter(mContext);
                    mImageInputFilter.onDisplaySizeChanged(mSurfaceWidth, mSurfaceHeight);
                    if (mIncommingWidth > 0 && mIncommingHeight > 0) {
                        mImageInputFilter.onInputSizeChanged(mIncommingWidth, mIncommingHeight);
                        mImageInputFilter.initFrameBuffer(mIncommingWidth, mIncommingHeight);
                        hasImageFilterInited = true;
                    }
                }
                if (mImageTexture != OpenGLUtils.GL_NOT_TEXTURE) {
                    OpenGLUtils.deleteTexture(mImageTexture);
                    mImageTexture = OpenGLUtils.GL_NOT_TEXTURE;
                }
                mImageTexture = OpenGLUtils.createTexture(bitmap, mImageTexture);
            }
            if (mHasInputSizeChanged && mImageInputFilter != null && !hasImageFilterInited) {
                Log.i(TAG, "bitmap != null mImageInputFilter.initFrameBuffer：" + mIncommingWidth + ";" + mIncommingHeight);
                mImageInputFilter.onInputSizeChanged(mIncommingWidth, mIncommingHeight);
                mImageInputFilter.initFrameBuffer(mIncommingWidth, mIncommingHeight);
            }
            if (bitmap != null) {
                Log.i(TAG, "mAnotherTextureBuffer == null：" + (mAnotherTextureBuffer == null));
                currentTexture = mImageTexture;
            }
            if ((mInputFilter == null && mImageInputFilter == null) || mOutputFilter == null)
                return;
            onFilterSizeChanged();
            beforeDrawFrame();
            if (mInputTexture != OpenGLUtils.GL_NOT_TEXTURE && mHasFrameBufferInited) {
                if (bitmap != null) {
                    Log.i(TAG, "bitmap != null mImageInputFilter.drawFrameBuffer1：" + currentTexture);
                    currentTexture = mImageInputFilter.drawFrameBuffer(currentTexture, mVertexBuffer, currentTextureBuffer);
                    Log.i(TAG, "bitmap != null mImageInputFilter.drawFrameBuffer2：" + currentTexture);
                } else if (mInputFilter != null) {
                    currentTexture = mInputFilter.drawFrameBuffer(currentTexture, mVertexBuffer, currentTextureBuffer);
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
    }

    protected int onDrawFrameBuffer(int textureId, FloatBuffer vertexBuffer,
                                    FloatBuffer textureBuffer) {
        return textureId;
    }

    protected void beforeDrawFrame() {

    }

    public abstract int createInputTexture();

    protected GLImageFilter initInputFilter() {
        if (mInputFilter == null) {
            mInputFilter = new GLImageFilter(mContext);
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

    public void onInputSizeChanged(int width, int height) {
        synchronized (mLock) {
            mHasInputSizeChanged = true;
            mIncommingWidth = width;
            mIncommingHeight = height;
        }
    }

    private void onFilterSizeChanged() {
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

    protected void _release() {
        Log.e(TAG, "release");
        if (mContext != null) {
            mContext = null;
        }
        if (mSurfaceView != null) {
            mSurfaceView = null;
        }
        if (bitmap != null) {
            bitmap = null;
        }
        synchronized (mLock) {
            mHasInputSizeChanged = false;
            mHasSurfaceChanged = false;
            mHasFrameBufferInited = false;
            if (mInputTexture != OpenGLUtils.GL_NOT_TEXTURE) {
                OpenGLUtils.deleteTexture(mInputTexture);
                mInputTexture = OpenGLUtils.GL_NOT_TEXTURE;
            }
        }
        if (mImageTexture != OpenGLUtils.GL_NOT_TEXTURE) {
            OpenGLUtils.deleteTexture(mImageTexture);
            mImageTexture = OpenGLUtils.GL_NOT_TEXTURE;
        }
        if (mInputFilter != null) {
            mInputFilter.release();
            mInputFilter = null;
        }
        if (mImageInputFilter != null) {
            mImageInputFilter.release();
            mImageInputFilter = null;
        }
        if (mOutputFilter != null) {
            mOutputFilter.release();
            mOutputFilter = null;
        }
        if (mVertexBuffer != null) {
            mVertexBuffer.clear();
            mVertexBuffer = null;
        }
        if (mAnotherTextureBuffer != null) {
            mAnotherTextureBuffer.clear();
            mAnotherTextureBuffer = null;
        }
        if (mTextureBuffer != null) {
            mTextureBuffer.clear();
            mTextureBuffer = null;
        }
    }

    public void release() {
        synchronized (mLock) {
            mReleaseing = true;
        }
    }

    private RenderListener mRenderListener;

    public void setRenderListener(RenderListener listener) {
        this.mRenderListener = listener;
    }

    public interface RenderListener {
        void onRequestRender();
    }

    protected void notifyRequestRender() {
        if (mRenderListener != null) {
            mRenderListener.onRequestRender();
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

    public void reverseY() {
        if (mAnotherTextureBuffer != null) {
            mAnotherTextureBuffer.clear();
        }
        mAnotherTextureBuffer = OpenGLUtils.createFloatBuffer(TextureRotationUtils.TextureVertices_yreverse);
    }

    public void reverseX() {
        if (mAnotherTextureBuffer != null) {
            mAnotherTextureBuffer.clear();
        }
        mAnotherTextureBuffer = OpenGLUtils.createFloatBuffer(TextureRotationUtils.TextureVertices_xreverse);
    }

    public void rotate90() {
        if (mAnotherTextureBuffer != null) {
            mAnotherTextureBuffer.clear();
        }
        mAnotherTextureBuffer = OpenGLUtils.createFloatBuffer(TextureRotationUtils.TextureVertices_90);
    }

    public void rotate270() {
        if (mAnotherTextureBuffer != null) {
            mAnotherTextureBuffer.clear();
        }
        mAnotherTextureBuffer = OpenGLUtils.createFloatBuffer(TextureRotationUtils.TextureVertices_270);
    }
}
