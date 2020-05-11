package com.wilbert.library.codecs;

import android.media.MediaCodec;
import android.media.MediaCodecList;
import android.media.MediaFormat;
import android.support.annotation.NonNull;

import com.wilbert.library.codecs.abs.IDecoder;
import com.wilbert.library.codecs.abs.InputInfo;
import com.wilbert.library.codecs.abs.FrameInfo;
import com.wilbert.library.log.ALog;

import java.io.IOException;
import java.util.concurrent.LinkedBlockingDeque;

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
    private InputInfo mCurrentInputInfo = null;
    private FrameInfo mCurrentFrameInfo = null;
    private LinkedBlockingDeque<InputInfo> mInputBuffers = new LinkedBlockingDeque<>(6);
    private LinkedBlockingDeque<FrameInfo> mOutputBuffers = new LinkedBlockingDeque<>(4);

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
        if (mPrepared && mDecoder != null) {
            return true;
        }
        if (format == null) {
            return false;
        }
        mInputFormat = format;
        MediaCodecList codecList = new MediaCodecList(REGULAR_CODECS);
        String codecName = codecList.findDecoderForFormat(mInputFormat);
        mDecoder = MediaCodec.createByCodecName(codecName);
        mDecoder.setCallback(mCallback);
        mDecoder.configure(mInputFormat, null, null, 0);
        mDecoder.start();
        mFlushEnable = false;
        return mPrepared = true;
    }

    @Override
    public boolean flush() {
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
        mCurrentInputInfo = mInputBuffers.pollLast();
        return mCurrentInputInfo;
    }

    @Override
    public void queueInputBuffer(InputInfo inputInfo) {
        if (mDecoder != null && inputInfo != null) {
            mDecoder.queueInputBuffer(inputInfo.inputIndex, 0, inputInfo.size, inputInfo.time, inputInfo.lastFrameFlag ? MediaCodec.BUFFER_FLAG_END_OF_STREAM : 0);
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
            ALog.i(TAG, "dequeueOutputBuffer same frame again");
            return mCurrentFrameInfo;
        }
        mCurrentFrameInfo = mOutputBuffers.pollLast();
        return mCurrentFrameInfo;
    }

    @Override
    public void queueOutputBuffer(FrameInfo frameInfo) {
        if (mDecoder != null && frameInfo != null) {
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
            if (index >= 0)
                mOutputBuffers.offerFirst(new FrameInfo(index, codec.getOutputBuffer(index), info));
            else
                ALog.i(TAG, "onOutputBufferAvailable:" + index);
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
            mFlushEnable = true;
        }
    };
}
