package com.yoshione.fingen.dao;

import android.content.Context;
import android.database.Cursor;

import com.yoshione.fingen.DBHelper;
import com.yoshione.fingen.interfaces.IAbstractModel;
import com.yoshione.fingen.interfaces.IDaoInheritor;
import com.yoshione.fingen.model.AccountsSetRef;

import java.util.List;

/**
 * Created by slv on 01.12.2016.
 * j
 */

public class AccountsSetsRefDAO extends BaseDAO implements AbstractDAO, IDaoInheritor {
    private static AccountsSetsRefDAO sInstance;

    private AccountsSetsRefDAO(Context context) {
        super(context, DBHelper.T_REF_ACCOUNTS_SETS, -1, DBHelper.T_REF_ACCOUNTS_SETS_ALL_COLUMNS);
        super.setDaoInheritor(this);
    }

    public synchronized static AccountsSetsRefDAO getInstance(Context context) {
        if (sInstance == null) {
            sInstance = new AccountsSetsRefDAO(context);
        }
        return sInstance;
    }

    @Override
    public IAbstractModel cursorToModel(Cursor cursor) {
        return cursorToAccountsSet(cursor);
    }

    private AccountsSetRef cursorToAccountsSet(Cursor cursor) {
        AccountsSetRef accountsSetRef = new AccountsSetRef();
        accountsSetRef.setID(cursor.getLong(mColumnIndexes.get(DBHelper.C_ID)));
        accountsSetRef.setName(cursor.getString(mColumnIndexes.get(DBHelper.C_REF_ACCOUNTS_SETS_NAME)));

        return accountsSetRef;
    }

    @SuppressWarnings("unchecked")
    public List<AccountsSetRef> getAllAccountsSets() throws Exception {
        return (List<AccountsSetRef>) getItems(getTableName(), null,
                null, null, DBHelper.C_REF_ACCOUNTS_SETS_NAME, null);
    }

    @Override
    public List<?> getAllModels() throws Exception {
        return getAllAccountsSets();
    }

    @Override
    public synchronized void deleteModel(IAbstractModel model, boolean resetTS, Context context) {
        AccountsSetsLogDAO accountsSetsLogDAO = AccountsSetsLogDAO.getInstance(context);
        accountsSetsLogDAO.bulkDeleteModel(accountsSetsLogDAO.getModels(String.format("%s = %s", DBHelper.C_LOG_ACCOUNTS_SETS_SET,
                String.valueOf(model.getID()))), resetTS);

        super.deleteModel(model, resetTS, context);
    }
}
