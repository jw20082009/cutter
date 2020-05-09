package com.wilbert.library.clips.abs;

/**
 * author : wilbert
 * e-mail : jw20082009@qq.com
 * time   : 2020/05/09
 * desc   :
 */
public interface IAudioClip {
    int getChannels();
    int getSampleRate();
    long getDuration();
    int getBitrate();
}
