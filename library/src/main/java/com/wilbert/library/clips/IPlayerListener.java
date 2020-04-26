package com.wilbert.library.clips;

/**
 * author : Administrator
 * e-mail : jw20082009@qq.com
 * time   : 2020/04/26
 * desc   :
 */
public interface IPlayerListener {

    void onPrepared(IPlayer clip);

    void onDestroy(IPlayer clip);

    void onError(IPlayer clip, Throwable error);
}
