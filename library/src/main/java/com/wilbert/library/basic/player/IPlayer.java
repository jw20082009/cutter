package com.wilbert.library.basic.player;

import com.wilbert.library.basic.kit.entities.VideoItem;

import java.io.IOException;

/**
 * Created by Android Studio.
 * User: wilbert jw20082009@qq.com
 * Date: 2019/8/23 11:32
 */
public interface IPlayer {

    void start();

    void pause();

    void setDataSource(VideoItem path) throws IOException, IllegalArgumentException, SecurityException, IllegalStateException;

    void setNextPlayer(IPlayer next);
}
