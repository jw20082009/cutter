
package com.wilbert.library.basic.widgets.recordbutton;

import android.animation.Animator;
import android.animation.TypeEvaluator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;
import android.os.SystemClock;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.animation.AccelerateDecelerateInterpolator;

import com.wilbert.library.R;
import com.wilbert.library.basic.utils.DensityUtils;

public class RecordButton extends View {

    private final boolean pressEnable = false;//是否支持点击按钮开始和结束进度

    private final Boolean NEED_PROGRESS = false;

    private final String TAG = "ReButton";

    private final float ZOOM_SCALE = 1.0F / 2;

    public static long MAX_PROGRESS = 20 * 1000;

    private long maxprogress = MAX_PROGRESS;

    private final int START_COLOR = Color.parseColor("#ffffffff");// 描边初始

    private final int END_COLOR = Color.parseColor("#ffffffff");// 描边放大后

    private int BG_COLOR_NORMAL;// = Color.parseColor("#4cffffff");// 按钮背景初始

    private int BG_COLOR_BIG;// = Color.parseColor("#00000000");// 按钮背景放大后

    private final int STATUS_IDLE = 0X00;

    private final int STATUS_ZOOM_OUT = 0X01;

    private final int STATUS_LARGE_IDLE = 0x02;

    private final int STATUS_PROGRESSING = 0X03;

    private final int STATUS_ZOOM_IN = 0X04;

    private int mStatus = STATUS_IDLE, mLastStatus = STATUS_IDLE;

    private long mCurrentProgress = 0;// 实际的录像时间

    private float mAccelerateProgressAngle = 0;// 表现的录像进度

    private int smallSize, largeSize;

    private float border;

    private float borderBig;

    private Paint bgPaint, // 描边
            forePaint, progressPaint;

    private float cx, cy;

    private IRecordListener listener;

    private OnClickListener onClickListener;

    private ValueAnimator smallAnimator, largeAnimator, processAnimator;

    public static final long ONCLICK_TIME = 1000;

    AnimEntity startAnim, endAnim, currentAnim;

    boolean canClick = false, mNeedRecord = false;

    private long vibrate_Time = 15, mStartProgressTime = 0L;

    private int measureWidth, measureHeight;

    private int touchSlop;

    boolean concernEvent = false;

    float totalProgress;

    float mDownX, mDownY;

    long mDownTime;

    Drawable drawablePause;

    public RecordButton(Context context) {
        this(context, null);
    }

    public RecordButton(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public RecordButton(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        BG_COLOR_NORMAL = getContext().getResources().getColor(R.color.basic_color_confirm);
        BG_COLOR_BIG = BG_COLOR_NORMAL;
        smallSize = DensityUtils.dp2px(getContext(), 60);
        largeSize = getContext().getResources().getDimensionPixelSize(R.dimen.basic_height_recordbtn);
        border = DensityUtils.dp2px(getContext(), 4);
        borderBig = DensityUtils.dp2px(getContext(), 8);
        drawablePause = getContext().getResources().getDrawable(R.drawable.basic_pause);
        cx = 1.0f * largeSize / 2;
        cy = 1.0f * largeSize / 2;

        bgPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        bgPaint.setStrokeWidth(border);
        bgPaint.setColor(BG_COLOR_NORMAL);
        bgPaint.setStrokeJoin(Paint.Join.MITER);

        progressPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        progressPaint.setStyle(Paint.Style.STROKE);
        progressPaint.setColor(Color.parseColor("#FDB709"));
        progressPaint.setStrokeWidth(borderBig);
        progressPaint.setDither(true);
        progressPaint.setStrokeJoin(Paint.Join.MITER);

        forePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        forePaint.setStyle(Paint.Style.STROKE);
        forePaint.setStrokeWidth(border);
        forePaint.setColor(START_COLOR);
        forePaint.setStrokeJoin(Paint.Join.MITER);

        startAnim = new AnimEntity();
        startAnim.bgRadius = smallSize / 2;
        startAnim.foreRadius = (smallSize - border) / 2;
        startAnim.foreWidth = (int) border;
        startAnim.foreColor = START_COLOR;
        // startAnim.bgColor = BG_COLOR_NORMAL;

        endAnim = new AnimEntity();
        endAnim.bgRadius = largeSize / 2;
        endAnim.foreRadius = (largeSize - borderBig) / 2;
        endAnim.foreWidth = (int) (borderBig);
        endAnim.foreColor = END_COLOR;
        touchSlop = ViewConfiguration.get(getContext()).getScaledTouchSlop();

        initParams();
    }

    private void initParams() {
        mNeedRecord = false;
        mDownTime = 0;
        mDownX = mDownY = 0;
        mCurrentProgress = 0;
        mAccelerateProgressAngle = 0;
        currentAnim = startAnim;
        concernEvent = false;

        if (processAnimator == null) {
            processAnimator = ValueAnimator.ofFloat(0, 360).setDuration(maxprogress);
            processAnimator.setInterpolator(new RecorInterpolator(0f, 0.2f, 1f, 1f));
            processAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                @Override
                public void onAnimationUpdate(ValueAnimator animation) {
                    mAccelerateProgressAngle = (float) animation.getAnimatedValue();
                    invalidate();
                }
            });
        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        int measureWidth = getMeasuredWidth();
        int measureHeight = getMeasuredHeight();
        if (measureWidth != this.measureWidth || measureHeight != this.measureHeight) {
            this.measureWidth = measureWidth;
            this.measureHeight = measureHeight;
            cx = measureWidth / 2.0f;
            cy = measureHeight - 1.0f * largeSize / 2;
            notifyLocation();
            progressPaint.setColor(Color.parseColor("#FDB709"));
        }
    }

    public void setInitProgress(int progress) {
        MAX_PROGRESS = progress;
        maxprogress = progress;
    }

    public long getInitProgress() {
        return MAX_PROGRESS;
    }

    public void setMaxProgress(long progress) {
        this.maxprogress = progress;
    }

    public long getMaxProgress() {
        return maxprogress;
    }

    public void startRecord() {
        mNeedRecord = true;
        if (mStatus == STATUS_LARGE_IDLE) {
            startProgressing();
        }
    }

    public void cancelRecord() {
        mNeedRecord = false;
        if (largeAnimator != null) {
            largeAnimator.cancel();
        }
        showZoomIn();
    }

    private void startProgressing() {
        mStartProgressTime = SystemClock.elapsedRealtime();
        setStatus(STATUS_PROGRESSING);
        notifyStatus();
        progressing();
        if (processAnimator != null) {
            processAnimator.start();
        }
    }

    private void progressing() {
        if (mStartProgressTime > 0) {
            long elapsedTime = SystemClock.elapsedRealtime() - mStartProgressTime;
            mCurrentProgress = elapsedTime;
            if (mStatus == STATUS_PROGRESSING && elapsedTime < maxprogress) {
                if (NEED_PROGRESS) {
                    invalidate();
                }
                notifyProgress();
                postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        progressing();
                    }
                }, 15);
            } else {
                showZoomIn();
            }
        }
    }

    private boolean isTouchRecord(float downX, float downY) {
        float halfLarge = largeSize / 2;
        return downX > cx - halfLarge && downX < cx + halfLarge && downY > cy - halfLarge
                && downY < cy + halfLarge;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN: {
                if (mStatus == STATUS_PROGRESSING && !pressEnable) {
                    mDownX = event.getX();
                    mDownY = event.getY();
                    concernEvent = false;
                    if (isTouchRecord(mDownX, mDownY)) {
                        cancelRecord();
                    }
                } else {
                    mDownX = event.getX();
                    mDownY = event.getY();
                    mDownTime = SystemClock.elapsedRealtime();
                    totalProgress = measureHeight - (measureHeight - mDownY);
                    if (isTouchRecord(mDownX, mDownY)) {
                        concernEvent = true;
                        canClick = true;
                        showZoomOut();
                        getParent().requestDisallowInterceptTouchEvent(true);
                    }
                }
            }
            break;
            case MotionEvent.ACTION_MOVE: {
                if (concernEvent) {
                    if (listener != null) {
                        float moveY = event.getY();
                        final int offsetX = (int) (event.getX() - mDownX);
                        final int offsetY = (int) (event.getY() - mDownY);
                        if (moveY < mDownY && moveY >= (1 - ZOOM_SCALE) * totalProgress
                                && isVerticalMoved(offsetX, offsetY)) {
                            listener.onUpProgressChanged(
                                    1.0f * (mDownY - moveY) / (totalProgress * ZOOM_SCALE));
                        }
                    }
                }
                if ((event.getX() - mDownX) > touchSlop || (event.getY() - mDownY) > touchSlop) {
                    canClick = false;
                }
            }
            break;
            case MotionEvent.ACTION_CANCEL:
            case MotionEvent.ACTION_UP: {
                if (concernEvent) {
                    getParent().requestDisallowInterceptTouchEvent(false);
                    concernEvent = false;
                    long offsetTime = SystemClock.elapsedRealtime() - mDownTime;
                    if (offsetTime < ONCLICK_TIME && canClick) {
                        if (onClickListener != null) {
                            // vibrate();
                            onClickListener.onClick(this);
                        }
                    }
                    if (pressEnable)
                        cancelRecord();
                }
            }
            break;
        }
        return concernEvent || super.onTouchEvent(event);
    }

    private boolean isVerticalMoved(int offsetX, int offsetY) {
        return Math.abs(offsetX) < touchSlop * 4 && Math.abs(offsetY) > touchSlop;
    }

    private void showZoomIn() {
        if (mStatus != STATUS_ZOOM_IN && mStatus != STATUS_IDLE) {
            if (processAnimator != null) {
                processAnimator.cancel();
                mAccelerateProgressAngle = 0;
            }
            bgPaint.setStrokeWidth(border);
            bgPaint.setColor(BG_COLOR_NORMAL);
            setStatus(STATUS_ZOOM_IN);
            notifyStatus();
            smallAnimator = ValueAnimator.ofObject(new ZoomOutEvaluator(), currentAnim, startAnim);
            smallAnimator.setDuration((long) (ONCLICK_TIME / 4.0f));
            smallAnimator.setInterpolator(new AccelerateDecelerateInterpolator());
            smallAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                @Override
                public void onAnimationUpdate(ValueAnimator valueAnimator) {
                    currentAnim = (AnimEntity) valueAnimator.getAnimatedValue();
                    invalidate();
                }
            });
            smallAnimator.addListener(new Animator.AnimatorListener() {
                @Override
                public void onAnimationStart(Animator animator) {
                }

                @Override
                public void onAnimationEnd(Animator animator) {
                    initParams();
                    setStatus(STATUS_IDLE);
                    notifyStatus();
                    invalidate();
                }

                @Override
                public void onAnimationCancel(Animator animator) {
                    initParams();
                    setStatus(STATUS_IDLE);
                    notifyStatus();
                    invalidate();
                }

                @Override
                public void onAnimationRepeat(Animator animator) {
                }
            });
            smallAnimator.start();
        } else {
            Log.i(TAG, "showZoomIn failed with status:" + mStatus);
        }
    }

    private void showZoomOut() {
        if (mStatus == STATUS_IDLE) {
            bgPaint.setStrokeWidth(borderBig);
            bgPaint.setColor(BG_COLOR_BIG);

            setStatus(STATUS_ZOOM_OUT);
            notifyStatus();
            if (largeAnimator == null) {
                largeAnimator = ValueAnimator.ofObject(new ZoomOutEvaluator(), startAnim, endAnim);
                largeAnimator.setDuration(ONCLICK_TIME);
                largeAnimator.setInterpolator(new RecorInterpolator(0.21f, 1.54f, 0.37f, 1f));
                largeAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                    @Override
                    public void onAnimationUpdate(ValueAnimator valueAnimator) {
                        currentAnim = (AnimEntity) valueAnimator.getAnimatedValue();
                        invalidate();
                    }
                });
                largeAnimator.addListener(new Animator.AnimatorListener() {
                    @Override
                    public void onAnimationStart(Animator animator) {
                        setStatus(STATUS_LARGE_IDLE);
                        notifyStatus();
                        if (mNeedRecord) {
                            startProgressing();
                        }
                    }

                    @Override
                    public void onAnimationEnd(Animator animator) {
                    }

                    @Override
                    public void onAnimationCancel(Animator animator) {
                        setStatus(STATUS_LARGE_IDLE);
                        notifyStatus();
                    }

                    @Override
                    public void onAnimationRepeat(Animator animator) {
                    }
                });
            }
            largeAnimator.start();
        } else {
            Log.i(TAG, "showZoomOut failed with status:" + mStatus);
        }
    }

    public void getLocation(LocationListener locationListener) {
        this.locationListener = locationListener;
        if (cx != 0 && this.measureWidth != 0 && cx == this.measureWidth / 2.0f) {
            notifyLocation();
        }
    }

    public int getSmallSize() {
        return (int) (smallSize + 2 * border);
    }

    public int getLargeSize() {
        return (int) (largeSize + 2 * border);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        canvas.drawCircle(cx, cy, currentAnim.bgRadius, bgPaint);
        forePaint.setColor(currentAnim.foreColor);
        forePaint.setStrokeWidth(currentAnim.foreWidth);
        canvas.drawCircle(cx, cy, currentAnim.foreRadius, forePaint);
        progressPaint.setStrokeWidth(currentAnim.foreWidth);
        if (NEED_PROGRESS) {
            RectF rectF = new RectF(cx - currentAnim.foreRadius, cy - currentAnim.foreRadius,
                    cx + currentAnim.foreRadius, cy + currentAnim.foreRadius);
            canvas.drawArc(rectF, -90, mAccelerateProgressAngle, false, progressPaint);
        }
        if ((mStatus == STATUS_PROGRESSING || mStatus == STATUS_LARGE_IDLE)
                && drawablePause != null) {
            canvas.translate(cx - 64, cy - 64);
            drawablePause.setBounds(0, 0, 128, 128);
            drawablePause.draw(canvas);
        }
    }

    @Override
    public void setOnClickListener(@Nullable OnClickListener l) {
        this.onClickListener = l;
    }

    public void setOnRecordListener(IRecordListener listener) {
        this.listener = listener;
    }

    class AnimEntity {

        public float bgRadius = 70f, foreRadius = 70f;

        public int foreColor = Color.WHITE, foreWidth = 20;

        @Override
        public String toString() {
            return "bgRadius:" + bgRadius + ";foreRadius:" + foreRadius + ";foreColor:" + foreColor
                    + ";foreWidth:" + foreWidth;
        }
    }

    class ZoomOutEvaluator implements TypeEvaluator<AnimEntity> {

        @Override
        public AnimEntity evaluate(float v, AnimEntity animEntity, AnimEntity t1) {
            AnimEntity startEntity = animEntity;
            AnimEntity endEntity = t1;

            AnimEntity animEntity1 = new AnimEntity();
            animEntity1.bgRadius = startEntity.bgRadius
                    + v * (endEntity.bgRadius - startEntity.bgRadius);
            animEntity1.foreRadius = startEntity.foreRadius
                    + v * (endEntity.foreRadius - startEntity.foreRadius);
            animEntity1.foreWidth = startEntity.foreWidth
                    + (int) (v * (endEntity.foreWidth - startEntity.foreWidth));

            int startInt = (Integer) startEntity.foreColor;
            int startA = (startInt >> 24) & 0xff;
            int startR = (startInt >> 16) & 0xff;
            int startG = (startInt >> 8) & 0xff;
            int startB = startInt & 0xff;

            int endInt = (Integer) endEntity.foreColor;
            int endA = (endInt >> 24) & 0xff;
            int endR = (endInt >> 16) & 0xff;
            int endG = (endInt >> 8) & 0xff;
            int endB = endInt & 0xff;

            animEntity1.foreColor = (int) ((startA + (int) (v * (endA - startA))) << 24)
                    | (int) ((startR + (int) (v * (endR - startR))) << 16)
                    | (int) ((startG + (int) (v * (endG - startG))) << 8)
                    | (int) ((startB + (int) (v * (endB - startB))));
            return animEntity1;
        }
    }

    public interface LocationListener {
        void onLocationChanged(int[] location);
    }

    public LocationListener locationListener;

    public void clearLocationListner() {
        locationListener = null;
    }

    private void notifyLocation() {
        removeCallbacks(notifyLocation);
        postDelayed(notifyLocation, 50);
    }

    private void notifyProgress() {
        if (this.listener != null) {
            listener.onProgressChanged(mCurrentProgress);
        }
    }

    Runnable notifyLocation = new Runnable() {
        @Override
        public void run() {
            if (locationListener != null && measureWidth > 0) {
                locationListener.onLocationChanged(new int[]{
                        (int) (cx - largeSize / 2.0f - border),
                        (int) (cy - largeSize / 2.0f - border)
                });
                clearLocationListner();
            }
        }
    };

    private void setStatus(int status) {
        mLastStatus = mStatus;
        mStatus = status;
    }

    private void notifyStatus() {
        if (listener != null) {
            switch (mStatus) {
                case STATUS_ZOOM_OUT:
                    listener.onRecordPreStart();
                    break;
                case STATUS_PROGRESSING:
                    listener.onRecordStarted();
                    break;
                case STATUS_ZOOM_IN:
                    if (mLastStatus == STATUS_PROGRESSING) {
                        listener.onRecordPreEnd();
                    } else if (mLastStatus == STATUS_LARGE_IDLE) {
                        listener.onRecordStarted();
                        listener.onRecordPreEnd();
                    }
                    break;
                case STATUS_IDLE:
                    if (mLastStatus == STATUS_ZOOM_IN) {
                        listener.onRecordEnded();
                    }
                    break;
            }
        }
    }

    public long getCurrentProgress() {
        return mCurrentProgress;
    }

}
