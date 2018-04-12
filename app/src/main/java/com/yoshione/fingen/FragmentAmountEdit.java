package com.yoshione.fingen;

import android.app.Activity;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.view.View;
import android.widget.Button;

import com.yoshione.fingen.model.Cabbage;
import com.yoshione.fingen.model.Transaction;
import com.yoshione.fingen.widgets.AmountEditor;

import java.math.BigDecimal;

/**
 * Created by Leonid on 08.01.2017.
 * Dialog fragment with amount editor. Use when add or edit category in budget.
 */

public class FragmentAmountEdit extends DialogFragment {

    FragmentBudget.OnAmountEditDialogDeleteListener mOnAmountEditDialogDeleteListener;
    FragmentBudget.IOnAmountEditComplete mOnComplete;
    AmountEditor mAmountEditor;

    public void setOnAmountEditDialogDeleteListener(FragmentBudget.OnAmountEditDialogDeleteListener onAmountEditDialogDeleteListener) {
        mOnAmountEditDialogDeleteListener = onAmountEditDialogDeleteListener;
    }

    public void setOnComplete(FragmentBudget.IOnAmountEditComplete onComplete) {
        mOnComplete = onComplete;
    }

    public static FragmentAmountEdit newInstance(String title, BigDecimal amount, Cabbage cabbage, long categoryID) {
        FragmentAmountEdit frag = new FragmentAmountEdit();
        Bundle args = new Bundle();
        args.putString("title", title);
        args.putString("amount", amount.toString());
        args.putParcelable("cabbage", cabbage);
        args.putLong("categoryID", categoryID);
        frag.setArguments(args);

        return frag;
    }

    public FragmentAmountEdit() {
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        String title = getArguments().getString("title");
        BigDecimal amount = new BigDecimal(getArguments().getString("amount"));
        Cabbage cabbage = getArguments().getParcelable("cabbage");
        if (cabbage == null) cabbage = new Cabbage();
        long categoryID = getArguments().getLong("categoryID");
        mAmountEditor = new AmountEditor(getActivity(), new AmountEditor.OnAmountChangeListener() {
            @Override
            public void OnAmountChange(BigDecimal newAmount, int newType) {

            }
        }, (amount.compareTo(BigDecimal.ZERO) <= 0) ? Transaction.TRANSACTION_TYPE_EXPENSE : Transaction.TRANSACTION_TYPE_INCOME,
                cabbage.getDecimalCount(), getActivity());
        mAmountEditor.setAmount(amount);
        mAmountEditor.setmAllowChangeCabbage(categoryID >= 0);
        mAmountEditor.setCabbage(cabbage);
        mAmountEditor.setId(R.id.amount_editor);
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(getActivity());
        alertDialogBuilder.setView(mAmountEditor);
        alertDialogBuilder.setTitle(title);
        alertDialogBuilder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

            }
        });
        alertDialogBuilder.setNeutralButton(getActivity().getString(R.string.act_delete), mOnAmountEditDialogDeleteListener);
        alertDialogBuilder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });

        return alertDialogBuilder.create();
    }

    @Override
    public void onStart() {
        super.onStart();
        mAmountEditor.requestFocus();
        AlertDialog d = (AlertDialog) getDialog();
        if (d != null) {
            Button positiveButton = d.getButton(Dialog.BUTTON_POSITIVE);
            positiveButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (mAmountEditor.getCabbage().getID() < 0) {
                        //show error
                        mAmountEditor.showCabbageError();
                    } else {
                        mOnComplete.onComplete(mAmountEditor.getAmount().multiply(new BigDecimal((mAmountEditor.getType() > 0) ? 1 : -1)), mAmountEditor.getCabbage());
                        dismiss();
                    }
                }
            });
        }
    }
}
