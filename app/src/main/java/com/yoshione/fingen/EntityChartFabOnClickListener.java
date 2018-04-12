package com.yoshione.fingen;

import android.app.Activity;
import android.content.Intent;
import android.view.View;

import com.github.mikephil.charting.charts.Chart;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.highlight.Highlight;
import com.yoshione.fingen.filters.AbstractFilter;
import com.yoshione.fingen.filters.AmountFilter;
import com.yoshione.fingen.filters.NestedModelFilter;
import com.yoshione.fingen.interfaces.IAbstractModel;
import com.yoshione.fingen.managers.FilterManager;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Created by slv on 04.07.2016.
 *
 */

public class EntityChartFabOnClickListener implements View.OnClickListener {
    private final Chart mChart;
    private final Activity mActivity;

    EntityChartFabOnClickListener(Chart chart, Activity activity) {
        mChart = chart;
        mActivity = activity;
    }

    @SuppressWarnings("unchecked")
    @Override
    public void onClick(View v) {
        Highlight highlight[] = mChart.getHighlighted();

        if (highlight == null || highlight.length == 0) {
            return;
        }

        List<Entry> entries = mChart.getEntriesAtIndex(highlight[0].getXIndex());

        if (entries == null || entries.size() == 0) {
            return;
        }

        IAbstractModel model = (IAbstractModel) entries.get(0).getData();

        ReportBuilder reportBuilder = ReportBuilder.getInstance(mActivity);
        ArrayList<AbstractFilter> filters = new ArrayList<>();

        AbstractFilter entityFilter = FilterManager.createFilter(model.getModelType(), model.getID());
        filters.add(entityFilter);

        AmountFilter amountFilter = new AmountFilter(new Random().nextInt(Integer.MAX_VALUE));
        switch (reportBuilder.getActiveShowIndex()) {
            case ReportBuilder.SHOW_EXPENSE:
                amountFilter.setmIncome(false);
                amountFilter.setmOutcome(true);
                amountFilter.setmTransfer(AmountFilter.TRANSACTION_TYPE_BOTH);
                filters.add(amountFilter);
                break;
            case ReportBuilder.SHOW_INCOME:
                amountFilter.setmIncome(true);
                amountFilter.setmOutcome(false);
                amountFilter.setmTransfer(AmountFilter.TRANSACTION_TYPE_BOTH);
                filters.add(amountFilter);
                break;
        }

        for (AbstractFilter filter : reportBuilder.getFilterList()) {
            if (entityFilter != null && filter.getClass().equals(NestedModelFilter.class) & entityFilter.getClass().equals(NestedModelFilter.class)) {
                if ( filter.getModelType() != entityFilter.getModelType()) {
                    filters.add(filter);
                }
            } else {
                if (!filter.getClass().equals(entityFilter != null ? entityFilter.getClass() : null)) {
                    filters.add(filter);
                }
            }
        }

        Intent intent = new Intent(mActivity, ActivityTransactions.class);
        intent.putParcelableArrayListExtra("filter_list", filters);
        intent.putExtra(FgConst.HIDE_FAB, true);
        intent.putExtra(FgConst.LOCK_SLIDINGUP_PANEL, true);
        mActivity.startActivity(intent);
    }
}
