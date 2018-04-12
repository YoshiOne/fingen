package com.yoshione.fingen.dao;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Color;
import android.util.Pair;


import com.yoshione.fingen.DBHelper;
import com.yoshione.fingen.adapter.AdapterBudget;
import com.yoshione.fingen.classes.ListSumsByCabbage;
import com.yoshione.fingen.classes.SumsByCabbage;
import com.yoshione.fingen.interfaces.IAbstractModel;
import com.yoshione.fingen.interfaces.IDaoInheritor;
import com.yoshione.fingen.model.BudgetForCategory;
import com.yoshione.fingen.model.Category;
import com.yoshione.fingen.utils.ColorUtils;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;

/**
 * Created by Leonid on 13.08.2015.
 * CategoriesDAO
 */
public class CategoriesDAO extends BaseDAO implements AbstractDAO, IDaoInheritor {
    public static final String TAG = "CategoryDAO";
    private static CategoriesDAO sInstance;

    private CategoriesDAO(Context context) {
        super(context, DBHelper.T_REF_CATEGORIES, IAbstractModel.MODEL_TYPE_CATEGORY , DBHelper.T_REF_CATEGORIES_ALL_COLUMNS);
        super.setDaoInheritor(this);
    }

    public synchronized static CategoriesDAO getInstance(Context context) {
        if (sInstance == null) {
            sInstance = new CategoriesDAO(context);
        }
        return sInstance;
    }

    public Category createCategory(Category category, Context context) throws Exception {
        List<?> models = getItems(getTableName(), null,
                String.format("%s = '%s' AND %s = %s AND %s != %s",
                        DBHelper.C_REF_CATEGORIES_NAME, category.getName(),
                        DBHelper.C_PARENTID, category.getParentID(),
                        DBHelper.C_ID, String.valueOf(category.getID()))
                , null, null, null);
        if (!models.isEmpty()) {
            return category;
        }
        if (category.getID() < 0 && context != null) {
            category.setColor(ColorUtils.getColor(context));
        }
        return (Category) super.createModel(category);
    }

    @Override
    public IAbstractModel cursorToModel(Cursor cursor) {
        return cursorToCategory(cursor);
    }

    private Category cursorToCategory(Cursor cursor) {
        Category category = new Category();

        category.setID(cursor.getLong(mColumnIndexes.get(DBHelper.C_ID)));
        category.setName(cursor.getString(mColumnIndexes.get(DBHelper.C_REF_CATEGORIES_NAME)));
        category.setParentID(cursor.getLong(mColumnIndexes.get(DBHelper.C_PARENTID)));

        category.setColor(Color.parseColor(cursor.getString(mColumnIndexes.get(DBHelper.C_REF_CATEGORIES_COLOR))));
        category.setOrderNum(cursor.getInt(mColumnIndexes.get(DBHelper.C_ORDERNUMBER)));

        category = (Category) DBHelper.getSyncDataFromCursor(category, cursor, mColumnIndexes);

        category.setFullName(cursor.getString(mColumnIndexes.get(DBHelper.C_FULL_NAME)));

        return category;
    }

    private Category cursorToCategoryWithPlanFact(Cursor cursor, LinkedHashMap<Long, Category> categoryHashMap,
                                                  int cabbageCI, int planCI, int factCI) {
        BigDecimal plan;
        BigDecimal fact;
        long cabbageId;
        cabbageId = cursor.getLong(cabbageCI);
        plan = new BigDecimal(cursor.getDouble(planCI));
        fact = new BigDecimal(cursor.getDouble(factCI));
        SumsByCabbage sums = new SumsByCabbage(cabbageId, BigDecimal.ZERO, BigDecimal.ZERO);
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

        long categoryId = cursor.getLong(mColumnIndexes.get(DBHelper.C_ID));

        Category category;

        if (categoryHashMap.containsKey(categoryId)) {
            category = categoryHashMap.get(categoryId);
        } else {
            category = new Category();
            category.setID(categoryId);
            category.setName(cursor.getString(mColumnIndexes.get(DBHelper.C_REF_CATEGORIES_NAME)));
//            category.setColor(Color.parseColor(cursor.getString(mColumnIndexes.get(DBHelper.C_REF_CATEGORIES_COLOR))));
            if (!cursor.isNull(mColumnIndexes.get(DBHelper.C_PARENTID))) {
                category.setParentID(cursor.getLong(mColumnIndexes.get(DBHelper.C_PARENTID)));
            } else {
                category.setParentID(-1);
            }
            category.setOrderNum(cursor.getInt(mColumnIndexes.get(DBHelper.C_ORDERNUMBER)));
            category.setFullName(cursor.getString(mColumnIndexes.get(DBHelper.C_FULL_NAME)));
            category.setBudget(new BudgetForCategory(new ListSumsByCabbage(), AdapterBudget.INFO_TYPE_ALL_TOTAL));
        }

        category.getBudget().getmSums().getmList().add(sums);

        return category;
    }

    @Override
    public void deleteModel(IAbstractModel model, boolean resetTS, Context context) {
        ContentValues values = new ContentValues();
        //Удаляем бюджеты
        BudgetDAO budgetDAO = BudgetDAO.getInstance(context);
        budgetDAO.bulkDeleteModel(budgetDAO.getModels(DBHelper.C_LOG_BUDGET_CATEGORY + " = " + model.getID()), resetTS);

        //Обнуляем эту категорию в таблице транзакций
        values.clear();
        values.put(DBHelper.C_LOG_TRANSACTIONS_CATEGORY, -1);
        TransactionsDAO.getInstance(context).bulkUpdateItem(DBHelper.C_LOG_TRANSACTIONS_CATEGORY + " = " + model.getID(), values, resetTS);

        //Обнуляем эту категорию в таблице шаблонов
        values.clear();
        values.put(DBHelper.C_LOG_TEMPLATES_CATEGORY, -1);
        TemplatesDAO.getInstance(context).bulkUpdateItem(DBHelper.C_LOG_TEMPLATES_CATEGORY + " = " + model.getID(), values, resetTS);

        //Обнуляем эту категорию в таблице Получателей
        values.clear();
        values.put(DBHelper.C_REF_PAYEES_DEFCATEGORY, -1);
        PayeesDAO.getInstance(context).bulkUpdateItem(DBHelper.C_REF_PAYEES_DEFCATEGORY + " = " + model.getID(), values, resetTS);

        //Обнуляем эту категорию в таблице долгов
        values.clear();
        values.put(DBHelper.C_REF_DEBTS_CATEGORY, -1);
        CreditsDAO.getInstance(context).bulkUpdateItem(DBHelper.C_REF_DEBTS_CATEGORY + " = " + model.getID(), values, resetTS);

        super.deleteModel(model, resetTS, context);
    }

    @SuppressWarnings("unchecked")
    public List<Category> getAllCategories() throws Exception {
        return (List<Category>) getItems(getTableName(), null,
                null, null, DBHelper.C_ORDERNUMBER + " ASC", null);
    }

    public /*synchronized*/ List<Category> getAllCategoriesWithPlanFact(int year, int month) {
        Calendar c = Calendar.getInstance();
        c.set(year, month, 1, 0, 0, 0);
        Date start = c.getTime();
        c.set(year, month, c.getActualMaximum(Calendar.DAY_OF_MONTH), 23, 59, 59);
        Date end = c.getTime();
//        DateTimeFormatter dateTimeFormatter = DateTimeFormatter.getInstance(context);

        String sql = String.format(
                "SELECT DISTINCT ref_Categories.*, ref_Currencies._id as Currency,\n" +
                        "   (\n" +
                        "   IFNULL((\n" +
                        "       SELECT Amount\n" +
                        "       FROM ref_budget as b\n" +
                        "       WHERE Year = %s AND Month = %s AND ref_Categories._id = Category and Currency = ref_Currencies._id AND b.Deleted = 0\n" +
                        "   ), 0)) AS Plan,\n" +
                        "   (\n" +
                        "   IFNULL((\n" +
                        "       SELECT SUM(prod.Price*prod.Quantity)\n" +
                        "       FROM (\n" +
                        "           SELECT log_Transactions.*, ref_Accounts.Currency\n" +
                        "           FROM log_Transactions, ref_Accounts\n" +
                        "           WHERE SrcAccount = ref_Accounts._id AND log_Transactions.Deleted = 0 AND IsClosed = 0) as t\n" +
                        "       INNER JOIN log_Products prod ON t._id = prod.TransactionID \n" +
                        "       WHERE\n" +
                        "           (ref_Categories._id = CASE WHEN prod.CategoryID < 0 THEN t.Category ELSE prod.CategoryID END)\n" +
                        "           AND (Currency = ref_Currencies._id)\n" +
                        "           AND (DestAccount < 0)\n" +
                        "           AND (DateTime >= %s)\n" +
                        "           AND (DateTime <= %s)\n" +
                        "           AND ref_Categories.Deleted = 0\n" +
                        "   ), 0)) AS Fact\n" +
                        "FROM ref_Categories, ref_Currencies\n" +
                        "LEFT OUTER JOIN (SELECT * FROM ref_budget WHERE Deleted = 0) AS c ON ref_Categories._id = c.Category\n" +
                        "WHERE ref_Categories.Deleted = 0 AND ref_Currencies.Deleted = 0",
                String.valueOf(year), String.valueOf(month), String.valueOf(start.getTime()), String.valueOf(end.getTime()));


        LinkedHashMap<Long, Category> categoryHashMap = new LinkedHashMap<>();
        Cursor cursor = mDatabase.rawQuery(sql, null);
        Category category;
        if (cursor.getCount() > 0) {
            int cabbageCI = cursor.getColumnIndex("Currency");
            int planCI = cursor.getColumnIndex("Plan");
            int factCI = cursor.getColumnIndex("Fact");
            cursor.moveToFirst();
            while (!cursor.isAfterLast()) {
                category = cursorToCategoryWithPlanFact(cursor, categoryHashMap, cabbageCI, planCI, factCI);
                categoryHashMap.put(category.getID(), category);
                cursor.moveToNext();
            }
        }
        cursor.close();

        return new ArrayList<>(categoryHashMap.values());
    }

    public Category getCategoryByID(long id) {
        return (Category) getModelById(id);
    }

    @Override
    public List<?> getAllModels() throws Exception {
        return getAllCategories();
    }
}
