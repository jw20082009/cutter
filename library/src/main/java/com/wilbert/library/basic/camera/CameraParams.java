
package com.wilbert.library.basic.camera;

import com.wilbert.library.basic.aftereffect.IFaceDetector;
import com.wilbert.library.basic.entity.StickerEntity;

import java.util.List;

/**
 * 用于预览的输入参数列表
 */
public class CameraParams {

    /**
     * 摄像头预览的期望宽度
     */
    public int expWidth = 720;

    /**
     * 摄像头预览的期望高度
     */
    public int expHeight = 1280;

    /**
     * 录制视频码率 (默认vbr模式,小于零代表自动设置码率))
     */
    public int bitrate = -1;

    /**
     * 录制视频帧率
     */
    public int frameRate = 30;

    /**
     * 最大录制长度，ms
     */
    public int maxduration = 20000;

    /**
     * 录制视频关键帧间隔，0代表全关键帧视频
     */
    public Integer iFrameInterval;

    /**
     * 人脸点检测
     */
    public IFaceDetector faceDetector;

    /**
     *
     */
    public List<StickerEntity> waterMarkers;

    /**
     * 编码类型：0：默认使用硬件编码（MediaCodec），1：使用软件编码（ffmpeg统一接口编码)
     */
    public int encodeType;

    public CameraParams() {
    }

    private static final CameraParams mInstance = new CameraParams();

    public static CameraParams getInstance() {
        return mInstance;
    }

    public static class Builder {

        public Builder size(int width, int height) {
            mInstance.expWidth = width;
            mInstance.expHeight = height;
            return this;
        }

        public Builder bitRate(int bitrate) {
            mInstance.bitrate = bitrate;
            return this;
        }

        public Builder frameRate(int frameRate) {
            mInstance.frameRate = frameRate;
            return this;
        }

        public Builder duration(int duration) {
            mInstance.maxduration = duration;
            return this;
        }

        public Builder FaceDetector(IFaceDetector faceDetector) {
            mInstance.faceDetector = faceDetector;
            return this;
        }

        public Builder iframeInterval(Integer interval) {
            mInstance.iFrameInterval = interval;
            return this;
        }

        public Builder waterMarker(List<StickerEntity> waterMarkers) {
            mInstance.waterMarkers = waterMarkers;
            return this;
        }

        public Builder encodeType(int type) {
            mInstance.encodeType = type;
            return this;
        }

        public CameraParams build() {
            return mInstance;
        }
    }
}
