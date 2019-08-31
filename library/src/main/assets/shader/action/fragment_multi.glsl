#extension GL_OES_EGL_image_external : require
precision mediump float;
varying vec2 vTextureCoord;
uniform sampler2D sTexture;
uniform int multiswitch;//0关闭，3：3分屏幕，4:4分屏，9：9分屏

void main() {
    mediump vec2 uv = vTextureCoord;
    if (multiswitch == 9){ //分屏
        if (uv.x < 1.0 / 3.0) {
            uv.x = uv.x * 3.0;
        } else if (uv.x < 2.0 / 3.0) {
            uv.x = (uv.x - 1.0 / 3.0) * 3.0;
        } else {
            uv.x = (uv.x - 2.0 / 3.0) * 3.0;
        }
        if (uv.y <= 1.0 / 3.0) {
            uv.y = uv.y * 3.0;
        } else if (uv.y < 2.0 / 3.0) {
            uv.y = (uv.y - 1.0 / 3.0) * 3.0;
        } else {
            uv.y = (uv.y - 2.0 / 3.0) * 3.0;
        }
    } else if (multiswitch == 6){
        if (uv.x <= 1.0 / 3.0) {
            uv.x = uv.x + 1.0 / 3.0;
        } else if (uv.x >= 2.0 / 3.0) {
            uv.x = uv.x - 1.0 / 3.0;
        }
        // 上下分两屏，保留 0.25 ~ 0.75部分
        if (uv.y <= 0.5) {
            uv.y = uv.y + 0.25;
        } else {
            uv.y = uv.y - 0.25;
        }
    } else if (multiswitch == 4){
        if (uv.x <= 0.5) {
            uv.x = uv.x * 2.0;
        } else {
            uv.x = (uv.x - 0.5) * 2.0;
        }
        if (uv.y <= 0.5) {
            uv.y = uv.y * 2.0;
        } else {
            uv.y = (uv.y - 0.5) * 2.0;
        }
    } else if (multiswitch == 3){
        if (uv.y < 1.0 / 3.0) {
            uv.y = uv.y + 1.0 / 3.0;
        } else if (uv.y > 2.0 / 3.0) {
            uv.y = uv.y - 1.0 / 3.0;
        }
    } else if (multiswitch == 2){
        if (uv.y >= 0.0 && uv.y <= 0.5) {
            uv.y = uv.y + 0.25;
        } else {
            uv.y = uv.y - 0.25;
        }
    }
    gl_FragColor = texture2D(sTexture, uv);
}
