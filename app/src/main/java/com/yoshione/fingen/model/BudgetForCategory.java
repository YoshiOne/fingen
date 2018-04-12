package com.yoshione.fingen.model;

import com.yoshione.fingen.classes.ListSumsByCabbage;

/**
 * Created by slv on 07.03.2016.
 *
 */
public class BudgetForCategory {
    private ListSumsByCabbage mSums;
    private final int mType;

    public BudgetForCategory(ListSumsByCabbage mSums, int mType) {
        this.mSums = mSums;
        this.mType = mType;
    }

    public int getmType() {
        return mType;
    }

    public ListSumsByCabbage getmSums() {
        return mSums;
    }

    public void setmSums(ListSumsByCabbage mSums) {
        this.mSums = mSums;
    }
}
