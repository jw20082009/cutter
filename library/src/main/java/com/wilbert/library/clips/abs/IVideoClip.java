package com.wilbert.library.clips.abs;

import com.wilbert.library.codecs.abs.FrameInfo;

/**
 * author : wilbert
 * e-mail : jw20082009@qq.com
 * time   : 2020/05/09
 * desc   :
 */
public interface IVideoClip {

    void prepare(String filepath);

    void setOnPreparedListener(IPreparedListener listener);

    void seekTo(long timeUs);

    void speedUp(float speed);

    String getFilepath();
}
