package com.yoshione.fingen.dao;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;

import com.yoshione.fingen.classes.ListSumsByCabbage;
import com.yoshione.fingen.classes.SumsByCabbage;
import com.yoshione.fingen.db.DbUtil;
import com.yoshione.fingen.interfaces.IDaoInheritor;
import com.yoshione.fingen.interfaces.IAbstractModel;
import com.yoshione.fingen.model.SimpleDebt;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import io.reactivex.Single;

public class SimpleDebtsDAO extends BaseDAO implements AbstractDAO, IDaoInheritor {

    //<editor-fold desc="ref_SimpleDebts">
    public static final String TABLE = "ref_SimpleDebts";

    public static final String COL_IS_ACTIVE = "IsActive";
    public static final String COL_START_AMOUNT = "StartAmount";
    public static final String COL_CURRENCY = "Currency";

    public static final String[] ALL_COLUMNS = joinArrays(COMMON_COLUMNS, new String[]{
            COL_NAME, COL_IS_ACTIVE, COL_START_AMOUNT, COL_CURRENCY, COL_SEARCH_STRING
    });

    public static final String SQL_CREATE_TABLE = "CREATE TABLE " + TABLE + " ("
            + COMMON_FIELDS     + ", "
            + COL_NAME          + " TEXT NOT NULL, "
            + COL_IS_ACTIVE     + " INTEGER NOT NULL, "
            + COL_START_AMOUNT  + " REAL NOT NULL, "
            + COL_CURRENCY      + " INTEGER REFERENCES [" + CabbagesDAO.TABLE + "]([" + COL_ID + "]) ON DELETE SET NULL ON UPDATE CASCADE, "
            + COL_SEARCH_STRING + " TEXT, "
            + "UNIQUE (" + COL_NAME + ", " + COL_SYNC_DELETED + ") ON CONFLICT ABORT);";
    //</editor-fold>

    private static SimpleDebtsDAO sInstance;

    public synchronized static SimpleDebtsDAO getInstance(Context context) {
        if (sInstance == null) {
            sInstance = new SimpleDebtsDAO(context);
        }
        return sInstance;
    }

    private SimpleDebtsDAO(Context context) {
        super(context, TABLE, ALL_COLUMNS);
        super.setDaoInheritor(this);
    }

    @Override
    public IAbstractModel createEmptyModel() {
        return new SimpleDebt();
    }

    @Override
    public IAbstractModel cursorToModel(Cursor cursor) {
        return cursorToSimpleDebt(cursor);
    }

    private SimpleDebt cursorToSimpleDebt(Cursor cursor) {
        SimpleDebt simpledebt = new SimpleDebt();
        simpledebt.setID(DbUtil.getLong(cursor, COL_ID));
        simpledebt.setName(DbUtil.getString(cursor, COL_NAME));
        simpledebt.setIsActive(DbUtil.getBoolean(cursor, COL_IS_ACTIVE));
        simpledebt.setStartAmount(new BigDecimal(DbUtil.getDouble(cursor, COL_START_AMOUNT)));
        simpledebt.setCabbageID(DbUtil.getLong(cursor, COL_CURRENCY));

        return simpledebt;
    }

    @SuppressWarnings("unchecked")
    public List<SimpleDebt> getAllSimpleDebts() {
        return (List<SimpleDebt>) getItems(getTableName(), null,
                null, null, String.format("%s DESC, %s ASC", COL_IS_ACTIVE, COL_NAME), null);
    }

    public SimpleDebt getSimpleDebtByID(long id) {
        return (SimpleDebt) getModelById(id);
    }

    @Override
    public List<?> getAllModels() {
        return getAllSimpleDebts();
    }

    @Override
    public void deleteModel(IAbstractModel model, boolean resetTS, Context context) {
        ContentValues values = new ContentValues();
        //Обнуляем таблице транзакций
        values.clear();
        values.put(TransactionsDAO.COL_SIMPLE_DEBT, -1);
        TransactionsDAO.getInstance(context).bulkUpdateItem(TransactionsDAO.COL_SIMPLE_DEBT + " = " + model.getID(), values, resetTS);

        super.deleteModel(model, resetTS, context);
    }

    public synchronized Single<ListSumsByCabbage> getGroupedSumsRx(boolean takeSearchString,
                                                                   ArrayList<Long> selectedIDs,
                                                                   Context context) {
        return Single.fromCallable(() -> getGroupedSums(takeSearchString, selectedIDs, context));
    }

    @SuppressWarnings("unchecked")
    public synchronized ListSumsByCabbage getGroupedSums(boolean takeSearchString,
                                                         ArrayList<Long> selectedIDs,
                                                         Context context) {
        //Создаем экземпляр результирующей записи "валюта - сумма"
        ListSumsByCabbage listSumsByCabbage = new ListSumsByCabbage();

        try {
            for (SimpleDebt dept : getAllSimpleDebts()) {
                SumsByCabbage sumsByCabbage = new SumsByCabbage(dept.getCabbageID(),
                        dept.getAmount() != null ? dept.getAmount() : BigDecimal.ZERO,
                        dept.getOweMe() != null ? dept.getOweMe() : BigDecimal.ZERO);
                sumsByCabbage.setStartBalance(dept.getStartAmount());
                listSumsByCabbage.getmList().add(sumsByCabbage);
            }
        } catch (Exception e) {
        }

        return listSumsByCabbage;
    }
}
