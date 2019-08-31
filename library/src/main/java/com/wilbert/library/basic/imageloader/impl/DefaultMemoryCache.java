package com.wilbert.library.basic.imageloader.impl;

import android.graphics.Bitmap;


import com.wilbert.library.basic.imageloader.inter.MemoryCache;

import java.lang.ref.SoftReference;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;



public class DefaultMemoryCache implements MemoryCache {

    /**
     * 内存缓存
     */
    private final Map<String,SoftReference<Bitmap>> cache = Collections.synchronizedMap(new HashMap<String, SoftReference<Bitmap>>());

    @Override
    public Bitmap get(String key) {
        SoftReference<Bitmap> ref = cache.get(key);
        if (ref != null){
            return ref.get();
        }
        return null;
    }

    @Override
    public void put(String key, Bitmap bitmap) {
        if (key == null || bitmap == null)return;
        cache.put(key,new SoftReference<>(bitmap));
    }

    @Override
    public void clear() {
        cache.clear();
    }
}
