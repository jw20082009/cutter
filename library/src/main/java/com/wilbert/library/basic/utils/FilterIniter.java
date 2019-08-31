package com.wilbert.library.basic.utils;

import android.content.Context;

import java.io.File;

public class FilterIniter {

    private static String filterResource;

    public static void init(final Context context, final FileListener listener) {
        filterResource = context.getExternalFilesDir(null).getAbsolutePath() + "/resources";
        if (new File(filterResource).exists()) {
            if (listener != null)
                listener.onSuccess(filterResource);
        } else {
//            new Thread(new Runnable() {
//                @Override
//                public void run() {
//                    FileUtils.CopyAssets(context, "resources", filterResource);
//                    if (listener != null) {
//                        listener.onSuccess(filterResource);
//                    }
//                }
//            }).start();
        }
    }

    public static String getFilterResource() {
        return filterResource;
    }

    public interface FileListener {
        void onSuccess(String filterResource);
    }
}
