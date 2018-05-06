package com.github.wessonwu.bitmapcompressor;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.Log;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Wesson on 2018/1/20.
 */

public class BitmapCompressor {
    private static final String TAG = "BitmapCompressor";
    private final static String DEFAULT_DISK_CACHE_DIR = "bitmap_compressor_cache";

    private List<String> mImagePaths;
    private InSampleSizeCalculator mCustomCalculator;
    private int mQuality;
    private Bitmap.CompressFormat mCompressFormat;
    private String mTargetPath;

    private BitmapCompressor(Builder builder) {
        mImagePaths = builder.mImagePaths;
        mQuality = builder.mQuality;
        mCustomCalculator = builder.mCustomCalculator;
        mCompressFormat = builder.mCompressFormat;
        mTargetPath = builder.mTargetPath;
    }

    public static Builder with(Context context) {
        return new Builder(context);
    }

    private CompressTask newCompressTask(Context context, String imagePath) throws IOException {
        return new CompressTask(new File(imagePath),
                getImageCacheFile(context, Util.getSuffix(imagePath)),
                mCustomCalculator,
                mQuality,
                mCompressFormat);
    }

    private File get(Context context, String imagePath) throws IOException {
        return newCompressTask(context, imagePath).compress();
    }

    private List<File> get(Context context, List<String> imagePaths) throws IOException {
        if (imagePaths == null || imagePaths.isEmpty()) {
            return null;
        }
        List<File> result = new ArrayList<>();
        for (String path : imagePaths) {
            result.add(get(context, path));
        }
        return result;
    }

    private void launch(final Context context,
                       final OnCompressListener onCompressListener) {
        if (onCompressListener == null) {
            throw new IllegalArgumentException("OnCompressListener can not be null.");
        }
        //AsyncTask.THREAD_POOL_EXECUTOR maybe OOM
        AsyncTask.SERIAL_EXECUTOR.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    onCompressListener.onStart();
                    List<File> result = get(context, mImagePaths);
                    onCompressListener.onCompleted(result);
                } catch (IOException e) {
                    onCompressListener.onError(e);
                }
            }
        });
    }

    /**
     * Returns a mFile with a cache audio name in the private cache directory.
     *
     * @param context A context.
     */
    private File getImageCacheFile(Context context, String suffix) throws IOException{
        File targetPathFile;
        if (TextUtils.isEmpty(mTargetPath)) {
            targetPathFile = getImageCacheDir(context);
        } else {
            targetPathFile = new File(mTargetPath);
        }
        if (targetPathFile == null) {
            throw new FileNotFoundException("Image cache directory can not find in the device.");
        }
        String targetPath = targetPathFile.getAbsolutePath();
        String cacheBuilder = targetPath + "/" +
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


    static class Builder {
        private Context mContext;
        private List<String> mImagePaths = new ArrayList<>();

        private InSampleSizeCalculator mCustomCalculator = new ExpectedSizeCalculator();
        private int mQuality = 75;
        private Bitmap.CompressFormat mCompressFormat = null;

        private String mTargetPath = null;

        Builder(Context context) {
            mContext = context;
        }

        public Builder setQuality(int quality) {
            mQuality = quality;
            return this;
        }

        public Builder setCompressFormat(Bitmap.CompressFormat format) {
            mCompressFormat = format;
            return this;
        }

        public Builder setCustomCalculator(InSampleSizeCalculator customCalculator) {
            mCustomCalculator = customCalculator;
            return this;
        }

        public Builder setCustomTargetPath(String customTargetPath) {
            mTargetPath = customTargetPath;
            return this;
        }

        public Builder load(String imagePath) {
            mImagePaths.add(imagePath);
            return this;
        }

        public Builder load(List<String> imagePaths) {
            mImagePaths.addAll(imagePaths);
            return this;
        }

        public File get(String imagePath) throws IOException {
            return build().get(mContext, imagePath);
        }

        public List<File> get(List<String> imagePaths) throws IOException {
            return build().get(mContext, imagePaths);
        }

        public void launch(OnCompressListener onCompressListener) {
            build().launch(mContext, onCompressListener);
        }

        private BitmapCompressor build() {
            return new BitmapCompressor(this);
        }
    }
}
