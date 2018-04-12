package com.yoshione.fingen.utils;

import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.formatter.ValueFormatter;
import com.github.mikephil.charting.formatter.YAxisValueFormatter;
import com.github.mikephil.charting.utils.ViewPortHandler;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Locale;

/**
 * Created by slv on 17.06.2016.
 *
 */

public class FgLargeValuesFormatter implements ValueFormatter, YAxisValueFormatter {

        private static final String[] SUFFIX = new String[]{
                "", "k", "m", "b", "t"
        };
        private static final int MAX_LENGTH = 4;
        private final DecimalFormat mFormat;

        public FgLargeValuesFormatter() {
            DecimalFormatSymbols otherSymbols = new DecimalFormatSymbols(Locale.getDefault());
            otherSymbols.setDecimalSeparator('.');
            otherSymbols.setGroupingSeparator(Character.MIN_VALUE);
            mFormat = new DecimalFormat("###E0", otherSymbols);
        }

        // ValueFormatter
        @Override
        public String getFormattedValue(float value, Entry entry, int dataSetIndex, ViewPortHandler viewPortHandler) {
            return makePretty(value);
        }

        // YAxisValueFormatter
        @Override
        public String getFormattedValue(float value, YAxis yAxis) {
            return makePretty(value);
        }

        /**
         * Formats each number properly. Special thanks to Roman Gromov
         * (https://github.com/romangromov) for this piece of code.
         */
        public String makePretty(double number) {

            String r = mFormat.format(number > 1 ? Math.round(number) : number);

            r = r.replaceAll("E[0-9]", SUFFIX[Character.getNumericValue(r.charAt(r.length() - 1)) / 3]);

            while (r.length() > MAX_LENGTH || r.matches("[0-9]+\\.[a-z]")) {
                r = r.substring(0, r.length() - 2) + r.substring(r.length() - 1);
            }

            return r;
        }
    }

