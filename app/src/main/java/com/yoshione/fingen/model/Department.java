package com.yoshione.fingen.model;

import android.content.ContentValues;
import android.graphics.Color;
import android.os.Parcel;
import android.support.annotation.NonNull;


import com.yoshione.fingen.DBHelper;
import com.yoshione.fingen.interfaces.IAbstractModel;

/**
 * Created by slv on 13.08.2015.
 *
 */
public class Department extends BaseModel implements IAbstractModel {
    public static final String TAG = "com.yoshione.fingen.Model.Department";

//    private long mId = -1;
    private String mName;
    private Boolean mIsActive;
    private long mParentID;
    private int mOrderNum;
    private boolean mExpanded;

    public Department(){
        super();
        this.mName = "";
        this.mIsActive = true;
        mParentID = -1;
        mExpanded = true;
    }

    public Department(long id) {
        super(id);
    }

    public Department(long id, String name, Boolean isActive, long parentID, int orderNum, boolean expanded) {
        setID(id);
        mName = name;
        mIsActive = isActive;
        mParentID = parentID;
        mOrderNum = orderNum;
        mExpanded = expanded;
    }

    public long getParentID() {
        return mParentID;
    }

    public void setParentID(long parentID) {
        mParentID = parentID;
    }

    public int getOrderNum() {
        return mOrderNum;
    }

    public void setOrderNum(int orderNum) {
        mOrderNum = orderNum;
    }

    public boolean isExpanded() {
        return mExpanded;
    }

    public void setExpanded(boolean expanded) {
        mExpanded = expanded;
    }

    public String getName() {
        return mName;
    }

    public void setName(String mName) {
        this.mName = mName;
    }

    public Boolean getIsActive() {
        return mIsActive;
    }

    public void setIsActive(Boolean mIsActive) {
        this.mIsActive = mIsActive;
    }

    @Override
    public String toString() {
        return mName;
    }

    @Override
    public String getSearchString() {
        return mName;
    }

    @Override
    public long getID() {
        return super.getID();
    }

    @Override
    public ContentValues getCV() {
        ContentValues values = super.getCV();
        values.put(DBHelper.C_REF_DEPARTMENTS_NAME,getName());
        values.put(DBHelper.C_REF_DEPARTMENTS_ISACTIVE, getIsActive());
        values.put(DBHelper.C_PARENTID, mParentID);
        values.put(DBHelper.C_ORDERNUMBER, getOrderNum());
        return values;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        super.writeToParcel(dest, flags);
        dest.writeString(this.mName);
        dest.writeValue(this.mIsActive);
        dest.writeLong(this.mParentID);
        dest.writeInt(this.mOrderNum);
        dest.writeByte(this.mExpanded ? (byte) 1 : (byte) 0);
    }

    protected Department(Parcel in) {
        super(in);
        this.mName = in.readString();
        this.mIsActive = (Boolean) in.readValue(Boolean.class.getClassLoader());
        this.mParentID = in.readLong();
        this.mOrderNum = in.readInt();
        this.mExpanded = in.readByte() != 0;
    }

    public static final Creator<Department> CREATOR = new Creator<Department>() {
        @Override
        public Department createFromParcel(Parcel source) {
            return new Department(source);
        }

        @Override
        public Department[] newArray(int size) {
            return new Department[size];
        }
    };

    @Override
    public int getColor() {
        return Color.TRANSPARENT;
    }

    @Override
    public String getLogTransactionsField() {
        return DBHelper.C_LOG_TRANSACTIONS_DEPARTMENT;
    }
}
