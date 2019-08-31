package com.wilbert.library.recorder.hardcodec;

import android.media.AudioFormat;
import android.media.MediaCodec;
import android.media.MediaCodecInfo;
import android.media.MediaCodecList;
import android.media.MediaFormat;
import android.util.Log;

import com.wilbert.library.recorder.interfaces.IMediaEncoderListener;
import com.wilbert.library.recorder.interfaces.IMuxer;

import java.io.IOException;
import java.nio.ByteBuffer;

/**
 * Created by Android Studio.
 * User: wilbert jw20082009@qq.com
 * Date: 2019/7/25 15:05
 */
public class MediaAudioEncoder extends MediaEncoder {

    private static final boolean DEBUG = false;    // TODO set false on release
    private static final String TAG = "MediaAudioEncoder";
    protected static final String MIME_TYPE = "audio/mp4a-latm";
    protected static final int BYTES_PER_SAMPLE = 16;
    protected int mSampleRate = 44100;    // 44.1[KHz] is only setting guaranteed to be available on all devices.
    protected int mBitRate = 64000;
    protected int mChannels = 2;
    protected ByteBuffer byteBuffer;
    protected static int SAMPLES_PER_FRAME = 1764;    // AAC, bytes/frame/channel

    public MediaAudioEncoder(IMuxer muxer, IMediaEncoderListener listener, int sampleRate, int channels) {
        super(muxer, listener);
        this.mSampleRate = sampleRate;
        this.mChannels = channels;
    }

    private int calcSamplesPerFrame(int channelNum, int sampleRate, int bytesPerSample) {
        return (int) (1.0f * sampleRate * channelNum / 1000 * 10 / 8 * bytesPerSample);
    }

    @Override
    public void prepare() throws IOException {
        if (DEBUG) Log.v(TAG, "prepare:");
        mTrackIndex = -1;
        mMuxerStarted = mIsEOS = false;
        // prepare MediaCodec for AAC encoding of audio data from inernal mic.
        final MediaCodecInfo audioCodecInfo = selectAudioCodec(MIME_TYPE);
        if (audioCodecInfo == null) {
            Log.e(TAG, "Unable to find an appropriate codec for " + MIME_TYPE);
            return;
        }
        if (DEBUG) Log.i(TAG, "selected codec: " + audioCodecInfo.getName());
        SAMPLES_PER_FRAME = calcSamplesPerFrame(mChannels, mSampleRate, BYTES_PER_SAMPLE);
        final MediaFormat audioFormat = MediaFormat.createAudioFormat(MIME_TYPE, mSampleRate, mChannels);
        audioFormat.setInteger(MediaFormat.KEY_AAC_PROFILE, MediaCodecInfo.CodecProfileLevel.AACObjectLC);
        audioFormat.setInteger(MediaFormat.KEY_CHANNEL_MASK, mChannels == 1 ? AudioFormat.CHANNEL_IN_MONO : AudioFormat.CHANNEL_IN_STEREO);
        audioFormat.setInteger(MediaFormat.KEY_BIT_RATE, mBitRate);
        audioFormat.setInteger(MediaFormat.KEY_CHANNEL_COUNT, mChannels);
        if (DEBUG) Log.i(TAG, "format: " + audioFormat);
        mMediaCodec = MediaCodec.createEncoderByType(MIME_TYPE);
        mMediaCodec.configure(audioFormat, null, null, MediaCodec.CONFIGURE_FLAG_ENCODE);
        mMediaCodec.start();
        if (DEBUG) Log.i(TAG, "prepare finishing");
        if (mListener != null) {
            try {
                mListener.onPrepared(this);
            } catch (final Exception e) {
                Log.e(TAG, "prepare:", e);
            }
        }
    }

    /**
     * select the first codec that match a specific MIME type
     *
     * @param mimeType
     * @return
     */
    private static final MediaCodecInfo selectAudioCodec(final String mimeType) {
        if (DEBUG) Log.v(TAG, "selectAudioCodec:");

        MediaCodecInfo result = null;
        // get the list of available codecs
        final int numCodecs = MediaCodecList.getCodecCount();
        LOOP:
        for (int i = 0; i < numCodecs; i++) {
            final MediaCodecInfo codecInfo = MediaCodecList.getCodecInfoAt(i);
            if (!codecInfo.isEncoder()) {    // skipp decoder
                continue;
            }
            final String[] types = codecInfo.getSupportedTypes();
            for (int j = 0; j < types.length; j++) {
                if (DEBUG) Log.i(TAG, "supportedType:" + codecInfo.getName() + ",MIME=" + types[j]);
                if (types[j].equalsIgnoreCase(mimeType)) {
                    if (result == null) {
                        result = codecInfo;
                        break LOOP;
                    }
                }
            }
        }
        return result;
    }
}
