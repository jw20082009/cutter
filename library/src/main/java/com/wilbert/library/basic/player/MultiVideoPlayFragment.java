package com.wilbert.library.basic.player;

import android.graphics.SurfaceTexture;
import android.media.MediaPlayer;
import android.opengl.GLSurfaceView;
import android.os.Bundle;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Surface;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.wilbert.library.basic.fragments.BaseLoadingHandlerFragment;
import com.wilbert.library.basic.kit.entities.VideoItem;
import com.wilbert.library.basic.renderer.OesRenderer;
import com.wilbert.library.basic.renderer.TransitionRenderer;
import com.wilbert.library.basic.utils.BitmapUtils;
import com.wilbert.library.basic.widgets.AspectGLSurfaceView;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * 连续播放多个视频
 * Created by Android Studio.
 * User: wilbert jw20082009@qq.com
 * Date: 2019/8/15 14:20
 */
public abstract class MultiVideoPlayFragment extends BaseLoadingHandlerFragment {
    protected final String TAG = "MultiVideoPlayFragment";
    public static final float IMAGE_DURATION = 2000;
    protected static final String PARAMS_VIDEOLIST = "videoList";
    protected static final String PARAMS_VIDEO_WIDTH = "width";
    protected static final String PARAMS_VIDEO_HEIGHT = "height";
    protected MediaPlayer firstPlayer, nextMediaPlayer, //负责一段视频播放结束后，播放下一段视频
            cachePlayer;     //负责setNextMediaPlayer的player缓存对象
    //存放所有视频url
    protected List<VideoItem> allVideoListQueue = new ArrayList<>();
    //存放正在播放的视频url
    protected List<VideoItem> playingListQueue = new ArrayList<>();

    //所有player对象的缓存
    protected HashMap<String, MediaPlayer> playersCache = new HashMap<String, MediaPlayer>();
    protected HashMap<String, ImagePlayer> imagePlayersCache = new HashMap<>();
    //当前播放到的视频段落数
    protected int currentVideoIndex;

    protected View mLayoutView;
    protected AspectGLSurfaceView mSurfaceView;
    protected SurfaceTexture mSurfaceTexture;
    protected Surface mPreviewSurface;
    protected OesRenderer mRenderer;
    protected int mVideoWidth, mVideoHeight;
    protected boolean mHasDisplaySetted = false;

    protected abstract int getLayoutId();

    protected abstract int getSurfaceViewId();

    public static Bundle createArgs(ArrayList<String> videoList, Integer outWidth, Integer outHeight) {
        Bundle bundle = new Bundle();
        if (outWidth != null && outHeight != null) {
            bundle.putInt(PARAMS_VIDEO_WIDTH, outWidth);
            bundle.putInt(PARAMS_VIDEO_HEIGHT, outHeight);
        }
        if (videoList != null) {
            ArrayList<VideoItem> videoItems = new ArrayList<>();
            for (String filepath : videoList) {
                VideoItem videoItem = new VideoItem();
                videoItem.setFilePath(filepath);
                if (BitmapUtils.isImageFile(filepath)) {
                    videoItem.setDurationTime(IMAGE_DURATION);
                } else {
                    videoItem.setDurationTime(1.0f);
                }
                videoItem.setStartTime(0f);
                videoItem.setVideoScale(1.0f);
                videoItem.setOffsetX(0);
                videoItem.setOffsetY(0);
                videoItem.setVideoVolume(1.0f);
                videoItems.add(videoItem);
            }
            bundle.putParcelableArrayList(PARAMS_VIDEOLIST, videoItems);
        }
        return bundle;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        mLayoutView = inflater.inflate(getLayoutId(), container, false);
        mSurfaceView = mLayoutView.findViewById(getSurfaceViewId());
        mRenderer = getRenderer();
        Bundle args = getArguments();
        if (args != null) {
            ArrayList<VideoItem> videoItems = args.getParcelableArrayList(PARAMS_VIDEOLIST);
            allVideoListQueue.addAll(videoItems);
            int width = args.getInt(PARAMS_VIDEO_WIDTH, -1);
            int height = args.getInt(PARAMS_VIDEO_HEIGHT, -1);
            if (width > 0 && height > 0) {
                Message uiMsg = obtainUIMessage(MSG_UI_VIDEOSIZE_CHANGED);
                uiMsg.arg1 = width;
                uiMsg.arg2 = height;
                uiMsg.sendToTarget();
            }
            playingListQueue.addAll(allVideoListQueue);
            Message playMsg = obtainThreadMessage(MSG_BACK_INIT_FIRST);
            playMsg.obj = playingListQueue.get(0);
            playMsg.sendToTarget();
        }
        return mLayoutView;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mSurfaceView.setMode(AspectGLSurfaceView.MODE_HEIGHT);
        mSurfaceView.setEGLContextClientVersion(2);
        mRenderer.setTextureListener(mTextureListener);
        mSurfaceView.setRenderer(mRenderer);
        mSurfaceView.setRenderMode(GLSurfaceView.RENDERMODE_WHEN_DIRTY);
    }

    protected OesRenderer getRenderer() {
        return new OesRenderer(mSurfaceView);
    }

    @Override
    public void onResume() {
        super.onResume();
        if (mVideoWidth > 0 && mVideoHeight > 0) {
            Message uiMsg = obtainUIMessage(MSG_UI_VIDEOSIZE_CHANGED);
            uiMsg.arg1 = mVideoWidth;
            uiMsg.arg2 = mVideoHeight;
            uiMsg.sendToTarget();
        }
        resumeCurrentPlayer();
    }

    @Override
    public void onPause() {
        super.onPause();
        pauseCurrentPlayer();
    }

    protected void resumeCurrentPlayer() {
        MediaPlayer player = playersCache.get(String.valueOf(currentVideoIndex));
        Log.i(TAG, "resumeCurrentPlayer1:" + currentVideoIndex);
        if (player != null) {
            Log.i(TAG, "resumeCurrentPlayer2:" + currentVideoIndex);
            player.start();
        }
    }

    protected void pauseCurrentPlayer() {
        MediaPlayer player = playersCache.get(String.valueOf(currentVideoIndex));
        if (player != null) {
            player.pause();
        }
    }

    protected void setMediaPlayerDisplay(MediaPlayer mediaPlayer) {
        if (mediaPlayer != null && mSurfaceTexture != null) {
            Log.i(TAG, "setMediaPlayerDisplay mMediaPlayer setSurface:" + mSurfaceTexture.hashCode());
            if (mPreviewSurface == null) {
                mPreviewSurface = new Surface(mSurfaceTexture);
            }
            mediaPlayer.setSurface(mPreviewSurface);
        }
    }

    protected OesRenderer.TextureListener mTextureListener = new OesRenderer.TextureListener() {
        @Override
        public void onSurfaceTextureCreated(SurfaceTexture surfaceTexture) {
            mSurfaceTexture = surfaceTexture;
            if (firstPlayer != null && !mHasDisplaySetted) {
                setMediaPlayerDisplay(firstPlayer);
                mHasDisplaySetted = true;
            }
        }
    };

    protected final int MSG_BACK_NEXT_PLAYER = 0x01;
    protected final int MSG_BACK_INIT_FIRST = 0x02;
    protected final int MSG_BACK_ADD_VIDEO = 0x03;

    @Override
    protected void handleThreadMessage(Message message) {
        super.handleThreadMessage(message);
        switch (message.what) {
            case MSG_BACK_ADD_VIDEO: {
                Object obj = message.obj;
                if (obj != null) {
                    VideoItem videoItem = (VideoItem) obj;
                    allVideoListQueue.add(videoItem);
                    playingListQueue.add(videoItem);
                    if (BitmapUtils.isImageFile(videoItem.getFilePath())) {
                        ImagePlayer imagePlayer = new ImagePlayer(mRenderer, new ImagePlayer.OnCompletionListener() {
                            @Override
                            public void onCompletion(ImagePlayer player) {
                                onPlayCompleted();
                            }
                        });
                        imagePlayersCache.put(String.valueOf(playingListQueue.size() - 1), imagePlayer);
                        imagePlayer.setDataSource(videoItem);
                    } else {
                        nextMediaPlayer = new MediaPlayer();
                        nextMediaPlayer
                                .setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                                    @Override
                                    public void onCompletion(MediaPlayer mp) {
                                        onPlayCompleted();
                                    }
                                });
                        try {
                            nextMediaPlayer.setDataSource(videoItem.getFilePath());
                            nextMediaPlayer.prepare();
                        } catch (IOException e) {
                            // TODO 自动生成的 catch 块
                            e.printStackTrace();
                        }
                        cachePlayer.setNextMediaPlayer(nextMediaPlayer);
                        cachePlayer = nextMediaPlayer;
                        playersCache.put(String.valueOf(playingListQueue.size() - 1), nextMediaPlayer);
                    }
                }
            }
            break;
            case MSG_BACK_INIT_FIRST: {
                Object obj = message.obj;
                if (obj != null) {
                    VideoItem videoItem = (VideoItem) obj;
                    String filepath = videoItem.getFilePath();
                    if (BitmapUtils.isImageFile(filepath)) {
                        final ImagePlayer imagePlayer = new ImagePlayer(mRenderer, new ImagePlayer.OnCompletionListener() {
                            @Override
                            public void onCompletion(ImagePlayer player) {
                                onPlayCompleted();
                            }
                        });
                        imagePlayer.setOnSizeChangedListener(new ImagePlayer.OnSizeChangedListener() {
                            @Override
                            public void onSizeChanged(ImagePlayer player, int width, int height) {
                                Message uiMsg = obtainUIMessage(MSG_UI_VIDEOSIZE_CHANGED);
                                uiMsg.arg1 = width;
                                uiMsg.arg2 = height;
                                uiMsg.sendToTarget();
                                player.start();
                            }
                        });
                        imagePlayersCache.put("0", imagePlayer);
                        cachePlayer = null;
                        imagePlayer.setDataSource(videoItem);
                    } else {
                        MediaPlayer player = new MediaPlayer();
                        try {
                            player.setDataSource(filepath);
                            player.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                                @Override
                                public void onPrepared(MediaPlayer mp) {
                                    firstPlayer = mp;
                                    playersCache.put("0", mp);
                                    if (mSurfaceTexture != null && !mHasDisplaySetted) {
                                        setMediaPlayerDisplay(mp);
                                        mHasDisplaySetted = true;
                                    }
                                    mp.start();
                                }
                            });
                            player.setOnVideoSizeChangedListener(new MediaPlayer.OnVideoSizeChangedListener() {
                                @Override
                                public void onVideoSizeChanged(MediaPlayer mp, int width, int height) {
                                    firstPlayer = mp;
                                    Message uiMsg = obtainUIMessage(MSG_UI_VIDEOSIZE_CHANGED);
                                    uiMsg.arg1 = width;
                                    uiMsg.arg2 = height;
                                    uiMsg.sendToTarget();
                                }
                            });
                            player.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                                @Override
                                public void onCompletion(MediaPlayer mp) {
                                    onPlayCompleted();
                                }
                            });
                            player.prepareAsync();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        cachePlayer = player;
                    }
                    if (playingListQueue.size() > 1)
                        sendEmptyThreadMessage(MSG_BACK_NEXT_PLAYER);
                }
            }
            break;
            case MSG_BACK_NEXT_PLAYER:
                for (int i = 1; i < playingListQueue.size(); i++) {
                    Log.i(TAG, "MSG_BACK_NEXT_PLAYER:" + i);
                    VideoItem videoItem = playingListQueue.get(i);
                    String filePath = videoItem.getFilePath();
                    if (BitmapUtils.isImageFile(filePath)) {
                        ImagePlayer imagePlayer = new ImagePlayer(mRenderer, new ImagePlayer.OnCompletionListener() {
                            @Override
                            public void onCompletion(ImagePlayer player) {
                                Log.i(TAG, "onCompletion");
                                onPlayCompleted();
                            }
                        });
                        imagePlayer.setDataSource(videoItem);
                        cachePlayer = null;
                        Log.i(TAG, "imagePlayersCache.put:" + i);
                        imagePlayersCache.put(String.valueOf(i), imagePlayer);
                    } else {
                        nextMediaPlayer = new MediaPlayer();
                        nextMediaPlayer
                                .setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                                    @Override
                                    public void onCompletion(MediaPlayer mp) {
                                        onPlayCompleted();
                                    }
                                });
                        try {
                            nextMediaPlayer.setDataSource(filePath);
                            nextMediaPlayer.prepare();
                        } catch (IOException e) {
                            // TODO 自动生成的 catch 块
                            e.printStackTrace();
                        }
                        if (cachePlayer != null) {
                            cachePlayer.setNextMediaPlayer(nextMediaPlayer);
                        }
                        cachePlayer = nextMediaPlayer;
                        playersCache.put(String.valueOf(i), nextMediaPlayer);
                    }
                }
                break;
        }
    }

    public void playVideo(int index) {
        releasePlayers();
        releaseImagePlayers();
        playingListQueue.clear();
        playingListQueue.addAll(allVideoListQueue.subList(index, allVideoListQueue.size()));
        Message playMsg = obtainThreadMessage(MSG_BACK_INIT_FIRST);
        playMsg.obj = playingListQueue.get(0);
        playMsg.sendToTarget();
    }

    public void addVideo(String filePath) {
        Message playMsg = obtainThreadMessage(MSG_BACK_ADD_VIDEO);
        playMsg.obj = filePath;
        playMsg.sendToTarget();
    }

    public void setVideoList(List<VideoItem> videoList) {
        Log.i(TAG, "setVideoList:" + videoList.size());
        releasePlayers();
        playingListQueue.clear();
        allVideoListQueue.clear();
        if (videoList != null && videoList.size() > 0) {
            allVideoListQueue.addAll(videoList);
            playingListQueue.addAll(videoList);
            Message playMsg = obtainThreadMessage(MSG_BACK_INIT_FIRST);
            playMsg.obj = playingListQueue.get(0);
            playMsg.sendToTarget();
        }
    }

    private boolean releasePlayer(int currentVideoIndex) {
        MediaPlayer lastPlayer = playersCache.get(String.valueOf(currentVideoIndex));
        if (lastPlayer != null) {
            lastPlayer.release();
            return true;
        } else {
            ImagePlayer imagePlayer = imagePlayersCache.get(String.valueOf(currentVideoIndex));
            Log.i(TAG, "releasePlayer:" + currentVideoIndex + ";" + (imagePlayer == null));
            if (imagePlayer != null) {
                imagePlayer.reset();
            }
            return false;
        }
    }

    protected void onPlayCompleted() {
        //get next player
        boolean released = releasePlayer(currentVideoIndex);
        currentVideoIndex++;
        MediaPlayer currentPlayer = playersCache.get(String.valueOf(currentVideoIndex));
        if (currentPlayer != null) {
            beforeChangeVideo(currentPlayer.getVideoWidth(), currentPlayer.getVideoHeight());
            setMediaPlayerDisplay(currentPlayer);
            if (!released) {
                currentPlayer.start();
            }
        } else {
            ImagePlayer imagePlayer = imagePlayersCache.get(String.valueOf(currentVideoIndex));
            Log.i(TAG, "onPlayCompleted:" + currentVideoIndex + ";" + (imagePlayer == null));
            if (imagePlayer != null) {
                beforeChangeVideo(imagePlayer.getImageWidth(), imagePlayer.getImageHeight());
                imagePlayer.start();
            } else {
                try {
                    Toast.makeText(getActivity(), "视频播放完毕..", Toast.LENGTH_SHORT)
                            .show();
                } catch (Exception e) {
                }
            }
        }
    }

    protected void beforeChangeVideo(int width, int height) {
    }

    protected final int MSG_UI_VIDEOSIZE_CHANGED = 0x01;

    @Override
    protected void handleUIMessage(Message message) {
        super.handleUIMessage(message);
        switch (message.what) {
            case MSG_UI_VIDEOSIZE_CHANGED:
                int width = message.arg1;
                int height = message.arg2;
                if (mVideoWidth <= 0 || mVideoHeight <= 0) {
                    mVideoWidth = width;
                    mVideoHeight = height;
                    mRenderer.onInputSizeChanged(width, height);
                    mSurfaceView.onInputSizeChanged(width, height);
                } else {
                    ((TransitionRenderer) (mRenderer)).simpleChangeVideoSize(width, height);
                }
                break;
        }
    }

    public void beforeFinish() {
        mRenderer.release();
        mSurfaceView.requestRender();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mSurfaceView != null) {
            mSurfaceView.surfaceDestroyed(mSurfaceView.getHolder());
            mSurfaceView = null;
        }
        releasePlayers();
        mRenderer = null;
        releaseImagePlayers();
    }

    protected void releaseImagePlayers() {
        if (imagePlayersCache != null) {
            Iterator<Map.Entry<String, ImagePlayer>> iterator = imagePlayersCache.entrySet().iterator();
            while (iterator.hasNext()) {
                Map.Entry<String, ImagePlayer> entry = iterator.next();
                ImagePlayer player = entry.getValue();
                try {
                    if (player != null)
                        player.onDestroy();
                } catch (Exception e) {
                }
            }
            imagePlayersCache.clear();
        }
    }

    protected void releasePlayers() {
        if (playersCache != null) {
            Iterator<Map.Entry<String, MediaPlayer>> iterator = playersCache.entrySet().iterator();
            while (iterator.hasNext()) {
                Map.Entry<String, MediaPlayer> entry = iterator.next();
                MediaPlayer player = entry.getValue();
                try {
                    if (player != null)
                        player.release();
                } catch (Exception e) {
                }
            }
            playersCache.clear();
        }
        currentVideoIndex = 0;
        mHasDisplaySetted = false;
        firstPlayer = null;
        nextMediaPlayer = null;
    }
}
