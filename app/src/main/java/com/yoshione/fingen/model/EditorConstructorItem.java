package com.yoshione.fingen.model;

/**
 * Created by slv on 27.02.2018.
 *
 */

public class EditorConstructorItem {
    String mName;
    int mID;
    int mOrder;
    boolean mVisible;
    boolean mAlwaysVisible;
    boolean mEnabled;
    boolean mAlwaysEnabled;

    public EditorConstructorItem(String name, int ID, int order, boolean visible, boolean alwaysVisible, boolean enabled, boolean alwaysEnabled) {
        mName = name;
        mID = ID;
        mOrder = order;
        mVisible = visible;
        mAlwaysVisible = alwaysVisible;
        mEnabled = enabled;
        mAlwaysEnabled = alwaysEnabled;
    }

    public String getName() {
        return mName;
    }

    public void setName(String name) {
        mName = name;
    }

    public int getID() {
        return mID;
    }

    public void setID(int ID) {
        mID = ID;
    }

    public int getOrder() {
        return mOrder;
    }

    public void setOrder(int order) {
        mOrder = order;
    }

    public boolean isVisible() {
        return mVisible;
    }

    public void setVisible(boolean visible) {
        mVisible = visible;
    }

    public boolean isAlwaysVisible() {
        return mAlwaysVisible;
    }

    public void setAlwaysVisible(boolean alwaysVisible) {
        mAlwaysVisible = alwaysVisible;
    }

    public boolean isEnabled() {
        return mEnabled;
    }

    public void setEnabled(boolean enabled) {
        mEnabled = enabled;
    }

    public boolean isAlwaysEnabled() {
        return mAlwaysEnabled;
    }

    public void setAlwaysEnabled(boolean alwaysEnabled) {
        mAlwaysEnabled = alwaysEnabled;
    }
}
