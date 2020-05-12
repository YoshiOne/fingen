package com.yoshione.fingen.dao;

import android.content.Context;
import android.database.Cursor;

import com.yoshione.fingen.db.DbUtil;
import com.yoshione.fingen.interfaces.IDaoInheritor;
import com.yoshione.fingen.interfaces.IAbstractModel;
import com.yoshione.fingen.model.BudgetCatSync;

import java.math.BigDecimal;
import java.util.List;

public class BudgetDAO extends BaseDAO implements AbstractDAO, IDaoInheritor {

    //<editor-fold desc="ref_budget">
    public static final String TABLE = "ref_budget";

    public static final String COL_YEAR = "Year";
    public static final String COL_MONTH = "Month";
    public static final String COL_CATEGORY = "Category";
    public static final String COL_AMOUNT = "Amount";
    public static final String COL_CURRENCY = "Currency";

    public static final String[] ALL_COLUMNS = joinArrays(COMMON_COLUMNS, new String[]{
            COL_YEAR, COL_MONTH, COL_CATEGORY, COL_AMOUNT, COL_CURRENCY
    });

    public static final String SQL_CREATE_TABLE = "CREATE TABLE [" + TABLE + "] ("
            + COMMON_FIELDS +   ", "
            + COL_YEAR +        " INTEGER NOT NULL, "
            + COL_MONTH +       " INTEGER NOT NULL, "
            + COL_CATEGORY +    " INTEGER NOT NULL REFERENCES [" + CategoriesDAO.TABLE + "]([" + COL_ID + "]) ON DELETE CASCADE ON UPDATE CASCADE, "
            + COL_AMOUNT +      " REAL NOT NULL, "
            + COL_CURRENCY +    " INTEGER NOT NULL REFERENCES [" + CabbagesDAO.TABLE + "]([" + COL_ID + "]) ON DELETE CASCADE ON UPDATE CASCADE, "
            + "UNIQUE (" + COL_SYNC_DELETED + ", " + COL_YEAR + ", " + COL_MONTH + ", " + COL_CATEGORY + ", " + COL_CURRENCY + ") ON CONFLICT REPLACE);";
    //</editor-fold>

    private static BudgetDAO sInstance;

    public synchronized static BudgetDAO getInstance(Context context) {
        if (sInstance == null) {
            sInstance = new BudgetDAO(context);
        }
        return sInstance;
    }

    private BudgetDAO(Context context) {
        super(context, TABLE, ALL_COLUMNS);
        super.setDaoInheritor(this);
    }

    @Override
    public IAbstractModel createEmptyModel() {
        return new BudgetCatSync();
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
                COL_YEAR, String.valueOf(year),
                COL_MONTH, String.valueOf(month),
                COL_CATEGORY, String.valueOf(categoryId),
                COL_CURRENCY, String.valueOf(cabbageId));
        bulkDeleteModel(getModels(where), true);
    }

    public void clearBudget(int year, int month) {
        String where = String.format("%s = %s AND %s = %s",
                COL_YEAR, String.valueOf(year),
                COL_MONTH, String.valueOf(month));
        bulkDeleteModel(getModels(where), true);
    }

    public /*synchronized*/ boolean budgetExists(int year, int month, long categoryId) {
        Cursor cursor = mDatabase.query(TABLE, null,
                COL_YEAR + " = ? AND " + COL_MONTH + " = ? AND " + COL_CATEGORY + " = ? AND " + COL_SYNC_DELETED + " = 0",
                new String[]{String.valueOf(year), String.valueOf(month), String.valueOf(categoryId)},
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
        budgetCatSync.setID(DbUtil.getLong(cursor, COL_ID));
        budgetCatSync.setYear(DbUtil.getInt(cursor, COL_YEAR));
        budgetCatSync.setMonth(DbUtil.getInt(cursor, COL_MONTH));
        budgetCatSync.setCategoryID(DbUtil.getLong(cursor, COL_CATEGORY));
        budgetCatSync.setCabbageID(DbUtil.getLong(cursor, COL_CURRENCY));
        budgetCatSync.setAmount(DbUtil.getDouble(cursor, COL_AMOUNT));

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
