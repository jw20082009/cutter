package com.wilbert.library.clips;

import android.support.annotation.NonNull;
import android.text.TextUtils;
import android.view.Surface;

import com.wilbert.library.clips.abs.IPlayer;
import com.wilbert.library.clips.abs.IPlayerListener;
import com.wilbert.library.codecs.abs.IExtractorListener;
import com.wilbert.library.codecs.VideoExtractor;
import com.wilbert.library.codecs.VideoExtractorWrapper;

import java.io.FileDescriptor;
import java.io.IOException;
import java.util.concurrent.CountDownLatch;

/**
 * author : Administrator
 * e-mail : jw20082009@qq.com
 * time   : 2020/04/26
 * desc   :
 */
public class VideoClip {
    private final String TAG = "VideoClip" + hashCode();
    private VideoExtractorWrapper mExtractor;
    private FileDescriptor mDescriptor;
    private IPlayerListener mListener;
    private boolean mPrepared = false;
    private Object mLock = new Object();
    private String mFilePath = null;
    private CountDownLatch mLatch = new CountDownLatch(1);
    private Surface mSurface;

    public VideoClip(String filepath) {
        synchronized (mLock) {
            mFilePath = filepath;
        }
    }

    public void prepare() {
        mLatch.await()
        if (TextUtils.isEmpty(mFilePath) && mDescriptor == null) {
            return;
        }
        mExtractor = new VideoExtractorWrapper();
        mExtractor.setListener(extractorListener);
        mExtractor.prepare(mFilePath, VideoExtractor.Type.VIDEO);
    }

    @Override
    public void start() {

    }

    @Override
    public void resume() {

    }

    @Override
    public void stop() {

    }

    @Override
    public void seekTo(long timeUS) {

    }

    @Override
    public long getCurrentTimeUs() {
        return 0;
    }

    @Override
    public void release() {
        synchronized (mLock) {
            if (mExtractor != null) {
                mExtractor.release();
                mExtractor = null;
            }
        }
    }

    @NonNull
    @Override
    public String toString() {
        return "VideoClip[" + hashCode() + "]";
    }

    public IExtractorListener extractorListener = new IExtractorListener() {

        @Override
        public void onPrepared(VideoExtractorWrapper extractor) {
            mPrepared = true;
        }

        @Override
        public void onReleased(VideoExtractorWrapper extractor) {
        }

        @Override
        public void onError(VideoExtractorWrapper extractor, Throwable throwable) {

        }
    };

}
