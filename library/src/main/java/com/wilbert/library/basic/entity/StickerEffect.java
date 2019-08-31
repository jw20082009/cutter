package com.wilbert.library.basic.entity;


import com.wilbert.library.R;

public enum StickerEffect {
    None(R.drawable.framep_black_oval, 0, "AR/empty", "none"),
    commonSticker(R.drawable.framep_black_oval, 1, "AR/effect1_1", "commonSticker"),
    commonSticker2(R.drawable.framep_black_oval, 2, "AR/effect1_2", "commonSticker2"),
    commonSticker3(R.drawable.framep_black_oval, 3, "AR/effect1_3", "commonSticker3"),
    commonSticker4(R.drawable.framep_black_oval, 4, "AR/effect1_4", "commonSticker4"),
    commonSticker5(R.drawable.framep_black_oval, 5, "AR/effect1_5", "commonSticker5"),
    face3d(R.drawable.framep_black_oval, 6, "AR/effect2", "face3d"),
    faceMesh(R.drawable.framep_black_oval, 7, "AR/effect3", "faceMesh"),
    face3d2(R.drawable.framep_black_oval, 7, "AR/effect4", "face3d2"),
    face3d3(R.drawable.framep_black_oval, 7, "AR/effect5", "face3d3");

    int thumbRes;
    int index;
    String path;
    String name;

    public String getResPath() {
        return path;
    }

    public int thumbRes() {
        return thumbRes;
    }

    public final int index() {
        return index;
    }

    public String stickerName() {
        return name;
    }
    StickerEffect(int res, int index, String path, String name) {
        this.thumbRes = res;
        this.index = index;
        this.path = path;
        this.name = name;
    }
}
