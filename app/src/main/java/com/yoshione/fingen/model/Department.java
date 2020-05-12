package com.yoshione.fingen.model;

import android.content.ContentValues;
import android.graphics.Color;
import android.os.Parcel;

import com.yoshione.fingen.dao.DepartmentsDAO;
import com.yoshione.fingen.dao.TransactionsDAO;
import com.yoshione.fingen.interfaces.IAbstractModel;

public class Department extends BaseModel implements IAbstractModel {

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
        values.put(DepartmentsDAO.COL_NAME, getName());
        values.put(DepartmentsDAO.COL_IS_ACTIVE, getIsActive());
        values.put(DepartmentsDAO.COL_PARENT_ID, mParentID);
        values.put(DepartmentsDAO.COL_ORDER_NUMBER, getOrderNum());
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
        return TransactionsDAO.COL_DEPARTMENT;
    }
}
