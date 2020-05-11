package com.wilbert.library.clips;

import android.support.annotation.NonNull;
import android.text.TextUtils;

import com.wilbert.library.clips.abs.IFrameWorker;
import com.wilbert.library.clips.abs.IVideoClip;
import com.wilbert.library.codecs.VideoExtractor;
import com.wilbert.library.codecs.VideoExtractorWrapper;
import com.wilbert.library.codecs.abs.FrameInfo;
import com.wilbert.library.codecs.abs.IExtractorListener;
import com.wilbert.library.log.ALog;

import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

/**
 * author : Administrator
 * e-mail : jw20082009@qq.com
 * time   : 2020/04/26
 * desc   :
 */
public class VideoClip implements IVideoClip, IFrameWorker {
    private final String TAG = "VideoClip" + hashCode();
    private VideoExtractorWrapper mExtractor;
    private boolean mPrepared = false;
    private String mFilePath = null;
    private Semaphore mPermit = new Semaphore(1);

    public VideoClip() {
    }

    @Override
    public int getWidth() {
        return 0;
    }

    @Override
    public int getHeight() {
        return 0;
    }

    @Override
    public int getRotation() {
        return 0;
    }

    @Override
    public int getFps() {
        return 0;
    }

    @Override
    public long getDuration() {
        return 0;
    }

    @Override
    public int getBitrate() {
        return 0;
    }

    @Override
    public void prepare(String filePath) {
        try {
            mPermit.tryAcquire(1, 100, TimeUnit.MILLISECONDS);
            if (TextUtils.isEmpty(filePath)) {
                return;
            }
            if (mExtractor != null) {
                mExtractor.release();
            }
            mFilePath = filePath;
            mExtractor = new VideoExtractorWrapper();
            mExtractor.setListener(extractorListener);
            mExtractor.prepare(mFilePath, VideoExtractor.Type.VIDEO);
            mPermit.release();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void seekTo(long timeUs) {
        try {
            mPermit.tryAcquire(1, 100, TimeUnit.MILLISECONDS);
            if (!mPrepared) {
                ALog.i(TAG, "seekTo:" + timeUs);
                return;
            }
            mExtractor.seekTo(timeUs);
            mPermit.release();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void speedUp(float speed) {

    }

    @Override
    public FrameInfo getNextFrame() {
        FrameInfo frameInfo = null;
        try {
            mPermit.tryAcquire(1, 100, TimeUnit.MILLISECONDS);
            if (!mPrepared) {
                ALog.i(TAG, "getNextFrame when not prepared");
                return null;
            }
            frameInfo = mExtractor.getNextFrameBuffer();
            mPermit.release();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return frameInfo;
    }

    @Override
    public void releaseFrame(FrameInfo frameInfo) {
        try {
            mPermit.tryAcquire(1, 100, TimeUnit.MILLISECONDS);
            if (!mPrepared) {
                ALog.i(TAG, "releaseFrame when not prepared");
                return;
            }
            mExtractor.releaseFrameBuffer(frameInfo);
            mPermit.release();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public void release() {
        try {
            mPermit.tryAcquire(1, 100, TimeUnit.MILLISECONDS);
            if (mExtractor != null) {
                mExtractor.release();
                mExtractor = null;
                mPrepared = false;
            }
            mPermit.release();
        } catch (InterruptedException e) {
            e.printStackTrace();
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
