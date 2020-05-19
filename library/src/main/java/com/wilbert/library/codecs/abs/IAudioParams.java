package com.wilbert.library.codecs.abs;

/**
 * author : wilbert
 * e-mail : jw20082009@qq.com
 * time   : 2020/05/19
 * desc   :
 */
public interface IAudioParams {

    long getDuration();

    int getBitrate();

    int getSampleRate();

    int getChannels();
}
