package com.github.wessonwu.bitmapcompressor;

import java.io.File;
import java.util.List;

/**
 * Created by Wesson on 2018/1/21.
 */

public interface OnCompressListener {
    void onStart();
    void onCompleted(List<File> compressedFiles);
    void onError(Throwable e);
}
