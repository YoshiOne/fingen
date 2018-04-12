package com.yoshione.fingen.dao;

import android.content.Context;
import android.database.Cursor;
import android.util.Pair;


import com.yoshione.fingen.DBHelper;
import com.yoshione.fingen.adapter.AdapterBudget;
import com.yoshione.fingen.classes.ListSumsByCabbage;
import com.yoshione.fingen.classes.SumsByCabbage;
import com.yoshione.fingen.interfaces.IDaoInheritor;
import com.yoshione.fingen.managers.AccountManager;
import com.yoshione.fingen.interfaces.IAbstractModel;
import com.yoshione.fingen.model.Account;
import com.yoshione.fingen.model.BudgetForCategory;
import com.yoshione.fingen.model.Cabbage;
import com.yoshione.fingen.model.Category;
import com.yoshione.fingen.model.Credit;
import com.yoshione.fingen.model.Payee;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

/**
 * Created by slv on 03.03.2016.
 *
 */
public class CreditsDAO extends BaseDAO implements AbstractDAO, IDaoInheritor {

    private static CreditsDAO sInstance;
    public synchronized static CreditsDAO getInstance(Context context) {
        if (sInstance == null) {
            sInstance = new CreditsDAO(context);
        }
        return sInstance;
    }

    private CreditsDAO(Context context) {
        super(context, DBHelper.T_REF_DEBTS, IAbstractModel.MODEL_TYPE_CREDIT , DBHelper.T_REF_DEBTS_ALL_COLUMNS);
        super.setDaoInheritor(this);
    }

    @Override
    public IAbstractModel cursorToModel(Cursor cursor) {
        return cursorToDebt(cursor);
    }

    private Credit cursorToDebt(Cursor cursor) {
        Credit credit = new Credit();

        credit.setID(cursor.getLong(mColumnIndexes.get(DBHelper.C_ID)));
        credit.setAccountID(cursor.getLong(mColumnIndexes.get(DBHelper.C_REF_DEBTS_ACCOUNT)));
        credit.setPayeeID(cursor.getLong(mColumnIndexes.get(DBHelper.C_REF_DEBTS_PAYEE)));
        credit.setCategoryID(cursor.getLong(mColumnIndexes.get(DBHelper.C_REF_DEBTS_CATEGORY)));
        credit.setClosed(cursor.getInt(mColumnIndexes.get(DBHelper.C_REF_DEBTS_CLOSED)) == 1);
        credit.setComment(cursor.getString(mColumnIndexes.get(DBHelper.C_REF_DEBTS_COMMENT)));

        credit = (Credit) DBHelper.getSyncDataFromCursor(credit, cursor, mColumnIndexes);

        return credit;
    }

    @Override
    public void deleteModel(IAbstractModel model, boolean resetTS, Context context) {
        //Удаляем бюджеты
        BudgetCreditsDAO budgetCreditsDAO = BudgetCreditsDAO.getInstance(context);
        budgetCreditsDAO.bulkDeleteModel(budgetCreditsDAO.getModels(DBHelper.C_LOG_BUDGET_DEBTS_CREDIT + " = " + model.getID()), resetTS);

        super.deleteModel(model, resetTS, context);
    }

    @SuppressWarnings("unchecked")
    public List<Credit> getAllDebts() throws Exception {
        return (List<Credit>) getItems(getTableName(), null, null, null, null, null);
    }

    public Credit getCreditByID(long id) {
        return (Credit) getModelById(id);
    }

    @Override
    public List<?> getAllModels() throws Exception {
        return getAllDebts();
    }

    public /*synchronized*/ List<Category> getDebtsAsCategoriesWithPlanFact(int year, int month, Context context) {
        /*
        SELECT DISTINCT ref_Debts.*,
           (
           SELECT Amount
           FROM ref_budget_debts
           WHERE Year = 2016 AND Month = 9 AND ref_Debts._id = Debt AND ref_budget_debts.Deleted = 0
           ) AS Plan,
           (
           SELECT SUM(Amount)
           FROM log_Transactions
           WHERE log_Transactions.SrcAccount = Account AND DateTime >= 1475280000064 AND DateTime <= 1477958399064  AND log_Transactions.Deleted = 0
           ) AS t1,
           (
           SELECT SUM(Amount)
           FROM log_Transactions
           WHERE log_Transactions.DestAccount = Account AND DateTime >= 1475280000064 AND DateTime <= 1477958399064  AND log_Transactions.Deleted = 0
           )*-1 AS t2
        FROM ref_Debts LEFT OUTER JOIN ref_budget_debts ON ref_Debts._id = Debt
        WHERE ref_Debts.Deleted = 0 AND (Plan IS NOT NULL OR t1 IS NOT NULL OR t2 IS NOT NULL)
        */

        Calendar c = Calendar.getInstance();
        c.set(year,month,1,0,0,0);
        Date start = c.getTime();
        c.set(year,month,c.getActualMaximum(Calendar.DAY_OF_MONTH),23,59,59);
        Date end = c.getTime();

        String sql = String.format(
                "SELECT DISTINCT ref_Debts.*,\n" +
                "   (\n" +
                "   SELECT Amount\n" +
                "   FROM ref_budget_debts\n" +
                "   WHERE Year = %s AND Month = %s AND ref_Debts._id = Debt AND ref_budget_debts.Deleted = 0\n" +
                "   ) AS Plan,   \n" +
                "   (\n" +
                "   SELECT SUM(Amount) \n" +
                "   FROM log_Transactions \n" +
                "   WHERE log_Transactions.SrcAccount = Account AND DateTime >= %s AND DateTime <= %s  AND log_Transactions.Deleted = 0\n" +
                "   ) AS t1,\n" +
                "   (\n" +
                "   SELECT SUM(Amount) \n" +
                "   FROM log_Transactions \n" +
                "   WHERE log_Transactions.DestAccount = Account AND DateTime >= %s AND DateTime <= %s  AND log_Transactions.Deleted = 0\n" +
                "   )*-1 AS t2\n" +
                "FROM ref_Debts LEFT OUTER JOIN ref_budget_debts ON ref_Debts._id = Debt\n" +
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
        Account account = AccountsDAO.getInstance(context).getAccountByID(cursor.getLong(cursor.getColumnIndex(DBHelper.C_REF_DEBTS_ACCOUNT)));
        Cabbage cabbage = AccountManager.getCabbage(account, context);
        BigDecimal plan;
        BigDecimal fact;
        plan = new BigDecimal(cursor.getDouble(cursor.getColumnIndex(DBHelper.C_REF_CATEGORIES_PLAN)));
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

        long categoryId = AdapterBudget.BUDGET_ITEM_DEBTS_ROOT - cursor.getLong(cursor.getColumnIndex(DBHelper.C_ID));

        Category category = new Category();

        category.setID(categoryId);
        category.setName(account.getName());
        category.setColor(0);
        category.setParentID(AdapterBudget.BUDGET_ITEM_DEBTS_ROOT);
        category.setOrderNum(0);
//        category.setSign(true);
        category.setBudget(new BudgetForCategory(new ListSumsByCabbage(), AdapterBudget.INFO_TYPE_ALL_TOTAL));

        category.getBudget().getmSums().getmList().add(sums);

        return category;
    }

    public /*synchronized*/ boolean isAccountBindToDebt(long accountId) {

        Cursor cursor = mDatabase.query(DBHelper.T_REF_DEBTS, null,
                DBHelper.C_REF_DEBTS_ACCOUNT + " = ?", new String[]{String.valueOf(accountId)},
                null, null, null);
        int count = cursor.getCount();
        cursor.close();

        return count > 0;
    }
}
