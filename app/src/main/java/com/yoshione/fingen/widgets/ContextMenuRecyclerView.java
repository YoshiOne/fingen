/*
 * Copyright (c) 2015.
 */

package com.yoshione.fingen.widgets;

import android.content.Context;
import android.util.AttributeSet;
import android.view.ContextMenu;
import android.view.View;

import com.l4digital.fastscroll.FastScrollRecyclerView;

/**
 * Created by slv on 25.11.2015.
 *
 */
public class ContextMenuRecyclerView extends FastScrollRecyclerView {

    private RecyclerContextMenuInfo mContextMenuInfo;



    public ContextMenuRecyclerView(Context context) {
        super(context);
    }

    public ContextMenuRecyclerView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public ContextMenuRecyclerView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    @Override
    protected ContextMenu.ContextMenuInfo getContextMenuInfo() {
        return mContextMenuInfo;
    }

    @Override
    public boolean showContextMenuForChild(View originalView) {
        try {
            final int longPressPosition = getChildAdapterPosition(originalView);
            if (longPressPosition >= 0) {
                final long longPressId = getAdapter().getItemId(longPressPosition);
                mContextMenuInfo = new RecyclerContextMenuInfo(longPressPosition, longPressId);
                return super.showContextMenuForChild(originalView);
            }
            return false;
        } catch (Exception e) {
            return false;
        }

    }

    public static class RecyclerContextMenuInfo implements ContextMenu.ContextMenuInfo {

        RecyclerContextMenuInfo(int position, long id) {
            this.position = position;
            this.id = id;
        }

        final public int position;
        final public long id;
    }

}
