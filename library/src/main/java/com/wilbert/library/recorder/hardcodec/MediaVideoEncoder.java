package com.wilbert.library.recorder.hardcodec;

import android.media.MediaCodec;
import android.media.MediaCodecInfo;
import android.media.MediaCodecList;
import android.media.MediaFormat;
import android.opengl.EGLContext;
import android.util.Log;
import android.view.Surface;

import com.wilbert.library.recorder.interfaces.IMediaEncoderListener;
import com.wilbert.library.recorder.interfaces.IMuxer;
import com.wilbert.library.recorder.interfaces.IVideoEncoder;

import java.io.IOException;
import java.nio.ByteBuffer;

/**
 * Created by Android Studio.
 * User: wilbert jw20082009@qq.com
 * Date: 2019/7/25 14:43
 */
public class MediaVideoEncoder extends MediaEncoder implements IVideoEncoder {

    protected static final String TAG = "MediaVideoEncoder";
    protected static final String MIME_TYPE = "video/avc";
    protected int mWidth;
    protected int mHeight;
    protected RenderHandler mRenderHandler;
    protected static final int FRAME_RATE = 25;
    protected static final float BPP = 0.25f;
    protected Surface mSurface;
    protected int expectFrameRate = -1;
    protected int expectBitrate = -1;

    public MediaVideoEncoder(IMuxer muxer, IMediaEncoderListener listener, Integer width, Integer height, Integer frameRate, Integer bitrate) {
        super(muxer, listener);
        if (DEBUG) Log.i(TAG, "MediaVideoEncoder: ");
        mWidth = width == null ? mWidth : width;
        mHeight = height == null ? mHeight : height;
        expectFrameRate = frameRate == null ? expectFrameRate : frameRate;
        expectBitrate = bitrate == null ? expectBitrate : bitrate;
        mRenderHandler = RenderHandler.createHandler(TAG);
    }


    public boolean frameAvailableSoon(final float[] tex_matrix) {
        boolean result;
        if (result = super.frameAvailableSoon())
            mRenderHandler.draw(tex_matrix);
        return result;
    }

    public boolean frameAvailableSoon(final float[] tex_matrix, final float[] mvp_matrix) {
        boolean result;
        if (result = super.frameAvailableSoon())
            mRenderHandler.draw(tex_matrix, mvp_matrix);
        return result;
    }

    @Override
    public void setEglContext(final EGLContext shared_context, final int tex_id) {
        mRenderHandler.setEglContext(shared_context, tex_id, mSurface, true);
    }

    @Override
    public boolean frameAvailableSoon(int textureId, final float[] mvp_matrix) {
        boolean result;
        if (result = super.frameAvailableSoon())
            mRenderHandler.draw(textureId, mvp_matrix);
        return result;
    }

    @Override
    public boolean frameAvailableSoon() {
        boolean result;
        if (result = super.frameAvailableSoon())
            mRenderHandler.draw(null);
        return result;
    }

    @Override
    public void prepare() throws IOException {
        if (DEBUG) Log.i(TAG, "prepare: ");
        mTrackIndex = -1;
        mMuxerStarted = mIsEOS = false;

        final MediaCodecInfo videoCodecInfo = selectVideoCodec(MIME_TYPE);
        if (videoCodecInfo == null) {
            Log.e(TAG, "Unable to find an appropriate codec for " + MIME_TYPE);
            return;
        }
        if (DEBUG) Log.i(TAG, "selected codec: " + videoCodecInfo.getName());

        final MediaFormat format = MediaFormat.createVideoFormat(MIME_TYPE, mWidth, mHeight);
        format.setInteger(MediaFormat.KEY_COLOR_FORMAT, MediaCodecInfo.CodecCapabilities.COLOR_FormatSurface);    // API >= 18
        format.setInteger(MediaFormat.KEY_BIT_RATE, expectBitrate > 0 ? expectBitrate : calcBitRate());
        format.setInteger(MediaFormat.KEY_FRAME_RATE, expectFrameRate > 0 ? expectFrameRate : FRAME_RATE);
        onConfigureFormat(format);
        if (DEBUG) Log.i(TAG, "format: " + format);

        mMediaCodec = MediaCodec.createEncoderByType(MIME_TYPE);
        mMediaCodec.configure(format, null, null, MediaCodec.CONFIGURE_FLAG_ENCODE);
        // get Surface for encoder input
        // this method only can call between #configure and #start
        mSurface = mMediaCodec.createInputSurface();    // API >= 18
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

    protected void onConfigureFormat(final MediaFormat format) {
        format.setInteger(MediaFormat.KEY_I_FRAME_INTERVAL, 10);
    }

    protected int calcBitRate() {
        final int bitrate = (int) (BPP * FRAME_RATE * mWidth * mHeight);
        Log.i(TAG, String.format("bitrate=%5.2f[Mbps]", bitrate / 1024f / 1024f));
        return bitrate;
    }

    /**
     * select the first codec that match a specific MIME type
     *
     * @param mimeType
     * @return null if no codec matched
     */
    protected static final MediaCodecInfo selectVideoCodec(final String mimeType) {
        if (DEBUG) Log.v(TAG, "selectVideoCodec:");

        // get the list of available codecs
        final int numCodecs = MediaCodecList.getCodecCount();
        for (int i = 0; i < numCodecs; i++) {
            final MediaCodecInfo codecInfo = MediaCodecList.getCodecInfoAt(i);

            if (!codecInfo.isEncoder()) {    // skipp decoder
                continue;
            }
            // select first codec that match a specific MIME type and color format
            final String[] types = codecInfo.getSupportedTypes();
            for (int j = 0; j < types.length; j++) {
                if (types[j].equalsIgnoreCase(mimeType)) {
                    if (DEBUG) Log.i(TAG, "codec:" + codecInfo.getName() + ",MIME=" + types[j]);
                    final int format = selectColorFormat(codecInfo, mimeType);
                    if (format > 0) {
                        return codecInfo;
                    }
                }
            }
        }
        return null;
    }

    /**
     * select color format available on specific codec and we can use.
     *
     * @return 0 if no colorFormat is matched
     */
    protected static final int selectColorFormat(final MediaCodecInfo codecInfo, final String mimeType) {
        if (DEBUG) Log.i(TAG, "selectColorFormat: ");
        int result = 0;
        final MediaCodecInfo.CodecCapabilities caps;
        try {
            Thread.currentThread().setPriority(Thread.MAX_PRIORITY);
            caps = codecInfo.getCapabilitiesForType(mimeType);
        } finally {
            Thread.currentThread().setPriority(Thread.NORM_PRIORITY);
        }
        int colorFormat;
        for (int i = 0; i < caps.colorFormats.length; i++) {
            colorFormat = caps.colorFormats[i];
            if (isRecognizedViewoFormat(colorFormat)) {
                if (result == 0)
                    result = colorFormat;
                break;
            }
        }
        if (result == 0)
            Log.e(TAG, "couldn't find a good color format for " + codecInfo.getName() + " / " + mimeType);
        return result;
    }

    /**
     * color formats that we can use in this class
     */
    protected static int[] recognizedFormats;

    static {
        recognizedFormats = new int[]{
//        	MediaCodecInfo.CodecCapabilities.COLOR_FormatYUV420Planar,
//        	MediaCodecInfo.CodecCapabilities.COLOR_FormatYUV420SemiPlanar,
//        	MediaCodecInfo.CodecCapabilities.COLOR_QCOM_FormatYUV420SemiPlanar,
                MediaCodecInfo.CodecCapabilities.COLOR_FormatSurface,
        };
    }

    private static final boolean isRecognizedViewoFormat(final int colorFormat) {
        if (DEBUG) Log.i(TAG, "isRecognizedViewoFormat:colorFormat=" + colorFormat);
        final int n = recognizedFormats != null ? recognizedFormats.length : 0;
        for (int i = 0; i < n; i++) {
            if (recognizedFormats[i] == colorFormat) {
                return true;
            }
        }
        return false;
    }

    @Override
    public void signalEndOfInputStream() {
        if (DEBUG) Log.d(TAG, "sending EOS to encoder");
        mMediaCodec.signalEndOfInputStream();    // API >= 18
        mIsEOS = true;
    }

    @Override
    public void release() {
        if (DEBUG) Log.i(TAG, "release:");
        if (mSurface != null) {
            mSurface.release();
            mSurface = null;
        }
        if (mRenderHandler != null) {
            mRenderHandler.release();
            mRenderHandler = null;
        }
        super.release();
    }

    @Override
    public int getEncodeType() {
        return 0;
    }

    @Override
    public ByteBuffer getBuffer(int width,int height) {
        return null;
    }
}
