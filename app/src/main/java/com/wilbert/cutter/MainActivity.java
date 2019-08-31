package com.wilbert.cutter;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.Button;
import android.widget.Toast;

import com.wilbert.cutter.bgm.BgmActivity;
import com.wilbert.cutter.cut.VideoEditActivity;
import com.wilbert.cutter.merge.MergeActivity;
import com.wilbert.library.basic.utils.FileUtils;

import java.io.File;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {
    private final String TAG = "MainActivity";
    public static final int REQUEST_CODE_VIDEO_CROP = 1;
    public static final int REQUEST_CODE_VIDEO_COMPRESS = 2;

    public static final int PERMISSION_REQUEST_VIDEO_CROP = 1;
    public static final int PERMISSION_REQUEST_VIDEO_COMPRESS = 2;

    Button btnCompress, btnCut, btnMusic, btnMerge;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        btnCompress = findViewById(R.id.btn_compress);
        btnCut = findViewById(R.id.btn_cut);
        btnMusic = findViewById(R.id.btn_music);
        btnMerge = findViewById(R.id.btn_merge);
        btnCompress.setOnClickListener(this);
        btnCut.setOnClickListener(this);
        btnMusic.setOnClickListener(this);
        btnMerge.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_compress:
                startCompress();
                break;
            case R.id.btn_cut:
                startCrop();
                break;
            case R.id.btn_music:
                startBgm();
                break;
            case R.id.btn_merge:
                startMerge();
                break;
        }
    }

    public void startMerge() {
        startActivity(new Intent(this, MergeActivity.class));
    }

    public void startBgm() {
        startActivity(new Intent(this, BgmActivity.class));
    }

    public void startCompress() {
        if (Build.VERSION.SDK_INT >= 23) {
            if (checkSelfPermission(
                    Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                if (shouldShowRequestPermissionRationale(
                        Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                }
                requestPermissions(new String[]{
                        Manifest.permission.WRITE_EXTERNAL_STORAGE
                }, PERMISSION_REQUEST_VIDEO_COMPRESS);
            } else {
                selectVideoFile(REQUEST_CODE_VIDEO_COMPRESS);
            }
        } else {
            selectVideoFile(REQUEST_CODE_VIDEO_COMPRESS);
        }
    }

    public void startCrop() {
        if (Build.VERSION.SDK_INT >= 23) {
            if (checkSelfPermission(
                    Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                if (shouldShowRequestPermissionRationale(
                        Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                }
                requestPermissions(new String[]{
                        Manifest.permission.WRITE_EXTERNAL_STORAGE
                }, PERMISSION_REQUEST_VIDEO_CROP);
            } else {
                selectVideoFile(PERMISSION_REQUEST_VIDEO_CROP);
            }
        } else {
            selectVideoFile(PERMISSION_REQUEST_VIDEO_CROP);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSION_REQUEST_VIDEO_CROP) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                selectVideoFile(PERMISSION_REQUEST_VIDEO_CROP);
            } else {
                Toast.makeText(this, "存储卡读写权限被拒绝", Toast.LENGTH_SHORT).show();
            }
        } else if (requestCode == PERMISSION_REQUEST_VIDEO_COMPRESS) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                selectVideoFile(REQUEST_CODE_VIDEO_COMPRESS);
            } else {
                Toast.makeText(this, "存储卡读写权限被拒绝", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void selectVideoFile(int code) {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("video/*");// 设置类型，我这里是任意类型，任意后缀的可以这样写。
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        startActivityForResult(intent, code);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE_VIDEO_CROP) {
            if (resultCode == RESULT_OK) {
                Uri uri = data.getData();
                if (uri != null) {
                    String path = FileUtils.getUriPath(this, uri);
                    if (path != null) {
                        File file = new File(path);
                        if (file.exists()) {
                            Log.i(TAG, path);
                            startActivity(VideoEditActivity.createIntent(this, path, 0));
                        }
                    }
                }
            }
        } else if (requestCode == REQUEST_CODE_VIDEO_COMPRESS) {
            if (resultCode == RESULT_OK) {
                Uri uri = data.getData();
                if (uri != null) {
                    String path = FileUtils.getUriPath(this, uri);
                    if (path != null) {
                        File file = new File(path);
                        if (file.exists()) {
                            Log.i(TAG, path);
                            startActivity(VideoEditActivity.createIntent(this, path, 1));
                        }
                    }
                }
            }
        }
    }
}
