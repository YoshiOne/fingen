/*
 * Copyright (c) 2015.
 */

package com.yoshione.fingen.utils;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.support.v4.content.ContextCompat;
import android.support.v7.preference.PreferenceManager;

import com.yoshione.fingen.R;

/**
 * Created by slv on 10.11.2015.
 *a
 */
public class ColorUtils {
//    private final int baseColor = Color.WHITE;
//
//    private final int baseRed = Color.red(baseColor);
//    private final int baseGreen = Color.green(baseColor);
//    private final int baseBlue = Color.blue(baseColor);

    private static final int[] COLORS = {
            Color.rgb(207, 248, 246),
            Color.rgb(148, 212, 212),
            Color.rgb(136, 180, 187),
            Color.rgb(118, 174, 175),
            Color.rgb(42,  109, 130),
            Color.rgb(217, 80,  138),
            Color.rgb(254, 149, 7),
            Color.rgb(254, 247, 120),
            Color.rgb(106, 167, 134),
            Color.rgb(53,  194, 209),
            Color.rgb(64,  89,  128),
            Color.rgb(149, 165, 124),
            Color.rgb(217, 184, 162),
            Color.rgb(191, 134, 134),
            Color.rgb(179, 48,  80),
            Color.rgb(193, 37,  82),
            Color.rgb(255, 102, 0),
            Color.rgb(245, 199, 0),
            Color.rgb(106, 150, 31),
            Color.rgb(179, 100, 53),
            Color.rgb(192, 255, 140),
            Color.rgb(255, 247, 140),
            Color.rgb(255, 208, 140),
            Color.rgb(140, 234, 255),
            Color.rgb(255, 140, 157)
    };

    public static int getColor(Context context) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        int colorInd = preferences.getInt("color_ind", 0);
        if (colorInd >= COLORS.length) {
            colorInd = 0;
        }
        preferences.edit().putInt("color_ind", colorInd + 1).apply();
        return COLORS[colorInd];
    }

    public static int getBackgroundColor(Activity activity) {
        return ContextCompat.getColor(activity, activity.getTheme().obtainStyledAttributes(new int[]{android.R.attr.windowBackground}).getResourceId(0, 0));
    }

    public static int getlistItemBackgroundColor(Activity activity) {
        return ContextCompat.getColor(activity, activity.getTheme().obtainStyledAttributes(new int[]{R.attr.listItemBackground}).getResourceId(0, 0));
    }

//    public static int getGrayBackgroundColor(Activity activity) {
//        return ContextCompat.getColor(activity, activity.getTheme().obtainStyledAttributes(new int[]{R.attr.grayBackground}).getResourceId(0, 0));
//    }

    public static int getTextColor(Activity activity) {
        return ContextCompat.getColor(activity, activity.getTheme().obtainStyledAttributes(new int[]{android.R.attr.textColor}).getResourceId(0, 0));
    }

    public static int ContrastColor(int color)
    {
        int d = 0;

        // Counting the perceptive luminance - human eye favors green color...
        double a = 1 - ( 0.299 * Color.red(color) + 0.587 * Color.green(color) + 0.114 * Color.blue(color))/255;

        if (a < 0.5)
            d = 0; // bright colors - black font
        else
            d = 255; // dark colors - white font

        return  Color.rgb(d, d, d);
    }
}
