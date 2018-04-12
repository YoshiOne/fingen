package com.yoshione.fingen.managers;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.support.v7.app.AlertDialog;
import android.support.v7.preference.PreferenceManager;
import android.widget.EditText;
import android.widget.Toast;

import com.yoshione.fingen.FgConst;
import com.yoshione.fingen.R;
import com.yoshione.fingen.dao.AccountsDAO;
import com.yoshione.fingen.dao.AccountsSetsLogDAO;
import com.yoshione.fingen.dao.AccountsSetsRefDAO;
import com.yoshione.fingen.interfaces.IOnComplete;
import com.yoshione.fingen.interfaces.IOnEditAction;
import com.yoshione.fingen.model.Account;
import com.yoshione.fingen.model.AccountsSet;
import com.yoshione.fingen.model.AccountsSetLog;
import com.yoshione.fingen.model.AccountsSetRef;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

/**
 * Created by slv on 01.12.2016.
 * a
 */

public class AccountsSetManager {

    private static AccountsSetManager sInstance;
    private AccountsSet mCurrentAccountSet;

    public static AccountsSetManager getInstance() {
        if (sInstance == null) {
            sInstance = new AccountsSetManager();
        }
        return sInstance;
    }

    public AccountsSetManager() {
        mCurrentAccountSet = null;
    }

    public List<AccountsSet> getAcoountsSets(Context context) {
        List<AccountsSetRef> accountsSetRefList;
        HashSet<AccountsSetLog> accountsSetLogList;
        List<AccountsSet> accountsSetList = new ArrayList<>();
        try {
            accountsSetRefList = AccountsSetsRefDAO.getInstance(context).getAllAccountsSets();
        } catch (Exception e) {
            accountsSetRefList = new ArrayList<>();
        }

        AccountsSetsLogDAO accountsSetsLogDAO = AccountsSetsLogDAO.getInstance(context);
        for (AccountsSetRef accountsSetRef : accountsSetRefList) {
            try {
                accountsSetLogList = new HashSet<>();
                accountsSetLogList.addAll(accountsSetsLogDAO.getAccountsBySet(accountsSetRef.getID()));
            } catch (Exception e) {
                accountsSetLogList = new HashSet<>();
            }
            accountsSetList.add(new AccountsSet(accountsSetRef, accountsSetLogList));
        }
        return accountsSetList;
    }

    public AccountsSet getAccountsSetByID(long id, Context context) {
        if (id >= 0) {
            AccountsSetRef accountsSetRef = (AccountsSetRef) AccountsSetsRefDAO.getInstance(context).getModelById(id);
            HashSet<AccountsSetLog> accountsSetLogList;
            try {
                accountsSetLogList = new HashSet<>();
                accountsSetLogList.addAll(AccountsSetsLogDAO.getInstance(context).getAccountsBySet(id));
            } catch (Exception e) {
                accountsSetLogList = new HashSet<>();
            }
            return new AccountsSet(accountsSetRef, accountsSetLogList);
        } else {
            return new AccountsSet(new AccountsSetRef(), new HashSet<AccountsSetLog>());
        }
    }

    public AccountsSet getCurrentAccountSet(Context context) {
        long currentSetID = PreferenceManager.getDefaultSharedPreferences(context).getLong(FgConst.PREF_CURRENT_ACCOUNT_SET, -1);
        if (mCurrentAccountSet != null && mCurrentAccountSet.getAccountsSetRef().getID() == currentSetID) {
            return mCurrentAccountSet;
        } else {
            mCurrentAccountSet = getAccountsSetByID(currentSetID, context);
            return mCurrentAccountSet;
        }
    }

    public void createAccountSet(final Activity activity, final IOnComplete onComplete) {
        editName(new AccountsSet(), activity, new IOnEditAction() {
            @Override
            public void onEdit(AccountsSet accountsSet) {
                editAccounts(accountsSet, activity, new IOnEditAction() {
                    @Override
                    public void onEdit(AccountsSet accountsSet) {
                        writeAccountsSet(accountsSet, activity, onComplete);
                    }
                });
            }
        });
    }

    public void editName(final AccountsSet accountsSet, Activity activity, final IOnEditAction onEditAction) {
        String title = activity.getString(R.string.ent_name);

        AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        builder.setTitle(title);
        final EditText input = (EditText) activity.getLayoutInflater().inflate(R.layout.template_edittext, null);
        input.setText(accountsSet.getAccountsSetRef().getName());
        builder.setView(input);

        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                accountsSet.getAccountsSetRef().setName(input.getText().toString());
                onEditAction.onEdit(accountsSet);
            }
        });

        builder.show();
        input.requestFocus();
    }

    public void editAccounts(final AccountsSet accountsSet, Activity activity, final IOnEditAction onEditAction) {
        AlertDialog dialog;
        final AccountsDAO accountsDAO = AccountsDAO.getInstance(activity);
        List<Account> accountList;
        try {
            accountList = accountsDAO.getAllAccounts(PreferenceManager.getDefaultSharedPreferences(activity)
                    .getBoolean(FgConst.PREF_SHOW_CLOSED_ACCOUNTS, true));
        } catch (Exception e) {
            accountList = new ArrayList<>();
        }
        CharSequence[] items = new CharSequence[accountList.size()];
        boolean[] checkedItems = new boolean[accountList.size()];
        for (int i = 0; i < accountList.size(); i++) {
            items[i] = accountList.get(i).getName();
            for (AccountsSetLog accountsSetLog : accountsSet.getAccountsSetLogList()) {
                if (accountList.get(i).getID() == accountsSetLog.getAccountID()) {
                    checkedItems[i] = true;
                    break;
                }
            }
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        builder.setTitle(activity.getResources().getString(R.string.ttl_select_accounts));
        builder.setMultiChoiceItems(items, checkedItems,
                new DialogInterface.OnMultiChoiceClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog1, int indexSelected, boolean isChecked) {

                    }
                })
                // Set the action buttons
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog1, int id) {
                        AlertDialog ad = (AlertDialog) dialog1;
                        String name;
                        long accId;
                        accountsSet.getAccountsSetLogList().clear();
                        for (int i = 0; i < ad.getListView().getCount(); i++) {
                            name = ad.getListView().getAdapter().getItem(i).toString();
                            try {
                                accId = accountsDAO.getModelByName(name).getID();
                            } catch (Exception e) {
                                accId = -1;
                            }
                            if (ad.getListView().isItemChecked(i)) {
                                accountsSet.getAccountsSetLogList().add(new AccountsSetLog(-1, accountsSet.getAccountsSetRef().getID(), accId));
                            }
                        }
                        onEditAction.onEdit(accountsSet);
                    }
                });

        dialog = builder.create();//AlertDialog dialog; create like this outside onClick
        dialog.show();
    }

    public void writeAccountsSet(AccountsSet accountsSet, Context context, IOnComplete onComplete) {
        try {
            accountsSet.setAccountsSetRef((AccountsSetRef) AccountsSetsRefDAO.getInstance(context).createModel(accountsSet.getAccountsSetRef()));
            AccountsSetsLogDAO.getInstance(context).createAccountsSet(accountsSet);
            SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
            preferences.edit().putLong(FgConst.PREF_CURRENT_ACCOUNT_SET, accountsSet.getAccountsSetRef().getID()).apply();
        } catch (Exception e) {
            Toast.makeText(context, context.getString(R.string.msg_error_on_write_to_db), Toast.LENGTH_SHORT).show();
        }
        onComplete.onComplete();
    }

    public void deleteAccountSet(AccountsSet accountsSet, Context context) {
        AccountsSetsRefDAO.getInstance(context).deleteModel(accountsSet.getAccountsSetRef(), true, context);
    }
}
