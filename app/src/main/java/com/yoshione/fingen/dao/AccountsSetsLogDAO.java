package com.yoshione.fingen.dao;

import android.content.Context;
import android.database.Cursor;

import com.yoshione.fingen.db.DbUtil;
import com.yoshione.fingen.interfaces.IAbstractModel;
import com.yoshione.fingen.interfaces.IDaoInheritor;
import com.yoshione.fingen.model.AccountsSet;
import com.yoshione.fingen.model.AccountsSetLog;

import java.util.ArrayList;
import java.util.List;

public class AccountsSetsLogDAO extends BaseDAO implements AbstractDAO, IDaoInheritor {

    //<editor-fold desc="log_Accounts_Sets">
    public static final String TABLE = "log_Accounts_Sets";

    public static final String COL_SET_ID = "SetID";
    public static final String COL_ACCOUNT_ID = "AccountID";

    public static final String[] ALL_COLUMNS = joinArrays(COMMON_COLUMNS, new String[]{
            COL_SET_ID, COL_ACCOUNT_ID
    });

    public static final String SQL_CREATE_TABLE = "CREATE TABLE " + TABLE + " ("
            + COMMON_FIELDS +   ", "
            + COL_SET_ID +      " INTEGER NOT NULL ON CONFLICT ABORT REFERENCES [" + AccountsSetsRefDAO.TABLE + "]([" + COL_ID + "]) ON DELETE CASCADE ON UPDATE CASCADE,"
            + COL_ACCOUNT_ID +  " INTEGER NOT NULL ON CONFLICT ABORT REFERENCES [" + AccountsDAO.TABLE + "]([" + COL_ID + "]) ON DELETE CASCADE ON UPDATE CASCADE,"
            + "UNIQUE (" + COL_SET_ID + ", " + COL_ACCOUNT_ID + ", " + COL_SYNC_DELETED + ") ON CONFLICT ABORT);";
    //</editor-fold>

    private static AccountsSetsLogDAO sInstance;

    public synchronized static AccountsSetsLogDAO getInstance(Context context) {
        if (sInstance == null) {
            sInstance = new AccountsSetsLogDAO(context);
        }
        return sInstance;
    }

    private AccountsSetsLogDAO(Context context) {
        super(context, TABLE, ALL_COLUMNS);
        super.setDaoInheritor(this);
    }

    @Override
    public IAbstractModel createEmptyModel() {
        return new AccountsSetLog();
    }

    @Override
    public IAbstractModel cursorToModel(Cursor cursor) {
        return cursorToAccountsSet(cursor);
    }

    private AccountsSetLog cursorToAccountsSet(Cursor cursor) {
        AccountsSetLog accountsSetLog = new AccountsSetLog();
        accountsSetLog.setID(DbUtil.getLong(cursor, COL_ID));
        accountsSetLog.setAccountSetID(DbUtil.getLong(cursor, COL_SET_ID));
        accountsSetLog.setAccountID(DbUtil.getLong(cursor, COL_ACCOUNT_ID));

        return accountsSetLog;
    }

    @SuppressWarnings("unchecked")
    public List<AccountsSetLog> getAccountsBySet(long accountSetRefID) throws Exception {
        return (List<AccountsSetLog>) getItems(getTableName(), null,
                String.format("%s = %s", COL_SET_ID, String.valueOf(accountSetRefID)), null, null, null);
    }

    public void createAccountsSet(AccountsSet accountsSet) throws Exception {
        List<IAbstractModel> itemsToDelete = new ArrayList<>(getAccountsBySet(accountsSet.getAccountsSetRef().getID()));
        bulkDeleteModel(itemsToDelete, true);

        List<IAbstractModel> itemsToCreate = new ArrayList<>(accountsSet.getAccountsSetLogList());
        bulkCreateModel(itemsToCreate, null, true);
    }
}
