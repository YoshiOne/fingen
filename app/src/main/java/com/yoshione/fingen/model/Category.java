package com.yoshione.fingen.model;

import android.content.ContentValues;
import android.os.Parcel;

import com.yoshione.fingen.dao.CategoriesDAO;
import com.yoshione.fingen.dao.TransactionsDAO;
import com.yoshione.fingen.interfaces.IAbstractModel;
import com.yoshione.fingen.interfaces.IOrderable;

public class Category extends BaseModel implements IAbstractModel, IOrderable {

    private int mColor;
    private long mParentID;
    private int mOrderNum;
    private boolean mExpanded;
    private BudgetForCategory mBudget;

    public Category() {
        super();
        mParentID = -1;
        mExpanded = true;
    }

    public Category(long id) {
        super(id);
    }

    public Category(long mID, String mName, Category mParentCategory, int mOrderNum, boolean mExpanded) {
        super();
        setID(mID);
        setName(mName);
        this.mParentID = mParentCategory.getID();
        this.mOrderNum = mOrderNum;
        this.mExpanded = mExpanded;
    }

    public BudgetForCategory getBudget() {
        return mBudget;
    }

    public void setBudget(BudgetForCategory mBudget) {
        this.mBudget = mBudget;
    }

    public boolean isExpanded() {
        return mExpanded;
    }

    public void setExpanded(boolean mExpanded) {
        this.mExpanded = mExpanded;
    }

    @Override
    public long getParentID() {
        return mParentID;
    }

    @Override
    public void setParentID(long parentID) {
        mParentID = parentID;
    }

    public int getColor() {
        return mColor;
    }

    public void setColor(int mColor) {
        this.mColor = mColor;
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
    public String toString() {
        return getName();
    }

    @Override
    public String getSearchString() {
        return getName();
    }

    @Override
    public long getID() {
        return super.getID();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        super.writeToParcel(dest, flags);
        dest.writeInt(this.mColor);
        dest.writeLong(this.mParentID);
        dest.writeInt(this.mOrderNum);
        dest.writeByte(this.mExpanded ? (byte) 1 : (byte) 0);
    }

    protected Category(Parcel in) {
        super(in);
        this.mColor = in.readInt();
        this.mParentID = in.readLong();
        this.mOrderNum = in.readInt();
        this.mExpanded = in.readByte() != 0;
    }

    public static final Creator<Category> CREATOR = new Creator<Category>() {
        @Override
        public Category createFromParcel(Parcel source) {
            return new Category(source);
        }

        @Override
        public Category[] newArray(int size) {
            return new Category[size];
        }
    };

    @Override
    public ContentValues getCV() {
        ContentValues values = super.getCV();
        values.put(CategoriesDAO.COL_NAME, getName());
        values.put(CategoriesDAO.COL_COLOR, String.format("#%06X", (0xFFFFFF & getColor())));
        values.put(CategoriesDAO.COL_PARENT_ID, mParentID);
        values.put(CategoriesDAO.COL_ORDER_NUMBER, getOrderNum());
        return values;
    }

    @Override
    public String getLogTransactionsField() {
        return TransactionsDAO.COL_CATEGORY;
    }
}
