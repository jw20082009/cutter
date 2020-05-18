package com.wilbert.cutter.edit;

import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.opengl.GLSurfaceView;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.wilbert.cutter.R;
import com.wilbert.library.clips.AudioClip;
import com.wilbert.library.clips.VideoClip;
import com.wilbert.library.contexts.AudioContext;
import com.wilbert.library.contexts.Timeline;
import com.wilbert.library.contexts.VideoContext;
import com.wilbert.library.contexts.abs.ITimeline;

import java.lang.ref.SoftReference;

public class EditActivity extends AppCompatActivity implements AudioManager.OnAudioFocusChangeListener, View.OnClickListener {
    final String TAG = "EditActivity";
    GLSurfaceView mSurfaceView;
    TextView mPauseBtn;
    VideoClip mVideoClip;
    AudioClip mAudioClip;
    VideoContext mVideoContext;
    AudioContext mAudioContext;
    AudioManager mAudioManager;
    ITimeline mTimeline;
    SoftReference<EditActivity> mFocusChangeListener;

    public static Intent createIntent(Context context, String filepath, int simpleCompress) {
        Intent intent = new Intent(context, EditActivity.class);
        intent.putExtra("uri", filepath);
        intent.putExtra("simplecompress", simpleCompress);
        return intent;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit);
        mSurfaceView = (GLSurfaceView) findViewById(R.id.gl_surfaceview);
        mAudioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        int audioSession = mAudioManager.generateAudioSessionId();
        mPauseBtn = findViewById(R.id.btn_pause);
        mPauseBtn.setOnClickListener(this);
        String url = getIntent().getStringExtra("uri");
        mVideoClip = new VideoClip();
        mAudioClip = new AudioClip();
        mVideoClip.prepare(url);
        mAudioClip.prepare(url);
        mTimeline = new Timeline();
        mVideoContext = new VideoContext(mSurfaceView, mVideoClip, mTimeline);
        mAudioContext = new AudioContext(mAudioClip, mTimeline, audioSession);
    }

    public boolean requestFocus() {
        if (mFocusChangeListener != null && mFocusChangeListener.get() != null) {
            return AudioManager.AUDIOFOCUS_REQUEST_GRANTED ==
                    mAudioManager.requestAudioFocus(mFocusChangeListener.get(),
                            AudioManager.STREAM_MUSIC,
                            AudioManager.AUDIOFOCUS_GAIN);
        }
        return false;
    }

    public boolean abandonFocus() {
        if (mFocusChangeListener != null && mFocusChangeListener.get() != null) {
            return AudioManager.AUDIOFOCUS_REQUEST_GRANTED ==
                    mAudioManager.abandonAudioFocus(mFocusChangeListener.get());
        }
        return false;
    }

    @Override
    public void onAudioFocusChange(int focusChange) {
        switch (focusChange) {
            case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT://Pause playback
                break;
            case AudioManager.AUDIOFOCUS_GAIN://Resume playback
                break;
            case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK://
                break;
            case AudioManager.AUDIOFOCUS_LOSS://Stop playback
                //am.unregisterMediaButtonEventReceiver(RemoteControlReceiver);
                abandonFocus();
                break;
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
//        mTimeline.start();
        requestFocus();
        mSurfaceView.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
//        mTimeline.pause();
        abandonFocus();
        mSurfaceView.onPause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.i(TAG, "onDestroy 0");
        mVideoClip.release();
        Log.i(TAG, "onDestroy 1");
        mAudioClip.release();
        Log.i(TAG, "onDestroy 2");
        mVideoContext.release();
        Log.i(TAG, "onDestroy 3");
        mAudioContext.release();
        Log.i(TAG, "onDestroy 4");
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_pause:
                if (mTimeline.isPlaying()) {
                    mTimeline.pause();
                } else {
                    mTimeline.start();
                }
                break;
        }
    }
}
