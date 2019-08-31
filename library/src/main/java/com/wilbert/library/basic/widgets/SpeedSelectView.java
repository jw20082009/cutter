
package com.wilbert.library.basic.widgets;

import android.content.Context;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;

import com.wilbert.library.R;
import com.wilbert.library.basic.entity.Speed;

public class SpeedSelectView extends LinearLayout implements View.OnClickListener {

    View verySlow, slow, normal, fast, veryfast;

    ISpeedListener speedListener;

    Speed currentSpeed = Speed.SPEED_NORMAL;

    public SpeedSelectView(Context context) {
        super(context);
        initView();
    }

    public SpeedSelectView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        initView();
    }

    public SpeedSelectView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initView();
    }

    public SpeedSelectView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        initView();
    }

    private void initView() {
        LayoutInflater.from(getContext()).inflate(R.layout.sample_layout_speed_select, this, true);
        setOrientation(HORIZONTAL);
        setBackgroundResource(R.drawable.sample_speed_bg);
        setGravity(Gravity.CENTER);
        verySlow = findViewById(R.id.sample_tv_veryslow);
        slow = findViewById(R.id.sample_tv_slow);
        normal = findViewById(R.id.sample_tv_normal);
        normal.setSelected(true);
        fast = findViewById(R.id.sample_tv_fast);
        veryfast = findViewById(R.id.sample_tv_veryfast);
        verySlow.setOnClickListener(this);
        slow.setOnClickListener(this);
        normal.setOnClickListener(this);
        fast.setOnClickListener(this);
        veryfast.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        verySlow.setSelected(id == R.id.sample_tv_veryslow);
        slow.setSelected(id == R.id.sample_tv_slow);
        normal.setSelected(id == R.id.sample_tv_normal);
        fast.setSelected(id == R.id.sample_tv_fast);
        veryfast.setSelected(id == R.id.sample_tv_veryfast);
        if (id == R.id.sample_tv_veryslow) {
            currentSpeed = Speed.SPEED_SLOW2;
        } else if (id == R.id.sample_tv_slow) {
            currentSpeed = Speed.SPEED_SLOW;
        } else if (id == R.id.sample_tv_fast) {
            currentSpeed = Speed.SPEED_FAST;
        } else if (id == R.id.sample_tv_veryfast) {
            currentSpeed = Speed.SPEED_FAST2;
        } else {
            currentSpeed = Speed.SPEED_NORMAL;
        }
        notifySpeedSelect();
    }

    public Speed getCurrentSpeed() {
        return currentSpeed;
    }

    public void setSpeedListener(ISpeedListener listener) {
        this.speedListener = listener;
    }

    private void notifySpeedSelect() {
        if (this.speedListener != null) {
            this.speedListener.onSpeedSelect(currentSpeed);
        }
    }

    public interface ISpeedListener {

        void onSpeedSelect(Speed speed);
    }
}
