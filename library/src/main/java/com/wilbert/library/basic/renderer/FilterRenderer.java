package com.wilbert.library.basic.renderer;

import android.opengl.GLSurfaceView;
import android.util.Log;

import com.wilbert.library.frameprocessor.beautykit.BeautyRenderer;
import com.wilbert.library.frameprocessor.gles.OpenGLUtils;

import java.nio.FloatBuffer;

import javax.microedition.khronos.opengles.GL10;

public class FilterRenderer extends OesRenderer {

    protected boolean hasBeautifyChanged = false;
    protected Object beautifyLock = new Object();
    protected BeautyRenderer beautyRenderer;

    public FilterRenderer(GLSurfaceView context) {
        super(context);
        beautyRenderer = new BeautyRenderer();
        beautyRenderer.init(context.getContext(),null);
    }

    @Override
    public void onSurfaceChanged(GL10 gl, int width, int height) {
        super.onSurfaceChanged(gl, width, height);
        beautyRenderer.onResume();
    }

    @Override
    public void onInputSizeChanged(int width, int height) {
        super.onInputSizeChanged(width, height);
        beautyRenderer.onInputSizeChanged(width, height,0,0);
    }

    @Override
    protected int onDrawFrameBuffer(int textureId, FloatBuffer vertexBuffer, FloatBuffer textureBuffer) {
        textureId = super.onDrawFrameBuffer(textureId, vertexBuffer, textureBuffer);
        if (textureId != OpenGLUtils.GL_NOT_TEXTURE) {
            textureId = beautyRenderer.onDrawFrame(textureId, null);
        }
        return textureId;
    }

    @Override
    protected void _release() {
        Log.e("FilterRenderer","release");
        super._release();
        if (beautyRenderer != null) {
            beautyRenderer.onPause();
        }
    }
}
