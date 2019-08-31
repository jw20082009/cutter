package com.wilbert.library.camera;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.media.audiofx.AudioEffect;
import android.opengl.GLSurfaceView;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.wilbert.library.basic.base.IResultListener;
import com.wilbert.library.basic.camera.CameraParams;
import com.wilbert.library.basic.entity.StickerEntity;
import com.wilbert.library.basic.fragments.BaseLoadingHandlerFragment;
import com.wilbert.library.basic.utils.OutFileGenerator;
import com.wilbert.library.basic.camera.IRecordViewListener;
import com.wilbert.library.basic.utils.Accelerometer;
import com.wilbert.library.basic.utils.CheckAudioPermission;
import com.wilbert.library.camera.Renderer.PreviewRenderer;
import com.wilbert.library.recorder.Recorder;
import com.wilbert.library.recorder.interfaces.IEncoder;
import com.wilbert.library.recorder.interfaces.IMediaEncoderListener;
import com.wilbert.library.recorder.interfaces.IVideoEncoder;
import com.wilbert.library.basic.renderer.IBeautyPreviewRenderer;
import com.wilbert.library.basic.renderer.IRecordablePreviewRenderer;
import com.wilbert.library.basic.entity.ResultEntity;
import com.wilbert.library.basic.entity.VideoEntity;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Android Studio.
 * User: wilbert jw20082009@qq.com
 * Date: 2019/8/26 14:33
 */
public abstract class BaseCameraFragment extends BaseLoadingHandlerFragment {
    private final String TAG = "BaseCameraFragment";
    protected static final int PERMISSION_REQUEST_AUDIO_PERMISSION = 1001;
    protected IResultListener<Boolean> mAudioPermissionListener;

    private SensorManager mSensorManager;
    private Activity mContext;
    private Accelerometer mAccelerometer = null;
    private Sensor mRotation;
    private GLSurfaceView mGLSurfaceView;
    private IRecordablePreviewRenderer mCameraDisplay;
    private boolean mPermissionDialogShowing = false;
    private boolean mIsHasAudioPermission = false;
    protected boolean mIsPaused = false;
    protected List<VideoEntity> mVideoEntities = new ArrayList<>();
    protected View mContentView;
    protected Recorder mRecorder;
    private boolean mIsRecording = false;
    protected String mVideoFilePath = null;

    public abstract int getLayoutId();

    public abstract GLSurfaceView getGLSurfaceView();

    protected abstract void initChildView();

    protected abstract AudioEffect getAudioEffect();

    protected abstract StickerEntity getWaterMarker();

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mContext = getActivity();
        mSensorManager = (SensorManager) mContext.getSystemService(Context.SENSOR_SERVICE);
        mRotation = mSensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        mContentView = inflater.inflate(getLayoutId(), container, false);
        return mContentView;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        if (mContext == null) {
            mContext = getActivity();
        }
        initChildView();
        initView();
        checkAudioPermissions();
    }

    private void initView() {
        FileUtils.copyModelFiles(mContext);
        mGLSurfaceView = getGLSurfaceView();
        mAccelerometer = new Accelerometer(mContext.getApplicationContext());
        mCameraDisplay = new PreviewRenderer(CameraParams.getInstance().expWidth,
                CameraParams.getInstance().expHeight);
        if (mCameraDisplay != null) {
            mCameraDisplay.setGLSurfaceView(mGLSurfaceView);
        }
    }

    public boolean checkAudioPermissions() {
        if (!mIsHasAudioPermission)
            mIsHasAudioPermission = CheckAudioPermission.isHasPermission(mContext);
        return mIsHasAudioPermission;
    }

    private void requestAudioPermission(IResultListener<Boolean> resultListener) {
        mAudioPermissionListener = resultListener;
        requestPermissions(new String[]{
                Manifest.permission.RECORD_AUDIO
        }, PERMISSION_REQUEST_AUDIO_PERMISSION);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSION_REQUEST_AUDIO_PERMISSION) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                if (mAudioPermissionListener != null) {
                    mAudioPermissionListener.onResult(true);
                }
            } else {
                if (mAudioPermissionListener != null) {
                    mAudioPermissionListener.onResult(false);
                }
                Toast.makeText(getActivity(), "麦克风权限被拒绝", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        setRecordStatus(false);
        if (mCameraDisplay != null) {
            mCameraDisplay.onResume();
        }
        mIsPaused = false;
    }

    public IBeautyPreviewRenderer getCameraDisplay() {
        return mCameraDisplay;
    }

    @Override
    public void onPause() {
        super.onPause();
        // if is recording, stop recording
        if (isRecording()) {
            stopRecording();
        }
        if (!mPermissionDialogShowing) {
            mAccelerometer.stop();
            if (mCameraDisplay != null)
                mCameraDisplay.onPause();
        }
    }

    private void setRecordStatus(boolean recording) {
        mIsRecording = recording;
    }

    protected boolean isRecording() {
        return mIsRecording;
    }

    protected void startRecording() {
        if (checkAudioPermissions()) {
            startRecordingReal();
        } else {
            requestAudioPermission(new IResultListener<Boolean>() {
                @Override
                public void onResult(Boolean aBoolean) {
                    if (aBoolean) {
                        startRecordingReal();
                    }
                }
            });
        }
    }

    protected void onStartRecord(List<VideoEntity> videoEntities) {
    }

    protected void onStopRecord(LocalVideoEntity videoEntity) {
    }

    protected void onDeleteRecordFile() {
    }

    protected void onResetRecord() {
    }

    protected void disableShowLayouts() {
    }

    protected void enableShowLayouts() {
    }

    protected void generateResult() {
        if (isRecording()) {
            onResetRecord();
        } else {
            ResultEntity resultEntity = null;
            if (mVideoEntities != null && mVideoEntities.size() > 0) {
                resultEntity = new ResultEntity();
            } else {
                Toast.makeText(getActivity(), "请先录制一段视频", Toast.LENGTH_SHORT).show();
            }
            onResultGenerating(resultEntity);
        }
    }

    protected void onResultGenerating(ResultEntity resultEntity) {
        if (resultEntity != null)
            resultEntity.setVideoEntities(mVideoEntities);
    }

    /**
     * 开始录音，不检查权限
     */
    private void startRecordingReal() {
        if (!isRecording()) {
            onStartRecord(mVideoEntities);
            setRecordStatus(true);
            try {
                mRecorder = new Recorder.Builder()
                        .filePath(OutFileGenerator.generateRecodeFile(getActivity()))
                        .iFrameInterval(CameraParams.getInstance().iFrameInterval)
                        .videoBitRate(CameraParams.getInstance().bitrate)
                        .videoFrameRate(CameraParams.getInstance().frameRate)
                        .videoSize(mCameraDisplay.getPreviewWidth(),
                                mCameraDisplay.getPreviewHeight())
                        .videoEncodeListener(mMediaEncoderListener)
                        .codecType(CameraParams.getInstance().encodeType)
                        .build();
                mRecorder.startRecording();
            } catch (final IOException e) {
                Log.e(TAG, "startCapture:", e);
            }
        }
    }

    /**
     * 停止录制，并将录制结果保存到视频列表中
     */
    protected void stopRecording() {
        if (mRecorder != null) {
            mVideoFilePath = mRecorder.getFilePath();
            mRecorder.stopRecording();
            mIsRecording = false;
            LocalVideoEntity videoEntity = new LocalVideoEntity();
            onStopRecord(videoEntity);
            videoEntity.setFilePath(mVideoFilePath);
            if (videoEntity.progressDuration > 2000) {
                mVideoEntities.add(videoEntity);
            } else {
                onDeleteRecordFile();
                Toast.makeText(getActivity(), "录制时长不足两秒无法保存文件", Toast.LENGTH_SHORT).show();
                deleteFile(mVideoFilePath);
            }
        }
        System.gc();
    }

    /**
     * 从已录制视频列表中删除最后一段
     */
    public void deleteLastVideo() {
        if (mVideoEntities != null && mVideoEntities.size() > 0) {
            int size = mVideoEntities.size();
            LocalVideoEntity videoEntity = (LocalVideoEntity) mVideoEntities
                    .remove(size - 1);
            deleteFile(videoEntity.getFilePath());
        }
    }

    private void deleteFile(final String filePath) {
        if (!TextUtils.isEmpty(filePath)) {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    File file = new File(filePath);
                    if (file.exists()) {
                        file.delete();
                    }
                }
            }).start();
        }
    }

    protected List<VideoEntity> getVideoEntities() {
        return mVideoEntities;
    }

    public IRecordViewListener mRecordViewListener = new IRecordViewListener() {
        @Override
        public void deleteLastVideo() {
            deleteLastVideo();
        }

        @Override
        public void onStartRecord(List<VideoEntity> recordedVideos) {
            disableShowLayouts();
        }

        @Override
        public void onStopRecord() {
            enableShowLayouts();
        }
    };

    /**
     * 录制编码器的编码回调
     */
    private final IMediaEncoderListener mMediaEncoderListener = new IMediaEncoderListener() {
        @Override
        public void onPrepared(final IEncoder encoder) {
            if (encoder instanceof IVideoEncoder && mCameraDisplay != null)
                mCameraDisplay.setVideoEncoder((IVideoEncoder) encoder);
        }

        @Override
        public void onStopped(final IEncoder encoder) {
            if (mCameraDisplay != null)
                mCameraDisplay.setVideoEncoder(null);
        }
    };

    /**
     * 从{@link VideoEntity}扩展了当前录制时长字段，用于表示当前视频在结果视频中的进度
     */
    public static class LocalVideoEntity extends VideoEntity {

        /**
         * 整个录制时长，从进度条的0位置开始
         */
        public long progressDuration;
    }


}
