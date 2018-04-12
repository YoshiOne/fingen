package com.yoshione.fingen.model;

import android.content.ContentValues;
import android.graphics.Color;
import android.os.Parcel;
import android.support.annotation.NonNull;


import com.yoshione.fingen.DBHelper;
import com.yoshione.fingen.interfaces.IAbstractModel;

/**
 * Created by slv on 13.08.2015.
 *
 */
public class Location extends BaseModel implements IAbstractModel {

    public static final String TAG = "com.yoshione.fingen.Model.Location";

    //    private long mId = -1;
    private String mName;
    private String mAddress;
    private Double mLon;
    private Double mLat;
    private long mParentID;
    private int mOrderNum;
    private boolean mExpanded;

    public Location() {
        super();
//        this.mId = mID;
        this.mName = "";
        this.mAddress = "";
        this.mLon = 0d;
        this.mLat = 0d;
        mParentID = -1;
        mExpanded = true;
    }

    public Location(long id) {
        super(id);
    }

    public Location(long id, String name, String address, Double lon, Double lat, long parentID, int orderNum, boolean expanded) {
        setID(id);
        mName = name;
        mAddress = address;
        mLon = lon;
        mLat = lat;
        mParentID = parentID;
        mOrderNum = orderNum;
        mExpanded = expanded;
    }

    public long getParentID() {
        return mParentID;
    }

    public void setParentID(long parentID) {
        mParentID = parentID;
    }

    public int getOrderNum() {
        return mOrderNum;
    }

    public void setOrderNum(int orderNum) {
        mOrderNum = orderNum;
    }

    public boolean isExpanded() {
        return mExpanded;
    }

    public void setExpanded(boolean expanded) {
        mExpanded = expanded;
    }

    public String getAddress() {
        return mAddress;
    }

    public void setAddress(String mAddress) {
        this.mAddress = mAddress;
    }

    public String getName() {
        return mName;
    }

    public void setName(String mName) {
        this.mName = mName;
    }

    public Double getLon() {
        return mLon;
    }

    public void setLon(Double mLon) {
        this.mLon = mLon;
    }

    public Double getLat() {
        return mLat;
    }

    public void setLat(Double mLat) {
        this.mLat = mLat;
    }

    @Override
    public String toString() {
        return mName;
    }

    @Override
    public String getSearchString() {
        return mName + " " + mAddress;
    }

    @Override
    public long getID() {
        return super.getID();
    }

    public boolean isUndefined() {
        return mLat == 0 & mLon == 0;
    }

    @Override
    public ContentValues getCV() {
        ContentValues values = super.getCV();

        values.put(DBHelper.C_REF_LOCATIONS_NAME, getName());
        values.put(DBHelper.C_REF_LOCATIONS_ADDRESS, getAddress());
        values.put(DBHelper.C_REF_LOCATIONS_LAT, getLat());
        values.put(DBHelper.C_REF_LOCATIONS_LON, getLon());
        values.put(DBHelper.C_PARENTID, mParentID);
        values.put(DBHelper.C_ORDERNUMBER, getOrderNum());
        return values;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        super.writeToParcel(dest, flags);
        dest.writeString(this.mName);
        dest.writeString(this.mAddress);
        dest.writeValue(this.mLon);
        dest.writeValue(this.mLat);
        dest.writeLong(this.mParentID);
        dest.writeInt(this.mOrderNum);
        dest.writeByte(this.mExpanded ? (byte) 1 : (byte) 0);
    }

    protected Location(Parcel in) {
        super(in);
        this.mName = in.readString();
        this.mAddress = in.readString();
        this.mLon = (Double) in.readValue(Double.class.getClassLoader());
        this.mLat = (Double) in.readValue(Double.class.getClassLoader());
        this.mParentID = in.readLong();
        this.mOrderNum = in.readInt();
        this.mExpanded = in.readByte() != 0;
    }

    public static final Creator<Location> CREATOR = new Creator<Location>() {
        @Override
        public Location createFromParcel(Parcel source) {
            return new Location(source);
        }

        @Override
        public Location[] newArray(int size) {
            return new Location[size];
        }
    };

    @Override
    public int getColor() {
        return Color.TRANSPARENT;
    }

    @Override
    public String getLogTransactionsField() {
        return DBHelper.C_LOG_TRANSACTIONS_LOCATION;
    }
}
