package com.yoshione.fingen.dao;

import android.content.Context;
import android.database.Cursor;

import com.yoshione.fingen.db.DbUtil;
import com.yoshione.fingen.interfaces.IDaoInheritor;
import com.yoshione.fingen.interfaces.IAbstractModel;
import com.yoshione.fingen.model.Sms;

import java.util.List;

public class SmsDAO extends BaseDAO<Sms> implements IDaoInheritor {

    //<editor-fold desc="log_Incoming_SMS">
    public static final String TABLE = "log_Incoming_SMS";

    public static final String COL_DATE_TIME = "DateTime";
    public static final String COL_SENDER = "Sender";
    public static final String COL_BODY = "Body";

    public static final String[] ALL_COLUMNS = joinArrays(COMMON_COLUMNS, new String[]{
            COL_DATE_TIME, COL_SENDER, COL_BODY
    });

    public static final String SQL_CREATE_TABLE = "CREATE TABLE " + TABLE + " ("
            + COMMON_FIELDS +   ", "
            + COL_DATE_TIME +   " TEXT NOT NULL, "
            + COL_SENDER +      " INTEGER NOT NULL, "
            + COL_BODY +        " TEXT NOT NULL);";
    //</editor-fold>

    private static SmsDAO sInstance;

    public synchronized static SmsDAO getInstance(Context context) {
        if (sInstance == null) {
            sInstance = new SmsDAO(context);
        }
        return sInstance;
    }

    private SmsDAO(Context context) {
        super(context, TABLE, ALL_COLUMNS);
        super.setDaoInheritor(this);
    }

    @Override
    public IAbstractModel createEmptyModel() {
        return new Sms();
    }

    @Override
    public IAbstractModel cursorToModel(Cursor cursor) {
        return cursorToSms(cursor);
    }

    private Sms cursorToSms(Cursor cursor) {
        Sms sms= new Sms();

        sms.setID(DbUtil.getLong(cursor, COL_ID));
        sms.setmDateTimeFromDbString(DbUtil.getString(cursor, COL_DATE_TIME));
        sms.setSenderId(DbUtil.getLong(cursor, COL_SENDER));
        sms.setmBody(DbUtil.getString(cursor, COL_BODY));

        return sms;
    }

    public Sms getSmsByID(long id) {
        return (Sms) getModelById(id);
    }

    @Override
    public List<Sms> getAllModels() {
        return getItems(getTableName(), null, null,
                null, COL_DATE_TIME + " desc", null);
    }
}
