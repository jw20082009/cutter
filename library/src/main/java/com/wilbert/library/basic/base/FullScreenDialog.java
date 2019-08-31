
package com.wilbert.library.basic.base;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.DisplayMetrics;
import android.view.WindowManager;

import com.wilbert.library.R;

public class FullScreenDialog extends Dialog {
    public FullScreenDialog(@NonNull Context context) {
        this(context, R.style.basic_fullscreen_dialog);
    }

    public FullScreenDialog(@NonNull Context context, int themeResId) {
        super(context, themeResId);
    }

    protected FullScreenDialog(@NonNull Context context, boolean cancelable,
            @Nullable OnCancelListener cancelListener) {
        super(context, cancelable, cancelListener);
    }

    @Override
    public void show() {
        super.show();
        WindowManager.LayoutParams layoutParams = getWindow().getAttributes();
        DisplayMetrics metrics = getContext().getResources().getDisplayMetrics();
        layoutParams.width = metrics.widthPixels;
        layoutParams.height = metrics.heightPixels;
        getWindow().getDecorView().setPadding(0, 0, 0, 0);
        getWindow().setAttributes(layoutParams);
    }
}
