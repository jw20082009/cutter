package com.wilbert.cutter.merge;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.media.MediaMetadataRetriever;
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
import com.wilbert.library.basic.entity.VideoHolder;
import com.wilbert.library.basic.utils.FileUtils;
import com.wilbert.library.basic.utils.OutFileGenerator;
import com.wilbert.library.basic.widgets.LoadingDialog;
import com.wilbert.library.videomerge.VideoEncode;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class MergeActivity extends BaseThreadHandlerActivity implements View.OnClickListener {

    private final String TAG = "BgmActivity";
    public static final int REQUEST_CODE_VIDEO_1 = 2;
    public static final int REQUEST_CODE_VIDEO_2 = 3;
    public static final int PERMISSION_REQUEST_VIDEO1 = 2;
    private String videoFile2;
    private String videoFile1;
    private TextView tvVideo2, tvVideo1, btnConfirm, btnVideo2, btnVideo1;
    private LoadingDialog mLoadingDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_merge);
        tvVideo1 = findViewById(R.id.tv_videofile1);
        tvVideo2 = findViewById(R.id.tv_videofile2);
        btnVideo2 = findViewById(R.id.tv_videofile2_select);
        btnVideo1 = findViewById(R.id.tv_videofile1_select);
        btnConfirm = findViewById(R.id.tv_confirm);
        btnVideo2.setOnClickListener(this);
        btnVideo1.setOnClickListener(this);
        btnConfirm.setOnClickListener(this);
    }

    public void selectVideo(int requestCode) {
        if (Build.VERSION.SDK_INT >= 23) {
            if (checkSelfPermission(
                    Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                if (shouldShowRequestPermissionRationale(
                        Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                }
                requestPermissions(new String[]{
                        Manifest.permission.WRITE_EXTERNAL_STORAGE
                }, requestCode);
            } else {
                selectVideoFile(requestCode);
            }
        } else {
            selectVideoFile(requestCode);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            selectVideoFile(requestCode);
        } else {
            Toast.makeText(this, "存储卡读写权限被拒绝", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE_VIDEO_2) {
            if (resultCode == RESULT_OK) {
                Uri uri = data.getData();
                if (uri != null) {
                    String path = FileUtils.getUriPath(this, uri);
                    if (path != null) {
                        File file = new File(path);
                        if (file.exists()) {
                            Log.i(TAG, path);
                            videoFile2 = path;
                            Message msg = obtainUIMessage(MSG_UI_VIDEO2_SELECTED);
                            msg.obj = videoFile2;
                            msg.sendToTarget();
                        }
                    }
                }
            }
        } else if (requestCode == REQUEST_CODE_VIDEO_1) {
            if (resultCode == RESULT_OK) {
                Uri uri = data.getData();
                if (uri != null) {
                    String path = FileUtils.getUriPath(this, uri);
                    if (path != null) {
                        File file = new File(path);
                        if (file.exists()) {
                            Log.i(TAG, path);
                            videoFile1 = path;
                            Message msg = obtainUIMessage(MSG_UI_VIDEO1_SELECTED);
                            msg.obj = videoFile1;
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

    private final int MSG_UI_VIDEO2_SELECTED = 0x01;
    private final int MSG_UI_VIDEO1_SELECTED = 0x02;
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
            case MSG_UI_VIDEO2_SELECTED:
                videoFile2 = (String) message.obj;
                tvVideo2.setText(videoFile2);
                break;
            case MSG_UI_VIDEO1_SELECTED:
                videoFile1 = (String) message.obj;
                tvVideo1.setText(videoFile1);
                break;
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.tv_videofile1_select:
                selectVideo(REQUEST_CODE_VIDEO_1);
                break;
            case R.id.tv_videofile2_select:
                selectVideo(REQUEST_CODE_VIDEO_2);
                break;
            case R.id.tv_confirm:
                if (TextUtils.isEmpty(videoFile2) || TextUtils.isEmpty(videoFile1)) {
                    Toast.makeText(this, "请先选择文件", Toast.LENGTH_SHORT).show();
                    return;
                }

                List<VideoHolder> list = new ArrayList<>();
                List<String> pathList = new ArrayList<>();
                pathList.add(videoFile1);
                pathList.add(videoFile2);
                showLoadingDialog();
                for (int i = 0; i < pathList.size(); i++) {
                    String videoPath = pathList.get(i);
                    if (videoPath != null) {
                        VideoHolder video = new VideoHolder();
                        video.setVideoFile(videoPath);
                        video.setFrameTime((long) (1.0f * 1000 * 1000 / 25));
                        video.setStartTime(0);
                        video.setCropLeft(0);
                        video.setCropTop(0);
                        MediaMetadataRetriever retriever = new MediaMetadataRetriever();
                        retriever.setDataSource(videoPath);
                        video.setEndTime(Long.parseLong(retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)) * 1000);
                        retriever.release();
                        list.add(video);
                    }
                }
                final String path = OutFileGenerator.generateMergeFile(MergeActivity.this, pathList);
                VideoEncode encode = new VideoEncode(MergeActivity.this, list, new VideoEncode.OnVideoEncodeListener() {
                    @Override
                    public void onSynAudio(final int index, final int progress) {
                        showLoadingProgress("synAudio" + index + ":" + progress + "%");
                    }

                    @Override
                    public void onDecoder(final int progress) {
                        showLoadingProgress("combine:" + progress + "%");
                        Log.i(TAG, "onDecoder,正在合并视频:" + progress);
                    }

                    @Override
                    public void onOver() {
                        dismissLoadingDialog();
                        postUI(new Runnable() {
                            @Override
                            public void run() {
                                Intent intent = SimpleVideoActivity.createIntent(MergeActivity.this, path);
                                startActivity(intent);
                            }
                        });
                        Log.i(TAG, "onOver");

                    }
                });
                //生成的视频文件

                File f = new File(path);
                if (f.exists()) {
                    f.delete();
                }
                encode.start(path);

                break;
        }
    }

    protected void showLoadingDialog() {
        if (mLoadingDialog == null) {
            mLoadingDialog = new LoadingDialog(MergeActivity.this);
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
