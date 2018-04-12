package com.yoshione.fingen.utils;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.VectorDrawable;
import android.support.v4.content.ContextCompat;

import com.yoshione.fingen.R;
import com.yoshione.fingen.model.Transaction;

/**
 * Created by Leonid on 22.01.2017.
 *
 */

public class AmountColorizer {
    private final int mColorPositive;
    private final int mColorNegative;
    private final int mColorTransfer;
    private final int mColorInactive;

    private final Drawable iconIncome;
    private final Drawable iconExpense;
    private final Drawable iconTransfer;
    private final Drawable iconClosed;
    private final Drawable iconZero;

    public AmountColorizer(Context context) {
        mColorPositive = ContextCompat.getColor(context, R.color.positive_color);
        mColorNegative = ContextCompat.getColor(context, R.color.negative_color);
        mColorTransfer = ContextCompat.getColor(context, R.color.transfer_color);
        mColorInactive = ContextCompat.getColor(context, R.color.light_gray_text);

        iconIncome = context.getDrawable(R.drawable.ic_income);
        iconExpense = context.getDrawable(R.drawable.ic_expense);
        iconTransfer = context.getDrawable(R.drawable.ic_transfer);
        iconClosed = context.getDrawable(R.drawable.ic_lock_gray);
        iconZero = context.getDrawable(R.drawable.ic_close_gray);
    }

    public Drawable getTransactionIcon(int transactionType) {
        switch (transactionType) {
            case Transaction.TRANSACTION_TYPE_INCOME:
                return iconIncome;
            case Transaction.TRANSACTION_TYPE_EXPENSE:
                return iconExpense;
            case Transaction.TRANSACTION_TYPE_TRANSFER:
                return iconTransfer;
            default:
                return new VectorDrawable();
        }
    }

    public int getTransactionColor(int transactionType) {
        switch (transactionType) {
            case Transaction.TRANSACTION_TYPE_INCOME:
                return mColorPositive;
            case Transaction.TRANSACTION_TYPE_EXPENSE:
                return mColorNegative;
            case Transaction.TRANSACTION_TYPE_TRANSFER:
                return mColorTransfer;
            default:
                return mColorInactive;
        }
    }

    public int getColorPositive() {
        return mColorPositive;
    }

    public int getColorNegative() {
        return mColorNegative;
    }

    public int getColorInactive() {
        return mColorInactive;
    }

    public Drawable getIconIncome() {
        return iconIncome;
    }

    public Drawable getIconExpense() {
        return iconExpense;
    }

    public Drawable getIconTransfer() {
        return iconTransfer;
    }

    public Drawable getIconClosed() {
        return iconClosed;
    }

    public Drawable getIconZero() {
        return iconZero;
    }
}
