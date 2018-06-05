package com.yoshione.fingen.screenwidget.transactions

import android.annotation.SuppressLint
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Handler
import android.provider.Telephony
import android.text.Spannable
import android.text.SpannableString
import android.text.style.RelativeSizeSpan
import android.view.View
import android.widget.RemoteViews
import android.widget.RemoteViewsService
import com.yoshione.fingen.R
import java.text.SimpleDateFormat
import java.util.*

class BalanceWidgetProvider : AppWidgetProvider() {

    private val updateHandler = Handler()

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Telephony.Sms.Intents.SMS_RECEIVED_ACTION) {
            updateHandler.postDelayed({
                val appWidgetManager = AppWidgetManager.getInstance(context)
                onUpdate(context, appWidgetManager, appWidgetManager.getAppWidgetIds(ComponentName(context, BalanceWidgetProvider::class.java)))
            }, 1000)
        } else {
            super.onReceive(context, intent)
        }
    }

    override fun onUpdate(context: Context, appWidgetManager: AppWidgetManager, appWidgetIds: IntArray) {
        appWidgetIds.forEach {
            val remoteView = RemoteViews(context.packageName, R.layout.widget_transactions)
            val adapterIntent = Intent(context, BalanceAdapterService::class.java)
            adapterIntent.data = Uri.fromParts("content", it.toString(), null)
            remoteView.setRemoteAdapter(R.id.widget_transactions_list, adapterIntent)
            appWidgetManager.updateAppWidget(it, remoteView)
            appWidgetManager.notifyAppWidgetViewDataChanged(it, R.id.widget_transactions_list);
        }
    }
}

class BalanceAdapterService : RemoteViewsService() {

    override fun onGetViewFactory(intent: Intent): RemoteViewsFactory {
        return BalanceListFactory(applicationContext, intent)
    }
}

data class DataItem(
        val card: String,
        val dateSent: Date,
        val difference: Double,
        val balance: Double,
        val parsedDate: Date)

class BalanceListFactory(private val context: Context, private val intent: Intent) : RemoteViewsService.RemoteViewsFactory {

    private val RECOST_DELAY = 2 * 60 * 60 * 1000
    private val items: MutableList<DataItem> = mutableListOf()

    override fun onCreate() = Unit
    override fun getLoadingView() = null
    override fun getItemId(position: Int) = position.toLong()
    override fun onDataSetChanged() = fetchData()
    override fun hasStableIds() = true
    override fun getCount() = items.size
    override fun getViewTypeCount() = 1
    override fun onDestroy() = Unit

    private fun fetchData() {
        items.clear()
        //val appWidgetId = Integer.valueOf(intent.data.schemeSpecificPart)

        //TODO
        //items.addAll(RecountedMapper.map(transactions =))
        items.addAll(arrayOf(
                DataItem("*1111", Date(), 10.0, 20.0, Date()),
                DataItem("*1111", Date(), 11.0, 21.0, Date()),
                DataItem("*1111", Date(), 12.0, 22.0, Date()),
                DataItem("*1111", Date(), 13.0, 23.0, Date()),
                DataItem("*1111", Date(), 14.0, 24.0, Date())
        ))
    }

    @SuppressLint("SimpleDateFormat")
    override fun getViewAt(position: Int): RemoteViews {
        val remoteViews = RemoteViews(context.packageName, R.layout.widget_transactions_item)
        val item = items[position]

        val dateSent = SimpleDateFormat("MM/dd").format(item.dateSent)
        val timeSent = SimpleDateFormat("HH:mm").format(item.dateSent)

        remoteViews.apply {
            setTextViewText(R.id.card, item.card)
            setTextViewText(R.id.date, "$dateSent $timeSent")

            val balanceText = SpannableString("%.2f".format(item.balance))
            balanceText.setSpan(RelativeSizeSpan(0.75f),
                    balanceText.indexOf('.'),
                    balanceText.length,
                    Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)

            setTextViewText(R.id.balance, balanceText)

            val isPlus = item.difference > 0
            val diffText = SpannableString((if (isPlus) "+" else "") + "%.2f".format(item.difference))
            diffText.setSpan(RelativeSizeSpan(0.75f),
                    diffText.indexOf('.'),
                    diffText.length,
                    Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)

            setTextViewText(R.id.difference, diffText)
            setTextColor(R.id.difference, context.resources.getColor(if (isPlus) R.color.color_income else R.color.color_expense, null))

            val timeDiff = Math.abs(item.dateSent.time - item.parsedDate.time)
            val moreThenDay = timeDiff > RECOST_DELAY
            setViewVisibility(R.id.recost, if (moreThenDay) View.VISIBLE else View.GONE)
        }
        return remoteViews
    }
}