package com.wilbert.library.clips;

import android.media.MediaFormat;
import android.support.annotation.NonNull;
import android.text.TextUtils;
import android.view.Surface;

import com.wilbert.library.codecs.IDecodeListener;
import com.wilbert.library.codecs.IExtractorListener;
import com.wilbert.library.codecs.InputInfo;
import com.wilbert.library.codecs.VideoDecoderWrapper;
import com.wilbert.library.codecs.VideoExtractor;
import com.wilbert.library.codecs.VideoExtractorWrapper;

import java.io.FileDescriptor;
import java.io.IOException;

/**
 * author : Administrator
 * e-mail : jw20082009@qq.com
 * time   : 2020/04/26
 * desc   :
 */
public class VideoClip implements IPlayer {
    private final String TAG = "VideoClip" + hashCode();
    private VideoExtractorWrapper mExtractor;
    private VideoDecoderWrapper mDecoder;
    private FileDescriptor mDescriptor;
    private IPlayerListener mListener;
    private boolean mPrepared = false;
    private Object mLock = new Object();
    private String mFilePath = null;
    private Surface mSurface;

    public VideoClip() {
    }

    @Override
    public void setDataSource(String filepath) throws IOException {
        synchronized (mLock) {
            mFilePath = filepath;
        }
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
    public void prepare(Surface surface) {
        synchronized (mLock) {
            if (TextUtils.isEmpty(mFilePath) && mDescriptor == null) {
                return;
            }
            mSurface = surface;
            mExtractor = new VideoExtractorWrapper();
            mExtractor.setListener(extractorListener);
            mExtractor.prepare(mFilePath, VideoExtractor.Type.VIDEO);
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
        synchronized (mLock) {
            if (mExtractor != null) {
                mExtractor.release();
                mExtractor = null;
            }
            if (mDecoder != null) {
                mDecoder.release();
                mDecoder = null;
            }
        }
    }

    @NonNull
    @Override
    public String toString() {
        return "VideoClip[" + hashCode() + "]";
    }

    public IExtractorListener extractorListener = new IExtractorListener() {
        @Override
        public void onInputBufferAvailable(InputInfo inputInfo) {
            mDecoder.queueInput(inputInfo);
        }

        @Override
        public void onPrepared(VideoExtractorWrapper extractor) {
            synchronized (mLock) {
                if (mDecoder == null && extractor != null) {
                    MediaFormat format = extractor.getMediaFormat();
                    if (format != null) {
                        mDecoder = new VideoDecoderWrapper();
                        mDecoder.setListener(decodeListener);
                        mDecoder.prepare(format, mSurface);
                    }
                }
            }
        }

        @Override
        public void onRelease(VideoExtractorWrapper extractor) {
        }

        @Override
        public void onError(VideoExtractorWrapper extractor, Throwable throwable) {

        }
    };

    public IDecodeListener decodeListener = new IDecodeListener() {
        @Override
        public void onInputBufferAvailable(InputInfo inputInfo) {
            synchronized (mLock) {
                if (mExtractor == null || inputInfo == null) {
                    return;
                }
            }
            mExtractor.offerBuffer(inputInfo);
        }

        @Override
        public void onPrepared(VideoDecoderWrapper decoder) {
            mPrepared = true;
        }

        @Override
        public void onReleasing(VideoDecoderWrapper decoder) {

        }

        @Override
        public void onError(VideoDecoderWrapper decoder, Throwable throwable) {

        }
    };

}
