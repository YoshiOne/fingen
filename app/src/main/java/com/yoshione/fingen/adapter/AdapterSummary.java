package com.yoshione.fingen.adapter;

import android.app.Activity;
import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TableLayout;
import android.widget.TextView;

import com.yoshione.fingen.ActivityTransactions;
import com.yoshione.fingen.FgConst;
import com.yoshione.fingen.R;
import com.yoshione.fingen.filters.AbstractFilter;
import com.yoshione.fingen.filters.DateRangeFilter;
import com.yoshione.fingen.managers.SumsManager;
import com.yoshione.fingen.model.Cabbage;
import com.yoshione.fingen.model.SummaryItem;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Random;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by slv on 07.11.2016.
 * 1
 */

public class AdapterSummary extends RecyclerView.Adapter {

    private List<SummaryItem> mList;
    private HashMap<Long, Cabbage> mCabbages;
    private Activity mActivity;

    public AdapterSummary(Activity activity) {
        mList = new ArrayList<>();
        mActivity = activity;
    }

    public void setList(List<SummaryItem> list, HashMap<Long, Cabbage> cabbages) {
        mList = list;
        mCabbages = cabbages;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        RecyclerView.ViewHolder vh;
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item_summary, parent, false);

        vh = new SummaryViewHolder(v);

        return vh;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        final SummaryItem item = mList.get(position);

        SummaryViewHolder vh = (SummaryViewHolder) holder;
        vh.itemView.setLongClickable(true);
        vh.mTextView.setText(item.getName());

        vh.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                DateRangeFilter filter = new DateRangeFilter(new Random().nextInt(),mActivity);
                filter.setRange(item.getIntervalFirst(), item.getIntervalSecond(), mActivity);
                ArrayList<AbstractFilter> filters = new ArrayList<>();
                filters.add(filter);

                Intent intent = new Intent(mActivity, ActivityTransactions.class);
                intent.putParcelableArrayListExtra("filter_list", filters);
                intent.putExtra("caption", item.getName());
                intent.putExtra(FgConst.HIDE_FAB, true);
                intent.putExtra(FgConst.LOCK_SLIDINGUP_PANEL, true);
                mActivity.startActivity(intent);
            }
        });

        SumsManager.updateSummaryTable(mActivity, vh.mLayoutSummaryTable, false, item.getListSumsByCabbage(), mCabbages, null);
    }

    @Override
    public int getItemCount() {
        return mList.size();
    }

    @Override
    public long getItemId(int position) {
        return mList.get(position).getID();
    }

    class SummaryViewHolder extends RecyclerView.ViewHolder {

        @BindView(R.id.textView)
        TextView mTextView;
        @BindView(R.id.layoutSummaryTable)
        TableLayout mLayoutSummaryTable;

        SummaryViewHolder(View v) {
            super(v);

            ButterKnife.bind(this, v);

        }
    }
}
