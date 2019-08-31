package com.wilbert.library.recorder.softcodec;

import com.wilbert.library.recorder.IRecorder;
import com.wilbert.library.recorder.Recorder;

import java.io.IOException;

/**
 * Created by Android Studio.
 * User: wilbert jw20082009@qq.com
 * Date: 2019/7/25 20:04
 */
public class SoftRecorder implements IRecorder {

    SoftEncoder encoder;

    public SoftRecorder(Recorder.Builder builder) {
        encoder = new SoftEncoder(builder);
    }

    @Override
    public void startRecording() throws IOException {
        encoder.prepare();
        encoder.startEncode();
    }

    @Override
    public String getFilePath() {
        return encoder.getFilePath();
    }

    @Override
    public void stopRecording() {
        encoder.stopEncode();
    }
}
