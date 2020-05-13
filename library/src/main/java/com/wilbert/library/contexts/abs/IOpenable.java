package com.wilbert.library.contexts.abs;

import java.io.Closeable;
import java.io.IOException;

/**
 * author : Administrator
 * e-mail : jw20082009@qq.com
 * time   : 2020/04/26
 * desc   :
 */
public interface IOpenable extends Closeable {
    void open() throws IOException;
}
