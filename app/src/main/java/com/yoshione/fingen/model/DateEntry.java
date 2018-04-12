package com.yoshione.fingen.model;

import java.math.BigDecimal;
import java.util.Date;

/**
 * Created by slv on 22.06.2016.
 *
 */

public class DateEntry {
    private final Date mDate;
    private final BigDecimal mIncome;
    private final BigDecimal mExpense;

    public DateEntry(Date date, BigDecimal income, BigDecimal expense) {
        mDate = date;
        mIncome = income;
        mExpense = expense;
    }

    public Date getDate() {
        return mDate;
    }

    public BigDecimal getIncome() {
        return mIncome;
    }

    public BigDecimal getExpense() {
        return mExpense;
    }
}
