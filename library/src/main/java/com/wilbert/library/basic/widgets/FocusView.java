
package com.wilbert.library.basic.widgets;

import android.animation.TypeEvaluator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.animation.LinearInterpolator;

import com.wilbert.library.basic.utils.DensityUtils;

public class FocusView extends View implements IFocusView {

    private final String TAG = "FocusView";

    AnimEntity animEntity;

    ValueAnimator animator;

    private Paint mOuterPaint, mInnerPaint;

    private final float OUT_ALPHA_START = 0.3F;

    private float focusX, focusY;

    private boolean mCanFocus = true;

    public FocusView(Context context) {
        super(context);
    }

    public FocusView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);
    }

    private void init(Context context, AttributeSet attrs) {
        mOuterPaint = new Paint();
        mOuterPaint.setAntiAlias(true);
        mOuterPaint.setStyle(Paint.Style.STROKE);
        mOuterPaint.setStrokeWidth(DensityUtils.dp2px(getContext(), 1.5f));
        mOuterPaint.setAlpha(0);
        mOuterPaint.setColor(Color.WHITE);

        mInnerPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mInnerPaint.setStyle(Paint.Style.STROKE);
        mInnerPaint.setStrokeWidth(DensityUtils.dp2px(getContext(), 1.5f));
        mInnerPaint.setColor(Color.WHITE);
        mInnerPaint.setAlpha(0);
        mCanFocus = true;
        setLayerType(View.LAYER_TYPE_SOFTWARE, null);
    }

    @Override
    public void startFocus(float x, float y) {
        this.focusX = x;
        this.focusY = y;
        removeCallbacks(focusRunnable);
        if (getLocalVisibleRect(new Rect()) && mCanFocus) {
            postDelayed(focusRunnable, 100);
        }
    }

    @Override
    public void cancelFocus() {
        removeCallbacks(focusRunnable);
        if (animator != null) {
            animator.cancel();
            if (animEntity != null) {
                animEntity.outerAlpha = 0;
                animEntity.innerAlpha = 0;
                invalidate();
                animator = null;
            }
        }
    }

    Runnable focusRunnable = new Runnable() {
        @Override
        public void run() {
            if (animator != null) {
                animator.cancel();
                if (animEntity != null) {
                    animEntity.outerAlpha = 0;
                    animEntity.innerAlpha = 0;
                    invalidate();
                }
            }
            startAnimator(focusX, focusY);
        }
    };

    private void startAnimator(final float x, final float y) {
        AnimEntity startEntity = new AnimEntity();
        startEntity.outerAlpha = OUT_ALPHA_START;
        startEntity.outerRadius = DensityUtils.dp2px(getContext(), 30);
        startEntity.innerAlpha = 1.0f;
        startEntity.innerRadius = DensityUtils.dp2px(getContext(), 7);
        startEntity.timeline = 0;

        AnimEntity endEntity = new AnimEntity();
        endEntity.outerAlpha = 0f;
        endEntity.outerRadius = DensityUtils.dp2px(getContext(), 20);
        endEntity.innerAlpha = 0f;
        endEntity.innerRadius = DensityUtils.dp2px(getContext(), 7);
        endEntity.timeline = 2800;

        animator = ValueAnimator.ofObject(new AnimatEvaluator(), startEntity, endEntity);
        animator.setDuration(1500);
        animator.setInterpolator(new LinearInterpolator());
        animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                animEntity = (AnimEntity) animation.getAnimatedValue();
                Log.i(TAG, "onAnimationUpdate:" + animEntity.toString());
                animEntity.x = x;
                animEntity.y = y;
                invalidate();
            }
        });
        animator.start();
    }

    // drawRect
    @Override
    protected void onDraw(Canvas canvas) {
        if (animEntity != null) {
            if (mOuterPaint.getAlpha() != animEntity.outerAlpha)
                mOuterPaint.setAlpha((int) (255 * animEntity.outerAlpha));
            if (mInnerPaint.getAlpha() != animEntity.innerAlpha)
                mInnerPaint.setAlpha((int) (255 * animEntity.innerAlpha));
            canvas.drawCircle(animEntity.x, animEntity.y, animEntity.outerRadius, mOuterPaint);
            canvas.drawCircle(animEntity.x, animEntity.y, animEntity.innerRadius, mInnerPaint);
        }
    }

    class AnimEntity {
        public float outerRadius;

        public float outerAlpha;

        public float innerRadius;

        public float innerAlpha;

        public int timeline;

        public float x, y;

        @Override
        public String toString() {
            return "outerRadius:" + outerRadius + ";outerAlpha:" + outerAlpha + ";innerRadius:"
                    + innerRadius + ";innerAlpha:" + innerAlpha + ";timeline" + timeline;
        }
    }

    class AnimatEvaluator implements TypeEvaluator<AnimEntity> {

        @Override
        public AnimEntity evaluate(float fraction, AnimEntity startValue, AnimEntity endValue) {
            AnimEntity animEntity = new AnimEntity();
            int timelinefraction = (int) (startValue.timeline
                    + fraction * (endValue.timeline - startValue.timeline));
            float outerRadius = 6.0f * fraction * (endValue.outerRadius - startValue.outerRadius)
                    + startValue.outerRadius;
            if (fraction < 1.0f / 6) {
                animEntity.outerRadius = outerRadius;
            } else {
                animEntity.outerRadius = endValue.outerRadius;
            }
            animEntity.timeline = timelinefraction;
            animEntity.outerAlpha = 1.0f;
            animEntity.innerAlpha = (float) (0.5f
                    * (1.0f + Math.cos(timelinefraction * Math.PI / 400)));// y = 1/2 * (cos PI *
            // x/800 + 1)
            if (fraction > 6.0f / 7) {
                if (animEntity.innerAlpha <= animEntity.outerAlpha) {
                    animEntity.outerAlpha = animEntity.innerAlpha;
                } else {
                    animEntity.outerAlpha = startValue.outerAlpha;
                }
            } else {
                animEntity.outerAlpha = startValue.outerAlpha;
            }
            animEntity.innerRadius = startValue.innerRadius
                    + fraction * (endValue.innerRadius - startValue.innerRadius);
            return animEntity;
        }
    }

    public void onDestroy() {
        removeCallbacks(focusRunnable);
    }

    public void disableFocus() {
        mCanFocus = false;
    }
}
