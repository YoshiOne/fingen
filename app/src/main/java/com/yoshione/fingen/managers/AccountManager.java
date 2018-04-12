/*
 * Copyright (c) 2015.
 */

package com.yoshione.fingen.managers;

import android.content.Context;
import android.support.v4.app.FragmentManager;

import com.yoshione.fingen.dao.CabbagesDAO;
import com.yoshione.fingen.FragmentSortAccounts;
import com.yoshione.fingen.model.Account;
import com.yoshione.fingen.model.Cabbage;
import com.yoshione.fingen.R;

/**
 * Created by slv on 04.12.2015.
 *
 */
public class AccountManager {

//    public static synchronized AccountManager getInstance() {
//        if (sInstance == null) {
//            sInstance = new AccountManager();
//        }
//        return sInstance;
//    }
//
//    private static AccountManager sInstance;
//
//    public AccountManager() {
//    }

    public static Cabbage getCabbage(Account account, Context context) {
        if (account.getCabbageId() < 0) {
            return new Cabbage();
        } else {
            CabbagesDAO cabbagesDAO = CabbagesDAO.getInstance(context);
            return cabbagesDAO.getCabbageByID(account.getCabbageId());
        }
    }

    public static void showSortDialog(FragmentManager fragmentManager, Context context){

        String title = context.getResources().getString(R.string.ttl_sort_accounts);

        FragmentSortAccounts alertDialog = FragmentSortAccounts.newInstance(title);
        alertDialog.show(fragmentManager, "fragment_cabbage_edit");
    }

}
