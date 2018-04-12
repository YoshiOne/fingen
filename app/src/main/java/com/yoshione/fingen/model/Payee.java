package com.yoshione.fingen.model;

import android.content.ContentValues;
import android.graphics.Color;
import android.os.Parcel;
import android.support.annotation.NonNull;


import com.yoshione.fingen.DBHelper;
import com.yoshione.fingen.interfaces.IAbstractModel;
import com.yoshione.fingen.interfaces.IOrderable;

/**
 * Created by slv on 14.08.2015.
 * a
 */
public class Payee extends BaseModel implements IAbstractModel, IOrderable {

    public static final String TAG = "com.yoshione.fingen.Model.Payee";
    private String mName;
    private long mDefCategoryID;
    private long mParentID;
    private int mOrderNum;
    private boolean mExpanded;

    public Payee() {
        super();
        mName = "";
        mDefCategoryID = -1;
        mParentID = -1;
        mExpanded = true;
    }

    public Payee(long id) {
        super(id);
    }

    public Payee(long id, String name, long defCategoryID, long parentID, int orderNum, boolean expanded) {
        super();
        setID(id);
        mName = name;
        mDefCategoryID = defCategoryID;
        mParentID = parentID;
        mOrderNum = orderNum;
        mExpanded = expanded;
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

    public String getName() {
        return mName;
    }

    public void setName(String mName) {
        this.mName = mName;
    }

    @Override
    public int getColor() {
        return Color.TRANSPARENT;
    }

    public long getDefCategoryID() {
        return mDefCategoryID;
    }

    public void setDefCategoryID(long mDefCategoryID) {
        this.mDefCategoryID = mDefCategoryID;
    }

    @Override
    public long getID() {
        return super.getID();
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

        values.put(DBHelper.C_REF_PAYEES_NAME, getName());
        values.put(DBHelper.C_REF_PAYEES_DEFCATEGORY, getDefCategoryID());
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
        dest.writeLong(this.mDefCategoryID);
        dest.writeLong(this.mParentID);
        dest.writeInt(this.mOrderNum);
        dest.writeByte(this.mExpanded ? (byte) 1 : (byte) 0);
    }

    protected Payee(Parcel in) {
        super(in);
        this.mName = in.readString();
        this.mDefCategoryID = in.readLong();
        this.mParentID = in.readLong();
        this.mOrderNum = in.readInt();
        this.mExpanded = in.readByte() != 0;
    }

    public static final Creator<Payee> CREATOR = new Creator<Payee>() {
        @Override
        public Payee createFromParcel(Parcel source) {
            return new Payee(source);
        }

        @Override
        public Payee[] newArray(int size) {
            return new Payee[size];
        }
    };

    @Override
    public String getLogTransactionsField() {
        return DBHelper.C_LOG_TRANSACTIONS_PAYEE;
    }
}
