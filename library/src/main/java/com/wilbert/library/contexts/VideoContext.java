package com.wilbert.library.contexts;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.PixelFormat;
import android.graphics.SurfaceTexture;
import android.graphics.drawable.Drawable;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.util.Log;
import android.view.Surface;

import com.wilbert.library.R;
import com.wilbert.library.basic.renderer.OesRenderer;
import com.wilbert.library.clips.abs.IFrameWorker;
import com.wilbert.library.codecs.ISurfaceObtainer;
import com.wilbert.library.codecs.abs.FrameInfo;
import com.wilbert.library.frameprocessor.gles.OpenGLUtils;
import com.wilbert.library.frameprocessor.gles.TextureRotationUtils;
import com.wilbert.library.log.ALog;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.nio.channels.FileChannel;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.TimeUnit;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import static com.wilbert.library.frameprocessor.gles.OpenGLUtils.checkGlError;

/**
 * author : Administrator
 * e-mail : jw20082009@qq.com
 * time   : 2020/04/26
 * desc   :
 */
public class VideoContext implements ISurfaceObtainer, Runnable, GLSurfaceView.Renderer {
    private final String TAG = "VideoContext";
    private FrameInfo mFrameInfo;
    private GLSurfaceView mSurfaceView;
    private IFrameWorker mWorker;
    private boolean mPlaying = true;

    private int mSurfaceWidth, mSurfaceHeight;
    private int mProgramOut;
    //    private int mTextureId;
    private int mPositionHandle;
    private int mTextureCoordinateHandle;
    private int mInputTextureHandle;
    private int mCoordsPerVertex = TextureRotationUtils.CoordsPerVertex;
    private int mVertexCount = TextureRotationUtils.CubeVertices.length / mCoordsPerVertex;
    private FloatBuffer mVertexBuffer;
    private FloatBuffer mTextureBuffer;
    private NV21Renderer mYuvRender;
    private int mTextureId = -1;
    private Object mLock = new Object();

    private BufferedOutputStream mOutStream;


    public VideoContext(GLSurfaceView surfaceView, IFrameWorker worker) {
        mSurfaceView = surfaceView;
        mSurfaceView.setEGLContextClientVersion(2);
        mSurfaceView.setRenderer(this);
        mSurfaceView.setRenderMode(GLSurfaceView.RENDERMODE_WHEN_DIRTY);
        mWorker = worker;
        try {
            mOutStream = new BufferedOutputStream(new FileOutputStream(new File("/sdcard/DCIM/test.data")));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        mYuvRender = new NV21Renderer();
        new Thread(this).start();
    }

    @Override
    public Surface getSurface() {
        return null;
    }

    private long mStartTime = 0;

    @Override
    public void run() {
        while (mPlaying) {
            if (mWorker != null) {
                mFrameInfo = mWorker.getNextFrame();
                if (mStartTime == 0) {
                    mStartTime = System.currentTimeMillis();
                } else if (System.currentTimeMillis() - mStartTime < 30_000) {
                    try {
                        synchronized (mLock) {
                            mLock.wait(30);
                        }
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
                if (mFrameInfo != null) {
                    mSurfaceView.requestRender();
                } else {
                    ALog.i(TAG, "getNextFrame with null return");
                }
            } else {
                ALog.i(TAG, "no worker and quit");
                mPlaying = false;
            }
        }
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
//        mTextureId = OpenGLUtils.createTexture(GLES20.GL_TEXTURE_2D);
        mYuvRender.onSurfaceCreated();
    }

    @Override
    public void onSurfaceChanged(GL10 gl, int width, int height) {
        mSurfaceWidth = width;
        mSurfaceHeight = height;
        mYuvRender.onSurfaceChanged(width, height);
    }

    @Override
    public void onDrawFrame(GL10 gl) {
        if (mWorker == null) {
            return;
        }
        GLES20.glClearColor(1.0f, 0.0f, 0.0f, 1.0f);
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT);
        FrameInfo frameInfo = mWorker.getNextFrame();
        if (frameInfo == null)
            return;
        int textureId = mYuvRender.yuv2Rgb(frameInfo);
        if (textureId == -1) {
            return;
        }
        checkGlError("onDrawFrame-1");
        GLES20.glUseProgram(mProgramOut);
        checkGlError("onDrawFrame0");
        GLES20.glClearColor(0.0f, 0.0f, 0.0f, 1.0f);
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT);
        mVertexBuffer.position(0);
        GLES20.glVertexAttribPointer(mPositionHandle, mCoordsPerVertex, GLES20.GL_FLOAT, false, 0, mVertexBuffer);
        GLES20.glEnableVertexAttribArray(mPositionHandle);
        checkGlError("onDrawFrame1");
        mTextureBuffer.position(0);
        GLES20.glVertexAttribPointer(mTextureCoordinateHandle, mCoordsPerVertex, GLES20.GL_FLOAT, false, 0, mTextureBuffer);
        GLES20.glEnableVertexAttribArray(mTextureCoordinateHandle);
        GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
        checkGlError("onDrawFrame2");
        if (mTextureId == -1) {
//            Bitmap bitmap = drawableToBitmap(mSurfaceView.getResources().getDrawable(R.drawable.sample_action_circle, mSurfaceView.getContext().getTheme()));
//            mTextureId = OpenGLUtils.createTexture(bitmap);
        }
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textureId);
        //GLES20.glTexImage2D(GLES20.GL_TEXTURE_2D, 0, GLES20.GL_RGB, frameInfo.frameWidth, frameInfo.frameHeight, 0, GLES20.GL_RGB, GLES20.GL_UNSIGNED_BYTE, frameInfo.outputBuffer);
        GLES20.glUniform1i(mInputTextureHandle, 0);
        GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP, 0, mVertexCount);
        GLES20.glDisableVertexAttribArray(mPositionHandle);
        GLES20.glDisableVertexAttribArray(mTextureCoordinateHandle);
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, 0);
        GLES20.glUseProgram(0);
        mWorker.releaseFrame(mFrameInfo);
        mFrameInfo = null;
    }

    public static final Bitmap drawableToBitmap(Drawable drawable) {
        Bitmap bitmap = Bitmap.createBitmap(drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight(),
                drawable.getOpacity() != PixelFormat.OPAQUE ? Bitmap.Config.ARGB_8888 : Bitmap.Config.RGB_565);
        Canvas canvas = new Canvas(bitmap);
        drawable.setBounds(0, 0, drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight());
        drawable.draw(canvas);
        return bitmap;
    }

    public void release() {
        if (mOutStream != null) {
            try {
                mOutStream.close();
                mOutStream = null;
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
