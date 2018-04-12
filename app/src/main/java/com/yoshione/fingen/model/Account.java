package com.yoshione.fingen.model;

import android.content.ContentValues;
import android.graphics.Color;
import android.os.Parcel;
import android.support.annotation.NonNull;

import com.yoshione.fingen.DBHelper;
import com.yoshione.fingen.interfaces.IAbstractModel;
import com.yoshione.fingen.interfaces.IOrderable;

import java.math.BigDecimal;
import java.math.RoundingMode;


/**
 * Created by slv on 12.08.2015.
 *
 */
public class Account extends BaseModel implements IAbstractModel, IOrderable {

    public static final String TAG = "com.yoshione.fingen.Model.Account";
//    public static final int SORT_TYPE_CUSTOM = 0;
//    private static final int SORT_TYPE_DATE_CREATION = 1;
//    private static final int SORT_TYPE_DATE_LASTOP = 2;
//    private static final int SORT_TYPE_BALANCE = 3;
//    private static final int SORT_TYPE_TYPE = 4;
//    private static final int SORT_TYPE_ALPHABET = 5;

    private static final int SORT_ORDER_ASC = 0;
//    private long mId = -1;
    private String mName;
    private long mCabbageId;
    private String mEmitent;
    private String mComment;
    private BigDecimal mStartBalance;
    private BigDecimal mCreditLimit;
    private BigDecimal mIncome;
    private BigDecimal mExpense;
    private AccountType mAccountType;
    private int mLast4Digits;
    private boolean mIsClosed;
    private int mOrder;
    private int mSortOrder = SORT_ORDER_ASC;

    public Account() {
//        this.mId = -1;
        super();
        this.mName = "";
        this.mCabbageId = -1;
        this.mEmitent = "";
        this.mComment = "";
        this.mStartBalance = BigDecimal.ZERO;
        this.mIncome = BigDecimal.ZERO;
        this.mExpense = BigDecimal.ZERO;
        this.mAccountType = AccountType.atCash;
        this.mLast4Digits = 0;
        this.mIsClosed = false;
        this.mOrder = 0;
        this.mCreditLimit = BigDecimal.ZERO;
    }

    public Account(long id) {
        super(id);
    }

    public Account(long mid, String mName, long mCabbageId, String mEmitent, String mComment, BigDecimal mStartBalance,
                   AccountType mAccountType, int mLast4Digits, boolean mIsClosed, BigDecimal mIncome, BigDecimal mExpense,
                   int mOrder, BigDecimal mCreditLimit) {
        super();
        setID(mid);
        this.mName = mName;
        this.mCabbageId = mCabbageId;
        this.mEmitent = mEmitent;
        this.mComment = mComment;
        this.mStartBalance = mStartBalance;
        this.mAccountType = mAccountType;
        this.mLast4Digits = mLast4Digits;
        this.mIsClosed = mIsClosed;
        this.mIncome = mIncome;
        this.mExpense = mExpense;
        this.mOrder = mOrder;
        this.mCreditLimit = mCreditLimit;
    }

    @Override
    public String toString() {
        return mName;
    }

    @Override
    public long getID() {
        return super.getID();
    }

    @Override
    public int compareTo(@NonNull IAbstractModel another) {
        int result;
        Account anotherAccount = (Account) another;
        int desc = mSortOrder == SORT_ORDER_ASC ? 1 : -1;
        switch (mSortType) {
            case SORT_BY_ACCOUNT_CUSTOM:
                result = mOrder - anotherAccount.mOrder;
                desc = 1;
                break;
            case SORT_BY_ACCOUNT_DATE_CREATION:
                result = (int) (getID() - anotherAccount.getID());
                break;
            case SORT_BY_ACCOUNT_DATE_LASTOP:
                result = mOrder - anotherAccount.mOrder;
                break;
            case SORT_BY_ACCOUNT_BALANCE:
                result = getCurrentBalance().compareTo(anotherAccount.getCurrentBalance());
                break;
            case SORT_BY_ACCOUNT_TYPE:
                result = mAccountType.ordinal() - anotherAccount.mAccountType.ordinal();
                break;
            default:
                return super.compareTo(another);
        }
        return result * desc;
    }

    public BigDecimal getCreditLimit() {
        return mCreditLimit;
    }

    public void setCreditLimit(BigDecimal mCreditLimit) {
        this.mCreditLimit = mCreditLimit;
    }

    public void setSortOrder(int mSortOrder) {
        this.mSortOrder = mSortOrder;
    }

    public int getOrder() {
        return mOrder;
    }

    public void setOrder(int mOrder) {
        this.mOrder = mOrder;
    }

    public boolean getIsClosed() {
        return mIsClosed;
    }

    public void setIsClosed(boolean mIsClosed) {
        this.mIsClosed = mIsClosed;
    }

    public String getName() {
        return mName;
    }

    @Override
    public String getFullName() {
        return mName;
    }

    public void setName(String mName) {
        this.mName = mName;
    }

    public long getCabbageId() {
        return mCabbageId;
    }

    public void setCabbageId(long mCabbageId) {
        this.mCabbageId = mCabbageId;
    }

    public String getEmitent() {
        return mEmitent;
    }

    public void setEmitent(String mEmitent) {
        this.mEmitent = mEmitent;
    }

    public String getComment() {
        return mComment;
    }

    public void setComment(String mComment) {
        this.mComment = mComment;
    }

    public BigDecimal getStartBalance() {
        return mStartBalance;
    }

    public void setStartBalance(BigDecimal mStartBalance) {
        this.mStartBalance = mStartBalance;
    }

    public AccountType getAccountType() {
        return mAccountType;
    }

    public void setAccountType(AccountType mAccountType) {
        this.mAccountType = mAccountType;
    }

    public int getLast4Digits() {
        return mLast4Digits;
    }

    public void setLast4Digits(int mLast4Digits) {
        this.mLast4Digits = mLast4Digits;
    }

    public BigDecimal getIncome() {
        return mIncome;
    }

    public void setIncome(BigDecimal mIncome) {
        this.mIncome = mIncome;
    }

    public BigDecimal getExpense() {
        return mExpense;
    }

    public void setExpense(BigDecimal mExpense) {
        this.mExpense = mExpense;
    }

    public BigDecimal getCurrentBalance() {
        return getIncome().add(getExpense().add(getStartBalance()));
    }

    public int getCreditLimitUsage() {
        if (getCurrentBalance().compareTo(BigDecimal.ZERO) >= 0) {
            return 100;
        } else {
            BigDecimal m1 = new BigDecimal(-1);
            BigDecimal m100 = new BigDecimal(100);
            BigDecimal x1 = getCurrentBalance().multiply(m1);
            BigDecimal x2 = getCreditLimit().multiply(m1);
            BigDecimal res = m100.subtract(x1.multiply(m100).divide(x2, RoundingMode.HALF_EVEN));
            if (res.compareTo(BigDecimal.ZERO) < 0) {
                return 0;
            } else {
                return (int) Math.round(res.doubleValue());
            }
        }
    }

    public enum AccountType {atCash, atAccount, atDebtCard, atCreditCard, atActive, atPassive, atOther}

    public boolean isValid() {
        return mCabbageId >= 0 & !mName.isEmpty();
    }

    @Override
    public String getSearchString() {
        return mName;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        super.writeToParcel(dest, flags);
        dest.writeString(this.mName);
        dest.writeLong(this.mCabbageId);
        dest.writeString(this.mEmitent);
        dest.writeString(this.mComment);
        dest.writeSerializable(this.mStartBalance);
        dest.writeSerializable(this.mCreditLimit);
        dest.writeSerializable(this.mIncome);
        dest.writeSerializable(this.mExpense);
        dest.writeInt(this.mAccountType == null ? -1 : this.mAccountType.ordinal());
        dest.writeInt(this.mLast4Digits);
        dest.writeByte(this.mIsClosed ? (byte) 1 : (byte) 0);
        dest.writeInt(this.mOrder);
        dest.writeInt(this.mSortOrder);
    }

    protected Account(Parcel in) {
        super(in);
        this.mName = in.readString();
        this.mCabbageId = in.readLong();
        this.mEmitent = in.readString();
        this.mComment = in.readString();
        this.mStartBalance = (BigDecimal) in.readSerializable();
        this.mCreditLimit = (BigDecimal) in.readSerializable();
        this.mIncome = (BigDecimal) in.readSerializable();
        this.mExpense = (BigDecimal) in.readSerializable();
        int tmpMAccountType = in.readInt();
        this.mAccountType = tmpMAccountType == -1 ? null : AccountType.values()[tmpMAccountType];
        this.mLast4Digits = in.readInt();
        this.mIsClosed = in.readByte() != 0;
        this.mOrder = in.readInt();
        this.mSortOrder = in.readInt();
    }

    public static final Creator<Account> CREATOR = new Creator<Account>() {
        @Override
        public Account createFromParcel(Parcel source) {
            return new Account(source);
        }

        @Override
        public Account[] newArray(int size) {
            return new Account[size];
        }
    };

    @Override
    public ContentValues getCV() {
        ContentValues values = super.getCV();

        values.put(DBHelper.C_REF_ACCOUNTS_NAME, getName());
        values.put(DBHelper.C_REF_ACCOUNTS_CURRENCY, getCabbageId());
        values.put(DBHelper.C_REF_ACCOUNTS_EMITENT, getEmitent());
        values.put(DBHelper.C_REF_ACCOUNTS_COMMENT, getComment());
        values.put(DBHelper.C_REF_ACCOUNTS_STARTBALANCE, getStartBalance().doubleValue());
        values.put(DBHelper.C_REF_ACCOUNTS_TYPE, getAccountType().ordinal());
        values.put(DBHelper.C_REF_ACCOUNTS_LAST4DIGITS, getLast4Digits());
        values.put(DBHelper.C_REF_ACCOUNTS_ISCLOSED, getIsClosed() ? 1 : 0);
        values.put(DBHelper.C_REF_ACCOUNTS_ORDER, getOrder());
        values.put(DBHelper.C_REF_ACCOUNTS_CREDITLIMIT, getCreditLimit().doubleValue());
        return values;
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
    public int getColor() {
        return Color.TRANSPARENT;
    }

    @Override
    public void setOrderNum(int orderNum) {

    }

    @Override
    public int getOrderNum() {
        return 0;
    }

    @Override
    public String getLogTransactionsField() {
        return DBHelper.C_LOG_TRANSACTIONS_SRCACCOUNT;
    }
}
