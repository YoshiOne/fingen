package com.yoshione.fingen.model;

import android.content.ContentValues;
import android.os.Parcel;


import com.yoshione.fingen.DBHelper;
import com.yoshione.fingen.interfaces.IAbstractModel;


/**
 * Created by slv on 07.09.2016.
 * *
 */

public class BudgetCatSync extends BaseModel implements IAbstractModel {
    private int mYear;
    private int mMonth;
    private long mCategoryID;
    private double mAmount;
    private long mCabbageID;

    public BudgetCatSync() {
        super();
        mYear = 0;
        mMonth = 0;
        mCategoryID = -1;
        mAmount = 0d;
        mCabbageID = -1;
    }

    public BudgetCatSync(long id) {
        super(id);
    }

//    public BudgetCatSync(int year, int month, long categoryID, double amount, long cabbageID) {
//        super();
//        mYear = year;
//        mMonth = month;
//        mCategoryID = categoryID;
//        mAmount = amount;
//        mCabbageID = cabbageID;
//    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        super.writeToParcel(dest, flags);
        dest.writeInt(this.mYear);
        dest.writeInt(this.mMonth);
        dest.writeLong(this.mCategoryID);
        dest.writeDouble(this.mAmount);
        dest.writeLong(this.mCabbageID);
    }

    protected BudgetCatSync(Parcel in) {
        super(in);
        this.mYear = in.readInt();
        this.mMonth = in.readInt();
        this.mCategoryID = in.readLong();
        this.mAmount = in.readDouble();
        this.mCabbageID = in.readLong();
    }

    public static final Creator<BudgetCatSync> CREATOR = new Creator<BudgetCatSync>() {
        @Override
        public BudgetCatSync createFromParcel(Parcel source) {
            return new BudgetCatSync(source);
        }

        @Override
        public BudgetCatSync[] newArray(int size) {
            return new BudgetCatSync[size];
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

    public long getCategoryID() {
        return mCategoryID;
    }

    public void setCategoryID(long categoryID) {
        mCategoryID = categoryID;
    }

    public double getAmount() {
        return mAmount;
    }

    public void setAmount(double amount) {
        mAmount = amount;
    }

    public long getCabbageID() {
        return mCabbageID;
    }

    public void setCabbageID(long cabbageID) {
        mCabbageID = cabbageID;
    }

    @Override
    public ContentValues getCV() {
        ContentValues values = super.getCV();

        values.put(DBHelper.C_LOG_BUDGET_YEAR,mYear);
        values.put(DBHelper.C_LOG_BUDGET_MONTH,mMonth);
        values.put(DBHelper.C_LOG_BUDGET_CATEGORY,mCategoryID);
        values.put(DBHelper.C_LOG_BUDGET_AMOUNT,mAmount);
        values.put(DBHelper.C_LOG_BUDGET_CURRENCY,mCabbageID);
        return values;
    }

    @Override
    public String getLogTransactionsField() {
        return null;
    }
}
