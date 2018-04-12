/*
 * Copyright (c) 2015.
 */

package com.yoshione.fingen.interfaces;

import android.content.ContentValues;
import android.os.Parcelable;

import java.math.BigDecimal;


/**
 * Created by slv on 07.12.2015.
 *
 */

public interface IAbstractModel extends Parcelable, Comparable<IAbstractModel> {
    int MODEL_TYPE_CABBAGE          = 1;
    int MODEL_TYPE_LOCATION         = 3;
    int MODEL_TYPE_PAYEE            = 4;
    int MODEL_TYPE_PROJECT          = 5;
    int MODEL_TYPE_SMSMARKER        = 6;
    int MODEL_TYPE_ACCOUNT          = 7;
    int MODEL_TYPE_CATEGORY         = 8;
    int MODEL_TYPE_SMS              = 10;
    int MODEL_TYPE_TRANSACTION      = 11;
    int MODEL_TYPE_CREDIT           = 12;
    int MODEL_TYPE_TEMPLATE         = 13;
    int MODEL_TYPE_DEPARTMENT       = 14;
    int MODEL_TYPE_SIMPLEDEBT       = 15;
    int MODEL_TYPE_SENDER           = 16;
    int MODEL_TYPE_BUDGET           = 17;
    int MODEL_TYPE_BUDGET_DEBT      = 18;
    int MODEL_TYPE_AMOUNT_FILTER    = 19;
    int MODEL_TYPE_PRODUCT          = 20;
    int MODEL_TYPE_PRODUCT_ENTRY    = 21;
    int MODEL_TYPE_DATE_RANGE       = 456;

    String toString();
    String getSearchString();

    long getID();
    void setID(long id);
    int getModelType();

    long getTS();
    void setTS(long TS);

    String getFBID();
    void setFBID(String fbid);

    boolean isDeleted();
    void setDeleted(boolean deleted);

    ContentValues getCV();

    long getParentID();

    void setParentID(long parentID);

    boolean isExpanded();

    void setExpanded(boolean expanded);

    String getName();

    String getFullName();

    void setName(String name);

    void setFullName(String fullName);

    int getColor();

    boolean isSelected();

    void setSelected(boolean selected);

    BigDecimal getIncome();

    void setIncome(BigDecimal income);

    BigDecimal getExpense();

    void setExpense(BigDecimal expense);

    void setSortType(int sortType);

    String getLogTransactionsField();
}
