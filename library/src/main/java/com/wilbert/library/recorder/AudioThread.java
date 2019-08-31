package com.wilbert.library.recorder;

import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.util.Log;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

/**
 * Created by Android Studio.
 * User: wilbert jw20082009@qq.com
 * Date: 2019/7/25 11:25
 */
public class AudioThread extends Thread {
    private static final String TAG = "AudioThread";
    private static final boolean DEBUG = false;
    protected int mSampleRate = 44100;    // 44.1[KHz] is only setting guaranteed to be available on all devices.
    protected int mChannels = 2;
    protected static final int AUDIO_FMT = AudioFormat.ENCODING_PCM_16BIT;
    protected static int SAMPLES_PER_FRAME = 1764;
    protected boolean isCapturing = false;
    protected final Object mSync = new Object();
    protected ByteBuffer byteBuffer;
    protected IAudioRecordListener listener;

    private static final int[] AUDIO_SOURCES = new int[]{
            MediaRecorder.AudioSource.MIC,
            MediaRecorder.AudioSource.DEFAULT,
            MediaRecorder.AudioSource.CAMCORDER,
            MediaRecorder.AudioSource.VOICE_COMMUNICATION,
            MediaRecorder.AudioSource.VOICE_RECOGNITION,
    };

    public AudioThread(IAudioRecordListener listener) {
        this.listener = listener;
    }

    public int getSampleRate() {
        return mSampleRate;
    }

    public int getChannelsNum() {
        return mChannels;
    }

    public int getSampleFmt() {
        return AUDIO_FMT;
    }

    @Override
    public void run() {
        android.os.Process.setThreadPriority(android.os.Process.THREAD_PRIORITY_URGENT_AUDIO);
        try {
            final int min_buffer_size = AudioRecord.getMinBufferSize(
                    mSampleRate, mChannels == 1 ? AudioFormat.CHANNEL_IN_MONO : AudioFormat.CHANNEL_IN_STEREO, AUDIO_FMT
            );
            onBufferSizeChanged(SAMPLES_PER_FRAME);
            AudioRecord audioRecord = null;
            for (final int source : AUDIO_SOURCES) {
                try {
                    audioRecord = new AudioRecord(
                            source, mSampleRate,
                            mChannels == 1 ? AudioFormat.CHANNEL_IN_MONO : AudioFormat.CHANNEL_IN_STEREO, AUDIO_FMT, min_buffer_size);
                    if (audioRecord.getState() != AudioRecord.STATE_INITIALIZED)
                        audioRecord = null;
                    isCapturing = true;
                    notifyPrepared();
                } catch (final Exception e) {
                    audioRecord = null;
                }
                if (audioRecord != null) break;
            }
            if (audioRecord != null) {
                try {
                    if (isCapturing) {
                        if (DEBUG) Log.v(TAG, "AudioThread:start audio recording");
                        final byte[] buffers = new byte[SAMPLES_PER_FRAME];
                        int readBytes;
                        audioRecord.startRecording();
                        try {
                            for (; isCapturing; ) {
                                // read audio data from internal mic
                                readBytes = audioRecord.read(buffers, 0, SAMPLES_PER_FRAME);
                                ByteBuffer buf = addEffectToPcm(buffers, readBytes);
                                if (readBytes > 0) {
                                    // set audio data to encoder
                                    buf.position(readBytes);
                                    buf.flip();
                                    notifyFrameAvaliable(buf, readBytes);
                                }
                            }
                            notifyStop();
                        } finally {
                            audioRecord.stop();
                        }
                    }
                } finally {
                    notifyRelease();
                    audioRecord.release();
                }
            } else {
                Log.e(TAG, "failed to initialize AudioRecord");
            }
        } catch (final Exception e) {
            Log.e(TAG, "AudioThread#run", e);
        }
        if (DEBUG) Log.v(TAG, "AudioThread:finished");
    }

    @Override
    public synchronized void start() {
        synchronized (mSync) {
            isCapturing = false;
        }
        super.start();
    }

    public void stopRecording() {
        synchronized (mSync) {
            isCapturing = false;
        }
    }

    private void notifyFrameAvaliable(ByteBuffer buf, int bytes) {
        if (this.listener != null) {
            this.listener.onFrameAvaliable(buf, bytes);
        }
    }

    private void notifyStop() {
        if (this.listener != null) {
            this.listener.onStop();
        }
    }

    private void notifyPrepared() {
        if (this.listener != null) {
            this.listener.onPrepared();
        }
    }

    private void notifyRelease() {
        if (this.listener != null) {
            this.listener.onRelease();
        }
    }

    protected ByteBuffer addEffectToPcm(byte[] byteBuffer, int size) {
        if (this.byteBuffer == null)
            this.byteBuffer = ByteBuffer.allocateDirect(SAMPLES_PER_FRAME).order(ByteOrder.nativeOrder());
        this.byteBuffer.clear();
        this.byteBuffer.put(byteBuffer);
        return this.byteBuffer;
    }

    protected void onBufferSizeChanged(int size) {
    }

    public interface IAudioRecordListener {
        void onPrepared();

        void onFrameAvaliable(ByteBuffer buffer, int length);

        void onStop();

        void onRelease();
    }
}
