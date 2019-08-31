package com.wilbert.library.recorder;

import android.media.audiofx.AudioEffect;
import android.util.Log;

import com.wilbert.library.recorder.hardcodec.HardRecorder;
import com.wilbert.library.recorder.interfaces.IMediaEncoderListener;
import com.wilbert.library.recorder.softcodec.SoftRecorder;

import java.io.IOException;

/**
 * Created by Android Studio.
 * User: wilbert jw20082009@qq.com
 * Date: 2019/7/24 20:54
 */
public class Recorder implements IRecorder {
    private IRecorder recorderStrategy;

    private Recorder(Builder builder) {
        if (builder.codecType == 0) {
            recorderStrategy = new HardRecorder(builder);
        } else {
            recorderStrategy = new SoftRecorder(builder);
        }
    }

    @Override
    public void startRecording() throws IOException {
        recorderStrategy.startRecording();
    }

    @Override
    public String getFilePath() {
        return recorderStrategy.getFilePath();
    }

    @Override
    public void stopRecording() {
        recorderStrategy.stopRecording();
    }

    public static class Builder {
        public String filePath;
        public Integer videoWidth;
        public Integer videoHeight;
        public Integer iFrameInterval;
        public Integer videoFrameRate;
        public Integer videoBitRate;
        public IMediaEncoderListener videoEncodeListener;
        public IMediaEncoderListener audioEncodeListener;
        public int codecType;

        public Builder codecType(int codecType) {
            this.codecType = codecType;
            return this;
        }

        public Builder filePath(String filePath) {
            this.filePath = filePath;
            return this;
        }

        public Builder videoSize(Integer videoWidth, Integer videoHeight) {
            this.videoWidth = videoWidth;
            this.videoHeight = videoHeight;
            return this;
        }

        public Builder iFrameInterval(Integer iFrameInterval) {
            this.iFrameInterval = iFrameInterval;
            return this;
        }

        public Builder videoFrameRate(Integer videoFrameRate) {
            this.videoFrameRate = videoFrameRate;
            return this;
        }

        public Builder videoBitRate(Integer videoBitRate) {
            this.videoBitRate = videoBitRate;
            return this;
        }

        public Builder videoEncodeListener(IMediaEncoderListener listener) {
            this.videoEncodeListener = listener;
            return this;
        }

        public Builder audioEncodeListener(IMediaEncoderListener listener) {
            this.audioEncodeListener = listener;
            return this;
        }

        public Recorder build() {
            if (videoBitRate == -1 && videoFrameRate > 0 && videoWidth > 0 && videoHeight > 0)
                videoBitRate = calcBitRate(videoFrameRate, videoWidth, videoHeight);
            return new Recorder(this);
        }

        protected int calcBitRate(int frameRate, int width, int height) {
            final int bitrate = (int) (0.4f * frameRate * width * height);
            Log.i("RecorderBuilder", String.format("bitrate=%5.2f[Mbps]", bitrate / 1024f / 1024f));
            return bitrate;
        }
    }


}
