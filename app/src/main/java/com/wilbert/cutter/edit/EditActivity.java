package com.wilbert.cutter.edit;

import android.content.Context;
import android.content.Intent;
import android.graphics.SurfaceTexture;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Surface;
import android.widget.TextView;

import com.wilbert.cutter.R;
import com.wilbert.library.clips.VideoClip;
import com.wilbert.library.contexts.VideoContext;
import com.wilbert.library.frameprocessor.gles.GLImageFilter;
import com.wilbert.library.frameprocessor.gles.OpenGLUtils;

import java.io.IOException;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;
import javax.microedition.khronos.opengles.GL10Ext;

public class EditActivity extends AppCompatActivity {
    final String TAG = "EditActivity";
    GLSurfaceView mSurfaceView;
    TextView mTipsView;
    VideoClip mClip;
    VideoContext mContext;
    int textureId = OpenGLUtils.GL_NOT_TEXTURE;
    Surface mSurface;
    SurfaceTexture mSurfaceTexture;
    GLImageFilter mImageFilter;

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
        mSurfaceView.setEGLContextClientVersion(2);
        mSurfaceView.setRenderer(mRenderer);
        mSurfaceView.setRenderMode(GLSurfaceView.RENDERMODE_WHEN_DIRTY);
        mTipsView = findViewById(R.id.tips);
//        mContext = new VideoContext(mSurfaceView);
        mClip = new VideoClip();
        try {
            mClip.setDataSource(getIntent().getStringExtra("uri"));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        mSurfaceView.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        mSurfaceView.onPause();
    }

    GLSurfaceView.Renderer mRenderer = new GLSurfaceView.Renderer() {
        @Override
        public void onSurfaceCreated(GL10 gl, EGLConfig config) {
            Log.i(TAG, "onSurfaceCreated");
            initTextureId();
//            GLES20.glClearColor(0.0f, 1.0f, 0f, 1.0f);
//            GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT);
        }

        @Override
        public void onSurfaceChanged(GL10 gl, int width, int height) {
            Log.i(TAG, "onSurfaceChanged:" + width + "*" + height);
        }

        @Override
        public void onDrawFrame(GL10 gl) {
            mSurfaceTexture.updateTexImage();
            Log.i(TAG, "onDrawFrame");
//            GLES20.glClearColor(1.0f, 0f, 0f, 1.0f);
//            GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT);
        }
    };

    private void initTextureId() {
        if (textureId == OpenGLUtils.GL_NOT_TEXTURE) {
//            mImageFilter = new GLImageFilter(EditActivity.this);
//            mImageFilter.
            textureId = OpenGLUtils.createOESTexture();
            mSurfaceTexture = new SurfaceTexture(textureId);
            mSurfaceTexture.setOnFrameAvailableListener(availableListener);
            mSurface = new Surface(mSurfaceTexture);
            mClip.prepare(mSurface);
        }
    }

    SurfaceTexture.OnFrameAvailableListener availableListener = new SurfaceTexture.OnFrameAvailableListener() {
        @Override
        public void onFrameAvailable(SurfaceTexture surfaceTexture) {
            Log.i(TAG, "onFrameAvailable");
            mSurfaceView.requestRender();
        }
    };
}
