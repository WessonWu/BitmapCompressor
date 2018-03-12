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
        final int atLeastSize = mAtLeastSize;
        if (width <= mExpectedSize ||
                height <= mAtLeastSize
                || width * height <= mExpectedSize) {
            return 1;
        }

        final int longSide = Math.max(width, height);
        final int shortSide = Math.min(width, height);

        double scale = shortSide / longSide;
        int expectedLongSide = (int) Math.sqrt(expectedSize / scale);
        int expectedShortSide = (int) (expectedLongSide * scale);

        if (expectedShortSide < atLeastSize) {
            expectedShortSide = atLeastSize;
            expectedLongSide = (int) (expectedShortSide / scale);
        }
        return Util.calculateInSampleSize(width, height, expectedLongSide, expectedShortSide);
    }
}
