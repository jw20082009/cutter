package com.wilbert.cutter.camera;

import android.graphics.Point;
import android.graphics.SurfaceTexture;
import android.hardware.Camera;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;

import com.wilbert.cutter.R;
import com.wilbert.library.camera.Renderer.STGLRender;
import com.wilbert.library.frameprocessor.gles.OpenGLUtils;

import java.nio.ByteBuffer;
import java.util.List;
import java.util.concurrent.TimeUnit;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

public class CameraActivity extends AppCompatActivity implements GLSurfaceView.Renderer {
    public static Camera mCamera;
    private final int CAMERA_ID = Camera.CameraInfo.CAMERA_FACING_BACK;
    private GLSurfaceView mSurfaceView;
    private Camera.CameraInfo mCameraInfo = new Camera.CameraInfo();
    private Point mPictureSize;
    private byte[] mFrameData;
    private int mTextureId = -1;

    private STGLRender mGLRender;
    private boolean frameBufferReady = false;
    private boolean nv21YUVDataDirty = false;
    private boolean renderFlag = false;
    private boolean mTextureInit = false;
    private ByteBuffer frameRenderBuffer = null;
    private int[] mTextureY;
    private int[] mTextureUV;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera);
        mSurfaceView = findViewById(R.id.gl_surfaceview);
        initSurface(mSurfaceView);
        initCamera();
    }

    private void initSurface(GLSurfaceView surfaceView) {
        surfaceView.setEGLContextClientVersion(2);
        surfaceView.setRenderer(this);
        surfaceView.setRenderMode(GLSurfaceView.RENDERMODE_WHEN_DIRTY);
        mGLRender = new STGLRender();
    }

    private void initCamera() {
        if (mCamera != null) {
            mCamera.setPreviewCallback(null);
            mCamera.stopPreview();
            mCamera.release();
        }
        mCamera = Camera.open(CAMERA_ID);
        mCamera.getCameraInfo(CAMERA_ID, mCameraInfo);
        Camera.Parameters parameters = mCamera.getParameters();
        if (parameters.getSupportedFocusModes()
                .contains(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE)) {
            parameters.setFocusMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE);
        }
        List<String> flashModes = parameters.getSupportedFlashModes();
        if (flashModes != null && flashModes.contains(Camera.Parameters.FLASH_MODE_OFF)) {
            setFlashMode(Camera.Parameters.FLASH_MODE_OFF);
        }
        mPictureSize = getSuitablePictureSize();
        parameters.setPictureSize(mPictureSize.x, mPictureSize.y);
        mCamera.setParameters(parameters);
        mCamera.setPreviewCallback(mPreviewCallback);
    }

    private Point getSuitablePictureSize() {
        Point defaultsize = new Point(4608, 3456);
        // Point defaultsize = new Point(3264, 2448);
        if (mCamera != null) {
            Point maxSize = new Point(0, 0);
            List<Camera.Size> sizes = mCamera.getParameters().getSupportedPictureSizes();
            for (Camera.Size s : sizes) {
                if ((s.width == defaultsize.x) && (s.height == defaultsize.y)) {
                    return defaultsize;
                }
                if (maxSize.x < s.width) {
                    maxSize.x = s.width;
                    maxSize.y = s.height;
                }
            }
            return maxSize;
        }
        return null;
    }

    public void setFlashMode(String mode) {
        if (mCamera == null || TextUtils.isEmpty(mode)) {
            return;
        }
        Camera.Parameters parameters = mCamera.getParameters();
        if (parameters == null) {
            return;
        }
        List<String> flashModes = parameters.getSupportedFlashModes();
        // Check if camera flash exists
        if (flashModes == null) {
            // Use the screen as a flashlight (next best thing)
            return;
        }
        String flashMode = parameters.getFlashMode();
        if (!mode.equals(flashMode) && flashModes.contains(mode)) {
            // Turn on the flash
            parameters.setFlashMode(mode);
        }
    }

    private Camera.PreviewCallback mPreviewCallback = new Camera.PreviewCallback() {
        @Override
        public void onPreviewFrame(final byte[] data, Camera camera) {
            camera.addCallbackBuffer(data);
            mFrameData = data;
        }
    };

    @Override
    public void onSurfaceCreated(GL10 gl, EGLConfig config) {
        if (mTextureId == -1) {
            mTextureId = OpenGLUtils.createTexture(GLES20.GL_TEXTURE_2D);
        }
    }

    @Override
    public void onSurfaceChanged(GL10 gl, int width, int height) {
        if (mPictureSize != null) {
            mGLRender.init(mPictureSize.x, mPictureSize.y);
        }
    }

    @Override
    public void onDrawFrame(GL10 gl) {
        if ( !frameBufferReady) {
            return;
        }
        renderFlag = !renderFlag;
        if (nv21YUVDataDirty) {
            updateFrameWhenDirty(mFrameData);
            updateNV21YUVTexture();
        }
        int textureId = mGLRender.YUV2RGB(mTextureY[0], mTextureUV[0], renderFlag);

    }

    private void setUpTexture() {
        // nv21 y texture
        mTextureY = new int[1];
        GLES20.glGenTextures(1, mTextureY, 0);
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, mTextureY[0]);
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER,
                GLES20.GL_NEAREST);
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER,
                GLES20.GL_NEAREST);
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_S,
                GLES20.GL_CLAMP_TO_EDGE);
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_T,
                GLES20.GL_CLAMP_TO_EDGE);

        // nv21 uv texture
        mTextureUV = new int[1];
        GLES20.glGenTextures(1, mTextureUV, 0);
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, mTextureUV[0]);
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER,
                GLES20.GL_NEAREST);
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER,
                GLES20.GL_NEAREST);
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_S,
                GLES20.GL_CLAMP_TO_EDGE);
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_T,
                GLES20.GL_CLAMP_TO_EDGE);
    }

    private void updateNV21YUVTexture() {
        Log.i("PreviewTracer", "updateNV21YUVTexture: " + mTextureY[0] + ";" + mTextureUV[0]);
        if (!mTextureInit) {
            frameRenderBuffer.position(0);
            GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, mTextureY[0]);
            GLES20.glTexImage2D(GLES20.GL_TEXTURE_2D, 0, GLES20.GL_LUMINANCE, mPictureSize.x,
                    mPictureSize.y, 0, GLES20.GL_LUMINANCE, GLES20.GL_UNSIGNED_BYTE,
                    frameRenderBuffer);
            frameRenderBuffer.position(4 * (mPictureSize.x / 2) * (mPictureSize.y / 2));
            GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, mTextureUV[0]);
            GLES20.glTexImage2D(GLES20.GL_TEXTURE_2D, 0, GLES20.GL_LUMINANCE_ALPHA,
                    mPictureSize.x / 2, mPictureSize.y / 2, 0, GLES20.GL_LUMINANCE_ALPHA,
                    GLES20.GL_UNSIGNED_BYTE, frameRenderBuffer);
            mTextureInit = true;
        } else {
            frameRenderBuffer.position(0);
            GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
            GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, mTextureY[0]);
            GLES20.glTexSubImage2D(GLES20.GL_TEXTURE_2D, 0, 0, 0, mPictureSize.x, mPictureSize.y,
                    GLES20.GL_LUMINANCE, GLES20.GL_UNSIGNED_BYTE, frameRenderBuffer);
            frameRenderBuffer.position(4 * (mPictureSize.x / 2) * (mPictureSize.y / 2));
            GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, mTextureUV[0]);
            GLES20.glTexSubImage2D(GLES20.GL_TEXTURE_2D, 0, 0, 0, mPictureSize.x / 2, mPictureSize.y / 2,
                    GLES20.GL_LUMINANCE_ALPHA, GLES20.GL_UNSIGNED_BYTE, frameRenderBuffer);
        }
    }

    private void initFrameRenderBuffer(int size) {
        frameRenderBuffer = ByteBuffer.allocateDirect(size * 6);
        frameRenderBuffer.position(0);
        frameBufferReady = true;
    }

    private void updateFrameWhenDirty(byte[] data) {
        frameRenderBuffer.clear();
        frameRenderBuffer.position(0);
        frameRenderBuffer.put(data);
        frameRenderBuffer.position(0);
        nv21YUVDataDirty = false;
    }
}
