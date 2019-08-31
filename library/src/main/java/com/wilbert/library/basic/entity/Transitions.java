package com.wilbert.library.basic.entity;

/**
 * Created by Android Studio.
 * User: wilbert jw20082009@qq.com
 * Date: 2019/8/14 15:23
 */
public enum Transitions {

    Angular("shader/transition/angular.glsl"),
    BowTieHorizontal("shader/transition/bowTieHorizontal.glsl"),
    BowTieVertical("shader/transition/bowTieVertical.glsl"),
    Circleopen("shader/transition/circleopen.glsl"),
    Cube("shader/transition/cube.glsl"),
    Directional("shader/transition/directional.glsl"),
    Doorway("shader/transition/doorway.glsl"),
    GlitchMemories("shader/transition/glitchMemories.glsl"),
    GridFlip("shader/transition/gridFlip.glsl"),
    Heart("shader/transition/heart.glsl"),
    Hexagonalize("shader/transition/hexagonalize.glsl"),
    InvertedPageCurl("shader/transition/invertedPageCurl.glsl"),
    LinearBlur("shader/transition/linearBlur.glsl"),
    Perlin("shader/transition/perlin.glsl"),
    Pinwheel("shader/transition/pinwheel.glsl"),
    Pixelize("shader/transition/pixelize.glsl"),
    PolkaDotsCurtain("shader/transition/polkaDotsCurtain.glsl"),
    Radial("shader/transition/radial.glsl"),
    Rotate_scale_fade("shader/transition/rotate_scale_fade.glsl"),
    SimpleZoom("shader/transition/simpleZoom.glsl"),
    Squareswire("shader/transition/squareswire.glsl"),
    StereoViewer("shader/transition/stereoViewer.glsl"),
    Swap("shader/transition/swap.glsl"),
    Swirl("shader/transition/swirl.glsl"),
    Waterdrop("shader/transition/waterdrop.glsl"),
    Sindowslice("shader/transition/windowslice.glsl");

    String path;

    Transitions(String path) {
        this.path = path;
    }

    public String getPath() {
        return path;
    }
}
