package com.yoshione.fingen.model;

import android.content.ContentValues;
import android.os.Parcel;


import com.yoshione.fingen.DBHelper;
import com.yoshione.fingen.interfaces.IAbstractModel;

/**
 * Created by slv on 03.03.2016.
 *
 */
public class Credit extends BaseModel implements IAbstractModel {

    public static final int DEBT_ACTION_BORROW = 0;
    public static final int DEBT_ACTION_REPAY = 1;
    public static final int DEBT_ACTION_GRANT = 2;
    public static final int DEBT_ACTION_TAKE = 3;

//    private long mId = -1;
    private long mAccountID = -1;
    private long mPayeeID = -1;
    private long mCategoryID = -1;
    private boolean mClosed = false;
    private String  mComment = "";


    public Credit() {
        super();
        mAccountID = -1;
        mPayeeID = -1;
        mCategoryID = -1;
        mClosed = false;
        mComment = "";
    }

    public Credit(long id) {
        super(id);
    }

    public Credit(long mId, long mAccountID, long mPayeeID, long mCategoryID, boolean mClosed, String mComment) {
        super();
        setID(mId);
        this.mAccountID = mAccountID;
        this.mPayeeID = mPayeeID;
        this.mCategoryID = mCategoryID;
        this.mClosed = mClosed;
        this.mComment = mComment;
    }

    @Override
    public long getID() {
        return super.getID();
    }

    public long getAccountID() {
        return mAccountID;
    }

    public void setAccountID(long mAccountID) {
        this.mAccountID = mAccountID;
    }

    public long getPayeeID() {
        return mPayeeID;
    }

    public void setPayeeID(long mPayeeID) {
        this.mPayeeID = mPayeeID;
    }

    public long getCategoryID() {
        return mCategoryID;
    }

    public void setCategoryID(long mCategoryID) {
        this.mCategoryID = mCategoryID;
    }

    public boolean isClosed() {
        return mClosed;
    }

    public void setClosed(boolean mClosed) {
        this.mClosed = mClosed;
    }

    public String getComment() {
        return mComment;
    }

    public void setComment(String mComment) {
        this.mComment = mComment;
    }

    public boolean isValid() {
        return mAccountID >= 0 & mPayeeID >=0;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        super.writeToParcel(dest, flags);
        dest.writeLong(this.mAccountID);
        dest.writeLong(this.mPayeeID);
        dest.writeLong(this.mCategoryID);
        dest.writeByte(this.mClosed ? (byte) 1 : (byte) 0);
        dest.writeString(this.mComment);
    }

    protected Credit(Parcel in) {
        super(in);
        this.mAccountID = in.readLong();
        this.mPayeeID = in.readLong();
        this.mCategoryID = in.readLong();
        this.mClosed = in.readByte() != 0;
        this.mComment = in.readString();
    }

    public static final Creator<Credit> CREATOR = new Creator<Credit>() {
        @Override
        public Credit createFromParcel(Parcel source) {
            return new Credit(source);
        }

        @Override
        public Credit[] newArray(int size) {
            return new Credit[size];
        }
    };

    @Override
    public ContentValues getCV() {
        ContentValues values = super.getCV();

        values.put(DBHelper.C_REF_DEBTS_ACCOUNT, getAccountID());
        values.put(DBHelper.C_REF_DEBTS_PAYEE, getPayeeID());
        values.put(DBHelper.C_REF_DEBTS_CATEGORY, getCategoryID());
        values.put(DBHelper.C_REF_DEBTS_CLOSED, isClosed());
        values.put(DBHelper.C_REF_DEBTS_COMMENT, getComment());
        return values;
    }

    @Override
    public String getLogTransactionsField() {
        return null;
    }
}
