package com.wilbert.library.frameprocessor.gles.effect;

import android.content.Context;

import com.wilbert.library.frameprocessor.gles.OpenGLUtils;
import com.wilbert.library.frameprocessor.gles.shaders.Shader;

/**
 * 仿抖音幻觉特效
 */
public class GLImageEffectIllusionFilter extends GLImageEffectFilter {
    public GLImageEffectIllusionFilter() {
        super(VERTEX_SHADER, Shader.FRAGMENT_ILLUSION);
    }

    public GLImageEffectIllusionFilter(Context context) {
        this(context, VERTEX_SHADER, OpenGLUtils.getShaderFromAssets(context, "shader/action/fragment_effect_illusion.glsl"));
    }

    public GLImageEffectIllusionFilter(Context context, String vertexShader, String fragmentShader) {
        super(context, vertexShader, fragmentShader);
    }
}
