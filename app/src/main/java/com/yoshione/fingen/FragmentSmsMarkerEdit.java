/*
 * Copyright (c) 2015. 
 */

package com.yoshione.fingen;

import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.design.widget.TextInputLayout;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import com.yoshione.fingen.dao.SmsMarkersDAO;
import com.yoshione.fingen.interfaces.IAbstractModel;
import com.yoshione.fingen.managers.SmsMarkerManager;
import com.yoshione.fingen.model.SmsMarker;
import com.yoshione.fingen.utils.RequestCodes;
import com.yoshione.fingen.utils.SmsParser;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;

import static android.app.Activity.RESULT_OK;

/**
 * Created by slv on 17.12.2015.
 * a
 */
public class FragmentSmsMarkerEdit extends DialogFragment {
    @BindView(R.id.editTextObject)
    EditText editTextObject;
    @BindView(R.id.editTextValue)
    EditText editTextValue;
    @BindView(R.id.textViewObject)
    EditText textViewObject;
    @BindView(R.id.editTextType)
    EditText editTextType;
    @BindView(R.id.textInputLayoutEditTextObject)
    TextInputLayout mTextInputLayoutEditTextObject;
    @BindView(R.id.textInputLayoutTextViewObject)
    TextInputLayout mTextInputLayoutTextViewObject;

    private IDialogDismissListener dialogDismissListener;
    IAbstractModel mObject;

    Unbinder unbinder;

    public void setDialogDismissListener(IDialogDismissListener dialogDismissListener) {
        this.dialogDismissListener = dialogDismissListener;
    }

    public FragmentSmsMarkerEdit() {
    }

    public static FragmentSmsMarkerEdit newInstance(String title, SmsMarker smsMarker) {
        FragmentSmsMarkerEdit frag = new FragmentSmsMarkerEdit();
        Bundle args = new Bundle();
        args.putString("title", title);
        args.putParcelable("smsMarker", smsMarker);
        frag.setArguments(args);

        return frag;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        String title = getArguments().getString("title");
        SmsMarker smsMarker = getArguments().getParcelable("smsMarker");
        View view = getActivity().getLayoutInflater().inflate(R.layout.fragment_smsmarker_edit, null);
        unbinder = ButterKnife.bind(this, view);

        if (smsMarker != null) {
            editTextType.setText(SmsMarkerManager.getMarkerTypeName(smsMarker.getType(), getActivity()));
            initObject(smsMarker);
            editTextValue.setText(smsMarker.getMarker());
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
                if (dialogDismissListener != null) {
                    dialogDismissListener.OnDialogDismiss(false);
                }
                dialog.dismiss();
            }
        });


        return alertDialogBuilder.create();
    }

    @SuppressWarnings("unchecked")
    private void initObject(final SmsMarker smsMarker) {
        String text;
        switch (smsMarker.getType()) {
            case SmsParser.MARKER_TYPE_ACCOUNT:
            case SmsParser.MARKER_TYPE_DESTACCOUNT:
                mObject = SmsMarkerManager.getObject(smsMarker, getActivity());
                if (mObject != null) {
                    textViewObject.setText(mObject.toString());
                    textViewObject.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            Intent intent = new Intent(getActivity(), ActivityAccounts.class);
                            intent.putExtra("showHomeButton", false);
                            intent.putExtra("model", mObject);
                            startActivityForResult(intent, RequestCodes.REQUEST_CODE_SELECT_MODEL);
                        }
                    });
                }
                break;
            case SmsParser.MARKER_TYPE_CABBAGE:
                mObject = SmsMarkerManager.getObject(smsMarker, getActivity());
                if (mObject != null) {
                    textViewObject.setText(mObject.toString());
                    textViewObject.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            Intent intent = new Intent(getActivity(), ActivityList.class);
                            intent.putExtra("showHomeButton", false);
                            intent.putExtra("model", mObject);
                            intent.putExtra("requestCode", RequestCodes.REQUEST_CODE_SELECT_MODEL);
                            startActivityForResult(intent, RequestCodes.REQUEST_CODE_SELECT_MODEL);
                        }
                    });
                }
                break;
            case SmsParser.MARKER_TYPE_PAYEE:
                mObject = SmsMarkerManager.getObject(smsMarker, getActivity());
                if (mObject != null) {
                    textViewObject.setText(mObject.toString());
                    textViewObject.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            Intent intent = new Intent(getActivity(), ActivityList.class);
                            intent.putExtra("showHomeButton", false);
                            intent.putExtra("model", mObject);
                            intent.putExtra("requestCode", RequestCodes.REQUEST_CODE_SELECT_MODEL);
                            startActivityForResult(intent, RequestCodes.REQUEST_CODE_SELECT_MODEL);
                        }
                    });
                }
                break;
            case SmsParser.MARKER_TYPE_TRTYPE:
                text = SmsMarkerManager.getObjectAsText(smsMarker, getActivity());
                textViewObject.setText(text);
                textViewObject.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        AlertDialog.Builder builderSingle = new AlertDialog.Builder(getActivity());
                        List<SmsMarkerManager.TrType> types = (List<SmsMarkerManager.TrType>) SmsMarkerManager.getAllObjects(smsMarker, getActivity());
                        final ArrayAdapter<SmsMarkerManager.TrType> arrayAdapterTypes = new ArrayAdapter<>(getActivity(), android.R.layout.select_dialog_singlechoice);

                        arrayAdapterTypes.addAll((types));

                        builderSingle.setSingleChoiceItems(arrayAdapterTypes, -1,
                                new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(final DialogInterface dialog, final int which) {
                                        new Handler().postDelayed(new Runnable() {
                                            @Override
                                            public void run() {
                                                dialog.cancel();
                                                ListView lw = ((AlertDialog) dialog).getListView();
                                                SmsMarkerManager.TrType type = (SmsMarkerManager.TrType) lw.getAdapter().getItem(which);
                                                textViewObject.setText(type.toString());
                                            }
                                        }, 200);

                                    }
                                });
                        builderSingle.setTitle(getString(R.string.ent_type));

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
                break;
            case SmsParser.MARKER_TYPE_IGNORE:
                text = SmsMarkerManager.getObjectAsText(smsMarker, getActivity());
                editTextObject.setText(text);
                break;
        }

        if (smsMarker.getType() == SmsParser.MARKER_TYPE_IGNORE) {
            mTextInputLayoutTextViewObject.setVisibility(View.GONE);
            mTextInputLayoutEditTextObject.setVisibility(View.GONE);
        } else {
            mTextInputLayoutTextViewObject.setVisibility(View.VISIBLE);
            mTextInputLayoutEditTextObject.setVisibility(View.GONE);
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK && requestCode == RequestCodes.REQUEST_CODE_SELECT_MODEL && data != null) {
            IAbstractModel model = data.getParcelableExtra("model");
            switch (model.getModelType()) {
                case IAbstractModel.MODEL_TYPE_ACCOUNT:
                case IAbstractModel.MODEL_TYPE_CABBAGE:
                case IAbstractModel.MODEL_TYPE_PAYEE:
                    mObject = data.getParcelableExtra("model");
                    textViewObject.setText(mObject.toString());
                    break;
            }
        }
    }

    private boolean isInputValid() {
        return !editTextType.getText().toString().isEmpty() &
                (
                        (!editTextObject.getText().toString().isEmpty() & (mTextInputLayoutEditTextObject.getVisibility() == View.VISIBLE)) |
                                (!textViewObject.getText().toString().isEmpty() & (mTextInputLayoutTextViewObject.getVisibility() == View.VISIBLE)) |
                                ((mTextInputLayoutEditTextObject.getVisibility() == View.GONE) & (mTextInputLayoutTextViewObject.getVisibility() == View.GONE))
                ) &
                !editTextValue.getText().toString().isEmpty();
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

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        unbinder.unbind();
    }

    private class OnOkListener implements View.OnClickListener {

        @Override
        public void onClick(View v) {
            if (isInputValid()) {
                SmsMarker smsMarker = getArguments().getParcelable("smsMarker");
                if (smsMarker != null) {
                    switch (smsMarker.getType()) {
                        case SmsParser.MARKER_TYPE_ACCOUNT:
                        case SmsParser.MARKER_TYPE_DESTACCOUNT:
                        case SmsParser.MARKER_TYPE_CABBAGE:
                        case SmsParser.MARKER_TYPE_PAYEE:
                            SmsMarkerManager.setObjctFromText(smsMarker, mObject.toString(), getActivity());
                            break;
                        case SmsParser.MARKER_TYPE_TRTYPE:
                            SmsMarkerManager.setObjctFromText(smsMarker, textViewObject.getText().toString(), getActivity());
                            break;
                        case SmsParser.MARKER_TYPE_IGNORE:
                            SmsMarkerManager.setObjctFromText(smsMarker, editTextObject.getText().toString(), getActivity());
                            break;
                    }
                    smsMarker.setMarker(editTextValue.getText().toString());
                    try {
                        SmsMarkersDAO.getInstance(getActivity()).createModel(smsMarker);
                    } catch (Exception e) {
                        Toast.makeText(getActivity(), R.string.msg_error_on_write_to_db, Toast.LENGTH_SHORT).show();
                        return;
                    }
                    if (dialogDismissListener != null) {
                        dialogDismissListener.OnDialogDismiss(true);
                    }
                    dismiss();
                }
            }
        }
    }

    public interface IDialogDismissListener {
        void OnDialogDismiss(boolean result);
    }
}
