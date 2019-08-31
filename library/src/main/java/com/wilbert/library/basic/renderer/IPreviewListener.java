
package com.wilbert.library.basic.renderer;

import java.nio.ByteBuffer;

public interface IPreviewListener {
    void onChangePreviewSize(int previewW, int previewH);

    void onPictureTaken(ByteBuffer buffer, int imageWidth, int imageHeight);
}
