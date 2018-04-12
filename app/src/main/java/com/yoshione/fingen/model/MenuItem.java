package com.yoshione.fingen.model;

import android.graphics.drawable.Drawable;
import android.view.View;

/**
 * Created by slv on 21.01.2016.
 *
 */
public class MenuItem {
    private final Drawable mIcon;
    private final String mTitle;
    private final View.OnClickListener mOnClickListener;

    public int getId() {
        return id;
    }

    private final int id;

    public MenuItem(Drawable mIcon, String mTitle, View.OnClickListener mOnClickListener, int id) {
        this.mIcon = mIcon;
        this.mTitle = mTitle;
        this.mOnClickListener = mOnClickListener;
        this.id = id;
    }

    public Drawable getmIcon() {
        return mIcon;
    }

    public String getmTitle() {
        return mTitle;
    }

    public View.OnClickListener getmOnClickListener() {
        return mOnClickListener;
    }

    public String toString() {
        return mTitle;
    }
}
