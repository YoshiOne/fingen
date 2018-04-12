/*
 * Copyright (c) 2015.
 */

package com.yoshione.fingen.filters;

import android.os.Parcel;
import android.os.Parcelable;
import android.text.TextUtils;

import com.yoshione.fingen.interfaces.IAbstractModel;

import java.util.HashSet;
import java.util.List;

/**
 * Created by Leonid on 07.11.2015.
 */
public class AccountFilter extends AbstractFilter implements Parcelable {

    private final HashSet<Long> mAccountIdSet;
    private Boolean mEnabled = true;
    private boolean mSystem;
    private long mId;
    private boolean mInverted;

    public AccountFilter(long id) {
        mId = id;
        mAccountIdSet = new HashSet<>();
        mInverted = false;
    }

    public boolean isSystem() {
        return mSystem;
    }

    public void setSystem(boolean system) {
        mSystem = system;
    }

    @Override
    public long getId() {
        return mId;
    }

    @Override
    public void setId(long id) {
        mId = id;
    }

    public void setInverted(boolean inverted) {
        mInverted = inverted;
    }

    @Override
    public boolean isInverted() {
        return mInverted;
    }

    @Override
    public HashSet<Long> getIDsSet() {
        return mAccountIdSet;
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
        return IAbstractModel.MODEL_TYPE_ACCOUNT;
    }

    @Override
    public String getSelectionString() {
        return "";
    }

    @Override
    public String saveToString() {
        if (!mAccountIdSet.isEmpty()) {
            return TextUtils.join("@", mAccountIdSet);
        } else {
            return "empty";
        }
    }

    @Override
    public boolean loadFromString(String s) {
        mAccountIdSet.clear();
        if (s.equals("empty")) {
            return true;
        } else {
            String strings[] = s.split("@");
            for (String id : strings) {
                try {
                    mAccountIdSet.add(Long.valueOf(id));
                } catch (NumberFormatException e) {
                    return false;
                }
            }
            return true;
        }
    }

    public void addAccount(long id) {
        mAccountIdSet.add(id);
    }

    public void addList(List<Long> list) {
        mAccountIdSet.addAll(list);
    }

    public void removeAccount(long id) {
        mAccountIdSet.remove(id);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeSerializable(this.mAccountIdSet);
        dest.writeValue(this.mEnabled);
        dest.writeByte(this.mSystem ? (byte) 1 : (byte) 0);
        dest.writeLong(this.mId);
        dest.writeByte(this.mInverted ? (byte) 1 : (byte) 0);
    }

    protected AccountFilter(Parcel in) {
        //noinspection unchecked
        this.mAccountIdSet = (HashSet<Long>) in.readSerializable();
        this.mEnabled = (Boolean) in.readValue(Boolean.class.getClassLoader());
        this.mSystem = in.readByte() != 0;
        this.mId = in.readLong();
        this.mInverted = in.readByte() != 0;
    }

    public static final Creator<AccountFilter> CREATOR = new Creator<AccountFilter>() {
        @Override
        public AccountFilter createFromParcel(Parcel source) {
            return new AccountFilter(source);
        }

        @Override
        public AccountFilter[] newArray(int size) {
            return new AccountFilter[size];
        }
    };
}
