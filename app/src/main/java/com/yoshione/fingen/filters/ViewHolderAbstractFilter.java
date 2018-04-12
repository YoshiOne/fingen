/*
 * Copyright (c) 2015.
 */

package com.yoshione.fingen.filters;

import android.support.v7.widget.RecyclerView;
import android.view.View;

/**
 * Created by slv on 09.11.2015.
 *
 */
public abstract class ViewHolderAbstractFilter extends RecyclerView.ViewHolder {

    public ViewHolderAbstractFilter(View itemView) {
        super(itemView);
    }

    public abstract AbstractFilter getFilter();

    public abstract void setFilter(AbstractFilter filter);

    public abstract void bindViewHolder();
}
