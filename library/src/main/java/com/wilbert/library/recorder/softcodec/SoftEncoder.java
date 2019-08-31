
package com.wilbert.library.recorder.softcodec;

import android.opengl.EGLContext;
import android.util.Log;

import com.wilbert.library.recorder.AudioThread;
import com.wilbert.library.recorder.Recorder;
import com.wilbert.library.recorder.interfaces.IEncoder;
import com.wilbert.library.recorder.interfaces.IMediaEncoderListener;
import com.wilbert.library.recorder.interfaces.IVideoEncoder;

import java.io.IOException;
import java.nio.ByteBuffer;

/**
 * Created by Android Studio. User: wilbert jw20082009@qq.com Date: 2019/7/25
 * 20:19
 */
public class SoftEncoder implements IEncoder, IVideoEncoder {
    private final String TAG = "SoftEncoder";

    AudioThread audioThread;

    Recorder.Builder builder;

    ByteBuffer byteBuffer;

    public SoftEncoder(Recorder.Builder builder) {
        this.builder = builder;
    }

    @Override
    public int getEncodeType() {
        return 1;
    }

    @Override
    public ByteBuffer getBuffer(int width, int height) {
        if (this.byteBuffer == null)
            this.byteBuffer = ByteBuffer.allocateDirect(width * height * 4);
        return this.byteBuffer;
    }

    @Override
    public void prepare() throws IOException {
        builder.videoEncodeListener.onPrepared(this);
    }

    @Override
    public void startEncode() {
        Log.i(TAG, "CGELOGTAG startEncode");
    }

    @Override
    public void encode(ByteBuffer buffer, int length) {
    }

    boolean stopped = false;

    @Override
    public void stopEncode() {
        Log.i(TAG, "CGELOGTAG stopEncode");
        audioThread.stopRecording();
    }

    @Override
    public void signalEndOfInputStream() {
    }

    @Override
    public boolean frameAvailableSoon() {
        return true;
    }

    @Override
    public void drain() {
    }

    @Override
    public void release() {
    }

    @Override
    public void setEglContext(EGLContext shared_context, int tex_id) {
    }

    @Override
    public boolean frameAvailableSoon(int textureId, float[] mvp_matrix) {
        return true;
    }

    public String getFilePath() {
        return builder.filePath;
    }

    AudioThread.IAudioRecordListener audioRecordListener = new AudioThread.IAudioRecordListener() {
        @Override
        public void onPrepared() {
            IMediaEncoderListener listener = builder.videoEncodeListener;
            if (listener != null) {
                listener.onPrepared(SoftEncoder.this);
            }
        }

        @Override
        public void onFrameAvaliable(ByteBuffer buffer, int length) {
            encode(buffer, length/2);
        }

        @Override
        public void onStop() {
            stopped = true;
            builder.videoEncodeListener.onStopped(SoftEncoder.this);
        }

        @Override
        public void onRelease() {
        }
    };
}
