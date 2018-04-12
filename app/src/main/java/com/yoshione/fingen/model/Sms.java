package com.yoshione.fingen.model;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.os.Parcel;


import com.yoshione.fingen.DBHelper;
import com.yoshione.fingen.interfaces.IAbstractModel;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by slv on 30.10.2015.
 *
 */
public class Sms extends BaseModel implements IAbstractModel {
    public static final String TAG = "com.yoshione.fingen.Model.Sms";

    //    private long mId = -1;
    private Date mDateTime;
    private long mSenderId;
    private String mBody;

    public Sms() {
        super();
        this.mDateTime = new Date();
        mSenderId = -1;
        mBody = "";
    }

    public Sms(long id) {
        super(id);
    }

    public Sms(long mId, Date mDateTime, long mSenderId, String mBody) {
        super();
        setID(mId);
        this.mDateTime = mDateTime;
        this.mSenderId = mSenderId;
        this.mBody = mBody;
    }

    public Date getmDateTime() {
        return mDateTime;
    }

    public void setmDateTime(Date mDateTime) {
        this.mDateTime = mDateTime;
    }

    private String getmDateTimeAsStringForSql() {

        @SuppressLint("SimpleDateFormat") DateFormat df = new SimpleDateFormat("yyyyMMdd  HH:mm");
        return df.format(mDateTime);
    }

    public void setmDateTimeFromDbString(String mDateTime) {

        @SuppressLint("SimpleDateFormat") DateFormat df = new SimpleDateFormat("yyyyMMdd  HH:mm");
        Date date = new Date();
        try {
            date = df.parse(mDateTime);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        this.mDateTime = date;
    }

    public long getSenderId() {
        return mSenderId;
    }

    public void setSenderId(long mSenderId) {
        this.mSenderId = mSenderId;
    }

    public String getmBody() {
        return mBody;
    }

    public void setmBody(String mBody) {
        this.mBody = mBody;
    }

    @Override
    public String toString() {
        return mBody;
    }

    @Override
    public String getSearchString() {
        return mBody;
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
        dest.writeLong(this.mDateTime != null ? this.mDateTime.getTime() : -1);
        dest.writeLong(this.mSenderId);
        dest.writeString(this.mBody);
    }

    protected Sms(Parcel in) {
        super(in);
        long tmpMDateTime = in.readLong();
        this.mDateTime = tmpMDateTime == -1 ? null : new Date(tmpMDateTime);
        this.mSenderId = in.readLong();
        this.mBody = in.readString();
    }

    public static final Creator<Sms> CREATOR = new Creator<Sms>() {
        @Override
        public Sms createFromParcel(Parcel source) {
            return new Sms(source);
        }

        @Override
        public Sms[] newArray(int size) {
            return new Sms[size];
        }
    };

    @Override
    public ContentValues getCV() {
        ContentValues values = super.getCV();

        values.put(DBHelper.C_LOG_INCOMING_SMS_DATETIME, getmDateTimeAsStringForSql());
        values.put(DBHelper.C_LOG_INCOMING_SMS_SENDER, getSenderId());
        values.put(DBHelper.C_LOG_INCOMING_SMS_BODY, getmBody());
        return values;
    }

    @Override
    public String getLogTransactionsField() {
        return null;
    }
}
