package com.yoshione.fingen.managers;

import android.content.Context;

import androidx.fragment.app.FragmentManager;

import com.yoshione.fingen.dao.CabbagesDAO;
import com.yoshione.fingen.FragmentSortAccounts;
import com.yoshione.fingen.model.Account;
import com.yoshione.fingen.model.Cabbage;
import com.yoshione.fingen.R;

public class AccountManager {

    public static Cabbage getCabbage(Account account, Context context) {
        if (account.getCabbageId() < 0) {
            return new Cabbage();
        } else {
            CabbagesDAO cabbagesDAO = CabbagesDAO.getInstance(context);
            return cabbagesDAO.getCabbageByID(account.getCabbageId());
        }
    }

    public static void showSortDialog(FragmentManager fragmentManager, Context context){
        FragmentSortAccounts alertDialog = FragmentSortAccounts.newInstance(context.getResources().getString(R.string.ttl_sort_accounts));
        alertDialog.show(fragmentManager, "fragment_cabbage_edit");
    }

}
