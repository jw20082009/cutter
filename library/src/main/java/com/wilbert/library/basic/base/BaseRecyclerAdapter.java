
package com.wilbert.library.basic.base;

import android.content.Context;
import android.graphics.Color;
import android.support.v7.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

/**
 * 描述 通用RecyclerView适配器基类
 *
 * @author walljiang
 * @since 2017/3/8 11:26
 */
public abstract class BaseRecyclerAdapter<T, V extends RecyclerView.ViewHolder>
        extends RecyclerView.Adapter<V> {

    protected Context mContext;

    protected List<T> mDatas = new ArrayList<T>();

    public BaseRecyclerAdapter(Context context) {
        this.mContext = context;
    }

    public Context getContext() {
        return mContext;
    }

    public T getItem(int position) {
        return mDatas == null ? null : mDatas.get(position);
    }

    /**
     * 获取所有数据
     *
     * @return
     */
    public List<T> getDatas() {
        return mDatas;
    }

    /**
     * 添加新数据到头部
     *
     * @param datas
     */
    public void addDatasAtFront(ArrayList<T> datas) {
        if (mDatas != null && datas != null && datas.size() > 0) {
            ArrayList<T> temp = new ArrayList<T>();
            temp.addAll(datas);
            temp.addAll(mDatas);
            mDatas.clear();
            mDatas.addAll(temp);
        }
    }

    /**
     * 添加新数据到末尾
     *
     * @param data
     */
    public void add(T data) {
        if (data != null) {
            checkNullDatas();
            mDatas.add(data);
            notifyDataSetChanged();
        }
    }

    /**
     * 添加数据到指定位置
     *
     * @param position
     * @param data
     */
    public void add(int position, T data) {
        if (data != null) {
            checkNullDatas();
            mDatas.add(position, data);
            notifyDataSetChanged();
        }
    }

    /**
     * 添加新数据到末尾
     *
     * @param datas
     */
    public void add(List<T> datas) {
        if (datas != null) {
            checkNullDatas();
            mDatas.addAll(datas);
            notifyDataSetChanged();
        }
    }

    /**
     * 添加数据到指定位置
     *
     * @param position
     * @param data
     */
    public void add(int position, List<T> data) {
        if (data != null) {
            checkNullDatas();
            mDatas.addAll(position, data);
            notifyDataSetChanged();
        }
    }

    /**
     * 清空列表， 放入新数据
     *
     * @param data
     */
    public void set(T data) {
        checkNullDatas();
        mDatas.clear();
        if (data != null) {
            mDatas.add(data);
        }
        notifyDataSetChanged();
    }

    /**
     * 清空列表， 放入新数据
     *
     * @param datas
     */
    public void set(List<T> datas) {
        if (datas != null) {
            mDatas = datas;
        }
        notifyDataSetChanged();
    }

    public void setData(List<T> datas) {
        mDatas.clear();
        if(datas != null && !datas.isEmpty()){
            mDatas.addAll(datas);
        }
    }

    /**
     * 删除指定的位置数据
     *
     * @param position
     * @return
     */
    public T del(int position) {
        checkNullDatas();
        if (position < mDatas.size()) {
            T t = mDatas.remove(position);
            notifyDataSetChanged();
            return t;
        }
        return null;
    }

    /**
     * 删除指定数据
     *
     * @param t
     */
    public void delete(T t) {
        checkNullDatas();
        if (mDatas.contains(t)) {
            mDatas.remove(t);
            notifyDataSetChanged();
        }
    }

    public void cleanData() {
        checkNullDatas();
        mDatas.clear();
    }

    protected void checkNullDatas() {
        if (mDatas == null) {
            mDatas = new ArrayList<T>();
        }
    }

    @Override
    public int getItemCount() {
        return mDatas == null ? 0 : mDatas.size();
    }

    protected String getString(int stringId) {
        return mContext != null ? mContext.getString(stringId) : null;
    }

    protected int getColor(int colorId) {
        return mContext != null ? mContext.getResources().getColor(colorId) : Color.TRANSPARENT;
    }

    protected float getDimension(int dimenId) {
        return mContext != null ? mContext.getResources().getDimension(dimenId) : 0;
    }
}
