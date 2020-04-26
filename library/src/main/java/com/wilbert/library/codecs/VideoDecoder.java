package com.wilbert.library.codecs;

import android.media.MediaCodec;
import android.media.MediaCodecList;
import android.media.MediaFormat;
import android.support.annotation.NonNull;
import android.view.Surface;

import com.wilbert.library.log.ALog;

import java.io.IOException;

import static android.media.MediaCodecList.REGULAR_CODECS;

/**
 * author : Administrator
 * e-mail : jw20082009@qq.com
 * time   : 2020/04/26
 * desc   :
 */
public class VideoDecoder {
    private final String TAG = "VideoDecoder";
    private MediaCodec mDecoder;
    private ISurfaceObtainer mObtainer;
    private boolean mPrepared = false;
    private MediaFormat mInputFormat;
    private MediaFormat mOutputFormat;
    private MediaCodec.Callback mOutCallback;

    /**
     * care must be taken if the codec is flushed immediately or shortly
     * after start, before any output buffer or output format change has been returned, as the codec
     * specific data may be lost during the flush. You must resubmit the data using buffers marked with
     * #BUFFER_FLAG_CODEC_CONFIG after such flush to ensure proper codec operation.
     */
    private boolean mFlushEnable = false;

    public VideoDecoder() {
    }

    public void setSurfaceObtainer(ISurfaceObtainer obtainer) {
        this.mObtainer = obtainer;
    }

    public boolean prepare(MediaFormat format) throws IOException {
        if (mPrepared && mDecoder != null) {
            return true;
        }
        Surface surface = obtainSurface();
        return prepare(format, surface);
    }

    public boolean prepare(MediaFormat format, Surface surface) throws IOException {
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
        if (surface != null) {
            mDecoder.configure(mInputFormat, surface, null, 0);
            mDecoder.start();
            mFlushEnable = false;
            mPrepared = true;
        }
        return mPrepared;
    }

    public boolean flush() {
        if (mFlushEnable) {
            mDecoder.flush();
            mDecoder.start();
            mFlushEnable = false;
            return true;
        }
        return false;
    }

    public void setCallback(MediaCodec.Callback callback) {
        mOutCallback = callback;
    }

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

    private Surface obtainSurface() {
        if (mObtainer != null) {
            Surface surface = mObtainer.getSurface();
            if (surface != null && surface.isValid()) {
                return surface;
            }
        }
        return null;
    }

    private MediaCodec.Callback mCallback = new MediaCodec.Callback() {

        @Override
        public void onInputBufferAvailable(@NonNull MediaCodec codec, int index) {
            if (mOutCallback != null) {
                mOutCallback.onInputBufferAvailable(codec, index);
            }
        }

        @Override
        public void onOutputBufferAvailable(@NonNull MediaCodec codec, int index, @NonNull MediaCodec.BufferInfo info) {
            mFlushEnable = true;
            if (mOutCallback != null) {
                mOutCallback.onOutputBufferAvailable(codec, index, info);
            }
        }

        @Override
        public void onError(@NonNull MediaCodec codec, @NonNull MediaCodec.CodecException e) {
            ALog.e(TAG, "onError", e);
            Surface surface = obtainSurface();
            if (surface != null) {
                mDecoder.reset();
                mDecoder.configure(mInputFormat, surface, null, 0);
                mDecoder.start();
            }
            if (mOutCallback != null) {
                mOutCallback.onError(codec, e);
            }
        }

        @Override
        public void onOutputFormatChanged(@NonNull MediaCodec codec, @NonNull MediaFormat format) {
            mFlushEnable = true;
            mOutputFormat = format;
            if (mOutCallback != null) {
                mOutCallback.onOutputFormatChanged(codec, format);
            }
        }
    };
}
