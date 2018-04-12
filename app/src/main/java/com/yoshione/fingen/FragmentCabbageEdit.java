/*
 * Copyright (c) 2015.
 */

package com.yoshione.fingen;

import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.yoshione.fingen.dao.CabbagesDAO;
import com.yoshione.fingen.managers.CabbageManager;
import com.yoshione.fingen.model.Cabbage;

import java.util.Currency;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;

/**
 * Created by slv on 04.12.2015.
 *
 */
public class FragmentCabbageEdit extends DialogFragment {

    @BindView(R.id.button_system_currencies)
    Button buttonSystemCurrencies;
    @BindView(R.id.edit_text_name)
    EditText editTextName;
    @BindView(R.id.edit_text_code)
    EditText editTextCode;
    @BindView(R.id.edit_text_symbol)
    EditText editTextSymbol;
    @BindView(R.id.edit_text_scale)
    EditText editTextScale;
    Unbinder unbinder;

    public FragmentCabbageEdit() {
    }

    public static FragmentCabbageEdit newInstance(String title, Cabbage cabbage) {
        FragmentCabbageEdit frag = new FragmentCabbageEdit();
        Bundle args = new Bundle();
        args.putString("title", title);
        args.putParcelable("cabbage", cabbage);
        frag.setArguments(args);

        return frag;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        String title = getArguments().getString("title");
        Cabbage cabbage = getArguments().getParcelable("cabbage");
        View view = getActivity().getLayoutInflater().inflate(R.layout.fragment_cabbage_edit, null);
        unbinder = ButterKnife.bind(this, view);

        if (cabbage != null) {
            editTextName.setText(cabbage.getName());
            editTextCode.setText(cabbage.getCode());
            editTextSymbol.setText(cabbage.getSimbol());
            editTextScale.setText(String.valueOf(cabbage.getDecimalCount()));
        }

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

    private boolean isInputValid() {
        return !editTextName.getText().toString().isEmpty() &
                !editTextCode.getText().toString().isEmpty() &
                !editTextSymbol.getText().toString().isEmpty() &
                !editTextScale.getText().toString().isEmpty();
    }

    @Override
    public void onStart() {
        super.onStart();
        AlertDialog d = (AlertDialog) getDialog();
        if (d != null) {
            Button positiveButton = d.getButton(Dialog.BUTTON_POSITIVE);
            positiveButton.setOnClickListener(new OnOkListener());
        }
    }

    private class OnOkListener implements View.OnClickListener {

        @Override
        public void onClick(View v) {
            if (isInputValid()) {
                Cabbage cabbage = getArguments().getParcelable("cabbage");
                if (cabbage != null) {
                    cabbage.setName(editTextName.getText().toString());
                    cabbage.setCode(editTextCode.getText().toString());
                    cabbage.setSimbol(editTextSymbol.getText().toString());
                    try {
                        cabbage.setDecimalCount(Integer.valueOf(editTextScale.getText().toString()));
                    } catch (Exception e) {
                        cabbage.setDecimalCount(2);
                    }
                    CabbagesDAO cabbagesDAO = CabbagesDAO.getInstance(getActivity());
                    try {
                        cabbagesDAO.createModel(cabbage);
                    } catch (Exception e) {
                        Toast.makeText(getActivity(), R.string.msg_error_on_write_to_db, Toast.LENGTH_SHORT).show();
                        return;
                    }
                }
                dismiss();
            }
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        unbinder.unbind();
    }

    @OnClick(R.id.button_system_currencies)
    void selectSystemCurrencyClick() {
        CabbageManager.getInstance().showSelectSystemCurrencyDialog(getActivity(), new CabbageManager.OnSelectCurrencyListener() {
            @Override
            public void OnSelectCurrency(Currency selectedCurrency) {
                editTextName.setText(selectedCurrency.getDisplayName());
                editTextCode.setText(selectedCurrency.getCurrencyCode());
                editTextSymbol.setText(selectedCurrency.getSymbol());
            }
        });
    }
}