package com.github.wessonwu.bitmapcompressor;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.text.TextUtils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * Created by Wesson on 2018/1/20.
 */

public class CompressTask {
    private File mSrcImage;
    private File mTargetImage;

    private InSampleSizeCalculator mCalculator;
    private int mQuality;
    private Bitmap.CompressFormat mCompressFormat;

    public CompressTask(File srcImage,
                        File targetImage,
                        int quality,
                        InSampleSizeCalculator calculator) {
        this(srcImage,
                targetImage,
                calculator,
                quality,
                null);
    }

    public CompressTask(File srcImage,
                        File targetImage,
                        InSampleSizeCalculator calculator,
                        int quality,
                        Bitmap.CompressFormat compressFormat) {
        mSrcImage = srcImage;
        mTargetImage = targetImage;
        mCalculator = calculator;
        mQuality = quality;
        mCompressFormat = compressFormat;
    }

    public File compress() throws IOException {
        FileOutputStream fileOutputStream = null;
        File parentDir = mTargetImage.getParentFile();
        if (!parentDir.exists()) {
            parentDir.mkdirs();
        }
        try {

            final String srcImagePath = mSrcImage.getAbsolutePath();

            // decode bitmap's properties.
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inJustDecodeBounds = true;
            options.inSampleSize = 1;
            BitmapFactory.decodeFile(srcImagePath, options);

            // get bitmap's properties.
            final int width = options.outWidth;
            final int height = options.outHeight;
            final String mimeType = options.outMimeType;

            // decode sampled bitmap.
            options.inJustDecodeBounds = false;
            options.inSampleSize = mCalculator.calculateInSampleSize(width, height);
            Bitmap scaledBitmap = BitmapFactory.decodeFile(srcImagePath, options);
            scaledBitmap = Util.checkAndRotateBitmap(scaledBitmap, srcImagePath);

            // write the compressed bitmap to target file.
            fileOutputStream = new FileOutputStream(mTargetImage);
            scaledBitmap.compress(
                    getCompressFormat(mimeType),
                    mQuality,
                    fileOutputStream);
        } finally {
            if (fileOutputStream != null) {
                fileOutputStream.flush();
                fileOutputStream.close();
            }
        }

        return mTargetImage;

    }

    private final static String MIME_JPG = "image/jpeg";
    private final static String MIME_PNG = "image/png";
    private final static String MIME_WEBP = "image/webp";

    private Bitmap.CompressFormat getCompressFormat(String mimeType) {
        if (mCompressFormat != null) {
            return mCompressFormat;
        }
        if (!TextUtils.isEmpty(mimeType)) {
            switch (mimeType) {
                case MIME_JPG:
                    return Bitmap.CompressFormat.JPEG;
                case MIME_PNG:
                    return Bitmap.CompressFormat.PNG;
                case MIME_WEBP:
                    return Bitmap.CompressFormat.WEBP;
            }
        }
        return Bitmap.CompressFormat.JPEG;
    }
}
