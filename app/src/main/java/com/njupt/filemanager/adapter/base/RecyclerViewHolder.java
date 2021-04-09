package com.njupt.filemanager.adapter.base;

import android.support.v7.widget.RecyclerView;
import android.view.View;

public abstract class RecyclerViewHolder<T> extends RecyclerView.ViewHolder {

   public RecyclerViewHolder(View itemView) {
        super(itemView);
    }

    public abstract void onBindViewHolder ( T t , RecyclerViewAdapter adapter , int position) ;
}
