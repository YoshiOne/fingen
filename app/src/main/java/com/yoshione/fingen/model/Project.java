package com.yoshione.fingen.model;

import android.content.ContentValues;
import android.graphics.Color;
import android.os.Parcel;
import android.support.annotation.NonNull;


import com.yoshione.fingen.DBHelper;
import com.yoshione.fingen.interfaces.IAbstractModel;
import com.yoshione.fingen.interfaces.IOrderable;

/**
 * Created by slv on 13.08.2015.
 *
 */
public class Project extends BaseModel implements IAbstractModel, IOrderable {

    //    private long mId = -1;
    private String mName;
    private Boolean mIsActive;
    private long mParentID;
    private int mOrderNum;
    private int mColor;

    public void setColor(int color) {
        mColor = color;
    }

    private boolean mExpanded;

    public Project() {
        super();
        this.mName = "";
        this.mIsActive = true;
        mParentID = -1;
        mOrderNum = 0;
        mExpanded = true;
        mColor = Color.TRANSPARENT;
    }

    public Project(long id) {
        super(id);
    }

//    public Project(long id, String name, Boolean isActive, long parentID, int orderNum, boolean expanded) {
//        setID(id);
//        mName = name;
//        mIsActive = isActive;
//        mParentID = parentID;
//        mOrderNum = orderNum;
//        mExpanded = expanded;
//    }

    public String getName() {
        return mName;
    }

    public void setName(String mName) {
        this.mName = mName;
    }

    @Override
    public int getColor() {
        return mColor;
    }

    public Boolean getIsActive() {
        return mIsActive;
    }

    public void setIsActive(Boolean mIsActive) {
        this.mIsActive = mIsActive;
    }

    @Override
    public long getParentID() {
        return mParentID;
    }

    @Override
    public void setParentID(long parentID) {
        mParentID = parentID;
    }

    @Override
    public int getOrderNum() {
        return mOrderNum;
    }

    @Override
    public void setOrderNum(int orderNum) {
        mOrderNum = orderNum;
    }

    @Override
    public boolean isExpanded() {
        return mExpanded;
    }

    @Override
    public void setExpanded(boolean expanded) {
        mExpanded = expanded;
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
    public ContentValues getCV() {
        ContentValues values = super.getCV();
        values.put(DBHelper.C_REF_PROJECTS_NAME, getName());
        values.put(DBHelper.C_REF_PROJECTS_ISACTIVE, getIsActive());
        values.put(DBHelper.C_PARENTID, mParentID);
        values.put(DBHelper.C_ORDERNUMBER, getOrderNum());
        values.put(DBHelper.C_REF_PROJECTS_COLOR,String.format("#%06X", (0xFFFFFF & getColor())));
        return values;
    }

    @Override
    public String getLogTransactionsField() {
        return DBHelper.C_LOG_TRANSACTIONS_PROJECT;
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
        dest.writeInt(this.mColor);
        dest.writeByte(this.mExpanded ? (byte) 1 : (byte) 0);
    }

    protected Project(Parcel in) {
        super(in);
        this.mName = in.readString();
        this.mIsActive = (Boolean) in.readValue(Boolean.class.getClassLoader());
        this.mParentID = in.readLong();
        this.mOrderNum = in.readInt();
        this.mColor = in.readInt();
        this.mExpanded = in.readByte() != 0;
    }

    public static final Creator<Project> CREATOR = new Creator<Project>() {
        @Override
        public Project createFromParcel(Parcel source) {
            return new Project(source);
        }

        @Override
        public Project[] newArray(int size) {
            return new Project[size];
        }
    };
}
