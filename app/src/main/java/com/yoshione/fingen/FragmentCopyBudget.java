package com.yoshione.fingen;

import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.AppCompatCheckBox;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;

import com.yoshione.fingen.utils.LocaleUtils;

import java.text.SimpleDateFormat;
import java.util.Calendar;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;

/**
 * Created by slv on 02.04.2016.
 *
 */
public class FragmentCopyBudget extends DialogFragment {

    private static final String TAG = "FragmentCopyBudget";

    @BindView(R.id.editTextYear)
    EditText editTextYear;
    @BindView(R.id.textViewMonth)
    EditText textViewMonth;
    @BindView(R.id.checkboxReplaceExists)
    AppCompatCheckBox checkboxReplaceExists;

    private int mYear;
    private int mMonth;

    Unbinder unbinder;

    void setCopyBudgetDialogListener(ICopyBudgetDialogListener copyBudgetDialogListener) {
        this.copyBudgetDialogListener = copyBudgetDialogListener;
    }

    private ICopyBudgetDialogListener copyBudgetDialogListener;

    public FragmentCopyBudget() {
    }

    public static FragmentCopyBudget newInstance(String title, int year, int month, boolean showCheckBox) {
        FragmentCopyBudget frag = new FragmentCopyBudget();
        Bundle args = new Bundle();
        args.putString("title", title);
        args.putInt("year", year);
        args.putInt("month", month);
        args.putBoolean("showCheckBox", showCheckBox);
        frag.setArguments(args);

        return frag;
    }

    private String[] getMonthList() {
        String monthList[] = new String[12];
        SimpleDateFormat dateFormat = new SimpleDateFormat("LLLL", LocaleUtils.getLocale(getActivity()));
        Calendar c = Calendar.getInstance();
        for (int i = 0; i < 12; i++) {
            c.set(Calendar.MONTH, i);
            monthList[i] = dateFormat.format(c.getTime());
        }
        return monthList;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        String title = getArguments().getString("title");
        mYear = getArguments().getInt("year");
        mMonth = getArguments().getInt("month");
        boolean showCheckBox = getArguments().getBoolean("showCheckBox");
        View view = getActivity().getLayoutInflater().inflate(R.layout.fragment_copy_budget, null);
        unbinder = ButterKnife.bind(this, view);

        final String monthList[] = getMonthList();

        editTextYear.setText(String.valueOf(mYear));
        textViewMonth.setText(monthList[mMonth]);
        textViewMonth.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AlertDialog.Builder builderSingle = new AlertDialog.Builder(getActivity());
                final ArrayAdapter<String> arrayAdapterTypes = new ArrayAdapter<>(getActivity(), android.R.layout.select_dialog_singlechoice);
                arrayAdapterTypes.addAll(monthList);

                builderSingle.setSingleChoiceItems(arrayAdapterTypes, mMonth,
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(final DialogInterface dialog, final int which) {
                                new Handler().postDelayed(new Runnable() {
                                    @Override
                                    public void run() {
                                        dialog.cancel();
                                        mMonth = which;
                                        textViewMonth.setText(monthList[mMonth]);
                                    }
                                }, 200);

                            }
                        });
                builderSingle.setTitle(getString(R.string.ent_date_range_month));

                builderSingle.setNegativeButton(
                        getResources().getString(android.R.string.cancel),
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                            }
                        });
                builderSingle.show();
            }
        });

        if (!showCheckBox) {
            checkboxReplaceExists.setChecked(true);
            checkboxReplaceExists.setVisibility(View.GONE);
        }

        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(getActivity());
        alertDialogBuilder.setView(view);
        alertDialogBuilder.setTitle(title);
        alertDialogBuilder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (copyBudgetDialogListener != null) {
                    int year = mYear;
                    try {
                        mYear = Integer.valueOf(editTextYear.getText().toString());
                    } catch (NumberFormatException e) {
                        mYear = year;
                    }

                    copyBudgetDialogListener.onOkClick(mYear, mMonth, checkboxReplaceExists.isChecked());
                }
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

    interface ICopyBudgetDialogListener {
        void onOkClick(int srcYear, int srcMonth, boolean replace);
    }

}
