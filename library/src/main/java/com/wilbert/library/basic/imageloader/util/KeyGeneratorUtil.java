
package com.wilbert.library.basic.imageloader.util;

import com.wilbert.library.basic.imageloader.ImageRequest;

/**
 * Created by yuanyang on 17/8/2.
 */

public class KeyGeneratorUtil {

    public static String generateKey(ImageRequest request) {
        if (request == null || request.getPath() == null)
            return null;
        StringBuffer sb = new StringBuffer(request.getPath()).append("_");
        if (request.getResizeOptions() != null) {
            sb.append(request.getResizeOptions().getResizeWidth()).append("x")
                    .append(request.getResizeOptions().getResizeHeight());
        } else {
            sb.append("-1").append("x").append("-1");
        }
        return sb.toString();

    }
}
