package com.wilbert.library.basic.aftereffect;

import android.support.v4.app.Fragment;

import com.wilbert.library.basic.entity.AeEntity;

public interface IView {
    Fragment getAeFragment(AeEntity entity);
    void detroyView();
}
