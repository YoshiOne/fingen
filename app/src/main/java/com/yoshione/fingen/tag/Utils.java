/*
 * Copyright (c) 2015.
 */

package com.yoshione.fingen.tag;

import android.content.Context;
import android.util.DisplayMetrics;
import android.util.TypedValue;

public class Utils {

	public static int dipToPx(Context c,float dipValue) {
		DisplayMetrics metrics = c.getResources().getDisplayMetrics();
		return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dipValue, metrics);
	}
}
