
package com.wilbert.library.basic.entity;

/**
 * 速度枚举，封装了5种被支持的可变速度
 */
public enum Speed {
    SPEED_NORMAL(1.0f), SPEED_SLOW2(0.5f), SPEED_SLOW(0.75f), SPEED_FAST(2f), SPEED_FAST2(4f);

    public float speed;

    Speed(float speed) {
        this.speed = speed;
    }

    public static Speed parseFromIndex(int i) {
        switch (i) {
            case 0:
                return SPEED_NORMAL;
            case 1:
                return SPEED_SLOW2;
            case 2:
                return SPEED_SLOW;
            case 3:
                return SPEED_FAST;
            case 4:
                return SPEED_FAST2;
            default:
                return SPEED_NORMAL;
        }
    }
}
