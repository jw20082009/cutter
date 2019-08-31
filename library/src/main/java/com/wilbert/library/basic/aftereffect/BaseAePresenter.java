package com.wilbert.library.basic.aftereffect;

import android.os.Bundle;
import android.support.annotation.IdRes;
import android.support.v4.app.FragmentManager;

import com.wilbert.library.basic.entity.AeEntity;

public abstract class BaseAePresenter<V extends IView, M extends IModel> {

    protected V view;
    protected M model;

    public BaseAePresenter(V view, M model) {
        this.view = view;
        this.model = model;
    }

    public abstract void showView(FragmentManager fragmentManager, @IdRes int layoutId, AeEntity aeEntity);

    public abstract void destroyView(FragmentManager fragmentManager);

    public static Bundle createAfterEffectArgs(String filePath) {
        Bundle bundle = new Bundle();
        bundle.putString("uri", filePath);
        return bundle;
    }

    public static String parseAfterEffectInputFile(Bundle bundle) {
        if (bundle != null) {
            return bundle.getString("uri");
        }
        return null;
    }

    public static Bundle createArgs(AeEntity aeEntity) {
        Bundle bundle = new Bundle();
        bundle.putParcelable("aeEntity", aeEntity);
        return bundle;
    }

    public static AeEntity parseAeEntity(Bundle bundle) {
        if (bundle != null) {
            return bundle.getParcelable("aeEntity");
        }
        return null;
    }
}
