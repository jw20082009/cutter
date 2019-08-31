package com.wilbert.library.basic.renderer;

import android.opengl.GLSurfaceView;
import android.util.Log;

import com.wilbert.library.basic.entity.Transitions;
import com.wilbert.library.frameprocessor.gles.GLImageFilter;
import com.wilbert.library.frameprocessor.gles.OpenGLUtils;
import com.wilbert.library.frameprocessor.gles.transition.BaseTransitionFilter;

import java.nio.FloatBuffer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

/**
 * Created by Android Studio.
 * User: wilbert jw20082009@qq.com
 * Date: 2019/8/13 17:28
 */
public class TransitionRenderer extends FilterRenderer {
    private final String TAG = "TransitionRenderer";
    private HashMap<String, BaseTransitionFilter> transitionFilterMap = new HashMap<>();
    private List<Transitions> transitionList = new ArrayList<>();
    private boolean transitionlistChanged = false;
    private int videoIndex = 0;

    public TransitionRenderer(GLSurfaceView context) {
        super(context);
    }

    public void changeVideoSize(int width, int height) {
        Log.i("ImagePlayer", "changeVideoSize:" + width + ";" + height);
        videoIndex++;
        if (!transitionFilterMap.isEmpty()) {
            Iterator<Map.Entry<String, BaseTransitionFilter>> filterIterator = transitionFilterMap.entrySet().iterator();
            while (filterIterator.hasNext()) {
                Map.Entry<String, BaseTransitionFilter> entry = filterIterator.next();
                BaseTransitionFilter transitionFilter = entry.getValue();
                transitionFilter.centerInsideFrameBuffer(width, height);
            }
        }
    }

    public void simpleChangeVideoSize(int width, int height) {
        if (!transitionFilterMap.isEmpty()) {
            Iterator<Map.Entry<String, BaseTransitionFilter>> filterIterator = transitionFilterMap.entrySet().iterator();
            while (filterIterator.hasNext()) {
                Map.Entry<String, BaseTransitionFilter> entry = filterIterator.next();
                BaseTransitionFilter transitionFilter = entry.getValue();
                transitionFilter.centerInsideFrameBufferSimple(width, height);
            }
        }
    }

    public void setTransitionList(List<Transitions> transitionList) {
        if (transitionList != null && transitionList.size() > 0) {
            //判断如果两个列表不相同就使用新的替换旧的
            if (transitionList.size() == this.transitionList.size()) {
                boolean equals = true;
                for (int i = 0; i < transitionList.size(); i++) {
                    if (transitionList.get(i) != this.transitionList.get(i)) {
                        equals = false;
                        break;
                    }
                }
                if (equals) {
                    return;
                }
            }
            transitionlistChanged = true;
            this.transitionList.clear();
            this.transitionList.addAll(transitionList);
        } else {
            if (this.transitionList.size() > 0) {
                transitionlistChanged = true;
                this.transitionList.clear();
            }
        }
    }

    @Override
    public void onSurfaceCreated(GL10 gl, EGLConfig config) {
        super.onSurfaceCreated(gl, config);
    }

    /**
     * 只在第一个视频的宽高获取到时被调用
     */
    @Override
    protected void onChildFilterSizeChanged() {
        super.onChildFilterSizeChanged();
        if (!transitionFilterMap.isEmpty()) {
            Iterator<Map.Entry<String, BaseTransitionFilter>> filterIterator = transitionFilterMap.entrySet().iterator();
            while (filterIterator.hasNext()) {
                Map.Entry<String, BaseTransitionFilter> entry = filterIterator.next();
                BaseTransitionFilter transitionFilter = entry.getValue();
                transitionFilter.onInputSizeChanged(mIncommingWidth, mIncommingHeight);
                transitionFilter.initFrameBuffer(mIncommingWidth, mIncommingHeight);
            }
        }
    }

    private BaseTransitionFilter getCurrentTransitionFilter() {
        int index = 0;
        if (videoIndex >= 0 && transitionList != null && transitionList.size() > 0) {
            index = videoIndex;
        } else {
            return null;
        }
        Transitions transitions = transitionList.get(index % transitionList.size());
        return transitionFilterMap.get(transitions.name());
    }

    @Override
    protected void beforeDrawFrame() {
        super.beforeDrawFrame();
        if (transitionlistChanged) {
            transitionlistChanged = false;
            if (transitionList.isEmpty()) {
                releaseTransitionList();
            } else {
                for (Transitions t : transitionList) {
                    if (!transitionFilterMap.containsKey(t.name())) {
                        BaseTransitionFilter baseTransitionFilter = new BaseTransitionFilter(mContext, GLImageFilter.VERTEX_SHADER, OpenGLUtils.getShaderFromAssets(mContext, t.getPath()));
                        if (mIncommingWidth > 0 && mIncommingHeight > 0) {
                            baseTransitionFilter.onInputSizeChanged(mIncommingWidth, mIncommingHeight);
                            baseTransitionFilter.initFrameBuffer(mIncommingWidth, mIncommingHeight);
                        }
                        baseTransitionFilter.onDisplaySizeChanged(mSurfaceWidth, mSurfaceHeight);
                        transitionFilterMap.put(t.name(), baseTransitionFilter);
                    }
                }
            }
        }
    }

    @Override
    protected int onDrawFrameBuffer(int textureId, FloatBuffer vertexBuffer, FloatBuffer textureBuffer) {
        textureId = super.onDrawFrameBuffer(textureId, vertexBuffer, textureBuffer);
        BaseTransitionFilter transitionFilter = getCurrentTransitionFilter();
        if (transitionFilter != null) {
            textureId = transitionFilter.drawFrameBuffer(textureId, vertexBuffer, textureBuffer);
        }
        return textureId;
    }

    @Override
    public void _release() {
        Log.e("TransitionRenderer", "release");
        super._release();
        releaseTransitionList();
    }

    private void releaseTransitionList() {
        if (!transitionFilterMap.isEmpty()) {
            Iterator<Map.Entry<String, BaseTransitionFilter>> filterIterator = transitionFilterMap.entrySet().iterator();
            while (filterIterator.hasNext()) {
                Map.Entry<String, BaseTransitionFilter> entry = filterIterator.next();
                BaseTransitionFilter transitionFilter = entry.getValue();
                transitionFilter.release();
            }
        }
        videoIndex = 0;
        transitionFilterMap.clear();
        transitionList.clear();
    }

    @Override
    public void rotate90() {
        super.rotate90();
        if (!transitionFilterMap.isEmpty()) {
            Iterator<Map.Entry<String, BaseTransitionFilter>> filterIterator = transitionFilterMap.entrySet().iterator();
            while (filterIterator.hasNext()) {
                Map.Entry<String, BaseTransitionFilter> entry = filterIterator.next();
                BaseTransitionFilter transitionFilter = entry.getValue();
                transitionFilter.setReversed(true);
            }
        }
    }

    @Override
    public void rotate270() {
        super.rotate270();
        if (!transitionFilterMap.isEmpty()) {
            Iterator<Map.Entry<String, BaseTransitionFilter>> filterIterator = transitionFilterMap.entrySet().iterator();
            while (filterIterator.hasNext()) {
                Map.Entry<String, BaseTransitionFilter> entry = filterIterator.next();
                BaseTransitionFilter transitionFilter = entry.getValue();
                transitionFilter.setReversed(true);
            }
        }
    }
}
