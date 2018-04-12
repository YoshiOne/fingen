package com.yoshione.fingen.model;

import android.content.ContentValues;
import android.os.Parcel;


import com.yoshione.fingen.DBHelper;
import com.yoshione.fingen.interfaces.IAbstractModel;

/**
 * Created by Leonid on 30.07.2016.
 *
 */

public class Sender extends BaseModel implements IAbstractModel {

//    private long mId = -1;
    private String mName;
    private Boolean mIsActive;
    private String mPhoneNo;
    private int mAmountPos;
    private int mBalancePos;
    private String mDateFormat;
    private boolean mLeadingCurrencySymbol;
    private boolean mAddCreditLimitToBalance;

    public Sender(long id, String name, Boolean isActive, String phoneNo, int amountPos, int balancePos,
                  String dateFormat, boolean leadingCurrencySymbol, boolean addCreditLimitToBalance) {
        super();
        setID(id);
        mName = name;
        mIsActive = isActive;
        mPhoneNo = phoneNo;
        mAmountPos = amountPos;
        mBalancePos = balancePos;
        mDateFormat = dateFormat;
        mLeadingCurrencySymbol = leadingCurrencySymbol;
        mAddCreditLimitToBalance = addCreditLimitToBalance;
    }

    public Sender(long id) {
        super(id);
    }

    public Sender() {
        super();
        mName = "";
        mIsActive = true;
        mPhoneNo = "";
        mAmountPos = 0;
        mBalancePos = 1;
        mDateFormat = "";
        mLeadingCurrencySymbol = false;
        mAddCreditLimitToBalance = false;
    }

    @Override
    public String toString() {
        if (mName.length() > 0 | mPhoneNo.length() > 0) {
            return String.format("%s (%s)", mName, mPhoneNo);
        } else {
            return "";
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

    public String getName() {
        return mName;
    }

    public void setName(String name) {
        mName = name;
    }

    public Boolean getActive() {
        return mIsActive;
    }

    public void setActive(Boolean active) {
        mIsActive = active;
    }

    public String getPhoneNo() {
        return mPhoneNo;
    }

    public void setPhoneNo(String phoneNo) {
        mPhoneNo = phoneNo;
    }

    public int getAmountPos() {
        return mAmountPos;
    }

    public void setAmountPos(int amountPos) {
        mAmountPos = amountPos;
    }

    public int getBalancePos() {
        return mBalancePos;
    }

    public void setBalancePos(int balancePos) {
        mBalancePos = balancePos;
    }

    public String getDateFormat() {
        return mDateFormat;
    }

    public void setDateFormat(String dateFormat) {
        mDateFormat = dateFormat;
    }

    public boolean isLeadingCurrencySymbol() {
        return mLeadingCurrencySymbol;
    }

    public void setLeadingCurrencySymbol(boolean leadingCurrencySymbol) {
        mLeadingCurrencySymbol = leadingCurrencySymbol;
    }

    @Override
    public String getFullName() {
        return String.format("%s (%s)", mName, mPhoneNo);
    }

    public boolean isAddCreditLimitToBalance() {
        return mAddCreditLimitToBalance;
    }

    public void setAddCreditLimitToBalance(boolean addCreditLimitToBalance) {
        mAddCreditLimitToBalance = addCreditLimitToBalance;
    }

    @Override
    public ContentValues getCV() {
        ContentValues values = super.getCV();
        values.put(DBHelper.C_REF_SENDERS_NAME, getName());
        values.put(DBHelper.C_REF_SENDERS_PHONENO, getPhoneNo());
        values.put(DBHelper.C_REF_SENDERS_AMOUNTPOS, getAmountPos());
        values.put(DBHelper.C_REF_SENDERS_BALANCEPOS, getBalancePos());
        values.put(DBHelper.C_REF_SENDERS_DATEFORMAT, getDateFormat());
        values.put(DBHelper.C_REF_SENDERS_LEADING_CURRENCY_SYMBOL, isLeadingCurrencySymbol());
        values.put(DBHelper.C_REF_SENDERS_ADD_CREDIT_LIMIT_TO_BALANCE, isAddCreditLimitToBalance());
        values.put(DBHelper.C_REF_SENDERS_ISACTIVE, getActive() ? 1 : 0);
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
        dest.writeString(this.mName);
        dest.writeValue(this.mIsActive);
        dest.writeString(this.mPhoneNo);
        dest.writeInt(this.mAmountPos);
        dest.writeInt(this.mBalancePos);
        dest.writeString(this.mDateFormat);
        dest.writeByte(this.mLeadingCurrencySymbol ? (byte) 1 : (byte) 0);
        dest.writeByte(this.mAddCreditLimitToBalance ? (byte) 1 : (byte) 0);
    }

    protected Sender(Parcel in) {
        super(in);
        this.mName = in.readString();
        this.mIsActive = (Boolean) in.readValue(Boolean.class.getClassLoader());
        this.mPhoneNo = in.readString();
        this.mAmountPos = in.readInt();
        this.mBalancePos = in.readInt();
        this.mDateFormat = in.readString();
        this.mLeadingCurrencySymbol = in.readByte() != 0;
        this.mAddCreditLimitToBalance = in.readByte() != 0;
    }

    public static final Creator<Sender> CREATOR = new Creator<Sender>() {
        @Override
        public Sender createFromParcel(Parcel source) {
            return new Sender(source);
        }

        @Override
        public Sender[] newArray(int size) {
            return new Sender[size];
        }
    };
}
