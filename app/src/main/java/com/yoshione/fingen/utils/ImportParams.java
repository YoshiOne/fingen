package com.yoshione.fingen.utils;

import android.annotation.SuppressLint;

import java.text.DateFormat;
import java.text.SimpleDateFormat;

/**
 * Created by slv on 18.05.2016.
 *
 */
public class ImportParams {
    public int date;//
    public int time;//
    public int account;//
    public int amount;
    public int currency;//
    public int category;//
    public int payee;//
    public int location;
    public int project;
    public int department;
    public int comment;//
    public int type;//
    public int fn;//
    public int fd;//
    public int fp;//
    private DateFormat dateFormat;
    public final boolean hasHeader;

    public ImportParams() {
        this.date = -1;
        this.time = -1;
        this.account = -1;
        this.amount = -1;
        this.currency = -1;
        this.category = -1;
        this.payee = -1;
        this.location = -1;
        this.project = -1;
        this.department = -1;
        this.comment = -1;
        this.hasHeader = true;
        this.type = -1;
        this.fn = -1;
        this.fd = -1;
        this.fp = -1;
    }

    public ImportParams(int date, int time, int account, int amount, int currency, int category,
                        int payee, int location, int project, int department, int comment, int type, int fn, int fd, int fp, boolean hasHeader) {
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
        this.fn = fn;
        this.fd = fd;
        this.fp = fp;
    }

    @SuppressLint("SimpleDateFormat")
    public void setDateFormat(String dateFormat) {
        this.dateFormat = new SimpleDateFormat(dateFormat);
    }

    public DateFormat getDateFormat() {
        return dateFormat;
    }
}
