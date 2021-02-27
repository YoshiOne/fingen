package com.yoshione.fingen.dao;

import android.content.Context;
import android.database.Cursor;

import com.yoshione.fingen.db.DbUtil;
import com.yoshione.fingen.interfaces.IDaoInheritor;
import com.yoshione.fingen.interfaces.IAbstractModel;
import com.yoshione.fingen.model.Sender;

import java.util.List;

public class SendersDAO extends BaseDAO implements AbstractDAO, IDaoInheritor {

    //<editor-fold desc="ref_Senders">
    public static final String TABLE = "ref_Senders";

    public static final String COL_PHONE_NO = "PhoneNo";
    public static final String COL_AMOUNT_POS = "AmountPos";
    public static final String COL_BALANCE_POS = "BalancePos";
    public static final String COL_DATE_FORMAT = "DateFormat";
    public static final String COL_LEADING_CURRENCY_SYMBOL = "LeadingCurrencySymbol";
    public static final String COL_IS_ACTIVE = "IsActive";
    public static final String COL_ADD_CREDIT_LIMIT_TO_BALANCE = "AddCreditLimitToBalance";

    public static final String[] ALL_COLUMNS = joinArrays(COMMON_COLUMNS, new String[]{
            COL_NAME, COL_PHONE_NO, COL_AMOUNT_POS, COL_BALANCE_POS, COL_DATE_FORMAT,
            COL_LEADING_CURRENCY_SYMBOL, COL_IS_ACTIVE, COL_ADD_CREDIT_LIMIT_TO_BALANCE
    });

    public static final String SQL_CREATE_TABLE = "CREATE TABLE " + TABLE + " ("
            + COMMON_FIELDS +                   ", "
            + COL_NAME +                        " TEXT NOT NULL, "
            + COL_PHONE_NO +                    " TEXT NOT NULL, "
            + COL_AMOUNT_POS +                  " INTEGER, "
            + COL_BALANCE_POS +                 " INTEGER, "
            + COL_LEADING_CURRENCY_SYMBOL +     " INTEGER, "
            + COL_DATE_FORMAT +                 " TEXT, "
            + COL_IS_ACTIVE +                   " INTEGER NOT NULL, "
            + COL_ADD_CREDIT_LIMIT_TO_BALANCE + " INTEGER NOT NULL DEFAULT 0, "
            + "UNIQUE (" + COL_NAME + ", " + COL_SYNC_DELETED + ") ON CONFLICT ABORT, "
            + "UNIQUE (" + COL_PHONE_NO + ", " + COL_SYNC_DELETED + ") ON CONFLICT ABORT);";
    //</editor-fold>

    private static SendersDAO sInstance;

    public synchronized static SendersDAO getInstance(Context context) {
        if (sInstance == null) {
            sInstance = new SendersDAO(context);
        }
        return sInstance;
    }

    private SendersDAO(Context context) {
        super(context, TABLE, ALL_COLUMNS);
        super.setDaoInheritor(this);
    }

    @Override
    public IAbstractModel createEmptyModel() {
        return new Sender();
    }

    @Override
    public IAbstractModel cursorToModel(Cursor cursor) {
        return cursorToSender(cursor);
    }

    private Sender cursorToSender(Cursor cursor) {
        Sender sender = new Sender();
        sender.setID(DbUtil.getLong(cursor, COL_ID));
        sender.setName(DbUtil.getString(cursor, COL_NAME));
        sender.setPhoneNo(DbUtil.getString(cursor, COL_PHONE_NO));
        sender.setAmountPos(DbUtil.getInt(cursor, COL_AMOUNT_POS));
        sender.setBalancePos(DbUtil.getInt(cursor, COL_BALANCE_POS));
        sender.setDateFormat(DbUtil.getString(cursor, COL_DATE_FORMAT));
        sender.setLeadingCurrencySymbol(DbUtil.getBoolean(cursor, COL_LEADING_CURRENCY_SYMBOL));
        sender.setActive(DbUtil.getBoolean(cursor, COL_IS_ACTIVE));
        sender.setAddCreditLimitToBalance(DbUtil.getBoolean(cursor, COL_ADD_CREDIT_LIMIT_TO_BALANCE));

        return sender;
    }

    @SuppressWarnings("unchecked")
    public List<Sender> getAllSenders() {
        return (List<Sender>) getItems(getTableName(), null, null, null, COL_NAME, null);
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
    public List<?> getAllModels() {
        return getAllSenders();
    }
}
