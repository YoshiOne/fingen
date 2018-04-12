package com.yoshione.fingen.dao;

import android.content.Context;
import android.database.Cursor;

import com.yoshione.fingen.DBHelper;
import com.yoshione.fingen.interfaces.IDaoInheritor;
import com.yoshione.fingen.interfaces.IAbstractModel;
import com.yoshione.fingen.model.Sender;

import java.util.List;

/**
 * Created by slv on 13.08.2015.
 * a
 */
public class SendersDAO extends BaseDAO implements AbstractDAO, IDaoInheritor {
    private static SendersDAO sInstance;

    private SendersDAO(Context context) {
        super(context, DBHelper.T_REF_SENDERS, IAbstractModel.MODEL_TYPE_SENDER , DBHelper.T_REF_SENDERS_ALL_COLUMNS);
        super.setDaoInheritor(this);
    }

    public synchronized static SendersDAO getInstance(Context context) {
        if (sInstance == null) {
            sInstance = new SendersDAO(context);
        }
        return sInstance;
    }

    @Override
    public IAbstractModel cursorToModel(Cursor cursor) {
        return cursorToSender(cursor);
    }

    private Sender cursorToSender(Cursor cursor) {
        Sender sender = new Sender();
        sender.setID(cursor.getLong(mColumnIndexes.get(DBHelper.C_ID)));
        sender.setName(cursor.getString(mColumnIndexes.get(DBHelper.C_REF_SENDERS_NAME)));
        sender.setPhoneNo(cursor.getString(mColumnIndexes.get(DBHelper.C_REF_SENDERS_PHONENO)));
        sender.setAmountPos(cursor.getInt(mColumnIndexes.get(DBHelper.C_REF_SENDERS_AMOUNTPOS)));
        sender.setBalancePos(cursor.getInt(mColumnIndexes.get(DBHelper.C_REF_SENDERS_BALANCEPOS)));
        sender.setDateFormat(cursor.getString(mColumnIndexes.get(DBHelper.C_REF_SENDERS_DATEFORMAT)));
        sender.setLeadingCurrencySymbol(cursor.getInt(mColumnIndexes.get(DBHelper.C_REF_SENDERS_LEADING_CURRENCY_SYMBOL)) == 1);
        sender.setActive(cursor.getInt(mColumnIndexes.get(DBHelper.C_REF_SENDERS_ISACTIVE)) == 1);
        sender.setAddCreditLimitToBalance(cursor.getInt(mColumnIndexes.get(DBHelper.C_REF_SENDERS_ADD_CREDIT_LIMIT_TO_BALANCE)) == 1);

        return sender;
    }

    @SuppressWarnings("unchecked")
    public List<Sender> getAllSenders() throws Exception {
        return (List<Sender>) getItems(getTableName(), null, null, null, DBHelper.C_REF_SENDERS_NAME, null);
    }

    public Sender getSenderByID(long id) {
        return (Sender) getModelById(id);
    }

    public Sender getSenderByPhoneNo(String phoneNo) {
        List<Sender> senders;
        try {
            senders = getAllSenders();
        } catch (Exception e) {
            return new Sender();
        }

        for (Sender sender : senders) {
            if (sender.getPhoneNo().toLowerCase().trim().equals(phoneNo.toLowerCase().trim())) {
                return sender;
            }
        }

        return new Sender();
    }

    @Override
    public List<?> getAllModels() throws Exception {
        return getAllSenders();
    }
}
