package com.yoshione.fingen.utils;

import android.annotation.SuppressLint;
import android.content.Context;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

/**
 * Created by slv on 20.05.2016.
 *
 */
public class DateTimeFormatter {
    private final DateFormat mDateShortFormat;// 6/30/09 or 30.06.09
    private final DateFormat mDateMediumFormat;// Jun 30, 2009 	30 juin 2009
    private final DateFormat mDateLongFormat;// June 30, 2009 	30 juin 2009
    private final DateFormat mDateSqlFormat;//
    private final DateFormat mTimeShortFormat;// 12:58
    private final DateFormat mTimeFullFormat;// 12:58:01
    private final DateFormat mDateTimeQrFormat;// 20171219T155200

    private static DateTimeFormatter sInstance;
    private Calendar mCalendar;
    private Locale mLocale;

    public static DateTimeFormatter getInstance(Context context) {
        if (sInstance == null) {
            sInstance = new DateTimeFormatter(context.getApplicationContext());
        }
        return sInstance;
    }

    @SuppressLint("SimpleDateFormat")
    private DateTimeFormatter(Context context) {
        mLocale = LocaleUtils.getLocale(context);
        mCalendar = Calendar.getInstance();
        mDateShortFormat = SimpleDateFormat.getDateInstance(DateFormat.SHORT, mLocale);
        mDateMediumFormat = SimpleDateFormat.getDateInstance(DateFormat.MEDIUM, mLocale);
        mDateLongFormat = SimpleDateFormat.getDateInstance(DateFormat.LONG, mLocale);
        mDateSqlFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        mDateTimeQrFormat = new SimpleDateFormat("yyyyMMddHHmmss");
        boolean is24Style = android.text.format.DateFormat.is24HourFormat(context);
        if (is24Style) {
            mTimeShortFormat = new SimpleDateFormat("HH:mm", mLocale);
            mTimeFullFormat = new SimpleDateFormat("HH:mm:ss", mLocale);
        } else {
            mTimeShortFormat = new SimpleDateFormat("h:mm a", mLocale);
            mTimeFullFormat = new SimpleDateFormat("h:mm:ss a", mLocale);
        }
    }

    public DateFormat getDateMediumFormat() {
        return mDateMediumFormat;
    }

    public static boolean is24(Context context) {
        return android.text.format.DateFormat.is24HourFormat(context);
    }

    public String getDateShortString(Date date) {
        return mDateShortFormat.format(date);
    }

    public String getDateMediumString(Date date) {
        return mDateMediumFormat.format(date);
    }

    public String getDateLongString(Date date) {
        return mDateLongFormat.format(date);
    }

    public String getDateLongStringWithDayOfWeekName(Date date) {
        mCalendar.setTime(date);
        String dayOfWeek = mCalendar.getDisplayName(Calendar.DAY_OF_WEEK, Calendar.SHORT, mLocale);
        return dayOfWeek + ", " + mDateLongFormat.format(date);
    }

    public String getTimeShortString(Date date) {
        return mTimeShortFormat.format(date);
    }

    public String getTimeFullString(Date date) {
        return mTimeFullFormat.format(date);
    }

    public Date parseTimeShortString(String s) {
        Date date;
        try {
            date = mTimeShortFormat.parse(s);
        } catch (ParseException e) {
            date = new Date();
        }
        return date;
    }

    public Date parseDateTimeSqlString(String s) {
        Date date;
        try {
            date = mDateSqlFormat.parse(s);
        } catch (ParseException e) {
            date = new Date();
        }
        return date;
    }

    public Date parseDateTimeQrString(String s) {
        Date date;
        try {
            date = mDateTimeQrFormat.parse(s);
        } catch (ParseException e) {
            date = new Date();
        }
        return date;
    }
}
