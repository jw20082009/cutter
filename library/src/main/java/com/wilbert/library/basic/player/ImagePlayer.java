package com.wilbert.library.basic.player;

import android.graphics.Bitmap;
import android.os.Message;
import android.util.Log;

import com.wilbert.library.basic.handler.UIHandler;
import com.wilbert.library.basic.imageloader.ImageRequest;
import com.wilbert.library.basic.imageloader.LocalImageLoader;
import com.wilbert.library.basic.kit.entities.VideoItem;
import com.wilbert.library.basic.renderer.BaseRenderer;

/**
 * Created by Android Studio.
 * User: wilbert jw20082009@qq.com
 * Date: 2019/8/22 16:00
 */
public class ImagePlayer extends UIHandler {
    private final String TAG = "ImagePlayer";
    private long durationTime;
    private long startTime;
    private BaseRenderer renderer;
    private OnCompletionListener completionListener;
    private OnSizeChangedListener onSizeChangedListener;
    private int imageWidth, imageHeight;
    private Bitmap bitmap;
    private VideoItem videoItem;

    public ImagePlayer(BaseRenderer renderer, OnCompletionListener completionListener) {
        this.renderer = renderer;
        this.completionListener = completionListener;
    }

    public void setDataSource(Bitmap bitmap, long durationTime) {
        Log.i(TAG, "setDataSource bitmap" + (bitmap == null));
        this.durationTime = durationTime;
        this.startTime = 0;
        this.bitmap = bitmap;
        if (renderer != null) {
            imageWidth = bitmap.getWidth();
            imageHeight = bitmap.getHeight();
            notifySizeChanged(imageWidth, imageHeight);
        }
    }

    public void start() {
        Log.i(TAG, "start bitmap" + ( bitmap == null ) + hashCode());
        if (bitmap != null && !bitmap.isRecycled()) {
            renderer.setBitmap(bitmap);
            startTime = System.currentTimeMillis();
            sendEmptyUIMessage(MSG_UI_REFRESH_FRAME);
        }
    }

    private void notifySizeChanged(int width, int height) {
        if (this.onSizeChangedListener != null) {
            this.onSizeChangedListener.onSizeChanged(this, width, height);
        }
    }

    public void setOnSizeChangedListener(OnSizeChangedListener listener) {
        this.onSizeChangedListener = listener;
    }

    public Bitmap getBitmap() {
        return bitmap;
    }

    public int getImageWidth() {
        return imageWidth;
    }

    public int getImageHeight() {
        return imageHeight;
    }

    public void setDataSource(VideoItem videoItem) {
        this.videoItem = videoItem;
        if (videoItem != null) {
            Log.i(TAG, "setDataSource " + videoItem.getFilePath());
            this.durationTime = (long) videoItem.getDurationTime();
            this.startTime = 0;
            ImageRequest request = new ImageRequest.ImageRequestBuilder().path(videoItem.getFilePath()).build();
            LocalImageLoader.getInstance(renderer.getContext()).loadImageBitmap(request,
                    new LocalImageLoader.ILocalBitmapListener() {
                        @Override
                        public void onLoadResult(Bitmap bitmap) {
                            setDataSource(bitmap, ImagePlayer.this.durationTime);
                        }
                    });
        }
    }

    private final int MSG_UI_REFRESH_FRAME = 0x01;

    @Override
    public void handleUIMessage(Message msg) {
        super.handleUIMessage(msg);
        switch (msg.what) {
            case MSG_UI_REFRESH_FRAME:
                long timeEllapsed = (System.currentTimeMillis() - startTime);
                if (timeEllapsed < durationTime) {
                    renderer.refreshFrame();
                    sendEmptyUIMessageDelay(MSG_UI_REFRESH_FRAME, 30);
                } else {
                    if (this.completionListener != null) {
                        this.completionListener.onCompletion(this);
                    }
                }
                break;
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        reset();
        renderer = null;
        durationTime = 0;
        if (bitmap != null && !bitmap.isRecycled()) {
            bitmap = null;
        }
    }

    public void reset() {
        Log.i(TAG, "reset bitmap");
        startTime = 0;
        if (renderer != null) {
            renderer.clearBitmap();
        }
    }

    public interface OnCompletionListener {
        void onCompletion(ImagePlayer player);
    }

    public interface OnSizeChangedListener {
        void onSizeChanged(ImagePlayer player, int width, int height);
    }
}
