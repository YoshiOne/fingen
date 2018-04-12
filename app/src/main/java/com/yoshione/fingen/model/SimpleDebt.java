package com.yoshione.fingen.model;

import android.content.ContentValues;
import android.os.Parcel;


import com.yoshione.fingen.DBHelper;
import com.yoshione.fingen.interfaces.IAbstractModel;

import java.math.BigDecimal;

/**
 * Created by slv on 13.08.2015.
 *
 */
public class SimpleDebt extends BaseModel implements IAbstractModel {
    public static final String TAG = "com.yoshione.fingen.Model.SimpleDebt";

//    private long mId = -1;
    private String mName;
    private Boolean mIsActive;
    private BigDecimal mStartAmount;//- Я должен, + мне должны
    private long mCabbageID;
    private BigDecimal mAmount;
    private BigDecimal mOweMe;

    public BigDecimal getAmount() {
        return mAmount;
    }

    public void setAmount(BigDecimal amount) {
        mAmount = amount;
    }

    public BigDecimal getOweMe() {
        return mOweMe;
    }

    public void setOweMe(BigDecimal oweMe) {
        mOweMe = oweMe;
    }

    public BigDecimal getIOwe() {
        return mIOwe;
    }

    public void setIOwe(BigDecimal IOwe) {
        mIOwe = IOwe;
    }

    private BigDecimal mIOwe;

    public SimpleDebt(){
        super();
        this.mName = "";
        this.mIsActive = true;
        mStartAmount = BigDecimal.ZERO;
        mCabbageID = -1;
    }

    public SimpleDebt(long id) {
        super(id);
    }

    public SimpleDebt(long id, String name, Boolean isActive, BigDecimal startAmount, long cabbageID) {
        super();
        setID(id);
        mName = name;
        mIsActive = isActive;
        mStartAmount = startAmount;
        mCabbageID = cabbageID;
    }

    public String getName() {
        return mName;
    }

    public void setName(String mName) {
        this.mName = mName;
    }

    public Boolean isActive() {
        return mIsActive;
    }

    public void setIsActive(Boolean mIsActive) {
        this.mIsActive = mIsActive;
    }

    public BigDecimal getStartAmount() {
        return mStartAmount;
    }

    public void setStartAmount(BigDecimal startAmount) {
        mStartAmount = startAmount;
    }

    public long getCabbageID() {
        return mCabbageID;
    }

    public void setCabbageID(long cabbageID) {
        mCabbageID = cabbageID;
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
        values.put(DBHelper.C_REF_SIMPLEDEBTS_NAME, mName);
        values.put(DBHelper.C_REF_SIMPLEDEBTS_ISACTIVE, mIsActive ? 1 : 0);
        values.put(DBHelper.C_REF_SIMPLEDEBTS_START_AMOUNT, mStartAmount.doubleValue());
        values.put(DBHelper.C_REF_SIMPLEDEBTS_CABBAGE, mCabbageID);
        return values;
    }

    @Override
    public String getLogTransactionsField() {
        return DBHelper.C_LOG_TRANSACTIONS_SIMPLEDEBT;
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
        dest.writeSerializable(this.mStartAmount);
        dest.writeLong(this.mCabbageID);
        dest.writeSerializable(this.mAmount);
        dest.writeSerializable(this.mOweMe);
        dest.writeSerializable(this.mIOwe);
    }

    protected SimpleDebt(Parcel in) {
        super(in);
        this.mName = in.readString();
        this.mIsActive = (Boolean) in.readValue(Boolean.class.getClassLoader());
        this.mStartAmount = (BigDecimal) in.readSerializable();
        this.mCabbageID = in.readLong();
        this.mAmount = (BigDecimal) in.readSerializable();
        this.mOweMe = (BigDecimal) in.readSerializable();
        this.mIOwe = (BigDecimal) in.readSerializable();
    }

    public static final Creator<SimpleDebt> CREATOR = new Creator<SimpleDebt>() {
        @Override
        public SimpleDebt createFromParcel(Parcel source) {
            return new SimpleDebt(source);
        }

        @Override
        public SimpleDebt[] newArray(int size) {
            return new SimpleDebt[size];
        }
    };
}
