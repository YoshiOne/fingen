/*
 * Copyright (c) 2015.
 */

package com.yoshione.fingen.filters;

import android.content.Context;
import android.os.Parcel;
import android.os.Parcelable;

import com.yoshione.fingen.DBHelper;
import com.yoshione.fingen.interfaces.IAbstractModel;
import com.yoshione.fingen.utils.LocaleUtils;

import java.util.Calendar;
import java.util.Date;
import java.util.HashSet;

/**
 * Created by slv on 05.11.2015.
 *
 */
public class DateRangeFilter extends AbstractFilter implements Parcelable {
    public static final int DATE_RANGE_DAY = Calendar.DAY_OF_MONTH;
    public static final int DATE_RANGE_WEEK = Calendar.DAY_OF_WEEK;
    public static final int DATE_RANGE_MONTH = Calendar.MONTH;
    public static final int DATE_RANGE_YEAR = Calendar.YEAR;

    public static final int DATE_RANGE_MODIFIER_CURRENT = 0;
    public static final int DATE_RANGE_MODIFIER_LAST = 1;
    public static final int DATE_RANGE_MODIFIER_CURRENTAND_LAST = 2;

    private Boolean mEnabled;
    private Date mStartDate;
    private Date mEndDate;

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


    public DateRangeFilter(long id, Context context){
        mId = id;
        mEnabled = true;
        setRange(DATE_RANGE_MONTH,DATE_RANGE_MODIFIER_CURRENT, context);
    }

    public void setRangeByMonth(int year, int month, Context context) {
        Calendar c = Calendar.getInstance(LocaleUtils.getLocale(context));
        c.set(year,month,1,0,0,0);
        mStartDate = c.getTime();
        c.set(year,month,c.getActualMaximum(Calendar.DAY_OF_MONTH),23,59,59);
        mEndDate = c.getTime();
    }

    public void setRange(int range, int modifier, Context context){
        mStartDate = getRangeStart(range,modifier, context);
        mEndDate = getRangeEnd(range,modifier, context);
    }

    public static Date getRangeStart(int inputRange, int modifier, Context context){
        int range = Calendar.HOUR_OF_DAY;
        switch (inputRange){
            case DATE_RANGE_DAY : {
                range = Calendar.HOUR_OF_DAY;
                break;
            }
            case DATE_RANGE_MONTH : {
                range = Calendar.DAY_OF_MONTH;
                break;
            }
            case DATE_RANGE_YEAR : {
                range = Calendar.MONTH;
                break;
            }
            case DATE_RANGE_WEEK : {
                range = Calendar.DAY_OF_WEEK;
                break;
            }
        }

        Calendar c = Calendar.getInstance(LocaleUtils.getLocale(context));
        if (range != Calendar.DAY_OF_WEEK) {
            if (range == Calendar.DAY_OF_MONTH) {
                c.set(range, 1);
            } else {
                c.set(range, 0);
            }

            if (range < Calendar.DAY_OF_MONTH) {
                c.set(Calendar.DAY_OF_MONTH, 1);
            }
        } else {
            c.set(Calendar.DAY_OF_WEEK, c.getFirstDayOfWeek());
        }

        c.set(Calendar.HOUR_OF_DAY, 0); //anything 0 - 23
        c.set(Calendar.MINUTE, 0);
        c.set(Calendar.SECOND, 0);
        c.set(Calendar.MILLISECOND, 0);

        switch (modifier) {
            case DATE_RANGE_MODIFIER_CURRENTAND_LAST :
            case DATE_RANGE_MODIFIER_LAST : {
                if (range != Calendar.DAY_OF_WEEK) {
                    c.add(inputRange, -1);
                } else {
                    c.add(Calendar.DAY_OF_WEEK,-7);
                }
                break;
            }
        }
        return c.getTime();
    }

    public static Date getRangeEnd(int inputRange, int modifier, Context context){
        int range = Calendar.HOUR_OF_DAY;
        switch (inputRange){
            case DATE_RANGE_DAY : {
                range = Calendar.HOUR_OF_DAY;
                break;
            }
            case DATE_RANGE_MONTH : {
                range = Calendar.DAY_OF_MONTH;
                break;
            }
            case DATE_RANGE_YEAR : {
                range = Calendar.MONTH;
                break;
            }
            case DATE_RANGE_WEEK : {
                range = Calendar.DAY_OF_WEEK;
                break;
            }
        }

        Calendar c = Calendar.getInstance(LocaleUtils.getLocale(context));

        switch (modifier) {
            case DATE_RANGE_MODIFIER_LAST : {
                if (range != Calendar.DAY_OF_WEEK) {
                    c.add(inputRange, -1);
                } else {
                    c.add(Calendar.DAY_OF_WEEK,-7);
                }
                break;
            }
        }

        if (range != Calendar.DAY_OF_WEEK) {
            c.set(range, c.getActualMaximum(range));

            if (range < Calendar.DAY_OF_MONTH) {
                c.set(Calendar.DAY_OF_MONTH, c.getActualMaximum(Calendar.DAY_OF_MONTH));
            }
        } else {
            c.set(Calendar.DAY_OF_WEEK, c.getFirstDayOfWeek());
            c.add(Calendar.DAY_OF_WEEK, 6);
        }

        c.set(Calendar.HOUR_OF_DAY, 23); //anything 0 - 23
        c.set(Calendar.MINUTE, 59);
        c.set(Calendar.SECOND, 59);
        c.set(Calendar.MILLISECOND, 999);
        return c.getTime();
    }

    @Override
    public Boolean getEnabled() {
        return mEnabled;
    }

    public Date getmStartDate() {
        return mStartDate;
    }

    public void setmStartDate(Date mStartDate) {
        this.mStartDate = mStartDate;
    }

    public Date getmEndDate() {
        return mEndDate;
    }

    public void setmEndDate(Date mEndDate) {
        this.mEndDate = mEndDate;
    }

    @Override
    public void setEnabled(Boolean enabled) {
        mEnabled = enabled;
    }

    @Override
    public int getModelType() {
        return IAbstractModel.MODEL_TYPE_DATE_RANGE;
    }

    @Override
    public String getSelectionString() {
        if (getEnabled()) {
            String condition = String.format("(%s >= %s AND %s <= %s)", DBHelper.C_LOG_TRANSACTIONS_DATETIME, String.valueOf(mStartDate.getTime()),
                    DBHelper.C_LOG_TRANSACTIONS_DATETIME, String.valueOf(mEndDate.getTime()));
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
        return String.valueOf(mStartDate.getTime()) + "@" + String.valueOf(mEndDate.getTime());
    }

    @Override
    public boolean loadFromString(String s) {
        String strings[] = s.split("@");
        if (strings.length != 2) {
            return false;
        }
        try {
            mStartDate = new Date(Long.valueOf(strings[0]));
            mEndDate = new Date(Long.valueOf(strings[1]));
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    @Override
    public HashSet<Long> getIDsSet() {
        return new HashSet<>();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeValue(this.mEnabled);
        dest.writeLong(this.mStartDate != null ? this.mStartDate.getTime() : -1);
        dest.writeLong(this.mEndDate != null ? this.mEndDate.getTime() : -1);
        dest.writeLong(this.mId);
        dest.writeByte(this.mInverted ? (byte) 1 : (byte) 0);
    }

    protected DateRangeFilter(Parcel in) {
        this.mEnabled = (Boolean) in.readValue(Boolean.class.getClassLoader());
        long tmpMStartDate = in.readLong();
        this.mStartDate = tmpMStartDate == -1 ? null : new Date(tmpMStartDate);
        long tmpMEndDate = in.readLong();
        this.mEndDate = tmpMEndDate == -1 ? null : new Date(tmpMEndDate);
        this.mId = in.readLong();
        this.mInverted = in.readByte() != 0;
    }

    public static final Creator<DateRangeFilter> CREATOR = new Creator<DateRangeFilter>() {
        @Override
        public DateRangeFilter createFromParcel(Parcel source) {
            return new DateRangeFilter(source);
        }

        @Override
        public DateRangeFilter[] newArray(int size) {
            return new DateRangeFilter[size];
        }
    };
}
