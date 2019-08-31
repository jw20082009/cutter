
package com.wilbert.library.camera.Renderer;

import android.content.Context;
import android.graphics.Rect;
import android.graphics.SurfaceTexture;
import android.hardware.Camera;
import android.opengl.EGL14;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.util.Log;
import android.util.Size;

import com.wilbert.library.basic.entity.StickerEntity;
import com.wilbert.library.basic.renderer.IPreviewListener;
import com.wilbert.library.basic.renderer.IRecordablePreviewRenderer;
import com.wilbert.library.basic.utils.STUtils;
import com.wilbert.library.camera.camera.CameraProxy;
import com.wilbert.library.frameprocessor.beautykit.BeautyRenderer;
import com.wilbert.library.frameprocessor.beautykit.IBeautyRenderer;
import com.wilbert.library.basic.aftereffect.IFaceDetector;
import com.wilbert.library.basic.entity.BeautifyEntity;
import com.wilbert.library.frameprocessor.entity.ByteWrapper;
import com.wilbert.library.frameprocessor.gles.GLImageFilter;
import com.wilbert.library.frameprocessor.gles.TextureRotationUtils;
import com.wilbert.library.frameprocessor.glutils.OpenGLUtils;
import com.wilbert.library.frameprocessor.sticker.StickerFilter;
import com.wilbert.library.basic.utils.Accelerometer;
import com.wilbert.library.basic.utils.LogUtils;
import com.wilbert.library.recorder.interfaces.IVideoEncoder;
import com.wilbert.library.basic.camera.CameraParams;
import java.io.File;
import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.LinkedBlockingDeque;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

public class PreviewRenderer implements IRecordablePreviewRenderer {

    private final String TAG = "PreviewRenderer";
    private final boolean DEBUG = false;
    public CameraProxy mCameraProxy;
    private IVideoEncoder mVideoEncoder;
    protected Context mContext;
    protected IPreviewListener mListener;
    protected GLSurfaceView mGlSurfaceView;
    protected boolean mIsPaused = false;
    protected SurfaceTexture mSurfaceTexture;
    protected int mTextureId = OpenGLUtils.NO_TEXTURE;
    protected int mExpectWidth, mExpectHeight, mImageWidth, mImageHeight, mSurfaceWidth, mSurfaceHeight;
    protected Size mPreviewSize = new Size(1280, 720);
    private int mCameraID = Camera.CameraInfo.CAMERA_FACING_FRONT;
    private boolean mCameraChanging = false;
    private boolean mSetPreViewSizeSucceed = false;
    private boolean mIsChangingPreviewSize = false;
    private int mCurrentPreview = 0;
    private boolean mInited = false;
    private float[] mStMatrix = new float[16];
    private boolean mNeedResetEglContext = false;
    // frame data
    private LinkedBlockingDeque<ByteWrapper> mLBDQ = new LinkedBlockingDeque<>();
    //    private byte[] mImageData;
    private boolean frameBufferReady = false;
    private ByteBuffer frameRenderBuffer = null;
    private boolean nv21YUVDataDirty;
    private boolean mBufferFilled = false;
    private long mStartTime;
    private STGLRender mGLRender;
    boolean renderFlag = false;
    private int[] mTextureY;
    private int[] mTextureUV;
    private boolean mTextureInit = false;
    private List<Float> mAspects = Arrays.asList(1.0f * 16 / 9, 1.0f * 4 / 3, 1.0f);
    private byte[][] mDataBuffer = new byte[2][];
    private int mInputTextureId;
    // for test fps
    private float mFps;
    private int mCount = 0;
    private long mCurrentTime = 0;
    private boolean mIsFirstCount = true;
    private int mFrameCost = 0;
    private long mRotateCost = 0;
    private long mObjectCost = 0;
    // encoder
    private int[] mVideoEncoderTexture;
    private boolean mNeedSave = false;
    private IBeautyRenderer mBeautyRenderer;
    private IFaceDetector mFaceDetector;
    private List<StickerEntity> mStickerEntities;
    private StickerFilter mStickerFilter;
    protected FloatBuffer mVertexBuffer;
    protected FloatBuffer mTextureBuffer;
    protected long mStickerStartTime = 0L;

    public PreviewRenderer(int expectWidth, int expectHeight) {
        mExpectWidth = expectWidth;
        mExpectHeight = expectHeight;
        mGLRender = new STGLRender();
        mStickerEntities = CameraParams.getInstance().waterMarkers;
    }

    @Override
    public void setBeautyRenderer(IBeautyRenderer mBeautyRenderer) {
        this.mBeautyRenderer = mBeautyRenderer;
        if (mBeautyRenderer != null) {
            mBeautyRenderer.init(mContext, mFaceDetector);
        }
    }

    @Override
    public void setBeautifyEffect(BeautifyEntity entity) {
        if (mBeautyRenderer != null) {
            mBeautyRenderer.setBeautifyEffect(entity);
        }
    }

    public String getStickerFilePath(Context context) {
        File stickerPath = new File(context.getExternalCacheDir().getAbsolutePath(), "sticker");
        if (!stickerPath.exists()) {
            stickerPath.mkdir();
        }
        return stickerPath.getAbsolutePath();
    }

    @Override
    public void setGLSurfaceView(GLSurfaceView glSurfaceView) {
        mContext = glSurfaceView.getContext();
        mGlSurfaceView = glSurfaceView;
        mFaceDetector = CameraParams.getInstance().faceDetector;
        glSurfaceView.setEGLContextClientVersion(2);
        glSurfaceView.setRenderer(this);
        glSurfaceView.setRenderMode(GLSurfaceView.RENDERMODE_WHEN_DIRTY);
        mBeautyRenderer = new BeautyRenderer();
        mBeautyRenderer.init(mContext, mFaceDetector);
        mCameraProxy = new CameraProxy(mContext);
    }

    @Override
    public void onResume() {
        if (mCameraProxy.getCamera() == null) {
            if (mCameraProxy.getNumberOfCameras() == 1) {
                mCameraID = Camera.CameraInfo.CAMERA_FACING_BACK;
            }
            mCameraProxy.openCamera(mCameraID);
            Camera.Size size = mCameraProxy.getSupportedRatioPreviewSize(mExpectWidth,
                    mExpectHeight);
            mPreviewSize = new Size(size.width, size.height);
        }
        mIsPaused = false;
        mTextureInit = false;
        mGLRender = new STGLRender();
        mSetPreViewSizeSucceed = false;
        mNeedResetEglContext = true;
        if (mGlSurfaceView != null) {
            mGlSurfaceView.onResume();
            mGlSurfaceView.forceLayout();
            mGlSurfaceView.queueEvent(new Runnable() {
                @Override
                public void run() {
                    if (mBeautyRenderer != null) {
                        mBeautyRenderer.onResume();
                    }
                }
            });
        }
    }

    @Override
    public void onPause() {
        mIsPaused = true;
        mTextureInit = false;
        mCameraProxy.releaseCamera();
        frameBufferReady = false;
        renderFlag = false;
        mInited = false;
        mSetPreViewSizeSucceed = false;
        mDataBuffer[0] = null;
        mDataBuffer[1] = null;
        if (mGlSurfaceView != null) {
            mGlSurfaceView.queueEvent(new Runnable() {
                @Override
                public void run() {
                    mLBDQ.clear();
                    deleteTextures();
                    if (mBeautyRenderer != null) {
                        mBeautyRenderer.onPause();
                    }
                    if (mSurfaceTexture != null) {
                        mSurfaceTexture.release();
                    }
                    mGLRender.destroyFrameBuffers();
                    if (mStickerFilter != null) {
                        mStickerFilter.release();
                        mStickerFilter = null;
                    }
                }
            });
            mGlSurfaceView.onPause();
        }
    }

    @Override
    public void onSurfaceCreated(GL10 gl, EGLConfig config) {
        LogUtils.i(TAG, "onSurfaceCreated");
        if (mIsPaused == true) {
            return;
        }
        GLES20.glEnable(GL10.GL_DITHER);
        GLES20.glClearColor(0.0f, 0.0f, 0.0f, 1.0f);
        GLES20.glEnable(GL10.GL_DEPTH_TEST);

        while (!mCameraProxy.isCameraOpen()) {
            if (mCameraProxy.cameraOpenFailed()) {
                return;
            }
            try {
                Thread.sleep(10, 0);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        if (mCameraProxy.getCamera() != null) {
            setUpCamera();
        }
    }

    /**
     * camera设备startPreview
     */
    private void setUpCamera() {
        // 初始化Camera设备预览需要的显示区域(mSurfaceTexture)
        if (mTextureId == OpenGLUtils.NO_TEXTURE) {
            mTextureId = OpenGLUtils.getExternalOESTextureID();
            mSurfaceTexture = new SurfaceTexture(mTextureId);
            //mSurfaceTexture.setOnFrameAvailableListener(mOnFrameAvailableListener);
        }
        mImageHeight = mPreviewSize.getWidth();// Integer.parseInt(size.substring(0, index));
        mImageWidth = mPreviewSize.getHeight();// Integer.parseInt(size.substring(index + 1));
        onInputSizeChanged(mImageWidth, mImageHeight);
        if (mListener != null) {
            mListener.onChangePreviewSize(mImageHeight, mImageWidth);
        }
        if (mIsPaused)
            return;
        while (!mSetPreViewSizeSucceed) {
            try {
                mCameraProxy.setPreviewSize(mImageHeight, mImageWidth);
                mSetPreViewSizeSucceed = true;
            } catch (Exception e) {
                mSetPreViewSizeSucceed = false;
            }
            if (!mSetPreViewSizeSucceed) {
                try {
                    Thread.sleep(10);
                } catch (Exception e) {
                }
            }
        }

        boolean flipHorizontal = mCameraProxy.isFlipHorizontal();
        boolean flipVertical = mCameraProxy.isFlipVertical();
        mGLRender.adjustTextureBuffer(mCameraProxy.getOrientation(), flipVertical, flipHorizontal);

        if (mIsPaused)
            return;
        mCameraProxy.startPreview(mSurfaceTexture, mPreviewCallback);
    }

    protected void onInputSizeChanged(int width, int height) {
        if (mBeautyRenderer != null) {
            mBeautyRenderer.onInputSizeChanged(width, height, mCameraProxy.getOrientation(), mCameraProxy.getCameraId());
        }
    }

    @Override
    public void onSurfaceChanged(GL10 gl, int width, int height) {
        LogUtils.i(TAG, "onSurfaceChanged");
        if (mIsPaused == true) {
            return;
        }
        if (mInited) { // 解决虚拟按键引起的glerror
            return;
        }
        GLES20.glClearColor(0.0f, 0.0f, 0.0f, 1.0f);
        adjustViewPort(width, height);
        mGLRender.init(mImageWidth, mImageHeight);
        mStartTime = System.currentTimeMillis();
        setUpTexture();
        mInited = true;
    }

    @Override
    public void onDrawFrame(GL10 gl) {
        // during switch camera
        if (mCameraChanging || !mBufferFilled || !frameBufferReady) {
            Log.i(TAG, "onDrawFrame return :" + mCameraChanging + ";" + mBufferFilled + ";"
                    + frameBufferReady);
            return;
        }
        if (mCameraProxy.getCamera() == null) {
            Log.i(TAG, "onDrawFrame return camera null");
            return;
        }
        if (mVideoEncoderTexture == null) {
            mVideoEncoderTexture = new int[1];
        }
        mStartTime = System.currentTimeMillis();
        renderFlag = !renderFlag;
        final int doubleBufIndex = renderFlag ? 0 : 1;
        if (mLBDQ.size() > 0) {
            mDataBuffer[doubleBufIndex] = mLBDQ.remove().getData();
        } else {
            renderFlag = !renderFlag;
            return;
        }
        if (nv21YUVDataDirty) {
            updateFrameWhenDirty(mDataBuffer[doubleBufIndex]);
            updateNV21YUVTexture();
        }

        int textureId = mGLRender.YUV2RGB(mTextureY[0], mTextureUV[0], renderFlag);
        GLES20.glClearColor(0.0f, 0.0f, 0.0f, 1.0f);
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT | GLES20.GL_DEPTH_BUFFER_BIT);
        long preProcessCostTime = System.currentTimeMillis();
        mInputTextureId = textureId;
        LogUtils.i(TAG, "preprocess cost time: %d",
                System.currentTimeMillis() - preProcessCostTime);
        int result = -1;
        /**
         * 帧处理代码
         */
        if (mBeautyRenderer != null) {
            mInputTextureId = mBeautyRenderer.onDrawFrame(mInputTextureId, mDataBuffer[doubleBufIndex]);
        }
        if (mStickerEntities != null) {
            if (mStickerFilter == null) {
                mStickerFilter = new StickerFilter(mContext, GLImageFilter.VERTEX_SHADER, GLImageFilter.FRAGMENT_SHADER);
                Log.i(TAG, "mSurfaceWidth:" + mSurfaceWidth + ";mSurfaceHeight:" + mSurfaceHeight);
                mStickerFilter.onDisplaySizeChanged(mSurfaceWidth, mSurfaceHeight);
                mStickerFilter.onInputSizeChanged(mImageWidth, mImageHeight);
                mStickerFilter.initFrameBuffer(mImageWidth, mImageHeight);
                for (StickerEntity stickerEntity : mStickerEntities) {
                    stickerEntity.setContainerWidth(mGlSurfaceView.getMeasuredWidth());
                    stickerEntity.setContainerHeight(mGlSurfaceView.getMeasuredHeight());
                }
                mStickerFilter.setStickerEntities(mStickerEntities);
                mVertexBuffer = com.wilbert.library.frameprocessor.gles.OpenGLUtils.createFloatBuffer(TextureRotationUtils.CubeVertices);
                mTextureBuffer = com.wilbert.library.frameprocessor.gles.OpenGLUtils.createFloatBuffer(TextureRotationUtils.TextureVertices);
                mStickerStartTime = System.currentTimeMillis();
            }
            // 使用当前的program
            mInputTextureId = mStickerFilter.drawFrameBuffer(mInputTextureId, mVertexBuffer, mTextureBuffer, System.currentTimeMillis() - mStickerStartTime);
        }
        if (mNeedSave) {
            savePicture(mInputTextureId);
            mNeedSave = false;
        }
        // video capturing
        if (mVideoEncoder != null) {
            GLES20.glFinish();
            mVideoEncoderTexture[0] = mInputTextureId;
            processStMatrix(mStMatrix);
            synchronized (this) {
                if (mVideoEncoder != null) {
                    if(mVideoEncoder.getEncodeType() == 0) {
                        if (mNeedResetEglContext) {
                            mVideoEncoder.setEglContext(EGL14.eglGetCurrentContext(),
                                    mVideoEncoderTexture[0]);
                            mNeedResetEglContext = false;
                        }
                        mVideoEncoder.frameAvailableSoon(mVideoEncoderTexture[0], mStMatrix);
                    }else{
                        mGLRender.saveTextureToFrameBuffer(mVideoEncoderTexture[0],mVideoEncoder.getBuffer(mImageWidth,mImageHeight));
                        mVideoEncoder.frameAvailableSoon(mVideoEncoderTexture[0], mStMatrix);
                    }

                }
            }
        }
        mFrameCost = (int) (System.currentTimeMillis() - mStartTime + mRotateCost + mObjectCost);
        long timer = System.currentTimeMillis();
        mCount++;
        if (mIsFirstCount) {
            mCurrentTime = timer;
            mIsFirstCount = false;
        } else {
            int cost = (int) (timer - mCurrentTime);
            if (cost >= 1000) {
                mCurrentTime = timer;
                mFps = (((float) mCount * 1000) / cost);
                mCount = 0;
            }
        }
        LogUtils.i(TAG, "render fps: %f", mFps);
        GLES20.glViewport(0, 0, mSurfaceWidth, mSurfaceHeight);
        mGLRender.onDrawFrame(mInputTextureId);
    }

    private void processStMatrix(float[] matrix) {
        matrix[0] = 1f;
        matrix[1] = 0f;
        matrix[2] = 0f;
        matrix[3] = 0f;
        matrix[4] = 0f;
        matrix[5] = -1.0f;
        matrix[6] = 0f;
        matrix[7] = 0f;
        matrix[8] = 0f;
        matrix[9] = 0f;
        matrix[10] = 1f;
        matrix[11] = 0f;
        matrix[12] = 0f;
        matrix[13] = 1.0f;
        matrix[14] = 0f;
        matrix[15] = 1f;
        return;
    }

    private Camera.PreviewCallback mPreviewCallback = new Camera.PreviewCallback() {
        @Override
        public void onPreviewFrame(final byte[] data, Camera camera) {
            camera.addCallbackBuffer(data);
            while (mLBDQ.size() > 1) {
                mLBDQ.pollLast();
            }
            mLBDQ.offer(new ByteWrapper(data));
            if (mCameraChanging || mCameraProxy.getCamera() == null) {
                return;
            }
            if (!frameBufferReady) {
                mLBDQ.clear();
                // queue.clear();
                initFrameRenderBuffer((mImageWidth / 2) * (mImageHeight / 2));
                mLBDQ.offer(new ByteWrapper(data));
                mBufferFilled = true;

            }
            nv21YUVDataDirty = true;
            if (mGlSurfaceView != null)
                mGlSurfaceView.requestRender();
        }
    };

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

    /**
     * 根据显示区域大小调整一些参数信息
     *
     * @param width
     * @param height
     */
    private void adjustViewPort(int width, int height) {
        mSurfaceHeight = height;
        mSurfaceWidth = width;
        GLES20.glViewport(0, 0, mSurfaceWidth, mSurfaceHeight);
        mGLRender.calculateVertexBuffer(mSurfaceWidth, mSurfaceHeight, mImageWidth, mImageHeight);
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
            GLES20.glTexImage2D(GLES20.GL_TEXTURE_2D, 0, GLES20.GL_LUMINANCE, mImageHeight,
                    mImageWidth, 0, GLES20.GL_LUMINANCE, GLES20.GL_UNSIGNED_BYTE,
                    frameRenderBuffer);
            frameRenderBuffer.position(4 * (mImageWidth / 2) * (mImageHeight / 2));
            GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, mTextureUV[0]);
            GLES20.glTexImage2D(GLES20.GL_TEXTURE_2D, 0, GLES20.GL_LUMINANCE_ALPHA,
                    mImageHeight / 2, mImageWidth / 2, 0, GLES20.GL_LUMINANCE_ALPHA,
                    GLES20.GL_UNSIGNED_BYTE, frameRenderBuffer);
            mTextureInit = true;
        } else {
            frameRenderBuffer.position(0);
            GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
            GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, mTextureY[0]);
            GLES20.glTexSubImage2D(GLES20.GL_TEXTURE_2D, 0, 0, 0, mImageHeight, mImageWidth,
                    GLES20.GL_LUMINANCE, GLES20.GL_UNSIGNED_BYTE, frameRenderBuffer);
            frameRenderBuffer.position(4 * (mImageWidth / 2) * (mImageHeight / 2));
            GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, mTextureUV[0]);
            GLES20.glTexSubImage2D(GLES20.GL_TEXTURE_2D, 0, 0, 0, mImageHeight / 2, mImageWidth / 2,
                    GLES20.GL_LUMINANCE_ALPHA, GLES20.GL_UNSIGNED_BYTE, frameRenderBuffer);
        }
    }

    /**
     * 释放纹理资源
     */
    protected void deleteTextures() {
        LogUtils.i(TAG, "delete textures");
        deleteCameraPreviewTexture();
        deleteInternalTextures();
    }

    // must in opengl thread
    private void deleteCameraPreviewTexture() {
        if (mTextureId != OpenGLUtils.NO_TEXTURE) {
            GLES20.glDeleteTextures(1, new int[]{
                    mTextureId
            }, 0);
        }
        mTextureId = OpenGLUtils.NO_TEXTURE;
    }

    private void deleteInternalTextures() {
        if (mVideoEncoderTexture != null) {
            GLES20.glDeleteTextures(1, mVideoEncoderTexture, 0);
            mVideoEncoderTexture = null;
        }
        if (mTextureY != null) {
            GLES20.glDeleteTextures(1, mTextureY, 0);
            mTextureY = null;
        }
        if (mTextureUV != null) {
            GLES20.glDeleteTextures(1, mTextureUV, 0);
            mTextureUV = null;
        }
    }

    @Override
    public void switchCamera() {
        if (Camera.getNumberOfCameras() == 1 || mCameraChanging) {
            return;
        }
        mCurrentPreview = 0;
        renderFlag = false;
        mDataBuffer[0] = null;
        mDataBuffer[1] = null;
        final int cameraID = 1 - mCameraID;
        mCameraChanging = true;
        mCameraProxy.openCamera(cameraID);
        if (mCameraProxy.cameraOpenFailed()) {
            return;
        }
        mSetPreViewSizeSucceed = false;
        if (mGlSurfaceView != null) {
            mGlSurfaceView.queueEvent(new Runnable() {
                @Override
                public void run() {
                    mLBDQ.clear();
                    // queue.clear();
                    deleteCameraPreviewTexture();
                    if (mCameraProxy.getCamera() != null) {
                        Camera.Size size = mCameraProxy.getSupportedRatioPreviewSize(mExpectWidth,
                                mExpectHeight);
                        mPreviewSize = new Size(size.width, size.height);
                        setUpCamera();
                    }
                    mGLRender.setShowRect(0, 0, 1.0f, 1.0f);
                    mCameraChanging = false;
                    mCameraID = cameraID;
                }
            });
        }
    }

    @Override
    public int getCameraID() {
        return mCameraID;
    }

    private void savePicture(int textureId) {
        ByteBuffer mTmpBuffer = ByteBuffer.allocate(mImageHeight * mImageWidth * 4);
        mGLRender.saveTextureToFrameBuffer(textureId, mTmpBuffer);
        mTmpBuffer.position(0);
        if (mListener != null) {
            mListener.onPictureTaken(mTmpBuffer, mImageWidth, mImageHeight);
        }
    }

    private void saveImageBuffer2Picture(byte[] imageBuffer) {
        ByteBuffer mTmpBuffer = ByteBuffer.allocate(mImageHeight * mImageWidth * 4);
        mTmpBuffer.put(imageBuffer);
        if (mListener != null) {
            mListener.onPictureTaken(mTmpBuffer, mImageWidth, mImageHeight);
        }
    }

    private int getCurrentOrientation() {
        int dir = Accelerometer.getDirection();
        int orientation;
        if (mCameraProxy.getOrientation() == 90 || mCameraProxy.getOrientation() == 270) {
            orientation = dir - 1;
            if (orientation < 0) {
                orientation = dir ^ 3;
            }
        } else {
            orientation = (dir + 2) % 4; // 适配平板方向
        }
        return orientation;
    }

    @Override
    public void changePreviewAspect(float aspect) {
        Log.i(TAG, "changePreviewAspect:" + aspect);
        mCurrentPreview = (mCurrentPreview + 1) % mAspects.size();
        int width = mImageWidth;
        int height = (int) (1.0F * aspect * mImageWidth);
        final float left = 0;
        final float right = 1.0f;
        final float top = 0.5f * (mImageHeight - height) / mImageHeight;
        final float bottom = 1.0f - top;
        if (mGlSurfaceView != null)
            mGlSurfaceView.queueEvent(new Runnable() {
                @Override
                public void run() {
                    mGLRender.setShowRect(left, top, right, bottom);
                }
            });
    }

    @Override
    public void setVideoEncoder(final IVideoEncoder encoder) {
        if (mGlSurfaceView != null)
            mGlSurfaceView.queueEvent(new Runnable() {
                @Override
                public void run() {
                    synchronized (this) {
                        if (encoder != null && mVideoEncoderTexture != null) {
                            encoder.setEglContext(EGL14.eglGetCurrentContext(),
                                    mVideoEncoderTexture[0]);
                        }
                        mVideoEncoder = encoder;
                    }
                }
            });
    }

    @Override
    public int getPreviewWidth() {
        return mImageWidth;
    }

    @Override
    public int getPreviewHeight() {
        return mImageHeight;
    }

    public int getFrameCost() {
        return mFrameCost;
    }

    public float getFpsInfo() {
        return (float) (Math.round(mFps * 10)) / 10;
    }

    public boolean isChangingPreviewSize() {
        return mIsChangingPreviewSize;
    }

    @Override
    public void setMeteringArea(float touchX, float touchY) {
        float[] touchPosition = new float[2];
        STUtils.calculateRotatetouchPoint(touchX, touchY, mSurfaceWidth, mSurfaceHeight, mCameraID,
                mCameraProxy.getOrientation(), touchPosition);
        Rect rect = STUtils.calculateArea(touchPosition, mSurfaceWidth, mSurfaceHeight, 100);
        mCameraProxy.setMeteringArea(rect);
    }

    @Override
    public void handleZoom(boolean isZoomOut) {
        if (mCameraProxy != null) {
            mCameraProxy.handleZoom(isZoomOut);
        }
    }

    @Override
    public void setExposureCompensation(int progress) {
        if (mCameraProxy != null) {
            mCameraProxy.setExposureCompensation(progress);
        }
    }

    @Override
    public List<Float> getAspects() {
        return mAspects;
    }

    @Override
    public void saveImage() {
        mNeedSave = true;
    }

    @Override
    public void setPreviewListener(IPreviewListener listener) {
        this.mListener = listener;
    }

    @Override
    public void onDestroy() {
        if (mBeautyRenderer != null) {
            mBeautyRenderer.onDestroy();
        }
        mGlSurfaceView.queueEvent(new Runnable() {
            @Override
            public void run() {
                if (mStickerFilter != null) {
                    mStickerFilter.release();
                    mStickerFilter = null;
                }
            }
        });
    }
}
