package com.wilbert.library.frameprocessor.sticker;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PaintFlagsDrawFilter;
import android.graphics.PorterDuff;
import android.opengl.GLES20;
import android.text.TextPaint;
import android.text.TextUtils;
import android.util.Log;

import com.wilbert.library.basic.entity.StickerEntity;
import com.wilbert.library.frameprocessor.gles.GLImageFilter;
import com.wilbert.library.frameprocessor.gles.OpenGLUtils;
import com.wilbert.library.basic.utils.DensityUtils;
import com.wilbert.library.basic.imageloader.ImageRequest;
import com.wilbert.library.basic.imageloader.LocalImageLoader;

import java.nio.FloatBuffer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class StickerFilter extends GLImageFilter {

    private boolean needDrawLog = true;
    List<StickerEntity> stickerEntities;
    Bitmap currentBitmap;
    int currentTextureId = OpenGLUtils.GL_NOT_TEXTURE;
    HashMap<String, Bitmap> currentStickerImage = new HashMap<>();
    Matrix matrix;
    Paint bitmapPaint;
    PaintFlagsDrawFilter drawFilter;
    TextPaint textPaint;
    int textPadding;

    public StickerFilter(Context context) {
        super(context, VERTEX_SHADER, OpenGLUtils.getShaderFromAssets(context, "shader/base/fragment_input_reversey.glsl"));
        init();
    }

    public StickerFilter(Context context, String vertexShader, String fragmentShader) {
        super(context, vertexShader, fragmentShader);
        init();
    }

    public StickerFilter(String vertexShader, String fragmentShader) {
        super(vertexShader, fragmentShader);
        init();
    }

    private void init() {
        bitmapPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        bitmapPaint.setFilterBitmap(true);
        drawFilter = new PaintFlagsDrawFilter(0, Paint.ANTI_ALIAS_FLAG | Paint.FILTER_BITMAP_FLAG);
    }

    private TextPaint getTextPaint() {
        if (textPaint == null) {
            textPaint = new TextPaint(Paint.ANTI_ALIAS_FLAG);
            textPaint.setColor(Color.WHITE);
            textPaint.setTextSize(DensityUtils.dp2px(mContext, 20));
            textPaint.setFilterBitmap(true);
        }
        return textPaint;
    }

    public void setStickerEntities(List<StickerEntity> stickerEntities) {
        this.stickerEntities = stickerEntities;
    }

    @Override
    public void onInputSizeChanged(int width, int height) {
        super.onInputSizeChanged(width, height);
        Log.i(TAG, "onInputSizeChanged:" + width + "*" + height + ";" + hashCode());
    }

    public boolean drawFrame(FloatBuffer vertexBuffer, FloatBuffer textureBuffer, long currentTime) {
        boolean result = false;
        if (stickerEntities != null && stickerEntities.size() > 0) {
            for (final StickerEntity stickerEntity : stickerEntities) {
                loadBitmap(currentTime, stickerEntity);
            }

            if (currentBitmap == null && !currentStickerImage.values().isEmpty()) {
                if (needDrawLog) {
                    needDrawLog = false;
                    Log.i(TAG, "drawFrame:" + mImageWidth + "*" + mImageHeight + ";" + hashCode());
                }
                currentBitmap = Bitmap.createBitmap(mImageWidth, mImageHeight, Bitmap.Config.ARGB_8888);
            }
            if (currentBitmap != null) {
                Canvas canvas = new Canvas(currentBitmap);
                canvas.setDrawFilter(drawFilter);
                canvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR);
                drawStickers(canvas);
                currentTextureId = OpenGLUtils.createTexture(currentBitmap, currentTextureId);
                result = drawFrame(currentTextureId, vertexBuffer, textureBuffer);
            }
        }
        return result;
    }

    @Override
    public int drawFrameBuffer(int textureId, FloatBuffer vertexBuffer, FloatBuffer textureBuffer) {
        // 没有FBO、没初始化、输入纹理不合法、滤镜不可用时，直接返回
        if (textureId == OpenGLUtils.GL_NOT_TEXTURE
                || mFrameBuffers == null
                || mFrameBufferTextures == null
                || !mIsInitialized
                || !mFilterEnable) {
            return textureId;
        }

        // 绑定FBO
        GLES20.glViewport(mStartX, mStartY, mFrameWidth, mFrameHeight);
        GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, mFrameBuffers[0]);
        // 使用当前的program
        GLES20.glUseProgram(mProgramHandle);
        // 运行延时任务，这个要放在glUseProgram之后，要不然某些设置项会不生效
        runPendingOnDrawTasks();
        // 绘制纹理
        onDrawTexture(textureId, vertexBuffer, textureBuffer);
        return mFrameBufferTextures[0];
    }

    public int drawFrameBuffer(int textureId, FloatBuffer vertexBuffer, FloatBuffer textureBuffer, long currentTime) {
        int result = this.drawFrameBuffer(textureId, vertexBuffer, textureBuffer);
        if (stickerEntities != null && stickerEntities.size() > 0) {
            for (final StickerEntity stickerEntity : stickerEntities) {
                loadBitmap(currentTime, stickerEntity);
            }
            if (currentBitmap == null && !currentStickerImage.values().isEmpty()) {
                StickerEntity stickerEntity = stickerEntities.get(0);
                /**
                 * 使用输入源宽高(等于framebuffer宽高)创建bitmap
                 */
                currentBitmap = Bitmap.createBitmap(mImageWidth, mImageHeight, Bitmap.Config.ARGB_8888);
            }
            if (currentBitmap != null) {
                Canvas canvas = new Canvas(currentBitmap);
                canvas.setDrawFilter(drawFilter);
                canvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR);
                drawStickers(canvas);
                currentTextureId = OpenGLUtils.createTexture(currentBitmap, currentTextureId);
                drawCustomFrame(currentTextureId, vertexBuffer, textureBuffer);
            }
        }
        GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, 0);
        return result;
    }

    private void loadBitmap(long currentTime, final StickerEntity stickerEntity) {
        List<String> stickers = stickerEntity.getStickers();
        if (stickers != null && stickers.size() > 0) {
            String path = stickerEntity.getCurrentStickerImage(currentTime);
            final int id = stickerEntity.getId();
            if (path != null) {
                ImageRequest request = new ImageRequest.ImageRequestBuilder().path(path).inMutable().build();
                LocalImageLoader.getInstance(mContext).loadImageBitmap(request,
                        new LocalImageLoader.ILocalBitmapListener() {
                            @Override
                            public void onLoadResult(Bitmap bitmap) {
                                drawText(bitmap, stickerEntity);
                                currentStickerImage.put(stickerEntity.getStickerId(), bitmap);
                            }
                        });
            } else {
                currentStickerImage.put(stickerEntity.getStickerId(), null);
            }
        } else if (!TextUtils.isEmpty(stickerEntity.getText())) {
            final int id = stickerEntity.getId();
            if (currentTime >= stickerEntity.getStartTime() && currentTime < (stickerEntity.getStartTime() + stickerEntity.getDurationTime())) {
                Bitmap bitmap = currentStickerImage.get(stickerEntity.getId());
                if (bitmap == null || bitmap.isRecycled()) {
                    TextPaint paint = getTextPaint();
                    paint.setTextSize(stickerEntity.getTextSize());
                    Paint.FontMetrics metrics = paint.getFontMetrics();
                    float height = textPadding * 2 + metrics.bottom - metrics.top;
                    float width = textPadding * 2 + paint.measureText(stickerEntity.getText());
                    bitmap = Bitmap.createBitmap((int) width, (int) height, Bitmap.Config.ARGB_8888);
                }
                drawText(bitmap, stickerEntity);
                currentStickerImage.put(stickerEntity.getStickerId(), bitmap);
            } else {
                currentStickerImage.put(stickerEntity.getStickerId(), null);
            }
        } else {
            // 无效的实体
        }
    }

    private void drawText(Bitmap bitmap, StickerEntity entity) {
        if (bitmap != null && !bitmap.isRecycled() && entity != null
                && !TextUtils.isEmpty(entity.getText())) {
            String text = entity.getText();
            Canvas canvas = new Canvas(bitmap);
            TextPaint paint = getTextPaint();
            paint.setColor(entity.getTextColor());
            paint.setTextSize(entity.getTextSize());
            Paint.FontMetrics metrics = paint.getFontMetrics();
            float top = metrics.top;
            float bottom = metrics.bottom;
            int lines = 1;
            if (entity.getMaxWordsPerLine() > 0)
                lines = (int) Math.ceil(1.0f * text.length() / entity.getMaxWordsPerLine());
            if (lines > entity.getMaxLines()) {
                lines = entity.getMaxLines();
            }
            List<String> splits = new ArrayList<>();
            for (int i = 0; i < lines; i++) {
                int start = i * entity.getMaxWordsPerLine();
                int end = start + entity.getMaxWordsPerLine();
                if (i == lines - 1) {
                    int length = entity.getMaxWordsPerLine()<=0?text.length():text.length() % entity.getMaxWordsPerLine();
                    if (length == 0) {
                        end = start + text.length();
                    } else {
                        end = start + length;
                    }
                }
                splits.add(text.substring(start, end));
            }
            if (splits.size() <= 0) {
                return;
            }
            int lineHeight = (int) (1.0f * bitmap.getHeight() / splits.size());
            for (int i = 0; i < splits.size(); i++) {
                String str = splits.get(i);
                int baseLineY = (int) (lineHeight / 2.0f + i * lineHeight - top / 2 - bottom / 2);
                canvas.drawText(str, textPadding, baseLineY, textPaint);
            }
        }
    }

    private void drawStickers(Canvas canvas) {
        if (stickerEntities != null && stickerEntities.size() > 0) {
            for (StickerEntity stickerEntity : stickerEntities) {
                Bitmap bitmap = currentStickerImage.get(stickerEntity.getStickerId());
                if (matrix == null) {
                    matrix = new Matrix();
                }
                if (stickerEntity.getMatrixArray() == null && bitmap != null) {
                    matrix.setTranslate(mImageWidth - bitmap.getWidth(), mImageHeight - bitmap.getHeight());
                    float[] matrixTrans = new float[9];
                    matrix.getValues(matrixTrans);
                    stickerEntity.setMatrixArray(matrixTrans);
                } else if (stickerEntity.getMatrixArray() != null) {
                    matrix.setValues(stickerEntity.getMatrixArray());
                }
                if (bitmap != null && !bitmap.isRecycled()) {
                    canvas.drawBitmap(bitmap, matrix, bitmapPaint);
                }
            }
        }
    }

    @Override
    public boolean drawFrame(int textureId, FloatBuffer vertexBuffer, FloatBuffer textureBuffer) {
        boolean result = false;
        GLES20.glDisable(GLES20.GL_DEPTH_TEST);
        GLES20.glEnable(GLES20.GL_BLEND);
        GLES20.glBlendFunc(GLES20.GL_ONE, GLES20.GL_ONE_MINUS_SRC_ALPHA);
        result = super.drawFrame(textureId, vertexBuffer, textureBuffer);
        GLES20.glDisable(GLES20.GL_BLEND);
        return result;
    }

    public boolean drawCustomFrame(int textureId, FloatBuffer vertexBuffer, FloatBuffer textureBuffer) {
        boolean result = false;
        GLES20.glDisable(GLES20.GL_DEPTH_TEST);
        GLES20.glEnable(GLES20.GL_BLEND);
        GLES20.glBlendFunc(GLES20.GL_ONE, GLES20.GL_ONE_MINUS_SRC_ALPHA);
        // 使用当前的program
        GLES20.glUseProgram(mProgramHandle);
        // 运行延时任务
        runPendingOnDrawTasks();

        GLES20.glClearColor(0.0f, 0.0f, 0.0f, 1.0f);

        // 绘制纹理
        onDrawTexture(textureId, vertexBuffer, textureBuffer);
        GLES20.glDisable(GLES20.GL_BLEND);
        return result;
    }

    @Override
    public void release() {
        super.release();
        if (currentTextureId != OpenGLUtils.GL_NOT_TEXTURE) {
            GLES20.glDeleteTextures(1, new int[]{
                    currentTextureId
            }, 0);
            currentTextureId = OpenGLUtils.GL_NOT_TEXTURE;
        }
    }
}
