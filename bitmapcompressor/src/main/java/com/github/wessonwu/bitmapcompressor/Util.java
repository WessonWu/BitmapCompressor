package com.github.wessonwu.bitmapcompressor;

import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.text.TextUtils;

import java.io.IOException;

/**
 * Created by Wesson on 2018/1/20.
 */

public class Util {
    public static int calculateInSampleSize(int width, int height,
                                            int reqWidth, int reqHeight) {
        int inSampleSize = 1;

        if (height > reqHeight || width > reqWidth) {

            final int halfHeight = height / 2;
            final int halfWidth = width / 2;

            // Calculate the largest inSampleSize value that is a power of 2 and keeps both
            // height and width larger than the requested height and width.
            while ((halfHeight / inSampleSize) >= reqHeight
                    && (halfWidth / inSampleSize) >= reqWidth) {
                inSampleSize *= 2;
            }
        }

        return inSampleSize;
    }

    public static Bitmap checkAndRotateBitmap(Bitmap bitmap, String imagePath) throws IOException {
        //check the rotation of the image and display it properly
        ExifInterface exif = new ExifInterface(imagePath);
        int orientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, 0);
        Matrix matrix = new Matrix();
        if (orientation == ExifInterface.ORIENTATION_ROTATE_90) {
            matrix.postRotate(90);
        } else if (orientation == ExifInterface.ORIENTATION_ROTATE_180) {
            matrix.postRotate(180);
        } else if (orientation == ExifInterface.ORIENTATION_ROTATE_270) {
            matrix.postRotate(270);
        } else {
            return bitmap;
        }
        return Bitmap.createBitmap(bitmap,
                0,
                0,
                bitmap.getWidth(),
                bitmap.getHeight(),
                matrix,
                true);
    }

    static String getSuffix(String path) {
        if (TextUtils.isEmpty(path)) {
            return ".jpg";
        }

        return path.substring(path.lastIndexOf("."),
                path.length());
    }
}
