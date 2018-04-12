package com.yoshione.fingen.model;

/**
 * Created by slv on 26.10.2016.
 * a
 */
public class StringIntItem {
    private int mID;
    private String mName;

    public StringIntItem(String name, int ID) {
        mID = ID;
        mName = name;
    }

    public int getID() {
        return mID;
    }

    public String getName() {
        return mName;
    }

    @Override
    public String toString() {
        return mName;
    }
}
