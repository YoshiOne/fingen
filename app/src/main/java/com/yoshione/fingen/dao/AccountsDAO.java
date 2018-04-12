package com.yoshione.fingen.dao;

import android.content.Context;
import android.database.Cursor;
import android.util.Pair;


import com.yoshione.fingen.DBHelper;
import com.yoshione.fingen.interfaces.IAbstractModel;
import com.yoshione.fingen.interfaces.IDaoInheritor;
import com.yoshione.fingen.model.Account;
import com.yoshione.fingen.model.Cabbage;
import com.yoshione.fingen.utils.Lg;
import com.yoshione.fingen.utils.SmsParser;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;

import static com.yoshione.fingen.DBHelper.C_ID;
import static com.yoshione.fingen.DBHelper.C_REF_ACCOUNTS_CURRENCY;
import static com.yoshione.fingen.DBHelper.C_SYNC_DELETED;

/**
 * Created by slv on 13.08.2015.
 * a
 */
public class AccountsDAO extends BaseDAO implements AbstractDAO, IDaoInheritor {


    private static AccountsDAO sInstance;

    private AccountsDAO(Context context) {
        super(context, DBHelper.T_REF_ACCOUNTS, IAbstractModel.MODEL_TYPE_ACCOUNT, DBHelper.T_REF_ACCOUNTS_ALL_COLUMNS);
        super.setDaoInheritor(this);
    }

    public synchronized static AccountsDAO getInstance(Context context) {
        if (sInstance == null) {
            sInstance = new AccountsDAO(context);
        }
        return sInstance;
    }

    @Override
    public IAbstractModel cursorToModel(Cursor cursor) {
        return cursorToAccount(cursor);
    }

    private synchronized Account cursorToAccount(Cursor cursor) {
        Account account = new Account();

        account.setID(cursor.getLong(mColumnIndexes.get(DBHelper.C_ID)));
        Account.AccountType accountType = Account.AccountType.values()[cursor.getInt(mColumnIndexes.get(DBHelper.C_REF_ACCOUNTS_TYPE))];
        account.setAccountType(accountType);
        account.setName(cursor.getString(mColumnIndexes.get(DBHelper.C_REF_ACCOUNTS_NAME)));
        account.setCabbageId(cursor.getLong(mColumnIndexes.get(DBHelper.C_REF_ACCOUNTS_CURRENCY)));
        account.setEmitent(cursor.getString(mColumnIndexes.get(DBHelper.C_REF_ACCOUNTS_EMITENT)));
        account.setLast4Digits(cursor.getInt(mColumnIndexes.get(DBHelper.C_REF_ACCOUNTS_LAST4DIGITS)));
        account.setComment(cursor.getString(mColumnIndexes.get(DBHelper.C_REF_ACCOUNTS_COMMENT)));
        account.setStartBalance(new BigDecimal(cursor.getDouble(mColumnIndexes.get(DBHelper.C_REF_ACCOUNTS_STARTBALANCE))));
        account.setIncome(new BigDecimal(cursor.getDouble(cursor.getColumnIndex("Income"))));
        account.setExpense(new BigDecimal(cursor.getDouble(cursor.getColumnIndex("Expense"))));

        account.setIsClosed(cursor.getInt(mColumnIndexes.get(DBHelper.C_REF_ACCOUNTS_ISCLOSED)) == 1);
        account.setOrder(cursor.getInt(mColumnIndexes.get(DBHelper.C_REF_ACCOUNTS_ORDER)));

        account.setCreditLimit(new BigDecimal(cursor.getDouble(mColumnIndexes.get(DBHelper.C_REF_ACCOUNTS_CREDITLIMIT))));

        account = (Account) DBHelper.getSyncDataFromCursor(account, cursor, mColumnIndexes);

        return account;
    }

    @Override
    public void deleteModel(IAbstractModel model, boolean resetTS, Context context) {
        Lg.log("Start deleteting account (ID %s; resetTS %s)", String.valueOf(model.getID()), String.valueOf(resetTS));

        //Удаляем все транзакции в которых участвует этот счет
        Lg.log("delete transactions...");
        TransactionsDAO transactionsDAO = TransactionsDAO.getInstance(context);
        transactionsDAO.bulkDeleteModel(transactionsDAO.getModels(String.format("%s = %s OR %s = %s", DBHelper.C_LOG_TRANSACTIONS_SRCACCOUNT,
                String.valueOf(model.getID()), DBHelper.C_LOG_TRANSACTIONS_DESTACCOUNT, String.valueOf(model.getID()))), resetTS);

        //Удаляем все шаблоны с этим счетом
        Lg.log("delete templates...");
        TemplatesDAO templatesDAO = TemplatesDAO.getInstance(context);
        templatesDAO.bulkDeleteModel(templatesDAO.getModels(String.format("%s = %s OR %s = %s", DBHelper.C_LOG_TEMPLATES_SRCACCOUNT,
                String.valueOf(model.getID()), DBHelper.C_LOG_TEMPLATES_DESTACCOUNT, String.valueOf(model.getID()))), resetTS);

        //Удаляем все долги, привязанные к счету
        Lg.log("delete credits...");
        CreditsDAO creditsDAO = CreditsDAO.getInstance(context);
        creditsDAO.bulkDeleteModel(creditsDAO.getModels(String.format("%s = %s", DBHelper.C_REF_DEBTS_ACCOUNT, String.valueOf(model.getID()))), resetTS);

        //Удаляем все долги, привязанные к счету
        Lg.log("delete AccountsSetsLogs...");
        AccountsSetsLogDAO accountsSetsLogDAO = AccountsSetsLogDAO.getInstance(context);
        accountsSetsLogDAO.bulkDeleteModel(accountsSetsLogDAO.getModels(String.format("%s = %s", DBHelper.C_LOG_ACCOUNTS_SETS_ACCOUNT, String.valueOf(model.getID()))), resetTS);

        //Удаляем все маркеры, соответсвующие счету
        Lg.log("delete sms markers...");
        SmsMarkersDAO smsMarkersDAO = SmsMarkersDAO.getInstance(context);
        smsMarkersDAO.bulkDeleteModel(smsMarkersDAO.getModels(String.format("(%s = %s OR %s = %s) AND %s = %s",
                DBHelper.C_LOG_SMS_PARSER_PATTERNS_TYPE, String.valueOf(SmsParser.MARKER_TYPE_ACCOUNT),
                DBHelper.C_LOG_SMS_PARSER_PATTERNS_TYPE, String.valueOf(SmsParser.MARKER_TYPE_DESTACCOUNT),
                DBHelper.C_LOG_SMS_PARSER_PATTERNS_OBJECT, String.valueOf(model.getID()))), resetTS);

        Lg.log("delete account...");
        super.deleteModel(model, resetTS, context);
    }

    @SuppressWarnings("unchecked")
    public List<Account> getAllAccounts(boolean includeClosed) throws Exception {
        String selection = null;
        if (!includeClosed) {
            selection = String.format("%s = 0", DBHelper.C_REF_ACCOUNTS_ISCLOSED);
        }


        return (List<Account>) getItems(getTableName(), DBHelper.T_REF_ACCOUNTS_ALL_COLUMNS, selection, null, DBHelper.C_REF_ACCOUNTS_NAME, null);
    }

    HashSet<Long> getAccountsIDsByCabbageID(long cabbageID) {
        String where = String.format("%s = %s AND %s = 0", DBHelper.C_REF_ACCOUNTS_CURRENCY, String.valueOf(cabbageID),
                DBHelper.C_SYNC_DELETED);
        Cursor cursor = mDatabase.query(getTableName(), new String[]{DBHelper.C_ID}, where, null, null, null, null);
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
        String where = DBHelper.C_REF_ACCOUNTS_ISCLOSED +" != 0 AND "+DBHelper.C_SYNC_DELETED+" = 0";
        Cursor cursor = mDatabase.query(getTableName(), new String[]{DBHelper.C_ID}, where, null, null, null, null);
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
        Cursor cursor = mDatabase.query(getTableName(), new String[]{C_ID, C_REF_ACCOUNTS_CURRENCY}, String.format("%s = 0", C_SYNC_DELETED), null, null, null, null);
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
                DBHelper.T_REF_ACCOUNTS,
                DBHelper.C_REF_ACCOUNTS_ORDER,
                DBHelper.C_ID,
                when,
                DBHelper.C_ID,
                ids);


        mDatabase.execSQL(sql);

    }

    @Override
    public List<?> getAllModels() throws Exception {
        return getAllAccounts(true);
    }
}
