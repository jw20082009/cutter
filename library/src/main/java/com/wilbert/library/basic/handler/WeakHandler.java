
package com.wilbert.library.basic.handler;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;

import java.lang.ref.SoftReference;

/**
 * Created by walljiang on 2017/10/3.
 */

public class WeakHandler extends Handler {

    SoftReference<HandlerCallback> callbackWeakReference;

    public WeakHandler(Looper looper, HandlerCallback callback) {
        super(looper);
        this.callbackWeakReference = new SoftReference(callback);
    }

    @Override
    public void handleMessage(Message msg) {
        super.handleMessage(msg);
        if (callbackWeakReference != null) {
            HandlerCallback callback = callbackWeakReference.get();
            if (callback != null) {
                callback.handleMessage(msg);
            }
        }
    }

    public void release() {
        removeCallbacks(null);
        if (callbackWeakReference != null) {
            callbackWeakReference.clear();
            callbackWeakReference = null;
        }
        if (getLooper() != Looper.getMainLooper()) {
            getLooper().quit();
        }
    }
}
