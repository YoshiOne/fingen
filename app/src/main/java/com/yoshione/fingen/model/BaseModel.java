package com.yoshione.fingen.model;

import android.content.ContentValues;
import android.graphics.Color;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.NonNull;

import com.yoshione.fingen.DBHelper;
import com.yoshione.fingen.interfaces.IAbstractModel;

import java.math.BigDecimal;

/**
 * Created by slv on 10.08.2016.
 *
 */

public class BaseModel implements Parcelable, IAbstractModel {

    public static final int SORT_BY_ACCOUNT_CUSTOM = 0;
    public static final int SORT_BY_ACCOUNT_DATE_CREATION = 1;
    public static final int SORT_BY_ACCOUNT_DATE_LASTOP = 2;
    public static final int SORT_BY_ACCOUNT_BALANCE = 3;
    public static final int SORT_BY_ACCOUNT_TYPE = 4;
    public static final int SORT_BY_NAME = 5;
    public static final int SORT_BY_INCOME_AND_EXPENSE = 6;
    public static final int SORT_BY_INCOME_MINUS_EXPENSE = 7;
    public static final int SORT_BY_INCOME = 8;
    public static final int SORT_BY_EXPENSE = 9;

    private long mID;
    private String mName;
    private String mFullName;
    private String mFBID;
    private long mTS;
    private boolean mDeleted;
    private boolean mDirty;
    private String mLastEdited;
    private boolean mSelected;
    private BigDecimal mIncome;
    private BigDecimal mExpense;
    protected int mSortType = SORT_BY_NAME;
    protected String TAG = getClass().getName();

    public BaseModel() {
        mID = -1;
        mFBID = "";
        mTS = -1;
        mDeleted = false;
        mDirty = false;
        mLastEdited = "";
        mName = "";
        mFullName = "";
        mIncome = BigDecimal.ZERO;
        mExpense = BigDecimal.ZERO;
    }

    public BaseModel(long id) {
        mID = id;
        mIncome = BigDecimal.ZERO;
        mExpense = BigDecimal.ZERO;
        mTS = -1;
        mDeleted = false;
        mDirty = false;
    }

    public boolean isSelected() {
        return mSelected;
    }

    public void setSelected(boolean selected) {
        mSelected = selected;
    }

    @Override
    public String getSearchString() {
        return "";
    }

    @Override
    public long getID() {
        return mID;
    }

    @Override
    public void setID(long ID) {
        mID = ID;
    }

    @Override
    public String getFBID() {
        return mFBID;
    }

    @Override
    public void setFBID(String FBID) {
        mFBID = FBID;
    }

    @Override
    public long getTS() {
        return mTS;
    }

    @Override
    public void setTS(long TS) {
        mTS = TS;
    }

    @Override
    public boolean isDeleted() {
        return mDeleted;
    }

    public void setDeleted(boolean deleted) {
        mDeleted = deleted;
    }

    public boolean isDirty() {
        return mDirty;
    }

    public void setDirty(boolean dirty) {
        mDirty = dirty;
    }

    public String getLastEdited() {
        return mLastEdited;
    }

    public void setLastEdited(String lastEdited) {
        mLastEdited = lastEdited;
    }

    public BigDecimal getIncome() {
        return mIncome;
    }

    public void setIncome(BigDecimal income) {
        mIncome = income;
    }

    public BigDecimal getExpense() {
        return mExpense;
    }

    public void setExpense(BigDecimal expense) {
        mExpense = expense;
    }

    @Override
    public int getModelType() {
        if (getClass().equals(Cabbage.class)) {
            return IAbstractModel.MODEL_TYPE_CABBAGE;
        }
        if (getClass().equals(Location.class)) {
            return IAbstractModel.MODEL_TYPE_LOCATION;
        }
        if (getClass().equals(Payee.class)) {
            return IAbstractModel.MODEL_TYPE_PAYEE;
        }
        if (getClass().equals(Project.class)) {
            return IAbstractModel.MODEL_TYPE_PROJECT;
        }
        if (getClass().equals(SmsMarker.class)) {
            return IAbstractModel.MODEL_TYPE_SMSMARKER;
        }
        if (getClass().equals(Account.class)) {
            return IAbstractModel.MODEL_TYPE_ACCOUNT;
        }
        if (getClass().equals(Category.class)) {
            return IAbstractModel.MODEL_TYPE_CATEGORY;
        }
        if (getClass().equals(Sms.class)) {
            return IAbstractModel.MODEL_TYPE_SMS;
        }
        if (getClass().equals(Transaction.class)) {
            return IAbstractModel.MODEL_TYPE_TRANSACTION;
        }
        if (getClass().equals(Credit.class)) {
            return IAbstractModel.MODEL_TYPE_CREDIT;
        }
        if (getClass().equals(Template.class)) {
            return IAbstractModel.MODEL_TYPE_TEMPLATE;
        }
        if (getClass().equals(Department.class)) {
            return IAbstractModel.MODEL_TYPE_DEPARTMENT;
        }
        if (getClass().equals(SimpleDebt.class)) {
            return IAbstractModel.MODEL_TYPE_SIMPLEDEBT;
        }
        if (getClass().equals(Sender.class)) {
            return IAbstractModel.MODEL_TYPE_SENDER;
        }
        if (getClass().equals(Product.class)) {
            return IAbstractModel.MODEL_TYPE_PRODUCT;
        }
        if (getClass().equals(ProductEntry.class)) {
            return IAbstractModel.MODEL_TYPE_PRODUCT_ENTRY;
        }
        return 0;
    }

    public static String getNameByType(int modelType) {
        String name = "";
        switch (modelType) {
            case MODEL_TYPE_CABBAGE:
                name = "CABBAGE";
                break;
            case MODEL_TYPE_LOCATION:
                name = "LOCATION";
                break;
            case MODEL_TYPE_PAYEE:
                name = "PAYEE";
                break;
            case MODEL_TYPE_PROJECT:
                name = "PROJECT";
                break;
            case MODEL_TYPE_SMSMARKER:
                name = "SMSMARKER";
                break;
            case MODEL_TYPE_ACCOUNT:
                name = "ACCOUNT";
                break;
            case MODEL_TYPE_CATEGORY:
                name = "CATEGORY";
                break;
            case MODEL_TYPE_SMS:
                name = "SMS";
                break;
            case MODEL_TYPE_TRANSACTION:
                name = "TRANSACTION";
                break;
            case MODEL_TYPE_CREDIT:
                name = "CREDIT";
                break;
            case MODEL_TYPE_TEMPLATE:
                name = "TEMPLATE";
                break;
            case MODEL_TYPE_DEPARTMENT:
                name = "DEPARTMENT";
                break;
            case MODEL_TYPE_SIMPLEDEBT:
                name = "SIMPLEDEBT";
                break;
            case MODEL_TYPE_SENDER:
                name = "SENDER";
                break;
            case MODEL_TYPE_DATE_RANGE:
                name = "RANGE";
                break;
        }
        return name;
    }

    public static IAbstractModel createModelByType(int modelType) {
        if (modelType == IAbstractModel.MODEL_TYPE_CABBAGE) {
            return new Cabbage();
        }
        if (modelType == IAbstractModel.MODEL_TYPE_ACCOUNT) {
            return new Account();
        }
        if (modelType == IAbstractModel.MODEL_TYPE_PROJECT) {
            return new Project();
        }
        if (modelType == IAbstractModel.MODEL_TYPE_DEPARTMENT) {
            return new Department();
        }
        if (modelType == IAbstractModel.MODEL_TYPE_LOCATION) {
            return new Location();
        }
        if (modelType == IAbstractModel.MODEL_TYPE_CATEGORY) {
            return new Category();
        }
        if (modelType == IAbstractModel.MODEL_TYPE_PAYEE) {
            return new Payee();
        }
        if (modelType == IAbstractModel.MODEL_TYPE_SIMPLEDEBT) {
            return new SimpleDebt();
        }
        if (modelType == IAbstractModel.MODEL_TYPE_CREDIT) {
            return new Credit();
        }
        if (modelType == IAbstractModel.MODEL_TYPE_TRANSACTION) {
            return new Transaction(-1);
        }
        if (modelType == IAbstractModel.MODEL_TYPE_TEMPLATE) {
            return new Template();
        }
        if (modelType == IAbstractModel.MODEL_TYPE_SMSMARKER) {
            return new SmsMarker();
        }
        if (modelType == IAbstractModel.MODEL_TYPE_SENDER) {
            return new Sender();
        }
        if (modelType == IAbstractModel.MODEL_TYPE_SMS) {
            return new Sms();
        }
        if (modelType == IAbstractModel.MODEL_TYPE_PRODUCT) {
            return new Product();
        }
        if (modelType == IAbstractModel.MODEL_TYPE_PRODUCT_ENTRY) {
            return new ProductEntry();
        }
        return new BaseModel();
    }

    @Override
    public String toString() {
        return String.format("id - %s, FBID = %s, TS = %s, Deleted = %s", String.valueOf(mID), mFBID, String.valueOf(mTS), String.valueOf(mDeleted));
    }

    @Override
    public ContentValues getCV() {
        ContentValues values = new ContentValues();
        return DBHelper.addSyncDataToCV(values, this);
    }

    @Override
    public long getParentID() {
        return -1;
    }

    @Override
    public void setParentID(long parentID) {

    }

    @Override
    public boolean isExpanded() {
        return false;
    }

    @Override
    public void setExpanded(boolean expanded) {

    }

    @Override
    public String getName() {
        return mName;
    }

    @Override
    public void setName(String name) {
        mName = name;
    }

    @Override
    public String getFullName() {
        return mFullName;
    }

    @Override
    public void setFullName(String fullName) {
        mFullName = fullName;
    }

    @Override
    public int getColor() {
        return Color.TRANSPARENT;
    }

//    @Override
//    public void setOrderNum(int orderNum) {
//
//    }
//
//    @Override
//    public int getOrderNum() {
//        return 0;
//    }

    public void setSortType(int sortType) {
        mSortType = sortType;
    }

    @Override
    public String getLogTransactionsField() {
        return null;
    }

    @Override
    public int compareTo(@NonNull IAbstractModel another) {
        BigDecimal v1;
        BigDecimal v2;
        switch (mSortType) {
            case SORT_BY_INCOME_AND_EXPENSE :
                v1 = mIncome.add(mExpense);
                v2 = another.getIncome().add(another.getExpense());
                return v1.compareTo( v2 );
            case SORT_BY_INCOME_MINUS_EXPENSE :
                v1 = mIncome.subtract(mExpense);
                v2 = another.getIncome().subtract(another.getExpense());
                return v1.compareTo( v2 );
            case SORT_BY_INCOME :
                v1 = mIncome;
                v2 = another.getIncome();
                return v1.compareTo( v2 );
            case SORT_BY_EXPENSE :
                v1 = mExpense;
                v2 = another.getExpense();
                return v1.compareTo( v2 );
            case SORT_BY_NAME :
                return getName().compareTo(another.getName());
            default:
                return 0;
        }
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeLong(this.mID);
        dest.writeString(this.mName);
        dest.writeString(this.mFullName);
        dest.writeString(this.mFBID);
        dest.writeLong(this.mTS);
        dest.writeByte(this.mDeleted ? (byte) 1 : (byte) 0);
        dest.writeByte(this.mDirty ? (byte) 1 : (byte) 0);
        dest.writeString(this.mLastEdited);
        dest.writeByte(this.mSelected ? (byte) 1 : (byte) 0);
        dest.writeSerializable(this.mIncome);
        dest.writeSerializable(this.mExpense);
        dest.writeInt(this.mSortType);
        dest.writeString(this.TAG);
    }

    protected BaseModel(Parcel in) {
        this.mID = in.readLong();
        this.mName = in.readString();
        this.mFullName = in.readString();
        this.mFBID = in.readString();
        this.mTS = in.readLong();
        this.mDeleted = in.readByte() != 0;
        this.mDirty = in.readByte() != 0;
        this.mLastEdited = in.readString();
        this.mSelected = in.readByte() != 0;
        this.mIncome = (BigDecimal) in.readSerializable();
        this.mExpense = (BigDecimal) in.readSerializable();
        this.mSortType = in.readInt();
        this.TAG = in.readString();
    }

    public static final Creator<BaseModel> CREATOR = new Creator<BaseModel>() {
        @Override
        public BaseModel createFromParcel(Parcel source) {
            return new BaseModel(source);
        }

        @Override
        public BaseModel[] newArray(int size) {
            return new BaseModel[size];
        }
    };
}
