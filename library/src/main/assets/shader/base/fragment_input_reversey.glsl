precision mediump float;
varying vec2 textureCoordinate;
uniform sampler2D inputTexture;
void main() {
    vec2 uv = vec2(textureCoordinate.x, 1.0 - textureCoordinate.y);
    gl_FragColor = texture2D(inputTexture, uv);
}