package com.xjh1994.read.adapter;

import android.content.Context;
import android.support.annotation.LayoutRes;
import android.view.View;

/**
 * Created by XJH on 16/3/10.
 */
public interface IAdapterItem<T> {
    @LayoutRes
    int getLayoutResId();

    void bindViews(View root);

    void setViews();

    void handleData(Context context, T data, int type);
}
