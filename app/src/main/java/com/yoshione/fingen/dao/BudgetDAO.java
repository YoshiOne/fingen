package com.yoshione.fingen.dao;

import android.content.Context;
import android.database.Cursor;
import android.util.Pair;


import com.yoshione.fingen.DBHelper;
import com.yoshione.fingen.interfaces.IDaoInheritor;
import com.yoshione.fingen.interfaces.IAbstractModel;
import com.yoshione.fingen.model.BudgetCatSync;
import com.yoshione.fingen.model.Cabbage;
import com.yoshione.fingen.model.Category;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Created by Leonid on 07.03.2016.
 *
 */
public class BudgetDAO extends BaseDAO implements AbstractDAO, IDaoInheritor {
    public static final String TAG = "BudgetDAO";

    private static BudgetDAO sInstance;
    public synchronized static BudgetDAO getInstance(Context context) {
        if (sInstance == null) {
            sInstance = new BudgetDAO(context);
        }
        return sInstance;
    }

    private BudgetDAO(Context context) {
        super(context, DBHelper.T_LOG_BUDGET, IAbstractModel.MODEL_TYPE_BUDGET , DBHelper.T_LOG_BUDGET_ALL_COLUMNS);
        super.setDaoInheritor(this);
    }

    public void createBudget(int year, int month, long categoryId, BigDecimal amount, long cabbageId) throws Exception {
        BudgetCatSync budget = new BudgetCatSync();
        budget.setYear(year);
        budget.setMonth(month);
        budget.setCategoryID(categoryId);
        budget.setAmount(amount.doubleValue());
        budget.setCabbageID(cabbageId);

        createModel(budget);
    }

    public void deleteBudget(int year, int month, long categoryId, long cabbageId) {
        String where = String.format("%s = %s AND %s = %s AND %s = %s AND %s = %s",
                DBHelper.C_LOG_BUDGET_YEAR, String.valueOf(year),
                DBHelper.C_LOG_BUDGET_MONTH, String.valueOf(month),
                DBHelper.C_LOG_BUDGET_CATEGORY, String.valueOf(categoryId),
                DBHelper.C_LOG_BUDGET_CURRENCY, String.valueOf(cabbageId));
        bulkDeleteModel(getModels(where), true);
    }

    public void clearBudget(int year, int month) {
        String where = String.format("%s = %s AND %s = %s",
                DBHelper.C_LOG_BUDGET_YEAR, String.valueOf(year),
                DBHelper.C_LOG_BUDGET_MONTH, String.valueOf(month));
        bulkDeleteModel(getModels(where), true);
    }

    public /*synchronized*/ boolean budgetExists(int year, int month, long categoryId) {

        Cursor cursor = mDatabase.query(DBHelper.T_LOG_BUDGET, null,
                DBHelper.C_LOG_BUDGET_YEAR + " = ? AND " +
                        DBHelper.C_LOG_BUDGET_MONTH + " = ? AND " +
                        DBHelper.C_LOG_BUDGET_CATEGORY + " = ? AND Deleted = 0", new String[]{String.valueOf(year),
                        String.valueOf(month),String.valueOf(categoryId)},
                null, null, null);
        boolean result = cursor.getCount() > 0;
        cursor.close();

        return result;
    }

    /**
     * Копирование плана бюджета с одного месяца на другой
     * @param fromYear исходный год
     * @param fromMonth исходный месяц
     * @param toYear год назначения
     * @param toMonth месяц назначения
     * @param replace заменять ли уже имеющиеся в месяце назначения значения или пропускать их
     * @param categoryId ID категории, для которой копируется план. -1 означает, что копируются все категории, что есть в исходном месяце.
     */
    public /*synchronized*/ void copyBudget(int fromYear, int fromMonth, int toYear, int toMonth, boolean replace, long categoryId) {
        //DROP temporary budget table
        String sql = "DROP TABLE IF EXISTS temp_budget;";
        mDatabase.execSQL(sql);


        String cat;//часть выражения SELECT отвечающая за выбор категории
        if (categoryId != -1) {
            cat = String.format(" AND Category = %s", String.valueOf(categoryId));
        } else {
            cat = "";
        }

        /*
         * Создаем временную таблицу, в которой содержатся план для всех категорий из исходного месяца
         * (либо одна категория, либо все)
         */
        sql = String.format("CREATE TEMP TABLE IF NOT EXISTS temp_budget AS\n" +
                        "SELECT * FROM ref_budget WHERE Year = %s AND Month = %s%s AND Deleted = 0;",
                String.valueOf(fromYear), String.valueOf(fromMonth), cat);
        mDatabase.execSQL(sql);

        /*
         * Изменяем во временной таблице год и месяц на toYear и toMonth
         */
        sql = String.format("UPDATE temp_budget SET Year = %s, Month = %s, _id = null;",
                String.valueOf(toYear), String.valueOf(toMonth));
        mDatabase.execSQL(sql);

        /*
         * Вставляем содержимое временной таблицы в основную (с заменой или без)
         */
        sql = "INSERT INTO ref_budget SELECT * FROM temp_budget";

        if (!replace) {
            sql = sql + "\nWHERE NOT EXISTS (SELECT Year, Month, Category, Currency\n" +
                    "       FROM ref_budget WHERE Year = temp_budget.Year " +
                    "       AND Month = temp_budget.Month " +
                    "       AND Currency = temp_budget.Currency " +
                    "       AND Category = temp_budget.Category" +
                    "       AND Deleted = 0);";
        }
        mDatabase.execSQL(sql);

    }



    @Override
    public IAbstractModel cursorToModel(Cursor cursor) {
        return cursorToBudgetCatSync(cursor);
    }

    private BudgetCatSync cursorToBudgetCatSync(Cursor cursor) {
        BudgetCatSync budgetCatSync = new BudgetCatSync();
        budgetCatSync.setID(cursor.getLong(mColumnIndexes.get(DBHelper.C_ID)));
        budgetCatSync.setYear(cursor.getInt(mColumnIndexes.get(DBHelper.C_LOG_BUDGET_YEAR)));
        budgetCatSync.setMonth(cursor.getInt(mColumnIndexes.get(DBHelper.C_LOG_BUDGET_MONTH)));
        budgetCatSync.setCategoryID(cursor.getLong(mColumnIndexes.get(DBHelper.C_LOG_BUDGET_CATEGORY)));
        budgetCatSync.setCabbageID(cursor.getLong(mColumnIndexes.get(DBHelper.C_LOG_BUDGET_CURRENCY)));
        budgetCatSync.setAmount(cursor.getDouble(mColumnIndexes.get(DBHelper.C_LOG_BUDGET_AMOUNT)));

        budgetCatSync = (BudgetCatSync) DBHelper.getSyncDataFromCursor(budgetCatSync, cursor, mColumnIndexes);

        return budgetCatSync;
    }

    @SuppressWarnings("unchecked")
    public List<BudgetCatSync> getAllBudgets() throws Exception {

        return (List<BudgetCatSync>) getItems(getTableName(), null, null, null, null, null);
    }

    @Override
    public List<?> getAllModels() throws Exception {
        return getAllBudgets();
    }
}
