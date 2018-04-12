/*
 * Copyright (c) 2015.
 */

package com.yoshione.fingen;

import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.util.Log;

import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Toast;

import com.yoshione.fingen.dao.CabbagesDAO;
import com.yoshione.fingen.dao.SimpleDebtsDAO;
import com.yoshione.fingen.model.Cabbage;
import com.yoshione.fingen.model.SimpleDebt;
import com.yoshione.fingen.model.Transaction;
import com.yoshione.fingen.widgets.AmountEditor;

import java.math.BigDecimal;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;

/**
 * Created by slv on 04.12.2015.
 *
 */
public class FragmentSimpleDebtEdit extends DialogFragment {

    @BindView(R.id.edit_text_name)
    EditText editTextName;
    Unbinder unbinder;
    @BindView(R.id.checkBoxClosed)
    CheckBox mCheckBoxClosed;
    @BindView(R.id.amountEditorStartAmount)
    AmountEditor amountEditorStartAmount;
    private SimpleDebt mSimpleDebt;

    public FragmentSimpleDebtEdit() {
    }

    public static FragmentSimpleDebtEdit newInstance(String title, SimpleDebt simpleDebt) {
        FragmentSimpleDebtEdit frag = new FragmentSimpleDebtEdit();
        Bundle args = new Bundle();
        args.putString("title", title);
        args.putParcelable("simpleDebt", simpleDebt);
        frag.setArguments(args);

        return frag;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        String title = getArguments().getString("title");
        mSimpleDebt = getArguments().getParcelable("simpleDebt");
        View view = getActivity().getLayoutInflater().inflate(R.layout.fragment_simpledebt_edit, null);
        unbinder = ButterKnife.bind(this, view);

        if (mSimpleDebt != null) {
            editTextName.setText(mSimpleDebt.getName());
            mCheckBoxClosed.setChecked(!mSimpleDebt.isActive());
        }

        amountEditorStartAmount.setActivity(getActivity());
        amountEditorStartAmount.setAmount(mSimpleDebt.getStartAmount());
        amountEditorStartAmount.setType((mSimpleDebt.getStartAmount().compareTo(BigDecimal.ZERO) >= 0) ? Transaction.TRANSACTION_TYPE_INCOME : Transaction.TRANSACTION_TYPE_EXPENSE);
        amountEditorStartAmount.setHint(getResources().getString(R.string.ent_debt_start_amount));
        amountEditorStartAmount.mOnAmountChangeListener = new AmountEditor.OnAmountChangeListener() {
            @Override
            public void OnAmountChange(BigDecimal newAmount, int newType) {
                mSimpleDebt.setStartAmount(newAmount.multiply(new BigDecimal((newType > 0) ? 1 : -1)));
            }
        };
        amountEditorStartAmount.setOnCabbageChangeListener(new AmountEditor.OnCabbageChangeListener() {
            @Override
            public void OnCabbageChange(Cabbage cabbage) {
                mSimpleDebt.setCabbageID(cabbage.getID());
            }
        });
        amountEditorStartAmount.setCabbage(CabbagesDAO.getInstance(getActivity()).getCabbageByID(mSimpleDebt.getCabbageID()));

        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(getActivity());
        alertDialogBuilder.setView(view);
        alertDialogBuilder.setTitle(title);
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
    public void onStart() {
        super.onStart();
        AlertDialog d = (AlertDialog) getDialog();
        if (d != null) {
            Button positiveButton = d.getButton(Dialog.BUTTON_POSITIVE);
            positiveButton.setOnClickListener(new OnOkListener());
        }
        Dialog dialog = getDialog();
        if (dialog != null)
        {
            int width = ViewGroup.LayoutParams.MATCH_PARENT;
            int height = ViewGroup.LayoutParams.WRAP_CONTENT;
            dialog.getWindow().setLayout(width, height);
        }
    }

    private class OnOkListener implements View.OnClickListener {

        @Override
        public void onClick(View v) {
            mSimpleDebt.setName(editTextName.getText().toString());
            mSimpleDebt.setIsActive(!mCheckBoxClosed.isChecked());
            if (mSimpleDebt.getCabbageID() < 0) {
                amountEditorStartAmount.showCabbageError();
                return;
            }
            if (mSimpleDebt != null && mSimpleDebt.getName().length() > 0) {
                SimpleDebtsDAO simpleDebtsDAO = SimpleDebtsDAO.getInstance(getActivity());
                try {
                    try {
                        mSimpleDebt = (SimpleDebt) simpleDebtsDAO.createModel(mSimpleDebt);
                    } catch (Exception e) {
                        Toast.makeText(getActivity(), R.string.msg_error_on_write_to_db, Toast.LENGTH_SHORT).show();
                        return;
                    }
                    if (mSimpleDebt.getID() >= 0) {
                        dismiss();
                    }
                } catch (Exception e) {
                    Log.d(this.getClass().getName(), "Error create simple debt");
                }
            }
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        unbinder.unbind();
    }
}