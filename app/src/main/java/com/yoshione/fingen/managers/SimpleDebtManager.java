package com.yoshione.fingen.managers;

import android.content.Context;

import androidx.fragment.app.FragmentManager;

import com.yoshione.fingen.FragmentSimpleDebtEdit;
import com.yoshione.fingen.R;
import com.yoshione.fingen.model.SimpleDebt;

public class SimpleDebtManager {

    public static void showEditDialog(final SimpleDebt simpleDebt, final FragmentManager fragmentManager, final Context context) {
        FragmentSimpleDebtEdit fragmentSimpleDebtEdit = FragmentSimpleDebtEdit.newInstance(simpleDebt.getID() < 0 ? context.getResources().getString(R.string.ent_new_debt) : context.getResources().getString(R.string.ent_edit_debt), simpleDebt);
        fragmentSimpleDebtEdit.show(fragmentManager, "fragmentSimpleDebtEdit");
    }
}
