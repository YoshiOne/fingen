package com.yoshione.fingen.managers;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Typeface;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.util.TypedValue;
import android.view.Gravity;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import com.yoshione.fingen.R;
import com.yoshione.fingen.classes.ListSumsByCabbage;
import com.yoshione.fingen.classes.SumsByCabbage;
import com.yoshione.fingen.model.Cabbage;
import com.yoshione.fingen.utils.CabbageFormatter;

import java.math.BigDecimal;
import java.util.HashMap;

/**
 * Created by Leonid on 06.11.2016.
 * a
 */

public class SumsManager {


    @SuppressLint("SetTextI18n")
    public static synchronized void updateSummaryTable(Activity activity, TableLayout layoutSumTable,
                                                boolean isAddStartBalance,
                                                ListSumsByCabbage listSumsByCabbage,
                                                HashMap<Long, Cabbage> cabbages,
                                                @Nullable String captions[]) {
        if (layoutSumTable == null) return;
        CabbageFormatter cf;
        layoutSumTable.removeAllViews();
        TableRow tableRow;
        TextView textView;
        SumsByCabbage sums;
        float textSize = 14;//activity.getResources().getDimension(R.dimen.text_size_small);
        int positiveAmountColor = ContextCompat.getColor(activity, R.color.positive_color);
        int negativeAmountColor = ContextCompat.getColor(activity, R.color.negative_color);
        int zeroAmountColor = ContextCompat.getColor(activity, R.color.light_gray_text);
        if (captions == null) {
            Resources res = activity.getResources();
            captions = new String[]{res.getString(R.string.ent_total), res.getString(R.string.ent_income), res.getString(R.string.ent_outcome)};
        }
        tableRow = new TableRow(activity);
        tableRow.setGravity(Gravity.CENTER);
        //Income caption
        textView = new TextView(activity);
        setTextViewParams(textView, positiveAmountColor, captions[1], true, textSize, Gravity.START);
        tableRow.addView(textView);
        //Expense caption
        textView = new TextView(activity);
        setTextViewParams(textView, negativeAmountColor, captions[2], true, textSize, Gravity.CENTER);
        tableRow.addView(textView);
        layoutSumTable.addView(tableRow);
        //Total caption
        textView = new TextView(activity);
        TypedArray a = activity.getTheme().obtainStyledAttributes(new int[]{android.R.attr.textColorPrimary});
        setTextViewParams(textView, ContextCompat.getColor(activity, a.getResourceId(0, 0)), captions[0], true, textSize, Gravity.END);
        tableRow.addView(textView);

        int rowCount = 0;

        BigDecimal total;
        BigDecimal income;
        BigDecimal expense;
        float textSize1;
        for (int j = 0; j < listSumsByCabbage.size(); j++) {
            if (!listSumsByCabbage.get(j).isEmpty(isAddStartBalance)) {
                sums = listSumsByCabbage.get(j);
                total = sums.getInTrSum().add(sums.getOutTrSum());
                if (isAddStartBalance) {
                    total = total.add(sums.getStartBalance());
                }
                income = sums.getInTrSum();
                expense = sums.getOutTrSum();

                if (Math.max(total.doubleValue(), Math.max(income.doubleValue(), expense.doubleValue())) > 1_000_000) {
                    textSize1 = textSize - 2.5f;
                } else {
                    textSize1 = textSize;
                }

                tableRow = new TableRow(activity);
                tableRow.setGravity(Gravity.CENTER);
                cf = new CabbageFormatter(cabbages.get(listSumsByCabbage.get(j).getCabbageId()));
                //Income sum
                textView = new TextView(activity);
                setTextViewParams(textView, 1, cf.format(income), false, textSize1, Gravity.START);
                if (income.compareTo(BigDecimal.ZERO) > 0) {
                    textView.setTextColor(positiveAmountColor);
                } else if (income.compareTo(BigDecimal.ZERO) < 0) {
                    textView.setTextColor(negativeAmountColor);
                } else {
                    textView.setTextColor(zeroAmountColor);
                }
                tableRow.addView(textView);
                //Expense sum
                textView = new TextView(activity);
                setTextViewParams(textView, 1, cf.format(expense), false, textSize1, Gravity.CENTER);
                if (expense.compareTo(BigDecimal.ZERO) > 0) {
                    textView.setTextColor(positiveAmountColor);
                } else if (expense.compareTo(BigDecimal.ZERO) < 0) {
                    textView.setTextColor(negativeAmountColor);
                } else {
                    textView.setTextColor(zeroAmountColor);
                }
                tableRow.addView(textView);
                //Total sum
                textView = new TextView(activity);
                setTextViewParams(textView, 1, cf.format(total), false, textSize1, Gravity.END);
                if (total.compareTo(BigDecimal.ZERO) > 0) {
                    textView.setTextColor(positiveAmountColor);
                } else if (total.compareTo(BigDecimal.ZERO) < 0) {
                    textView.setTextColor(negativeAmountColor);
                } else {
                    textView.setTextColor(zeroAmountColor);
                }
                tableRow.addView(textView);
                layoutSumTable.addView(tableRow);
                rowCount++;
            }
        }
        if (rowCount == 0) {
            tableRow = new TableRow(activity);
            tableRow.setGravity(Gravity.CENTER);
            textView = new TextView(activity);
            setTextViewParams(textView, zeroAmountColor, "—", false, textSize, Gravity.START);
            tableRow.addView(textView);
            textView = new TextView(activity);
            setTextViewParams(textView, zeroAmountColor, "—", false, textSize, Gravity.CENTER);
            tableRow.addView(textView);
            textView = new TextView(activity);
            setTextViewParams(textView, zeroAmountColor, "—", false, textSize, Gravity.END);
            tableRow.addView(textView);
            layoutSumTable.addView(tableRow);
        }
    }

    private static void setTextViewParams(TextView textView, int color, String text, boolean typeFaceLight, float textSize, int gravity) {
        textView.setTextSize(TypedValue.COMPLEX_UNIT_DIP, textSize);
        textView.setGravity(gravity);
        textView.setTextColor(color);
        textView.setText(text);
        if (typeFaceLight) {
            textView.setTypeface(Typeface.create("sans-serif-light", Typeface.NORMAL));
        } else {
            textView.setTypeface(Typeface.create("sans-serif-medium", Typeface.NORMAL));
        }
        TableRow.LayoutParams params = new TableRow.LayoutParams(TableRow.LayoutParams.MATCH_PARENT, TableRow.LayoutParams.WRAP_CONTENT);
        params.bottomMargin = 4;
//        params.setMarginStart(16);
        textView.setLayoutParams(params);
    }
}
