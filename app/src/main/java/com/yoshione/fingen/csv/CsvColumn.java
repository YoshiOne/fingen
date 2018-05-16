package com.yoshione.fingen.csv;

public class CsvColumn {
    private String mCaption;
    private int mIndex;

    public CsvColumn(String caption, int index) {
        mCaption = caption;
        mIndex = index;
    }

    public String getCaption() {
        return mCaption;
    }

    public void setCaption(String caption) {
        mCaption = caption;
    }

    public int getIndex() {
        return mIndex;
    }

    public void setIndex(int index) {
        mIndex = index;
    }

    public boolean isExists() {
        return mIndex >= 0;
    }
}
