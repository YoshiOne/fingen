package com.yoshione.fingen;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.widget.RemoteViews;

import com.yoshione.fingen.model.Transaction;
import com.yoshione.fingen.utils.PrefUtils;

/**
 * Implementation of App Widget functionality.
 */
public class WidgetTransfer extends AppWidgetProvider {

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        RemoteViews remoteViews = new RemoteViews(context.getPackageName(), R.layout.widget_transfer);
        Intent configIntent = new Intent(context, ActivityEditTransaction.class);
        Transaction transaction = new Transaction(PrefUtils.getDefDepID(context));
        transaction.setTransactionType(Transaction.TRANSACTION_TYPE_TRANSFER);
        configIntent.putExtra("transaction", transaction);
        configIntent.putExtra("update_date", true);
        configIntent.putExtra("EXIT", true);
        configIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        Uri data = Uri.withAppendedPath(Uri.parse("ABCD" + "://widget/id/"),String.valueOf(appWidgetIds[0]));
        configIntent.setData(data);

        PendingIntent configPendingIntent = PendingIntent.getActivity(context, 0, configIntent, 0);

        remoteViews.setOnClickPendingIntent(R.id.widgetContainer, configPendingIntent);
        appWidgetManager.updateAppWidget(appWidgetIds, remoteViews);
    }

    @Override
    public void onEnabled(Context context) {
        // Enter relevant functionality for when the first widget is created
    }

    @Override
    public void onDisabled(Context context) {
        // Enter relevant functionality for when the last widget is disabled
    }
}

