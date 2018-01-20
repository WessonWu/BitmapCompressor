package com.github.wessonwu.bitmapcompressor;

/**
 * Created by Wesson on 2018/1/20.
 */

public interface InSampleSizeCalculator {
    /**
     * Calculate inSampleSize {@link android.graphics.BitmapFactory.Options#inSampleSize}.
     * @param width Raw width of the bitmap that need to compress.
     * @param height Raw height of the bitmap that need to compress.
     * @return inSampleSize
     */
    int calculateInSampleSize(int width, int height);
}
