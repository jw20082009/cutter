package com.wilbert.library.recorder.hardcodec;

import android.media.MediaCodec;
import android.media.MediaFormat;
import android.util.Log;

import com.wilbert.library.recorder.interfaces.IVideoEncoder;
import com.wilbert.library.recorder.interfaces.IMediaEncoderListener;
import com.wilbert.library.recorder.interfaces.IMuxer;

import java.io.IOException;

/**
 * Created by Android Studio.
 * User: wilbert jw20082009@qq.com
 * Date: 2019/7/25 15:00
 */
public class MediaIFrameEncoder extends MediaVideoEncoder {

    int mIFrameInterval = 10;

    public MediaIFrameEncoder(IMuxer muxer, IMediaEncoderListener listener, Integer width, Integer height, Integer frameRate, Integer bitrate, Integer iFrameInterval) {
        super(muxer, listener, width, height, frameRate, bitrate);
        this.mIFrameInterval = iFrameInterval == null ? mIFrameInterval : iFrameInterval;
    }

    @Override
    protected void onConfigureFormat(MediaFormat format) {
        format.setInteger(MediaFormat.KEY_I_FRAME_INTERVAL, mIFrameInterval);
    }

    @Override
    public void prepare() throws IOException {
        try {
            super.prepare();
        } catch (MediaCodec.CodecException e) {
            /** Nexus5上-1代表全关键帧 */
            if (mIFrameInterval == 0) {
                mIFrameInterval = -1;
                super.prepare();
            } else if (mIFrameInterval == -1) {
                mIFrameInterval = 0;
                super.prepare();
            } else {
                throw e;
            }
        }
    }

    @Override
    protected int calcBitRate() {
        final int bitrate = (int) (0.4f * FRAME_RATE * mWidth * mHeight);
        Log.i(TAG, String.format("bitrate=%5.2f[Mbps]", bitrate / 1024f / 1024f));
        return bitrate;
    }
}
