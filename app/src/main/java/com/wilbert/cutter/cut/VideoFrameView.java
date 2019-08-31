
package com.wilbert.cutter.cut;

import android.content.Context;
import android.graphics.Bitmap;
import android.media.MediaMetadataRetriever;
import android.os.Build;
import android.util.AttributeSet;

import com.wilbert.library.basic.widgets.videocutview.IMetadataRetriever;
import com.wilbert.library.basic.widgets.videocutview.VideoCutViewBar;

public class VideoFrameView extends VideoCutViewBar {

    MediaMetadataRetriever mediaMetadataRetriever;

    public VideoFrameView(Context context) {
        super(context);
        init();
    }

    public VideoFrameView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public VideoFrameView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        mediaMetadataRetriever = new MediaMetadataRetriever();
    }

    @Override
    public IMetadataRetriever getMetadataRetriever() {
        return new IMetadataRetriever() {
            @Override
            public void setDataSource(String filepath) {
                mediaMetadataRetriever.setDataSource(filepath);
            }

            @Override
            public int getVideoWidth() {
                return Integer.parseInt(mediaMetadataRetriever
                        .extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_WIDTH));
            }

            @Override
            public int getVideoHeight() {
                return Integer.parseInt(mediaMetadataRetriever
                        .extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_HEIGHT));
            }

            @Override
            public int getVideoRotation() {
                return Integer.parseInt(mediaMetadataRetriever
                        .extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_ROTATION));
            }

            @Override
            public int getVideoDuration() {
                return Integer.parseInt(mediaMetadataRetriever
                        .extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION));
            }

            @Override
            public Bitmap getScaledFrameAtTime(long timeUs, int width, int height) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
                    return mediaMetadataRetriever.getScaledFrameAtTime(timeUs,
                            MediaMetadataRetriever.OPTION_CLOSEST_SYNC, width, height);
                } else {
                    return mediaMetadataRetriever.getFrameAtTime(timeUs,
                            MediaMetadataRetriever.OPTION_CLOSEST_SYNC);
                }
            }

            @Override
            public void release() {
                mediaMetadataRetriever.release();
            }
        };
    }
}
