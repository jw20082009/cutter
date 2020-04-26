package com.wilbert.library.clips;

import java.io.FileDescriptor;
import java.io.FileNotFoundException;
import java.io.IOException;

/**
 * author : Administrator
 * e-mail : jw20082009@qq.com
 * time   : 2020/04/26
 * desc   :
 */
public interface IPlayer {

    void setDataSource(String filepath) throws IOException;

    void setDataSource(FileDescriptor descriptor);

    void setPlayerListener(IPlayerListener listener);

    void prepare();

    void start();

    void resume();

    void stop();

    void seekTo(long timeUS);

    long getCurrentTimeUs();

    void release();
}
