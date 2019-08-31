package com.wilbert.library.videoprocessor;

import android.media.MediaCodecInfo;
import android.media.MediaExtractor;
import android.media.MediaFormat;
import android.media.MediaMetadataRetriever;
import android.media.MediaMuxer;

import com.wilbert.library.videoprocessor.effect.VideoDecodeWithEffectThread;
import com.wilbert.library.videoprocessor.util.AudioUtil;
import com.wilbert.library.videoprocessor.util.CL;
import com.wilbert.library.videoprocessor.util.VideoProgressAve;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicBoolean;

import static com.wilbert.library.videoprocessor.VideoProcessor.DEFAULT_FRAME_RATE;
import static com.wilbert.library.videoprocessor.VideoProcessor.DEFAULT_I_FRAME_INTERVAL;

/**
 * Created by Android Studio.
 * User: wilbert jw20082009@qq.com
 * Date: 2019/8/7 11:21
 */
public class HardVideoProcessor {

    VideoParams mParams;

    public HardVideoProcessor(VideoParams params) {
        this.mParams = params;
        try {
            init();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void init() throws Exception {
        MediaMetadataRetriever retriever = new MediaMetadataRetriever();
        retriever.setDataSource(mParams.getInput());
        int originWidth = Integer.parseInt(
                retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_WIDTH));
        int originHeight = Integer.parseInt(
                retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_HEIGHT));
        int rotationValue = Integer.parseInt(
                retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_ROTATION));
        int oriBitrate = Integer
                .parseInt(retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_BITRATE));
        int durationMs = Integer
                .parseInt(retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION));
        retriever.release();
        if (mParams.getBitrate() == null) {
            mParams.setBitrate(oriBitrate);
        }
        if (mParams.getiFrameInterval() == null) {
            mParams.setiFrameInterval(DEFAULT_I_FRAME_INTERVAL);
        }
        int resultWidth = mParams.getOutWidth() == null ? originWidth : mParams.getOutWidth();
        int resultHeight = mParams.getOutHeight() == null ? originHeight : mParams.getOutHeight();
        resultWidth = resultWidth % 2 == 0 ? resultWidth : resultWidth + 1;
        resultHeight = resultHeight % 2 == 0 ? resultHeight : resultHeight + 1;
        if (rotationValue == 90 || rotationValue == 270) {
            int temp = resultHeight;
            resultHeight = resultWidth;
            resultWidth = temp;

            temp = originHeight;
            originHeight = originWidth;
            originWidth = temp;
        }
        MediaExtractor extractor = new MediaExtractor();
        extractor.setDataSource(mParams.getInput());
        int videoIndex = VideoUtil.selectTrack(extractor, false);
        int audioIndex = VideoUtil.selectTrack(extractor, true);
        MediaMuxer mediaMuxer = new MediaMuxer(mParams.getOutput(),
                MediaMuxer.OutputFormat.MUXER_OUTPUT_MPEG_4);
        int muxerAudioTrackIndex = 0;
        boolean shouldChangeAudioSpeed = mParams.getChangeAudioSpeed() == null ? true
                : mParams.getChangeAudioSpeed();
        Integer audioEndTimeMs = mParams.getEndTimeMs();
        if (audioIndex >= 0) {
            MediaFormat audioTrackFormat = extractor.getTrackFormat(audioIndex);
            String audioMimeType = MediaFormat.MIMETYPE_AUDIO_AAC;
            int bitrate = AudioUtil.getAudioBitrate(audioTrackFormat);
            int channelCount = audioTrackFormat.getInteger(MediaFormat.KEY_CHANNEL_COUNT);
            int sampleRate = audioTrackFormat.getInteger(MediaFormat.KEY_SAMPLE_RATE);
            int maxBufferSize = AudioUtil.getAudioMaxBufferSize(audioTrackFormat);
            MediaFormat audioEncodeFormat = MediaFormat.createAudioFormat(audioMimeType, sampleRate,
                    channelCount);// 参数对应-> mime type、采样率、声道数
            audioEncodeFormat.setInteger(MediaFormat.KEY_BIT_RATE, bitrate);// 比特率
            audioEncodeFormat.setInteger(MediaFormat.KEY_AAC_PROFILE,
                    MediaCodecInfo.CodecProfileLevel.AACObjectLC);
            audioEncodeFormat.setInteger(MediaFormat.KEY_MAX_INPUT_SIZE, maxBufferSize);

            if (shouldChangeAudioSpeed) {
                if (mParams.getStartTimeMs() != null || mParams.getEndTimeMs() != null
                        || mParams.getSpeed() != null) {
                    long durationUs = audioTrackFormat.getLong(MediaFormat.KEY_DURATION);
                    if (mParams.getStartTimeMs() != null && mParams.getEndTimeMs() != null) {
                        durationUs = (mParams.getEndTimeMs() - mParams.getStartTimeMs()) * 1000;
                    }
                    if (mParams.getSpeed() != null) {
                        durationUs /= mParams.getSpeed();
                    }
                    audioEncodeFormat.setLong(MediaFormat.KEY_DURATION, durationUs);
                }
            } else {
                long videoDurationUs = durationMs * 1000;
                long audioDurationUs = audioTrackFormat.getLong(MediaFormat.KEY_DURATION);
                if (mParams.getStartTimeMs() != null || mParams.getEndTimeMs() != null
                        || mParams.getSpeed() != null) {
                    if (mParams.getStartTimeMs() != null && mParams.getEndTimeMs() != null) {
                        videoDurationUs = (mParams.getEndTimeMs() - mParams.getStartTimeMs()) * 1000;
                    }
                    if (mParams.getSpeed() != null) {
                        videoDurationUs /= mParams.getSpeed();
                    }
                    long avDurationUs = videoDurationUs < audioDurationUs ? videoDurationUs
                            : audioDurationUs;
                    audioEncodeFormat.setLong(MediaFormat.KEY_DURATION, avDurationUs);
                    audioEndTimeMs = (mParams.getStartTimeMs() == null ? 0 : mParams.getStartTimeMs())
                            + (int) (avDurationUs / 1000);
                }
            }

            AudioUtil.checkCsd(audioEncodeFormat, MediaCodecInfo.CodecProfileLevel.AACObjectLC,
                    sampleRate, channelCount);
            // 提前推断出音頻格式加到MeidaMuxer，不然实际上应该到音频预处理完才能addTrack，会卡住视频编码的进度
            muxerAudioTrackIndex = mediaMuxer.addTrack(audioEncodeFormat);
        }
        extractor.selectTrack(videoIndex);
        if (mParams.getStartTimeMs() != null) {
            extractor.seekTo(mParams.getStartTimeMs() * 1000, MediaExtractor.SEEK_TO_PREVIOUS_SYNC);
        } else {
            extractor.seekTo(0, MediaExtractor.SEEK_TO_PREVIOUS_SYNC);
        }

        VideoProgressAve progressAve = new VideoProgressAve(mParams.getListener());
        progressAve.setSpeed(mParams.getSpeed());
        progressAve.setStartTimeMs(mParams.getStartTimeMs() == null ? 0 : mParams.getStartTimeMs());
        progressAve.setEndTimeMs(mParams.getEndTimeMs() == null ? durationMs : mParams.getEndTimeMs());
        AtomicBoolean decodeDone = new AtomicBoolean(false);
        CountDownLatch muxerStartLatch = new CountDownLatch(1);
        VideoEncodeThread encodeThread = new VideoEncodeThread(extractor, mediaMuxer,
                mParams.getBitrate(), resultWidth, resultHeight, mParams.getiFrameInterval(),
                mParams.getFrameRate() == null ? DEFAULT_FRAME_RATE : mParams.getFrameRate(), videoIndex,
                decodeDone, muxerStartLatch);
        int srcFrameRate = VideoUtil.getFrameRate(mParams.getInput());
        if (srcFrameRate <= 0) {
            srcFrameRate = (int) Math.ceil(VideoUtil.getAveFrameRate(mParams.getInput()));
        }
        VideoDecodeThread decodeThread = null;
        if (mParams.getAeEntity() != null) {
            decodeThread = new VideoDecodeWithEffectThread(mParams.getContext(), originWidth, originHeight, resultWidth, resultHeight, encodeThread, extractor,
                    mParams.getStartTimeMs(), mParams.getEndTimeMs(), srcFrameRate,
                    mParams.getFrameRate() == null ? DEFAULT_FRAME_RATE : mParams.getFrameRate(),
                    mParams.getSpeed(), mParams.isDropFrames(), videoIndex, decodeDone);
        } else {
            decodeThread = new VideoDecodeThread(encodeThread, extractor, mParams.getStartTimeMs(), mParams.getEndTimeMs(), srcFrameRate, mParams.getFrameRate() == null ? DEFAULT_FRAME_RATE : mParams.getFrameRate(),
                    mParams.getSpeed(), mParams.isDropFrames(), videoIndex, decodeDone);
        }
        AudioProcessThread audioProcessThread = new AudioProcessThread(mParams.getContext(), mParams.getInput(),
                mediaMuxer, mParams.getStartTimeMs(), audioEndTimeMs,
                shouldChangeAudioSpeed ? mParams.getSpeed() : null, muxerAudioTrackIndex,
                muxerStartLatch);
        encodeThread.setProgressAve(progressAve);
        audioProcessThread.setProgressAve(progressAve);
        decodeThread.start();
        encodeThread.start();
        audioProcessThread.start();
        try {
            long s = System.currentTimeMillis();
            decodeThread.join();
            encodeThread.join();
            long e1 = System.currentTimeMillis();
            audioProcessThread.join();
            long e2 = System.currentTimeMillis();
            CL.w(String.format("编解码:%dms,音频:%dms", e1 - s, e2 - s));
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        try {
            mediaMuxer.release();
            extractor.release();
        } catch (Exception e2) {
            CL.e(e2);
        }
        if (encodeThread.getException() != null) {
            throw encodeThread.getException();
        } else if (decodeThread.getException() != null) {
            throw decodeThread.getException();
        } else if (audioProcessThread.getException() != null) {
            throw audioProcessThread.getException();
        }
    }
}
