package com.github.wessonwu.bitmapcompressor;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.Log;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Wesson on 2018/1/20.
 */

public class BitmapCompressor {
    private static final String TAG = "BitmapCompressor";
    private final static String DEFAULT_DISK_CACHE_DIR = "bitmap_compressor_cache";

    private Context mContext;
    private List<String> mImagePaths;

    private InSampleSizeCalculator mCustomCalculator;
    private int mQuality;
    private Bitmap.CompressFormat mCompressFormat;

    private String mTargetPath;
    private OnCompressListener mOnCompressListener;

    private BitmapCompressor(Context context) {
        mContext = context;
        mImagePaths = new ArrayList<>();
        mQuality = 75;
        mCustomCalculator = new ExpectedSizeCalculator();
    }

    public static BitmapCompressor with(Context mContext) {
        return new BitmapCompressor(mContext);
    }

    public BitmapCompressor setQuality(int quality) {
        mQuality = quality;
        return this;
    }

    public BitmapCompressor setCompressFormat(Bitmap.CompressFormat format) {
        mCompressFormat = format;
        return this;
    }

    public BitmapCompressor setCustomCalculator(InSampleSizeCalculator customCalculator) {
        mCustomCalculator = customCalculator;
        return this;
    }

    public BitmapCompressor setCustomTargetPath(String customTargetPath) {
        mTargetPath = customTargetPath;
        return this;
    }

    public BitmapCompressor load(String imagePath) {
        mImagePaths.add(imagePath);
        return this;
    }

    public BitmapCompressor load(List<String> imagePaths) {
        mImagePaths.addAll(imagePaths);
        return this;
    }

    public File get(String imagePath) throws IOException {
        return newCompressTask(imagePath).compress();
    }

    public List<File> get(List<String> imagePaths) throws IOException {
        if (imagePaths == null || imagePaths.isEmpty()) {
            return null;
        }
        List<File> result = new ArrayList<>();
        for (String path : imagePaths) {
            result.add(get(path));
        }
        return result;
    }

    public void launch(OnCompressListener onCompressListener) {
        mOnCompressListener = onCompressListener;
        if (mOnCompressListener == null) {
            throw new IllegalArgumentException("OnCompressListener can not be null.");
        }
        AsyncTask.SERIAL_EXECUTOR.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    mOnCompressListener.onStart();
                    List<File> result = get(mImagePaths);
                    mOnCompressListener.onCompleted(result);
                } catch (IOException e) {
                    mOnCompressListener.onError(e);
                }
            }
        });
    }

    private CompressTask newCompressTask(String imagePath) {
        return new CompressTask(new File(imagePath),
                getImageCacheFile(mContext, Util.getSuffix(imagePath)),
                mCustomCalculator,
                mQuality,
                mCompressFormat);
    }

    /**
     * Returns a mFile with a cache audio name in the private cache directory.
     *
     * @param context A context.
     */
    private File getImageCacheFile(Context context, String suffix) {
        if (TextUtils.isEmpty(mTargetPath)) {
            mTargetPath = getImageCacheDir(context).getAbsolutePath();
        }

        String cacheBuilder = mTargetPath + "/" +
                System.currentTimeMillis() +
                (int) (Math.random() * 1000) +
                (TextUtils.isEmpty(suffix) ? ".jpg" : suffix);

        return new File(cacheBuilder);
    }

    /**
     * Returns a directory with a default name in the private cache directory of the application to
     * use to store retrieved audio.
     *
     * @param context A context.
     * @see #getImageCacheDir(Context, String)
     */
    @Nullable
    private File getImageCacheDir(Context context) {
        return getImageCacheDir(context, DEFAULT_DISK_CACHE_DIR);
    }

    /**
     * Returns a directory with the given name in the private cache directory of the application to
     * use to store retrieved media and thumbnails.
     *
     * @param context   A context.
     * @param cacheName The name of the subdirectory in which to store the cache.
     * @see #getImageCacheDir(Context)
     */
    @Nullable
    private File getImageCacheDir(Context context, String cacheName) {
        File cacheDir = context.getExternalCacheDir();
        if (cacheDir != null) {
            File result = new File(cacheDir, cacheName);
            if (!result.mkdirs() && (!result.exists() || !result.isDirectory())) {
                // File wasn't able to create a directory, or the result exists but not a directory
                return null;
            }
            return result;
        }
        if (Log.isLoggable(TAG, Log.ERROR)) {
            Log.e(TAG, "default disk cache dir is null");
        }
        return null;
    }
}
