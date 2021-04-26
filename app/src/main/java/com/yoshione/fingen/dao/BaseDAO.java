package com.yoshione.fingen.dao;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.os.SystemClock;
import android.util.Log;
import android.util.Pair;

import androidx.annotation.Nullable;

import com.yoshione.fingen.DBHelper;
import com.yoshione.fingen.db.IOnConflict;
import com.yoshione.fingen.interfaces.IAbstractModel;
import com.yoshione.fingen.interfaces.IDaoInheritor;
import com.yoshione.fingen.model.Account;
import com.yoshione.fingen.model.BaseModel;
import com.yoshione.fingen.model.Cabbage;
import com.yoshione.fingen.model.Category;
import com.yoshione.fingen.model.Credit;
import com.yoshione.fingen.model.Department;
import com.yoshione.fingen.model.Events;
import com.yoshione.fingen.model.Location;
import com.yoshione.fingen.model.Payee;
import com.yoshione.fingen.model.Project;
import com.yoshione.fingen.model.Sender;
import com.yoshione.fingen.model.SimpleDebt;
import com.yoshione.fingen.model.Sms;
import com.yoshione.fingen.model.SmsMarker;
import com.yoshione.fingen.model.Template;
import com.yoshione.fingen.model.Transaction;

import org.greenrobot.eventbus.EventBus;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

import io.requery.android.database.sqlite.SQLiteDatabase;

public abstract class BaseDAO<T extends IAbstractModel> implements AbstractDAO<T> {

    //<editor-fold desc="common fields">
    public static final String COL_ID = "_id";
    public static final String COL_SYNC_FBID = "FBID";
    public static final String COL_SYNC_TS = "TS";
    public static final String COL_SYNC_DELETED = "Deleted";
    public static final String COL_SYNC_DIRTY = "Dirty";
    public static final String COL_SYNC_LAST_EDITED = "LastEdited";
    public static final String COL_SEARCH_STRING = "SearchString";
    public static final String COL_PARENT_ID = "ParentID";
    public static final String COL_ORDER_NUMBER = "OrderNumber";
    public static final String COL_FULL_NAME = "FullName";
    public static final String COL_NAME = "Name";

    public static final String COMMON_FIELDS =
            COL_ID +                 " INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT, "
            + COL_SYNC_FBID +        " TEXT, "
            + COL_SYNC_TS +          " INTEGER, "
            + COL_SYNC_DELETED +     " INTEGER, "
            + COL_SYNC_DIRTY +       " INTEGER, "
            + COL_SYNC_LAST_EDITED + " TEXT";

    public static final String[] COMMON_COLUMNS = {
            COL_ID, COL_SYNC_FBID, COL_SYNC_TS, COL_SYNC_DELETED, COL_SYNC_DIRTY, COL_SYNC_LAST_EDITED
    };
    //</editor-fold>

    protected final String TAG = this.getClass().toString();

    protected HashMap<String, Integer> mColumnIndexes;
    SQLiteDatabase mDatabase;
    private String mTableName;
    private String[] mAllColumns;
    private IDaoInheritor<T> mDaoInheritor;

    public BaseDAO(Context context, String tableName, String[] allColumns) {
        try {
            DatabaseUpgradeHelper dbh = DatabaseUpgradeHelper.getInstance();
            while (dbh.isUpgrading()) {
                SystemClock.sleep(50);
            }
            init(DBHelper.getInstance(context).getDatabase(), tableName, allColumns);
        } catch (SQLException e) {
            Log.e(TAG, "SQLException on openning database " + e.getMessage());
            e.printStackTrace();
        }
    }

    @Override
    public IAbstractModel createEmptyModel() {
        return new BaseModel();
    }

    String[] getAllColumns() {
        return mAllColumns;
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
        if (aClass.equals(ProductsDAO.class)) return ProductsDAO.getInstance(context);
        return null;
    }

    private void init(SQLiteDatabase database, String tableName, String[] allColumns) {
        mDatabase = database;
        mDaoInheritor = null;
        mTableName = tableName;
        mAllColumns = allColumns;
        mColumnIndexes = new HashMap<>();

        Cursor cursor = mDatabase.query(getTableName(), null, COL_ID + " < 0", null, null, null, null);
        if (cursor != null) {
            for (String column : mAllColumns) {
                mColumnIndexes.put(column, cursor.getColumnIndex(column));
            }
        }
    }

    @Override
    public List<T> getAllModels() {
        return null;
    }

    List<IAbstractModel> getModels(String where) {
        List<IAbstractModel> models = new ArrayList<>();
        Cursor cursor = mDatabase.query(mTableName, null, COL_SYNC_DELETED + " = 0 AND (" + where + ")", null, null, null, null);
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
        Cursor cursor = mDatabase.query(mTableName, new String[]{COL_ID}, COL_SYNC_DELETED + " = 0", null, null, null, null);
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

    List<T> getItems(String tableName, String[] allColumns, @Nullable String selection, @Nullable String groupBy,
                     @Nullable String order, @Nullable String limit) {
        String where = COL_SYNC_DELETED + " = 0" + (selection != null && !selection.isEmpty() ? " AND (" + selection + ")" : "");
        Cursor cursor;
        List<T> modelList = new ArrayList<>();
        try {
            cursor = mDatabase.query(tableName, allColumns, where, null, groupBy, null, order, limit);
        } catch (Exception e) {
            return modelList;
        }
        if (cursor != null) {
            try {
                if (cursor.moveToFirst()) {
                    while (!cursor.isAfterLast()) {
                        T model = mDaoInheritor.cursorToModel(cursor);
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

    public long getModelCount() {
        if (mTableName.isEmpty()) return 0;
        int count;
        Cursor cursor = mDatabase.query(mTableName, new String[]{COL_ID}, null, null, null, null, null);
        count = cursor.getCount();
        cursor.close();
        return count;
    }

    @Override
    public IAbstractModel getModelById(long id) {
        return getModelByIdCustomColumns(id, null);
    }

    @Override
    public IAbstractModel getModelByName(String name) {
        Cursor cursor = mDatabase.query(mTableName, getAllColumns(),
                COL_SYNC_DELETED + " = 0 AND " + COL_NAME + " = '" + name + "'", null, null, null, null);
        IAbstractModel model = null;
        if (cursor != null) {
            try {
                if (cursor.moveToFirst()) {
                    model = mDaoInheritor.cursorToModel(cursor);
                }
            } finally {
                if (model == null) {
                    model = createEmptyModel();
                }
                cursor.close();
            }
        }
        return model;
    }

    @Override
    public IAbstractModel getModelByFullName(String fullName) {
        Cursor cursor = mDatabase.query(mTableName, getAllColumns(),
                COL_SYNC_DELETED + " = 0 AND " + COL_FULL_NAME + " = '" + fullName + "'", null, null, null, null);
        IAbstractModel model = null;
        if (cursor != null) {
            try {
                if (cursor.moveToFirst()) {
                    model = mDaoInheritor.cursorToModel(cursor);
                }
            } finally {
                if (model == null) {
                    model = createEmptyModel();
                }
                cursor.close();
            }
        }
        return model;
    }

    public IAbstractModel getModelByIdCustomColumns(long id, String[] columns) {
        Cursor cursor = mDatabase.query(mTableName, columns,
                COL_SYNC_DELETED + " = 0 AND " + COL_ID + " = " + id, null, null, null, null);
        IAbstractModel model = null;
        if (cursor != null) {
            try {
                if (cursor.moveToFirst()) {
                    model = mDaoInheritor.cursorToModel(cursor);
                }
            } finally {
                if (model == null) {
                    model = createEmptyModel();
                }
                cursor.close();
            }
        }
        return model;
    }

    private IAbstractModel createItem(IAbstractModel model) throws Exception {
        long insertId = createItem(model.getCV(), model.getID(), true);
        return getModelById(insertId);
    }

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
        mDatabase.beginTransaction();
        int i = 0;
        long id;
        List<IAbstractModel> createdModels = new ArrayList<>();
        for (IAbstractModel model : models) {
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
        if (mTableName.equals(CategoriesDAO.TABLE)) {
            cv.put(CategoriesDAO.COL_SIGN, 1);
        }
        long result;

        boolean inTransaction = mDatabase.inTransaction();
        if (!inTransaction) {
            mDatabase.beginTransaction();
        }

        //<editor-fold desc="Отменяем изменения в кэше балансов, сделанные предыдущей версией транзакции, если она уже была в БД">

        if (updateDependencies) {
            if (mTableName.equals(TransactionsDAO.TABLE) && id >= 0) {
                Cursor cursor = mDatabase.query(mTableName,
                        new String[] {
                                TransactionsDAO.COL_AMOUNT, TransactionsDAO.COL_DATE_TIME, TransactionsDAO.COL_SRC_ACCOUNT,
                                TransactionsDAO.COL_DEST_ACCOUNT, TransactionsDAO.COL_EXCHANGE_RATE
                        },
                        TransactionsDAO.COL_ID + " = " + id, null, null, null, null);
                if (cursor.getCount() > 0) {
                    cursor.moveToFirst();
                    double amount = cursor.getDouble(0);
                    long dateTime = cursor.getLong(1);
                    long srcAccountID = cursor.getLong(2);
                    long destAccountID = cursor.getLong(3);
                    double exRate = cursor.getDouble(4);
                    RunningBalanceDAO.updateBalance(mDatabase, true, amount, -1, srcAccountID, id, dateTime);

                    if (destAccountID >= 0) {
                        RunningBalanceDAO.updateBalance(mDatabase, true, amount * exRate, 1, destAccountID, id, dateTime);
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
            int rowsAffected = mDatabase.update(mTableName, cv, COL_ID + " = " + id, null);
            if (rowsAffected == 0) {
                mDatabase.endTransaction();
                throw new Exception();
            }
            result = id;
        }

        if (updateDependencies) {
            //<editor-fold desc="Добавляем сумму транзакции к кэшам балансов">
            if (mTableName.equals(TransactionsDAO.TABLE)) {
                double amount = cv.getAsDouble(TransactionsDAO.COL_AMOUNT);
                double exRate = cv.getAsDouble(TransactionsDAO.COL_EXCHANGE_RATE);
                long srcAccountID = cv.getAsLong(TransactionsDAO.COL_SRC_ACCOUNT);
                long destAccountID = cv.getAsLong(TransactionsDAO.COL_DEST_ACCOUNT);
                long dateTime = cv.getAsLong(TransactionsDAO.COL_DATE_TIME);

                RunningBalanceDAO.updateBalance(mDatabase, false, amount, 1, srcAccountID, result, dateTime);

                if (destAccountID >= 0) {
                    RunningBalanceDAO.updateBalance(mDatabase, false, amount * exRate, -1, destAccountID, result, dateTime);
                }
            }
            //</editor-fold>

            //<editor-fold desc="Обновляем поисковые строки транслитом и полные имена сущностей">
            switch (mTableName) {
                case CategoriesDAO.TABLE:
                case PayeesDAO.TABLE:
                case ProjectsDAO.TABLE:
                case LocationsDAO.TABLE:
                case DepartmentsDAO.TABLE:
                    DBHelper.updateFullNames(mTableName, true, mDatabase);
                    break;
                case AccountsDAO.TABLE:
                case TemplatesDAO.TABLE:
                case SimpleDebtsDAO.TABLE:
                case TransactionsDAO.TABLE:
                case ProductsDAO.TABLE:
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

    synchronized void bulkUpdateItem(String where, ContentValues contentValues, boolean resetTS) {
        List<IAbstractModel> inputModels = getModels(where);
        if (inputModels.isEmpty()) return;

        List<IAbstractModel> outputModels = new ArrayList<>();

        ContentValues cv = new ContentValues();
        mDatabase.beginTransaction();
        for (IAbstractModel model : inputModels) {
            cv.clear();
            cv.putAll(contentValues);
            if (resetTS) cv.put(COL_SYNC_TS, -1);
            mDatabase.update(mTableName, cv, String.format("%s = %s", COL_ID, String.valueOf(model.getID())), null);
            outputModels.add(getModelById(model.getID()));
        }
        mDatabase.setTransactionSuccessful();
        mDatabase.endTransaction();
        if (resetTS)
            EventBus.getDefault().postSticky(new Events.EventOnModelChanged(outputModels, createEmptyModel().getModelType(), Events.MODEL_CHANGED));
    }

    @Override
    synchronized public void deleteModel(IAbstractModel model, boolean resetTS, Context context) {
        int maxDel = DBHelper.getMaxDel(mDatabase, mTableName);
        ContentValues cv = new ContentValues();
        cv.put(COL_SYNC_DELETED, ++maxDel);
        cv.put(COL_SYNC_TS, resetTS ? -1 : model.getTS());
        mDatabase.update(mTableName, cv, String.format("%s = %s", COL_ID, String.valueOf(model.getID())), null);

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
            cv.put(COL_SYNC_DELETED, ++maxDel);
            if (resetTS) cv.put(COL_SYNC_TS, -1);
            mDatabase.update(mTableName, cv, String.format("%s = %s", COL_ID, String.valueOf(model.getID())), null);
            model.setDeleted(true);
            deletedModels.add(model);
        }
        if (!isTransactionOpened) {
            mDatabase.setTransactionSuccessful();
            mDatabase.endTransaction();
        }
        if (resetTS)
            EventBus.getDefault().postSticky(new Events.EventOnModelChanged(deletedModels, createEmptyModel().getModelType(), Events.MODEL_DELETED));
        return deletedModels;
    }

    @Override
    public void deleteAllModels() {
        bulkDeleteModel(getModels("*"), true);
    }

    public String getTableName() {
        return mTableName;
    }

    public void setDaoInheritor(IDaoInheritor daoInheritor) {
        mDaoInheritor = daoInheritor;
    }

    @Override
    public void updateOrder(List<Pair<Long, Integer>> pairs) {
        if (pairs.size() == 0) {
            return;
        }

        StringBuilder ids = new StringBuilder();
        String comma = "";
        String when = "";

        for (Pair<Long, Integer> pair : pairs) {
            when = String.format("%s\tWHEN %s THEN %s\n", when, String.valueOf(pair.first), String.valueOf(pair.second));
            ids.append(comma).append(pair.first);
            comma = ",";
        }

        String sql = String.format("UPDATE %s SET %s = CASE %s %s END WHERE %s IN (%s)",
                mTableName, COL_ORDER_NUMBER, COL_ID, when, COL_ID, ids.toString());

        mDatabase.execSQL(sql);
    }

    protected static String[] joinArrays(String[] collection1, String[] collection2) {
        List<String> arr = new ArrayList<>(Arrays.asList(collection1));
        arr.addAll(Arrays.asList(collection2));
        return arr.toArray(new String[0]);
    }
}
