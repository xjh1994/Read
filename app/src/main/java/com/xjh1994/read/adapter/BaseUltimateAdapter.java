package com.xjh1994.read.adapter;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.marshalchen.ultimaterecyclerview.UltimateRecyclerviewViewHolder;
import com.marshalchen.ultimaterecyclerview.UltimateViewAdapter;
import com.xjh1994.read.R;

import java.util.List;

/**
 * Created by XJH on 16/3/24.
 */
public abstract class BaseUltimateAdapter<T> extends UltimateViewAdapter implements IAdapter<T> {

    private Context context;
    private List<T> dataList;
    private Object mType;

    public BaseUltimateAdapter(Context context, List<T> dataList) {
        this.context = context;
        this.dataList = dataList;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        if (position < getItemCount() && (customHeaderView != null ? position <= dataList.size() : position < dataList.size()) && (customHeaderView != null ? position > 0 : true)) {
            ((BaseViewHolder) holder).item.handleData(context, getConvertedData(dataList.get(position), mType), position);
        }
    }

    @Override
    public BaseViewHolder getViewHolder(View view) {
        return new BaseViewHolder(view, false);
    }

    @Override
    public BaseViewHolder onCreateViewHolder(ViewGroup parent) {
        IAdapterItem item = createItem(mType);
        View v = LayoutInflater.from(parent.getContext()).inflate(item.getLayoutResId(), parent, false);
        BaseViewHolder vh = new BaseViewHolder(v, item, true);
        return vh;
    }

    @Override
    public int getAdapterItemCount() {
        return dataList.size();
    }

    @Override
    public long generateHeaderId(int position) {
        if (getItem(position).toString().length() > 0)
            return getItem(position).toString().charAt(0);
        else return -1;
    }


    @Override
    public RecyclerView.ViewHolder onCreateHeaderViewHolder(ViewGroup parent) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.stick_header_item, parent, false);
        return new RecyclerView.ViewHolder(view) {
        };
    }

    @Override
    public void onBindHeaderViewHolder(RecyclerView.ViewHolder holder, int position) {

    }

    @Override
    public void setData(@NonNull List<T> data) {
        this.dataList = data;
    }

    @Override
    public List<T> getData() {
        return dataList;
    }

    public void insert(List list, int position) {
        insertInternal(list, dataList);
    }

    public void remove(int position) {
        removeInternal(dataList, position);
    }

    public void clear() {
        clearInternal(dataList);
    }

    @Override
    public void toggleSelection(int pos) {
        super.toggleSelection(pos);
    }

    @Override
    public void setSelected(int pos) {
        super.setSelected(pos);
    }

    @Override
    public void clearSelection(int pos) {
        super.clearSelection(pos);
    }

    public void swapPositions(int from, int to) {
        swapPositions(dataList, from, to);
    }

    @Override
    public Object getItemType(T t) {
        return -1;
    }

    @NonNull
    @Override
    public Object getConvertedData(T data, Object type) {
        return data;
    }

    public static class BaseViewHolder extends UltimateRecyclerviewViewHolder {

        protected IAdapterItem item;

        protected BaseViewHolder(View view, boolean isItem) {
            super(view);
        }

        protected BaseViewHolder(View itemView, IAdapterItem item, boolean isItem) {
            super(itemView);
            this.item = item;

            if (isItem) {
                this.item.bindViews(itemView);
            }
        }


    }

    public T getItem(int position) {
        if (customHeaderView != null)
            position--;
        if (position < dataList.size())
            return dataList.get(position);
        else return null;
    }
}
