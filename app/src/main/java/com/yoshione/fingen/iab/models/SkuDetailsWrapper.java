package com.yoshione.fingen.iab.models;

import com.anjlab.android.iab.v3.SkuDetails;

public class SkuDetailsWrapper {

    private SkuDetails mSkuDetails;
    private boolean mIsPurchased;

    public SkuDetailsWrapper() {
        mSkuDetails = null;
        mIsPurchased = false;
    }

    public SkuDetails getSkuDetails() {
        return mSkuDetails;
    }

    public void setSkuDetails(SkuDetails skuDetails) {
        mSkuDetails = skuDetails;
    }

    public boolean isPurchased() {
        return mIsPurchased;
    }

    public void setPurchased(boolean purchased) {
        mIsPurchased = true;
    }
}
