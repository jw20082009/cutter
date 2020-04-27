package com.wilbert.library.codecs;

import android.media.MediaExtractor;
import android.media.MediaFormat;
import android.text.TextUtils;

import com.wilbert.library.log.ALog;

import java.io.FileDescriptor;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;

/**
 * author : Wilbert
 * e-mail : jw20082009@qq.com
 * time   : 2020/04/26
 * desc   :
 */
public class VideoExtractor {
    private final String TAG = "VideoExtractor";

    public enum Type {
        AUDIO, VIDEO;
    }

    private final long mIgnoreTimeUs = 40000;//40ms
    private MediaExtractor mExtractor;
    private MediaFormat mFormat;
    private boolean mPrepared = false;
    private long mCurrentTimeUs = 0;
    private Type mType;

    public VideoExtractor() {
    }

    public boolean prepare(String filepath, Type type) throws IOException {
        if (TextUtils.isEmpty(filepath)) {
            throw new IOException("cannot prepare empty filepath");
        }
        mExtractor = new MediaExtractor();
        mExtractor.setDataSource(filepath);
        for (int i = 0; i < this.mExtractor.getTrackCount(); ++i) {
            MediaFormat format = this.mExtractor.getTrackFormat(i);
            String mime = format.getString(MediaFormat.KEY_MIME);
            if ((type == Type.VIDEO && mime.startsWith("video/")) || (type == Type.AUDIO && mime.startsWith("audio/"))) {
                mExtractor.selectTrack(i);
                mFormat = format;
                break;
            }
        }
        if (mFormat != null) {
            mPrepared = true;
            mCurrentTimeUs = 0;
        }
        return mPrepared;
    }

    public boolean prepare(FileDescriptor descriptor, Type type) throws IOException {
        mExtractor = new MediaExtractor();
        mExtractor.setDataSource(descriptor);
        for (int i = 0; i < this.mExtractor.getTrackCount(); ++i) {
            MediaFormat format = this.mExtractor.getTrackFormat(i);
            String mime = format.getString(MediaFormat.KEY_MIME);
            if ((type == Type.VIDEO && mime.startsWith("video/")) || (type == Type.AUDIO && mime.startsWith("audio/"))) {
                mExtractor.selectTrack(i);
                mFormat = format;
                break;
            }
        }
        if (mFormat != null) {
            mPrepared = true;
            mCurrentTimeUs = 0;
        }
        return mPrepared;
    }

    /**
     * @param buffer
     * @return 返回当前读取的sampleTime, 当读取到的size<= 0 时为-1
     */
    public long fillBuffer(InputInfo buffer) {
        if (buffer == null || !mPrepared) {
            ALog.i(TAG, "fillBuffer when " + (buffer == null ? "buffer is null" : "buffer not null") + "; mPrepared:" + mPrepared);
            return -1;
        }
        int size = mExtractor.readSampleData(buffer.buffer, 0);
        long time = mExtractor.getSampleTime();
        buffer.time = time;
        buffer.size = size;
        if (size > 0) {
            mExtractor.advance();
            mCurrentTimeUs = mExtractor.getSampleTime();
        } else {
            time = -1;
            buffer.lastFrameFlag = true;
        }
        return time;
    }

    public long seekTo(long timeUs) {
        if (!mPrepared || Math.abs(timeUs - mCurrentTimeUs) < mIgnoreTimeUs) {
            return mCurrentTimeUs;
        }
        long time = mCurrentTimeUs;
        int retryTimes = 10;
        do {
            mExtractor.seekTo(timeUs, MediaExtractor.SEEK_TO_CLOSEST_SYNC);
            time = mExtractor.getSampleTime();
        } while (time < 0 && --retryTimes > 0);
        mCurrentTimeUs = time;
        return mCurrentTimeUs;
    }

    public boolean isPrepared() {
        return mPrepared;
    }

    public MediaFormat getMediaFormat() {
        return mFormat;
    }

    public void release() {
        if (mExtractor != null) {
            mExtractor.release();
            mExtractor = null;
            mPrepared = false;
        }
    }
}
