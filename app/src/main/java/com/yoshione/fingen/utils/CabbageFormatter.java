/*
 * Copyright (c) 2015.
 */

package com.yoshione.fingen.utils;

import com.yoshione.fingen.model.Cabbage;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.NumberFormat;

/**
 * Created by Leonid on 11.11.2015.
 *
 */
public class CabbageFormatter {
    private final RoundingMode mRoundingMode = RoundingMode.HALF_EVEN;
    private final Cabbage mCabbage;

    public CabbageFormatter(Cabbage mCabbage) {
        this.mCabbage = mCabbage;
    }

    public synchronized String format(BigDecimal amount){

        if (mCabbage != null) {
            NumberFormat df = NumberFormat.getCurrencyInstance();
            df.setMinimumFractionDigits(mCabbage.getDecimalCount());
            df.setMaximumFractionDigits(mCabbage.getDecimalCount());
            DecimalFormatSymbols dfs = new DecimalFormatSymbols();
            dfs.setCurrencySymbol(mCabbage.getSimbol());
            dfs.setGroupingSeparator(' ');
            dfs.setMonetaryDecimalSeparator('.');
            ((DecimalFormat) df).setDecimalFormatSymbols(dfs);

            return df.format(amount.setScale(mCabbage.getDecimalCount(), mRoundingMode));
        } else {
            return "";
        }
    }
}
