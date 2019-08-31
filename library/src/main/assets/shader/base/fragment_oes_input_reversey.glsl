#extension GL_OES_EGL_image_external : require
precision mediump float;
varying vec2 textureCoordinate;
uniform samplerExternalOES inputTexture;
void main() {
    vec2 uv = vec2(textureCoordinate.x, 1.0 - textureCoordinate.y);
    gl_FragColor = texture2D(inputTexture, uv);
}