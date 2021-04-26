package com.yoshione.fingen;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import androidx.appcompat.app.AlertDialog;

import com.yoshione.fingen.dao.TemplatesDAO;
import com.yoshione.fingen.model.Template;
import com.yoshione.fingen.model.Transaction;

import java.util.List;

public class ActivityShortcut extends Activity {


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        AlertDialog.Builder builderSingle = new AlertDialog.Builder(this);
        final ArrayAdapter<Template> arrayAdapterTemplates = new ArrayAdapter<>(this, android.R.layout.select_dialog_singlechoice);
        List<Template> templates = TemplatesDAO.getInstance(this).getAllModels();
        arrayAdapterTemplates.addAll(templates);

        builderSingle.setSingleChoiceItems(arrayAdapterTemplates, -1,
                (dialog, which) -> {
                    ListView lw = ((AlertDialog) dialog).getListView();
                    Template template = (Template) lw.getAdapter().getItem(which);
                    final Intent shortcutIntent=new Intent(getApplicationContext(), ActivityEditTransaction.class);
                    shortcutIntent.putExtra("template_name", template.getName());
                    shortcutIntent.putExtra("EXIT", true);
                    int icon = R.drawable.ic_main;
                    switch (template.getTrType()) {
                        case Transaction.TRANSACTION_TYPE_INCOME:
                            icon = R.mipmap.ic_template_income;
                            break;
                        case Transaction.TRANSACTION_TYPE_EXPENSE:
                            icon = R.mipmap.ic_template_expense;
                            break;
                        case Transaction.TRANSACTION_TYPE_TRANSFER:
                            icon = R.mipmap.ic_template_transfer;
                            break;
                    }
                    final Intent.ShortcutIconResource iconResource=Intent.ShortcutIconResource.fromContext(getApplicationContext(),icon);
                    final Intent intent=new Intent();
                    intent.putExtra(Intent.EXTRA_SHORTCUT_INTENT,shortcutIntent);
                    intent.putExtra(Intent.EXTRA_SHORTCUT_NAME,template.getName());
                    intent.putExtra(Intent.EXTRA_SHORTCUT_ICON_RESOURCE,iconResource);
                    setResult(RESULT_OK,intent);
                    dialog.dismiss();
                    finish();
                });
        builderSingle.setTitle(getString(R.string.ttl_select_template));

        builderSingle.setNegativeButton(
                getResources().getString(android.R.string.cancel),
                (dialog, which) -> {
                    dialog.dismiss();
                    finish();
                });
        builderSingle.setOnCancelListener(dialogInterface -> finish());

        builderSingle.show();
    }

}
