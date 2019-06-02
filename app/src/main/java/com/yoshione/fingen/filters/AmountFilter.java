/*
 * Copyright (c) 2015.
 */

package com.yoshione.fingen.filters;

import android.os.Parcel;
import android.os.Parcelable;
import android.text.TextUtils;

import com.yoshione.fingen.DBHelper;
import com.yoshione.fingen.interfaces.IAbstractModel;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.HashSet;

/**
 * Created by slv on 05.11.2015.
 * a
 */
public class AmountFilter extends AbstractFilter implements Parcelable {
    public static final int TRANSACTION_TYPE_TRANSACTION = 1;
    public static final int TRANSACTION_TYPE_TRANSFER = 2;
    public static final int TRANSACTION_TYPE_BOTH = 0;
    private Boolean mEnabled;
    private BigDecimal mMinAmount;
    private BigDecimal mMaxAmount;
    private boolean mIncome;
    private boolean mExpense;
    private int mType;

    private long mId;
    private boolean mInverted;

    @Override
    public boolean isInverted() {
        return mInverted;
    }

    @Override
    public void setInverted(boolean inverted) {
        mInverted = inverted;
    }

    @Override
    public long getId() {
        return mId;
    }

    @Override
    public void setId(long id) {
        mId = id;
    }

    public AmountFilter(long id) {
        mId = id;
        mEnabled = true;
        mIncome = true;
        mExpense = true;
        mMinAmount = new BigDecimal(BigInteger.ZERO);
        mMaxAmount = new BigDecimal(Integer.MAX_VALUE);
        mType = TRANSACTION_TYPE_BOTH;
    }

    @Override
    public Boolean getEnabled() {
        return mEnabled;
    }

    @Override
    public void setEnabled(Boolean enabled) {
        mEnabled = enabled;
    }

    @Override
    public int getModelType() {
        return IAbstractModel.MODEL_TYPE_AMOUNT_FILTER;
    }

    @Override
    public String getSelectionString(HashSet<Long> allAccountIDS) {
        if (!mEnabled) return "";

        String ids = TextUtils.join(",", allAccountIDS);

        String transactionIncome =
                String.format("DestAccount < 0 AND SrcAccount IN (%s) AND Amount BETWEEN %s AND %s",
                        ids, mMinAmount.abs().toString(), mMaxAmount.abs().toString());
        String transactionExpense =
                String.format("DestAccount < 0 AND SrcAccount IN (%s) AND Amount BETWEEN %s AND %s",
                        ids, mMaxAmount.abs().negate().toString(), mMinAmount.negate().toString());
        String transferIncome =
                String.format("DestAccount IN (%s) AND Amount BETWEEN %s AND %s",
                        ids, mMaxAmount.abs().negate().toString(), mMinAmount.negate().toString());
        String transferExpense =
                String.format("DestAccount >= 0 AND SrcAccount IN (%s) AND Amount BETWEEN %s AND %s",
                        ids, mMaxAmount.abs().negate().toString(), mMinAmount.abs().negate().toString());

        String condition = "";
        switch (mType) {
            case TRANSACTION_TYPE_BOTH:
                if (mIncome & !mExpense) {
                    condition = String.format("(%s) OR (%s)", transactionIncome, transferIncome);
                } else if (!mIncome & mExpense) {
                    condition = String.format("(%s) OR (%s)", transactionExpense, transferExpense);
                } else if (mIncome & mExpense)
                    condition = String.format("(%s) OR (%s) OR (%s) OR (%s)", transactionIncome, transferIncome, transactionExpense, transferExpense);
                break;
            case TRANSACTION_TYPE_TRANSACTION:
                if (mIncome & !mExpense) {
                    condition = String.format("(%s)", transactionIncome);
                } else if (!mIncome & mExpense) {
                    condition = String.format("(%s)", transactionExpense);
                } else if (mIncome & mExpense)
                    condition = String.format("(%s) OR (%s)", transactionIncome, transactionExpense);
                break;
            case TRANSACTION_TYPE_TRANSFER:
                if (mIncome & !mExpense) {
                    condition = String.format("(%s)", transferIncome);
                } else if (!mIncome & mExpense) {
                    condition = String.format("(%s)", transferExpense);
                } else if (mIncome & mExpense)
                    condition = String.format("(%s) OR (%s)", transferIncome, transferExpense);
                break;
        }
        if (mInverted && !condition.isEmpty()) {
            condition = String.format("NOT(%s)", condition);
        }

        return condition;
    }

    public String getSelectionString1() {
        if (getEnabled()) {
            String pos;
            String neg;
            String type;
            if (mIncome) {
                pos = String.format("%s BETWEEN %s AND %s", DBHelper.C_LOG_TRANSACTIONS_AMOUNT, mMinAmount.abs().toString(), mMaxAmount.abs().toString());
            } else {
                pos = "";
            }

            if (mExpense) {
                neg = String.format("%s BETWEEN %s AND %s", DBHelper.C_LOG_TRANSACTIONS_AMOUNT, mMaxAmount.abs().
                        multiply(new BigDecimal(-1)).toString(), mMinAmount.multiply(new BigDecimal(-1)).toString());
            } else {
                neg = "";
            }

            type = "";
            switch (mType) {
                case TRANSACTION_TYPE_BOTH: {
                    type = "";
                    break;
                }
                case TRANSACTION_TYPE_TRANSACTION: {
                    type = String.format("%s < 0", DBHelper.C_LOG_TRANSACTIONS_DESTACCOUNT);
                    break;
                }

                case TRANSACTION_TYPE_TRANSFER: {
                    type = String.format("%s >= 0", DBHelper.C_LOG_TRANSACTIONS_DESTACCOUNT);
                    break;
                }
            }

            boolean or = !pos.isEmpty() & !neg.isEmpty();

            String amount;
            if (or) {
                amount = String.format("(%s) OR (%s)", pos, neg);
            } else {
                amount = String.format("%s%s", pos, neg);
            }

            boolean and = !amount.isEmpty() & !type.isEmpty();

            String condition;
            if (and) {
                condition = String.format("((%s) AND (%s))", amount, type);
            } else {
                condition = String.format("(%s%s)", amount, type);
            }

            if (mInverted) {
                condition = String.format("NOT(%s)", condition);
            }

            return condition;
        } else {
            return "";
        }
    }

    @Override
    public String saveToString() {
        return
                String.valueOf(mMinAmount.doubleValue()) + "@" +
                        String.valueOf(mMaxAmount.doubleValue()) + "@" +
                        String.valueOf(mIncome) + "@" +
                        String.valueOf(mExpense) + "@" +
                        String.valueOf(mType);
    }

    @Override
    public boolean loadFromString(String s) {
        String strings[] = s.split("@");
        if (strings.length != 5) {
            return false;
        }
        try {
            mMinAmount = new BigDecimal(strings[0]);
            mMaxAmount = new BigDecimal(strings[1]);
            mIncome = Boolean.valueOf(strings[2]);
            mExpense = Boolean.valueOf(strings[3]);
            mType = Integer.valueOf(strings[4]);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    @Override
    public HashSet<Long> getIDsSet() {
        return new HashSet<>();
    }

    public BigDecimal getmMinAmount() {
        return mMinAmount;
    }

    public void setmMinAmount(BigDecimal mMinAmount) {
        this.mMinAmount = mMinAmount.abs();
        compareAmounts();
    }

    public BigDecimal getmMaxAmount() {
        return mMaxAmount;
    }

    public void setmMaxAmount(BigDecimal mMaxAmount) {
        this.mMaxAmount = mMaxAmount.abs();
        compareAmounts();
    }

    public boolean ismIncome() {
        return mIncome;
    }

    public void setmIncome(boolean mIncome) {
        this.mIncome = mIncome;
    }

    public boolean ismOutcome() {
        return mExpense;
    }

    public void setmOutcome(boolean mOutcome) {
        this.mExpense = mOutcome;
    }

//    public int ismTransfer() {
//        return mType;
//    }

    public void setmTransfer(int mTransfer) {
        this.mType = mTransfer;
    }

    public int getType() {
        return mType;
    }

    private void compareAmounts() {
        if (mMaxAmount.compareTo(mMinAmount) < 0) {
            BigDecimal temp = new BigDecimal(mMaxAmount.doubleValue());
            mMaxAmount = new BigDecimal(mMinAmount.doubleValue());
            mMinAmount = temp;
        }
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeValue(this.mEnabled);
        dest.writeSerializable(this.mMinAmount);
        dest.writeSerializable(this.mMaxAmount);
        dest.writeByte(this.mIncome ? (byte) 1 : (byte) 0);
        dest.writeByte(this.mExpense ? (byte) 1 : (byte) 0);
        dest.writeInt(this.mType);
        dest.writeLong(this.mId);
        dest.writeByte(this.mInverted ? (byte) 1 : (byte) 0);
    }

    protected AmountFilter(Parcel in) {
        this.mEnabled = (Boolean) in.readValue(Boolean.class.getClassLoader());
        this.mMinAmount = (BigDecimal) in.readSerializable();
        this.mMaxAmount = (BigDecimal) in.readSerializable();
        this.mIncome = in.readByte() != 0;
        this.mExpense = in.readByte() != 0;
        this.mType = in.readInt();
        this.mId = in.readLong();
        this.mInverted = in.readByte() != 0;
    }

    public static final Creator<AmountFilter> CREATOR = new Creator<AmountFilter>() {
        @Override
        public AmountFilter createFromParcel(Parcel source) {
            return new AmountFilter(source);
        }

        @Override
        public AmountFilter[] newArray(int size) {
            return new AmountFilter[size];
        }
    };
}
