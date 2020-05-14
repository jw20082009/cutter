package com.wilbert.library.codecs;

import android.media.MediaFormat;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.text.TextUtils;

import com.wilbert.library.codecs.abs.FrameInfo;
import com.wilbert.library.codecs.abs.IDecoder;
import com.wilbert.library.codecs.abs.IExtractor;
import com.wilbert.library.codecs.abs.IExtractorListener;
import com.wilbert.library.codecs.abs.InputInfo;
import com.wilbert.library.contexts.VideoContext;
import com.wilbert.library.log.ALog;

import java.io.FileDescriptor;
import java.io.IOException;
import java.util.concurrent.Semaphore;

/**
 * author : wilbert
 * e-mail : jw20082009@qq.com
 * time   : 2020/04/26
 * desc   :
 */
public class VideoExtractorWrapper implements IExtractor {
    private final String TAG = "ExtractorWrapper";
    private VideoExtractor mExtractor;
    private MediaFormat mFormat;
    private ExtractorHandler mHandler;
    private DecodeHandler mDecodeHandler;
    private FileDescriptor mDescriptor;
    private IExtractorListener mListener;
    private String mFilePath = null;
    private boolean mPrepared = false;
    private boolean mReleasing = false;
    private IDecoder mDecoder;
    private Semaphore mPreparePhore = new Semaphore(2);
    private Object mLock = new Object();

    public VideoExtractorWrapper() {
    }

    @Override
    public void prepare(String filePath, VideoExtractor.Type type) {
        synchronized (mLock) {
            initHandler();
            mFilePath = filePath;
            mHandler.removeMessages(MSG_PREPARE_EXTRACTOR);
            Message prepareMessage = mHandler.obtainMessage(MSG_PREPARE_EXTRACTOR);
            prepareMessage.obj = type;
            prepareMessage.sendToTarget();
            mHandler.removeMessages(MSG_PREPARE_DECODER);
            Message decodeMessage = mHandler.obtainMessage(MSG_PREPARE_DECODER);
            decodeMessage.sendToTarget();
            mPreparePhore = new Semaphore(2);
        }
    }

    @Override
    public void start() {
        ALog.i(VideoContext.TAG, TAG + " start");
        synchronized (mLock) {
            initHandler();
            if (!mHandler.hasMessages(MSG_FEED_BUFFER)) {
                mHandler.sendEmptyMessage(MSG_FEED_BUFFER);
            }
        }
    }

    @Override
    public FrameInfo getNextFrameBuffer() {
        synchronized (mLock) {
            if (!mPrepared)
                return null;
            FrameInfo frameInfo = mDecoder.dequeueOutputBuffer();
            if (!mHandler.hasMessages(MSG_FEED_BUFFER)) {
                mHandler.sendEmptyMessage(MSG_FEED_BUFFER);
            }
            return frameInfo;
        }
    }

    @Override
    public void releaseFrameBuffer(FrameInfo frameInfo) {
        synchronized (mLock) {
            if (!mPrepared || frameInfo == null)
                return;
            mDecoder.queueOutputBuffer(frameInfo);
        }
    }

    @Override
    public void seekTo(long timeUs) {
        synchronized (mLock) {
            if (!mPrepared) {
                return;
            }
            mHandler.removeMessages(MSG_FEED_BUFFER);
            mHandler.removeMessages(MSG_SEEK);
            Message seekMessage = mHandler.obtainMessage(MSG_SEEK);
            seekMessage.obj = timeUs;
            seekMessage.sendToTarget();
            mHandler.removeMessages(MSG_FEED_BUFFER);
            mHandler.sendEmptyMessage(MSG_FEED_BUFFER);
        }
    }

    @Override
    public MediaFormat getMediaFormat() {
        synchronized (mLock) {
            if (mPrepared) {
                return mFormat;
            }
        }
        return null;
    }

    @Override
    public void setListener(IExtractorListener listener) {
        this.mListener = listener;
    }

    @Override
    public void release() {
        synchronized (mLock) {
            mReleasing = true;
            mHandler.release();
            mDecodeHandler.release();
        }
    }

    private void initHandler() {
        if (mHandler == null) {
            HandlerThread thread = new HandlerThread("VideoExtractorWrapper[" + hashCode() + "]");
            thread.start();
            mHandler = new ExtractorHandler(thread.getLooper());
            HandlerThread thread1 = new HandlerThread("VideoDecoder[" + hashCode() + "]");
            thread1.start();
            mDecodeHandler = new DecodeHandler(thread1.getLooper());
        }
    }

    private void _release() {
        if (mExtractor != null) {
            mExtractor.release();
            mExtractor = null;
        }
        mPrepared = false;
        if (mDecoder != null) {
            mDecoder.release();
            mDecoder = null;
        }
        mReleasing = false;
    }

    private final int MSG_PREPARE_EXTRACTOR = 0x01;
    private final int MSG_FEED_BUFFER = 0x02;
    private final int MSG_SEEK = 0x03;

    private boolean firstFrame = true;

    class ExtractorHandler extends Handler {

        public ExtractorHandler(Looper looper) {
            super(looper);
        }

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case MSG_PREPARE_EXTRACTOR:
                    ALog.i(VideoContext.TAG, TAG + " MSG_PREPARE 0");
                    if (mExtractor != null) {
                        mExtractor.release();
                    }
                    try {
                        mExtractor = new VideoExtractor();
                        long start = System.currentTimeMillis();
                        if (!TextUtils.isEmpty(mFilePath)) {
                            mExtractor.prepare(mFilePath, (VideoExtractor.Type) msg.obj);
                        } else {
                            mExtractor.prepare(mDescriptor, (VideoExtractor.Type) msg.obj);
                        }
                        ALog.i(VideoContext.TAG, TAG + " mExtractor.prepare:" + (System.currentTimeMillis() - start));
                        start = System.currentTimeMillis();
                        try {
                            mFormat = mExtractor.getMediaFormat();
                            mDecoder.prepare(mFormat);
                            ALog.i(VideoContext.TAG, TAG + " mDecoder.prepare:" + (System.currentTimeMillis() - start));
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        notifyPrepared();
                    } catch (IOException e) {
                        mPrepared = false;
                        e.printStackTrace();
                        if (mListener != null) {
                            mListener.onError(VideoExtractorWrapper.this, e);
                        }
                    } finally {
                        if (!mPrepared && mExtractor != null) {
                            mExtractor.release();
                            mExtractor = null;
                            mPrepared = false;
                            if (mListener != null) {
                                mListener.onReleased(VideoExtractorWrapper.this);
                            }
                        }
                    }
                    ALog.i(VideoContext.TAG, TAG + " MSG_PREPARE 1");
                    break;
                case MSG_FEED_BUFFER:
                    if (!mPrepared)
                        break;
                    if (firstFrame) {
                        ALog.i(VideoContext.TAG, TAG + " MSG_FEED_BUFFER");
                        firstFrame = false;
                    }
                    InputInfo inputInfo = mDecoder.dequeueInputBuffer();
                    if (inputInfo != null && inputInfo.buffer != null) {
                        long time = mExtractor.fillBuffer(inputInfo);
                        inputInfo.lastFrameFlag = time == -1 ? true : false;
                        mDecoder.queueInputBuffer(inputInfo);
                    }
                    if (!mReleasing) {
                        sendEmptyMessage(MSG_FEED_BUFFER);
                    }
                    break;
                case MSG_SEEK: {
                    if (!mPrepared) {
                        break;
                    }
                    Object tag = msg.obj;
                    long seekTimeUs = 0;
                    if (tag != null) {
                        seekTimeUs = (long) tag;
                    }
                    mExtractor.seekTo(seekTimeUs);
                    mDecoder.flush();
                }
                break;
            }
        }

        public void release() {
            post(new Runnable() {
                @Override
                public void run() {
                    _release();
                    getLooper().quit();
                }
            });
        }
    }

    private final int MSG_PREPARE_DECODER = 0x01;

    class DecodeHandler extends Handler {

        public DecodeHandler(Looper looper) {
            super(looper);
        }

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case MSG_PREPARE_DECODER:
                    mDecoder = new VideoDecoder();
                    break;
            }
        }

        public void release() {

        }
    }

    private void notifyPrepared() {
        mPrepared = true;
        if (mListener != null) {
            mListener.onPrepared(VideoExtractorWrapper.this);
        }
    }
}
