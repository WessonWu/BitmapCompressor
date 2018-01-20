package com.github.wessonwu.bitmapcompressor;

/**
 * Created by Wesson on 2018/1/20.
 */

public class SimpleCalculator implements InSampleSizeCalculator {
    private final int mMaxWidth;
    private final int mMaxHeight;

    public SimpleCalculator(int maxWidth, int maxHeight) {
        mMaxWidth = maxWidth;
        mMaxHeight = maxHeight;
    }

    @Override
    public int calculateInSampleSize(int width, int height) {
        return Util.calculateInSampleSize(width, height, mMaxWidth, mMaxHeight);
    }
}
