package com.yoshione.fingen.dao;

import android.content.Context;

import io.requery.android.database.sqlite.SQLiteDatabase;

public class RunningBalanceDAO extends BaseDAO implements AbstractDAO {

    //<editor-fold desc="log_Running_Balance">
    public static final String TABLE = "log_Running_Balance";

    public static final String COL_ACCOUNT_ID = "AccountID";
    public static final String COL_TRANSACTION_ID = "TransactionID";
    public static final String COL_DATE_TIME = "DateTimeRB";
    public static final String COL_INCOME = "Income";
    public static final String COL_EXPENSE = "Expense";

    public static final String[] ALL_COLUMNS = {
            COL_ACCOUNT_ID, COL_TRANSACTION_ID, COL_DATE_TIME, COL_INCOME, COL_EXPENSE
    };

    public static final String SQL_CREATE_TABLE = "CREATE TABLE " + TABLE + " ("
            + COL_ACCOUNT_ID        + " INTEGER NOT NULL, "
            + COL_TRANSACTION_ID    + " INTEGER NOT NULL, "
            + COL_DATE_TIME         + " INTEGER NOT NULL, "
            + COL_INCOME            + " REAL NOT NULL, "
            + COL_EXPENSE           + " REAL NOT NULL, "
            + "PRIMARY KEY (AccountID, TransactionID));";

    public static final String SQL_CREATE_INDEX_ACCOUNTS = "CREATE INDEX idx_RB_Accounts ON log_Running_Balance (AccountID);";
    public static final String SQL_CREATE_INDEX_TRANSACTIONS = "CREATE INDEX idx_RB_Transactions ON log_Running_Balance (TransactionID);";
    public static final String SQL_CREATE_INDEX_DATETIME = "CREATE INDEX idx_RB_DateTime ON log_Running_Balance (DateTimeRB);";
    //</editor-fold>

    private static RunningBalanceDAO sInstance;

    public synchronized static RunningBalanceDAO getInstance(Context context) {
        if (sInstance == null) {
            sInstance = new RunningBalanceDAO(context);
        }
        return sInstance;
    }

    public static void updateBalance(SQLiteDatabase db, boolean revert, double amount, int mult, long accountId, long transactionID, long dateTime) {
        // ToDo: Check!!! Учесть измение стартового баланса!!!
        String fieldName;
        if (amount * mult > 0) {
            fieldName = !revert ? RunningBalanceDAO.COL_INCOME : RunningBalanceDAO.COL_EXPENSE;
        } else {
            fieldName = !revert ? RunningBalanceDAO.COL_EXPENSE : RunningBalanceDAO.COL_INCOME;
        }

        String sql;
        if (!revert) {
            sql = "INSERT OR REPLACE INTO " + RunningBalanceDAO.TABLE + " (AccountID, TransactionID, DateTimeRB, Income, Expense) VALUES ("
                    + accountId + ", " + transactionID + ", " + dateTime + ", "
                    + "IFNULL((SELECT Income FROM " + RunningBalanceDAO.TABLE + " WHERE AccountID = " + accountId + " AND DateTimeRB <= " + dateTime + " ORDER BY DateTimeRB DESC LIMIT 1), 0), "
                    + "IFNULL((SELECT Expense FROM " + RunningBalanceDAO.TABLE + " WHERE AccountID = " + accountId + " AND DateTimeRB <= " + dateTime + " ORDER BY DateTimeRB DESC LIMIT 1), 0))";
            db.execSQL(sql);
        }

        sql = "UPDATE " + RunningBalanceDAO.TABLE + " SET " + fieldName + " = " + fieldName + " + " + amount * mult
                + " WHERE AccountID = " + accountId + " AND DateTimeRB >= " + dateTime;
        db.execSQL(sql);
    }

    private RunningBalanceDAO(Context context) {
        super(context, TABLE, ALL_COLUMNS);
    }
}
