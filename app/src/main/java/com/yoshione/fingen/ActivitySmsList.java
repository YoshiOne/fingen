package com.yoshione.fingen;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import com.yoshione.fingen.dao.SendersDAO;
import com.yoshione.fingen.model.Sender;
import com.yoshione.fingen.model.Sms;
import com.yoshione.fingen.model.Transaction;
import com.yoshione.fingen.utils.RequestCodes;
import com.yoshione.fingen.utils.SmsParser;
import com.yoshione.fingen.widgets.ToolbarActivity;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class ActivitySmsList extends ToolbarActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    protected int getLayoutResourceId() {
        return R.layout.activity_sms_list;
    }

    @Override
    protected String getLayoutTitle() {
        return getString(R.string.ent_incoming);
    }

    @Override
    public void onStart() {
        super.onStart();
    }

    @Override
    public void onStop() {
        super.onStop();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_import_sms, menu);
        super.onCreateOptionsMenu(menu);
        menu.findItem(R.id.action_paste).setIcon(getDrawable(R.drawable.ic_paste_white));
        menu.findItem(R.id.action_import).setIcon(getDrawable(R.drawable.ic_import_white));
        menu.findItem(R.id.action_import).setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                Intent intent = new Intent(ActivitySmsList.this, ActivityImportSms.class);
                ActivitySmsList.this.startActivity(intent);
                return true;
            }
        });

        menu.findItem(R.id.action_paste).setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                ActivitySmsList.this.pasteSms();
                return true;
            }
        });
        return true;
    }

    private void pasteSms() {
        ClipboardManager clipboard = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
        if (!clipboard.hasPrimaryClip()) return;
        ClipData data = clipboard.getPrimaryClip();
        ClipData.Item item = data.getItemAt(0);

        final String text;
        if (item != null) {
            try {
                text = item.getText().toString();
            } catch (Exception e) {
                Toast.makeText(this, getString(R.string.err_parse_clipboard), Toast.LENGTH_SHORT).show();
                return;
            }
        } else {
            text = "";
        }

        if (text.isEmpty()) return;


        AlertDialog.Builder builderSingle = new AlertDialog.Builder(this);
        final ArrayAdapter<Sender> arrayAdapterCabbages = new ArrayAdapter<>(this, android.R.layout.select_dialog_singlechoice);
        List<Sender> cabbages;
        try {
            cabbages = SendersDAO.getInstance(this).getAllSenders();
        } catch (Exception e) {
            cabbages = new ArrayList<>();
        }
        arrayAdapterCabbages.addAll(cabbages);

        builderSingle.setSingleChoiceItems(arrayAdapterCabbages, -1,
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(final DialogInterface dialog, final int which) {
                        dialog.cancel();
                        ListView lw = ((AlertDialog) dialog).getListView();
                        Sender sender = (Sender) lw.getAdapter().getItem(which);
                        Sms sms = new Sms(-1, new Date(), sender.getID(), text);
                        SmsParser smsParser = new SmsParser(sms, getApplicationContext());
                        Transaction transaction = smsParser.extractTransaction();

                        Intent intent = new Intent(ActivitySmsList.this, ActivityEditTransaction.class);
                        intent.putExtra("transaction", transaction);
                        intent.putExtra("sms", sms);
                        startActivityForResult(intent, RequestCodes.REQUEST_CODE_EDIT_TRANSACTION);
                    }
                });
        builderSingle.setTitle(getString(R.string.title_select_sender));

        builderSingle.setNegativeButton(
                getResources().getString(android.R.string.cancel),
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
        builderSingle.show();
    }
}
