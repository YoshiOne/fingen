package com.yoshione.fingen;

import android.content.SharedPreferences;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.github.clans.fab.FloatingActionButton;
import com.github.mikephil.charting.animation.Easing;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.formatter.DefaultValueFormatter;
import com.github.mikephil.charting.formatter.PercentFormatter;
import com.github.mikephil.charting.highlight.Highlight;
import com.github.mikephil.charting.interfaces.datasets.IPieDataSet;
import com.github.mikephil.charting.listener.OnChartValueSelectedListener;
import com.github.mikephil.charting.utils.ColorTemplate;
import com.yoshione.fingen.dao.AbstractDAO;
import com.yoshione.fingen.dao.BaseDAO;
import com.yoshione.fingen.interfaces.IAbstractModel;
import com.yoshione.fingen.managers.AbstractModelManager;
import com.yoshione.fingen.utils.BaseNode;
import com.yoshione.fingen.utils.CabbageFormatter;
import com.yoshione.fingen.utils.ColorUtils;
import com.yoshione.fingen.utils.FgLargeValuesFormatter;
import com.yoshione.fingen.utils.NormalValuesFormatter;

import java.math.BigDecimal;
import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;

/**
 * Created by Leonid on 16.08.2015.
 * a
 */
public class FragmentPieChart extends Fragment implements OnChartValueSelectedListener {

    public static final String TAG = "FragmentPieChart";
    @BindView(R.id.pieChart)
    PieChart mPieChart;
    Unbinder unbinder;
    @BindView(R.id.fab)
    FloatingActionButton mFab;
    @BindView(R.id.textViewSelected)
    TextView mTextViewSelected;
    @BindView(R.id.imageViewColor)
    ImageView mImageViewColor;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(@NonNull final LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_pie_chart, container, false);
        unbinder = ButterKnife.bind(this, view);

        mFab.setOnClickListener(new EntityChartFabOnClickListener(mPieChart, getActivity()));

        setupPieChart();

        return view;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getActivity());
        switch (item.getItemId()) {
            case R.id.action_toggle_percents:
                boolean showPercents = preferences.getBoolean(FgConst.PREF_SHOW_PIE_PERCENTS, true);
                preferences.edit().putBoolean(FgConst.PREF_SHOW_PIE_PERCENTS, !showPercents).apply();
                updateChart(false);
                return true;
            case R.id.action_toggle_lines:
                boolean showLines = preferences.getBoolean(FgConst.PREF_SHOW_PIE_LINES, true);
                preferences.edit().putBoolean(FgConst.PREF_SHOW_PIE_LINES, !showLines).apply();
                updateChart(false);
                return true;
//            case R.id.action_toggle_shrink_values:
//                boolean showShrinkLabels = preferences.getBoolean(FgConst.PREF_SHRINK_CHART_LABELS, true);
//                preferences.edit().putBoolean(FgConst.PREF_SHRINK_CHART_LABELS, !showShrinkLabels).apply();
//                updateChart(false);
//                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        updateChart(true);
    }

    void updateChart(boolean animate) {
        ReportBuilder reportBuilder = ReportBuilder.getInstance(getActivity());
        long parentID = reportBuilder.getParentID();
        if (parentID < 0) {
            mPieChart.setCenterText("");
        } else {
            AbstractDAO dao = BaseDAO.getDAO(reportBuilder.getModelType(), getActivity());
            if (dao != null) {
                IAbstractModel model = dao.getModelById(parentID);
                mPieChart.setCenterText(model.getFullName());
            }
        }

        setData(reportBuilder.getEntitiesDataset(), animate);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        unbinder.unbind();
    }

    private void setupPieChart() {
        mPieChart.setDescription("");
        mPieChart.setExtraOffsets(40, 10, 40, 5);

        mPieChart.setDragDecelerationFrictionCoef(0.95f);

        mPieChart.setDrawHoleEnabled(true);
        mPieChart.setHoleColor(ColorUtils.getBackgroundColor(getActivity()));
        mPieChart.setCenterTextColor(ColorUtils.getTextColor(getActivity()));

        mPieChart.setHoleRadius(50f);
        mPieChart.setTransparentCircleRadius(50f);

        mPieChart.setRotationAngle(0);
        mPieChart.setRotationEnabled(true);
        mPieChart.setHighlightPerTapEnabled(true);

        mPieChart.setOnChartValueSelectedListener(this);

        mPieChart.getLegend().setEnabled(false);
    }

    private void setData(BaseNode tree, boolean animate) {
        ReportBuilder reportBuilder = ReportBuilder.getInstance(getActivity());
        if (reportBuilder.getActiveShowIndex() == ReportBuilder.SHOW_INCOME & tree.getModel().getIncome().compareTo(BigDecimal.ZERO) == 0) {
            mPieChart.clear();
            return;
        }

        if (reportBuilder.getActiveShowIndex() == ReportBuilder.SHOW_EXPENSE & tree.getModel().getExpense().compareTo(BigDecimal.ZERO) == 0) {
            mPieChart.clear();
            return;
        }

        ArrayList<Entry> yVals = new ArrayList<>();
        ArrayList<String> xVals = new ArrayList<>();
        ArrayList<IAbstractModel> models = new ArrayList<>();
        BigDecimal val;
        int i = 0;
        for (BaseNode node : tree.getChildren()) {
            switch (reportBuilder.getActiveShowIndex()) {
                case ReportBuilder.SHOW_INCOME:
                    val = node.getModel().getIncome();
                    break;
                case ReportBuilder.SHOW_EXPENSE:
                    val = node.getModel().getExpense();
                    break;
                default:
                    val = BigDecimal.ZERO;
            }
            if (val.compareTo(BigDecimal.ZERO) != 0) {
                models.add(node.getModel());
            }
        }

        if (models.isEmpty()) {
            mPieChart.clear();
            return;
        }

        BigDecimal total = reportBuilder.getActiveShowIndex() == ReportBuilder.SHOW_INCOME ? tree.getModel().getIncome() : tree.getModel().getExpense();

        BigDecimal v;
        for (int j = models.size() - 1; j >= 0; j--) {
            if (reportBuilder.getActiveShowIndex() == ReportBuilder.SHOW_INCOME) {
                v = models.get(j).getIncome();
            } else {
                v = models.get(j).getExpense();
            }
            if ((v.floatValue() / total.floatValue()) < 0.005) {
                models.remove(j);
            }
        }

        ArrayList<IAbstractModel> models1 = new ArrayList<>();
        while (!models.isEmpty()) {
            models1.add(models.get(0));
            models.remove(0);
            if (!models.isEmpty()) {
                models1.add(models.get(models.size() - 1));
                models.remove(models.size() - 1);
            }
        }

        for (IAbstractModel model : models1) {
            if (reportBuilder.getActiveShowIndex() == ReportBuilder.SHOW_INCOME) {
                v = model.getIncome();
            } else {
                v = model.getExpense();
            }
            Entry pieEntry = new Entry(v.floatValue(), i++);
            pieEntry.setData(model);
            yVals.add(pieEntry);
            xVals.add(model.toString());
        }

        PieDataSet dataSet = new PieDataSet(yVals, "");
        dataSet.setSliceSpace(0f);
        dataSet.setValueTextColor(ColorUtils.getTextColor(getActivity()));
//        dataSet.setSelectionShift(5f);
        if (PreferenceManager.getDefaultSharedPreferences(getActivity()).getBoolean(FgConst.PREF_SHOW_PIE_LINES, true)) {
            dataSet.setValueLinePart1OffsetPercentage(80.f);
            dataSet.setValueLinePart1Length(0.6f);
            dataSet.setValueLinePart2Length(0.1f);
            dataSet.setValueLineVariableLength(true);
            dataSet.setValueLineColor(ColorUtils.getTextColor(getActivity()));
            dataSet.setXValuePosition(PieDataSet.ValuePosition.OUTSIDE_SLICE);
            dataSet.setYValuePosition(PieDataSet.ValuePosition.OUTSIDE_SLICE);
            mPieChart.setExtraOffsets(40, 10, 40, 5);
        } else {
            dataSet.setXValuePosition(PieDataSet.ValuePosition.INSIDE_SLICE);
            dataSet.setYValuePosition(PieDataSet.ValuePosition.INSIDE_SLICE);
            mPieChart.setExtraOffsets(5, 5, 5, 5);
        }

        // add a lot of colors

        ArrayList<Integer> colors = new ArrayList<>();

        for (int c : ColorTemplate.VORDIPLOM_COLORS) colors.add(c);
        for (int c : ColorTemplate.JOYFUL_COLORS) colors.add(c);
        for (int c : ColorTemplate.COLORFUL_COLORS) colors.add(c);
        for (int c : ColorTemplate.LIBERTY_COLORS) colors.add(c);
        for (int c : ColorTemplate.PASTEL_COLORS) colors.add(c);
        colors.add(ColorTemplate.getHoloBlue());

        dataSet.setColors(colors);

        PieData data = new PieData(xVals, dataSet);
        if (PreferenceManager.getDefaultSharedPreferences(getActivity()).getBoolean(FgConst.PREF_SHOW_PIE_PERCENTS, true)) {
            mPieChart.setUsePercentValues(true);
            data.setValueFormatter(new PercentFormatter());
        } else {
            mPieChart.setUsePercentValues(false);
            if (PreferenceManager.getDefaultSharedPreferences(getActivity()).getBoolean(FgConst.PREF_SHRINK_CHART_LABELS, true)) {
                data.setValueFormatter(new FgLargeValuesFormatter());
            } else {
                data.setValueFormatter(new NormalValuesFormatter());
            }
        }
        data.setValueTextSize(11f);
        mPieChart.setData(data);

        mImageViewColor.setVisibility(View.INVISIBLE);
        mTextViewSelected.setVisibility(View.INVISIBLE);

        if (animate) {
            mPieChart.animateY(1000, Easing.EasingOption.EaseInOutQuad);
            mFab.hide(false);
            Handler handler = new Handler();
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    if (mPieChart != null) {
                        mPieChart.highlightValues(null);
                    }
                }
            }, 1000);

        } else {
            mPieChart.invalidate();
            if (mPieChart.getHighlighted() == null) {
                mFab.hide(false);
            } else {
                mFab.show(false);
            }
        }

    }

    @Override
    public void onValueSelected(Entry e, int dataSetIndex, Highlight h) {
        ReportBuilder reportBuilder = ReportBuilder.getInstance(getActivity());
                IAbstractModel model = (IAbstractModel) e.getData();
                if (AbstractModelManager.getAllChildren(model, getActivity()).size() > 0) {
                    reportBuilder.setParentID(model.getID());
                    updateChart(true);
                    return;
                }
        CabbageFormatter cabbageFormatter = null;
        try {
            cabbageFormatter = new CabbageFormatter(reportBuilder.getActiveCabbage());
        } catch (Exception e1) {
            e1.printStackTrace();
        }
        String s = String.format("%s %s", e.getData().toString(), cabbageFormatter.format(new BigDecimal(e.getVal())));
//        Toast.makeText(getActivity(), s, Toast.LENGTH_SHORT).show();
        GradientDrawable bgShape = (GradientDrawable) mImageViewColor.getBackground();
        IPieDataSet dataSet = mPieChart.getData().getDataSet();
        bgShape.setColor(dataSet.getColor(dataSet.getEntryIndex(e)));
        mTextViewSelected.setText(s);

        mFab.show(true);
        mImageViewColor.setVisibility(View.VISIBLE);
        mTextViewSelected.setVisibility(View.VISIBLE);
    }

    @Override
    public void onNothingSelected() {
        mFab.hide(true);
        mImageViewColor.setVisibility(View.INVISIBLE);
        mTextViewSelected.setVisibility(View.INVISIBLE);
    }
}
