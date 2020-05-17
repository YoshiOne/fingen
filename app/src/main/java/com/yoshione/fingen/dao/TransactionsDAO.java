package com.yoshione.fingen.dao;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.text.TextUtils;
import android.util.Log;
import android.util.Pair;

import androidx.annotation.NonNull;
import androidx.collection.LongSparseArray;

import com.yoshione.fingen.BuildConfig;
import com.yoshione.fingen.DBHelper;
import com.yoshione.fingen.classes.ListSumsByCabbage;
import com.yoshione.fingen.classes.SumsByCabbage;
import com.yoshione.fingen.db.DbUtil;
import com.yoshione.fingen.filters.AbstractFilter;
import com.yoshione.fingen.filters.DateRangeFilter;
import com.yoshione.fingen.filters.FilterListHelper;
import com.yoshione.fingen.interfaces.IAbstractModel;
import com.yoshione.fingen.interfaces.IDaoInheritor;
import com.yoshione.fingen.managers.FilterManager;
import com.yoshione.fingen.managers.TransferManager;
import com.yoshione.fingen.model.DateEntry;
import com.yoshione.fingen.model.ProductEntry;
import com.yoshione.fingen.model.SimpleDebt;
import com.yoshione.fingen.model.SummaryItem;
import com.yoshione.fingen.model.Transaction;
import com.yoshione.fingen.utils.LocaleUtils;
import com.yoshione.fingen.utils.Translit;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;

import io.reactivex.Single;

public class TransactionsDAO extends BaseDAO implements AbstractDAO, IDaoInheritor {

    //<editor-fold desc="log_Transactions">
    public static final String TABLE = "log_Transactions";

    public static final String COL_DATE_TIME = "DateTime";
    public static final String COL_SRC_ACCOUNT = "SrcAccount";
    public static final String COL_PAYEE = "Payee";
    public static final String COL_CATEGORY = "Category";
    public static final String COL_AMOUNT = "Amount";
    public static final String COL_PROJECT = "Project";
    public static final String COL_SIMPLE_DEBT = "SimpleDebt";
    public static final String COL_DEPARTMENT = "Department";
    public static final String COL_LOCATION = "Location";
    public static final String COL_COMMENT = "Comment";
    public static final String COL_FILE = "File";
    public static final String COL_DEST_ACCOUNT = "DestAccount";
    public static final String COL_EXCHANGE_RATE = "ExchangeRate";
    public static final String COL_AUTO_CREATED = "AutoCreated";
    public static final String COL_LON = "Lon";
    public static final String COL_LAT = "Lat";
    public static final String COL_ACCURACY = "Accuracy";
    public static final String COL_FN = "FN";
    public static final String COL_FD = "FD";
    public static final String COL_FP = "FP";
    public static final String COL_SPLIT = "Split";
    
    public static final String[] ALL_COLUMNS = joinArrays(COMMON_COLUMNS, new String[]{
            COL_DATE_TIME, COL_SRC_ACCOUNT, COL_PAYEE,
            COL_CATEGORY, COL_AMOUNT, COL_PROJECT,
            COL_SIMPLE_DEBT, COL_DEPARTMENT, COL_LOCATION,
            COL_COMMENT, COL_FILE, COL_DEST_ACCOUNT,
            COL_EXCHANGE_RATE, COL_AUTO_CREATED, COL_LON,
            COL_LAT, COL_ACCURACY, COL_SEARCH_STRING,
            COL_FN, COL_FD, COL_FP, COL_SPLIT
    });
    
    public static final String SQL_CREATE_TABLE = "CREATE TABLE " + TABLE + " ("
            + COMMON_FIELDS +       ", "
            + COL_DATE_TIME +       " INTEGER NOT NULL, "
            + COL_SRC_ACCOUNT +     " INTEGER NOT NULL REFERENCES [" + AccountsDAO.TABLE + "]([" + COL_ID + "]) ON DELETE CASCADE ON UPDATE CASCADE, "
            + COL_PAYEE +           " INTEGER REFERENCES [" + PayeesDAO.TABLE + "]([" + COL_ID + "]) ON DELETE SET NULL ON UPDATE CASCADE, "
            + COL_CATEGORY +        " INTEGER REFERENCES [" + CategoriesDAO.TABLE + "]([" + COL_ID + "]) ON DELETE SET NULL ON UPDATE CASCADE, "
            + COL_AMOUNT +          " REAL NOT NULL, "
            + COL_PROJECT +         " INTEGER REFERENCES [" + ProjectsDAO.TABLE + "]([" + COL_ID + "]) ON DELETE SET NULL ON UPDATE CASCADE, "
            + COL_SIMPLE_DEBT +     " INTEGER REFERENCES [" + SimpleDebtsDAO.TABLE + "]([" + COL_ID + "]) ON DELETE SET NULL ON UPDATE CASCADE, "
            + COL_DEPARTMENT +      " INTEGER REFERENCES [" + DepartmentsDAO.TABLE + "]([" + COL_ID + "]) ON DELETE SET NULL ON UPDATE CASCADE, "
            + COL_LOCATION +        " INTEGER REFERENCES [" + LocationsDAO.TABLE + "]([" + COL_ID + "]) ON DELETE SET NULL ON UPDATE CASCADE,"
            + COL_COMMENT +         " TEXT, "
            + COL_FILE +            " TEXT, "
            + COL_DEST_ACCOUNT +    " INTEGER NOT NULL REFERENCES [" + AccountsDAO.TABLE + "]([" + COL_ID + "]) ON DELETE CASCADE ON UPDATE CASCADE, "
            + COL_EXCHANGE_RATE +   " REAL NOT NULL, "
            + COL_AUTO_CREATED +    " INTEGER NOT NULL, "
            + COL_LON +             " REAL, "
            + COL_LAT +             " REAL, "
            + COL_ACCURACY +        " INTEGER,"
            + COL_FN +              " INTEGER DEFAULT 0,"
            + COL_FD +              " INTEGER DEFAULT 0,"
            + COL_FP +              " INTEGER DEFAULT 0,"
            + COL_SPLIT +           " INTEGER DEFAULT 0,"
            + COL_SEARCH_STRING +   " TEXT);";

    public static final String SQL_CREATE_INDEX = "CREATE INDEX [idx] ON [log_Transactions] ([Deleted], [DateTime], [SrcAccount], [DestAccount], [Payee], [Category], [Project], [Department], [Location], [SimpleDebt]);";
    //</editor-fold>

    private static TransactionsDAO sInstance;

    public synchronized static TransactionsDAO getInstance(Context context) {
        if (sInstance == null) {
            sInstance = new TransactionsDAO(context);
        }
        return sInstance;
    }

    private static final String SQLTAG = "SQLLOG";
    private ProductEntrysDAO mProductEntrysDAO;

    private TransactionsDAO(Context context) {
        super(context, TABLE, ALL_COLUMNS);
        super.setDaoInheritor(this);
        mProductEntrysDAO = ProductEntrysDAO.getInstance(context);
    }

    @Override
    public IAbstractModel createEmptyModel() {
        return new Transaction(-1);
    }

    @Override
    public IAbstractModel cursorToModel(Cursor cursor) {
        return cursorToTransaction(cursor);
    }

    private Transaction cursorToTransaction(Cursor cursor) {
        Transaction transaction = new Transaction(-1);

        transaction.setID(DbUtil.getLong(cursor, COL_ID));
        transaction.setDateTime(new Date(DbUtil.getLong(cursor, COL_DATE_TIME)));
        transaction.setAccountID(DbUtil.getLong(cursor, COL_SRC_ACCOUNT));
        transaction.setPayeeID(DbUtil.getLong(cursor, COL_PAYEE));
        transaction.setCategoryID(DbUtil.getLong(cursor, COL_CATEGORY));
        transaction.setAmount(new BigDecimal(DbUtil.getDouble(cursor, COL_AMOUNT)), Transaction.TRANSACTION_TYPE_UNDEFINED);
        transaction.setProjectID(DbUtil.getLong(cursor, COL_PROJECT));
        transaction.setDepartmentID(DbUtil.getLong(cursor, COL_DEPARTMENT));
        transaction.setLocationID(DbUtil.getLong(cursor, COL_LOCATION));
        transaction.setComment(DbUtil.getString(cursor, COL_COMMENT));
        transaction.setFile(DbUtil.getString(cursor, COL_FILE));
        transaction.setDestAccountID(DbUtil.getLong(cursor, COL_DEST_ACCOUNT));
        transaction.setExchangeRate(new BigDecimal(DbUtil.getDouble(cursor, COL_EXCHANGE_RATE)));
        transaction.setAutoCreated(DbUtil.getBoolean(cursor, COL_AUTO_CREATED));
        transaction.setLat(DbUtil.getDouble(cursor, COL_LAT));
        transaction.setLon(DbUtil.getDouble(cursor, COL_LON));
        transaction.setAccuracy(DbUtil.getInt(cursor, COL_ACCURACY));
        transaction.setSimpleDebtID(DbUtil.getLong(cursor, COL_SIMPLE_DEBT));
        transaction.setFN(DbUtil.getLong(cursor, COL_FN));
        transaction.setFD(DbUtil.getLong(cursor, COL_FD));
        transaction.setFP(DbUtil.getLong(cursor, COL_FP));

        if (DbUtil.getBoolean(cursor, COL_SPLIT)) {
            List<ProductEntry> entries = new ArrayList<>();
            for (IAbstractModel entry : mProductEntrysDAO.getAllEntriesOfTransaction(transaction.getID(), false)) {
                entries.add((ProductEntry) entry);
            }
            transaction.setProductEntries(entries);
        }

        if (cursor.getColumnCount() > mColumnIndexes.size()) {
            transaction.setFromAccountBalance(new BigDecimal(cursor.getDouble(mColumnIndexes.size())));
            transaction.setToAccountBalance(new BigDecimal(cursor.getDouble(mColumnIndexes.size() + 1)));
        }

        if (transaction.getDestAccountID() >= 0) {
            transaction.setTransactionType(Transaction.TRANSACTION_TYPE_TRANSFER);
        }

        return transaction;
    }

    private void updateBalanceForTransaction(Transaction t, int mult, boolean revert) {
        RunningBalanceDAO.updateBalance(mDatabase, revert, t.getAmount().doubleValue(), mult, t.getAccountID(), t.getID(), t.getDateTime().getTime());
        if (t.getDestAccountID() >= 0) {
            RunningBalanceDAO.updateBalance(mDatabase, revert, TransferManager.getDestAmount(t).doubleValue(), mult, t.getDestAccountID(), t.getID(), t.getDateTime().getTime());
        }
    }

    @Override
    public IAbstractModel createModel(IAbstractModel model) throws Exception {
        mDatabase.beginTransaction();
        Transaction transaction = (Transaction) model;

        //если это перевод, то удаляем все его покупки (вдруг это раньше был расход со сплитом)
        if (transaction.getTransactionType() == Transaction.TRANSACTION_TYPE_TRANSFER) {
            transaction.getProductEntries().clear();
        }

        List<IAbstractModel> oldEntries = mProductEntrysDAO.getAllEntriesOfTransaction(transaction.getID(), true);
        ProductEntry defaultEntry = new ProductEntry();
        List<ProductEntry> newEntries = transaction.getProductEntries();

        transaction = (Transaction) super.createModel(model);

//        if (newEntries.isEmpty()) {
//            newEntries.add(new ProductEntry(-1, 0, BigDecimal.ONE, BigDecimal.ZERO, -1, -1, transaction.getID()));
//        }

        List<IAbstractModel> entriesToDelete = new ArrayList<>();
        List<ProductEntry> entriesCreated = new ArrayList<>();

        boolean entryExist;
        for (IAbstractModel oldEntry : oldEntries) {
            if (((ProductEntry) oldEntry).getProductID() == 0) {
                defaultEntry = (ProductEntry) oldEntry;
            } else {
                entryExist = false;
                for (ProductEntry newEntry : newEntries) {
                    entryExist = oldEntry.getID() == newEntry.getID();
                    if (entryExist) break;
                }
                if (!entryExist) {
                    entriesToDelete.add(oldEntry);
                }
            }
        }

        mProductEntrysDAO.bulkDeleteModel(entriesToDelete, true);

        defaultEntry.setPrice(transaction.getAmount().setScale(2, RoundingMode.HALF_EVEN));
        defaultEntry.setTransactionID(transaction.getID());
        defaultEntry.setProductID(0);

        for (ProductEntry newEntry : newEntries) {
            newEntry.setTransactionID(transaction.getID());
            entriesCreated.add((ProductEntry) mProductEntrysDAO.createModel(newEntry));
            defaultEntry.setPrice(defaultEntry.getPrice().subtract(newEntry.getPrice().multiply(newEntry.getQuantity())).setScale(2,RoundingMode.HALF_EVEN));
        }

        mProductEntrysDAO.createModel(defaultEntry);

        transaction.setProductEntries(entriesCreated);

        mDatabase.setTransactionSuccessful();
        mDatabase.endTransaction();
        return transaction;
    }

    @Override
    public synchronized void deleteModel(IAbstractModel model, boolean resetTS, Context context) {
        Transaction transaction = (Transaction) model;
        updateBalanceForTransaction(transaction, -1, true);
        mProductEntrysDAO.bulkDeleteModel(mProductEntrysDAO.getAllEntriesOfTransaction(transaction.getID(), true), true);
        super.deleteModel(model, resetTS, context);
    }

    @Override
    public List<IAbstractModel> bulkDeleteModel(List<IAbstractModel> models, boolean resetTS) {
        mDatabase.beginTransaction();

        for (IAbstractModel model : models) {
            updateBalanceForTransaction((Transaction) model, -1, true);
        }

        HashSet<Long> tids = new HashSet<>();

        for (IAbstractModel model : models) {
            if (!tids.contains(model.getID())) {
                tids.add(model.getID());
            }
        }

        if (!models.isEmpty()) {
            String where = String.format("%s IN (%s)", ProductEntrysDAO.COL_TRANSACTION_ID, TextUtils.join(",", tids));
            mProductEntrysDAO.bulkDeleteModel(mProductEntrysDAO.getModels(where), true);
        }

        List<IAbstractModel> deletedTransactions = super.bulkDeleteModel(models, resetTS, true);

        mDatabase.setTransactionSuccessful();
        mDatabase.endTransaction();

        return deletedTransactions;
    }

    public void bulkUpdateEntity(ArrayList<String> ids, IAbstractModel entity, boolean resetTS) throws Exception {
        String field = entity.getLogTransactionsField();
        if (field == null) return;
        mDatabase.beginTransaction();

        Transaction transaction;
        for (String id : ids) {
            transaction = getTransactionByID(Long.valueOf(id));
            switch (entity.getModelType()) {
                case IAbstractModel.MODEL_TYPE_ACCOUNT:
                    transaction.setAccountID(entity.getID());
                    break;
                case IAbstractModel.MODEL_TYPE_PAYEE:
                    transaction.setPayeeID(entity.getID());
                    break;
                case IAbstractModel.MODEL_TYPE_CATEGORY:
                    transaction.setCategoryID(entity.getID());
                    break;
                case IAbstractModel.MODEL_TYPE_PROJECT:
                    transaction.setProjectID(entity.getID());
                    break;
                case IAbstractModel.MODEL_TYPE_LOCATION:
                    transaction.setLocationID(entity.getID());
                    break;
                case IAbstractModel.MODEL_TYPE_DEPARTMENT:
                    transaction.setDepartmentID(entity.getID());
                    break;
                case IAbstractModel.MODEL_TYPE_SIMPLEDEBT:
                    transaction.setSimpleDebtID(entity.getID());
                    break;
            }
            createModel(transaction);
        }

        mDatabase.setTransactionSuccessful();
        mDatabase.endTransaction();
    }

    @Override
    public void deleteAllModels() {
        mDatabase.execSQL("DELETE FROM " + RunningBalanceDAO.TABLE);
        super.deleteAllModels();
    }

    @SuppressWarnings("unchecked")
    public synchronized Single<List<Transaction>> getRangeTransactionsRx(int first, int count, FilterListHelper filterListHelper, Context context) {
        return Single.fromCallable(() -> getRangeTransactions(first, count, filterListHelper, context));
    }

    @SuppressWarnings("unchecked")
    private synchronized List<Transaction> getRangeTransactions(int first, int count, FilterListHelper filterListHelper, Context context) {

            List < AbstractFilter > filterList = filterListHelper.getFilters();

            String tableName;

            if (filterListHelper.getSearchString().isEmpty()) {
                tableName = TABLE;
            } else {
                createSearchTransactionsTable(Translit.toTranslit(filterListHelper.getSearchString().toLowerCase()).replaceAll("'", "''"));
                tableName = DBHelper.T_SEARCH_TRANSACTIONS;
            }

            String selection = FilterManager.createSelectionString(filterList, AccountsDAO.getInstance(context).getAllModelsIDs(), true,true,true, context);
            String where;
            if (selection != null && !selection.isEmpty()) {
                where = " AND (" + selection + ") ";
            } else {
                where = "";
            }

            String sql = "SELECT DISTINCT lt.*, (frb." + RunningBalanceDAO.COL_INCOME + " + frb." + RunningBalanceDAO.COL_EXPENSE + " + sa." + AccountsDAO.COL_START_BALANCE + ") as FromBalance, (trb." + RunningBalanceDAO.COL_INCOME + " + trb." + RunningBalanceDAO.COL_EXPENSE + " + da." + AccountsDAO.COL_START_BALANCE + ") as ToBalance\n" +
                    "FROM " + tableName + " as lt\n" +
                    "LEFT OUTER JOIN " + AccountsDAO.TABLE + " sa ON sa." + COL_ID + " = " + COL_SRC_ACCOUNT + "\n" +
                    "LEFT OUTER JOIN " + AccountsDAO.TABLE + " da ON da." + COL_ID + " = " + COL_DEST_ACCOUNT + "\n" +
                    "LEFT OUTER JOIN " + RunningBalanceDAO.TABLE + " AS frb ON frb." + RunningBalanceDAO.COL_TRANSACTION_ID + " = lt." + COL_ID + " AND frb." + RunningBalanceDAO.COL_ACCOUNT_ID + " = lt." + COL_SRC_ACCOUNT + "\n" +
                    "LEFT OUTER JOIN " + RunningBalanceDAO.TABLE + " AS trb ON trb." + RunningBalanceDAO.COL_TRANSACTION_ID + " = lt." + COL_ID + " AND trb." + RunningBalanceDAO.COL_ACCOUNT_ID + " = lt." + COL_DEST_ACCOUNT + "\n" +
                    "WHERE lt." + COL_SYNC_DELETED + " = 0" + where + "\n" +
                    "ORDER BY " + COL_DATE_TIME + " DESC\n" +
                    "LIMIT " + first + ", " + count;

            if (BuildConfig.DEBUG) {
                Log.d(SQLTAG, sql);
            }

            Cursor cursor;
            List<Transaction> transactions = new ArrayList<>();
            try {
                cursor = mDatabase.rawQuery(sql, null);
            } catch (Exception e) {
                return transactions;
            }
            if (cursor != null) {
                try {
                    if (cursor.moveToFirst()) {
                        while (!cursor.isAfterLast()) {
                            transactions.add(cursorToTransaction(cursor));
                            cursor.moveToNext();
                        }
                    }
                } finally {
                    cursor.close();
                }
            }
            return transactions;
    }

    public List<Transaction> getTransactionsByQR(Transaction transaction, Context context) {
        Calendar c = Calendar.getInstance(LocaleUtils.getLocale(context));
        c.setTime(transaction.getDateTime());
        c.set(Calendar.HOUR_OF_DAY, 0); //anything 0 - 23
        c.set(Calendar.MINUTE, 0);
        c.set(Calendar.SECOND, 0);
        c.set(Calendar.MILLISECOND, 0);
        Date start = c.getTime();
        c.set(Calendar.HOUR_OF_DAY, 23); //anything 0 - 23
        c.set(Calendar.MINUTE, 59);
        c.set(Calendar.SECOND, 59);
        c.set(Calendar.MILLISECOND, 999);
        Date end = c.getTime();
        String select = String.format(
                        "ROUND(%s,2) = %s AND\n" +
                        "%s > '%s' AND \n" +
                        "%s < '%s' AND \n" +
                        "Deleted = 0",
                COL_AMOUNT, String.valueOf(transaction.getAmount().doubleValue()),
                COL_DATE_TIME, String.valueOf(start.getTime()),
                COL_DATE_TIME, String.valueOf(end.getTime()));

        Cursor cursor = mDatabase.query(TABLE, null,
                select, null,
                null, null, null);

        List<Transaction> transactions = new ArrayList<>();
        if (cursor != null) {
            try {
                if (cursor.moveToFirst()) {
                    while (!cursor.isAfterLast()) {
                        transactions.add(cursorToTransaction(cursor));
                        cursor.moveToNext();
                    }
                }
            } finally {
                cursor.close();
            }
        }
        return transactions;
    }

    @Override
    public List<?> getAllModels() {
        return getAllTransactions();
    }

    @SuppressWarnings("unchecked")
    public List<Transaction> getAllTransactions() {

        return (List<Transaction>) getItems(getTableName(), null,
                null, null,
                String.format("%s %s", COL_DATE_TIME, "desc"),
                null);
    }

    public Transaction getTransactionByID(long id) {
        return (Transaction) getModelById(id);
    }

    /**
     * Создает временную таблицу транзакций на основе log_Transactions  с учетом фильтров
     */
    private synchronized void createTempTransactionsTable(List<AbstractFilter> filterList, Context context) {
        HashSet<Long> allAccountsIDs = AccountsDAO.getInstance(context).getAllModelsIDs();
        //Генерируем условие для SQL запроса
        String filterSelectionSrc = FilterManager.createSelectionString(filterList, allAccountsIDs, false, true, false, context);
        String filterSelectionDst = FilterManager.createSelectionString(filterList, allAccountsIDs, false, false, false, context);
        String filterSrc = filterSelectionSrc.isEmpty() ? "" : "AND " + filterSelectionSrc;
        String filterDst = filterSelectionDst.isEmpty() ? "" : "AND " + filterSelectionDst;
        String filteredAccountsString = TextUtils.join(",", FilterManager.getAccountIDsFromFilters(filterList, allAccountsIDs));

        String sql = "DROP TABLE IF EXISTS temp_all_Transactions;";
        if (BuildConfig.DEBUG) {
            Log.d(SQLTAG, sql);
        }
        mDatabase.execSQL(sql);

        //<editor-fold desc="Mega SQL" defaultstate = "collapsed">
        /*
         * Запрос состоит из трех частей:
         * 1. Объединяем с таблицей log_Products, и все транзакции которые имеют товары дублируются по количеству товаров.
         *  Если у товара есть категория то такая транзакция будет иметь категорию товара, если нет,
         *  то будет иметь свою исходную категорию. Сумма транзакции равна цене товара умноженной на количество
         * 2. Далее идут все транзакции у которых нет товаров
         * 3. Входящие переводы
         */
        sql = "CREATE TEMP TABLE temp_all_Transactions AS \n" +
                "SELECT " +
                "   t._id,\n" +
                "   t.SrcAccount AS Account,\n" +
                "   ROUND(prod.Price*prod.Quantity, 2) AS Amount,\n" +
                "   DateTime,\n" +
                "   Payee,\n" +
                "   CASE WHEN prod.CategoryID < 0 THEN t.Category ELSE prod.CategoryID END AS Category,\n" +
                "   CASE WHEN prod.ProjectID  < 0 THEN t.Project  ELSE prod.ProjectID  END AS Project,\n" +
                "   SimpleDebt,\n" +
                "   CASE WHEN prod.DepartmentID < 0 THEN t.Department ELSE prod.DepartmentID END AS Department,\n" +
                "   Location,\n" +
                "   a.Currency,\n" +
                "   Amount > 0 AS Income,\n " +
                "   IFNULL(a.Currency = b.Currency AND DestAccount IN(" + filteredAccountsString + "), 0) AS ExcludeTransfer\n" +
                "FROM log_Transactions AS t\n" +
                "   INNER JOIN log_Products prod ON t._id = prod.[TransactionID] \n" +
                "   LEFT OUTER JOIN ref_Accounts AS a ON t.SrcAccount = a._id\n" +
                "   LEFT OUTER JOIN ref_Accounts AS b ON t.DestAccount = b._id\n" +
                "WHERE\n" +
                "   t.Deleted = 0 AND prod.Deleted = 0\n" +
                "   AND (SrcAccount != DestAccount) " + filterSrc
                    .replaceAll("Category","CASE WHEN prod.CategoryID < 0 THEN t.Category ELSE prod.CategoryID END")
                    .replaceAll("Project", "CASE WHEN prod.ProjectID  < 0 THEN t.Project  ELSE prod.ProjectID  END")
                    .replaceAll("Department", "CASE WHEN prod.DepartmentID  < 0 THEN t.Department  ELSE prod.DepartmentID  END")
                + "\n" +

                "UNION ALL\n" +

                "SELECT " +
                "   t._id,\n" +
                "   t.DestAccount AS Account,\n" +
                "   Amount*ExchangeRate*-1,\n" +
                "   DateTime,\n" +
                "   Payee,\n" +
                "   Category,\n" +
                "   Project,\n" +
                "   SimpleDebt,\n" +
                "   Department,\n" +
                "   Location,\n" +
                "   a.Currency,\n" +
                "   1 AS Income,\n " +
                "   IFNULL(a.Currency = b.Currency AND SrcAccount IN(" + filteredAccountsString + "), 0) AS ExcludeTransfer\n" +
                "FROM\n" +
                "   log_Transactions AS t\n" +
                "   LEFT OUTER JOIN ref_Accounts AS a ON t.DestAccount = a._id\n" +
                "   LEFT OUTER JOIN ref_Accounts AS b ON t.SrcAccount = b._id\n" +
                "WHERE " +
                "   t.Deleted = 0\n" +
                "   AND DestAccount >= 0\n" +
                "   AND (SrcAccount != DestAccount) " + filterDst+";";
        //</editor-fold>

        if (BuildConfig.DEBUG) {
            Log.d(SQLTAG, sql);
        }
        mDatabase.execSQL(sql);
        sql = "CREATE INDEX [temp_all_Transactions_Income] ON [temp_all_Transactions] (Income);";
        if (BuildConfig.DEBUG) {
            Log.d(SQLTAG, sql);
        }
        mDatabase.execSQL(sql);
    }

    /**
     * Создает временную таблицу транзакций на основе log_Transactions  с учетом фильтров и поискового запроса
     */
    private synchronized void createTempTransactionsTable(List<AbstractFilter> filterList, String searchString, Context context) {
        HashSet<Long> allAccountsIDs = AccountsDAO.getInstance(context).getAllModelsIDs();
        //Генерируем условие для SQL запроса
        String filterSelectionSrc = FilterManager.createSelectionString(filterList, allAccountsIDs, false, true, false, context);
        String filterSelectionDst = FilterManager.createSelectionString(filterList, allAccountsIDs, false, false, false, context);
        String filterSrc = filterSelectionSrc.isEmpty() ? "" : "AND " + filterSelectionSrc;
        String filterDst = filterSelectionDst.isEmpty() ? "" : "AND " + filterSelectionDst;
        String filteredAccountsString = TextUtils.join(",", FilterManager.getAccountIDsFromFilters(filterList, allAccountsIDs));

        String sql = "DROP TABLE IF EXISTS temp_all_Transactions;";
        if (BuildConfig.DEBUG) {
            Log.d(SQLTAG, sql);
        }
        mDatabase.execSQL(sql);

        //<editor-fold desc="Mega SQL" defaultstate = "collapsed">
        sql = "CREATE TEMP TABLE temp_all_Transactions AS \n" +

                "SELECT t._id,\n" +
                "   t.SrcAccount     AS AccountID,\n" +
                "   ROUND(prod.Price*prod.Quantity, 2) AS Amount,DateTime, a.Currency,\n" +
                "   ROUND(prod.Price*prod.Quantity, 2) > 0       AS Income,\n" +
                "   IFNULL(a.Currency = b.Currency AND DestAccount IN(" + filteredAccountsString + "), 0) AS ExcludeTransfer,\n" +
                "   a.Name           AS AccountName,\n" +
                "   s.Name           AS SimpleDebtName,\n" +
                "   a.SearchString   AS AccountName,\n" +
                "   s.SearchString   AS SimpleDebtName,\n" +
                "   p.SearchString   AS PayeeName,\n" +
                "   c.SearchString   AS CategoryName,\n" +
                "   pr.SearchString  AS ProjectName,\n" +
                "   d.SearchString   AS DepartmentName,\n" +
                "   l.SearchString   AS LocationName\n" +
                "FROM log_Transactions AS t\n" +
                "   INNER JOIN log_Products prod ON t._id = prod.TransactionID \n" +
                "   LEFT OUTER JOIN ref_Accounts AS a ON t.SrcAccount = a._id\n" +
                "   LEFT OUTER JOIN ref_Accounts AS b ON t.DestAccount = b._id\n" +
                "   LEFT OUTER JOIN ref_Categories AS c ON CASE WHEN prod.CategoryID < 0 THEN t.Category ELSE prod.CategoryID END = c._id\n" +
                "   LEFT OUTER JOIN ref_Payees  AS p ON t.Payee = p._id\n" +
                "   LEFT OUTER JOIN ref_Locations AS l ON t.Location = l._id\n" +
                "   LEFT OUTER JOIN ref_Projects AS pr ON CASE WHEN prod.ProjectID < 0 THEN t.Project ELSE prod.ProjectID END = pr._id\n" +
                "   LEFT OUTER JOIN ref_SimpleDebts AS s ON t.SimpleDebt = s._id\n" +
                "   LEFT OUTER JOIN ref_Departments AS d ON t.Department = d._id\n" +
                "WHERE t.Deleted = 0 AND prod.Deleted = 0\n" +
                "   AND (SrcAccount != DestAccount) " + filterSrc
                    .replaceAll("Category","CASE WHEN prod.CategoryID < 0 THEN t.Category ELSE prod.CategoryID END")
                    .replaceAll("Project", "CASE WHEN prod.ProjectID  < 0 THEN t.Project  ELSE prod.ProjectID  END")
                            + "\n" +
                "   AND (AccountName    LIKE '%" + searchString + "%'\n" +
                "   OR SimpleDebtName   LIKE '%" + searchString + "%'\n" +
                "   OR PayeeName        LIKE '%" + searchString + "%'\n" +
                "   OR CategoryName     LIKE '%" + searchString + "%'\n" +
                "   OR PayeeName        LIKE '%" + searchString + "%'\n" +
                "   OR ProjectName      LIKE '%" + searchString + "%'\n" +
                "   OR DepartmentName   LIKE '%" + searchString + "%'\n" +
                "   OR LocationName     LIKE '%" + searchString + "%')\n" +

                "UNION ALL\n" +

                "SELECT t._id,\n" +
                "   t.DestAccount   AS AccountID,\n" +
                "   Amount*ExchangeRate*-1 AS Amount1,DateTime, a.Currency,\n" +
                "   1 > 0           AS Income,\n" +
                "   IFNULL(a.Currency = b.Currency AND SrcAccount IN(" + filteredAccountsString + "), 0) AS ExcludeTransfer,\n" +
                "   a.Name          AS AccountName,\n" +
                "   s.Name          AS SimpleDebtName,\n" +
                "   a.SearchString  AS AccountName,\n" +
                "   s.SearchString  AS SimpleDebtName,\n" +
                "   p.SearchString  AS PayeeName,\n" +
                "   c.SearchString  AS CategoryName,\n" +
                "   pr.SearchString AS ProjectName,\n" +
                "   d.SearchString  AS DepartmentName,\n" +
                "   l.SearchString  AS LocationName\n" +
                "FROM log_Transactions AS t " +
                "   LEFT OUTER JOIN ref_Accounts    AS a  ON t.DestAccount  = a._id\n" +
                "   LEFT OUTER JOIN ref_Accounts    AS b  ON t.SrcAccount   = b._id\n" +
                "   LEFT OUTER JOIN ref_Categories  AS c  ON t.Category     = c._id\n" +
                "   LEFT OUTER JOIN ref_Payees      AS p  ON t.Payee        = p._id\n" +
                "   LEFT OUTER JOIN ref_Locations   AS l  ON t.Location     = l._id\n" +
                "   LEFT OUTER JOIN ref_Projects    AS pr ON t.Project      = pr._id\n" +
                "   LEFT OUTER JOIN ref_SimpleDebts AS s  ON t.SimpleDebt   = s._id\n" +
                "   LEFT OUTER JOIN ref_Departments AS d  ON t.Department   = d._id\n" +
                "WHERE t.Deleted = 0 " +
                "   AND DestAccount >= 0 " +
                "   AND (SrcAccount != DestAccount) " + filterDst + "\n" +
                "   AND (AccountName    LIKE '%" + searchString + "%'\n" +
                "   OR SimpleDebtName   LIKE '%" + searchString + "%'\n" +
                "   OR PayeeName        LIKE '%" + searchString + "%'\n" +
                "   OR CategoryName     LIKE '%" + searchString + "%'\n" +
                "   OR PayeeName        LIKE '%" + searchString + "%'\n" +
                "   OR ProjectName      LIKE '%" + searchString + "%'\n" +
                "   OR DepartmentName   LIKE '%" + searchString + "%'\n" +
                "   OR LocationName     LIKE '%" + searchString + "%');";
        //</editor-fold>

        if (BuildConfig.DEBUG) {
            Log.d(SQLTAG, sql);
        }
        mDatabase.execSQL(sql);
        sql = "CREATE INDEX [temp_all_Transactions_Income] ON [temp_all_Transactions] (Income);";
        if (BuildConfig.DEBUG) {
            Log.d(SQLTAG, sql);
        }
        mDatabase.execSQL(sql);
    }

    private synchronized void createSearchTransactionsTable(String searchString) {
//        if (searchString.equals(mLastSearchString)) return;
        String sql = "DROP TABLE IF EXISTS " + DBHelper.T_SEARCH_TRANSACTIONS + ";";
        if (BuildConfig.DEBUG) {
            Log.d(SQLTAG, sql);
        }
        mDatabase.execSQL(sql);

        //<editor-fold desc="SQL"  defaultstate = "collapsed">
        sql = "CREATE TEMP TABLE " + DBHelper.T_SEARCH_TRANSACTIONS + " AS \n" +
                "SELECT t.*\n" +
                "FROM log_Transactions AS t " +
                "   INNER JOIN log_Products prod ON t._id = prod.TransactionID \n" +
                "   LEFT OUTER JOIN ref_Accounts    AS a  ON t.SrcAccount   = a._id\n" +
                "   LEFT OUTER JOIN ref_Categories AS c ON CASE WHEN prod.CategoryID < 0 THEN t.Category ELSE prod.CategoryID END = c._id\n" +
//                "   LEFT OUTER JOIN ref_Categories  AS c  ON t.Category     = c._id\n" +
                "   LEFT OUTER JOIN ref_Payees      AS p  ON t.Payee        = p._id\n" +
                "   LEFT OUTER JOIN ref_Locations   AS l  ON t.Location     = l._id\n" +
                "   LEFT OUTER JOIN ref_Projects AS pr ON CASE WHEN prod.ProjectID < 0 THEN t.Project ELSE prod.ProjectID END = pr._id\n" +
//                "   LEFT OUTER JOIN ref_Projects    AS pr ON t.Project      = pr._id\n" +
                "   LEFT OUTER JOIN ref_SimpleDebts AS s  ON t.SimpleDebt   = s._id\n" +
                "   LEFT OUTER JOIN ref_Departments AS d  ON t.Department   = d._id\n" +
                "WHERE t.Deleted = 0 \n" +
                "    AND (lower(t.SearchString) LIKE '%" + searchString + "%'\n" +
                "    OR lower(a.SearchString)   LIKE '%" + searchString + "%'\n" +
                "    OR lower(s.SearchString)   LIKE '%" + searchString + "%'\n" +
                "    OR lower(p.SearchString)   LIKE '%" + searchString + "%'\n" +
                "    OR lower(c.SearchString)   LIKE '%" + searchString + "%'\n" +
                "    OR lower(pr.SearchString)  LIKE '%" + searchString + "%'\n" +
                "    OR lower(d.SearchString)   LIKE '%" + searchString + "%'\n" +
                "    OR lower(l.SearchString)   LIKE '%" + searchString + "%');";
        //</editor-fold>
        if (BuildConfig.DEBUG) {
            Log.d(SQLTAG, sql);
        }
        mDatabase.execSQL(sql);
    }

    public void getSumForSimpleDebt(SimpleDebt simpleDebt) {

        String sql = "SELECT TOTAL(amount) AS Sum,\n" +
                "SUM ( CASE WHEN Amount < 0 THEN Amount ELSE 0 END) AS OweMe,\n" +
                "SUM ( CASE WHEN Amount > 0 THEN Amount ELSE 0 END) AS IOwe\n" +
                "FROM ref_Accounts a JOIN ref_Currencies ON ref_Currencies._id = Currency\n" +
                "     JOIN (SELECT * FROM log_Transactions WHERE SimpleDebt = " + String.valueOf(simpleDebt.getID()) + " AND DestAccount < 0 AND Deleted = 0) t ON a._id = t.SrcAccount\n" +
                "WHERE Currency = " + String.valueOf(simpleDebt.getCabbageID());

        Cursor cursor = mDatabase.rawQuery(sql, null);

        if (cursor != null) {
            if (cursor.moveToFirst()) {
                try {
                    simpleDebt.setAmount(new BigDecimal(cursor.getDouble(0)));
                    simpleDebt.setOweMe(new BigDecimal(cursor.getDouble(1)));
                    simpleDebt.setIOwe(new BigDecimal(cursor.getDouble(2)));
                } finally {
                    cursor.close();
                }
            }
        }
    }

    public synchronized Single<ListSumsByCabbage> getGroupedSumsRx(FilterListHelper filterListHelper,
                                                         boolean takeSearchString,
                                                         ArrayList<Long> selectedIDs,
                                                         Context context) {
        return Single.fromCallable(() -> getGroupedSums(filterListHelper, takeSearchString, selectedIDs, context));
    }

    /**
     * @param filterListHelper - автоматически добавляет фильтр исключающий закрытые счета
     */
    @SuppressWarnings("unchecked")
    public synchronized ListSumsByCabbage getGroupedSums(FilterListHelper filterListHelper,
                                                         boolean takeSearchString,
                                                         ArrayList<Long> selectedIDs,
                                                         Context context) {
        //Создаем экземпляр результирующей записи "валюта - сумма"
        ListSumsByCabbage listSumsByCabbage = new ListSumsByCabbage();

        //Получаем актуальный список фильтров
        List<AbstractFilter> filterList = filterListHelper.getFilters();

        if (filterListHelper.getSearchString().isEmpty() || !takeSearchString) {
            createTempTransactionsTable(filterList, context);
        } else {
            createTempTransactionsTable(filterList, Translit.toTranslit(filterListHelper.getSearchString().toLowerCase()).replaceAll("'", "''"), context);
        }

        HashSet<Long> accIDs = FilterManager.getAccountIDsFromFilters(filterList, AccountsDAO.getInstance(context).getAllModelsIDs());
        String accIDsString = TextUtils.join(",", accIDs);

        String selectedTransactionsTable = "";
        if (selectedIDs != null && !selectedIDs.isEmpty()) {
            mDatabase.execSQL("DROP TABLE IF EXISTS temp_sel_Transactions;");
            mDatabase.execSQL("CREATE TEMP TABLE temp_sel_Transactions (_id INTEGER NOT NULL PRIMARY KEY)");
            mDatabase.beginTransaction();
            ContentValues cv = new ContentValues();
            for (Long id : selectedIDs) {
                cv.put("_id", id);
                mDatabase.insert("temp_sel_Transactions", null, cv);
            }
            mDatabase.setTransactionSuccessful();
            mDatabase.endTransaction();
            selectedTransactionsTable = " AS t INNER JOIN temp_sel_Transactions AS st ON t._id = st._id ";
        }

        String sql = "SELECT\n" +
                "c._id,\n" +
                "IFNULL((SELECT TOTAL(CASE WHEN Income = 1 THEN Amount ELSE 0 END) FROM temp_all_Transactions" + selectedTransactionsTable + " WHERE c._id = Currency AND NOT (ExcludeTransfer = 1)), 0) AS InAmountSum,\n" +
                "IFNULL((SELECT TOTAL(CASE WHEN Income = 0 THEN Amount ELSE 0 END) FROM temp_all_Transactions" + selectedTransactionsTable + " WHERE c._id = Currency AND NOT (ExcludeTransfer = 1)), 0) AS OutAmountSum,\n" +
                "IFNULL((SELECT TOTAL(StartBalance) FROM ref_Accounts WHERE Currency = c._id AND _id IN (" + accIDsString + ")), 0) AS StartBalance\n" +
                "FROM ref_Currencies c\n" +
                "ORDER BY c.OrderNumber ASC";
        if (BuildConfig.DEBUG) {
            Log.d(SQLTAG, sql);
        }

        Cursor cursor = mDatabase.rawQuery(sql, null);
        if (cursor != null) {
            try {
                if (cursor.moveToFirst()) {
                    SumsByCabbage sumsByCabbage;
                    while (!cursor.isAfterLast()) {
                        sumsByCabbage = new SumsByCabbage(cursor.getLong(0),
                                new BigDecimal(cursor.getDouble(1)),
                                new BigDecimal(cursor.getDouble(2)));
                        sumsByCabbage.setStartBalance(new BigDecimal(cursor.getDouble(3)));
                        listSumsByCabbage.getmList().add(sumsByCabbage);
                        cursor.moveToNext();
                    }
                }
            } finally {
                cursor.close();
            }
        }

        return listSumsByCabbage;
    }

    public synchronized Single<List<SummaryItem>> getSummaryGroupedSumsRx(@NonNull List<SummaryItem> intervals,
                                                   FilterListHelper filterListHelper,
                                                   @NonNull Context context){
        return Single.fromCallable(() -> getSummaryGroupedSums(intervals, filterListHelper, context));
    }

    @SuppressWarnings("unchecked")
    private synchronized List<SummaryItem> getSummaryGroupedSums(@NonNull List<SummaryItem> intervals,
                                                                 FilterListHelper filterListHelper,
                                                                 @NonNull Context context) {
        SummaryItem summaryItem;

        //Получаем список фильтров
        List<AbstractFilter> filterList = filterListHelper.getFilters();

        createTempTransactionsTable(filterList, context);

        int intervalStart;
        int intervalEnd;
        Date minDate;
        Date maxDate;
        SumsByCabbage sumsByCabbage;
        Cursor cursor;

        for (int j = 0; j < intervals.size(); j++) {
            summaryItem = intervals.get(j);
            intervalStart = intervals.get(j).getIntervalFirst();
            intervalEnd = intervals.get(j).getIntervalSecond();
            minDate = DateRangeFilter.getRangeStart(intervalStart, intervalEnd, context);
            maxDate = DateRangeFilter.getRangeEnd(intervalStart, intervalEnd, context);
            cursor = mDatabase.rawQuery("SELECT\n" +
                    "Currency,\n" +
                    "TOTAL(CASE WHEN Income = 1 THEN Amount ELSE 0 END) AS InAmountSum,\n" +
                    "TOTAL(CASE WHEN Income = 0 THEN Amount ELSE 0 END) AS OutAmountSum\n" +
                    "FROM temp_all_Transactions\n" +
                    "WHERE DateTime >= " + String.valueOf(minDate.getTime()) + " AND DateTime <= " + String.valueOf(maxDate.getTime()) + " AND NOT (ExcludeTransfer = 1)\n" +
                    "GROUP BY Currency;", null);
            if (cursor != null) {
                try {
                    if (cursor.moveToFirst()) {
                        while (!cursor.isAfterLast()) {
                            sumsByCabbage = new SumsByCabbage(cursor.getLong(0), new BigDecimal(cursor.getDouble(1)), new BigDecimal(cursor.getDouble(2)));
                            sumsByCabbage.setStartBalance(new BigDecimal(cursor.getDouble(0)));
                            summaryItem.getListSumsByCabbage().getmList().add(sumsByCabbage);
                            cursor.moveToNext();
                        }
                    }
                } finally {
                    cursor.close();
                }
            }
        }
        return intervals;
    }

    private String getEntityField(int modelType) throws Exception {
        switch (modelType) {
            case IAbstractModel.MODEL_TYPE_ACCOUNT:
                return "Account";
            case IAbstractModel.MODEL_TYPE_CATEGORY:
                return COL_CATEGORY;
            case IAbstractModel.MODEL_TYPE_PROJECT:
                return COL_PROJECT;
            case IAbstractModel.MODEL_TYPE_SIMPLEDEBT:
                return COL_SIMPLE_DEBT;
            case IAbstractModel.MODEL_TYPE_PAYEE:
                return COL_PAYEE;
            case IAbstractModel.MODEL_TYPE_LOCATION:
                return COL_LOCATION;
            case IAbstractModel.MODEL_TYPE_DEPARTMENT:
                return COL_DEPARTMENT;
            default:
                throw new Exception();
        }
    }

    public synchronized LongSparseArray<LongSparseArray<IAbstractModel>> getEntityReport(int modelType, FilterListHelper filterListHelper, Context context) throws Exception {
        LongSparseArray<LongSparseArray<IAbstractModel>> map = new LongSparseArray<>();
        AbstractDAO abstractDAO = BaseDAO.getDAO(modelType, context);
        if (abstractDAO == null) return map;
        List<AbstractFilter> filterList = filterListHelper.getFilters();
        long cabbageID;
        long modelID;
        IAbstractModel model;
        LongSparseArray<IAbstractModel> models;
        String entityField = getEntityField(modelType);
        String excludeTransfers = modelType == IAbstractModel.MODEL_TYPE_ACCOUNT ? "" : "WHERE  ExcludeTransfer != 1 ";

        createTempTransactionsTable(filterList, context);

        String sql = "SELECT\n" +
                "Currency, " + entityField + ",\n" +
                "TOTAL(CASE WHEN Income = 1 THEN Amount ELSE 0 END) AS InAmountSum,\n" +
                "TOTAL(CASE WHEN Income = 0 THEN Amount ELSE 0 END) AS OutAmountSum\n" +
                "FROM temp_all_Transactions\n" +
                excludeTransfers +
                "GROUP BY Currency, " + entityField + ";";
        if (BuildConfig.DEBUG) {
            Log.d(SQLTAG, sql);
        }

        Cursor cursor = mDatabase.rawQuery(sql, null);
        if (cursor != null) {
            try {
                if (cursor.moveToFirst()) {
                    while (!cursor.isAfterLast()) {
                        cabbageID = cursor.getLong(0);
                        modelID = cursor.getLong(1);
                        if (modelID > 0) {
                            model = abstractDAO.getModelById(modelID);
                        } else {
                            model = abstractDAO.createEmptyModel();
                        }
                        model.setIncome(new BigDecimal(cursor.getDouble(2)).abs());
                        model.setExpense(new BigDecimal(cursor.getDouble(3)).abs());
                        if (map.indexOfKey(cabbageID) < 0) {
                            map.put(cabbageID, new LongSparseArray<>());
                        }
                        models = map.get(cabbageID);
                        models.put(model.getID(), model);
                        cursor.moveToNext();
                    }
                }
            } finally {
                cursor.close();
            }
        }
        return map;
    }

    public synchronized LinkedHashMap<Long, List<DateEntry>> getCommonDateReport(String datePattern, String dateFormat, FilterListHelper filterListHelper, Context context) throws Exception {
        LinkedHashMap<Long, List<DateEntry>> map = new LinkedHashMap<>();
        List<AbstractFilter> filterList = filterListHelper.getFilters();
        Date date;
        BigDecimal income;
        BigDecimal expense;
        long cabbageID;
        List<DateEntry> entries;
        @SuppressLint("SimpleDateFormat") DateFormat df = new SimpleDateFormat(dateFormat);

        createTempTransactionsTable(filterList, context);

        String sql = "SELECT \n" +
                "Currency, \n" +
                "strftime('" + datePattern + "', DateTime/1000, 'unixepoch', 'localtime') AS Date,\n" +
                "TOTAL(CASE WHEN Income = 1 THEN Amount ELSE 0 END) AS InAmountSum,\n" +
                "TOTAL(CASE WHEN Income = 0 THEN Amount ELSE 0 END) AS OutAmountSum\n" +
                "FROM temp_all_Transactions\n" +
                "WHERE  ExcludeTransfer != 1\n" +
                "GROUP BY Currency, strftime('" + datePattern + "', DateTime/1000, 'unixepoch', 'localtime');";
        if (BuildConfig.DEBUG) {
            Log.d(SQLTAG, sql);
        }

        Cursor cursor = mDatabase.rawQuery(sql, null);
        if (cursor != null) {
            try {
                if (cursor.moveToFirst()) {
                    while (!cursor.isAfterLast()) {
                        cabbageID = cursor.getLong(0);
                        date = df.parse(cursor.getString(1));
                        income = new BigDecimal(cursor.getDouble(2)).abs();
                        expense = new BigDecimal(cursor.getDouble(3)).abs();
                        if (!map.containsKey(cabbageID)) {
                            map.put(cabbageID, new ArrayList<>());
                        }
                        entries = map.get(cabbageID);
                        entries.add(new DateEntry(date, income, expense));
                        cursor.moveToNext();
                    }
                }
            } finally {
                cursor.close();
            }
        }

        return map;
    }

    public Transaction hasDuplicates(Transaction transaction) {
        /*
        SELECT *
        FROM log_Transactions
        WHERE
            SrcAccount = 1 AND
            ROUND(Amount,2) = -134 AND
            DATETIME(DateTime) > DATETIME('2016-02-22 00:00:00') AND
            DATETIME(DateTime) < DATETIME('2016-02-22 20:00:00')
        */
//        DateTimeFormatter df = DateTimeFormatter.getInstance(context);
        String select = String.format(/*"SELECT * \n" +
                        "FROM %s \n" +
                        "WHERE \n" +*/
                "    %s = %s AND\n" +
                        "    ROUND(%s,2) = %s AND\n" +
                        "    %s > '%s' AND \n" +
                        "    %s < '%s' AND \n" +
                        "    Deleted = 0", /*DBHelper.T_LOG_TRANSACTIONS,*/
                COL_SRC_ACCOUNT, String.valueOf(transaction.getAccountID()),
                COL_AMOUNT, String.valueOf(transaction.getAmount().doubleValue()),
                COL_DATE_TIME, String.valueOf(transaction.getDateTime().getTime() - 60000 * 30),
                COL_DATE_TIME, String.valueOf(transaction.getDateTime().getTime() + 60000 * 30));

//        Log.d(TAG, select);


        Cursor cursor = mDatabase.query(TABLE, null,
                select, null,
                null, null, null);

        if (cursor.getCount() > 0) {
            cursor.moveToFirst();
            Transaction existsTransaction = cursorToTransaction(cursor);
            cursor.close();

            return existsTransaction;
        } else {
            cursor.close();

            return new Transaction(-1);
        }
    }

    public Transaction getLastTransactionForFN(Transaction transaction) {
        Transaction lastTransaction = new Transaction(-1);
        String sql = "SELECT * FROM log_Transactions "+
                "WHERE Deleted = 0 AND FN =" + String.valueOf(transaction.getFN()) + "\n" +
                "ORDER BY DateTime DESC\n" +
                "LIMIT 1";
        Cursor cursor;
        try {
            cursor = mDatabase.rawQuery(sql, null);
        } catch (Exception e) {
            return lastTransaction;
        }
        if (cursor != null) {
            try {
                if (cursor.moveToFirst()) {
                    while (!cursor.isAfterLast()) {
                        lastTransaction = cursorToTransaction(cursor);
                        cursor.moveToNext();
                    }
                }
            } finally {
                cursor.close();
            }
        }
        return lastTransaction;
    }

    public boolean isTransactionLastForAccount(Transaction transaction) {
        /*
        SELECT *
        FROM log_Transactions
        WHERE
            SrcAccount = 1 AND
            DateTime > 123456789)
        */
        String select = String.format("%s = %s AND %s > '%s' AND Deleted = 0",
                COL_SRC_ACCOUNT, String.valueOf(transaction.getAccountID()),
                COL_DATE_TIME, String.valueOf(transaction.getDateTime()));


        Cursor cursor = mDatabase.query(TABLE, null,
                select, null,
                null, null, null);

        boolean result = cursor.getCount() == 0;
        cursor.close();

        return result;
    }

    public Pair<Date, Date> getFullDateRange() {
        Date first = new Date();
        Date last = new Date();
        String minCol = String.format("MIN(%s) AS minDate", COL_DATE_TIME);
        String maxCol = String.format("MAX(%s) AS maxDate", COL_DATE_TIME);

        Cursor cursor = mDatabase.query(TABLE, new String[]{minCol, maxCol},
                "Deleted = 0", null, null, null, null);
        if (cursor.getCount() == 0) {
            cursor.close();
            return new Pair<>(first, last);
        }
        cursor.moveToFirst();
        first = new Date(cursor.getLong(cursor.getColumnIndex("minDate")));
        last = new Date(cursor.getLong(cursor.getColumnIndex("maxDate")));
        cursor.close();

        return new Pair<>(first, last);
    }
}
