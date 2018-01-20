package com.github.wessonwu.bitmapcompressor;

/**
 * Created by Wesson on 2018/1/20.
 */

public class ExpectedSizeCalculator implements InSampleSizeCalculator {
    @Override
    public int calculateInSampleSize(int width, int height) {
        final int expectedSize = 960 * 540;

        final double atLeastSize = 540;
        final double longSide = Math.max(width, height);
        final double shortSide = Math.min(width, height);

        double scale = shortSide / longSide;
        double expectedLongSide = Math.sqrt(expectedSize / scale);
        double expectedShortSide = expectedLongSide * scale;

        if (expectedShortSide < atLeastSize) {
            expectedShortSide = atLeastSize;
            expectedLongSide = expectedShortSide / scale;
        }
        return Util.calculateInSampleSize(width, height, (int)expectedLongSide, (int)expectedShortSide);
    }
}
