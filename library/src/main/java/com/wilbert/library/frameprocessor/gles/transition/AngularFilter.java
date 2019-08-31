package com.wilbert.library.frameprocessor.gles.transition;

import android.content.Context;

import com.wilbert.library.frameprocessor.gles.OpenGLUtils;

/**
 * Created by Android Studio.
 * User: wilbert jw20082009@qq.com
 * Date: 2019/8/14 14:22
 */
public class AngularFilter extends BaseTransitionFilter {


    public AngularFilter(Context context) {
        super(context, VERTEX_SHADER, OpenGLUtils.getShaderFromAssets(context, "shader/transition/angular.glsl"));
    }
}
