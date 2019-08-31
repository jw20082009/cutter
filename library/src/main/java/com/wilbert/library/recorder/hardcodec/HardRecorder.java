package com.wilbert.library.recorder.hardcodec;

import com.wilbert.library.recorder.AudioThread;
import com.wilbert.library.recorder.IRecorder;
import com.wilbert.library.recorder.MediaMuxerWrapper;
import com.wilbert.library.recorder.Recorder;
import com.wilbert.library.recorder.interfaces.IEncoder;
import com.wilbert.library.recorder.interfaces.IMuxer;

import java.io.IOException;
import java.nio.ByteBuffer;

/**
 * Created by Android Studio.
 * User: wilbert jw20082009@qq.com
 * Date: 2019/7/25 19:52
 */
public class HardRecorder implements IRecorder {
    IMuxer muxer;
    IEncoder videoEncoder, audioEncoder;
    AudioThread audioThread;

    public HardRecorder(Recorder.Builder builder) {
        muxer = new MediaMuxerWrapper(builder.filePath);
        videoEncoder = new MediaIFrameEncoder(muxer, builder.videoEncodeListener, builder.videoWidth, builder.videoHeight, builder.videoFrameRate, builder.videoBitRate, builder.iFrameInterval);
        audioThread = new AudioThread(audioRecordListener);
        audioEncoder = new MediaAudioEncoder(muxer, builder.audioEncodeListener, audioThread.getSampleRate(), audioThread.getChannelsNum());
    }

    public void startRecording() throws IOException {
        videoEncoder.prepare();
        audioEncoder.prepare();
        videoEncoder.startEncode();
        audioEncoder.startEncode();
        audioThread.start();
    }

    public String getFilePath() {
        return muxer.getOutputPath();
    }

    public void stopRecording() {
        if (videoEncoder != null)
            videoEncoder.stopEncode();
        if (audioThread != null) {
            audioThread.stopRecording();
        }
        if (audioEncoder != null)
            audioEncoder.stopEncode();
    }

    AudioThread.IAudioRecordListener audioRecordListener = new AudioThread.IAudioRecordListener() {
        @Override
        public void onPrepared() {
        }

        @Override
        public void onFrameAvaliable(ByteBuffer buffer, int length) {
            audioEncoder.encode(buffer, length);
            audioEncoder.frameAvailableSoon();
        }

        @Override
        public void onStop() {
            audioEncoder.frameAvailableSoon();
        }

        @Override
        public void onRelease() {
        }
    };
}
