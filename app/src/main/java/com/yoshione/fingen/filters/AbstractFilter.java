/*
 * Copyright (c) 2015.
 */

package com.yoshione.fingen.filters;

import android.os.Parcelable;

import java.util.HashSet;

/**
 * Created by slv on 05.11.2015.
 *
 */
public abstract class AbstractFilter implements Parcelable {
    public abstract void setId(long id);
    public abstract long getId();
    public abstract void setInverted(boolean inverted);
    public abstract boolean isInverted();
    public abstract Boolean getEnabled();
    public abstract void setEnabled(Boolean enabled);
    public abstract int getModelType();
    public abstract String getSelectionString();
    public abstract String saveToString();
    public abstract boolean loadFromString(String s);
    public abstract HashSet<Long> getIDsSet();
 }
