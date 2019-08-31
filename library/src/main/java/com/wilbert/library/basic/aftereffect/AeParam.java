
package com.wilbert.library.basic.aftereffect;

import com.wilbert.library.basic.entity.StickerEntity;

import java.util.List;

/**
 * 后期特效的输入参数列表
 */
public class AeParam {

    private static final AeParam mInstance = new AeParam();

    /**
     * 输入视频文件
     */
    public String inputfile;

    /**
     * 主要用于原视频已经存在bgm时避免重叠
     * true:更换bgm会与视频原音混合,可设置比例
     * false:更换bgm后原视频音轨占比为0，不可调整原音音量
     */
    public boolean canMixMusic = true;

    /**
     * 是否在进入时强制预处理视频为全关键帧
     */
    public boolean forcePreVideo = true;

    /**
     * 人脸检测
     */
    public IFaceDetector faceDetector;

    /**
     * 水印列表，可以通过时间参数控制组合水印的显示，默认显示位置为右下角
     */
    public List<StickerEntity> waterMarkers;

    /**
     * 设置是否显示水印，true：会在后期操作过程中显示水印且会在生成文件中包含水印，false:后期操作过程中不显示但是生成文件中会包含水印
     */
    public boolean showWaterMarkers;

    private AeParam() {
        reset();
    }

    private void reset() {
    }

    public static AeParam getInstance() {
        return mInstance;
    }
}
