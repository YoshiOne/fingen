package com.yoshione.fingen.dao;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;

import com.yoshione.fingen.DBHelper;
import com.yoshione.fingen.classes.ListSumsByCabbage;
import com.yoshione.fingen.classes.SumsByCabbage;
import com.yoshione.fingen.interfaces.IDaoInheritor;
import com.yoshione.fingen.interfaces.IAbstractModel;
import com.yoshione.fingen.model.SimpleDebt;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import io.reactivex.Single;

/**
 * Created by slv on 13.08.2015.
 * 1
 */
public class SimpleDebtsDAO extends BaseDAO implements AbstractDAO, IDaoInheritor {
    private static SimpleDebtsDAO sInstance;

    private SimpleDebtsDAO(Context context) {
        super(context, DBHelper.T_REF_SIMPLEDEBTS, IAbstractModel.MODEL_TYPE_SIMPLEDEBT , DBHelper.T_REF_SIMPLEDEBTS_ALL_COLUMNS);
        super.setDaoInheritor(this);
    }

    public synchronized static SimpleDebtsDAO getInstance(Context context) {
        if (sInstance == null) {
            sInstance = new SimpleDebtsDAO(context);
        }
        return sInstance;
    }

    @Override
    public IAbstractModel cursorToModel(Cursor cursor) {
        return cursorToSimpleDebt(cursor);
    }

    private SimpleDebt cursorToSimpleDebt(Cursor cursor) {
        SimpleDebt simpledebt = new SimpleDebt();
        simpledebt.setID(cursor.getLong(mColumnIndexes.get(DBHelper.C_ID)));
        simpledebt.setName(cursor.getString(mColumnIndexes.get(DBHelper.C_REF_SIMPLEDEBTS_NAME)));
        simpledebt.setIsActive(cursor.getInt(mColumnIndexes.get(DBHelper.C_REF_SIMPLEDEBTS_ISACTIVE)) == 1);
        simpledebt.setStartAmount(new BigDecimal(cursor.getDouble(mColumnIndexes.get(DBHelper.C_REF_SIMPLEDEBTS_START_AMOUNT))));
        simpledebt.setCabbageID(cursor.getLong(mColumnIndexes.get(DBHelper.C_REF_SIMPLEDEBTS_CABBAGE)));

        simpledebt = (SimpleDebt) DBHelper.getSyncDataFromCursor(simpledebt, cursor, mColumnIndexes);

        return simpledebt;
    }

    @SuppressWarnings("unchecked")
    public List<SimpleDebt> getAllSimpleDebts() throws Exception {
        return (List<SimpleDebt>) getItems(getTableName(), null,
                null, null, String.format("%s DESC, %s ASC", DBHelper.C_REF_SIMPLEDEBTS_ISACTIVE, DBHelper.C_REF_SIMPLEDEBTS_NAME), null);
    }

    public SimpleDebt getSimpleDebtByID(long id) {
        return (SimpleDebt) getModelById(id);
    }

    @Override
    public List<?> getAllModels() throws Exception {
        return getAllSimpleDebts();
    }

    @Override
    public void deleteModel(IAbstractModel model, boolean resetTS, Context context) {
        ContentValues values = new ContentValues();
        //Обнуляем таблице транзакций
        values.clear();
        values.put(DBHelper.C_LOG_TRANSACTIONS_SIMPLEDEBT, -1);
        TransactionsDAO.getInstance(context).bulkUpdateItem(DBHelper.C_LOG_TRANSACTIONS_SIMPLEDEBT + " = " + model.getID(), values, resetTS);

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
