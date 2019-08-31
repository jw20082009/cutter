package com.wilbert.library.basic.base;

public interface IProcessListener {
    void onSuccess(String result);

    void onFailed(String message);
}
