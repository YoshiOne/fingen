package com.yoshione.fingen.dao;

import android.content.Context;
import android.database.Cursor;
import android.util.Pair;


import com.yoshione.fingen.DBHelper;
import com.yoshione.fingen.interfaces.IDaoInheritor;
import com.yoshione.fingen.interfaces.IAbstractModel;
import com.yoshione.fingen.model.BudgetCreditSync;
import com.yoshione.fingen.model.Credit;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Created by Leonid on 07.03.2016.
 *
 */
public class BudgetCreditsDAO extends BaseDAO implements AbstractDAO, IDaoInheritor {
    public static final String TAG = "BudgetDAO";

    private static BudgetCreditsDAO sInstance;
    public synchronized static BudgetCreditsDAO getInstance(Context context) {
        if (sInstance == null) {
            sInstance = new BudgetCreditsDAO(context);
        }
        return sInstance;
    }

    private BudgetCreditsDAO(Context context) {
        super(context, DBHelper.T_LOG_BUDGET_DEBTS, IAbstractModel.MODEL_TYPE_BUDGET_DEBT , DBHelper.T_LOG_BUDGET_DEBTS_ALL_COLUMNS);
        super.setDaoInheritor(this);
    }

    public void createBudget(int year, int month, long creditId, BigDecimal amount) throws Exception {
        BudgetCreditSync budget = new BudgetCreditSync();
        budget.setYear(year);
        budget.setMonth(month);
        budget.setCreditID(creditId);
        budget.setAmount(amount.doubleValue());
        createModel(budget);
    }

    public void clearBudget(int year, int month) {
        String where = String.format("%s = %s AND %s = %s",
                DBHelper.C_LOG_BUDGET_DEBTS_YEAR, String.valueOf(year),
                DBHelper.C_LOG_BUDGET_DEBTS_MONTH, String.valueOf(month));
        bulkDeleteModel(getModels(where), true);
    }

    public boolean budgetExists(int year, int month, long creditId) {

        Cursor cursor = mDatabase.query(DBHelper.T_LOG_BUDGET_DEBTS, null,
                DBHelper.C_LOG_BUDGET_DEBTS_YEAR + " = ? AND " +
                DBHelper.C_LOG_BUDGET_DEBTS_MONTH + " = ? AND " +
                DBHelper.C_LOG_BUDGET_DEBTS_CREDIT + " = ? AND Deleted = 0", new String[]{String.valueOf(year),
                        String.valueOf(month),String.valueOf(creditId)},
                null, null, null);
        boolean result = cursor.getCount() > 0;
        cursor.close();

        return result;
    }

    public /*synchronized*/ void copyBudget(int fromYear, int fromMonth, int toYear, int toMonth, boolean replace, long categoryId) {
        String sql = "DROP TABLE IF EXISTS temp_budget;";

        mDatabase.execSQL(sql);
        String cat = "";
        if (categoryId != -1) {
            cat = String.format(" AND Debt = %s", String.valueOf(categoryId));
        }
        sql = String.format("CREATE TEMP TABLE IF NOT EXISTS temp_budget AS\n" +
                        "SELECT * FROM ref_budget_debts WHERE Year = %s AND Month = %s%s AND Deleted = 0;",
                String.valueOf(fromYear), String.valueOf(fromMonth), cat);
        mDatabase.execSQL(sql);
        sql = String.format("UPDATE temp_budget SET Year = %s, Month = %s, _id = null;",
                String.valueOf(toYear), String.valueOf(toMonth));
        mDatabase.execSQL(sql);

        sql = "INSERT INTO ref_budget_debts SELECT * FROM temp_budget";

        if (!replace) {
            sql = sql + "\nWHERE NOT EXISTS (SELECT Year, Month, Debt\n" +
                    "       FROM ref_budget_debts WHERE Year = temp_budget.Year " +
                    "       AND Month = temp_budget.Month " +
                    "       AND Debt = temp_budget.Debt" +
                    "       AND Deleted = 0);";
        }
        mDatabase.execSQL(sql);

    }



    @Override
    public IAbstractModel cursorToModel(Cursor cursor) {
        return cursorToBudgetDebtSync(cursor);
    }

    private BudgetCreditSync cursorToBudgetDebtSync(Cursor cursor) {
        BudgetCreditSync budgetCreditSync = new BudgetCreditSync();
        budgetCreditSync.setID(cursor.getLong(mColumnIndexes.get(DBHelper.C_ID)));
        budgetCreditSync.setYear(cursor.getInt(mColumnIndexes.get(DBHelper.C_LOG_BUDGET_DEBTS_YEAR)));
        budgetCreditSync.setMonth(cursor.getInt(mColumnIndexes.get(DBHelper.C_LOG_BUDGET_DEBTS_MONTH)));
        budgetCreditSync.setCreditID(cursor.getLong(mColumnIndexes.get(DBHelper.C_LOG_BUDGET_DEBTS_CREDIT)));
        budgetCreditSync.setAmount(cursor.getDouble(mColumnIndexes.get(DBHelper.C_LOG_BUDGET_AMOUNT)));

        budgetCreditSync = (BudgetCreditSync) DBHelper.getSyncDataFromCursor(budgetCreditSync, cursor, mColumnIndexes);

        return budgetCreditSync;
    }

    @SuppressWarnings("unchecked")
    public List<BudgetCreditSync> getAllBudgets() throws Exception {

        return (List<BudgetCreditSync>) getItems(getTableName(), null, null, null, null, null);
    }

    @Override
    public List<?> getAllModels() throws Exception {
        return getAllBudgets();
    }
}
