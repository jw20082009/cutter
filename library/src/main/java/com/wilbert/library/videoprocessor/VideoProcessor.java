
package com.wilbert.library.videoprocessor;

import android.annotation.TargetApi;
import android.content.Context;
import android.support.annotation.IntRange;
import android.support.annotation.Nullable;
import android.text.TextUtils;

import com.wilbert.library.basic.entity.AeEntity;
import com.wilbert.library.basic.utils.OutFileGenerator;
import com.wilbert.library.videoprocessor.util.VideoProgressListener;

import java.io.IOException;
import java.util.List;

/**
 * Created by huangwei on 2018/2/2.
 */
@TargetApi(21)
public class VideoProcessor {
    final static String TAG = "VideoProcessor";

    final static String OUTPUT_MIME_TYPE = "video/avc";

    public static int DEFAULT_FRAME_RATE = 25;

    /**
     * 只有关键帧距为0的才能方便做逆序
     */
    public final static int DEFAULT_I_FRAME_INTERVAL = 1;

    public final static int DEFAULT_AAC_BITRATE = 192 * 1000;

    /**
     * 控制音频合成时，如果输入的音频文件长度不够，是否重复填充
     */
    public static boolean AUDIO_MIX_REPEAT = true;

    final static int TIMEOUT_USEC = 2500;

    public static void scaleVideo(Context context, String input, String output, int outWidth,
                                  int outHeight) throws Exception {
        processor(context).input(input).output(output).outWidth(outWidth).outHeight(outHeight)
                .process();
    }

    public static void cutVideo(Context context, String input, String output, int startTimeMs,
                                int endTimeMs) throws Exception {
        processor(context).input(input).output(output).startTimeMs(startTimeMs).endTimeMs(endTimeMs).process();
    }

    public static void cutVideo(Context context, String input, String output, int startTimeMs,
                                int endTimeMs, VideoProgressListener listener) throws Exception {
        processor(context).input(input).output(output).startTimeMs(startTimeMs).endTimeMs(endTimeMs).progressListener(listener).process();
    }

    /**
     * 将全关键帧视频一分为二
     *
     * @param context
     * @param input    输入视频文件
     * @param output1  输出的第一段视频
     * @param output2  输出的第二段视频
     * @param cutPoint 视频切割点的归一化值（0~1.0）
     * @param listener
     * @throws Exception
     */
    public static void splitVideoWithoutDecode(Context context, String input, String output1, String output2, float cutPoint, VideoProgressListener listener) throws Exception {
        NoDecodeVideoProcessor.cutWithoutDecode(input, output1, output2, cutPoint, listener);
    }

    /**
     * 直接对视频部分帧变速处理,用于所有帧都是关键帧的情况（丢弃音频）
     */
    public static void repeatVideoNoDecode(String input, String output, long startTimeUs, long durationTimeUs, float speed, @Nullable VideoProgressListener listener)
            throws IOException {
        NoDecodeVideoProcessor.repeatVideoNoDecode(input, output, startTimeUs, durationTimeUs, speed, listener);
    }

    public static void reverseVideoNoDecode(String input, String output, boolean reverseAudio)
            throws IOException {
        NoDecodeVideoProcessor.reverseVideoNoDecode(input, output, reverseAudio);
    }

    /**
     * 直接对视频进行逆序,用于所有帧都是关键帧的情况
     */
    public static void reverseVideoNoDecode(String input, String output, boolean reverseAudio,
                                            List<Long> videoFrameTimeStamps, @Nullable VideoProgressListener listener)
            throws IOException {
        NoDecodeVideoProcessor.reverseVideoNoDecode(input, output, reverseAudio, videoFrameTimeStamps, listener);
    }

    /**
     * 直接对视频部分帧变速处理,用于所有帧都是关键帧的情况（丢弃音频）
     */
    public static void speedVideoNoDecode(String input, String output, long startTimeUs, long durationTimeUs, float speed, @Nullable VideoProgressListener listener)
            throws IOException {
        NoDecodeVideoProcessor.speedVideoNoDecode(input, output, startTimeUs, durationTimeUs, speed, listener);
    }

    public static void changeVideoSpeed(Context context, String input, String output, float speed)
            throws Exception {
        processor(context).input(input).output(output).speed(speed).process();
    }

    public static boolean checkAllIFrameVideo(String input) throws IOException {
        return HardProcessor.checkAllIFrameVideo(input);
    }

    public static String preProcessVideo(Context context, String input, String output,
                                         VideoProgressListener listener) throws Exception {
        return HardProcessor.preProcessVideo(context, input, output, listener);
    }

    /**
     * 对视频先检查，如果不是全关键帧，先处理成所有帧都是关键帧，再逆序
     */
    public static void reverseVideo(Context context, String input, String output,
                                    boolean reverseAudio, @Nullable VideoProgressListener listener) throws Exception {
        HardProcessor.reverseVideo(context, input, output, reverseAudio, listener);
    }

    public static void processVideo(VideoParams params)
            throws Exception {
        if (params.getCodecType() == null || params.getCodecType() == 0) {
            processVideoHardCodec(params);
        } else {
            processVideoSoftCodec(params);
        }
    }

    public static void processVideoSoftCodec(VideoParams params)
            throws Exception {
        String outFile = params.getOutput();
        if (TextUtils.isEmpty(outFile)) {
            outFile = OutFileGenerator.generateAeFile(params.getContext(), params.getInput());
            params.setOutput(outFile);
        }
    }

    /**
     * 支持裁剪缩放快慢放
     */
    public static void processVideoHardCodec(VideoParams mParams) throws Exception {
        HardProcessor.processVideoHardCodec(mParams);
    }

    /**
     * 不需要改变音频速率的情况下，直接读写就可 只支持16bit音频
     *
     * @param videoVolume 0静音，100表示原音
     */
    public static void adjustVideoVolume(Context context, final String videoInput,
                                         final String output, @IntRange(from = 0, to = 100) int videoVolume, float faceInSec,
                                         float fadeOutSec) throws IOException {
        HardProcessor.adjustVideoVolume(context, videoInput, output, videoVolume, faceInSec, fadeOutSec);
    }

    public static void mixAudioTrack(Context context, final String videoInput,
                                     final String audioInput, final String output, Integer startTimeMs, Integer endTimeMs,
                                     @IntRange(from = 0, to = 100) int videoVolume,
                                     @IntRange(from = 0, to = 100) int aacVolume, float fadeInSec, float fadeOutSec)
            throws IOException {
        mixAudioTrack(context, videoInput, audioInput, output, startTimeMs, endTimeMs, 0,
                videoVolume, aacVolume, fadeInSec, fadeOutSec);
    }

    /**
     * 不需要改变音频速率的情况下，直接读写就可 只支持16bit音频
     *
     * @param videoVolume 0静音，100表示原音
     * @param aacVolume   0静音，100表示原音
     */
    public static void mixAudioTrack(Context context, final String videoInput,
                                     final String audioInput, final String output, Integer startTimeMs, Integer endTimeMs,
                                     final Integer audioStartTimeMs, @IntRange(from = 0, to = 100) int videoVolume,
                                     @IntRange(from = 0, to = 100) int aacVolume, float fadeInSec, float fadeOutSec)
            throws IOException {
        HardProcessor.mixAudioTrack(context, videoInput, audioInput, output, startTimeMs, endTimeMs, audioStartTimeMs, videoVolume, aacVolume, fadeInSec, fadeOutSec);
    }

    public static Processor processor(Context context) {
        return new Processor(context);
    }

    public static class Processor {
        VideoParams params;

        public Processor(Context context) {
            params = new VideoParams();
            params.setContext(context);
        }

        public Processor input(String input) {
            params.setInput(input);
            return this;
        }

        public Processor output(String output) {
            params.setOutput(output);
            return this;
        }

        public Processor outWidth(int outWidth) {
            params.setOutWidth(outWidth);
            return this;
        }

        public Processor outHeight(int outHeight) {
            params.setOutHeight(outHeight);
            return this;
        }

        public Processor startTimeMs(int startTimeMs) {
            params.setStartTimeMs(startTimeMs);
            return this;
        }

        public Processor endTimeMs(int endTimeMs) {
            params.setEndTimeMs(endTimeMs);
            return this;
        }

        public Processor speed(float speed) {
            params.setSpeed(speed);
            return this;
        }

        public Processor changeAudioSpeed(boolean changeAudioSpeed) {
            params.setChangeAudioSpeed(changeAudioSpeed);
            return this;
        }

        public Processor bitrate(int bitrate) {
            params.setBitrate(bitrate);
            return this;
        }

        public Processor frameRate(int frameRate) {
            params.setFrameRate(frameRate);
            return this;
        }

        public Processor iFrameInterval(int iFrameInterval) {
            params.setiFrameInterval(iFrameInterval);
            return this;
        }

        /**
         * 帧率超过指定帧率时是否丢帧,默认为true
         */
        public Processor dropFrames(boolean dropFrames) {
            params.setDropFrames(dropFrames);
            return this;
        }

        public Processor progressListener(VideoProgressListener listener) {
            params.setListener(listener);
            return this;
        }

        public Processor afterEffect(AeEntity aeEntity) {
            params.setAeEntity(aeEntity);
            return this;
        }

        public Processor codecType(int codecType) {
            params.setCodecType(codecType);
            return this;
        }

        public void process() throws Exception {
            processVideo(params);
        }
    }
}
