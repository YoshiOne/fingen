package com.yoshione.fingen.dao;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.util.Pair;


import com.yoshione.fingen.DBHelper;
import com.yoshione.fingen.interfaces.IDaoInheritor;
import com.yoshione.fingen.interfaces.IAbstractModel;
import com.yoshione.fingen.model.Category;
import com.yoshione.fingen.model.Payee;
import com.yoshione.fingen.utils.SmsParser;

import java.util.HashMap;
import java.util.List;

/**
 * Created by slv on 14.08.2015.
 * +
 */
public class PayeesDAO extends BaseDAO implements AbstractDAO, IDaoInheritor {

    private static PayeesDAO sInstance;

    private PayeesDAO(Context context) {
        super(context, DBHelper.T_REF_PAYEES, IAbstractModel.MODEL_TYPE_PAYEE , DBHelper.T_REF_PAYEES_ALL_COLUMNS);
        super.setDaoInheritor(this);
    }

    public synchronized static PayeesDAO getInstance(Context context) {
        if (sInstance == null) {
            sInstance = new PayeesDAO(context);
        }
        return sInstance;
    }

    @Override
    public IAbstractModel cursorToModel(Cursor cursor) {
        return cursorToPayee(cursor);
    }

    private Payee cursorToPayee(Cursor cursor) {
        Payee payee = new Payee();

        payee.setID(cursor.getLong(mColumnIndexes.get(DBHelper.C_ID)));
        payee.setName(cursor.getString(mColumnIndexes.get(DBHelper.C_REF_PAYEES_NAME)));
        payee.setFullName(cursor.getString(mColumnIndexes.get(DBHelper.C_FULL_NAME)));
        payee.setParentID(cursor.getLong(mColumnIndexes.get(DBHelper.C_PARENTID)));

        payee.setOrderNum(cursor.getInt(mColumnIndexes.get(DBHelper.C_ORDERNUMBER)));

        if (!cursor.isNull(mColumnIndexes.get(DBHelper.C_REF_PAYEES_DEFCATEGORY))) {
            payee.setDefCategoryID(cursor.getLong(mColumnIndexes.get(DBHelper.C_REF_PAYEES_DEFCATEGORY)));
        } else {
            payee.setDefCategoryID(-1);
        }

        payee = (Payee) DBHelper.getSyncDataFromCursor(payee, cursor, mColumnIndexes);

        return payee;
    }

    @Override
    public void deleteModel(IAbstractModel model, boolean resetTS, Context context) {
        ContentValues values = new ContentValues();
        //Обнуляем таблице транзакций
        values.clear();
        values.put(DBHelper.C_LOG_TRANSACTIONS_PAYEE, -1);
        TransactionsDAO.getInstance(context).bulkUpdateItem(DBHelper.C_LOG_TRANSACTIONS_PAYEE + " = " + model.getID(), values, resetTS);

        //Обнуляем таблице шаблонов
        values.clear();
        values.put(DBHelper.C_LOG_TEMPLATES_PAYEE, -1);
        TemplatesDAO.getInstance(context).bulkUpdateItem(DBHelper.C_LOG_TEMPLATES_PAYEE + " = " + model.getID(), values, resetTS);

        //Обнуляем таблице долгов
        values.clear();
        values.put(DBHelper.C_REF_DEBTS_PAYEE, -1);
        CreditsDAO.getInstance(context).bulkUpdateItem(DBHelper.C_REF_DEBTS_PAYEE + " = " + model.getID(), values, resetTS);

        //Удаляем все маркеры
        SmsMarkersDAO smsMarkersDAO = SmsMarkersDAO.getInstance(context);
        smsMarkersDAO.bulkDeleteModel(smsMarkersDAO.getModels(String.format("%s = %s AND %s = %s",
                DBHelper.C_LOG_SMS_PARSER_PATTERNS_TYPE, String.valueOf(SmsParser.MARKER_TYPE_PAYEE),
                DBHelper.C_LOG_SMS_PARSER_PATTERNS_OBJECT, String.valueOf(model.getID()))), resetTS);

        super.deleteModel(model, resetTS, context);
    }

    @SuppressWarnings("unchecked")
    public List<Payee> getAllPayees() throws Exception {
        return (List<Payee>) getItems(getTableName(), null, null, null,
                DBHelper.C_ORDERNUMBER + "," + DBHelper.C_REF_PAYEES_NAME, null);
    }

    public Payee getPayeeByID(long id) {
        return (Payee) getModelById(id);
    }

    @Override
    public List<?> getAllModels() throws Exception {
        return getAllPayees();
    }
}
