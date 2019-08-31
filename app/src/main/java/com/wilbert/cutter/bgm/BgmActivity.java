package com.wilbert.cutter.bgm;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Message;
import android.support.annotation.NonNull;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.wilbert.cutter.R;
import com.wilbert.library.basic.activity.BaseThreadHandlerActivity;
import com.wilbert.library.basic.activity.SimpleVideoActivity;
import com.wilbert.library.basic.utils.FileUtils;
import com.wilbert.library.basic.utils.OutFileGenerator;
import com.wilbert.library.basic.widgets.LoadingDialog;
import com.wilbert.library.videoprocessor.VideoProcessor;

import java.io.File;
import java.io.IOException;

public class BgmActivity extends BaseThreadHandlerActivity implements View.OnClickListener {
    private final String TAG = "BgmActivity";
    public static final int REQUEST_CODE_VIDEO_BGM = 2;
    public static final int REQUEST_CODE_MUSIC_BGM = 3;
    public static final int PERMISSION_REQUEST_VIDEO_BGM = 2;
    public static final int PERMISSION_REQUEST_MUSIC_BGM = 3;
    private String musicFile;
    private String videoFile;
    private TextView tvMusic, tvVideo, btnConfirm, btnMusic, btnVideo;
    private LoadingDialog mLoadingDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bgm);
        tvMusic = findViewById(R.id.tv_musicfile);
        tvVideo = findViewById(R.id.tv_videofile);
        btnMusic = findViewById(R.id.tv_musicfile_select);
        btnVideo = findViewById(R.id.tv_videofile_select);
        btnConfirm = findViewById(R.id.tv_confirm);
        btnMusic.setOnClickListener(this);
        btnVideo.setOnClickListener(this);
        btnConfirm.setOnClickListener(this);
    }

    public void selectBgm() {
        if (Build.VERSION.SDK_INT >= 23) {
            if (checkSelfPermission(
                    Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                if (shouldShowRequestPermissionRationale(
                        Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                }
                requestPermissions(new String[]{
                        Manifest.permission.WRITE_EXTERNAL_STORAGE
                }, PERMISSION_REQUEST_MUSIC_BGM);
            } else {
                selectMusicFile(REQUEST_CODE_MUSIC_BGM);
            }
        } else {
            selectMusicFile(REQUEST_CODE_MUSIC_BGM);
        }
    }

    public void selectVideo() {
        if (Build.VERSION.SDK_INT >= 23) {
            if (checkSelfPermission(
                    Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                if (shouldShowRequestPermissionRationale(
                        Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                }
                requestPermissions(new String[]{
                        Manifest.permission.WRITE_EXTERNAL_STORAGE
                }, PERMISSION_REQUEST_VIDEO_BGM);
            } else {
                selectVideoFile(REQUEST_CODE_VIDEO_BGM);
            }
        } else {
            selectVideoFile(REQUEST_CODE_VIDEO_BGM);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSION_REQUEST_MUSIC_BGM) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                selectMusicFile(REQUEST_CODE_MUSIC_BGM);
            } else {
                Toast.makeText(this, "存储卡读写权限被拒绝", Toast.LENGTH_SHORT).show();
            }
        } else if (requestCode == PERMISSION_REQUEST_VIDEO_BGM) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                selectVideoFile(PERMISSION_REQUEST_VIDEO_BGM);
            } else {
                Toast.makeText(this, "存储卡读写权限被拒绝", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE_MUSIC_BGM) {
            if (resultCode == RESULT_OK) {
                Uri uri = data.getData();
                if (uri != null) {
                    String path = FileUtils.getUriPath(this, uri);
                    if (path != null) {
                        File file = new File(path);
                        if (file.exists()) {
                            Log.i(TAG, path);
                            musicFile = path;
                            Message msg = obtainUIMessage(MSG_UI_MUSIC_SELECTED);
                            msg.obj = musicFile;
                            msg.sendToTarget();
                        }
                    }
                }
            }
        } else if (requestCode == REQUEST_CODE_VIDEO_BGM) {
            if (resultCode == RESULT_OK) {
                Uri uri = data.getData();
                if (uri != null) {
                    String path = FileUtils.getUriPath(this, uri);
                    if (path != null) {
                        File file = new File(path);
                        if (file.exists()) {
                            Log.i(TAG, path);
                            videoFile = path;
                            Message msg = obtainUIMessage(MSG_UI_VIDEO_SELECTED);
                            msg.obj = videoFile;
                            msg.sendToTarget();
                        }
                    }
                }
            }
        }
    }

    private void selectVideoFile(int code) {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("video/*");// 设置类型，我这里是任意类型，任意后缀的可以这样写。
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        startActivityForResult(intent, code);
    }

    private void selectMusicFile(int code) {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("audio/mpeg;audio/aac;audio/aac-adts");// 设置类型，我这里是任意类型，任意后缀的可以这样写。
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        startActivityForResult(intent, code);
    }

    private final int MSG_UI_MUSIC_SELECTED = 0x01;
    private final int MSG_UI_VIDEO_SELECTED = 0x02;
    private final int MSG_UI_SUCCESS = 0x03;

    @Override
    protected void handleUIMessage(Message message) {
        super.handleUIMessage(message);
        switch (message.what) {
            case MSG_UI_SUCCESS:
                dismissLoadingDialog();
                String outfile = (String) message.obj;
                startActivity(SimpleVideoActivity.createIntent(this, outfile));
                break;
            case MSG_UI_MUSIC_SELECTED:
                musicFile = (String) message.obj;
                tvMusic.setText(musicFile);
                break;
            case MSG_UI_VIDEO_SELECTED:
                videoFile = (String) message.obj;
                tvVideo.setText(videoFile);
                break;
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.tv_musicfile_select:
                selectBgm();
                break;
            case R.id.tv_videofile_select:
                selectVideo();
                break;
            case R.id.tv_confirm:
                if (TextUtils.isEmpty(musicFile) || TextUtils.isEmpty(videoFile)) {
                    Toast.makeText(this, "请先选择文件", Toast.LENGTH_SHORT).show();
                    return;
                }
                final String outputFile = OutFileGenerator.generateMusicFile(this, videoFile);
                showLoadingDialog();
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            VideoProcessor.mixAudioTrack(BgmActivity.this, videoFile, musicFile, outputFile, null, null, null, 50, 50, 1, 1);
                            Message msg = obtainUIMessage(MSG_UI_SUCCESS);
                            msg.obj = outputFile;
                            msg.sendToTarget();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }).start();
                break;
        }
    }

    protected void showLoadingDialog() {
        if (mLoadingDialog == null) {
            mLoadingDialog = new LoadingDialog(BgmActivity.this);
            mLoadingDialog.setCancelable(false);
            mLoadingDialog.setCanceledOnTouchOutside(false);
        }
        if (mLoadingDialog != null)
            mLoadingDialog.show();
    }

    protected void showLoadingProgress(int progress) {
        if (mLoadingDialog != null && mLoadingDialog.isShowing()) {
            mLoadingDialog.setProgress(progress);
        }
    }

    protected void showLoadingProgress(String progress) {
        if (mLoadingDialog != null && mLoadingDialog.isShowing()) {
            mLoadingDialog.setProgress(progress);
        }
    }

    protected void dismissLoadingDialog() {
        if (mLoadingDialog != null)
            mLoadingDialog.dismiss();
    }
}
