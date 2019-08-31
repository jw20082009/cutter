package com.wilbert.cutter.merge;

import android.graphics.Bitmap;

/**
 * Created by Android Studio.
 * User: wilbert jw20082009@qq.com
 * Date: 2019/8/15 11:09
 */
public class TransitionVideoEntity {
    private int thumbRes;
    private Bitmap thumb;
    private String filepath;

    public int getThumbRes() {
        return thumbRes;
    }

    public void setThumbRes(int thumbRes) {
        this.thumbRes = thumbRes;
    }

    public Bitmap getThumb() {
        return thumb;
    }

    public void setThumb(Bitmap thumb) {
        this.thumb = thumb;
    }

    public String getFilepath() {
        return filepath;
    }

    public void setFilepath(String filepath) {
        this.filepath = filepath;
    }
}
