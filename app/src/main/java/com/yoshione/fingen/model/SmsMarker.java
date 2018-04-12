/*
 * Copyright (c) 2015.
 */

package com.yoshione.fingen.model;

import android.content.ContentValues;
import android.os.Parcel;


import com.yoshione.fingen.DBHelper;
import com.yoshione.fingen.interfaces.IAbstractModel;

/**
 * Created by Leonid on 01.11.2015.
 *
 */
public class SmsMarker extends BaseModel implements IAbstractModel {
    public static final String TAG = "com.yoshione.fingen.Model.SmsMarker";

//    private long mId = -1;
    private int mType = -1;
    private String mObject = "";
    private String mMarker = "";

    public void setSearchString(String searchString) {
        mSearchString = searchString;
    }

    private String mSearchString = "";

    public SmsMarker(){
        super();
        mType = -1;
        mObject = "";
        mMarker = "";
    }

    public SmsMarker(long id) {
        super(id);
    }

    public SmsMarker(long mId, int mType, String mObject, String mPattern) {
        super();
        setID(mId);
        this.mType = mType;
        this.mObject = mObject;
        this.mMarker = mPattern;
    }

    public int getType() {
        return mType;
    }

    public void setType(int mType) {
        this.mType = mType;
    }

    public String getObject() {
        return mObject;
    }

    public void setObject(String mObject) {
        this.mObject = mObject;
    }

    public String getMarker() {
        return mMarker;
    }

    public void setMarker(String mMarker) {
        this.mMarker = mMarker;
    }

    @Override
    public String toString() {
        return String.format("%s~%s~%s",String.valueOf(mType),mObject, mMarker);
    }

    @Override
    public String getSearchString() {
        return mSearchString;
    }

    @Override
    public long getID() {
        return super.getID();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        super.writeToParcel(dest, flags);
        dest.writeInt(this.mType);
        dest.writeString(this.mObject);
        dest.writeString(this.mMarker);
        dest.writeString(this.mSearchString);
    }

    protected SmsMarker(Parcel in) {
        super(in);
        this.mType = in.readInt();
        this.mObject = in.readString();
        this.mMarker = in.readString();
        this.mSearchString = in.readString();
    }

    public static final Creator<SmsMarker> CREATOR = new Creator<SmsMarker>() {
        @Override
        public SmsMarker createFromParcel(Parcel source) {
            return new SmsMarker(source);
        }

        @Override
        public SmsMarker[] newArray(int size) {
            return new SmsMarker[size];
        }
    };

    @Override
    public ContentValues getCV() {
        ContentValues values = super.getCV();

        values.put(DBHelper.C_LOG_SMS_PARSER_PATTERNS_TYPE, getType());
        values.put(DBHelper.C_LOG_SMS_PARSER_PATTERNS_OBJECT, getObject());
        values.put(DBHelper.C_LOG_SMS_PARSER_PATTERNS_PATTERN, getMarker());
        return values;
    }

    @Override
    public String getLogTransactionsField() {
        return null;
    }
}
