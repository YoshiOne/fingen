package com.yoshione.fingen.dao;

import android.content.Context;
import android.database.Cursor;

import com.yoshione.fingen.db.DbUtil;
import com.yoshione.fingen.interfaces.IDaoInheritor;
import com.yoshione.fingen.interfaces.IAbstractModel;
import com.yoshione.fingen.model.BudgetCreditSync;

import java.math.BigDecimal;
import java.util.List;

public class BudgetCreditsDAO extends BaseDAO<BudgetCreditSync> implements IDaoInheritor {

    //<editor-fold desc="ref_budget_debts">
    public static final String TABLE = "ref_budget_debts";

    public static final String COL_YEAR = "Year";
    public static final String COL_MONTH = "Month";
    public static final String COL_DEBT = "Debt";
    public static final String COL_AMOUNT = "Amount";

    public static final String[] ALL_COLUMNS = joinArrays(COMMON_COLUMNS, new String[]{
            COL_YEAR, COL_MONTH, COL_DEBT, COL_AMOUNT
    });

    public static final String SQL_CREATE_TABLE = "CREATE TABLE [" + TABLE + "] ("
            + COMMON_FIELDS +   ", "
            + COL_YEAR +        " INTEGER NOT NULL, "
            + COL_MONTH +       " INTEGER NOT NULL, "
            + COL_DEBT +        " INTEGER NOT NULL REFERENCES [" + CreditsDAO.TABLE + "]([" + COL_ID + "]) ON DELETE CASCADE ON UPDATE CASCADE, "
            + COL_AMOUNT +      " REAL NOT NULL, "
            + "UNIQUE (" + COL_SYNC_DELETED + ", " + COL_YEAR + ", " + COL_MONTH + ", " + COL_DEBT + ") ON CONFLICT REPLACE);";
    //</editor-fold>

    private static BudgetCreditsDAO sInstance;

    public synchronized static BudgetCreditsDAO getInstance(Context context) {
        if (sInstance == null) {
            sInstance = new BudgetCreditsDAO(context);
        }
        return sInstance;
    }

    private BudgetCreditsDAO(Context context) {
        super(context, TABLE, ALL_COLUMNS);
        super.setDaoInheritor(this);
    }

    @Override
    public IAbstractModel createEmptyModel() {
        return new BudgetCreditSync();
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
                COL_YEAR, String.valueOf(year),
                COL_MONTH, String.valueOf(month));
        bulkDeleteModel(getModels(where), true);
    }

    public boolean budgetExists(int year, int month, long creditId) {
        Cursor cursor = mDatabase.query(TABLE, null,
                COL_YEAR + " = ? AND " + COL_MONTH + " = ? AND " + COL_DEBT + " = ? AND " + COL_SYNC_DELETED + " = 0",
                new String[] { String.valueOf(year), String.valueOf(month), String.valueOf(creditId) },
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
        budgetCreditSync.setID(DbUtil.getLong(cursor, COL_ID));
        budgetCreditSync.setYear(DbUtil.getInt(cursor, COL_YEAR));
        budgetCreditSync.setMonth(DbUtil.getInt(cursor, COL_MONTH));
        budgetCreditSync.setCreditID(DbUtil.getLong(cursor, COL_DEBT));
        budgetCreditSync.setAmount(DbUtil.getDouble(cursor, COL_AMOUNT));

        return budgetCreditSync;
    }

    @Override
    public List<BudgetCreditSync> getAllModels() {
        return getItems(getTableName(), null, null, null, null, null);
    }
}
