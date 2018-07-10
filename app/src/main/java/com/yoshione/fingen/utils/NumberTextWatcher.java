package com.yoshione.fingen.utils;

import android.text.Editable;
import android.text.TextWatcher;
import android.widget.EditText;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.ParseException;

public class NumberTextWatcher implements TextWatcher {
    @SuppressWarnings("unused")
    private static final String TAG = "NumberTextWatcher";
    private DecimalFormat df;
    private DecimalFormat dfnd;
    private boolean hasFractionalPart;
    private EditText et;

    public NumberTextWatcher(EditText et) {
        DecimalFormatSymbols symbols = new DecimalFormatSymbols();
        symbols.setDecimalSeparator('.');
        symbols.setGroupingSeparator(' ');
        df = new DecimalFormat("#,###.##");
        df.setDecimalSeparatorAlwaysShown(true);
        df.setDecimalFormatSymbols(symbols);
        dfnd = new DecimalFormat("#,###");
        dfnd.setDecimalFormatSymbols(symbols);
        this.et = et;
        hasFractionalPart = false;
    }

    public DecimalFormat getDf() {
        return df;
    }

    public DecimalFormat getDfnd() {
        return dfnd;
    }

    public boolean isHasFractionalPart() {
        return hasFractionalPart;
    }

    @Override
    public void afterTextChanged(Editable s) {
        et.removeTextChangedListener(this);

        try {
            int inilen, endlen;
            inilen = et.getText().length();

            String v = s.toString().replace(String.valueOf(df.getDecimalFormatSymbols().getGroupingSeparator()), "");
            Number n = df.parse(v);
            int cp = et.getSelectionStart();
            String sn;
            if (hasFractionalPart) {
                sn = df.format(n);
            } else {
                sn = dfnd.format(n);
            }
            et.setText(sn);
            endlen = et.getText().length();
            int sel = (cp + (endlen - inilen));
            if (sel > 0 && sel <= et.getText().length()) {
                et.setSelection(sel);
            } else {
                // place cursor at the end?
                et.setSelection(et.getText().length() - 1);
            }
        } catch (NumberFormatException nfe) {
            // do nothing?
        } catch (ParseException e) {
            // do nothing?
        }

        et.addTextChangedListener(this);
    }

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {
    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {
//        String st = s.toString().replace(".", String.valueOf(df.getDecimalFormatSymbols().getDecimalSeparator()));
        if (s.toString().contains(String.valueOf(df.getDecimalFormatSymbols().getDecimalSeparator()))) {
            hasFractionalPart = true;
        } else {
            hasFractionalPart = false;
        }
    }
}
