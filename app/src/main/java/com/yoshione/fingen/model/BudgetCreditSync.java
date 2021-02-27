package com.yoshione.fingen.model;

import android.content.ContentValues;
import android.os.Parcel;

import com.yoshione.fingen.dao.BudgetCreditsDAO;
import com.yoshione.fingen.interfaces.IAbstractModel;

public class BudgetCreditSync extends BaseModel implements IAbstractModel {
    private int mYear;
    private int mMonth;
    private long mCreditID;
    private double mAmount;

    public BudgetCreditSync() {
        super();
        mYear = 0;
        mMonth = 0;
        mCreditID = -1;
        mAmount = 0d;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        super.writeToParcel(dest, flags);
        dest.writeInt(this.mYear);
        dest.writeInt(this.mMonth);
        dest.writeLong(this.mCreditID);
        dest.writeDouble(this.mAmount);
    }

    protected BudgetCreditSync(Parcel in) {
        super(in);
        this.mYear = in.readInt();
        this.mMonth = in.readInt();
        this.mCreditID = in.readLong();
        this.mAmount = in.readDouble();
    }

    public static final Creator<BudgetCreditSync> CREATOR = new Creator<BudgetCreditSync>() {
        @Override
        public BudgetCreditSync createFromParcel(Parcel source) {
            return new BudgetCreditSync(source);
        }

        @Override
        public BudgetCreditSync[] newArray(int size) {
            return new BudgetCreditSync[size];
        }
    };

    public int getYear() {
        return mYear;
    }

    public void setYear(int year) {
        mYear = year;
    }

    public int getMonth() {
        return mMonth;
    }

    public void setMonth(int month) {
        mMonth = month;
    }

    public long getCreditID() {
        return mCreditID;
    }

    public void setCreditID(long creditID) {
        mCreditID = creditID;
    }

    public double getAmount() {
        return mAmount;
    }

    public void setAmount(double amount) {
        mAmount = amount;
    }

    @Override
    public ContentValues getCV() {
        ContentValues values = super.getCV();

        values.put(BudgetCreditsDAO.COL_YEAR, mYear);
        values.put(BudgetCreditsDAO.COL_MONTH, mMonth);
        values.put(BudgetCreditsDAO.COL_DEBT, mCreditID);
        values.put(BudgetCreditsDAO.COL_AMOUNT, mAmount);
        return values;
    }

    @Override
    public String getLogTransactionsField() {
        return null;
    }
}
