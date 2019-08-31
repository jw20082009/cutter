package com.wilbert.cutter.cut;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.wilbert.cutter.R;
import com.wilbert.library.basic.activity.SimpleVideoActivity;
import com.wilbert.library.basic.aftereffect.BaseVideoActivity;
import com.wilbert.library.basic.entity.AeEntity;
import com.wilbert.library.basic.entity.StickerEntity;
import com.wilbert.library.basic.utils.DensityUtils;
import com.wilbert.library.basic.utils.OutFileGenerator;
import com.wilbert.library.basic.widgets.AspectGLSurfaceView;
import com.wilbert.library.basic.widgets.videocutview.VideoCutViewBar;
import com.wilbert.library.videoprocessor.VideoProcessor;
import com.wilbert.library.videoprocessor.util.VideoProgressListener;

import java.lang.ref.SoftReference;
import java.util.Arrays;

public class VideoEditActivity extends BaseVideoActivity implements View.OnClickListener {
    final String TAG = "VideoEditActivity";
    final int COMPRESS_BITRATE = (int) (1.0 * 1000 * 1000 * 4);

    View mLoadingView, mConfirmView;

    TextView mTvProgress;

    MyHandler mHandler;

    VideoFrameView mVideoFrameView;

    TextView tvAndroidFront, tvAndroidCurrent, tvAndroidBack;

    static final int maxcorptime = 5 * 1000;

    long mCurrentStart = 0, mCurrentRange = maxcorptime;

    boolean simpleCompress = false;

    public static Intent createIntent(Context context, String filepath, int simpleCompress) {
        Intent intent = new Intent(context, VideoEditActivity.class);
        intent.putExtra("uri", filepath);
        intent.putExtra("simplecompress", simpleCompress);
        return intent;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        simpleCompress = (getIntent().getIntExtra("simplecompress", 0) == 1);
        mHandler = new MyHandler(this);
        initView();
        hideLoading();
    }

    private void initView() {
        mLoadingView = findViewById(R.id.fl_loading);
        mTvProgress = findViewById(R.id.tv_progress);
        mConfirmView = findViewById(R.id.tv_confirm);
        mVideoFrameView = findViewById(R.id.video_crop_view_bar);
        mConfirmView.setOnClickListener(this);
        tvAndroidFront = findViewById(R.id.tv_android_front);
        tvAndroidCurrent = findViewById(R.id.tv_android_current);
        tvAndroidBack = findViewById(R.id.tv_android_back);

        if (!simpleCompress) {
            mVideoFrameView.setVideoPath(mFilePath);
            mVideoFrameView.setMaxTime(maxcorptime);
            mVideoFrameView.setOnVideoCropViewBarListener(androidCorpListener);
            tvAndroidFront.setText(getString(R.string.str_videocrop, 1.0f * mCurrentStart / 1000));
            tvAndroidCurrent
                    .setText(getString(R.string.str_videocrop_selected, 1.0f * mCurrentRange / 1000));
            tvAndroidBack.setText(
                    getString(R.string.str_videocrop, 1.0f * (mCurrentStart + mCurrentRange) / 1000));
            mVideoFrameView.setVisibility(View.VISIBLE);
            tvAndroidFront.setVisibility(View.VISIBLE);
            tvAndroidCurrent.setVisibility(View.VISIBLE);
            tvAndroidBack.setVisibility(View.VISIBLE);
        } else {
            mVideoFrameView.setVisibility(View.GONE);
            tvAndroidFront.setVisibility(View.GONE);
            tvAndroidCurrent.setVisibility(View.GONE);
            tvAndroidBack.setVisibility(View.GONE);
        }
    }

    @Override
    protected AspectGLSurfaceView getSurfaceView() {
        return findViewById(R.id.surfaceview);
    }

    @Override
    protected int getLayoutId() {
        return R.layout.activity_video_edit;
    }

    @Override
    protected String getFilePath() {
        return getIntent().getStringExtra("uri");
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.tv_confirm) {
            showLoading();
            new Thread(new Runnable() {
                @Override
                public void run() {
                    String outPath = "";
                    try {
                        if (simpleCompress) {
                            AeEntity aeEntity = new AeEntity();
                            StickerEntity stickerEntity = new StickerEntity();
                            stickerEntity.startTime = 0;
                            stickerEntity.durationTime = Integer.MAX_VALUE;
                            stickerEntity.setContainerWidth(200);
                            stickerEntity.setContainerHeight(50);
                            stickerEntity.setTextSize(DensityUtils.dp2px(VideoEditActivity.this, 22));
                            stickerEntity.setText("测试测试");
                            stickerEntity.setMaxLines(1);
                            stickerEntity.setTextColor(Color.RED);
                            aeEntity.setFilePath(mFilePath);
                            aeEntity.setOutPath(OutFileGenerator.generateAeFile(VideoEditActivity.this, mFilePath));
                            aeEntity.setStickerEntities(Arrays.asList(stickerEntity));
                            outPath = OutFileGenerator.generateCompressFile(VideoEditActivity.this);
                            VideoProcessor.processor(VideoEditActivity.this).input(mFilePath)
                                    .output(outPath).frameRate(25).bitrate(COMPRESS_BITRATE).afterEffect(aeEntity)
                                    .progressListener(new VideoProgressListener() {
                                        @Override
                                        public void onProgress(float progress) {
                                            Message message = Message.obtain(mHandler,
                                                    MSG_SHOW_PROGRESS);
                                            message.obj = progress * 100;
                                            message.sendToTarget();
                                        }
                                    }).process();
                        } else {
                            outPath = OutFileGenerator.generateCutFile(VideoEditActivity.this, mFilePath);
                            VideoProcessor.processor(VideoEditActivity.this).input(mFilePath)
                                    .output(outPath).frameRate(25).startTimeMs((int) mCurrentStart)
                                    .endTimeMs((int) (mCurrentStart + mCurrentRange))
                                    .progressListener(new VideoProgressListener() {
                                        @Override
                                        public void onProgress(float progress) {
                                            Message message = Message.obtain(mHandler,
                                                    MSG_SHOW_PROGRESS);
                                            message.obj = progress * 100;
                                            message.sendToTarget();
                                        }
                                    }).process();
                        }
                        Message finishMsg = mHandler.obtainMessage(MSG_FINISH_CUT);
                        finishMsg.obj = outPath;
                        finishMsg.sendToTarget();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }).start();
        }
    }

    private final int MSG_HIDE_LOADING = 0x01;

    private final int MSG_SHOW_PROGRESS = 0x02;

    private final int MSG_FINISH_PREPROCESS = 0x05;

    private final int MSG_FINISH_CUT = 0x06;

    public void handleMessage(Message msg) {
        switch (msg.what) {
            case MSG_FINISH_CUT:
                hideLoading();
                startActivity(SimpleVideoActivity.createIntent(this, (String) msg.obj));
                break;
            case MSG_FINISH_PREPROCESS:
                final String filePath = (String) msg.obj;
                break;
            case MSG_HIDE_LOADING:
                hideLoading();
                break;
            case MSG_SHOW_PROGRESS:
                mTvProgress.setText(getString(R.string.str_preprocess, (float) msg.obj) + "%");
                break;
        }
    }

    private void showLoading() {
        mLoadingView.setVisibility(View.VISIBLE);
    }

    private void hideLoading() {
        mLoadingView.setVisibility(View.GONE);
    }

    VideoCutViewBar.OnVideoCropViewBarListener androidCorpListener = new VideoCutViewBar.OnVideoCropViewBarListener() {
        @Override
        public void onTouchDown() {
            Log.i(TAG, "onTouchDown:");
            mMediaPlayer.pause();
        }

        @Override
        public void onTouchUp() {
            Log.i(TAG, "onTouchUp:");
            mMediaPlayer.seekTo((int) mCurrentStart);
            mMediaPlayer.start();
        }

        @Override
        public void onTouchChange(long time) {
            mCurrentStart = time;
            Log.i(TAG, "onTouchChange " + time);
            tvAndroidFront.setText(getString(R.string.str_videocrop, 1.0f * time / 1000));
            tvAndroidBack.setText(
                    getString(R.string.str_videocrop, 1.0f * (time + mCurrentRange) / 1000));
        }

        @Override
        public void onRangeChange(long time, long range) {
            Log.i(TAG, "onRangeChanged " + time + ";" + range);
            mCurrentStart = time;
            mCurrentRange = range;
            tvAndroidFront.setText(getString(R.string.str_videocrop, 1.0f * time / 1000));
            tvAndroidCurrent
                    .setText(getString(R.string.str_videocrop_selected, 1.0f * range / 1000));
            tvAndroidBack.setText(getString(R.string.str_videocrop, 1.0f * (time + range) / 1000));
        }

        @Override
        public void onError(String error) {
            Log.i(TAG, "onError " + error);
        }
    };

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mHandler.invalidate();
    }

    static class MyHandler extends Handler {

        SoftReference<VideoEditActivity> reference;

        public MyHandler(VideoEditActivity activity) {
            reference = new SoftReference<>(activity);
        }

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            if (reference != null) {
                VideoEditActivity activity = reference.get();
                if (activity != null) {
                    activity.handleMessage(msg);
                }
            }
        }

        public void invalidate() {
            if (reference != null) {
                reference.clear();
                reference = null;
            }
        }
    }
}
