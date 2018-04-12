package com.yoshione.fingen.model;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

/**
 * Created by slv on 01.12.2016.
 * a
 */

public class AccountsSet {
    private AccountsSetRef mAccountsSetRef;
    private HashSet<AccountsSetLog> mAccountsSetLogList;
    private boolean mSelected;

    public AccountsSet() {
        mAccountsSetRef = new AccountsSetRef();
        mAccountsSetLogList = new HashSet<>();
        mSelected = false;
    }

    public AccountsSet(AccountsSetRef accountsSetRef, HashSet<AccountsSetLog> accountsSetLogList) {
        mAccountsSetRef = accountsSetRef;
        mAccountsSetLogList = accountsSetLogList;
        mSelected = false;
    }

    public AccountsSetRef getAccountsSetRef() {
        return mAccountsSetRef;
    }

    public void setAccountsSetRef(AccountsSetRef accountsSetRef) {
        mAccountsSetRef = accountsSetRef;
        for (AccountsSetLog accountsSetLog : mAccountsSetLogList) {
            accountsSetLog.setAccountSetID(accountsSetRef.getID());
        }
    }

    public HashSet<AccountsSetLog> getAccountsSetLogList() {
        return mAccountsSetLogList;
    }

    public void setAccountsSetLogList(HashSet<AccountsSetLog> accountsSetLogList) {
        mAccountsSetLogList = accountsSetLogList;
    }

    public boolean isSelected() {
        return mSelected;
    }

    public void setSelected(boolean selected) {
        mSelected = selected;
    }

    public List<Long> getAccountsIDsList() {
        List<Long> list = new ArrayList<>();
        for (AccountsSetLog accountsSetLog : mAccountsSetLogList) {
            list.add(accountsSetLog.getAccountID());
        }
        return list;
    }
}
