package com.yoshione.fingen.dao;

import android.content.Context;
import android.database.Cursor;

import com.yoshione.fingen.db.DbUtil;
import com.yoshione.fingen.interfaces.IAbstractModel;
import com.yoshione.fingen.interfaces.IDaoInheritor;
import com.yoshione.fingen.model.AccountsSetRef;

import java.util.List;

public class AccountsSetsRefDAO extends BaseDAO<AccountsSetRef> implements IDaoInheritor {

    //<editor-fold desc="ref_Accounts_Sets">
    public static final String TABLE = "ref_Accounts_Sets";

    public static final String[] ALL_COLUMNS = joinArrays(COMMON_COLUMNS, new String[]{ COL_NAME });

    public static final String SQL_CREATE_TABLE = "CREATE TABLE " + TABLE + " ("
            + COMMON_FIELDS +   ", "
            + COL_NAME +        " TEXT, "
            + "UNIQUE (" + COL_NAME + ", " + COL_SYNC_DELETED + ") ON CONFLICT ABORT);";
    //</editor-fold>

    private static AccountsSetsRefDAO sInstance;

    public synchronized static AccountsSetsRefDAO getInstance(Context context) {
        if (sInstance == null) {
            sInstance = new AccountsSetsRefDAO(context);
        }
        return sInstance;
    }

    private AccountsSetsRefDAO(Context context) {
        super(context, TABLE, ALL_COLUMNS);
        super.setDaoInheritor(this);
    }

    @Override
    public IAbstractModel createEmptyModel() {
        return new AccountsSetRef();
    }

    @Override
    public IAbstractModel cursorToModel(Cursor cursor) {
        return cursorToAccountsSet(cursor);
    }

    private AccountsSetRef cursorToAccountsSet(Cursor cursor) {
        AccountsSetRef accountsSetRef = new AccountsSetRef();
        accountsSetRef.setID(DbUtil.getLong(cursor, COL_ID));
        accountsSetRef.setName(DbUtil.getString(cursor, COL_NAME));

        return accountsSetRef;
    }

    @Override
    public List<AccountsSetRef> getAllModels() {
        return getItems(getTableName(), null,
                null, null, COL_NAME, null);
    }

    @Override
    public synchronized void deleteModel(IAbstractModel model, boolean resetTS, Context context) {
        AccountsSetsLogDAO accountsSetsLogDAO = AccountsSetsLogDAO.getInstance(context);
        accountsSetsLogDAO.bulkDeleteModel(accountsSetsLogDAO.getModels(String.format("%s = %s", AccountsSetsLogDAO.COL_SET_ID,
                String.valueOf(model.getID()))), resetTS);

        super.deleteModel(model, resetTS, context);
    }
}
