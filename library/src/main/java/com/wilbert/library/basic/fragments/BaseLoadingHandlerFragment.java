
package com.wilbert.library.basic.fragments;

import com.wilbert.library.basic.widgets.LoadingDialog;

public class BaseLoadingHandlerFragment extends BaseThreadHandlerFragment {

    protected LoadingDialog mLoadingDialog;

    protected void showLoadingDialog() {
        if (mLoadingDialog == null && getContext() != null) {
            mLoadingDialog = new LoadingDialog(getContext());
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
