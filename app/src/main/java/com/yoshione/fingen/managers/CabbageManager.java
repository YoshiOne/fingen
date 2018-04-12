/*
 * Copyright (c) 2015.
 */

package com.yoshione.fingen.managers;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AlertDialog;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;

import com.yoshione.fingen.FragmentCabbageEdit;
import com.yoshione.fingen.R;
import com.yoshione.fingen.adapter.CustomAlertAdapter;
import com.yoshione.fingen.model.Cabbage;
import com.yoshione.fingen.utils.LocaleUtils;

import java.util.ArrayList;
import java.util.Currency;
import java.util.List;
import java.util.Locale;

/**
 * Created by slv on 08.12.2015.
 */
public class CabbageManager {

    private static CabbageManager sInstance;

    private CabbageManager() {
    }

    public static synchronized CabbageManager getInstance() {
        if (sInstance == null) {
            sInstance = new CabbageManager();
        }
        return sInstance;
    }

    public static void showEditdialog(Cabbage cabbage, FragmentManager fragmentManager, Context context) {

        String title;
        if (cabbage.getID() < 0) {
            title = context.getResources().getString(R.string.ttl_new_currency);
        } else {
            title = context.getResources().getString(R.string.ttl_edit_currency);
        }

        FragmentCabbageEdit alertDialog = FragmentCabbageEdit.newInstance(title, cabbage);
        alertDialog.show(fragmentManager, "fragment_cabbage_edit");
    }

    public static Cabbage createFromCode(String code, Context context) {
        List<Currency> currencies = new ArrayList<>();
        Cabbage cabbage = null;
        currencies.addAll(Currency.getAvailableCurrencies());
        for (Currency currency : currencies) {
            if (currency.getCurrencyCode().equals(code)) {
                cabbage = new Cabbage();
                cabbage.setCode(code);
                cabbage.setDecimalCount(2);
                Locale locale = LocaleUtils.getLocale(context);
                cabbage.setName(currency.getDisplayName(locale));
                cabbage.setSimbol(currency.getSymbol(locale));
            }
        }
        return cabbage;
    }

    public void showSelectSystemCurrencyDialog(final Activity activity, final OnSelectCurrencyListener selectCurrencyListener) {
//        OnCurrencyItemClickListener onCurrencyItemClickListener = new OnCurrencyItemClickListener(selectCurrencyListener);

        final ArrayList<Currency> array_sort = new ArrayList<>();
        final int[] textlength = {0};
        final List<Currency> currencies = new ArrayList<>();
        currencies.addAll(Currency.getAvailableCurrencies());

        final EditText editText = new EditText(activity);
        final ListView listView = new ListView(activity);
        editText.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_search_gray, 0, 0, 0);

        array_sort.addAll(currencies);

        LinearLayout layout = new LinearLayout(activity);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.addView(editText);
        layout.addView(listView);
        AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        builder.setView(layout);
        CustomAlertAdapter arrayAdapter = new CustomAlertAdapter(activity, array_sort);
        listView.setAdapter(arrayAdapter);

        editText.addTextChangedListener(new TextWatcher() {
            public void afterTextChanged(Editable s) {

            }

            public void beforeTextChanged(CharSequence s,
                                          int start, int count, int after) {

            }

            public void onTextChanged(CharSequence s, int start, int before, int count) {
                editText.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0);
                textlength[0] = editText.getText().length();
                array_sort.clear();
                String curName;
                for (int i = 0; i < currencies.size(); i++) {
                    curName = String.format("%s (%s)", currencies.get(i).getDisplayName(), currencies.get(i).getCurrencyCode());
                    if (textlength[0] <= curName.length()) {

                        if (curName.toLowerCase().contains(editText.getText().toString().toLowerCase().trim())) {
                            array_sort.add(currencies.get(i));
                        }
                    }
                }
                listView.setAdapter(new CustomAlertAdapter(activity, array_sort));
            }
        });


        final AlertDialog dialog = builder.setTitle(activity.getString(R.string.ttl_available_system_currencies))
                .setNegativeButton(activity.getResources().getString(android.R.string.cancel),
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                            }
                        })
                .show();

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                Currency currency = (Currency) adapterView.getAdapter().getItem(i);
                selectCurrencyListener.OnSelectCurrency(currency);
                dialog.dismiss();
            }
        });

    }

    public interface OnSelectCurrencyListener {
        void OnSelectCurrency(Currency selectedCurrency);
    }

    private class OnCurrencyItemClickListener implements DialogInterface.OnClickListener {

        private final OnSelectCurrencyListener mOnSelectCurrencyListener;

        OnCurrencyItemClickListener(OnSelectCurrencyListener mOnSelectCurrencyListener) {
            this.mOnSelectCurrencyListener = mOnSelectCurrencyListener;
        }

        @Override
        public void onClick(DialogInterface dialog, int which) {
            List<Currency> currencies = new ArrayList<>();
            currencies.addAll(Currency.getAvailableCurrencies());
            Currency currency = currencies.get(which);
            if (mOnSelectCurrencyListener != null) {
                mOnSelectCurrencyListener.OnSelectCurrency(currency);
            }
        }
    }
}
