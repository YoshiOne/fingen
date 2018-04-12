package com.yoshione.fingen.utils;

import android.annotation.SuppressLint;

import java.text.DateFormat;
import java.text.SimpleDateFormat;

/**
 * Created by slv on 18.05.2016.
 *
 */
public class ImportParams {
    public final int date;//
    public final int time;//
    public final int account;//
    public final int amount;
    public final int currency;//
    public final int category;//
    public final int payee;//
    public final int location;
    public final int project;
    public final int department;
    public final int comment;//
    public final int type;//
    private DateFormat dateFormat;
    public final boolean hasHeader;

    public ImportParams(int date, int time, int account, int amount, int currency, int category,
                        int payee, int location, int project, int department, int comment, int type, boolean hasHeader) {
        this.date = date;
        this.time = time;
        this.account = account;
        this.amount = amount;
        this.currency = currency;
        this.category = category;
        this.payee = payee;
        this.location = location;
        this.project = project;
        this.department = department;
        this.comment = comment;
        this.hasHeader = hasHeader;
        this.type = type;
    }

    @SuppressLint("SimpleDateFormat")
    public void setDateFormat(String dateFormat) {
        this.dateFormat = new SimpleDateFormat(dateFormat);
    }

    DateFormat getDateFormat() {
        return dateFormat;
    }
}
