package com.wilbert.library.basic.aftereffect;

public interface IAeListener {
    /**
     * 点击返回按钮
     */
    void onBack();

    /**
     * 点击确认按钮
     */
    void onConfirm();

    /**
     * 点击选择音乐
     */
    void onSelectMusic();

    /**
     * 点击显示后期动作
     */
    void onAction();

    /**
     * 点击显示后期速度
     */
    void onSpeed();

    /**
     * 点击显示后期滤镜
     */
    void onFilter();

    /**
     * 点击显示后期贴纸
     */
    void onSticker();

    /**
     * 点击显示后期字幕
     */
    void onSubtitle();
}
