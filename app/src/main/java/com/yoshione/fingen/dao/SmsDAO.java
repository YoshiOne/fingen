package com.yoshione.fingen.dao;

import android.content.Context;
import android.database.Cursor;
import android.widget.Toast;

import com.yoshione.fingen.DBHelper;
import com.yoshione.fingen.FGApplication;
import com.yoshione.fingen.interfaces.IDaoInheritor;
import com.yoshione.fingen.interfaces.IAbstractModel;
import com.yoshione.fingen.model.Sms;

import java.util.List;

/**
 * Created by slv on 30.10.2015.
 *
 */
public class SmsDAO extends BaseDAO implements AbstractDAO, IDaoInheritor {

    private static SmsDAO sInstance;

    public synchronized static SmsDAO getInstance(Context context) {
        if (sInstance == null) {
            sInstance = new SmsDAO(context);
        }
        return sInstance;
    }

    private SmsDAO(Context context) {
        super(context, DBHelper.T_LOG_INCOMING_SMS, IAbstractModel.MODEL_TYPE_SMS , DBHelper.T_LOG_INCOMING_SMS_ALL_COLUMNS);
        super.setDaoInheritor(this);
    }

    @Override
    public IAbstractModel cursorToModel(Cursor cursor) {
        return cursorToSms(cursor);
    }

    private Sms cursorToSms(Cursor cursor) {
        Sms sms= new Sms();

        sms.setID(cursor.getLong(mColumnIndexes.get(DBHelper.C_ID)));
        sms.setmDateTimeFromDbString(cursor.getString(mColumnIndexes.get(DBHelper.C_LOG_INCOMING_SMS_DATETIME)));
        sms.setSenderId(cursor.getLong(mColumnIndexes.get(DBHelper.C_LOG_INCOMING_SMS_SENDER)));
        sms.setmBody(cursor.getString(mColumnIndexes.get(DBHelper.C_LOG_INCOMING_SMS_BODY)));

        return sms;
    }

    @SuppressWarnings("unchecked")
    public List<Sms> getAllSmss() throws Exception {
        return (List<Sms>) getItems(getTableName(), null, null,
                null, DBHelper.C_LOG_INCOMING_SMS_DATETIME + " desc", null);
    }

    public Sms getSmsByID(long id) {
        return (Sms) getModelById(id);
    }

    @Override
    public List<?> getAllModels() throws Exception {
        return getAllSmss();
    }
}
