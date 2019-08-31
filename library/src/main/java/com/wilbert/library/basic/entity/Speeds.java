package com.wilbert.library.basic.entity;

import android.graphics.Color;
import android.text.TextUtils;

public enum Speeds {
    None("无", 0, Color.TRANSPARENT), Slow("慢动作", 0, Color.parseColor("#66990000")),
    Repeat("反复", 0, Color.parseColor("#66ff0000")), Reverse("倒放", 0, Color.parseColor("#66cc0000"));

    public String name;
    public int thumbRes;
    public int color;

    Speeds(String name, int thumb, int color) {
        this.name = name;
        this.thumbRes = thumb;
        this.color = color;
    }

    public static Speeds parseSpeeds(String name) {
        if (TextUtils.equals(None.name, name)) {
            return None;
        } else if (TextUtils.equals(Slow.name, name)) {
            return Slow;
        } else if (TextUtils.equals(Repeat.name, name)) {
            return Repeat;
        } else if (TextUtils.equals(Reverse.name, name)) {
            return Reverse;
        }
        return None;
    }
}
