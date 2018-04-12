package com.yoshione.fingen.utils;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.ContextWrapper;
import android.graphics.Color;
import android.os.Build;

import com.yoshione.fingen.R;

/**
 * Created by slv on 07.11.2017.
 */

public class NotificationHelper extends ContextWrapper {
    public static final String PRIMARY_CHANNEL = "default";
    private static NotificationHelper sInstance;
    private NotificationManager manager;

    public NotificationHelper(Context base) {
        super(base);

        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel chan1 = new NotificationChannel(PRIMARY_CHANNEL,
                    getString(R.string.app_name), NotificationManager.IMPORTANCE_DEFAULT);
            chan1.setLightColor(Color.GREEN);
            chan1.setLockscreenVisibility(Notification.VISIBILITY_PRIVATE);
            getManager().createNotificationChannel(chan1);
        }
    }

    public synchronized static NotificationHelper getInstance(Context context) {
        if (sInstance == null) {
            sInstance = new NotificationHelper(context.getApplicationContext());
        }
        return sInstance;
    }

    private NotificationManager getManager() {
        if (manager == null) {
            manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        }
        return manager;
    }

    public void notify(int id, Notification.Builder notification) {
        getManager().notify(id, notification.build());
    }

    public Notification.Builder getNotification() {
        Notification.Builder nb;
        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            nb = new Notification.Builder(getApplicationContext(), PRIMARY_CHANNEL);
        } else {
            nb = new Notification.Builder(getApplicationContext());
        }
        return nb;
    }

    public void cancel(int id) {
        getManager().cancel(id);
    }
}
