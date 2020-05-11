package com.wilbert.library.contexts;

import android.opengl.GLES11Ext;
import android.opengl.GLES20;
import android.util.Log;

import com.wilbert.library.codecs.abs.FrameInfo;
import com.wilbert.library.frameprocessor.gles.OpenGLUtils;
import com.wilbert.library.frameprocessor.gles.TextureRotationUtils;

import java.nio.ByteBuffer;
import java.nio.FloatBuffer;

import javax.microedition.khronos.opengles.GL;

import static com.wilbert.library.frameprocessor.gles.OpenGLUtils.checkGlError;

/**
 * author : wilbert
 * e-mail : jw20082009@qq.com
 * time   : 2020/05/11
 * desc   :
 */
public class NV21Renderer {

    private static final String YUV_INPUT_VERTEX_SHADER = "" + "attribute vec4 position;\n"
            + "attribute vec4 inputTextureCoordinate;\n" + "\n"
            + "varying vec2 textureCoordinate;\n" + "\n" + "void main()\n" + "{\n"
            + "	textureCoordinate = inputTextureCoordinate.xy;\n" + "	gl_Position = position;\n"
            + "}";

    private static final String YUV_TEXTURE =
//            "#extension GL_OES_EGL_image_external : require"
//            +
            "precision mediump float;                           \n"
                    + "varying vec2 textureCoordinate;                           \n"
                    + "uniform sampler2D y_texture;                       \n"
                    + "uniform sampler2D uv_texture;                      \n"
                    + "void main (void){                                  \n"
                    + "   float y = texture2D(y_texture, textureCoordinate).r;        \n"

                    // We had put the Y values of each pixel to the R,G,B components by
                    // GL_LUMINANCE,
                    // that's why we're pulling it from the R component, we could also use G or B
                    + "   vec2 uv = texture2D(uv_texture, textureCoordinate).xw - 0.5;       \n"

                    // The numbers are just YUV to RGB conversion constants
                    + "   float r = y + 1.370705 * uv.x;\n"
                    + "   float g = y - 0.698001 * uv.x - 0.337633 * uv.y;\n"
                    + "   float b = y + 1.732446 * uv.y;\n      " +
                    // We finally set the RGB color of our pixel
                    "   gl_FragColor = vec4(r, g, b, 1.0);              \n"
                    + "}";

    private int mProgramIn;
    private int yuvPositionLoc = -1;
    private int yuvTextureLoc = -1;
    private int yTextureLoc = -1;
    private int uvTextureLoc = -1;
    private int[] mTextureY;
    private int[] mTextureUV;
    private int[] mFrameBuffers;
    private int[] mFrameBufferTextures;
    private int mCoordsPerVertex = TextureRotationUtils.CoordsPerVertex;
    private int mSurfaceWidth, mSurfaceHeight;
    private FloatBuffer mVertexBuffer;
    private FloatBuffer mTextureBuffer;
    private boolean mTextureInit = false;
    private final int TEXTURE_TYPE =
//            GLES11Ext.GL_TEXTURE_EXTERNAL_OES;
            GLES20.GL_TEXTURE_2D;

    public NV21Renderer() {

    }

    public void onSurfaceCreated() {
        mProgramIn = OpenGLUtils.createProgram(YUV_INPUT_VERTEX_SHADER, YUV_TEXTURE);
        yuvPositionLoc = GLES20.glGetAttribLocation(mProgramIn, "position");
        yuvTextureLoc = GLES20.glGetAttribLocation(mProgramIn, "inputTextureCoordinate");
        yTextureLoc = GLES20.glGetUniformLocation(mProgramIn, "y_texture");
        uvTextureLoc = GLES20.glGetUniformLocation(mProgramIn, "uv_texture");
        mVertexBuffer = OpenGLUtils.createFloatBuffer(TextureRotationUtils.CubeVertices);
        mTextureBuffer = OpenGLUtils.createFloatBuffer(TextureRotationUtils.TextureVertices);
    }

    public void onSurfaceChanged(int width, int height) {
        mSurfaceWidth = width;
        mSurfaceHeight = height;
        if (mFrameBuffers == null) {
            mFrameBuffers = new int[2];
            mFrameBufferTextures = new int[2];
            GLES20.glGenFramebuffers(2, mFrameBuffers, 0);
            GLES20.glGenTextures(2, mFrameBufferTextures, 0);
            bindFrameBuffer(mFrameBufferTextures[0], mFrameBuffers[0], width, height);
            bindFrameBuffer(mFrameBufferTextures[1], mFrameBuffers[1], width, height);
        }
        setUpTexture();
    }

    private void bindFrameBuffer(int textureId, int frameBuffer, int width, int height) {
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textureId);
        GLES20.glTexImage2D(GLES20.GL_TEXTURE_2D, 0, GLES20.GL_RGBA, width, height, 0,
                GLES20.GL_RGBA, GLES20.GL_UNSIGNED_BYTE, null);
        GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER,
                GLES20.GL_LINEAR);
        GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER,
                GLES20.GL_LINEAR);
        GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_S,
                GLES20.GL_CLAMP_TO_EDGE);
        GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_T,
                GLES20.GL_CLAMP_TO_EDGE);

        GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, frameBuffer);
        GLES20.glFramebufferTexture2D(GLES20.GL_FRAMEBUFFER, GLES20.GL_COLOR_ATTACHMENT0,
                GLES20.GL_TEXTURE_2D, textureId, 0);
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, 0);
        GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, 0);
    }

    private void setUpTexture() {
        // nv21 y texture
        mTextureY = new int[1];
        GLES20.glGenTextures(1, mTextureY, 0);
        GLES20.glBindTexture(TEXTURE_TYPE, mTextureY[0]);
        GLES20.glTexParameteri(TEXTURE_TYPE, GLES20.GL_TEXTURE_MAG_FILTER,
                GLES20.GL_NEAREST);
        GLES20.glTexParameteri(TEXTURE_TYPE, GLES20.GL_TEXTURE_MIN_FILTER,
                GLES20.GL_NEAREST);
        GLES20.glTexParameteri(TEXTURE_TYPE, GLES20.GL_TEXTURE_WRAP_S,
                GLES20.GL_CLAMP_TO_EDGE);
        GLES20.glTexParameteri(TEXTURE_TYPE, GLES20.GL_TEXTURE_WRAP_T,
                GLES20.GL_CLAMP_TO_EDGE);

        // nv21 uv texture
        mTextureUV = new int[1];
        GLES20.glGenTextures(1, mTextureUV, 0);
        GLES20.glBindTexture(TEXTURE_TYPE, mTextureUV[0]);
        GLES20.glTexParameteri(TEXTURE_TYPE, GLES20.GL_TEXTURE_MAG_FILTER,
                GLES20.GL_NEAREST);
        GLES20.glTexParameteri(TEXTURE_TYPE, GLES20.GL_TEXTURE_MIN_FILTER,
                GLES20.GL_NEAREST);
        GLES20.glTexParameteri(TEXTURE_TYPE, GLES20.GL_TEXTURE_WRAP_S,
                GLES20.GL_CLAMP_TO_EDGE);
        GLES20.glTexParameteri(TEXTURE_TYPE, GLES20.GL_TEXTURE_WRAP_T,
                GLES20.GL_CLAMP_TO_EDGE);
    }

    private void updateNV21YUVTexture(FrameInfo frameInfo) {
        Log.i("PreviewTracer", "updateNV21YUVTexture: " + mTextureY[0] + ";" + mTextureUV[0]);
        if (frameInfo == null) {
            return;
        }
        ByteBuffer frameRenderBuffer = frameInfo.outputBuffer;
        if (!mTextureInit) {
            frameRenderBuffer.position(0);
            GLES20.glBindTexture(TEXTURE_TYPE, mTextureY[0]);
            checkGlError("updateNV21YUVTexture00");
            GLES20.glTexImage2D(TEXTURE_TYPE, 0, GLES20.GL_LUMINANCE, frameInfo.frameWidth,
                    frameInfo.frameHeight, 0, GLES20.GL_LUMINANCE, GLES20.GL_UNSIGNED_BYTE,
                    frameRenderBuffer);
            checkGlError("updateNV21YUVTexture01");
            frameRenderBuffer.position(frameInfo.frameHeight * frameInfo.frameWidth);
            checkGlError("updateNV21YUVTexture02");
            GLES20.glBindTexture(TEXTURE_TYPE, mTextureUV[0]);
            checkGlError("updateNV21YUVTexture03");
            GLES20.glTexImage2D(TEXTURE_TYPE, 0, GLES20.GL_LUMINANCE_ALPHA,
                    frameInfo.frameWidth / 2, frameInfo.frameHeight / 2, 0, GLES20.GL_LUMINANCE_ALPHA,
                    GLES20.GL_UNSIGNED_BYTE, frameRenderBuffer);
            checkGlError("updateNV21YUVTexture04");
            mTextureInit = true;
        } else {
            frameRenderBuffer.position(0);
            GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
            checkGlError("updateNV21YUVTexture10");
            GLES20.glBindTexture(TEXTURE_TYPE, mTextureY[0]);
            checkGlError("updateNV21YUVTexture12");
            GLES20.glTexSubImage2D(TEXTURE_TYPE, 0, 0, 0, frameInfo.frameWidth, frameInfo.frameHeight,
                    GLES20.GL_LUMINANCE, GLES20.GL_UNSIGNED_BYTE, frameRenderBuffer);
            checkGlError("updateNV21YUVTexture13");
            frameRenderBuffer.position(frameInfo.frameWidth * frameInfo.frameHeight);
            GLES20.glBindTexture(TEXTURE_TYPE, mTextureUV[0]);
            checkGlError("updateNV21YUVTexture14");
            GLES20.glTexSubImage2D(TEXTURE_TYPE, 0, 0, 0, frameInfo.frameWidth / 2, frameInfo.frameHeight / 2,
                    GLES20.GL_LUMINANCE_ALPHA, GLES20.GL_UNSIGNED_BYTE, frameRenderBuffer);
            checkGlError("updateNV21YUVTexture15");
        }
    }

    public int yuv2Rgb(FrameInfo frameInfo) {
        if (frameInfo == null || frameInfo.bufferInfo == null || frameInfo.bufferInfo.size <= 0)
            return -1;
        updateNV21YUVTexture(frameInfo);
        checkGlError("onDrawFrame-1");
        GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, mFrameBuffers[0]);
        mVertexBuffer.position(0);
        GLES20.glVertexAttribPointer(yuvPositionLoc, mCoordsPerVertex, GLES20.GL_FLOAT, false, 0, mVertexBuffer);
        GLES20.glEnableVertexAttribArray(yuvPositionLoc);
        checkGlError("onDrawFrame0");
        mTextureBuffer.position(0);

        GLES20.glVertexAttribPointer(yuvTextureLoc, mCoordsPerVertex, GLES20.GL_FLOAT, false, 0, mTextureBuffer);
        GLES20.glEnableVertexAttribArray(yuvTextureLoc);
        GLES20.glViewport(0, 0, mSurfaceWidth, mSurfaceHeight);
        if (mTextureY[0] != -1) {
            GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
            checkGlError("onDrawFrame10");
            GLES20.glBindTexture(TEXTURE_TYPE, mTextureY[0]);
            checkGlError("onDrawFrame11:" + mTextureY[0]);
            GLES20.glUniform1i(yTextureLoc, 0);
            int error = GLES20.glGetError();
            checkGlError("onDrawFrame12:" + yTextureLoc);
        }
        checkGlError("onDrawFrame1");
        if (mTextureUV[0] != -1) {
            GLES20.glActiveTexture(GLES20.GL_TEXTURE1);
            GLES20.glBindTexture(TEXTURE_TYPE, mTextureUV[0]);
            GLES20.glUniform1i(uvTextureLoc, 1);
        }
        checkGlError("onDrawFrame2");
        GLES20.glViewport(0, 0, mSurfaceWidth, mSurfaceHeight);
        GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP, 0, 4);

        GLES20.glDisableVertexAttribArray(yuvPositionLoc);
        GLES20.glDisableVertexAttribArray(yuvTextureLoc);
        GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
        GLES20.glBindTexture(TEXTURE_TYPE, 0);
        GLES20.glActiveTexture(GLES20.GL_TEXTURE1);
        GLES20.glBindTexture(TEXTURE_TYPE, 0);
        GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, 0);
        GLES20.glUseProgram(0);
        return mFrameBufferTextures[0];
    }
}
