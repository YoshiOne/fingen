package com.yoshione.fingen.utils;

import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.formatter.ValueFormatter;
import com.github.mikephil.charting.utils.ViewPortHandler;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.NumberFormat;

/**
 * Created by slv on 17.06.2016.
 *
 */

public class NormalValuesFormatter implements ValueFormatter {

        private NumberFormat mFormat;

        public NormalValuesFormatter() {
            mFormat = NumberFormat.getCurrencyInstance();
            mFormat.setMinimumFractionDigits(0);
            mFormat.setMaximumFractionDigits(2);
            DecimalFormatSymbols dfs = new DecimalFormatSymbols();
            dfs.setCurrencySymbol("");
            dfs.setGroupingSeparator(' ');
            dfs.setMonetaryDecimalSeparator('.');
            ((DecimalFormat) mFormat).setDecimalFormatSymbols(dfs);
        }

        // ValueFormatter
        @Override
        public String getFormattedValue(float value, Entry entry, int dataSetIndex, ViewPortHandler viewPortHandler) {
            return mFormat.format(value);
        }

        public String getFormattedValue(float value) {
            return mFormat.format(value);
        }
    }

