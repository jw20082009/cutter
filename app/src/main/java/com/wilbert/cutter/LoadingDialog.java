
package com.wilbert.cutter;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.widget.ProgressBar;
import android.widget.TextView;

public class LoadingDialog extends Dialog {

    ProgressBar progressBar;
    TextView progressTv;
    public LoadingDialog(@NonNull Context context) {
        super(context,R.style.basic_dialog);
    }

    public LoadingDialog(@NonNull Context context, int themeResId) {
        super(context, themeResId);
    }

    protected LoadingDialog(@NonNull Context context, boolean cancelable,
                            @Nullable OnCancelListener cancelListener) {
        super(context, cancelable, cancelListener);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.sample_dialog_loading);
        progressBar = findViewById(R.id.basic_progressbar);
        progressTv = findViewById(R.id.basic_tv_progress);
    }

    public void setProgress(final int progress){
        if(progressTv != null){
            progressTv.post(new Runnable() {
                @Override
                public void run() {
                    progressTv.setText("" + progress);
                }
            });
        }
    }

    public void setProgress(final String progress){
        if(progressTv != null){
            progressTv.post(new Runnable() {
                @Override
                public void run() {
                    progressTv.setText("" + progress);
                }
            });
        }
    }
}
