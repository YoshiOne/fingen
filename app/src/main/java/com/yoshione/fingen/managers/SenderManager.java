package com.yoshione.fingen.managers;

import android.content.Context;

import androidx.fragment.app.FragmentManager;

import com.yoshione.fingen.FragmentSenderEdit;
import com.yoshione.fingen.R;
import com.yoshione.fingen.model.Sender;

public class SenderManager {

    public static void showEditDialog(final Sender sender, final FragmentManager fragmentManager, final Context context) {
        FragmentSenderEdit fragmentSenderEdit = FragmentSenderEdit.newInstance(sender.getID() < 0 ? context.getResources().getString(R.string.ent_new_sender) : context.getResources().getString(R.string.ent_edit_sender), sender);
        fragmentSenderEdit.show(fragmentManager, "fragmentSenderEdit");
    }
}
