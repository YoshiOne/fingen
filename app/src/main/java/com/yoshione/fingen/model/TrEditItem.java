package com.yoshione.fingen.model;

public class TrEditItem {
    String mID;
    String mName;
    boolean mVisible;
    boolean mHideUnderMore;
    boolean mLockVisible;
    boolean mLockHide;

    public TrEditItem(String ID, String name, boolean visible, boolean hideUnderMore, boolean lockVisible, boolean lockHide) {
        mID = ID;
        mName = name;
        mVisible = visible;
        mHideUnderMore = hideUnderMore;
        mLockVisible = lockVisible;
        mLockHide = lockHide;
    }

    public String getID() {
        return mID;
    }

    public String getName() {
        return mName;
    }

    public boolean isVisible() {
        return mVisible;
    }

    public boolean isHideUnderMore() {
        return mHideUnderMore;
    }

    public void setVisible(boolean visible) {
        mVisible = visible;
    }

    public void setHideUnderMore(boolean hideUnderMore) {
        mHideUnderMore = hideUnderMore;
    }

    public boolean isLockVisible() {
        return mLockVisible;
    }

    public boolean isLockHide() {
        return mLockHide;
    }
}
