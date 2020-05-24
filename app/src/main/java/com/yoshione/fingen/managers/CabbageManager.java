package com.yoshione.fingen.managers;

import android.app.Activity;
import android.content.Context;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;

import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.FragmentManager;

import com.yoshione.fingen.FragmentCabbageEdit;
import com.yoshione.fingen.R;
import com.yoshione.fingen.adapter.CustomAlertAdapter;
import com.yoshione.fingen.model.Cabbage;
import com.yoshione.fingen.utils.LocaleUtils;

import java.util.ArrayList;
import java.util.Currency;
import java.util.List;
import java.util.Locale;

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
        String title = cabbage.getID() < 0 ? context.getResources().getString(R.string.ttl_new_currency) : context.getResources().getString(R.string.ttl_edit_currency);
        FragmentCabbageEdit alertDialog = FragmentCabbageEdit.newInstance(title, cabbage);
        alertDialog.show(fragmentManager, "fragment_cabbage_edit");
    }

    public static Cabbage createFromCode(String code, Context context) {
        Cabbage cabbage = null;
        List<Currency> currencies = new ArrayList<>(Currency.getAvailableCurrencies());
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
        final List<Currency> currencies = new ArrayList<>(Currency.getAvailableCurrencies());
        final ArrayList<Currency> array_sort = new ArrayList<>(currencies);

        final EditText editText = new EditText(activity);
        final ListView listView = new ListView(activity);
        editText.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_search_gray, 0, 0, 0);

        LinearLayout layout = new LinearLayout(activity);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.addView(editText);
        layout.addView(listView);
        AlertDialog.Builder builder = new AlertDialog.Builder(activity).setView(layout);

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
                int textLength = editText.getText().length();
                array_sort.clear();
                String curName;
                for (int i = 0; i < currencies.size(); i++) {
                    curName = String.format("%s (%s)", currencies.get(i).getDisplayName(), currencies.get(i).getCurrencyCode());
                    if (textLength <= curName.length()) {
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
                        (dialog1, which) -> dialog1.dismiss())
                .show();

        listView.setOnItemClickListener((adapterView, view, i, l) -> {
            Currency currency = (Currency) adapterView.getAdapter().getItem(i);
            selectCurrencyListener.OnSelectCurrency(currency);
            dialog.dismiss();
        });
    }

    public interface OnSelectCurrencyListener {
        void OnSelectCurrency(Currency selectedCurrency);
    }

}
