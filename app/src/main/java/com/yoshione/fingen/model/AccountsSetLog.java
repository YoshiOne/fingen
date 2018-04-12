package com.yoshione.fingen.model;

import android.content.ContentValues;
import android.os.Parcel;


import com.yoshione.fingen.DBHelper;
import com.yoshione.fingen.interfaces.IAbstractModel;

/**
 * Created by slv on 01.12.2016.
 * a
 */

public class AccountsSetLog extends BaseModel implements IAbstractModel {
    private long mAccountSetID;
    private long mAccountID;

    public AccountsSetLog() {
        super();
        this.mAccountSetID = -1;
        this.mAccountID = -1;
    }

    public AccountsSetLog(long id) {
        super(id);
    }

    public AccountsSetLog(long id, long accountSetID, long accountID) {
        setID(id);
        mAccountSetID = accountSetID;
        mAccountID = accountID;
    }

    public long getAccountSetID() {
        return mAccountSetID;
    }

    public void setAccountSetID(long accountSetID) {
        mAccountSetID = accountSetID;
    }

    public long getAccountID() {
        return mAccountID;
    }

    public void setAccountID(long accountID) {
        mAccountID = accountID;
    }

    @Override
    public ContentValues getCV() {
        ContentValues values = super.getCV();
        values.put(DBHelper.C_LOG_ACCOUNTS_SETS_SET, getAccountSetID());
        values.put(DBHelper.C_LOG_ACCOUNTS_SETS_ACCOUNT, getAccountID());
        return values;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        super.writeToParcel(dest, flags);
        dest.writeLong(this.mAccountSetID);
        dest.writeLong(this.mAccountID);
    }

    protected AccountsSetLog(Parcel in) {
        super(in);
        this.mAccountSetID = in.readLong();
        this.mAccountID = in.readLong();
    }

    public static final Creator<AccountsSetLog> CREATOR = new Creator<AccountsSetLog>() {
        @Override
        public AccountsSetLog createFromParcel(Parcel source) {
            return new AccountsSetLog(source);
        }

        @Override
        public AccountsSetLog[] newArray(int size) {
            return new AccountsSetLog[size];
        }
    };
}
