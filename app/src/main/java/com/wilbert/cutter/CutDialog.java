package com.wilbert.cutter;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;

/**
 * Created by Android Studio.
 * User: wilbert jw20082009@qq.com
 * Date: 2019/8/31 14:44
 */
public class CutDialog extends Dialog {
    public CutDialog(Context context) {
        super(context, R.style.basic_dialog);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }
}
