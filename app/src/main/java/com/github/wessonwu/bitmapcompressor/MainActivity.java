package com.github.wessonwu.bitmapcompressor;

import android.Manifest;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.PermissionChecker;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.SimpleTarget;
import com.bumptech.glide.request.transition.Transition;

import java.io.File;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    private final static int REQ_CODE_PERMISSIONS = 0x10;

    private final static int REQ_CODE_PICK_IMAGE = 0x11;

    ImageView mIvSource;
    ImageView mIvResult;
    TextView mTvSourceSize;
    TextView mTvResultSize;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mIvSource = findViewById(R.id.iv_source);
        mIvResult = findViewById(R.id.iv_result);
        mTvSourceSize = findViewById(R.id.tv_source_size);
        mTvResultSize = findViewById(R.id.tv_result_size);

        if (!checkPermission(Manifest.permission.READ_EXTERNAL_STORAGE)
                || !checkPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
            ActivityCompat.requestPermissions(this, new String[] {Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE}, REQ_CODE_PERMISSIONS);
        }
    }

    private boolean checkPermission(String permission) {
        return PermissionChecker.checkSelfPermission(this, permission) == PermissionChecker.PERMISSION_GRANTED;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == REQ_CODE_PERMISSIONS) {
            if (checkPermission(Manifest.permission.READ_EXTERNAL_STORAGE)
                    && checkPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE)) {

            }
        }
    }

    public void onPick(View view) {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("image/*");
        startActivityForResult(intent, REQ_CODE_PICK_IMAGE);
    }

    public void compress(View view) {
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQ_CODE_PICK_IMAGE && resultCode == RESULT_OK) {
            Uri uri = data.getData();
            String path = RealPathUtil.getRealPath(this, uri);
            File image = new File( path);
            if (image.exists()) {
                Glide.with(this)
                        .asFile()
                        .load(image)
                        .into(new SimpleTarget<File>() {
                            @Override
                            public void onResourceReady(@NonNull File resource, @Nullable Transition<? super File> transition) {
                                onSourceFileChanged(resource);
                            }
                        });
            }
        }
    }

    private void onSourceFileChanged(File source) {
        Glide.with(this)
                .asBitmap()
                .load(source)
                .into(mIvSource);
        mTvSourceSize.setText(getSize(source.length()));
        BitmapCompressor.with(this)
                .load(source.getAbsolutePath())
                .setCustomCalculator(new LubanCalculator())
                .launch(new OnCompressListener() {
                    @Override
                    public void onStart() {

                    }

                    @Override
                    public void onCompleted(final List<File> compressedFiles) {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                if (compressedFiles != null && !compressedFiles.isEmpty()) {
                                    Glide.with(MainActivity.this)
                                            .asBitmap()
                                            .load(compressedFiles.get(0))
                                            .into(mIvResult);
                                    mTvResultSize.setText(getSize(compressedFiles.get(0).length()));
                                }
                            }
                        });
                    }

                    @Override
                    public void onError(Throwable e) {

                    }
                });
    }

    private String getSize(long length) {
        if (length < KB) {
            return length + "";
        }
        if (length < MB) {
            return String.format("%.1fKB", (float) length / KB);
        }

        return String.format("%.1fMB", (float) length / MB);
    }

    private final static long KB = 1024;
    private final static long MB = 1024 * 1024;
}
