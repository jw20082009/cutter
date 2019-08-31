package com.wilbert.library.basic.aftereffect;

import android.content.Context;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;

import com.wilbert.library.basic.handler.ThreadHandler;
import com.wilbert.library.basic.utils.OutFileGenerator;
import com.wilbert.library.videoprocessor.VideoProcessor;
import com.wilbert.library.videoprocessor.util.VideoProgressListener;

import java.io.File;
import java.lang.ref.SoftReference;

public class AfterEffectDelegate extends ThreadHandler {

    private final String TAG = "AfterEffectDelegate";

    private AfterEffectDelegate() {
    }

    enum Instance {
        INSTANCE;

        AfterEffectDelegate instance;

        Instance() {
            instance = new AfterEffectDelegate();
        }
    }

    public static AfterEffectDelegate getInstance() {
        return Instance.INSTANCE.instance;
    }

    private String inputVideo;
    private String iframeVideo;
    private SoftReference<Context> reference;
    private VideoProgressListener preListener;
    private int retryTimes = 0;

    public String getPreProgressedVideo(String inputVideo) {
        if (TextUtils.equals(inputVideo, this.inputVideo))
            return iframeVideo;
        else
            return null;
    }

    public String getInputVideo() {
        return inputVideo;
    }

    public void convertIFrameVideo(Context context, String inputVideo) {
        this.inputVideo = inputVideo;
        this.iframeVideo = null;
        this.reference = new SoftReference<>(context);
        removeThreadMessage(MSG_BACK_IFRAME_CONVERT);
        sendEmptyThreadMessageDelay(MSG_BACK_IFRAME_CONVERT, 30);
    }

    private final int MSG_BACK_IFRAME_CONVERT = 0x01;

    @Override
    public void handleThreadMessage(Message msg) {
        super.handleThreadMessage(msg);
        switch (msg.what) {
            case MSG_BACK_IFRAME_CONVERT:
                if (reference != null) {
                    Context context = reference.get();
                    if (!TextUtils.isEmpty(inputVideo) && new File(inputVideo).canRead()) {
                        try {
                            String output = OutFileGenerator.generateIFrameFile(context, inputVideo);
                            if (!new File(output).exists()) {
                                iframeVideo = VideoProcessor.preProcessVideo(context, inputVideo, output, preProgressListener);
                                Log.i(TAG, "preProcessVideo finish " + iframeVideo);
                                preProgressListener.onProgress(100);
                            } else {
                                iframeVideo = output;
                                preProgressListener.onProgress(100);
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    } else {
                        if (retryTimes < 3) {
                            retryTimes++;
                            sendEmptyThreadMessageDelay(MSG_BACK_IFRAME_CONVERT, 30);
                        } else {
                            if (preListener != null) {
                                preListener.onProgress(-1);
                            }
                            Log.i(TAG, "inputFile can't read");
                        }
                    }
                }
                break;
        }
    }

    VideoProgressListener preProgressListener = new VideoProgressListener() {
        @Override
        public void onProgress(float progress) {
            Log.i(TAG, "keyframe progress " + progress);
            if (progress >= 1 && TextUtils.isEmpty(iframeVideo)) {
            } else {
                if (preListener != null) {
                    preListener.onProgress(progress);
                }
            }
        }
    };

    public void setPreProgressListener(VideoProgressListener listener) {
        this.preListener = listener;
    }
}
