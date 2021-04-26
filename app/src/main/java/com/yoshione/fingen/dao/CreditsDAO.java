package com.yoshione.fingen.dao;

import android.content.Context;
import android.database.Cursor;

import com.yoshione.fingen.adapter.AdapterBudget;
import com.yoshione.fingen.classes.ListSumsByCabbage;
import com.yoshione.fingen.classes.SumsByCabbage;
import com.yoshione.fingen.db.DbUtil;
import com.yoshione.fingen.interfaces.IDaoInheritor;
import com.yoshione.fingen.managers.AccountManager;
import com.yoshione.fingen.interfaces.IAbstractModel;
import com.yoshione.fingen.model.Account;
import com.yoshione.fingen.model.BudgetForCategory;
import com.yoshione.fingen.model.Cabbage;
import com.yoshione.fingen.model.Category;
import com.yoshione.fingen.model.Credit;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class CreditsDAO extends BaseDAO<Credit> implements IDaoInheritor {

    //<editor-fold desc="ref_Debts">
    public static final String TABLE = "ref_Debts";

    public static final String COL_ACCOUNT = "Account";
    public static final String COL_PAYEE = "Payee";
    public static final String COL_CATEGORY = "Category";
    public static final String COL_CLOSED = "Closed";
    public static final String COL_SRC_ACCOUNT = "SrcAccount";
    public static final String COL_COMMENT = "Comment";

    public static final String[] ALL_COLUMNS = joinArrays(COMMON_COLUMNS, new String[]{
            COL_ACCOUNT, COL_PAYEE, COL_CATEGORY, COL_CLOSED, COL_COMMENT
    });

    public static final String SQL_CREATE_TABLE = "CREATE TABLE [" + TABLE + "] ("
            + COMMON_FIELDS +   ", "
            + COL_ACCOUNT +     " INTEGER NOT NULL ON CONFLICT ABORT REFERENCES [" + AccountsDAO.TABLE + "]([" + COL_ID + "]) ON DELETE CASCADE ON UPDATE CASCADE, "
            + COL_PAYEE +       " INTEGER NOT NULL REFERENCES [" + PayeesDAO.TABLE + "]([" + COL_ID + "]) ON DELETE CASCADE ON UPDATE CASCADE, "
            + COL_CATEGORY +    " INTEGER NOT NULL REFERENCES [" + CategoriesDAO.TABLE + "]([" + COL_ID + "]) ON DELETE SET NULL ON UPDATE CASCADE, "
            + COL_CLOSED +      " INTEGER NOT NULL, "
            + COL_SRC_ACCOUNT + " INTEGER, "
            + COL_COMMENT +     " TEXT, "
            + "UNIQUE (" + COL_ACCOUNT + ", " + COL_SYNC_DELETED + ") ON CONFLICT ABORT);";
    //</editor-fold>

    private static CreditsDAO sInstance;

    public synchronized static CreditsDAO getInstance(Context context) {
        if (sInstance == null) {
            sInstance = new CreditsDAO(context);
        }
        return sInstance;
    }

    private CreditsDAO(Context context) {
        super(context, TABLE, ALL_COLUMNS);
        super.setDaoInheritor(this);
    }

    @Override
    public IAbstractModel createEmptyModel() {
        return new Credit();
    }

    @Override
    public IAbstractModel cursorToModel(Cursor cursor) {
        return cursorToDebt(cursor);
    }

    private Credit cursorToDebt(Cursor cursor) {
        Credit credit = new Credit();

        credit.setID(DbUtil.getLong(cursor, COL_ID));
        credit.setAccountID(DbUtil.getLong(cursor, COL_ACCOUNT));
        credit.setPayeeID(DbUtil.getLong(cursor, COL_PAYEE));
        credit.setCategoryID(DbUtil.getLong(cursor, COL_CATEGORY));
        credit.setClosed(DbUtil.getBoolean(cursor, COL_CLOSED));
        credit.setComment(DbUtil.getString(cursor, COL_COMMENT));

        return credit;
    }

    @Override
    public void deleteModel(IAbstractModel model, boolean resetTS, Context context) {
        //Удаляем бюджеты
        BudgetCreditsDAO budgetDebtsDAO = BudgetCreditsDAO.getInstance(context);
        budgetDebtsDAO.bulkDeleteModel(budgetDebtsDAO.getModels(BudgetCreditsDAO.COL_DEBT + " = " + model.getID()), resetTS);

        super.deleteModel(model, resetTS, context);
    }

    public Credit getCreditByID(long id) {
        return (Credit) getModelById(id);
    }

    @Override
    public List<Credit> getAllModels() {
        return getItems(getTableName(), null, null, null, null, null);
    }

    public /*synchronized*/ List<Category> getDebtsAsCategoriesWithPlanFact(int year, int month, Context context) {
        Calendar c = Calendar.getInstance();
        c.set(year,month,1,0,0,0);
        Date start = c.getTime();
        c.set(year,month,c.getActualMaximum(Calendar.DAY_OF_MONTH),23,59,59);
        Date end = c.getTime();

        String sql = String.format(
                "SELECT DISTINCT ref_Debts.*, " +
                "   (" +
                "   SELECT Amount " +
                "   FROM ref_budget_debts " +
                "   WHERE Year = %s AND Month = %s AND ref_Debts._id = Debt AND ref_budget_debts.Deleted = 0 " +
                "   ) AS Plan, " +
                "   (" +
                "   SELECT SUM(Amount) " +
                "   FROM log_Transactions " +
                "   WHERE log_Transactions.SrcAccount = Account AND DateTime >= %s AND DateTime <= %s  AND log_Transactions.Deleted = 0" +
                "   ) AS t1, " +
                "   (" +
                "   SELECT SUM(Amount) " +
                "   FROM log_Transactions " +
                "   WHERE log_Transactions.DestAccount = Account AND DateTime >= %s AND DateTime <= %s  AND log_Transactions.Deleted = 0" +
                "   )*-1 AS t2 " +
                "FROM ref_Debts LEFT OUTER JOIN ref_budget_debts ON ref_Debts._id = Debt " +
                "WHERE ref_Debts.Deleted = 0 AND (Plan IS NOT NULL OR t1 IS NOT NULL OR t2 IS NOT NULL)",
                String.valueOf(year), String.valueOf(month),
                String.valueOf(start.getTime()), String.valueOf(end.getTime()),
                String.valueOf(start.getTime()), String.valueOf(end.getTime()));

        List<Category> categoryList = new ArrayList<>();

        Cursor cursor = mDatabase.rawQuery(sql, null);
        if (cursor.getCount() > 0) {
            cursor.moveToFirst();
            while (!cursor.isAfterLast()) {
                categoryList.add(cursorToCategoryWithPlanFact(cursor, context));
                cursor.moveToNext();
            }
        }
        cursor.close();

        return categoryList;
    }

    private Category cursorToCategoryWithPlanFact(Cursor cursor, Context context) {
        Account account = AccountsDAO.getInstance(context).getAccountByID(DbUtil.getLong(cursor, COL_ACCOUNT));
        Cabbage cabbage = AccountManager.getCabbage(account, context);
        BigDecimal plan;
        BigDecimal fact;
        plan = new BigDecimal(cursor.getDouble(cursor.getColumnIndex("Plan")));
        fact = new BigDecimal(cursor.getDouble(cursor.getColumnIndex("t1")))
                .add(new BigDecimal(cursor.getDouble(cursor.getColumnIndex("t2"))));
        SumsByCabbage sums = new SumsByCabbage(cabbage.getID(), BigDecimal.ZERO, BigDecimal.ZERO);
        if (plan.compareTo(BigDecimal.ZERO) < 0) {
            sums.setOutPlan(plan);
        } else {
            sums.setInPlan(plan);
        }
        if (fact.compareTo(BigDecimal.ZERO) < 0) {
            sums.setOutTrSum(fact);
        } else {
            sums.setInTrSum(fact);
        }

        long categoryId = AdapterBudget.BUDGET_ITEM_DEBTS_ROOT - cursor.getLong(cursor.getColumnIndex(COL_ID));

        Category category = new Category();

        category.setID(categoryId);
        category.setName(account.getName());
        category.setColor(0);
        category.setParentID(AdapterBudget.BUDGET_ITEM_DEBTS_ROOT);
        category.setOrderNum(0);
        category.setBudget(new BudgetForCategory(new ListSumsByCabbage(), AdapterBudget.INFO_TYPE_ALL_TOTAL));

        category.getBudget().getmSums().getmList().add(sums);

        return category;
    }

    public boolean isAccountBindToDebt(long accountId) {
        Cursor cursor = mDatabase.query(TABLE, null,
                COL_ACCOUNT + " = ?", new String[]{String.valueOf(accountId)},
                null, null, null);
        int count = cursor.getCount();
        cursor.close();

        return count > 0;
    }
}
