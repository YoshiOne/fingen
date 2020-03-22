package com.yoshione.fingen.utils;

import android.content.Context;
import android.util.DisplayMetrics;

/**
 * Created by Leonid on 24.11.2016.
 *
 */

public class ScreenUtils {


    public static int dpToPx(float dp, Context context) {
        DisplayMetrics displayMetrics = context.getResources().getDisplayMetrics();
        float density = displayMetrics.xdpi / DisplayMetrics.DENSITY_DEFAULT;
        if (density < 2)
            density = 2;
        return Math.round(dp * density);
    }

    public static float PxToDp(final Context context, final float px) {
        return px / context.getResources().getDisplayMetrics().density;
    }
}
