package com.wilbert.library.contexts;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.PixelFormat;
import android.graphics.drawable.Drawable;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.view.Surface;

import com.wilbert.library.clips.abs.IFrameWorker;
import com.wilbert.library.codecs.ISurfaceObtainer;
import com.wilbert.library.codecs.abs.FrameInfo;
import com.wilbert.library.frameprocessor.gles.OpenGLUtils;
import com.wilbert.library.frameprocessor.gles.TextureRotationUtils;
import com.wilbert.library.log.ALog;

import java.nio.FloatBuffer;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

/**
 * author : Administrator
 * e-mail : jw20082009@qq.com
 * time   : 2020/04/26
 * desc   :
 */
public class VideoContext implements ISurfaceObtainer {
    private final String TAG = "VideoContext";
    private FrameInfo mFrameInfo;
    private GLSurfaceView mSurfaceView;
    private IFrameWorker mWorker;
    private boolean mPlaying = true;

    private int mSurfaceWidth, mSurfaceHeight;
    private int mProgramOut;
    private int mPositionHandle;
    private int mTextureCoordinateHandle;
    private int mInputTextureHandle;
    private int mCoordsPerVertex = TextureRotationUtils.CoordsPerVertex;
    private int mVertexCount = TextureRotationUtils.CubeVertices.length / mCoordsPerVertex;
    private Timeline mTimeline = new Timeline();
    private FloatBuffer mVertexBuffer;
    private FloatBuffer mTextureBuffer;
    private Object mLock = new Object();
    private Object mSync = new Object();


    public VideoContext(GLSurfaceView surfaceView, IFrameWorker worker) {
        mWorker = worker;
        mSurfaceView = surfaceView;
        mSurfaceView.setEGLContextClientVersion(2);
        mSurfaceView.setRenderer(new VideoRenderer());
        mSurfaceView.setRenderMode(GLSurfaceView.RENDERMODE_WHEN_DIRTY);
        new Thread(mFrameWorker).start();
    }

    public void release() {

    }

    @Override
    public Surface getSurface() {
        return null;
    }

    private Runnable mFrameWorker = new Runnable() {
        private boolean mFirstFrame = true;

        @Override
        public void run() {
            while (mPlaying) {
                if (mWorker != null) {
                    long timeElapse = -1;
                    synchronized (mSync) {
                        mFrameInfo = mWorker.getNextFrame();
                        if (mFirstFrame) {
                            mTimeline.start();
                            mFirstFrame = false;
                        }
                        if (mFrameInfo != null) {
                            timeElapse = mTimeline.compareTime(mFrameInfo.presentationTimeUs);
                        } else {
                            timeElapse = 10_000;
                        }
                    }
                    if (timeElapse > 0) {
                        try {
                            synchronized (mLock) {
                                long timeWait = timeElapse / 1000;
                                if (timeWait > 0) {
                                    mLock.wait(timeWait);
                                }
                            }
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                    mSurfaceView.requestRender();
                } else {
                    ALog.i(TAG, "no worker and quit");
                    mPlaying = false;
                }
            }
        }
    };

    private class VideoRenderer implements GLSurfaceView.Renderer {

        private final int STATUS_IDLE = 0x00;
        private final int STATUS_PREPARED = 0x01;
        private final int STATUS_RUNNING = 0x02;
        private final int STATUS_RELEASING = 0x03;

        private int mStatus = STATUS_IDLE;
        private NV21Renderer mYuvRender;

        public VideoRenderer() {
            mYuvRender = new NV21Renderer();
        }

        @Override
        public void onSurfaceCreated(GL10 gl, EGLConfig config) {
            mProgramOut = OpenGLUtils.createProgram(OpenGLUtils.getShaderFromAssets(mSurfaceView.getContext(),
                    "shader/base/vertex_normal.glsl"), OpenGLUtils.getShaderFromAssets(mSurfaceView.getContext(),
                    "shader/base/fragment_normal.glsl"));
            mPositionHandle = GLES20.glGetAttribLocation(mProgramOut, "aPosition");
            mTextureCoordinateHandle = GLES20.glGetAttribLocation(mProgramOut, "aTextureCoord");
            mInputTextureHandle = GLES20.glGetUniformLocation(mProgramOut, "inputTexture");
            mVertexBuffer = OpenGLUtils.createFloatBuffer(TextureRotationUtils.CubeVertices);
            mTextureBuffer = OpenGLUtils.createFloatBuffer(TextureRotationUtils.TextureVertices);
            mYuvRender.onSurfaceCreated();
            mStatus = STATUS_PREPARED;
        }

        @Override
        public void onSurfaceChanged(GL10 gl, int width, int height) {
            mSurfaceWidth = width;
            mSurfaceHeight = height;
            mStatus = STATUS_RUNNING;
        }

        @Override
        public void onDrawFrame(GL10 gl) {
            if (mStatus == STATUS_RELEASING) {
                _release();
                changeStatus(STATUS_IDLE);
                return;
            }
            if (mWorker == null) {
                return;
            }
            GLES20.glClearColor(1.0f, 1.0f, 1.0f, 1.0f);
            GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT);
            FrameInfo frameInfo = mFrameInfo;
            synchronized (mSync) {
                frameInfo = mFrameInfo;
                if (frameInfo == null)
                    return;
                mFrameInfo = null;
            }
            int textureId = mYuvRender.yuv2Rgb(frameInfo);
            if (textureId == -1) {
                return;
            }
            GLES20.glUseProgram(mProgramOut);
            mVertexBuffer.position(0);
            GLES20.glVertexAttribPointer(mPositionHandle, mCoordsPerVertex, GLES20.GL_FLOAT, false, 0, mVertexBuffer);
            GLES20.glEnableVertexAttribArray(mPositionHandle);
            mTextureBuffer.position(0);
            GLES20.glVertexAttribPointer(mTextureCoordinateHandle, mCoordsPerVertex, GLES20.GL_FLOAT, false, 0, mTextureBuffer);
            GLES20.glEnableVertexAttribArray(mTextureCoordinateHandle);
            GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
            GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textureId);
            GLES20.glUniform1i(mInputTextureHandle, 0);
            GLES20.glViewport(0, 0, mSurfaceWidth, mSurfaceHeight);
            GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP, 0, mVertexCount);
            GLES20.glDisableVertexAttribArray(mPositionHandle);
            GLES20.glDisableVertexAttribArray(mTextureCoordinateHandle);
            GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, 0);
            GLES20.glUseProgram(0);
            mWorker.releaseFrame(frameInfo);
        }

        private void _release() {
            if (mYuvRender != null) {
                mYuvRender.release();
                mYuvRender = null;
            }
        }

        public void release() {
            changeStatus(STATUS_RELEASING);
        }

        public void changeStatus(int status) {
            synchronized (mSync) {
                mStatus = status;
            }
        }
    }
}
