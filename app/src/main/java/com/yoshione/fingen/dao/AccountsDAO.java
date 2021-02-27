package com.yoshione.fingen.dao;

import android.content.Context;
import android.database.Cursor;
import android.util.Pair;

import com.yoshione.fingen.db.DbUtil;
import com.yoshione.fingen.interfaces.IAbstractModel;
import com.yoshione.fingen.interfaces.IDaoInheritor;
import com.yoshione.fingen.model.Account;
import com.yoshione.fingen.utils.Lg;
import com.yoshione.fingen.utils.SmsParser;

import java.math.BigDecimal;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;

import io.reactivex.Single;

public class AccountsDAO extends BaseDAO implements AbstractDAO, IDaoInheritor {

    //<editor-fold desc="ref_Accounts">
    public static final String TABLE = "ref_Accounts";

    public static final String COL_TYPE = "Type";
    public static final String COL_CURRENCY = "Currency";
    public static final String COL_EMITENT = "Emitent";
    public static final String COL_LAST4DIGITS = "Last4Digits";
    public static final String COL_COMMENT = "Comment";
    public static final String COL_START_BALANCE = "StartBalance";
    public static final String COL_IS_CLOSED = "IsClosed";
    public static final String COL_ORDER = "CustomOrder";
    public static final String COL_CREDIT_LIMIT = "CreditLimit";
    public static final String COL_IS_INCLUDE_INTO_TOTALS = "IsIncludeIntoTotals";
    public static final String COL_INCOME = "Income";
    public static final String COL_EXPENSE = "Expense";

    public static final String SELECT_INCOME = "(SELECT " + RunningBalanceDAO.COL_INCOME + " FROM " + RunningBalanceDAO.TABLE + " WHERE " + RunningBalanceDAO.COL_ACCOUNT_ID + " = " + COL_ID + " AND " + RunningBalanceDAO.COL_DATE_TIME + " = (SELECT MAX(" + RunningBalanceDAO.COL_DATE_TIME + ") FROM " + RunningBalanceDAO.TABLE + " WHERE " + RunningBalanceDAO.COL_ACCOUNT_ID + " = " + COL_ID + " )) AS " + COL_INCOME;
    public static final String SELECT_EXPENSE = "(SELECT " + RunningBalanceDAO.COL_EXPENSE + " FROM " + RunningBalanceDAO.TABLE + " WHERE " + RunningBalanceDAO.COL_ACCOUNT_ID + " = " + COL_ID + " AND " + RunningBalanceDAO.COL_DATE_TIME + " = (SELECT MAX(" + RunningBalanceDAO.COL_DATE_TIME + ") FROM " + RunningBalanceDAO.TABLE + " WHERE " + RunningBalanceDAO.COL_ACCOUNT_ID + " = " + COL_ID + " )) AS " + COL_EXPENSE;

    public static final String[] ALL_COLUMNS = joinArrays(COMMON_COLUMNS, new String[]{
            COL_TYPE, COL_NAME, COL_CURRENCY, COL_EMITENT, COL_LAST4DIGITS, COL_COMMENT,
            COL_START_BALANCE, COL_IS_CLOSED, COL_ORDER, COL_CREDIT_LIMIT,
            COL_IS_INCLUDE_INTO_TOTALS, SELECT_INCOME, SELECT_EXPENSE, COL_SEARCH_STRING
    });

    public static final String SQL_CREATE_TABLE = "CREATE TABLE " + TABLE + " ("
            + COMMON_FIELDS +               ", "
            + COL_TYPE +                    " INTEGER NOT NULL, "
            + COL_NAME +                    " TEXT NOT NULL, "
            + COL_CURRENCY +                " INTEGER REFERENCES [" + CabbagesDAO.TABLE + "]([" + COL_ID + "]) ON DELETE SET NULL ON UPDATE CASCADE, "
            + COL_EMITENT +                 " TEXT, "
            + COL_LAST4DIGITS +             " INTEGER, "
            + COL_COMMENT +                 " TEXT, "
            + COL_START_BALANCE +           " REAL NOT NULL, "
            + COL_IS_CLOSED +               " INTEGER NOT NULL, "
            + COL_ORDER +                   " INTEGER, "
            + COL_CREDIT_LIMIT +            " REAL, "
            + COL_IS_INCLUDE_INTO_TOTALS +  " INTEGER NOT NULL DEFAULT 1, "
            + COL_SEARCH_STRING +           " TEXT, "
            + "UNIQUE (" + COL_NAME + ", " + COL_SYNC_DELETED + ") ON CONFLICT ABORT);";
    //</editor-fold>

    private static AccountsDAO sInstance;

    public synchronized static AccountsDAO getInstance(Context context) {
        if (sInstance == null) {
            sInstance = new AccountsDAO(context);
        }
        return sInstance;
    }

    private AccountsDAO(Context context) {
        super(context, TABLE, ALL_COLUMNS);
        super.setDaoInheritor(this);
    }

    @Override
    public IAbstractModel createEmptyModel() {
        return new Account();
    }

    @Override
    public IAbstractModel cursorToModel(Cursor cursor) {
        return cursorToAccount(cursor);
    }

    private synchronized Account cursorToAccount(Cursor cursor) {
        Account account = new Account();

        account.setID(DbUtil.getLong(cursor, COL_ID));
        account.setAccountType(Account.AccountType.values()[DbUtil.getInt(cursor, COL_TYPE)]);
        account.setName(DbUtil.getString(cursor, COL_NAME));
        account.setCabbageId(DbUtil.getLong(cursor, COL_CURRENCY));
        account.setEmitent(DbUtil.getString(cursor, COL_EMITENT));
        account.setLast4Digits(DbUtil.getInt(cursor, COL_LAST4DIGITS));
        account.setComment(DbUtil.getString(cursor, COL_COMMENT));
        account.setStartBalance(new BigDecimal(DbUtil.getDouble(cursor, COL_START_BALANCE)));
        account.setIncome(new BigDecimal(DbUtil.getDouble(cursor, COL_INCOME)));
        account.setExpense(new BigDecimal(DbUtil.getDouble(cursor, COL_EXPENSE)));

        account.setIsClosed(DbUtil.getInt(cursor, COL_IS_CLOSED) == 1);
        account.setOrder(DbUtil.getInt(cursor, COL_ORDER));

        account.setCreditLimit(new BigDecimal(DbUtil.getDouble(cursor, COL_CREDIT_LIMIT)));

        return account;
    }

    @Override
    public void deleteModel(IAbstractModel model, boolean resetTS, Context context) {
        Lg.log("Start deleting account (ID %s; resetTS %s)", String.valueOf(model.getID()), String.valueOf(resetTS));

        //Удаляем все транзакции в которых участвует этот счет
        Lg.log("delete transactions...");
        TransactionsDAO transactionsDAO = TransactionsDAO.getInstance(context);
        transactionsDAO.bulkDeleteModel(transactionsDAO.getModels(String.format("%s = %s OR %s = %s", TransactionsDAO.COL_SRC_ACCOUNT,
                String.valueOf(model.getID()), TransactionsDAO.COL_DEST_ACCOUNT, String.valueOf(model.getID()))), resetTS);

        //Удаляем все шаблоны с этим счетом
        Lg.log("delete templates...");
        TemplatesDAO templatesDAO = TemplatesDAO.getInstance(context);
        templatesDAO.bulkDeleteModel(templatesDAO.getModels(String.format("%s = %s OR %s = %s", TemplatesDAO.COL_SRC_ACCOUNT,
                String.valueOf(model.getID()), TemplatesDAO.COL_DEST_ACCOUNT, String.valueOf(model.getID()))), resetTS);

        //Удаляем все долги, привязанные к счету
        Lg.log("delete credits...");
        CreditsDAO creditsDAO = CreditsDAO.getInstance(context);
        creditsDAO.bulkDeleteModel(creditsDAO.getModels(String.format("%s = %s", CreditsDAO.COL_ACCOUNT, String.valueOf(model.getID()))), resetTS);

        //Удаляем все долги, привязанные к счету
        Lg.log("delete AccountsSetsLogs...");
        AccountsSetsLogDAO accountsSetsLogDAO = AccountsSetsLogDAO.getInstance(context);
        accountsSetsLogDAO.bulkDeleteModel(accountsSetsLogDAO.getModels(String.format("%s = %s", AccountsSetsLogDAO.COL_ACCOUNT_ID, String.valueOf(model.getID()))), resetTS);

        //Удаляем все маркеры, соответсвующие счету
        Lg.log("delete sms markers...");
        SmsMarkersDAO smsMarkersDAO = SmsMarkersDAO.getInstance(context);
        smsMarkersDAO.bulkDeleteModel(smsMarkersDAO.getModels(String.format("(%s = %s OR %s = %s) AND %s = %s",
                SmsMarkersDAO.COL_TYPE, String.valueOf(SmsParser.MARKER_TYPE_ACCOUNT),
                SmsMarkersDAO.COL_TYPE, String.valueOf(SmsParser.MARKER_TYPE_DESTACCOUNT),
                SmsMarkersDAO.COL_OBJECT, String.valueOf(model.getID()))), resetTS);

        Lg.log("delete account...");
        super.deleteModel(model, resetTS, context);
    }

    public Single<List<Account>> getAllAccountsRx(boolean includeClosed) {
        return Single.fromCallable(() -> getAllAccounts(includeClosed));
    }

    @SuppressWarnings("unchecked")
    public List<Account> getAllAccounts(boolean includeClosed) throws Exception {
        String selection = null;
        if (!includeClosed) {
            selection = String.format("%s = 0", COL_IS_CLOSED);
        }

        return (List<Account>) getItems(getTableName(), ALL_COLUMNS, selection, null, COL_NAME, null);
    }

    HashSet<Long> getAccountsIDsByCabbageID(long cabbageID) {
        String where = String.format("%s = %s AND %s = 0", COL_CURRENCY, String.valueOf(cabbageID),
                COL_SYNC_DELETED);
        Cursor cursor = mDatabase.query(getTableName(), new String[]{COL_ID}, where, null, null, null, null);
        HashSet<Long> ids = new HashSet<>();
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

    public HashSet<Long> getClosedAccountsIDs() {
        String where = COL_IS_CLOSED +" != 0 AND " + COL_SYNC_DELETED + " = 0";
        Cursor cursor = mDatabase.query(getTableName(), new String[]{COL_ID}, where, null, null, null, null);
        HashSet<Long> ids = new HashSet<>();
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

    public Account getAccountByID(long id) {
        return (Account) getModelByIdCustomColumns(id, getAllColumns());
    }

    @Override
    public IAbstractModel getModelById(long id) {
        return super.getModelByIdCustomColumns(id, getAllColumns());
    }

    public LinkedHashMap<Long,HashSet<Long>> getIDsByCabbages() {
        LinkedHashMap<Long,HashSet<Long>> array = new LinkedHashMap<>();
        Cursor cursor = mDatabase.query(getTableName(), new String[]{COL_ID, COL_CURRENCY}, String.format("%s = 0", COL_SYNC_DELETED), null, null, null, null);
        long cabbage;
        HashSet<Long> ids;
        if (cursor != null) {
            try {
                if (cursor.moveToFirst()) {
                    while (!cursor.isAfterLast()) {
                        cabbage = cursor.getLong(1);
                        ids = array.get(cabbage);
                        if (ids == null) {
                            ids = new HashSet<>();
                            ids.add(cursor.getLong(0));
                            array.put(cabbage, ids);
                        } else {
                            ids.add(cursor.getLong(0));
                        }
                        cursor.moveToNext();
                    }
                }
            } finally {
                cursor.close();
            }
        }
        return array;
    }

    /*synchronized*/
    public void updateOrder(List<Pair<Long, Integer>> pairs) {
        if (pairs.size() == 0) {
            return;
        }

        StringBuilder ids = new StringBuilder();
        String comma = "";
        String when = "";

        for (Pair<Long, Integer> pair : pairs) {
            when = String.format("%s WHEN %s THEN %s ", when, String.valueOf(pair.first), String.valueOf(pair.second));
            ids.append(comma).append(pair.first);
            comma = ",";
        }

        String sql = String.format("UPDATE %s SET %s = CASE %s %s END WHERE %s IN (%s)",
                TABLE, COL_ORDER, COL_ID, when, COL_ID, ids.toString());

        mDatabase.execSQL(sql);

    }

    @Override
    public List<?> getAllModels() throws Exception {
        return getAllAccounts(true);
    }
}
