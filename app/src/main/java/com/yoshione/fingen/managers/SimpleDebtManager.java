package com.yoshione.fingen.managers;

import android.content.Context;
import android.support.v4.app.FragmentManager;

import com.yoshione.fingen.FragmentSimpleDebtEdit;
import com.yoshione.fingen.R;
import com.yoshione.fingen.model.SimpleDebt;

/**
 * Created by slv on 08.04.2016.
 *
 */
public class SimpleDebtManager {

    public static void showEditDialog(final SimpleDebt simpleDebt, final FragmentManager fragmentManager, final Context context) {

        String title;
        if (simpleDebt.getID() < 0) {
            title = context.getResources().getString(R.string.ent_new_debt);
        } else {
            title = context.getResources().getString(R.string.ent_edit_debt);
        }

        FragmentSimpleDebtEdit fragmentSimpleDebtEdit = FragmentSimpleDebtEdit.newInstance(title,simpleDebt);
        fragmentSimpleDebtEdit.show(fragmentManager, "fragmentSimpleDebtEdit");
    }
}
