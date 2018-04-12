package com.yoshione.fingen.dao;

import android.annotation.SuppressLint;
import android.content.Context;
import android.database.Cursor;
import android.util.Pair;


import com.yoshione.fingen.DBHelper;
import com.yoshione.fingen.interfaces.IDaoInheritor;
import com.yoshione.fingen.interfaces.IAbstractModel;
import com.yoshione.fingen.model.Cabbage;
import com.yoshione.fingen.utils.SmsParser;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Created by slv on 04.12.2015.
 **
 */
public class CabbagesDAO extends BaseDAO implements AbstractDAO, IDaoInheritor {
    public static final String TAG = "CabbagesDAO";
    // Database fields

    private static CabbagesDAO sInstance;
    public synchronized static CabbagesDAO getInstance(Context context) {
        if (sInstance == null) {
            sInstance = new CabbagesDAO(context);
        }
        return sInstance;
    }

    private CabbagesDAO(Context context) {
        super(context, DBHelper.T_REF_CURRENCIES, IAbstractModel.MODEL_TYPE_CABBAGE , DBHelper.T_REF_CURRENCIES_ALL_COLUMNS);
        super.setDaoInheritor(this);
    }

    @Override
    public List<?> getAllModels() throws Exception {
        return getItems(getTableName(), null, null, null,
                DBHelper.C_ORDERNUMBER + "," + DBHelper.C_REF_CURRENCIES_NAME, null);
    }

    public HashMap<Long, Cabbage> getCabbagesMap() {
         HashMap<Long, Cabbage> map = new HashMap<>();
        List<Cabbage> cabbages;
        try {
            cabbages = (List<Cabbage>) getAllModels();
        } catch (Exception e) {
            cabbages = new ArrayList<>();
        }
        for (Cabbage cabbage : cabbages) {
            map.put(cabbage.getID(), cabbage);
        }
        return map;
    }

    @Override
    public IAbstractModel cursorToModel(Cursor cursor) {
        return cursorToCabbage(cursor);
    }

    private Cabbage cursorToCabbage(Cursor cursor) {
        Cabbage cabbage = new Cabbage();
        cabbage.setID(cursor.getLong(mColumnIndexes.get(DBHelper.C_ID)));
        cabbage.setCode(cursor.getString(mColumnIndexes.get(DBHelper.C_REF_CURRENCIES_CODE)));
        cabbage.setSimbol(cursor.getString(mColumnIndexes.get(DBHelper.C_REF_CURRENCIES_SYMBOL)));
        cabbage.setName(cursor.getString(mColumnIndexes.get(DBHelper.C_REF_CURRENCIES_NAME)));
        cabbage.setDecimalCount(cursor.getInt(mColumnIndexes.get(DBHelper.C_REF_CURRENCIES_DECIMALCOUNT)));
        cabbage.setOrderNum(cursor.getInt(mColumnIndexes.get(DBHelper.C_ORDERNUMBER)));

        cabbage = (Cabbage) DBHelper.getSyncDataFromCursor(cabbage, cursor, mColumnIndexes);

        return cabbage;
    }

    public Cabbage getCabbageByID(long id) {
        return (Cabbage) getModelById(id);
    }

    @SuppressWarnings("unchecked")
    public Cabbage getCabbageByCode(String code) {
        List<Cabbage> cabbages;
        try {
            cabbages = (List<Cabbage>) getAllModels();
        } catch (Exception e) {
            return new Cabbage();
        }

        for (Cabbage cabbage : cabbages) {
            if (cabbage.getCode().toLowerCase().equals(code.toLowerCase())) {
                return cabbage;
            }
        }

        return new Cabbage();
    }

    @Override
    public void deleteModel(IAbstractModel model, boolean resetTS, Context context) {
        SmsMarkersDAO smsMarkersDAO = SmsMarkersDAO.getInstance(context);
        smsMarkersDAO.bulkDeleteModel(smsMarkersDAO.getModels(String.format("%s = %s AND %s = %s", DBHelper.C_LOG_SMS_PARSER_PATTERNS_TYPE, String.valueOf(SmsParser.MARKER_TYPE_CABBAGE),
                DBHelper.C_LOG_SMS_PARSER_PATTERNS_OBJECT, String.valueOf(model.getID()))), resetTS);

        super.deleteModel(model, resetTS, context);
    }
}
