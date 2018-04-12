package com.yoshione.fingen;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.preference.PreferenceManager;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.github.clans.fab.FloatingActionButton;
import com.github.mikephil.charting.charts.HorizontalBarChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.formatter.ValueFormatter;
import com.github.mikephil.charting.highlight.Highlight;
import com.github.mikephil.charting.interfaces.datasets.IBarDataSet;
import com.github.mikephil.charting.listener.OnChartValueSelectedListener;
import com.yoshione.fingen.filters.AbstractFilter;
import com.yoshione.fingen.filters.AmountFilter;
import com.yoshione.fingen.filters.DateRangeFilter;
import com.yoshione.fingen.model.DateEntry;
import com.yoshione.fingen.utils.ColorUtils;
import com.yoshione.fingen.utils.FgLargeValuesFormatter;
import com.yoshione.fingen.utils.NormalValuesFormatter;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.Random;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;

/**
 * Created by Leonid on 16.08.2015.
 * FragmentTimeBarChart
 */
public class FragmentTimeBarChart extends Fragment implements OnChartValueSelectedListener {

    public static final String TAG = "FragmentBarChart";
    private final boolean mDrawBarLabels = false;
    @BindView(R.id.barChart)
    HorizontalBarChart mBarChart;
    Unbinder unbinder;
    @BindView(R.id.fab)
    FloatingActionButton mFab;
    private FgLargeValuesFormatter largeValuesFormatter;
    private NormalValuesFormatter mormalValuesFormatter;
//    public MyMarkerView mMyMarkerView;

    @SuppressWarnings("unchecked")
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_bar_chart, container, false);
        unbinder = ButterKnife.bind(this, view);

        mFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Highlight highlight[] = mBarChart.getHighlighted();

                if (highlight == null || highlight.length == 0) {
                    return;
                }

                List<Entry> entries = mBarChart.getEntriesAtIndex(highlight[0].getXIndex());

                if (entries == null || entries.size() == 0) {
                    return;
                }

                Pair<Date, Date> range = (Pair<Date, Date>) entries.get(0).getData();
                ReportBuilder reportBuilder = ReportBuilder.getInstance(getActivity());
                ArrayList<AbstractFilter> filters = new ArrayList<>();

                DateRangeFilter dateRangeFilter = new DateRangeFilter(new Random().nextInt(Integer.MAX_VALUE), getActivity());
                dateRangeFilter.setmStartDate(range.first);
                dateRangeFilter.setmEndDate(range.second);
                filters.add(dateRangeFilter);

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
                    if (!filter.getClass().equals(DateRangeFilter.class)) {
                        filters.add(filter);
                    }
                }

                Intent intent = new Intent(getActivity(), ActivityTransactions.class);
                intent.putParcelableArrayListExtra("filter_list", filters);
                intent.putExtra(FgConst.HIDE_FAB, true);
                intent.putExtra(FgConst.LOCK_SLIDINGUP_PANEL, true);
                Objects.requireNonNull(getActivity()).startActivity(intent);
            }
        });

        setupBarChart();

        largeValuesFormatter = new FgLargeValuesFormatter();
        mormalValuesFormatter = new NormalValuesFormatter();

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        updateChart();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        unbinder.unbind();
    }

    private void setupBarChart() {
        mBarChart.setDrawBarShadow(false);

        mBarChart.setDrawValueAboveBar(true);

        mBarChart.setDescription("");

        mBarChart.setMaxVisibleValueCount(Integer.MAX_VALUE);

        mBarChart.setPinchZoom(false);

        mBarChart.setDrawGridBackground(false);
        mBarChart.setOnChartValueSelectedListener(this);
        mBarChart.setHighlightPerDragEnabled(false);

        int textColor = ColorUtils.getTextColor(getActivity());

        XAxis xl = mBarChart.getXAxis();
        xl.setPosition(XAxis.XAxisPosition.BOTTOM);
        xl.setDrawAxisLine(true);
        xl.setDrawGridLines(true);
        xl.setGridLineWidth(0.3f);
        xl.setTextColor(textColor);

        YAxis yl = mBarChart.getAxisLeft();
        yl.setDrawAxisLine(true);
        yl.setDrawGridLines(true);
        yl.setGridLineWidth(0.3f);
        yl.setAxisMinValue(0f);
        yl.setTextColor(textColor);

        YAxis yr = mBarChart.getAxisRight();
        yr.setDrawAxisLine(true);
        yr.setDrawGridLines(false);
        yr.setAxisMinValue(0f);
        yr.setTextColor(textColor);

        mBarChart.getLegend().setEnabled(false);
    }

    private boolean isShrinkValues() {
        return android.preference.PreferenceManager.getDefaultSharedPreferences(getActivity()).getBoolean(FgConst.PREF_SHRINK_CHART_LABELS, true);
    }

    private ValueFormatter getValueFormatter() {
        if (isShrinkValues()) {
            return largeValuesFormatter;
        } else {
            return mormalValuesFormatter;
        }
    }

    private Pair<ArrayList<IBarDataSet>, ArrayList<String>> getDatasetsInAndEx(List<DateEntry> entries) {
        int i = 0;
        ArrayList<BarEntry> yValsExpense = new ArrayList<>();
        ArrayList<BarEntry> yValsIncome = new ArrayList<>();
        ArrayList<String> xVals = new ArrayList<>();
        DateFormat df = ReportBuilder.getInstance(getActivity()).getDateFormatter();
        BarEntry barEntry;

        for (DateEntry entry : entries) {
            if (entry.getExpense().compareTo(BigDecimal.ZERO) > 0) {
                barEntry = new BarEntry(entry.getExpense().setScale(2, RoundingMode.HALF_EVEN).floatValue(), i);
                barEntry.setData(getPairDates(entry.getDate()));
                yValsExpense.add(barEntry);
            }
            if (entry.getIncome().compareTo(BigDecimal.ZERO) > 0) {
                barEntry = new BarEntry(entry.getIncome().setScale(2, RoundingMode.HALF_EVEN).floatValue(), i);
                barEntry.setData(getPairDates(entry.getDate()));
                yValsIncome.add(barEntry);
            }
            if (entry.getExpense().compareTo(BigDecimal.ZERO) > 0 | entry.getIncome().compareTo(BigDecimal.ZERO) > 0) {
                xVals.add(df.format(entry.getDate()));
                i++;
            }
        }

        BarDataSet setExpense = new BarDataSet(yValsExpense, getString(R.string.ent_outcome));
        setExpense.setColor(ContextCompat.getColor(Objects.requireNonNull(getActivity()), R.color.negative_color));
        setExpense.setValueFormatter(getValueFormatter());
        setExpense.setDrawValues(mDrawBarLabels);
        BarDataSet setIncome = new BarDataSet(yValsIncome, getString(R.string.ent_income));
        setIncome.setColor(ContextCompat.getColor(getActivity(), R.color.positive_color));
        setIncome.setValueFormatter(getValueFormatter());
        setIncome.setDrawValues(mDrawBarLabels);

        int textColor = ColorUtils.getTextColor(getActivity());
        setExpense.setValueTextColor(textColor);
        setIncome.setValueTextColor(textColor);

        ArrayList<IBarDataSet> dataSets = new ArrayList<>();
        dataSets.add(setIncome);
        dataSets.add(setExpense);
        return new Pair<>(dataSets, xVals);
    }

    private Pair<ArrayList<IBarDataSet>, ArrayList<String>> getDatasetsInMinusEx(List<DateEntry> entries) {
        int i = 0;
        ArrayList<BarEntry> yValsSum = new ArrayList<>();
        ArrayList<String> xVals = new ArrayList<>();
        ArrayList<Integer> colors = new ArrayList<>();
        DateFormat df = ReportBuilder.getInstance(getActivity()).getDateFormatter();
        BarEntry barEntry;

        BigDecimal sum;
        for (DateEntry entry : entries) {
            sum = entry.getIncome().subtract(entry.getExpense());
            barEntry = new BarEntry(sum.abs().floatValue(), i);
            barEntry.setData(getPairDates(entry.getDate()));
            yValsSum.add(barEntry);
            xVals.add(df.format(entry.getDate()));
            if (sum.compareTo(BigDecimal.ZERO) >= 0) {
                colors.add(ContextCompat.getColor(Objects.requireNonNull(getActivity()), R.color.positive_color));
            } else {
                colors.add(ContextCompat.getColor(Objects.requireNonNull(getActivity()), R.color.negative_color));
            }
            i++;
        }

        BarDataSet setSum = new BarDataSet(yValsSum, getString(R.string.ent_income) + " - " + getString(R.string.ent_outcome));
        setSum.setColors(colors);
        setSum.setValueFormatter(getValueFormatter());
        setSum.setDrawValues(mDrawBarLabels);

        int textColor = ColorUtils.getTextColor(getActivity());
        setSum.setValueTextColor(textColor);

        ArrayList<IBarDataSet> dataSets = new ArrayList<>();
        dataSets.add(setSum);
        return new Pair<>(dataSets, xVals);
    }

    private Pair<ArrayList<IBarDataSet>, ArrayList<String>> getDatasetsSingle(List<DateEntry> entries, boolean expense) {
        int i = 0;
        ArrayList<BarEntry> yVals = new ArrayList<>();
        ArrayList<String> xVals = new ArrayList<>();
        DateFormat df = ReportBuilder.getInstance(getActivity()).getDateFormatter();
        BarEntry barEntry;

        for (DateEntry entry : entries) {
            if (expense) {
                if (entry.getExpense().compareTo(BigDecimal.ZERO) > 0) {
                    barEntry = new BarEntry(entry.getExpense().setScale(2, RoundingMode.HALF_EVEN).floatValue(), i);
                    barEntry.setData(getPairDates(entry.getDate()));
                    yVals.add(barEntry);
                    xVals.add(df.format(entry.getDate()));
                    i++;
                }
            } else {
                if (entry.getIncome().compareTo(BigDecimal.ZERO) > 0) {
                    barEntry = new BarEntry(entry.getIncome().setScale(2, RoundingMode.HALF_EVEN).floatValue(), i);
                    barEntry.setData(getPairDates(entry.getDate()));
                    yVals.add(barEntry);
                    xVals.add(df.format(entry.getDate()));
                    i++;
                }
            }
        }

        BarDataSet set;
        if (expense) {
            set = new BarDataSet(yVals, getString(R.string.ent_outcome));
            set.setColor(ContextCompat.getColor(Objects.requireNonNull(getActivity()), R.color.negative_color));
            set.setValueFormatter(getValueFormatter());
        } else {
            set = new BarDataSet(yVals, getString(R.string.ent_income));
            set.setColor(ContextCompat.getColor(Objects.requireNonNull(getActivity()), R.color.positive_color));
            set.setValueFormatter(getValueFormatter());
        }

        int textColor = ColorUtils.getTextColor(getActivity());
        set.setValueTextColor(textColor);
        set.setDrawValues(mDrawBarLabels);

        ArrayList<IBarDataSet> dataSets = new ArrayList<>();
        dataSets.add(set);
        return new Pair<>(dataSets, xVals);
    }

    public void updateChart() {
        setData(ReportBuilder.getInstance(getActivity()).getDatesDataset());
    }

    private void setData(List<DateEntry> entries) {
        ReportBuilder reportBuilder = ReportBuilder.getInstance(getActivity());
        if (entries.size() == 0) {
            mBarChart.clear();
            return;
        }
        Pair<ArrayList<IBarDataSet>, ArrayList<String>> pair;
        switch (reportBuilder.getActiveShowIndex()) {
            case ReportBuilder.SHOW_INCOME_AND_EXPENSE:
                pair = getDatasetsInAndEx(entries);
                break;
            case ReportBuilder.SHOW_INCOME_MINUS_EXPENSE:
                pair = getDatasetsInMinusEx(entries);
                break;
            case ReportBuilder.SHOW_INCOME:
                pair = getDatasetsSingle(entries, false);
                break;
            case ReportBuilder.SHOW_EXPENSE:
                pair = getDatasetsSingle(entries, true);
                break;
            default:
                return;
        }

        if (pair.second.size() == 0) {
            mBarChart.clear();
            return;
        }

        BarData data = new BarData(pair.second, pair.first);
        data.setValueTextSize(10f);
        data.setGroupSpace(80f);
//        data.setValueTypeface(tf);

        mBarChart.setData(data);
        mBarChart.fitScreen();
        mBarChart.animateY(1000);
        mBarChart.highlightValues(null);

        if (mBarChart.getHighlighted() == null) {
            mFab.hide(false);
        } else {
            mFab.show(false);
        }
    }

    private Pair<Date, Date> getPairDates(Date date) {
        int dateRange = PreferenceManager.getDefaultSharedPreferences(Objects.requireNonNull(getActivity())).getInt("report_date_range", 0);
        Calendar c = Calendar.getInstance();
        c.setTime(date);
        c.set(Calendar.HOUR_OF_DAY, 23);
        c.set(Calendar.MINUTE, 59);
        c.set(Calendar.SECOND, 59);
        switch (dateRange) {
            case ReportBuilder.DATE_RANGE_Day:
                break;
            case ReportBuilder.DATE_RANGE_Month:
                c.set(Calendar.DAY_OF_MONTH, c.getActualMaximum(Calendar.DAY_OF_MONTH));
                break;
            case ReportBuilder.DATE_RANGE_YEAR:
                c.set(Calendar.MONTH, c.getActualMaximum(Calendar.MONTH));
                c.set(Calendar.DAY_OF_MONTH, c.getActualMaximum(Calendar.DAY_OF_MONTH));
                break;
        }
        return new Pair<>(date, c.getTime());
    }

    @Override
    public void onValueSelected(Entry e, int dataSetIndex, Highlight h) {
        mFab.show(true);
    }

    @Override
    public void onNothingSelected() {
        mFab.hide(true);
    }
}
