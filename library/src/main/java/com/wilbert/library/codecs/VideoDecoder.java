package com.wilbert.library.codecs;

import android.media.MediaCodec;
import android.media.MediaCodecList;
import android.media.MediaFormat;
import android.support.annotation.NonNull;

import com.wilbert.library.codecs.abs.FrameInfo;
import com.wilbert.library.codecs.abs.IDecoder;
import com.wilbert.library.codecs.abs.InputInfo;
import com.wilbert.library.contexts.Timeline;
import com.wilbert.library.contexts.VideoContext;
import com.wilbert.library.log.ALog;

import java.io.IOException;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.TimeUnit;

import static android.media.MediaCodecList.REGULAR_CODECS;

/**
 * author : Administrator
 * e-mail : jw20082009@qq.com
 * time   : 2020/04/26
 * desc   :
 */
public class VideoDecoder implements IDecoder {
    private final String TAG = "VideoDecoder";
    private MediaCodec mDecoder;
    private boolean mPrepared = false;
    private MediaFormat mInputFormat;
    private MediaFormat mOutputFormat;
    private InputInfo mCurrentInputInfo = null;
    private FrameInfo mCurrentFrameInfo = null;
    private LinkedBlockingDeque<InputInfo> mInputBuffers = new LinkedBlockingDeque<>(6);
    private LinkedBlockingDeque<FrameInfo> mOutputBuffers = new LinkedBlockingDeque<>(4);
    private int mDecodeWidth = 0;
    private int mDecodeHeight = 0;
    private int mDecodeRotation = 0;

    /**
     * care must be taken if the codec is flushed immediately or shortly
     * after start, before any output buffer or output format change has been returned, as the codec
     * specific data may be lost during the flush. You must resubmit the data using buffers marked with
     * #BUFFER_FLAG_CODEC_CONFIG after such flush to ensure proper codec operation.
     */
    private boolean mFlushEnable = false;

    public VideoDecoder() {
    }

    @Override
    public boolean prepare(MediaFormat format) throws IOException {
        ALog.i(TAG, "prepare:" + mPrepared);
        if (mPrepared && mDecoder != null) {
            return true;
        }
        if (format == null) {
            return false;
        }
        mInputFormat = format;
        String mime = format.getString(MediaFormat.KEY_MIME);
        mDecoder = MediaCodec.createDecoderByType(mime);
        mDecoder.setCallback(mCallback);
        mDecoder.configure(mInputFormat, null, null, 0);
        mDecoder.start();
        mFlushEnable = false;
        return mPrepared = true;
    }

    @Override
    public boolean flush() {
        ALog.i(TAG, "flush");
        if (mFlushEnable) {
            mCurrentInputInfo = null;
            mCurrentFrameInfo = null;
            mInputBuffers.clear();
            mOutputBuffers.clear();
            mDecoder.flush();
            mDecoder.start();
            mFlushEnable = false;
            return true;
        }
        return false;
    }

    @Override
    public void release() {
        ALog.i(TAG, "release");
        if (mDecoder != null) {
            mDecoder.stop();
            mDecoder.setCallback(null);
            mDecoder.release();
            mDecoder = null;
            mFlushEnable = false;
            mPrepared = false;
        }
    }

    @Override
    public InputInfo dequeueInputBuffer() {
        if (mDecoder == null) {
            return null;
        }
        if (mCurrentInputInfo != null) {
            //保证外部必须先返回上一个buffer,才能取走下一个buffer
            ALog.i(TAG, "dequeueInputBuffer same frame again");
            return null;
        }
        try {
            mCurrentInputInfo = mInputBuffers.pollLast(100, TimeUnit.MILLISECONDS);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        if (mCurrentInputInfo != null) {
            ALog.i(TAG, "dequeueInputBuffer:" + mCurrentInputInfo.time + ";bufferIndex:" + mCurrentInputInfo.inputIndex);
        }
        return mCurrentInputInfo;
    }

    @Override
    public void queueInputBuffer(InputInfo inputInfo) {
        if (mDecoder != null && inputInfo != null) {
            if (inputInfo != null) {
                ALog.i(TAG, "queueInputBuffer,time:" + inputInfo.time + ";size:" + inputInfo.size + ";flag:" + inputInfo.lastFrameFlag + ";index:" + inputInfo.inputIndex);
            }
            mDecoder.queueInputBuffer(inputInfo.inputIndex, 0, inputInfo.size <= 0 ? 0 : inputInfo.size,
                    inputInfo.time <= 0 ? 0 : inputInfo.time, inputInfo.lastFrameFlag ? MediaCodec.BUFFER_FLAG_END_OF_STREAM : 0);
            if (mCurrentInputInfo != null && mCurrentInputInfo.inputIndex == inputInfo.inputIndex) {
                mCurrentInputInfo = null;
            }
        }
    }

    @Override
    public FrameInfo dequeueOutputBuffer() {
        if (mDecoder == null) {
            return null;
        }
        if (mCurrentFrameInfo != null) {
            //保证外部必须先返回上一个buffer,才能取走下一个buffer
            ALog.i(TAG, "dequeueOutputBuffer same frame again");
            return null;
        }
        try {
            mCurrentFrameInfo = mOutputBuffers.pollLast(100, TimeUnit.MILLISECONDS);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        if (mCurrentFrameInfo != null) {
            //ALog.i(TAG, "dequeueOutputBuffer:" + mCurrentFrameInfo.presentationTimeUs + ";bufferIndex:" + mCurrentFrameInfo.outputIndex);
        }
        return mCurrentFrameInfo;
    }

    @Override
    public void queueOutputBuffer(FrameInfo frameInfo) {
        if (mDecoder != null && frameInfo != null) {
            if (frameInfo != null) {
                //ALog.i(TAG, "releaseOutputBuffer:" + frameInfo.presentationTimeUs + ";bufferIndex:" + mCurrentFrameInfo.outputIndex);
            }
            mDecoder.releaseOutputBuffer(frameInfo.outputIndex, false);
            if (mCurrentFrameInfo != null && mCurrentFrameInfo.outputIndex == frameInfo.outputIndex) {
                mCurrentFrameInfo = null;
            }
        }
    }

    private MediaCodec.Callback mCallback = new MediaCodec.Callback() {

        @Override
        public void onInputBufferAvailable(@NonNull MediaCodec codec, int index) {
            if (index >= 0)
                mInputBuffers.offerFirst(new InputInfo(index, codec.getInputBuffer(index)));
            else
                ALog.i(TAG, "onInputBufferAvailable:" + index);
        }

        @Override
        public void onOutputBufferAvailable(@NonNull MediaCodec codec, int index, @NonNull MediaCodec.BufferInfo info) {
            if (index >= 0 && mOutputFormat != null) {
                FrameInfo frameInfo = new FrameInfo(index, codec.getOutputBuffer(index), info.size,
                        info.presentationTimeUs, mDecodeWidth, mDecodeHeight, mDecodeRotation);
                mOutputBuffers.offerFirst(frameInfo);
            } else {
                ALog.i(TAG, "onOutputBufferAvailable:" + index);
            }
        }

        @Override
        public void onError(@NonNull MediaCodec codec, @NonNull MediaCodec.CodecException e) {
            ALog.e(TAG, "onError", e);
            mDecoder.reset();
            mDecoder.configure(mInputFormat, null, null, 0);
            mDecoder.start();
        }

        @Override
        public void onOutputFormatChanged(@NonNull MediaCodec codec, @NonNull MediaFormat format) {
            mOutputFormat = format;
            if (mOutputFormat != null) {
                if (mOutputFormat.containsKey(MediaFormat.KEY_WIDTH))
                    mDecodeWidth = mOutputFormat.getInteger(MediaFormat.KEY_WIDTH);
                if (mOutputFormat.containsKey(MediaFormat.KEY_HEIGHT))
                    mDecodeHeight = mOutputFormat.getInteger(MediaFormat.KEY_HEIGHT);
                if (mOutputFormat.containsKey(MediaFormat.KEY_ROTATION))
                    mDecodeRotation = mOutputFormat.getInteger(MediaFormat.KEY_ROTATION);
            }
            mFlushEnable = true;
        }
    };
}
