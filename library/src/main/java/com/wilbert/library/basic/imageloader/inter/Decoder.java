package com.wilbert.library.basic.imageloader.inter;

import android.graphics.Bitmap;

import com.wilbert.library.basic.imageloader.ImageRequest;


/**
 * Created by yuanyang on 17/8/2.
 */

public interface Decoder {

    Bitmap decode(ImageRequest request);
}
