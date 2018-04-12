package com.yoshione.fingen.utils;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.support.annotation.NonNull;
import android.util.Base64;

import com.yoshione.fingen.FGApplication;

/**
 * Created by slv on 07.07.2016.
 */

public class InApp {
    public final static String SKU_REPORTS = "fingen.reports";
    public final static int SKU_REPORTS_ID = 0;
    ApplicationInfo appInfo;

    public static String getDeveloperKey() {
        Context context = FGApplication.getContext();
        int resId = context.getResources().getIdentifier("inAppLicenseKey", "string", context.getPackageName());
        String key = context.getString(resId);
        resId = context.getResources().getIdentifier("inAppLicenseSalt", "string", context.getPackageName());
        String salt = context.getString(resId);
        return fromX(key, salt);
    }

    /**
     * Method deciphers previously ciphered message
     *
     * @param message ciphered message
     * @param salt    salt which was used for ciphering
     * @return deciphered message
     */
    @NonNull
    private static String fromX(@NonNull String message, @NonNull String salt) {
        return x(new String(Base64.decode(message, 0)), salt);
    }

    /**
     * Method ciphers message. Later {@link #fromX} method might be used for deciphering
     *
     * @param message message to be ciphered
     * @param salt    salt to be used for ciphering
     * @return ciphered message
     */
    @NonNull
    private static String toX(@NonNull String message, @NonNull String salt) {
        return new String(Base64.encode(x(message, salt).getBytes(), 0));
    }

    /**
     * Symmetric algorithm used for ciphering/deciphering. Note that in your application you probably want to modify
     * algorithm used for ciphering/deciphering.
     *
     * @param message message
     * @param salt    salt
     * @return ciphered/deciphered message
     */
    @NonNull
    private static String x(@NonNull String message, @NonNull String salt) {
        final char[] m = message.toCharArray();
        final char[] s = salt.toCharArray();

        final int ml = m.length;
        final int sl = s.length;
        final char[] result = new char[ml];

        for (int i = 0; i < ml; i++) {
            result[i] = (char) (m[i] ^ s[i % sl]);
        }
        return new String(result);
    }
}
