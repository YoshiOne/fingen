package com.yoshione.fingen;

import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

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
import com.yoshione.fingen.interfaces.IAbstractModel;
import com.yoshione.fingen.managers.AbstractModelManager;
import com.yoshione.fingen.utils.BaseNode;
import com.yoshione.fingen.utils.CabbageFormatter;
import com.yoshione.fingen.utils.ColorUtils;
import com.yoshione.fingen.utils.FgLargeValuesFormatter;
import com.yoshione.fingen.utils.NormalValuesFormatter;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;

/**
 * Created by Leonid on 16.08.2015.
 * a
 */
public class FragmentBarChart extends Fragment implements OnChartValueSelectedListener {

    public static final String TAG = "FragmentBarChart";
    private final boolean mDrawBarLabels = false;
    @BindView(R.id.barChart)
    HorizontalBarChart mBarChart;
    Unbinder unbinder;
    @BindView(R.id.fab)
    FloatingActionButton mFab;

    private FgLargeValuesFormatter largeValuesFormatter;
    private NormalValuesFormatter mormalValuesFormatter;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_bar_chart, container, false);
        unbinder = ButterKnife.bind(this, view);

        mFab.setOnClickListener(new EntityChartFabOnClickListener(mBarChart, getActivity()));

        setupBarChart();

        largeValuesFormatter = new FgLargeValuesFormatter();
        mormalValuesFormatter = new NormalValuesFormatter();

        return view;
    }

    private boolean isShrinkValues() {
        return PreferenceManager.getDefaultSharedPreferences(getActivity()).getBoolean(FgConst.PREF_SHRINK_CHART_LABELS, true);
    }

    private String formatValue(float value) {
        if (isShrinkValues()) {
            return largeValuesFormatter.makePretty(value);
        } else {
            return mormalValuesFormatter.getFormattedValue(value);
        }
    }

    private ValueFormatter getValueFormatter() {
        if (isShrinkValues()) {
            return largeValuesFormatter;
        } else {
            return mormalValuesFormatter;
        }
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
        xl.setPosition(XAxis.XAxisPosition.BOTTOM_INSIDE);
        xl.setDrawAxisLine(true);
        xl.setDrawGridLines(true);
        xl.setGridLineWidth(0.3f);
        xl.setTextColor(textColor);

        YAxis yl = mBarChart.getAxisLeft();
        yl.setDrawAxisLine(true);
        yl.setDrawGridLines(true);
        yl.setGridLineWidth(0.3f);
        yl.setAxisMinValue(0f); // this replaces setStartAtZero(true)
//        yl.setValueFormatter(valuesFormatter);
        yl.setTextColor(textColor);

        YAxis yr = mBarChart.getAxisRight();
        yr.setDrawAxisLine(true);
        yr.setDrawGridLines(false);
        yr.setAxisMinValue(0f); // this replaces setStartAtZero(true)
//        yr.setValueFormatter(valuesFormatter);
        yr.setTextColor(textColor);

        mBarChart.getLegend().setEnabled(false);
    }

    private Pair<ArrayList<IBarDataSet>, ArrayList<String>> getDatasetsInAndEx(BaseNode tree) {
        int i = 0;
        ArrayList<BarEntry> yValsExpense = new ArrayList<>();
        ArrayList<BarEntry> yValsIncome = new ArrayList<>();
        ArrayList<String> xVals = new ArrayList<>();

        BarEntry barEntry;
        IAbstractModel model;
        for (BaseNode node : tree.getChildren()) {
            model = node.getModel();
            if (model.getExpense().compareTo(BigDecimal.ZERO) > 0) {
                barEntry = new BarEntry(model.getExpense().setScale(2, RoundingMode.HALF_EVEN).floatValue(), i);
                barEntry.setData(model);
                yValsExpense.add(barEntry);
            }
            if (model.getIncome().compareTo(BigDecimal.ZERO) > 0) {
                barEntry = new BarEntry(model.getIncome().setScale(2, RoundingMode.HALF_EVEN).floatValue(), i);
                barEntry.setData(model);
                yValsIncome.add(barEntry);
            }
            xVals.add(String.format("%s (%s)", model.getFullName(),
                    formatValue(model.getIncome().subtract(model.getExpense()).abs().floatValue())));
            i++;
        }

        BarDataSet setExpense = new BarDataSet(yValsExpense, getString(R.string.ent_outcome));
        setExpense.setColor(ContextCompat.getColor(getActivity(), R.color.negative_color));
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

    private Pair<ArrayList<IBarDataSet>, ArrayList<String>> getDatasetsInMinusEx(BaseNode tree) {
        int i = 0;
        ArrayList<BarEntry> yValsSum = new ArrayList<>();
        ArrayList<String> xVals = new ArrayList<>();
        ArrayList<Integer> colors = new ArrayList<>();

        BigDecimal sum;
        BarEntry barEntry;
        IAbstractModel model;
        for (BaseNode node : tree.getChildren()) {
            model = node.getModel();
            sum = model.getIncome().subtract(model.getExpense());
            if (sum.compareTo(BigDecimal.ZERO) != 0) {
                barEntry = new BarEntry(sum.abs().floatValue(), i);
                barEntry.setData(model);
                yValsSum.add(barEntry);
                xVals.add(String.format("%s (%s)", model.getFullName(), formatValue(model.getIncome().subtract(model.getExpense()).abs().floatValue())));
                if (sum.compareTo(BigDecimal.ZERO) >= 0) {
                    colors.add(ContextCompat.getColor(getActivity(), R.color.positive_color));
                } else {
                    colors.add(ContextCompat.getColor(getActivity(), R.color.negative_color));
                }
                i++;
            }
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

    private Pair<ArrayList<IBarDataSet>, ArrayList<String>> getDatasetsSingle(BaseNode tree, boolean expense) {
        int i = 0;
        ArrayList<BarEntry> yVals = new ArrayList<>();
        ArrayList<String> xVals = new ArrayList<>();

        BarEntry barEntry;
        IAbstractModel model;
        for (BaseNode node : tree.getChildren()) {
            model = node.getModel();
            if (expense) {
                if (model.getExpense().compareTo(BigDecimal.ZERO) > 0) {
                    barEntry = new BarEntry(model.getExpense().setScale(2, RoundingMode.HALF_EVEN).floatValue(), i);
                    barEntry.setData(model);
                    yVals.add(barEntry);
                    xVals.add(String.format("%s (%s)", model.getFullName(), formatValue(model.getExpense().abs().floatValue())));
                    i++;
                }
            } else {
                if (model.getIncome().compareTo(BigDecimal.ZERO) > 0) {
                    barEntry = new BarEntry(model.getIncome().setScale(2, RoundingMode.HALF_EVEN).floatValue(), i);
                    barEntry.setData(model);
                    yVals.add(barEntry);
                    xVals.add(String.format("%s (%s)", model.getFullName(), formatValue(model.getIncome().abs().floatValue())));
                    i++;
                }
            }
        }

        BarDataSet set;
        if (expense) {
            set = new BarDataSet(yVals, getString(R.string.ent_outcome));
            set.setColor(ContextCompat.getColor(getActivity(), R.color.negative_color));
            set.setValueFormatter(getValueFormatter());
        } else {
            set = new BarDataSet(yVals, getString(R.string.ent_income));
            set.setColor(ContextCompat.getColor(getActivity(), R.color.positive_color));
            set.setValueFormatter(getValueFormatter());
        }

        int textColor = ColorUtils.getTextColor(getActivity());
        set.setValueTextColor(textColor);
        set.setDrawValues(mDrawBarLabels);

        ArrayList<IBarDataSet> dataSets = new ArrayList<>();
        dataSets.add(set);
        return new Pair<>(dataSets, xVals);
    }

    void updateChart() {
        setData(ReportBuilder.getInstance(getActivity()).getEntitiesDataset());
    }

    private void setData(BaseNode tree) {
        ReportBuilder reportBuilder = ReportBuilder.getInstance(getActivity());
        if (tree.getChildren().size() == 0) {
            mBarChart.clear();
            return;
        }

        Pair<ArrayList<IBarDataSet>, ArrayList<String>> pair;
        switch (reportBuilder.getActiveShowIndex()) {
            case ReportBuilder.SHOW_INCOME_AND_EXPENSE:
                pair = getDatasetsInAndEx(tree);
                break;
            case ReportBuilder.SHOW_INCOME_MINUS_EXPENSE:
                pair = getDatasetsInMinusEx(tree);
                break;
            case ReportBuilder.SHOW_INCOME:
                pair = getDatasetsSingle(tree, false);
                break;
            case ReportBuilder.SHOW_EXPENSE:
                pair = getDatasetsSingle(tree, true);
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

    @Override
    public void onValueSelected(Entry e, int dataSetIndex, Highlight h) {
        ReportBuilder reportBuilder = ReportBuilder.getInstance(getActivity());
//        int dataInd = PreferenceManager.getDefaultSharedPreferences(mActivityReports).getInt("report_data", 0);
//        switch (dataInd) {
//            case ActivityReports.DATA_CATEGORY:
//            case ActivityReports.DATA_PAYEE:
//            case ActivityReports.DATA_PROJECT:
//            case ActivityReports.DATA_DEPARTMENT:
//            case ActivityReports.DATA_LOCATION:
                IAbstractModel model = (IAbstractModel) e.getData();
                if (AbstractModelManager.getAllChildren(model, getActivity()).size() > 0) {
                    reportBuilder.setParentID(model.getID());
                    updateChart();
                    return;
                }
//                break;
//        }
        CabbageFormatter cabbageFormatter = null;
        try {
            cabbageFormatter = new CabbageFormatter(reportBuilder.getActiveCabbage());
        } catch (Exception e1) {
            e1.printStackTrace();
        }
        String s = String.format("%s %s", e.getData().toString(), cabbageFormatter.format(new BigDecimal(e.getVal())));
        Toast.makeText(getActivity(), s, Toast.LENGTH_SHORT).show();
        mFab.show(true);
    }

    @Override
    public void onNothingSelected() {
        mFab.hide(true);
    }

}
