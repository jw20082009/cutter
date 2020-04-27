package com.wilbert.library.codecs;

import android.media.MediaFormat;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.text.TextUtils;

import com.wilbert.library.log.ALog;

import java.io.FileDescriptor;
import java.io.IOException;
import java.util.LinkedList;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.TimeUnit;

/**
 * author : wilbert
 * e-mail : jw20082009@qq.com
 * time   : 2020/04/26
 * desc   :
 */
public class VideoExtractorWrapper {
    private final String TAG = "ExtractorWrapper";
    private VideoExtractor mExtractor;
    private ExtractorHandler mHandler;
    private FileDescriptor mDescriptor;
    private IExtractorListener mListener;
    private String mFilePath = null;
    private boolean mPrepared = false;
    private boolean mReleasing = false;
    private Object mLock = new Object();
    private LinkedBlockingDeque<InputInfo> mInputBuffers = new LinkedBlockingDeque<>(5);

    public VideoExtractorWrapper() {
    }

    public void prepare(String filePath, VideoExtractor.Type type) {
        synchronized (mLock) {
            initHandler();
            mFilePath = filePath;
            mHandler.removeMessages(MSG_PREPARE);
            Message prepareMessage = mHandler.obtainMessage(MSG_PREPARE);
            prepareMessage.obj = type;
            prepareMessage.sendToTarget();
        }
    }

    public void prepare(FileDescriptor descriptor, VideoExtractor.Type type) {
        synchronized (mLock) {
            initHandler();
            mDescriptor = descriptor;
            mHandler.removeMessages(MSG_PREPARE);
            Message prepareMessage = mHandler.obtainMessage(MSG_PREPARE);
            prepareMessage.obj = type;
            prepareMessage.sendToTarget();
        }
    }

    public void offerBuffer(InputInfo inputInfo) {
        if (inputInfo == null) {
            return;
        }
        mInputBuffers.offerLast(inputInfo);
        ALog.i(TAG, "offerBuffer:" + mInputBuffers.size());
        synchronized (mLock) {
            if (!mPrepared) {
                return;
            }
            mHandler.removeMessages(MSG_FEED_BUFFER);
            mHandler.sendEmptyMessage(MSG_FEED_BUFFER);
        }
    }

    public void seekTo(long timeUs) {
        synchronized (mLock) {
            if (!mPrepared) {
                return;
            }
            mHandler.removeMessages(MSG_FEED_BUFFER);
            mInputBuffers.clear();
            mHandler.removeMessages(MSG_SEEK);
            Message seekMessage = mHandler.obtainMessage(MSG_SEEK);
            seekMessage.obj = timeUs;
            seekMessage.sendToTarget();
            mHandler.removeMessages(MSG_FEED_BUFFER);
            mHandler.sendEmptyMessage(MSG_FEED_BUFFER);
        }
    }

    public MediaFormat getMediaFormat() {
        synchronized (mLock) {
            if (mPrepared) {
                return mExtractor.getMediaFormat();
            }
        }
        return null;
    }

    public void setListener(IExtractorListener listener) {
        this.mListener = listener;
    }

    public void release() {
        synchronized (mLock) {
            mReleasing = true;
            mHandler.release();
        }
    }

    private void initHandler() {
        if (mHandler == null) {
            HandlerThread thread = new HandlerThread("VideoExtractorWrapper[" + hashCode() + "]");
            thread.start();
            mHandler = new ExtractorHandler(thread.getLooper());
        }
    }

    private void _release() {
        if (mInputBuffers != null) {
            mInputBuffers.clear();
        }
        if (mExtractor != null) {
            mExtractor.release();
            mExtractor = null;
        }
        mReleasing = false;
    }

    private final int MSG_PREPARE = 0x01;
    private final int MSG_FEED_BUFFER = 0x02;
    private final int MSG_SEEK = 0x03;

    class ExtractorHandler extends Handler {

        public ExtractorHandler(Looper looper) {
            super(looper);
        }

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case MSG_PREPARE:
                    if (mExtractor != null) {
                        mExtractor.release();
                    }
                    try {
                        mExtractor = new VideoExtractor();
                        if (!TextUtils.isEmpty(mFilePath)) {
                            mExtractor.prepare(mFilePath, (VideoExtractor.Type) msg.obj);
                        } else {
                            mExtractor.prepare(mDescriptor, (VideoExtractor.Type) msg.obj);
                        }
                        mPrepared = true;
                        if (mListener != null) {
                            mListener.onPrepared(VideoExtractorWrapper.this);
                        }
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
                        }
                    }
                    break;
                case MSG_FEED_BUFFER:
                    if (!mPrepared)
                        break;
                    try {
                        InputInfo inputInfo = mInputBuffers.pollFirst(10, TimeUnit.MILLISECONDS);
                        if (inputInfo != null && inputInfo.buffer != null) {
                            long time = mExtractor.fillBuffer(inputInfo);
                            inputInfo.lastFrameFlag = time == -1 ? true : false;
                            synchronized (mLock) {
                                if (mListener != null) {
                                    mListener.onInputBufferAvailable(inputInfo);
                                }
                            }
                        }
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    if (!mReleasing && !mInputBuffers.isEmpty()) {
                        sendEmptyMessage(MSG_FEED_BUFFER);
                    }
                    break;
                case MSG_SEEK: {
                    Object tag = msg.obj;
                    long seekTimeUs = 0;
                    if (tag != null) {
                        seekTimeUs = (long) tag;
                    }
                    mExtractor.seekTo(seekTimeUs);
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
}
