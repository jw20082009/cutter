package com.wilbert.cutter.edit;

import android.content.Context;
import android.content.Intent;
import android.opengl.GLSurfaceView;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.TextView;

import com.wilbert.cutter.R;
import com.wilbert.library.clips.VideoClip;
import com.wilbert.library.contexts.VideoContext;

public class EditActivity extends AppCompatActivity {
    final String TAG = "EditActivity";
    GLSurfaceView mSurfaceView;
    TextView mTipsView;
    VideoClip mClip;
    VideoContext mContext;


    public static Intent createIntent(Context context, String filepath, int simpleCompress) {
        Intent intent = new Intent(context, EditActivity.class);
        intent.putExtra("uri", filepath);
        intent.putExtra("simplecompress", simpleCompress);
        return intent;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit);
        mSurfaceView = (GLSurfaceView) findViewById(R.id.gl_surfaceview);
        mTipsView = findViewById(R.id.tips);
        mClip = new VideoClip();
        mClip.prepare(getIntent().getStringExtra("uri"));
        mContext = new VideoContext(mSurfaceView, mClip);

    }

    @Override
    protected void onResume() {
        super.onResume();
        mSurfaceView.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        mContext.release();
        mSurfaceView.onPause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mContext.release();
    }
}
