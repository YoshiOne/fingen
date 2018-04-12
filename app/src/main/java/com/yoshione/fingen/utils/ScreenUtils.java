package com.yoshione.fingen.utils;

import android.content.Context;
import android.util.DisplayMetrics;

/**
 * Created by Leonid on 24.11.2016.
 *
 */

public class ScreenUtils {


    public static int dpToPx(int dp, Context context) {
        DisplayMetrics displayMetrics = context.getResources().getDisplayMetrics();
        return Math.round(dp * (displayMetrics.xdpi / DisplayMetrics.DENSITY_DEFAULT));
    }

    public static float PxToDp(final Context context, final float px) {
        return px / context.getResources().getDisplayMetrics().density;
    }
}
