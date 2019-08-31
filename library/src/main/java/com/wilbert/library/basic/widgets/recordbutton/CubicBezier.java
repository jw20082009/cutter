package com.wilbert.library.basic.widgets.recordbutton;

import android.graphics.PointF;

/**
 * 2019/2/18
 * from 陈秋阳
 * 功能描述：贝塞尔曲线三阶函数
 */
public class CubicBezier {
    private static final String TAG = "CubicBezier";

    PointF p1;
    PointF p2;
    private PointF[] coords;
    int precision = 1000;

    public CubicBezier(PointF p1, PointF p2) {
        this.p1 = p1;
        this.p2 = p2;
        this.coords = this.getCoordsArray(precision);
    }


    //获取阶点
    private PointF getCoord(float t) {
        // 如果t取值不在0到1之间，则终止操作
        if (t > 1 || t < 0) return null;
        float tLeft = 1 - t;
        float coefficient1 = (float) (3 * t * Math.pow(tLeft, 2));
        float coefficient2 = (float) (3 * tLeft * Math.pow(t, 2));
        float coefficient3 = (float) Math.pow(t, 3);
        float px = coefficient1 * p1.x + coefficient2 * p2.x + coefficient3;
        float py = coefficient1 * p1.y + coefficient2 * p2.y + coefficient3;
        // 结果只保留三位有效数字
        return new PointF(px, py);
    }

    private PointF[] getCoordsArray(int precision) {
        float step = 1/((float)precision + 1);
        PointF[] result = new PointF[precision];
        for (int t = 0; t < precision; t++) {
            result[t] = this.getCoord(t*step);
        }
        return result;
    }

    public float getY(float x) {
        if (x >= 1) return 1;
        if (x <= 0) return 0;
        int startX = 0;
        for (int i = 0; i < this.coords.length; i++) {
            if (this.coords[i].x >= x) {
                startX = i;
                break;
            }
        }
        if (startX == 0 ){
            if (x == 0){
                return 0;
            }
            return getY((float) (x-0.01));
        }
        PointF axis1 = this.coords[startX];
        PointF axis2 = this.coords[startX - 1];
        float k = (axis2.y - axis1.y) / (axis2.x - axis1.x);
        float b = axis1.y - k * axis1.x;
        return (k * x + b);
    }
}

