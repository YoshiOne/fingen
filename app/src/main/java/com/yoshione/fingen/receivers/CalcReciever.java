package com.yoshione.fingen.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.yoshione.fingen.calc.CalculatorActivity;
import com.yoshione.fingen.widgets.AmountEditor;

import java.math.BigDecimal;

/**
 * Created by slv on 15.12.2017.
 *
 */
public class CalcReciever extends BroadcastReceiver {
    AmountEditor mEditor;
    Context mContext;

    public CalcReciever() {
        mEditor = null;
        mContext = null;
    }

    public CalcReciever(AmountEditor editor, Context context) {
        mEditor = editor;
        mContext = context;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        if (mEditor == null || mContext == null) {
            return;
        }
        String value = intent.getStringExtra(CalculatorActivity.RESULT);
        BigDecimal amount;
        try {
            amount = new BigDecimal(value);
        } catch (Exception e) {
            amount = BigDecimal.ZERO;
        }
        mEditor.setAmount(amount);
        mContext.unregisterReceiver(this);
    }
}
