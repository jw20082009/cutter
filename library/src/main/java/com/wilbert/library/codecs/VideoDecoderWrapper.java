package com.wilbert.library.codecs;

import android.media.MediaCodec;
import android.media.MediaFormat;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.support.annotation.NonNull;
import android.view.Surface;

import com.wilbert.library.log.ALog;

import java.io.IOException;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.TimeUnit;

/**
 * author : wilbert
 * e-mail : jw20082009@qq.com
 * time   : 2020/04/27
 * desc   :
 */
public class VideoDecoderWrapper {
    private final String TAG = "DecoderWrapper";
    private VideoDecoder mDecoder;
    private DecodeHandler mHandler;
    private MediaFormat mFormat;
    private Surface mSurface;
    private Object mLock = new Object();
    private boolean mPrepared = false;
    private IDecodeListener mListener;
    private LinkedBlockingDeque<OutputInfo> mOutputBuffers = new LinkedBlockingDeque<>(5);

    public VideoDecoderWrapper() {
    }

    public void prepare(MediaFormat format, Surface surface) {
        if (format == null || surface == null)
            return;
        synchronized (mLock) {
            mFormat = format;
            mSurface = surface;
            initHandler();
            mHandler.removeMessages(MSG_PREPARE);
            mHandler.sendEmptyMessage(MSG_PREPARE);
        }
    }

    public void flush() {
        synchronized (mLock) {
            if (!mPrepared)
                return;
            mHandler.removeMessages(MSG_FLUSH);
            mHandler.sendEmptyMessage(MSG_FLUSH);
        }
    }

    public void setListener(IDecodeListener listener) {
        mListener = listener;
    }

    public void queueInput(InputInfo inputInfo) {
        synchronized (mLock) {
            if (inputInfo != null && mPrepared) {
                mDecoder.queueInput(inputInfo);
            }
        }
    }

    public OutputInfo pollFirst() {
        if (mOutputBuffers != null) {
            try {
                return mOutputBuffers.pollFirst(50, TimeUnit.MILLISECONDS);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    public void release() {
        synchronized (mLock) {
            if (mHandler != null) {
                mHandler.release();
                mHandler = null;
            }
        }
    }

    private void initHandler() {
        if (mHandler == null) {
            HandlerThread thread = new HandlerThread("VideoDecoderWrapper[" + hashCode() + "]");
            thread.start();
            mHandler = new DecodeHandler(thread.getLooper());
        }
    }

    private void _release() {
        synchronized (mLock) {
            mPrepared = false;
        }
        if (mDecoder != null) {
            mDecoder.setCallback(null);
            mDecoder.release();
            mDecoder = null;
        }
        if (mOutputBuffers != null) {
            mOutputBuffers.clear();
        }
    }

    private final int MSG_PREPARE = 0x01;
    private final int MSG_FLUSH = 0x02;

    class DecodeHandler extends Handler {
        public DecodeHandler(Looper looper) {
            super(looper);
        }

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case MSG_FLUSH:
                    if (!mPrepared) {
                        return;
                    }
                    if (mDecoder != null) {
                        mOutputBuffers.clear();
                        mDecoder.flush();
                    }
                    break;
                case MSG_PREPARE:
                    if (mDecoder != null) {
                        mDecoder.release();
                    }
                    try {
                        mDecoder = new VideoDecoder();
                        mDecoder.setCallback(mDecodeCallback);
                        mDecoder.prepare(mFormat, mSurface);
                        synchronized (mLock) {
                            mPrepared = true;
                        }
                        if (mListener != null) {
                            mListener.onPrepared(VideoDecoderWrapper.this);
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                        synchronized (mLock) {
                            if (mListener != null) {
                                mListener.onError(VideoDecoderWrapper.this, e);
                            }
                            mPrepared = false;
                        }
                    } finally {
                        if (!mPrepared && mDecoder != null) {
                            mDecoder.release();
                            mDecoder = null;
                        }
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

    public MediaCodec.Callback mDecodeCallback = new MediaCodec.Callback() {
        @Override
        public void onInputBufferAvailable(@NonNull MediaCodec codec, int index) {
            InputInfo inputInfo = new InputInfo(index, codec.getInputBuffer(index));
            if (mListener != null) {
                mListener.onInputBufferAvailable(inputInfo);
            }
        }

        int i = 0;

        @Override
        public void onOutputBufferAvailable(@NonNull MediaCodec codec, int index, @NonNull MediaCodec.BufferInfo info) {
            OutputInfo outputInfo = new OutputInfo(index, info);
//            mOutputBuffers.offerLast(outputInfo);
            ALog.i(TAG, "onOutputBufferAvailable:" + mOutputBuffers.size());
            if ((info.flags & MediaCodec.BUFFER_FLAG_CODEC_CONFIG) != 0) {
                codec.releaseOutputBuffer(index, false);
            } else {
                if (i < 10) {
                    codec.releaseOutputBuffer(index, true);
                    i++;
                }
            }
        }

        @Override
        public void onError(@NonNull MediaCodec codec, @NonNull MediaCodec.CodecException e) {
            if (mListener != null) {
                mListener.onError(VideoDecoderWrapper.this, e);
            }
        }

        @Override
        public void onOutputFormatChanged(@NonNull MediaCodec codec, @NonNull MediaFormat format) {

        }
    };
}
