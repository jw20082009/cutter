
package com.wilbert.library.basic.widgets;

import android.content.Context;
import android.content.res.TypedArray;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;

import com.wilbert.library.basic.utils.DensityUtils;
import com.wilbert.library.R;

public class AspectView extends LinearLayout {

    View blank1, blank2, rect;

    float aspect;

    public AspectView(Context context) {
        this(context, null);
    }

    public AspectView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public AspectView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        this(context, attrs, defStyleAttr, 0);
    }

    public AspectView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        TypedArray ta = getContext().obtainStyledAttributes(attrs, R.styleable.sample_aspect);
        aspect = ta.getFloat(R.styleable.sample_aspect_ratio, 1.0f * 16 / 9);
        ta.recycle();
        initview();
    }

    private void initview() {
        LayoutInflater.from(getContext()).inflate(R.layout.sample_layout_aspect, this, true);
        blank1 = findViewById(R.id.sample_aspect_blank1);
        blank2 = findViewById(R.id.sample_aspect_blank2);
        rect = findViewById(R.id.sample_aspect_rect);
        setGravity(Gravity.CENTER);
        setOrientation(VERTICAL);
        setAspect(aspect);
    }

    public void setAspect(float aspect) {
        this.aspect = aspect;
        if (aspect > 1.5f)
            aspect = 1.5f;
        int width = DensityUtils.dp2px(getContext(), 15);
        int height = (int) (width * aspect);
        LayoutParams params = (LayoutParams) rect.getLayoutParams();
        params.width = width;
        params.height = height;
        rect.setLayoutParams(params);
    }

    public float getAspect(){
        return aspect;
    }
}
