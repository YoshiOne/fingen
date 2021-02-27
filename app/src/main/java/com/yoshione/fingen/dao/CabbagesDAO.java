package com.yoshione.fingen.dao;

import android.content.Context;
import android.database.Cursor;

import com.yoshione.fingen.db.DbUtil;
import com.yoshione.fingen.interfaces.IDaoInheritor;
import com.yoshione.fingen.interfaces.IAbstractModel;
import com.yoshione.fingen.model.Cabbage;
import com.yoshione.fingen.utils.SmsParser;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class CabbagesDAO extends BaseDAO implements AbstractDAO, IDaoInheritor {

    //<editor-fold desc="ref_Currencies">
    public static final String TABLE = "ref_Currencies";

    public static final String COL_CODE = "Code"; //трехсимвольный уникальный идентификатор валюты
    public static final String COL_SYMBOL = "Symbol"; //символы, испульзуемые при выводе суммы (например $, руб.)
    public static final String COL_DECIMAL_COUNT = "DecimalCount"; //количество знаков после запятой (-1  без ограничений)
    public static final String COL_ORDER_NUMBER = "OrderNumber";

    public static final String ALL_COLUMNS[] = joinArrays(COMMON_COLUMNS, new String[]{
            COL_CODE, COL_SYMBOL, COL_NAME, COL_DECIMAL_COUNT, COL_ORDER_NUMBER
    });

    public static final String SQL_CREATE_TABLE = "CREATE TABLE " + TABLE + " ("
            + COMMON_FIELDS +       ", "
            + COL_CODE +            " TEXT NOT NULL, "
            + COL_SYMBOL +          " TEXT NOT NULL, "
            + COL_NAME +            " TEXT NOT NULL, "
            + COL_DECIMAL_COUNT +   " INTEGER NOT NULL, "
            + COL_ORDER_NUMBER +    " INTEGER, "
            + "UNIQUE (" + COL_CODE + ", " + COL_SYNC_DELETED + ") ON CONFLICT ABORT);";
    //</editor-fold>

    private static CabbagesDAO sInstance;

    public synchronized static CabbagesDAO getInstance(Context context) {
        if (sInstance == null) {
            sInstance = new CabbagesDAO(context);
        }
        return sInstance;
    }

    private CabbagesDAO(Context context) {
        super(context, TABLE, ALL_COLUMNS);
        super.setDaoInheritor(this);
    }

    @Override
    public IAbstractModel createEmptyModel() {
        return new Cabbage();
    }

    @Override
    public List<?> getAllModels() {
        return getItems(getTableName(), null, null, null,
                COL_ORDER_NUMBER + "," + COL_NAME, null);
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
        cabbage.setID(DbUtil.getLong(cursor, COL_ID));
        cabbage.setCode(DbUtil.getString(cursor, COL_CODE));
        cabbage.setSimbol(DbUtil.getString(cursor, COL_SYMBOL));
        cabbage.setName(DbUtil.getString(cursor, COL_NAME));
        cabbage.setDecimalCount(DbUtil.getInt(cursor, COL_DECIMAL_COUNT));
        cabbage.setOrderNum(DbUtil.getInt(cursor, COL_ORDER_NUMBER));

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
        smsMarkersDAO.bulkDeleteModel(smsMarkersDAO.getModels(String.format("%s = %s AND %s = %s",
                SmsMarkersDAO.COL_TYPE, String.valueOf(SmsParser.MARKER_TYPE_CABBAGE),
                SmsMarkersDAO.COL_OBJECT, String.valueOf(model.getID()))), resetTS);

        super.deleteModel(model, resetTS, context);
    }
}
