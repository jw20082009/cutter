package com.wilbert.library.recorder;

import java.io.IOException;

/**
 * Created by Android Studio.
 * User: wilbert jw20082009@qq.com
 * Date: 2019/7/25 20:02
 */
public interface IRecorder {
    void startRecording() throws IOException;

    String getFilePath();

    void stopRecording();
}
