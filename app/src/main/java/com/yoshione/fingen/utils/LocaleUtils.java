package com.yoshione.fingen.utils;

import android.content.Context;
import android.os.Build;

import java.util.Locale;

/**
 * Created by Leonid on 13.11.2016.
 */

public class LocaleUtils {
    public static Locale getLocale(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            return  context.getResources().getConfiguration().getLocales().get(0);
        } else {
            return context.getResources().getConfiguration().locale;
        }
    }
}
