package com.yoshione.fingen.model;

import android.content.ContentValues;
import android.os.Parcel;

import com.yoshione.fingen.dao.TemplatesDAO;
import com.yoshione.fingen.interfaces.IAbstractModel;

import java.math.BigDecimal;
import java.math.RoundingMode;

public class Template extends BaseModel implements IAbstractModel {

    private long mAccountID;
    private long mDestAccountID;
    private long mPayeeID;
    private long mCategoryID;
    private BigDecimal mAmount;
    private long mProjectID;
    private long mDepartmentID;
    private long mLocationID;
    private String mName;
    private String mComment;
    private BigDecimal mExchangeRate;
    private int mTrType;

    public Template() {
        super();
        this.mAccountID = -1;
        this.mPayeeID = -1;
        this.mCategoryID = -1;
        this.mAmount = BigDecimal.ZERO;
        this.mProjectID = -1;
        this.mDepartmentID = -1;
        this.mLocationID = -1;
        this.mName = "";
        this.mDestAccountID = -1;
        this.mExchangeRate = BigDecimal.ONE;
        this.mTrType = -1;
        this.mComment = "";
    }

    public Template(long id) {
        super(id);
    }

    public long getCategoryID() {
        return mCategoryID;
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

    public int getTrType() {
        return mTrType;
    }

    public void setTrType(int mTrType) {
        this.mTrType = mTrType;
    }

    public long getAccountID() {
        return mAccountID;
    }

    public void setAccountID(long mAccountID) {
        this.mAccountID = mAccountID;
    }

    public long getDestAccountID() {
        return mDestAccountID;
    }

    public void setDestAccountID(long mDestAccountID) {
        this.mDestAccountID = mDestAccountID;
    }

    public long getPayeeID() {
        return mPayeeID;
    }

    public void setPayeeID(long mPayeeID) {
        this.mPayeeID = mPayeeID;
    }

    public long getProjectID() {
        return mProjectID;
    }

    public void setProjectID(long mProjectID) {
        this.mProjectID = mProjectID;
    }

    public long getDepartmentID() {
        return mDepartmentID;
    }

    public void setDepartmentID(long departmentID) {
        mDepartmentID = departmentID;
    }

    public long getLocationID() {
        return mLocationID;
    }

    public void setLocationID(long locationID) {
        mLocationID = locationID;
    }

    public String getName() {
        return mName;
    }

    public void setName(String mName) {
        this.mName = mName;
    }

    public BigDecimal getExchangeRate() {
        return mExchangeRate;
    }

    public void setCategoryID(long categoryID) {
        mCategoryID = categoryID;
    }

    public void setExchangeRate(BigDecimal exchangeRate) {
        mExchangeRate = exchangeRate;
    }

    public String getComment() {
        return mComment;
    }

    public void setComment(String comment) {
        mComment = comment;
    }

    public BigDecimal getAmount() {
        int sign = (mTrType > 0) ? 1 : -1;
        return mAmount.multiply(new BigDecimal(sign));
    }

    public void setAmount(BigDecimal mAmount) {
        this.mAmount = mAmount.abs();
    }

    public void extractFromTransaction(Transaction transaction) {
        mAccountID = transaction.getAccountID();
        mDestAccountID = transaction.getDestAccountID();
        if (mDestAccountID < 0) {
            mPayeeID = transaction.getPayeeID();
        }
        mAmount = transaction.getAmount().abs();
        mTrType = transaction.getTransactionType();
        mCategoryID = transaction.getCategoryID();
        mExchangeRate = transaction.getExchangeRate();

        mProjectID = transaction.getProjectID();
        mDepartmentID = transaction.getDepartmentID();
        mLocationID = transaction.getLocationID();
        mComment = transaction.getComment();
    }

    public boolean isValid() {
        return !mName.isEmpty();
    }

    @Override
    public ContentValues getCV() {
        ContentValues values = super.getCV();

        values.put(TemplatesDAO.COL_SRC_ACCOUNT, getAccountID());
        values.put(TemplatesDAO.COL_PAYEE, getPayeeID());
        values.put(TemplatesDAO.COL_CATEGORY, getCategoryID());
        values.put(TemplatesDAO.COL_AMOUNT, getAmount().setScale(2, RoundingMode.HALF_EVEN).doubleValue());
        values.put(TemplatesDAO.COL_EXCHANGE_RATE, getExchangeRate().setScale(2, RoundingMode.HALF_EVEN).doubleValue());
        values.put(TemplatesDAO.COL_PROJECT, getProjectID());
        values.put(TemplatesDAO.COL_DEPARTMENT, getDepartmentID());
        values.put(TemplatesDAO.COL_LOCATION, getLocationID());
        values.put(TemplatesDAO.COL_NAME, getName());
        values.put(TemplatesDAO.COL_COMMENT, getComment());
        values.put(TemplatesDAO.COL_DEST_ACCOUNT, getDestAccountID());
        values.put(TemplatesDAO.COL_TYPE, getTrType());
        return values;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        super.writeToParcel(dest, flags);
        dest.writeLong(this.mAccountID);
        dest.writeLong(this.mDestAccountID);
        dest.writeLong(this.mPayeeID);
        dest.writeLong(this.mCategoryID);
        dest.writeSerializable(this.mAmount);
        dest.writeLong(this.mProjectID);
        dest.writeLong(this.mDepartmentID);
        dest.writeLong(this.mLocationID);
        dest.writeString(this.mName);
        dest.writeString(this.mComment);
        dest.writeSerializable(this.mExchangeRate);
        dest.writeInt(this.mTrType);
    }

    protected Template(Parcel in) {
        super(in);
        this.mAccountID = in.readLong();
        this.mDestAccountID = in.readLong();
        this.mPayeeID = in.readLong();
        this.mCategoryID = in.readLong();
        this.mAmount = (BigDecimal) in.readSerializable();
        this.mProjectID = in.readLong();
        this.mDepartmentID = in.readLong();
        this.mLocationID = in.readLong();
        this.mName = in.readString();
        this.mComment = in.readString();
        this.mExchangeRate = (BigDecimal) in.readSerializable();
        this.mTrType = in.readInt();
    }

    public static final Creator<Template> CREATOR = new Creator<Template>() {
        @Override
        public Template createFromParcel(Parcel source) {
            return new Template(source);
        }

        @Override
        public Template[] newArray(int size) {
            return new Template[size];
        }
    };

    @Override
    public String getLogTransactionsField() {
        return null;
    }
}
