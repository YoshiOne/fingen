package com.yoshione.fingen.model;

import com.yoshione.fingen.classes.ListSumsByCabbage;

/**
 * Created by slv on 07.11.2016.
 */

public class SummaryItem {
    private long mID;
    private int mIntervalFirst;
    private int mIntervalSecond;
    private ListSumsByCabbage mListSumsByCabbage;
    private String Name;

    public SummaryItem(long ID, int intervalFirst, int intervalSecond, ListSumsByCabbage listSumsByCabbage, String name) {
        mID = ID;
        mIntervalFirst = intervalFirst;
        mIntervalSecond = intervalSecond;
        mListSumsByCabbage = listSumsByCabbage;
        Name = name;
    }

    public ListSumsByCabbage getListSumsByCabbage() {
        return mListSumsByCabbage;
    }

    public int getIntervalFirst() {
        return mIntervalFirst;
    }

    public int getIntervalSecond() {
        return mIntervalSecond;
    }

    public long getID() {
        return mID;
    }

    public String getName() {
        return Name;
    }
}
