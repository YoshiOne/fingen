package com.yoshione.fingen.adapter;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.ContextThemeWrapper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ArrayAdapter;

import com.yoshione.fingen.FgConst;
import com.yoshione.fingen.R;
import com.yoshione.fingen.adapter.viewholders.TransactionViewHolder;
import com.yoshione.fingen.adapter.viewholders.TransactionViewHolderParams;
import com.yoshione.fingen.model.Transaction;
import com.yoshione.fingen.utils.ScreenUtils;
import com.yoshione.fingen.widgets.ToolbarActivity;

import java.util.ArrayList;
import java.util.List;

public class TransactionsArrayAdapter extends ArrayAdapter<Transaction> {
    private final ContextThemeWrapper mContextThemeWrapper;
    private List<Transaction> mTransactionList = new ArrayList<>();
    private TransactionViewHolderParams mParams;
    private ToolbarActivity mActivity;

    public TransactionsArrayAdapter(@NonNull ToolbarActivity activity, List<Transaction> list, AdapterTransactions.OnTransactionItemEventListener eventListener) {
        super(activity, 0, list);
        mTransactionList.clear();
        mTransactionList.addAll(list);

        if (PreferenceManager.getDefaultSharedPreferences(activity).getBoolean(FgConst.PREF_COMPACT_VIEW_MODE, false)) {
            mContextThemeWrapper = new ContextThemeWrapper(activity, R.style.StyleListItemTransationsCompact);
        } else {
            mContextThemeWrapper = new ContextThemeWrapper(activity, R.style.StyleListItemTransationsNormal);
        }
        mParams = new TransactionViewHolderParams(activity);
        mParams.mOnTransactionItemEventListener = eventListener;
        mParams.mShowDateInsteadOfRunningBalance = true;
        mActivity = activity;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        ViewGroup.LayoutParams lp = parent.getLayoutParams();
        lp.width = WindowManager.LayoutParams.MATCH_PARENT;
        lp.height = ScreenUtils.dpToPx(500f, mActivity);
        parent.setLayoutParams(lp);
        @SuppressLint("ViewHolder") View view = LayoutInflater.from(mContextThemeWrapper).inflate(R.layout.list_item_transactions_2, parent, false);
        TransactionViewHolder viewHolder = new TransactionViewHolder(mParams, null, mActivity, view);
        viewHolder.bindTransaction(mTransactionList.get(position));
        return viewHolder.itemView;
    }

    @Override
    public int getCount() {
        return mTransactionList.size();
    }
}
