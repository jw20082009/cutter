package com.wilbert.library.codecs;

import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;

/**
 * author : wilbert
 * e-mail : jw20082009@qq.com
 * time   : 2020/04/26
 * desc   :
 */
public class VideoExtractorWrapper {

    private VideoExtractor mExtractor;
    private ExtractorHandler mHandler;
    private Object mLock = new Object();

    public VideoExtractorWrapper() {
    }

    public void prepare() {
        synchronized (mLock) {
            initHandler();
            mHandler.removeMessages(MSG_PREPARE);
            mHandler.sendEmptyMessage(MSG_PREPARE);
        }
    }

    private void initHandler() {
        if (mHandler == null) {
            HandlerThread thread = new HandlerThread("VideoExtractorWrapper[" + hashCode() + "]");
            thread.start();
            mHandler = new ExtractorHandler(thread.getLooper());
        }
    }

    private final int MSG_PREPARE = 0x01;

    class ExtractorHandler extends Handler {

        public ExtractorHandler(Looper looper) {
            super(looper);
        }

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case MSG_PREPARE:
                    mExtractor = new VideoExtractor();
                    mExtractor.prepare()
                    break;
            }
        }

        public void release() {

        }
    }
}
