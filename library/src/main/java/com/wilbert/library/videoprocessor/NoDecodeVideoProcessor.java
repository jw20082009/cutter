package com.wilbert.library.videoprocessor;

import android.media.MediaCodec;
import android.media.MediaExtractor;
import android.media.MediaFormat;
import android.media.MediaMetadataRetriever;
import android.media.MediaMuxer;
import android.support.annotation.Nullable;
import android.util.Log;
import com.wilbert.library.videoprocessor.util.CL;
import com.wilbert.library.videoprocessor.util.VideoProgressListener;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.List;

/**
 * Created by Android Studio.
 * User: wilbert jw20082009@qq.com
 * Date: 2019/8/9 10:34
 */
public class NoDecodeVideoProcessor {

    private static final String TAG = "NoDecodeVideoProcessor";

    /**
     * 直接对视频部分帧变速处理,用于所有帧都是关键帧的情况（丢弃音频）
     */
    public static void repeatVideoNoDecode(String input, String output, long startTimeUs, long durationTimeUs, float speed, @Nullable VideoProgressListener listener)
            throws IOException {
        long start = System.currentTimeMillis();
        MediaExtractor extractor = new MediaExtractor();
        extractor.setDataSource(input);
        int videoTrackIndex = VideoUtil.selectTrack(extractor, false);
        MediaMuxer mediaMuxer = new MediaMuxer(output, MediaMuxer.OutputFormat.MUXER_OUTPUT_MPEG_4);
        extractor.selectTrack(videoTrackIndex);
        MediaFormat videoTrackFormat = extractor.getTrackFormat(videoTrackIndex);
        long videoDurationUs = (long) (videoTrackFormat.getLong(MediaFormat.KEY_DURATION) + (durationTimeUs * 2));
        int videoMuxerTrackIndex = mediaMuxer.addTrack(videoTrackFormat);
        mediaMuxer.start();
        int maxBufferSize = videoTrackFormat.getInteger(MediaFormat.KEY_MAX_INPUT_SIZE);
        ByteBuffer buffer = ByteBuffer.allocateDirect(maxBufferSize);
        long lastFrameTimeUs = 0, lastSeekTimeUs = 0;
        int repeatTime = 0;
        MediaCodec.BufferInfo info = new MediaCodec.BufferInfo();
        try {
            // 写视频帧
            while (true) {
                long sampleTime = extractor.getSampleTime();
                long frameTimeUs = sampleTime;
                if (sampleTime < startTimeUs) {
                    if (repeatTime > 0) {
                        long deltaSeekTime = (long) ((sampleTime - lastSeekTimeUs));
                        frameTimeUs = lastFrameTimeUs + (deltaSeekTime < 0 ? 1 : deltaSeekTime);
                    } else {
                        frameTimeUs = sampleTime;
                    }
                } else if (sampleTime >= startTimeUs && sampleTime <= (startTimeUs + durationTimeUs)) {
                    long deltaSeekTime = (long) ((sampleTime - lastSeekTimeUs));
                    frameTimeUs = lastFrameTimeUs + (deltaSeekTime < 0 ? 1 : deltaSeekTime);
                } else {
                    long deltaSeekTime = (long) ((sampleTime - lastSeekTimeUs));
                    frameTimeUs = lastFrameTimeUs + (deltaSeekTime < 0 ? 1 : deltaSeekTime);
                }
                Log.i(TAG, "presentationTimeUs:" + frameTimeUs);
                lastFrameTimeUs = frameTimeUs;
                info.presentationTimeUs = frameTimeUs;
                info.size = extractor.readSampleData(buffer, 0);
                info.flags = extractor.getSampleFlags();
                if (info.size < 0) {
                    break;
                }
                mediaMuxer.writeSampleData(videoMuxerTrackIndex, buffer, info);
                if (listener != null) {
                    float videoProgress = info.presentationTimeUs / (float) videoDurationUs;
                    videoProgress = videoProgress > 1 ? 1 : videoProgress;
                    videoProgress *= 0.7f;
                    listener.onProgress(videoProgress);
                }
                lastSeekTimeUs = sampleTime;
                if (repeatTime < 2 && sampleTime >= startTimeUs + durationTimeUs) {
                    extractor.seekTo(startTimeUs, MediaExtractor.SEEK_TO_NEXT_SYNC);
                    repeatTime++;
                } else {
                    extractor.advance();
                }
            }
            if (listener != null) {
                listener.onProgress(1f);
            }
        } catch (Exception e) {
            CL.e(e);
        } finally {
            extractor.release();
            mediaMuxer.release();
        }
        Log.i(TAG, "reverseVideoNoDecode " + (System.currentTimeMillis() - start));
    }

    public static void reverseVideoNoDecode(String input, String output, boolean reverseAudio)
            throws IOException {
        reverseVideoNoDecode(input, output, reverseAudio, null, null);
    }

    /**
     * 直接对视频进行逆序,用于所有帧都是关键帧的情况
     */
    public static void reverseVideoNoDecode(String input, String output, boolean reverseAudio,
                                            List<Long> videoFrameTimeStamps, @Nullable VideoProgressListener listener)
            throws IOException {
        long start = System.currentTimeMillis();
        MediaMetadataRetriever retriever = new MediaMetadataRetriever();
        retriever.setDataSource(input);
        int durationMs = Integer
                .parseInt(retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION));
        retriever.release();
        MediaExtractor extractor = new MediaExtractor();
        extractor.setDataSource(input);
        int videoTrackIndex = VideoUtil.selectTrack(extractor, false);
        int audioTrackIndex = -1; //VideoUtil.selectTrack(extractor, true);
        boolean audioExist = audioTrackIndex >= 0;

        final int MIN_FRAME_INTERVAL = 10 * 1000;
        MediaMuxer mediaMuxer = new MediaMuxer(output, MediaMuxer.OutputFormat.MUXER_OUTPUT_MPEG_4);
        extractor.selectTrack(videoTrackIndex);
        MediaFormat videoTrackFormat = extractor.getTrackFormat(videoTrackIndex);
        long videoDurationUs = videoTrackFormat.getLong(MediaFormat.KEY_DURATION);
        long audioDurationUs = 0;
        int videoMuxerTrackIndex = mediaMuxer.addTrack(videoTrackFormat);
        int audioMuxerTrackIndex = 0;
        if (audioExist) {
            MediaFormat audioTrackFormat = extractor.getTrackFormat(audioTrackIndex);
            audioMuxerTrackIndex = mediaMuxer.addTrack(audioTrackFormat);
            audioDurationUs = audioTrackFormat.getLong(MediaFormat.KEY_DURATION);
        }
        mediaMuxer.start();
        int maxBufferSize = videoTrackFormat.getInteger(MediaFormat.KEY_MAX_INPUT_SIZE);
        ByteBuffer buffer = ByteBuffer.allocateDirect(maxBufferSize);
        VideoUtil.seekToLastFrame(extractor, videoTrackIndex, durationMs);
        long lastFrameTimeUs = -1;
        MediaCodec.BufferInfo info = new MediaCodec.BufferInfo();
        try {
            // 写视频帧
            if (videoFrameTimeStamps != null && videoFrameTimeStamps.size() > 0) {
                for (int i = videoFrameTimeStamps.size() - 1; i >= 0; i--) {
                    extractor.seekTo(videoFrameTimeStamps.get(i),
                            MediaExtractor.SEEK_TO_CLOSEST_SYNC);
                    long sampleTime = extractor.getSampleTime();
                    if (lastFrameTimeUs == -1) {
                        lastFrameTimeUs = sampleTime;
                    }
                    info.presentationTimeUs = lastFrameTimeUs - sampleTime;
                    info.size = extractor.readSampleData(buffer, 0);
                    info.flags = extractor.getSampleFlags();

                    if (info.size < 0) {
                        break;
                    }
                    mediaMuxer.writeSampleData(videoMuxerTrackIndex, buffer, info);
                    if (listener != null) {
                        float videoProgress = info.presentationTimeUs / (float) videoDurationUs;
                        videoProgress = videoProgress > 1 ? 1 : videoProgress;
                        videoProgress *= 0.7f;
                        listener.onProgress(videoProgress);
                    }
                }
            } else {
                while (true) {
                    long sampleTime = extractor.getSampleTime();
                    if (lastFrameTimeUs == -1) {
                        lastFrameTimeUs = sampleTime;
                    }
                    info.presentationTimeUs = lastFrameTimeUs - sampleTime;
                    info.size = extractor.readSampleData(buffer, 0);
                    info.flags = extractor.getSampleFlags();

                    if (info.size < 0) {
                        break;
                    }
                    mediaMuxer.writeSampleData(videoMuxerTrackIndex, buffer, info);
                    if (listener != null) {
                        float videoProgress = info.presentationTimeUs / (float) videoDurationUs;
                        videoProgress = videoProgress > 1 ? 1 : videoProgress;
                        videoProgress *= 0.7f;
                        listener.onProgress(videoProgress);
                    }
                    long seekTime = sampleTime - MIN_FRAME_INTERVAL;
                    if (seekTime <= 0) {
                        break;
                    }
                    extractor.seekTo(seekTime, MediaExtractor.SEEK_TO_PREVIOUS_SYNC);
                }
            }
            // 写音频帧
            if (audioExist) {
                extractor.unselectTrack(videoTrackIndex);
                extractor.selectTrack(audioTrackIndex);
                if (reverseAudio) {
                    List<Long> audioFrameStamps = VideoUtil.getFrameTimeStampsList(extractor);
                    lastFrameTimeUs = -1;
                    for (int i = audioFrameStamps.size() - 1; i >= 0; i--) {
                        extractor.seekTo(audioFrameStamps.get(i),
                                MediaExtractor.SEEK_TO_CLOSEST_SYNC);
                        long sampleTime = extractor.getSampleTime();
                        if (lastFrameTimeUs == -1) {
                            lastFrameTimeUs = sampleTime;
                        }
                        info.presentationTimeUs = lastFrameTimeUs - sampleTime;
                        info.size = extractor.readSampleData(buffer, 0);
                        info.flags = extractor.getSampleFlags();
                        if (info.size < 0) {
                            break;
                        }
                        mediaMuxer.writeSampleData(audioMuxerTrackIndex, buffer, info);
                        if (listener != null) {
                            float audioProgress = info.presentationTimeUs / (float) audioDurationUs;
                            audioProgress = audioProgress > 1 ? 1 : audioProgress;
                            audioProgress = 0.7f + audioProgress * 0.3f;
                            listener.onProgress(audioProgress);
                        }
                    }
                } else {
                    extractor.seekTo(0, MediaExtractor.SEEK_TO_CLOSEST_SYNC);
                    while (true) {
                        long sampleTime = extractor.getSampleTime();
                        if (sampleTime == -1) {
                            break;
                        }
                        info.presentationTimeUs = sampleTime;
                        info.size = extractor.readSampleData(buffer, 0);
                        info.flags = extractor.getSampleFlags();
                        if (info.size < 0) {
                            break;
                        }
                        mediaMuxer.writeSampleData(audioMuxerTrackIndex, buffer, info);
                        if (listener != null) {
                            float audioProgress = info.presentationTimeUs / (float) audioDurationUs;
                            audioProgress = audioProgress > 1 ? 1 : audioProgress;
                            audioProgress = 0.7f + audioProgress * 0.3f;
                            listener.onProgress(audioProgress);
                        }
                        extractor.advance();
                    }
                }
            }
            if (listener != null) {
                listener.onProgress(1f);
            }
        } catch (Exception e) {
            CL.e(e);
        } finally {
            extractor.release();
            mediaMuxer.release();
        }
        Log.i(TAG, "reverseVideoNoDecode " + (System.currentTimeMillis() - start));
    }

    /**
     * 直接对视频部分帧变速处理,用于所有帧都是关键帧的情况（丢弃音频）
     */
    public static void speedVideoNoDecode(String input, String output, long startTimeUs, long durationTimeUs, float speed, @Nullable VideoProgressListener listener)
            throws IOException {
        long start = System.currentTimeMillis();
        MediaExtractor extractor = new MediaExtractor();
        extractor.setDataSource(input);
        int videoTrackIndex = VideoUtil.selectTrack(extractor, false);
        int audioTrackIndex = VideoUtil.selectTrack(extractor, true);
        boolean audioExist = audioTrackIndex >= 0;

        MediaMuxer mediaMuxer = new MediaMuxer(output, MediaMuxer.OutputFormat.MUXER_OUTPUT_MPEG_4);
        extractor.selectTrack(videoTrackIndex);
        MediaFormat videoTrackFormat = extractor.getTrackFormat(videoTrackIndex);
        long videoDurationUs = (long) (videoTrackFormat.getLong(MediaFormat.KEY_DURATION) + (durationTimeUs - durationTimeUs / speed));
        int videoMuxerTrackIndex = mediaMuxer.addTrack(videoTrackFormat);
        mediaMuxer.start();
        int maxBufferSize = videoTrackFormat.getInteger(MediaFormat.KEY_MAX_INPUT_SIZE);
        ByteBuffer buffer = ByteBuffer.allocateDirect(maxBufferSize);
        long lastFrameTimeUs = 0, lastSeekTimeUs = 0;
        MediaCodec.BufferInfo info = new MediaCodec.BufferInfo();
        try {
            // 写视频帧
            while (true) {
                long sampleTime = extractor.getSampleTime();
                long frameTimeUs = sampleTime;
                if (sampleTime < startTimeUs) {
                    frameTimeUs = sampleTime;
                } else if (sampleTime >= startTimeUs && sampleTime <= (startTimeUs + durationTimeUs)) {
                    frameTimeUs = lastFrameTimeUs + (long) ((sampleTime - lastSeekTimeUs) / speed);
                } else {
                    frameTimeUs = lastFrameTimeUs + (sampleTime - lastSeekTimeUs);
                }
                Log.i(TAG, "presentationTimeUs:" + frameTimeUs);
                lastFrameTimeUs = frameTimeUs;
                info.presentationTimeUs = frameTimeUs;
                info.size = extractor.readSampleData(buffer, 0);
                info.flags = extractor.getSampleFlags();
                if (info.size < 0) {
                    break;
                }
                mediaMuxer.writeSampleData(videoMuxerTrackIndex, buffer, info);
                if (listener != null) {
                    float videoProgress = info.presentationTimeUs / (float) videoDurationUs;
                    videoProgress = videoProgress > 1 ? 1 : videoProgress;
                    videoProgress *= 0.7f;
                    listener.onProgress(videoProgress);
                }
                lastSeekTimeUs = sampleTime;
                extractor.advance();
            }

            if (listener != null) {
                listener.onProgress(1f);
            }
        } catch (Exception e) {
            CL.e(e);
        } finally {
            extractor.release();
            mediaMuxer.release();
        }
        Log.i(TAG, "reverseVideoNoDecode " + (System.currentTimeMillis() - start));
    }

    /**
     * 将全关键帧视频一分为二
     *
     * @param input    输入视频文件
     * @param output1  输出的第一段视频
     * @param output2  输出的第二段视频
     * @param point    视频切割点的归一化值（0~1.0）
     * @param listener
     * @throws Exception
     */
    public static void cutWithoutDecode(String input, String output1, String output2, float point, VideoProgressListener listener) throws IOException {
        MediaExtractor extractor = new MediaExtractor();
        extractor.setDataSource(input);
        int videoTrackIndex = VideoUtil.selectTrack(extractor, false);
        int audioTrackIndex = VideoUtil.selectTrack(extractor, true);
        boolean audioExist = audioTrackIndex >= 0;
        final int MIN_FRAME_INTERVAL = 10 * 1000;
        MediaMuxer mediaMuxer1 = new MediaMuxer(output1, MediaMuxer.OutputFormat.MUXER_OUTPUT_MPEG_4);
        MediaMuxer mediaMuxer2 = new MediaMuxer(output2, MediaMuxer.OutputFormat.MUXER_OUTPUT_MPEG_4);
        extractor.selectTrack(videoTrackIndex);
        MediaFormat videoTrackFormat = extractor.getTrackFormat(videoTrackIndex);
        int audioMuxerTrackIndex1 = 0;
        int audioMuxerTrackIndex2 = 0;
        int videoMuxerTrackIndex1 = 0;
        int videoMuxerTrackIndex2 = 0;
        long videoDurationUs = 0;
        long audioDurationUs = 0;
        videoDurationUs = videoTrackFormat.getLong(MediaFormat.KEY_DURATION);
        long cutPoint = (long) (point * videoDurationUs);
        if (audioExist) {
            MediaFormat audioTrackFormat = extractor.getTrackFormat(audioTrackIndex);
            audioDurationUs = audioTrackFormat.getLong(MediaFormat.KEY_DURATION);
            cutPoint = (long) (point * audioDurationUs);
            audioTrackFormat.setLong(MediaFormat.KEY_DURATION, cutPoint);
            audioMuxerTrackIndex1 = mediaMuxer1.addTrack(audioTrackFormat);
            audioTrackFormat.setLong(MediaFormat.KEY_DURATION, (audioDurationUs - cutPoint));
            audioMuxerTrackIndex2 = mediaMuxer2.addTrack(audioTrackFormat);
        }
        videoTrackFormat.setLong(MediaFormat.KEY_DURATION, cutPoint);
        videoMuxerTrackIndex1 = mediaMuxer1.addTrack(videoTrackFormat);
        videoTrackFormat.setLong(MediaFormat.KEY_DURATION, (videoDurationUs - cutPoint));
        videoMuxerTrackIndex2 = mediaMuxer2.addTrack(videoTrackFormat);

        int maxBufferSize = videoTrackFormat.getInteger(MediaFormat.KEY_MAX_INPUT_SIZE);
        ByteBuffer buffer = ByteBuffer.allocateDirect(maxBufferSize);

        MediaCodec.BufferInfo info = new MediaCodec.BufferInfo();
        try {
            MediaMuxer currentMuxer = mediaMuxer1;
            mediaMuxer1.start();
            long lastFrameTimeUs = 0;
            int currentTrackIndex = videoMuxerTrackIndex1;
            // 写视频帧
            while (true) {
                long sampleTime = extractor.getSampleTime();
                if (lastFrameTimeUs == 0 && sampleTime > cutPoint) {
                    currentMuxer = mediaMuxer2;
                    currentTrackIndex = videoMuxerTrackIndex2;
                    currentMuxer.start();
                    lastFrameTimeUs = sampleTime;
                }
                info.presentationTimeUs = sampleTime - lastFrameTimeUs;
                info.size = extractor.readSampleData(buffer, 0);
                info.flags = extractor.getSampleFlags();
                if (info.size < 0) {
                    break;
                }
                currentMuxer.writeSampleData(currentTrackIndex, buffer, info);
                if (listener != null) {
                    float videoProgress = sampleTime / (float) videoDurationUs;
                    videoProgress = videoProgress > 1 ? 1 : videoProgress;
                    videoProgress *= 0.7f;
                    listener.onProgress(videoProgress);
                }
                extractor.advance();
            }
            // 写音频帧
            if (audioExist) {
                extractor.unselectTrack(videoTrackIndex);
                extractor.selectTrack(audioTrackIndex);
                extractor.seekTo(0, MediaExtractor.SEEK_TO_CLOSEST_SYNC);
                currentMuxer = mediaMuxer1;
                lastFrameTimeUs = 0;
                currentTrackIndex = audioMuxerTrackIndex1;
                while (true) {
                    long sampleTime = extractor.getSampleTime();
                    if (sampleTime == -1) {
                        break;
                    }
                    if (lastFrameTimeUs == 0 && sampleTime > cutPoint) {
                        currentMuxer = mediaMuxer2;
                        currentTrackIndex = audioMuxerTrackIndex2;
                        lastFrameTimeUs = sampleTime;
                    }
                    info.presentationTimeUs = sampleTime - lastFrameTimeUs;
                    info.size = extractor.readSampleData(buffer, 0);
                    info.flags = extractor.getSampleFlags();
                    if (info.size < 0) {
                        break;
                    }
                    currentMuxer.writeSampleData(currentTrackIndex, buffer, info);
                    if (listener != null) {
                        float audioProgress = sampleTime / (float) audioDurationUs;
                        audioProgress = audioProgress > 1 ? 1 : audioProgress;
                        audioProgress = 0.7f + audioProgress * 0.3f;
                        listener.onProgress(audioProgress);
                    }
                    extractor.advance();
                }
            }
            if (listener != null) {
                listener.onProgress(1f);
            }
        } catch (Exception e) {
            CL.e(e);
        } finally {
            extractor.release();
            mediaMuxer1.release();
        }
    }
}
