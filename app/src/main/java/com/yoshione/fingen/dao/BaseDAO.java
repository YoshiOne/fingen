package com.yoshione.fingen.dao;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.os.SystemClock;
import android.support.annotation.Nullable;
import android.util.Pair;

import android.util.Log;
import com.yoshione.fingen.DBHelper;
import com.yoshione.fingen.db.IOnConflict;
import com.yoshione.fingen.interfaces.IAbstractModel;
import com.yoshione.fingen.interfaces.IDaoInheritor;
import com.yoshione.fingen.model.Account;
import com.yoshione.fingen.model.AccountsSetLog;
import com.yoshione.fingen.model.AccountsSetRef;
import com.yoshione.fingen.model.BaseModel;
import com.yoshione.fingen.model.BudgetCatSync;
import com.yoshione.fingen.model.BudgetCreditSync;
import com.yoshione.fingen.model.Cabbage;
import com.yoshione.fingen.model.Category;
import com.yoshione.fingen.model.Credit;
import com.yoshione.fingen.model.Department;
import com.yoshione.fingen.model.Events;
import com.yoshione.fingen.model.Location;
import com.yoshione.fingen.model.Payee;
import com.yoshione.fingen.model.Product;
import com.yoshione.fingen.model.ProductEntry;
import com.yoshione.fingen.model.Project;
import com.yoshione.fingen.model.Sender;
import com.yoshione.fingen.model.SimpleDebt;
import com.yoshione.fingen.model.Sms;
import com.yoshione.fingen.model.SmsMarker;
import com.yoshione.fingen.model.Template;
import com.yoshione.fingen.model.Transaction;

import org.greenrobot.eventbus.EventBus;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

import io.requery.android.database.sqlite.SQLiteDatabase;

import static com.yoshione.fingen.DBHelper.C_ID;
import static com.yoshione.fingen.DBHelper.C_SYNC_DELETED;

/**
 * Created by slv on 12.08.2016.
 * 1
 */

public class BaseDAO implements AbstractDAO {
    protected final String TAG = this.getClass().toString();
    protected HashMap<String, Integer> mColumnIndexes;
    SQLiteDatabase mDatabase;
    private String mTableName;
    private int mModelType;

    String[] getAllColumns() {
        return mAllColumns;
    }

    public void setAllColumns(String[] allColumns) {
        mAllColumns = allColumns;
    }

    private String mAllColumns[];
    private IDaoInheritor mDaoInheritor;

    public BaseDAO(Context context, String tableName, int modelType, String allColumns[]) {
        try {
            DatabaseUpgradeHelper dbh = DatabaseUpgradeHelper.getInstance();
            while (dbh.isUpgrading()) {
                SystemClock.sleep(50);
            }
            init(DBHelper.getInstance(context).getDatabase(), tableName, modelType, allColumns);
        } catch (SQLException e) {
            Log.e(TAG, "SQLException on openning database " + e.getMessage());
            e.printStackTrace();
        }
    }

    public static AbstractDAO getDAO(int modelType, Context context) {
        switch (modelType) {
            case IAbstractModel.MODEL_TYPE_CABBAGE:
                return CabbagesDAO.getInstance(context);
            case IAbstractModel.MODEL_TYPE_LOCATION:
                return LocationsDAO.getInstance(context);
            case IAbstractModel.MODEL_TYPE_PAYEE:
                return PayeesDAO.getInstance(context);
            case IAbstractModel.MODEL_TYPE_PROJECT:
                return ProjectsDAO.getInstance(context);
            case IAbstractModel.MODEL_TYPE_SMSMARKER:
                return SmsMarkersDAO.getInstance(context);
            case IAbstractModel.MODEL_TYPE_ACCOUNT:
                return AccountsDAO.getInstance(context);
            case IAbstractModel.MODEL_TYPE_CATEGORY:
                return CategoriesDAO.getInstance(context);
            case IAbstractModel.MODEL_TYPE_SMS:
                return SmsDAO.getInstance(context);
            case IAbstractModel.MODEL_TYPE_TRANSACTION:
                return TransactionsDAO.getInstance(context);
            case IAbstractModel.MODEL_TYPE_CREDIT:
                return CreditsDAO.getInstance(context);
            case IAbstractModel.MODEL_TYPE_TEMPLATE:
                return TemplatesDAO.getInstance(context);
            case IAbstractModel.MODEL_TYPE_DEPARTMENT:
                return DepartmentsDAO.getInstance(context);
            case IAbstractModel.MODEL_TYPE_SIMPLEDEBT:
                return SimpleDebtsDAO.getInstance(context);
            case IAbstractModel.MODEL_TYPE_SENDER:
                return SendersDAO.getInstance(context);
            case IAbstractModel.MODEL_TYPE_BUDGET:
                return BudgetDAO.getInstance(context);
            case IAbstractModel.MODEL_TYPE_BUDGET_DEBT:
                return BudgetCreditsDAO.getInstance(context);
            case IAbstractModel.MODEL_TYPE_PRODUCT:
                return ProductsDAO.getInstance(context);
        }
        return null;
    }

    public static AbstractDAO getDAO(Class aClass, Context context) {
        if (aClass.equals(Cabbage.class)) return CabbagesDAO.getInstance(context);
        if (aClass.equals(Location.class)) return LocationsDAO.getInstance(context);
        if (aClass.equals(Payee.class)) return PayeesDAO.getInstance(context);
        if (aClass.equals(Project.class)) return ProjectsDAO.getInstance(context);
        if (aClass.equals(SmsMarker.class)) return SmsMarkersDAO.getInstance(context);
        if (aClass.equals(Account.class)) return AccountsDAO.getInstance(context);
        if (aClass.equals(Category.class)) return CategoriesDAO.getInstance(context);
        if (aClass.equals(Sms.class)) return SmsDAO.getInstance(context);
        if (aClass.equals(Transaction.class)) return TransactionsDAO.getInstance(context);
        if (aClass.equals(Credit.class)) return CreditsDAO.getInstance(context);
        if (aClass.equals(Template.class)) return TemplatesDAO.getInstance(context);
        if (aClass.equals(Department.class)) return DepartmentsDAO.getInstance(context);
        if (aClass.equals(SimpleDebt.class)) return SimpleDebtsDAO.getInstance(context);
        if (aClass.equals(Sender.class)) return SendersDAO.getInstance(context);
        if (aClass.equals(BudgetDAO.class)) return BudgetDAO.getInstance(context);
        if (aClass.equals(BudgetCreditsDAO.class)) return BudgetCreditsDAO.getInstance(context);
        return null;
    }

    private void init(SQLiteDatabase database, String tableName, int modelType, String allColumns[]) {
        mDatabase = database;
        mDaoInheritor = null;
        mTableName = tableName;
        mModelType = modelType;
        mAllColumns = allColumns;
        mColumnIndexes = new HashMap<>();

        Cursor cursor = mDatabase.query(getTableName(), null, C_ID + " < 0", null, null, null, null);
        if (cursor != null) {
            for (String column : mAllColumns) {
                mColumnIndexes.put(column, cursor.getColumnIndex(column));
            }
        }
    }

    //regio1 Select
    @Override
    public List<?> getAllModels() throws Exception {
        return null;
    }
    //endregio1

    /*synchronized*/ List<IAbstractModel> getModels(String where) {
        List<IAbstractModel> models = new ArrayList<>();
        Cursor cursor = mDatabase.query(mTableName, null, C_SYNC_DELETED + " = 0 AND (" + where + ")", null, null, null, null);
        if (cursor != null) {
            try {
                if (cursor.moveToFirst()) {
                    while (!cursor.isAfterLast()) {
                        models.add(mDaoInheritor.cursorToModel(cursor));
                        cursor.moveToNext();
                    }
                }
            } finally {
                cursor.close();
            }
        }
        return models;
    }

    @Override
    public HashSet<Long> getAllModelsIDs() {
        HashSet<Long> ids = new HashSet<>();
        Cursor cursor = mDatabase.query(mTableName, new String[]{C_ID}, C_SYNC_DELETED + " = 0", null, null, null, null);
        if (cursor != null) {
            try {
                if (cursor.moveToFirst()) {
                    while (!cursor.isAfterLast()) {
                        ids.add(cursor.getLong(0));
                        cursor.moveToNext();
                    }
                }
            } finally {
                cursor.close();
            }
        }
        return ids;
    }

    List<?> getItems(String tableName, String allColumns[], @Nullable String selection, @Nullable String groupBy,
                     @Nullable String order, @Nullable String limit) throws Exception {
        String where;
        if (selection != null && !selection.isEmpty()) {
            where = C_SYNC_DELETED + " = 0 AND ("+selection+") ";
        } else {
            where = C_SYNC_DELETED + " = 0";
        }
        Cursor cursor;
        List<IAbstractModel> modelList = new ArrayList<>();
        try {
            cursor = mDatabase.query(tableName, allColumns, where, null, groupBy, null, order, limit);
        } catch (Exception e) {
            return modelList;
        }
        if (cursor != null) {
            try {
                if (cursor.moveToFirst()) {
                    while (!cursor.isAfterLast()) {
                        IAbstractModel model = mDaoInheritor.cursorToModel(cursor);
                        modelList.add(model);
                        cursor.moveToNext();
                    }
                }
            } finally {
                cursor.close();
            }
        }
        return modelList;
    }

    @Override
    /*synchronized*/ public long getLastTS() {
        if (mTableName.isEmpty()) return -1;
        long ts;
        Cursor cursor = mDatabase.query(mTableName, new String[]{String.format("IFNULL(MAX(%s), -1) AS %s", DBHelper.C_SYNC_TS, DBHelper.C_SYNC_TS)},
                null, null, null, null, null);
        if (cursor.getCount() > 0) {
            cursor.moveToFirst();
            ts = cursor.getLong(0);
        } else {
            ts = -1;
        }
        cursor.close();

        return ts;
    }

    public long getModelCount() {
        if (mTableName.isEmpty()) return 0;
        int count;
        Cursor cursor = mDatabase.query(mTableName, new String[]{C_ID}, null, null, null, null, null);
        count = cursor.getCount();
        cursor.close();
        return count;
    }

    @Override
    public List<IAbstractModel> getModelsByIDs(List<Long> idList) {
        List<IAbstractModel> models = new ArrayList<>();
        for (long id : idList) {
            models.add(getModelById(id));
        }
        return models;
    }
    //endregio1

    @Override
    public IAbstractModel getModelById(long id) {
        return getModelByIdCustomColumns(id, null);
    }

    @SuppressWarnings("unchecked")
    @Override
    public IAbstractModel getModelByName(String name) throws Exception {
        List<IAbstractModel> models;

        models = (List<IAbstractModel>) getAllModels();

        for (IAbstractModel model : models) {
            if (model.getName().toLowerCase().equals(name.toLowerCase())) {
                return model;
            }
        }

        return BaseModel.createModelByType(mModelType);
    }

    @SuppressWarnings("unchecked")
    @Override
    public IAbstractModel getModelByFullName(String fullName) throws Exception {
        List<IAbstractModel> models;

        models = (List<IAbstractModel>) getAllModels();

        for (IAbstractModel model : models) {
            if (model.getFullName().toLowerCase().equals(fullName.toLowerCase())) {
                return model;
            }
        }

        return BaseModel.createModelByType(mModelType);
    }

    //regio1 Delete

    /*synchronized*/
    public IAbstractModel getModelByIdCustomColumns(long id, String allColumns[]) {
        Cursor cursor = mDatabase.query(mTableName, allColumns,
                C_SYNC_DELETED + " = 0 AND " + C_ID + " = " + String.valueOf(id), null, null, null, null);
        IAbstractModel model = null;
        if (cursor != null) {
            try {
                if (cursor.moveToFirst()) {
                    cursor.moveToFirst();
                    model = mDaoInheritor.cursorToModel(cursor);
                }
            } finally {
                if (model == null) {
                    model = createModelByTable();
                }
                cursor.close();
            }
        }
        return model;
    }

    /*synchronized*/
    public long getIDByFBID(String FBID) {
        if (mTableName.isEmpty() || FBID.isEmpty()) return -1;
        long id;
        Cursor cursor = mDatabase.query(mTableName, new String[]{C_ID},
                String.format("%s = '%s'", DBHelper.C_SYNC_FBID, FBID),
                null, null, null, null);
        if (cursor.getCount() > 0) {
            cursor.moveToFirst();
            id = cursor.getLong(mColumnIndexes.get(C_ID));
        } else {
            id = -1;
        }
        cursor.close();

        return id;
    }

    //regio1 Create
    private IAbstractModel createItem(IAbstractModel model) throws Exception {
        long insertId = createItem(model.getCV(), model.getID(), true);
        return getModelById(insertId);
    }
    //endregio1

    @Override
    public IAbstractModel createModel(IAbstractModel model) throws Exception {
        if (model.getTS() > 0) {
            model.setTS(-1);
        }
        int operation = model.getID() < 0 ? Events.MODEL_ADDED : Events.MODEL_CHANGED;
        IAbstractModel newModel = createItem(model);
        EventBus.getDefault().postSticky(new Events.EventOnModelChanged(Collections.singletonList(newModel), model.getModelType(), operation));
        return newModel;
    }

    @Override
    public IAbstractModel createModelWithoutEvent(IAbstractModel model) throws Exception {
        return createItem(model);
    }

    @Override
    public List<IAbstractModel> bulkCreateModel(List<IAbstractModel> models, IOnConflict onConflictListener, boolean updateDependencies) throws Exception {
//        List<ContentValues> valuesList = new ArrayList<>();
//        for (IAbstractModel model : models) {
//            valuesList.add(model.getCV());
//        }

        mDatabase.beginTransaction();
        int i = 0;
        long id;
        List<IAbstractModel> createdModels = new ArrayList<>();
        for (IAbstractModel model : models) {
//            id = models.get(i++).getID();
            try {
                id = createItem(model.getCV(), model.getID(), updateDependencies);
            } catch (Exception e) {
                if (onConflictListener != null) {
                    onConflictListener.conflict(model);
                }
                mDatabase.endTransaction();
                throw new Exception();
            }
            createdModels.add(getModelById(id));

        }
        mDatabase.setTransactionSuccessful();
        mDatabase.endTransaction();
        return createdModels;
    }

    public synchronized long createItem(ContentValues cv, long id, boolean updateDependencies) throws Exception {
        //Нужно для совместимости. Поле знак удалено из java, но осталось в БД
        if (mTableName.equals(DBHelper.T_REF_CATEGORIES)) {
            cv.put(DBHelper.C_REF_CATEGORIES_SIGN, 1);
        }
        long result;

        boolean inTransaction = mDatabase.inTransaction();
        if (!inTransaction) {
            mDatabase.beginTransaction();
        }

        //<editor-fold desc="Отменяем изменения в кэше балансов, сделанные предыдущей версией транзакции, если она уже была в БД">

        if (updateDependencies) {
            if (mTableName.equals(DBHelper.T_LOG_TRANSACTIONS) && id >= 0) {
                Cursor cursor = mDatabase.query(mTableName, new String[]{DBHelper.C_LOG_TRANSACTIONS_AMOUNT,
                                DBHelper.C_LOG_TRANSACTIONS_DATETIME, DBHelper.C_LOG_TRANSACTIONS_SRCACCOUNT,
                                DBHelper.C_LOG_TRANSACTIONS_DESTACCOUNT, DBHelper.C_LOG_TRANSACTIONS_EXCHANGERATE},
                        DBHelper.C_ID + " = " + String.valueOf(id), null, null, null, null);
                if (cursor.getCount() > 0) {
                    cursor.moveToFirst();
                    double amount = cursor.getDouble(0);
                    long dateTime = cursor.getLong(1);
                    long srcAccountID = cursor.getLong(2);
                    long destAccountID = cursor.getLong(3);
                    double exRate = cursor.getDouble(4);
                    updateBalance(true, amount, -1, srcAccountID, id, dateTime);

                    if (destAccountID >= 0) {
                        updateBalance(true, amount * exRate, 1, destAccountID, id, dateTime);
                    }
                }
                cursor.close();
                //Откатываем все изменения, которые были сделаны этой транзакцией
            }
        }
        //</editor-fold>

        if (id < 0) {
            result = mDatabase.insertOrThrow(mTableName, null, cv);
        } else {
            int rowsAffected = mDatabase.update(mTableName, cv, C_ID + " = " + id, null);
            if (rowsAffected == 0) {
                mDatabase.endTransaction();
                throw new Exception();
            }
            result = id;
        }

        if (updateDependencies) {
            //<editor-fold desc="Добавляем сумму транзакции к кэшам балансов">
            if (mTableName.equals(DBHelper.T_LOG_TRANSACTIONS)) {
                double amount = cv.getAsDouble(DBHelper.C_LOG_TRANSACTIONS_AMOUNT);
                double exRate = cv.getAsDouble(DBHelper.C_LOG_TRANSACTIONS_EXCHANGERATE);
                long srcAccountID = cv.getAsLong(DBHelper.C_LOG_TRANSACTIONS_SRCACCOUNT);
                long destAccountID = cv.getAsLong(DBHelper.C_LOG_TRANSACTIONS_DESTACCOUNT);
                long dateTime = cv.getAsLong(DBHelper.C_LOG_TRANSACTIONS_DATETIME);

                updateBalance(false, amount, 1, srcAccountID, result, dateTime);

                if (destAccountID >= 0) {
                    updateBalance(false, amount * exRate, -1, destAccountID, result, dateTime);
                }
            }
            //</editor-fold>

            //<editor-fold desc="Обновляем поисковые строки транслитом и полные имена сущностей">
            switch (mTableName) {
                case DBHelper.T_REF_CATEGORIES:
                case DBHelper.T_REF_PAYEES:
                case DBHelper.T_REF_PROJECTS:
                case DBHelper.T_REF_LOCATIONS:
                case DBHelper.T_REF_DEPARTMENTS:
                    DBHelper.updateFullNames(mTableName, true, mDatabase);
                    break;
                case DBHelper.T_REF_ACCOUNTS:
                case DBHelper.T_LOG_TEMPLATES:
                case DBHelper.T_REF_SIMPLEDEBTS:
                case DBHelper.T_LOG_TRANSACTIONS:
                case DBHelper.T_REF_PRODUCTS:
                    DBHelper.updateFullNames(mTableName, false, mDatabase);
                    break;
            }
            //</editor-fold>
        }

        if (!inTransaction) {
            mDatabase.setTransactionSuccessful();
            mDatabase.endTransaction();
        }

        return result;
    }

    void updateBalance(boolean revert, double amount, int mult, long accountId, long transactionID, long dateTime) {
        //Учесть измение стартового баланса!!!
        String fieldName;
        if (amount * mult > 0) {
            if (!revert) {
                fieldName = DBHelper.C_LOG_RB_INCOME;
            } else {
                fieldName = DBHelper.C_LOG_RB_EXPENSE;
            }
        } else {
            if (!revert) {
                fieldName = DBHelper.C_LOG_RB_EXPENSE;
            } else {
                fieldName = DBHelper.C_LOG_RB_INCOME;
            }
        }

        String sql;
        if (!revert) {
            sql = "INSERT OR REPLACE INTO log_Running_Balance (AccountID, TransactionID, DateTimeRB, Income, Expense) VALUES("
                    + String.valueOf(accountId) + ", " + String.valueOf(transactionID) + ", " + String.valueOf(dateTime) + ", "
                    + "IFNULL((SELECT Income FROM log_Running_Balance WHERE AccountID = " + String.valueOf(accountId) + " AND DateTimeRB <= " + String.valueOf(dateTime) + " ORDER BY DateTimeRB DESC LIMIT 1), 0), "
                    + "IFNULL((SELECT Expense FROM log_Running_Balance WHERE AccountID = " + String.valueOf(accountId) + " AND DateTimeRB <= " + String.valueOf(dateTime) + " ORDER BY DateTimeRB DESC LIMIT 1), 0))";
            mDatabase.execSQL(sql);
        }

        sql = "UPDATE log_Running_Balance SET " + fieldName + " = " + fieldName + " + " + String.valueOf(amount * mult)
                + " WHERE AccountID = " + String.valueOf(accountId) + " AND DateTimeRB >= " + String.valueOf(dateTime);
        mDatabase.execSQL(sql);
    }

    //regio1 Update
    synchronized void bulkUpdateItem(String where, ContentValues contentValues, boolean resetTS) {
        List<IAbstractModel> inputModels = getModels(where);
        List<IAbstractModel> outputModels = new ArrayList<>();

        if (inputModels.isEmpty()) return;

        ContentValues cv = new ContentValues();
        mDatabase.beginTransaction();
        for (IAbstractModel model : inputModels) {
            cv.clear();
            cv.putAll(contentValues);
            if (resetTS) cv.put(DBHelper.C_SYNC_TS, -1);
            mDatabase.update(mTableName, cv, String.format("%s = %s", C_ID, String.valueOf(model.getID())), null);
            outputModels.add(getModelById(model.getID()));
        }
        mDatabase.setTransactionSuccessful();
        mDatabase.endTransaction();
        if (resetTS)
            EventBus.getDefault().postSticky(new Events.EventOnModelChanged(outputModels, createModelByTable().getModelType(), Events.MODEL_CHANGED));
    }

    @Override
    synchronized public void deleteModel(IAbstractModel model, boolean resetTS, Context context) {
        int maxDel = DBHelper.getMaxDel(mDatabase, mTableName);
        ContentValues cv = new ContentValues();
        cv.put(C_SYNC_DELETED, ++maxDel);
        if (resetTS) {
            cv.put(DBHelper.C_SYNC_TS, -1);
        } else {
            cv.put(DBHelper.C_SYNC_TS, model.getTS());
        }
        mDatabase.update(mTableName, cv, String.format("%s = %s", C_ID, String.valueOf(model.getID())), null);

        model.setDeleted(true);
        Events.EventOnModelChanged event = new Events.EventOnModelChanged(Collections.singletonList(model), model.getModelType(), Events.MODEL_DELETED);
        if (resetTS) EventBus.getDefault().postSticky(event);

    }

    @Override
    public List<IAbstractModel> bulkDeleteModel(List<IAbstractModel> models, boolean resetTS) {
        return bulkDeleteModel(models, resetTS, false);
    }

    protected List<IAbstractModel> bulkDeleteModel(List<IAbstractModel> models, boolean resetTS, boolean isTransactionOpened) {
        int maxDel = DBHelper.getMaxDel(mDatabase, mTableName);
        List<IAbstractModel> deletedModels = new ArrayList<>();

        if (!isTransactionOpened) {
            mDatabase.beginTransaction();
        }
        ContentValues cv = new ContentValues();
        for (IAbstractModel model : models) {
            cv.clear();
            cv.put(C_SYNC_DELETED, ++maxDel);
            if (resetTS) cv.put(DBHelper.C_SYNC_TS, -1);
            mDatabase.update(mTableName, cv, String.format("%s = %s", C_ID, String.valueOf(model.getID())), null);
            model.setDeleted(true);
            deletedModels.add(model);
        }
        if (!isTransactionOpened) {
            mDatabase.setTransactionSuccessful();
            mDatabase.endTransaction();
        }
        if (resetTS)
            EventBus.getDefault().postSticky(new Events.EventOnModelChanged(deletedModels, createModelByTable().getModelType(), Events.MODEL_DELETED));
        return deletedModels;
    }

    @Override
    public void deleteAllModels() {
        List<IAbstractModel> models = getModels("*");
        bulkDeleteModel(models, true);
    }

    //regio1 Service
    public String getTableName() {
        return mTableName;
    }
    //endregio1

    public void setDaoInheritor(IDaoInheritor daoInheritor) {
        mDaoInheritor = daoInheritor;
    }


    @Override
    public void updateOrder(List<Pair<Long, Integer>> pairs) {
        if (pairs.size() == 0) {
            return;
        }

        String ids = "";
        String comma = "";
        String when = "";

        for (Pair<Long, Integer> pair : pairs) {
            when = String.format("%s\tWHEN %s THEN %s\n", when, String.valueOf(pair.first), String.valueOf(pair.second));
            ids = ids + comma + String.valueOf(pair.first);
            comma = ",";
        }

        String sql = String.format("UPDATE %s\n" +
                        "\tSET %s = CASE %s\n" +
                        "%s\n" +
                        "\tEND\n" +
                        "WHERE %s IN (%s)",
                mTableName,
                DBHelper.C_ORDERNUMBER,
                C_ID,
                when,
                C_ID,
                ids);


        mDatabase.execSQL(sql);
    }

    private IAbstractModel createModelByTable() {
        if (mTableName.equals(DBHelper.T_REF_CURRENCIES)) {
            return new Cabbage();
        }
        if (mTableName.equals(DBHelper.T_REF_ACCOUNTS)) {
            return new Account();
        }
        if (mTableName.equals(DBHelper.T_REF_PROJECTS)) {
            return new Project();
        }
        if (mTableName.equals(DBHelper.T_REF_DEPARTMENTS)) {
            return new Department();
        }
        if (mTableName.equals(DBHelper.T_REF_LOCATIONS)) {
            return new Location();
        }
        if (mTableName.equals(DBHelper.T_REF_CATEGORIES)) {
            return new Category();
        }
        if (mTableName.equals(DBHelper.T_REF_PAYEES)) {
            return new Payee();
        }
        if (mTableName.equals(DBHelper.T_REF_SIMPLEDEBTS)) {
            return new SimpleDebt();
        }
        if (mTableName.equals(DBHelper.T_REF_DEBTS)) {
            return new Credit();
        }
        if (mTableName.equals(DBHelper.T_LOG_TRANSACTIONS)) {
            return new Transaction(-1);
        }
        if (mTableName.equals(DBHelper.T_LOG_TEMPLATES)) {
            return new Template();
        }
        if (mTableName.equals(DBHelper.T_LOG_SMS_PARSER_PATTERNS)) {
            return new SmsMarker();
        }
        if (mTableName.equals(DBHelper.T_LOG_BUDGET)) {
            return new BudgetCatSync();
        }
        if (mTableName.equals(DBHelper.T_LOG_BUDGET_DEBTS)) {
            return new BudgetCreditSync();
        }
        if (mTableName.equals(DBHelper.T_REF_SENDERS)) {
            return new Sender();
        }
        if (mTableName.equals(DBHelper.T_LOG_INCOMING_SMS)) {
            return new Sms();
        }
        if (mTableName.equals(DBHelper.T_LOG_ACCOUNTS_SETS)) {
            return new AccountsSetLog();
        }
        if (mTableName.equals(DBHelper.T_REF_ACCOUNTS_SETS)) {
            return new AccountsSetRef();
        }
        if (mTableName.equals(DBHelper.T_REF_PRODUCTS)) {
            return new Product();
        }
        if (mTableName.equals(DBHelper.T_LOG_PRODUCTS)) {
            return new ProductEntry();
        }
        return new BaseModel();
    }
    //endregio1
}
