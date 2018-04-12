/*
 * Copyright (c) 2015.
 */

package com.yoshione.fingen.model;

import android.content.ContentValues;
import android.os.Parcel;

import com.yoshione.fingen.DBHelper;
import com.yoshione.fingen.interfaces.IAbstractModel;
import com.yoshione.fingen.interfaces.IOrderable;

/**
 * Created by slv on 04.12.2015.
 *
 */
public class Cabbage extends BaseModel implements IAbstractModel, IOrderable {
//    private long mId;
    private String mCode;
    private String mSimbol;
    private String mName;
    private int mDecimalCount;
    private int mOrderNum;

    public Cabbage() {
        super();
//        mId = -1;
        mCode = "";
        mSimbol = "";
        mName = "";
        mDecimalCount = 2;
    }

    public Cabbage(long id) {
        super(id);
    }

    public Cabbage(long mId, String mCode, String mSimbol, String mName, int mDecimalCount) {
        super();
        setID(mId);
        this.mCode = mCode;
        this.mSimbol = mSimbol;
        this.mName = mName;
        this.mDecimalCount = mDecimalCount;
    }

    @Override
    public int getOrderNum() {
        return mOrderNum;
    }

    @Override
    public void setOrderNum(int orderNum) {
        mOrderNum = orderNum;
    }

    public String getCode() {
        return mCode;
    }

    public void setCode(String mCode) {
        this.mCode = mCode;
    }

    public String getSimbol() {
        return mSimbol;
    }

    public void setSimbol(String mSimbol) {
        this.mSimbol = mSimbol;
    }

    public String getName() {
        return mName;
    }

    public void setName(String mName) {
        this.mName = mName;
    }

    public int getDecimalCount() {
        return mDecimalCount;
    }

    public void setDecimalCount(int mDecimalCount) {
        this.mDecimalCount = mDecimalCount;
    }

    @Override
    public String toString() {
        if (getID() < 0) {
            return "";
        } else {
            return String.format("%s (%s, %s)", mName, mCode, mSimbol);
        }
    }

    @Override
    public String getSearchString() {
        return toString();
    }

    @Override
    public long getID() {
        return super.getID();
    }

    @Override
    public ContentValues getCV() {
        ContentValues values = super.getCV();
        values.put(DBHelper.C_REF_CURRENCIES_CODE,getCode());
        values.put(DBHelper.C_REF_CURRENCIES_SYMBOL, getSimbol());
        values.put(DBHelper.C_REF_CURRENCIES_NAME, getName());
        values.put(DBHelper.C_REF_CURRENCIES_DECIMALCOUNT, getDecimalCount());
        return values;
    }

    @Override
    public String getLogTransactionsField() {
        return null;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        super.writeToParcel(dest, flags);
        dest.writeString(this.mCode);
        dest.writeString(this.mSimbol);
        dest.writeString(this.mName);
        dest.writeInt(this.mDecimalCount);
        dest.writeInt(this.mOrderNum);
    }

    protected Cabbage(Parcel in) {
        super(in);
        this.mCode = in.readString();
        this.mSimbol = in.readString();
        this.mName = in.readString();
        this.mDecimalCount = in.readInt();
        this.mOrderNum = in.readInt();
    }

    public static final Creator<Cabbage> CREATOR = new Creator<Cabbage>() {
        @Override
        public Cabbage createFromParcel(Parcel source) {
            return new Cabbage(source);
        }

        @Override
        public Cabbage[] newArray(int size) {
            return new Cabbage[size];
        }
    };
}
