package com.wilbert.library.clips;

import android.media.MediaCodec;
import android.media.MediaFormat;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.support.annotation.NonNull;

import com.wilbert.library.codecs.InputInfo;
import com.wilbert.library.codecs.OutputInfo;
import com.wilbert.library.codecs.VideoDecoder;
import com.wilbert.library.codecs.VideoExtractor;
import com.wilbert.library.contexts.VideoContext;
import com.wilbert.library.log.ALog;

import java.io.FileDescriptor;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.concurrent.LinkedBlockingDeque;

/**
 * author : Administrator
 * e-mail : jw20082009@qq.com
 * time   : 2020/04/26
 * desc   :
 */
public class VideoClip implements IPlayer {
    private final String TAG = "VideoClip" + hashCode();
    private VideoContext mContext;
    private VideoDecoder mDecoder;
    private VideoExtractor mExtractor;
    private ClipHandler mHandler;
    private FileDescriptor mDescriptor;
    private IPlayerListener mListener;
    private boolean mPrepared = false;
    private Object mLock = new Object();
    private LinkedBlockingDeque<InputInfo> mInputInfos = new LinkedBlockingDeque<>();
    private LinkedBlockingDeque<OutputInfo> mOutputInfos = new LinkedBlockingDeque<>();

    public VideoClip(VideoContext context) {
        mContext = context;
    }

    @Override
    public void setDataSource(String filepath) throws IOException {
        setDataSource(new FileOutputStream(filepath).getFD());
    }

    @Override
    public void setDataSource(FileDescriptor descriptor) {
        synchronized (mLock) {
            mDescriptor = descriptor;
        }
    }

    @Override
    public void setPlayerListener(IPlayerListener listener) {
        synchronized (mLock) {
            mListener = listener;
        }
    }

    @Override
    public void prepare() {
        synchronized (mLock) {
            if (mDescriptor == null) {
                return;
            }
            _initHandler();
            mHandler.removeMessages(MSG_PREPARE);
            mHandler.sendEmptyMessage(MSG_PREPARE);
        }
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

    }

    private boolean _prepareExtractor() {
        boolean prepared = false;
        if (mExtractor != null) {
            mExtractor.release();
        }
        try {
            mExtractor = new VideoExtractor();
            mExtractor.prepare(mDescriptor, VideoExtractor.Type.VIDEO);
            prepared = true;
        } catch (IOException e) {
            e.printStackTrace();
            synchronized (mLock) {
                if (mListener != null) {
                    mListener.onError(VideoClip.this, e);
                }
            }
            prepared = false;
        } finally {
            if (!prepared && mExtractor != null) {
                mExtractor.release();
                mExtractor = null;
            }
        }
        return prepared;
    }

    private boolean _prepareDecoder() {
        boolean prepared = false;
        if (mDecoder != null) {
            mDecoder.release();
        }
        if (mExtractor.isPrepared()) {
            try {
                mDecoder = new VideoDecoder();
                mDecoder.setCallback(mDecodeCallback);
                mDecoder.prepare(mExtractor.getMediaFormat(), mContext.getSurface());
                prepared = true;
            } catch (IOException e) {
                e.printStackTrace();
                synchronized (mLock) {
                    if (mListener != null) {
                        mListener.onError(VideoClip.this, e);
                    }
                }
                prepared = false;
            } finally {
                if (!prepared && mDecoder != null) {
                    mDecoder.release();
                    mDecoder = null;
                }
            }
        }
        return prepared;
    }

    private void _release() {

    }

    private void _initHandler() {
        if (mHandler == null) {
            HandlerThread thread = new HandlerThread(toString());
            thread.start();
            mHandler = new ClipHandler(thread.getLooper());
        }
    }

    @NonNull
    @Override
    public String toString() {
        return "VideoClip[" + hashCode() + "]";
    }

    private final int MSG_PREPARE = 0x01;

    class ClipHandler extends Handler {
        public ClipHandler(Looper looper) {
            super(looper);
        }

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case MSG_PREPARE:
                    if (mContext == null) {
                        ALog.e(TAG, "MSG_PREPARE & mContext == null");
                        break;
                    }
                    boolean extractorPrepared = _prepareExtractor();
                    boolean decoderPrepared = _prepareDecoder();
                    if (extractorPrepared && decoderPrepared) {
                        mPrepared = true;
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
            mInputInfos.offerLast(new InputInfo(index, codec.getInputBuffer(index)));
            mExtractor.fillBuffer()
        }

        @Override
        public void onOutputBufferAvailable(@NonNull MediaCodec codec, int index, @NonNull MediaCodec.BufferInfo info) {

        }

        @Override
        public void onError(@NonNull MediaCodec codec, @NonNull MediaCodec.CodecException e) {

        }

        @Override
        public void onOutputFormatChanged(@NonNull MediaCodec codec, @NonNull MediaFormat format) {

        }
    };
}
