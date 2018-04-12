/*
 * Copyright (c) 2015.
 */

package com.yoshione.fingen.utils;

import android.content.SharedPreferences;

/**
 * Created by slv on 03.12.2015.
 *
 */
public class NotificationCounter {
    private final SharedPreferences mSharedPreferences;

    public NotificationCounter(SharedPreferences mSharedPreferences) {
        this.mSharedPreferences = mSharedPreferences;
    }

    public void addNotification(int notificationId){
        mSharedPreferences.edit().putInt(String.format("NotifID_%s", String.valueOf(notificationId)),getNotificationCount(notificationId)+1).apply();
    }

    public void removeNotification(int notificationId){
        mSharedPreferences.edit().remove(String.format("NotifID_%s",String.valueOf(notificationId))).apply();
    }

    public int getNotificationCount(int notificationId){
        return mSharedPreferences.getInt(String.format("NotifID_%s",String.valueOf(notificationId)),0);
    }
}
