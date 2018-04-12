/*
 * Copyright (c) 2015.
 */

package com.yoshione.fingen.utils;

import android.content.Context;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.support.v4.content.ContextCompat;

import com.yoshione.fingen.R;
import com.yoshione.fingen.model.Account;

/**
 * Created by slv on 10.12.2015.
 *
 */
public class IconGenerator {

    public static Drawable getAccountIcon(Account.AccountType accountType, int compareToZero, boolean closed, Context context) {
        Drawable icon;
        if (!closed) {
            switch (accountType) {
                case atCash:
                    icon = context.getDrawable(R.drawable.ic_wallet_gray);
                    break;
                case atAccount:
                    icon = context.getDrawable(R.drawable.ic_account_gray);
                    break;
                case atDebtCard:
                case atCreditCard:
                    icon = context.getDrawable(R.drawable.ic_credit_card_gray);
                    break;
                case atActive:
                case atPassive:
                case atOther:
                default:
                    icon = context.getDrawable(R.drawable.ic_money_gray);
            }
            int color;
            switch (compareToZero) {
                case 1:
                    color = ContextCompat.getColor(context, R.color.positive_color);
                    break;
                case -1:
                    color = ContextCompat.getColor(context, R.color.negative_color);
                    break;
                default:
                    color = ContextCompat.getColor(context, R.color.light_gray_text);
                    break;
            }
            icon.setColorFilter(color, PorterDuff.Mode.SRC_ATOP);
        } else {
            icon = context.getDrawable(R.drawable.ic_lock_gray);
        }
        return icon;
    }

    public static Drawable getExpandIndicatorIcon(boolean expanded, Context context) {
        if (expanded) {
            return ContextCompat.getDrawable(context, R.drawable.ic_expand_less);
        } else {
            return ContextCompat.getDrawable(context, R.drawable.ic_expand_more);
        }
    }
}
