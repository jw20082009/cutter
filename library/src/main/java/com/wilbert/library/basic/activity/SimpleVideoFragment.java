package com.wilbert.library.basic.activity;

import android.os.Bundle;
import android.text.TextUtils;

import com.wilbert.library.basic.aftereffect.BaseVideoFragment;
import com.wilbert.library.basic.widgets.AspectGLSurfaceView;
import com.wilbert.library.R;

public class SimpleVideoFragment extends BaseVideoFragment {

    public static Bundle createArgs(String filePath) {
        Bundle bundle = new Bundle();
        bundle.putString("uri", filePath);
        return bundle;
    }

    @Override
    protected AspectGLSurfaceView getSurfaceView() {
        if (mSurfaceView == null)
            mSurfaceView = mLayoutView.findViewById(R.id.gl_surfaceview);
        return mSurfaceView;
    }

    @Override
    protected int getLayoutId() {
        return R.layout.sample_fragment_videosimple;
    }

    @Override
    protected String getFilePath() {
        if (TextUtils.isEmpty(mPlayingVideoPath) && getArguments() != null) {
            mPlayingVideoPath = getArguments().getString("uri");
        }
        return mPlayingVideoPath;
    }
}
