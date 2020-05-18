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

import java.util.concurrent.atomic.AtomicInteger;

/**
 * author : Administrator
 * e-mail : jw20082009@qq.com
 * time   : 2020/04/26
 * desc   :
 */
public class VideoClip implements IVideoClip, IFrameWorker {
    private final String TAG = "VideoClip" + hashCode();
    private VideoExtractorWrapper mExtractor;
    private String mFilePath = null;

    public static final int STATUS_RELEASED = 0x00;
    public static final int STATUS_RELEASING = 0X01;
    public static final int STATUS_PREPARING = 0x02;
    public static final int STATUS_IDLE = 0x03;
    public static final int STATUS_PREPARED = 0x04;

    private AtomicInteger mStatus = new AtomicInteger(0);

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
        if (TextUtils.isEmpty(filePath) || mStatus.get() >= STATUS_PREPARING) {
            return;
        }
        mStatus.set(STATUS_PREPARING);
        if (mExtractor != null) {
            mExtractor.release();
        }
        mFilePath = filePath;
        mExtractor = new VideoExtractorWrapper();
        mExtractor.setListener(extractorListener);
        mExtractor.prepare(mFilePath, VideoExtractor.Type.VIDEO);
        mStatus.set(STATUS_IDLE);
    }

    @Override
    public void seekTo(long timeUs) {
        if (mStatus.get() < STATUS_PREPARED) {
            ALog.i(TAG, "seekTo:" + timeUs);
            return;
        }
        mExtractor.seekTo(timeUs);
    }

    @Override
    public void speedUp(float speed) {

    }

    @Override
    public FrameInfo getNextFrame() {
        if (mStatus.get() < STATUS_IDLE)
            return null;
        return mExtractor.getNextFrameBuffer();
    }

    @Override
    public void releaseFrame(FrameInfo frameInfo) {
        if (mStatus.get() < STATUS_IDLE) {
            ALog.i(TAG, "releaseFrame when not prepared");
            return;
        }
        mExtractor.releaseFrameBuffer(frameInfo);
    }

    @Override
    public void release() {
        if (mStatus.get() <= STATUS_RELEASING)
            return;
        mStatus.set(STATUS_RELEASING);
        if (mExtractor != null) {
            mExtractor.release();
            mExtractor = null;
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
            mStatus.set(STATUS_PREPARED);
            mExtractor.start();
        }

        @Override
        public void onReleased(VideoExtractorWrapper extractor) {
            mStatus.set(STATUS_RELEASED);
        }

        @Override
        public void onError(VideoExtractorWrapper extractor, Throwable throwable) {

        }
    };

}
