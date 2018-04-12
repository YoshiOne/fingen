package com.yoshione.fingen.managers;

import android.content.Context;
import android.support.v4.app.FragmentManager;

import com.yoshione.fingen.FragmentSenderEdit;
import com.yoshione.fingen.R;
import com.yoshione.fingen.model.Sender;

/**
 * Created by slv on 08.04.2016.
 *
 */
public class SenderManager {

    public static void showEditDialog(final Sender sender, final FragmentManager fragmentManager, final Context context) {

        String title;
        if (sender.getID() < 0) {
            title = context.getResources().getString(R.string.ent_new_sender);
        } else {
            title = context.getResources().getString(R.string.ent_edit_sender);
        }

        FragmentSenderEdit fragmentSenderEdit = FragmentSenderEdit.newInstance(title,sender);
        fragmentSenderEdit.show(fragmentManager, "fragmentSenderEdit");
    }
}
