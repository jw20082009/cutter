package com.wilbert.library.basic.activity;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.View;
import android.widget.Button;

import com.wilbert.library.R;
import com.wilbert.library.basic.player.MultiVideoPlayFragment;

/**
 * Created by Android Studio.
 * User: wilbert jw20082009@qq.com
 * Date: 2019/8/15 14:47
 */
public class PlayersFragment extends MultiVideoPlayFragment implements View.OnClickListener {

    Button button0, button1, button2, button3;

    @Override
    protected int getLayoutId() {
        return R.layout.sample_fragment_player;
    }

    @Override
    protected int getSurfaceViewId() {
        return R.id.sample_gl_surfaceview;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        button0 = mLayoutView.findViewById(R.id.sample_btn_0);
        button1 = mLayoutView.findViewById(R.id.sample_btn_1);
        button2 = mLayoutView.findViewById(R.id.sample_btn_2);
        button3 = mLayoutView.findViewById(R.id.sample_btn_3);
        button0.setOnClickListener(this);
        button1.setOnClickListener(this);
        button2.setOnClickListener(this);
        button3.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        if (id == R.id.sample_btn_0) {
            playVideo(0);
        } else if (id == R.id.sample_btn_1) {
            playVideo(1);
        } else if (id == R.id.sample_btn_2) {
            playVideo(2);
        } else if (id == R.id.sample_btn_3) {
            playVideo(3);
        }
    }
}
