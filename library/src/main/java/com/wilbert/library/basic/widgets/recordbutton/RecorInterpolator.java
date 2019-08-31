package com.wilbert.library.basic.widgets.recordbutton;

import android.graphics.PointF;
import android.view.animation.Interpolator;

/**
 * 2019/2/18
 * from 陈秋阳
 * 功能描述：先放大再缩小一点
 * 三阶贝塞尔曲线, P0 = (0,0),P1 = (0.18,1),P2 = (0.15,0.95),P3 = (1,1)
 */
public class RecorInterpolator implements Interpolator {
    PointF p1;
    PointF p2;
    CubicBezier cubcBezier;
    public RecorInterpolator(float x1,float y1,float x2,float y2) {
        this.p1 = new PointF(x1,y1);
        this.p2 = new PointF(x2,y2);
        cubcBezier = new CubicBezier(p1,p2);
    }

    @Override
    public float getInterpolation(float input) {
        return cubcBezier.getY(input);
    }
}
