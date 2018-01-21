package com.github.wessonwu.bitmapcompressor;

/**
 * Created by Wesson on 2018/1/20.
 */

public class ExpectedSizeCalculator implements InSampleSizeCalculator {
    private int mExpectedSize;
    private int mAtLeastSize;

    public ExpectedSizeCalculator() {
        this(960 * 540, 480);
    }

    public ExpectedSizeCalculator(int expectedSize, int atLeastSize) {
        mExpectedSize = expectedSize;
        mAtLeastSize = atLeastSize;
    }

    @Override
    public int calculateInSampleSize(int width, int height) {
        final int expectedSize = mExpectedSize;
        final double atLeastSize = mAtLeastSize;

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
