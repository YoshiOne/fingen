package com.yoshione.fingen.model;

import android.graphics.drawable.Drawable;
import android.view.View;

import com.anjlab.android.iab.v3.SkuDetails;

/**
 * Created by Leonid on 19.07.2016.
 *
 */

public class SkuDetailsItem {
    private final SkuDetails mSkuDetails;
    private final Drawable mIcon;
    private final long mId;
    private final boolean mIsPurchased;
    private final View.OnClickListener mOnClickListener;

    public SkuDetailsItem(SkuDetails skuDetails, Drawable icon, long id, boolean isPurchased, View.OnClickListener onClickListener) {
        mSkuDetails = skuDetails;
        mIcon = icon;
        mId = id;
        mIsPurchased = isPurchased;
        mOnClickListener = onClickListener;
    }

    public boolean isPurchased() {
        return mIsPurchased;
    }

    public long getId() {
        return mId;
    }

    public SkuDetails getSkuDetails() {
        return mSkuDetails;
    }

    public Drawable getIcon() {
        return mIcon;
    }

    public View.OnClickListener getOnClickListener() {
        return mOnClickListener;
    }
}
