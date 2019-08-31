package com.wilbert.library.videoprocessor.effect;

import android.content.Context;
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

/**
 * Created by Android Studio.
 * User: wilbert jw20082009@qq.com
 * Date: 2019/8/20 19:39
 */
public class TransitionRenderer extends ActionRenderer {

    private final String TAG = "TransitionRenderer";
    private HashMap<String, BaseTransitionFilter> transitionFilterMap = new HashMap<>();
    private List<Transitions> transitionList = new ArrayList<>();
    private boolean transitionlistChanged = false;
    private int videoIndex = 0;
    private int firstWidth, firstHeight;

    public TransitionRenderer(Context context) {
        super(context);
    }

    public void changeVideoSize(int width, int height) {
        videoIndex++;
        Log.i("MultiVideoPlayFragment", "changeVideoSize:" + videoIndex);
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
        firstWidth = width;
        firstHeight = height;
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
            this.transitionList.clear();
            this.transitionList.addAll(transitionList);
        } else {
            this.transitionList.clear();
        }
        transitionlistChanged = true;
    }

    @Override
    public void onInputSizeChanged(int width, int height) {
        super.onInputSizeChanged(width, height);
        Log.i(TAG, "onInputSizeChanged:" + width + ";" + height);
    }

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
                        if (firstWidth > 0 && firstHeight > 0) {
                            baseTransitionFilter.centerInsideFrameBufferSimple(firstWidth, firstHeight);
                        }
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
    public void release() {
        super.release();
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
}
