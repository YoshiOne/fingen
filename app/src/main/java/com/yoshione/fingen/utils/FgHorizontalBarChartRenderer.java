package com.yoshione.fingen.utils;

import android.app.Activity;
import android.graphics.Canvas;
import android.graphics.PointF;

import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.renderer.XAxisRendererHorizontalBarChart;
import com.github.mikephil.charting.utils.Transformer;
import com.github.mikephil.charting.utils.ViewPortHandler;
import com.yoshione.fingen.FragmentBarChart;

public class FgHorizontalBarChartRenderer extends XAxisRendererHorizontalBarChart {
    private int mOutlineColor;

    public FgHorizontalBarChartRenderer(Activity activity, ViewPortHandler viewPortHandler, XAxis xAxis, Transformer trans, BarChart chart) {
        super(viewPortHandler, xAxis, trans, chart);

        mOutlineColor = ColorUtils.getTextInverseColor(activity);
    }

    @Override
    protected void drawLabel(Canvas c, String label, int xIndex, float x, float y, PointF anchor, float angleDegrees) {
        ChartUtils.drawXAxisValue(c, label, x, y, mAxisLabelPaint, anchor, angleDegrees, mOutlineColor);
        super.drawLabel(c, label, xIndex, x, y, anchor, angleDegrees);
    }
}
