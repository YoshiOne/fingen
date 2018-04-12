package com.yoshione.fingen;


import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.view.View;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.RadioGroup;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;

import static com.yoshione.fingen.filters.DateRangeFilter.DATE_RANGE_DAY;
import static com.yoshione.fingen.filters.DateRangeFilter.DATE_RANGE_MODIFIER_CURRENT;
import static com.yoshione.fingen.filters.DateRangeFilter.DATE_RANGE_MODIFIER_CURRENTAND_LAST;
import static com.yoshione.fingen.filters.DateRangeFilter.DATE_RANGE_MODIFIER_LAST;
import static com.yoshione.fingen.filters.DateRangeFilter.DATE_RANGE_MONTH;
import static com.yoshione.fingen.filters.DateRangeFilter.DATE_RANGE_WEEK;
import static com.yoshione.fingen.filters.DateRangeFilter.DATE_RANGE_YEAR;

/**
 * Created by slv on 30.11.2017.
 * *
 */

public class FragmentDateFilterEdit extends DialogFragment {
    @BindView(R.id.radioButtonDateRangeCurrent)
    RadioButton mRadioButtonDateRangeCurrent;
    @BindView(R.id.radioButtonDateRangeLast)
    RadioButton mRadioButtonDateRangePast;
    @BindView(R.id.radioButtonDateRangeCurrentAndLast)
    RadioButton mRadioButtonDateRangeCurrentAndPast;
    @BindView(R.id.radioGroupModifier)
    RadioGroup mRadioGroupModifier;
    @BindView(R.id.radioButtonDateRangeDay)
    RadioButton mRadioButtonDateRangeDay;
    @BindView(R.id.radioButtonDateRangeWeek)
    RadioButton mRadioButtonDateRangeWeek;
    @BindView(R.id.radioButtonDateRangeMonth)
    RadioButton mRadioButtonDateRangeMonth;
    @BindView(R.id.radioButtonDateRangeYear)
    RadioButton mRadioButtonDateRangeYear;
    @BindView(R.id.radioGroupRange)
    RadioGroup mRadioGroupRange;
    Unbinder unbinder;
    IOnComplete mOnComplete;

    public void setOnComplete(IOnComplete onComplete) {
        mOnComplete = onComplete;
    }

    public static FragmentDateFilterEdit newInstance(int range, int modifier) {

        Bundle args = new Bundle();
        args.putInt("range", range);
        args.putInt("modifier", modifier);

        FragmentDateFilterEdit fragment = new FragmentDateFilterEdit();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        View view = getActivity().getLayoutInflater().inflate(R.layout.fragment_date_filter_settings, null);
        unbinder = ButterKnife.bind(this, view);

        switch (getArguments().getInt("modifier")) {
            case DATE_RANGE_MODIFIER_CURRENT:
                mRadioGroupModifier.check(R.id.radioButtonDateRangeCurrent);
                break;
            case DATE_RANGE_MODIFIER_LAST:
                mRadioGroupModifier.check(R.id.radioButtonDateRangeLast);
                break;
            case DATE_RANGE_MODIFIER_CURRENTAND_LAST:
                mRadioGroupModifier.check(R.id.radioButtonDateRangeCurrentAndLast);
                break;
        }
        switch (getArguments().getInt("range")) {
            case DATE_RANGE_DAY:
                mRadioGroupRange.check(R.id.radioButtonDateRangeDay);
                break;
            case DATE_RANGE_WEEK:
                mRadioGroupRange.check(R.id.radioButtonDateRangeWeek);
                break;
            case DATE_RANGE_MONTH:
                mRadioGroupRange.check(R.id.radioButtonDateRangeMonth);
                break;
            case DATE_RANGE_YEAR:
                mRadioGroupRange.check(R.id.radioButtonDateRangeYear);
                break;
        }

        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(getActivity());
        alertDialogBuilder.setView(view);
        alertDialogBuilder.setTitle("");
        alertDialogBuilder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

            }
        });
        alertDialogBuilder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });


        return alertDialogBuilder.create();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        unbinder.unbind();
    }

    @Override
    public void onStart() {
        super.onStart();

        AlertDialog d = (AlertDialog) getDialog();
        if (d != null) {
            Button positiveButton = d.getButton(Dialog.BUTTON_POSITIVE);
            positiveButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    int range;
                    int modifier;
                    switch (mRadioGroupRange.getCheckedRadioButtonId()) {
                        case R.id.radioButtonDateRangeDay:
                            range = DATE_RANGE_DAY;
                            break;
                        case R.id.radioButtonDateRangeWeek:
                            range = DATE_RANGE_WEEK;
                            break;
                        case R.id.radioButtonDateRangeMonth:
                            range = DATE_RANGE_MONTH;
                            break;
                        case R.id.radioButtonDateRangeYear:
                            range = DATE_RANGE_YEAR;
                            break;
                        default:
                            range = DATE_RANGE_MONTH;
                    }
                    switch (mRadioGroupModifier.getCheckedRadioButtonId()) {
                        case R.id.radioButtonDateRangeCurrent:
                            modifier = DATE_RANGE_MODIFIER_CURRENT;
                            break;
                        case R.id.radioButtonDateRangeLast:
                            modifier = DATE_RANGE_MODIFIER_LAST;
                            break;
                        case R.id.radioButtonDateRangeCurrentAndLast:
                            modifier = DATE_RANGE_MODIFIER_CURRENTAND_LAST;
                            break;
                        default:
                            modifier = DATE_RANGE_MODIFIER_CURRENT;
                    }
                    mOnComplete.onComplete(range, modifier);
                    dismiss();
                }
            });
        }
    }

    public interface IOnComplete {
        void onComplete(int range, int modifier);
    }

}
