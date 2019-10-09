package com.yoshione.fingen.model;

public class TabItem {
    String mID;
    String mName;
    boolean mVisible;
    boolean mLockVisible;
    boolean mLockHide;

    public TabItem(String ID, String name, boolean visible, boolean lockVisible, boolean lockHide) {
        mID = ID;
        mName = name;
        mVisible = visible;
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

    public void setVisible(boolean visible) {
        mVisible = visible;
    }

    public boolean isLockVisible() {
        return mLockVisible;
    }

    public boolean isLockHide() {
        return mLockHide;
    }
}
