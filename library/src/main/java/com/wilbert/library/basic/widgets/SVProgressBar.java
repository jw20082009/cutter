
package com.wilbert.library.basic.widgets;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.widget.ProgressBar;

import com.wilbert.library.basic.utils.DensityUtils;
import com.wilbert.library.R;
import com.wilbert.library.basic.widgets.recordbutton.RecordButton;

import java.util.ArrayList;
import java.util.List;

public class SVProgressBar extends ProgressBar {

    private float minProgress = 1.0f * 2 * 1000 / RecordButton.MAX_PROGRESS;

    private boolean mIsDeleting = false;

    private List<Float> pointProgressList = new ArrayList<>();

    private Paint pointPaint, deletingPaint;

    public SVProgressBar(Context context) {
        super(context);
        initView();
    }

    public SVProgressBar(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        initView();
    }

    public SVProgressBar(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initView();
    }

    public SVProgressBar(Context context, @Nullable AttributeSet attrs, int defStyleAttr,
                         int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        initView();
    }

    @Override
    public synchronized void setMax(int max) {
        minProgress = 1.0f * 2 * 1000 / max;
        super.setMax(max);
    }

    /**
     * @param progress 默认的最小时间标识点，默认为(2000/总时长)
     */
    public void setMinProgress(float progress) {
        minProgress = progress;
    }

    private void initView() {
        setProgressDrawable(getContext().getDrawable(R.drawable.sample_record_progress));
        pointPaint = new Paint();
        pointPaint.setColor(Color.WHITE);
        pointPaint.setStrokeWidth(DensityUtils.dp2px(getContext(), 2));
    }

    private void initDeletingPaint(int height) {
        deletingPaint = new Paint();
        deletingPaint.setStrokeWidth(height);
        deletingPaint.setColor(getResources().getColor(R.color.basic_secondaryProgressBar));
    }

    public void setPointProgressList(List<Float> pointProgressList) {
        this.pointProgressList = pointProgressList;
        post(new Runnable() {
            @Override
            public void run() {
                invalidate();
            }
        });
    }

    public void clearDeleteStatus() {
        mIsDeleting = false;
        invalidate();
    }

    public boolean deleteLastPointProgress() {
        return deleteLastPointProgress(mIsDeleting);
    }

    public boolean deleteLastPointProgress(boolean isDeleting) {
        this.mIsDeleting = isDeleting;
        if ((this.pointProgressList != null && this.pointProgressList.size() > 0) || getProgress() > 0) {
            if (!mIsDeleting) {
                mIsDeleting = true;
                invalidate();
            } else {
                int size = pointProgressList.size();
                if (size > 0) {
                    float leftPoint = this.pointProgressList.remove(size - 1);
                    setProgress((int) (leftPoint * getMax()));
                } else {
                    setProgress(0);
                }
                mIsDeleting = false;
                invalidate();
                return true;
            }
        }
        return false;
    }

    @Override
    protected synchronized void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        int width = getMeasuredWidth();
        int height = getMeasuredHeight();
        int progress = getProgress();
        int max = getMax();
        if (progress < minProgress * max) {
            int currentLeft = (int) (width * minProgress);
            canvas.drawLine(currentLeft, 0, currentLeft, height, pointPaint);
        }
        if (pointProgressList != null && pointProgressList.size() > 0) {
            for (float f : pointProgressList) {
                int currentLeft = (int) (width * f);
                canvas.drawLine(currentLeft, 0, currentLeft, height, pointPaint);
            }
        }
        int currentLeft = (int) (width * (1.0f * progress / max));
        canvas.drawLine(currentLeft, 0, currentLeft, height, pointPaint);
        if (mIsDeleting) {
            int size = pointProgressList.size();
            if ((pointProgressList != null && size > 0) || progress > 0) {
                if (deletingPaint == null) {
                    initDeletingPaint(height);
                }
                int deleteLeft = 0;
                if (size > 0) {
                    deleteLeft = (int) (width * pointProgressList.get(size - 1));
                }
                canvas.drawLine(currentLeft, height / 2.0f, deleteLeft, height / 2.0f, deletingPaint);
            }
        }
    }

}
