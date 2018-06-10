package com.yoshione.fingen.model;

import android.os.Parcel;
import android.os.Parcelable;

import java.math.BigDecimal;
import java.util.Date;

public class DateRangeSum {
    private Date mDateStart;
    private Date mDateEnd;
    private BigDecimal mSum;

    public DateRangeSum(Date dateStart, Date dateEnd, BigDecimal sum) {
        mDateStart = dateStart;
        mDateEnd = dateEnd;
        mSum = sum;
    }

    public Date getDateStart() {
        return mDateStart;
    }

    public void setDateStart(Date dateStart) {
        mDateStart = dateStart;
    }

    public Date getDateEnd() {
        return mDateEnd;
    }

    public void setDateEnd(Date dateEnd) {
        mDateEnd = dateEnd;
    }

    public BigDecimal getSum() {
        return mSum;
    }

    public void setSum(BigDecimal sum) {
        mSum = sum;
    }

}
