/*
 * Copyright (c) 2015.
 */

package com.yoshione.fingen;

import android.Manifest;
import android.app.Dialog;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.content.res.TypedArray;
import android.database.Cursor;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.StringRes;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import com.yoshione.fingen.dao.SendersDAO;
import com.yoshione.fingen.model.Sender;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import biz.kasual.materialnumberpicker.MaterialNumberPicker;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;
import permissions.dispatcher.NeedsPermission;
import permissions.dispatcher.OnNeverAskAgain;
import permissions.dispatcher.OnPermissionDenied;
import permissions.dispatcher.OnShowRationale;
import permissions.dispatcher.PermissionRequest;
import permissions.dispatcher.RuntimePermissions;

/**
 * Created by slv on 04.12.2015.
 * a
 */
@RuntimePermissions
public class FragmentSenderEdit extends DialogFragment {

    Unbinder unbinder;
    @BindView(R.id.editTextName)
    EditText mEditTextName;
    @BindView(R.id.editTextPhoneNo)
    EditText mEditTextPhoneNo;
    @BindView(R.id.editTextAmountPos)
    EditText mEditTextAmountPos;
    @BindView(R.id.editTextBalancePos)
    EditText mEditTextBalancePos;
    @BindView(R.id.imageButtonSelectSender)
    ImageButton mImageButtonSelectSender;
    @BindView(R.id.checkBoxAddCreditLimitToBalance)
    CheckBox mCheckBoxAddCreditLimitToBalance;
    private Sender mSender;

    public FragmentSenderEdit() {
    }

    public static FragmentSenderEdit newInstance(String title, Sender sender) {
        FragmentSenderEdit frag = new FragmentSenderEdit();
        Bundle args = new Bundle();
        args.putString("title", title);
        args.putParcelable("sender", sender);
        frag.setArguments(args);

        return frag;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        String title = getArguments().getString("title");
        mSender = getArguments().getParcelable("sender");
        View view = getActivity().getLayoutInflater().inflate(R.layout.fragment_sender_edit, null);
        unbinder = ButterKnife.bind(this, view);

        if (mSender != null) {
            mEditTextName.setText(mSender.getName());
            mEditTextPhoneNo.setText(mSender.getPhoneNo());
            mEditTextAmountPos.setText(String.valueOf(mSender.getAmountPos()+1));
            mEditTextBalancePos.setText(String.valueOf(mSender.getBalancePos()+1));
            mCheckBoxAddCreditLimitToBalance.setChecked(mSender.isAddCreditLimitToBalance());
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

    @Override
    public void onStart() {
        super.onStart();
        AlertDialog d = (AlertDialog) getDialog();
        if (d != null) {
            Button positiveButton = d.getButton(Dialog.BUTTON_POSITIVE);
            positiveButton.setOnClickListener(new OnOkListener());
        }
        Dialog dialog = getDialog();
        if (dialog != null) {
            int width = ViewGroup.LayoutParams.MATCH_PARENT;
            int height = ViewGroup.LayoutParams.WRAP_CONTENT;
            dialog.getWindow().setLayout(width, height);
        }
    }

    private int getThemeId() {
        try {
            Class<?> wrapper = Context.class;
            Method method = wrapper.getMethod("getThemeResId");
            method.setAccessible(true);
            return (Integer) method.invoke(getActivity());
        } catch (Exception e) {
            e.printStackTrace();
        }
        return 0;
    }

    @SuppressWarnings("ResourceType")
    @OnClick({R.id.editTextAmountPos, R.id.editTextBalancePos, R.id.imageButtonSelectSender, R.id.checkBoxAddCreditLimitToBalance})
    public void onClick(View view) {
        int[] attrs = {android.R.attr.background, android.R.attr.textColorPrimary};
        TypedArray ta = getActivity().obtainStyledAttributes(getThemeId(), attrs);
        int colorBgnd = ta.getColor(0, Color.TRANSPARENT);
        int colorText = ta.getColor(1, Color.BLACK);
        ta.recycle();
        final MaterialNumberPicker numberPicker;
        switch (view.getId()) {
            case R.id.editTextAmountPos:
                 numberPicker = new MaterialNumberPicker.Builder(getActivity())
                        .minValue(1)
                        .maxValue(10)
                        .defaultValue(mSender.getAmountPos()+1)
                        .backgroundColor(colorBgnd)
                        .separatorColor(Color.TRANSPARENT)
                        .textColor(colorText)
                        .textSize(20)
                        .enableFocusability(false)
                        .wrapSelectorWheel(true)
                        .build();
                new AlertDialog.Builder(getActivity())
                        .setTitle(getActivity().getString(R.string.ent_amount_pos))
                        .setView(numberPicker)
                        .setPositiveButton(getString(android.R.string.ok), new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                mSender.setAmountPos(numberPicker.getValue()-1);
                                mEditTextAmountPos.setText(String.valueOf(numberPicker.getValue()));
                            }
                        })
                        .show();
                break;
            case R.id.editTextBalancePos:
                numberPicker = new MaterialNumberPicker.Builder(getActivity())
                        .minValue(1)
                        .maxValue(10)
                        .defaultValue(mSender.getBalancePos()+1)
                        .backgroundColor(colorBgnd)
                        .separatorColor(Color.TRANSPARENT)
                        .textColor(colorText)
                        .textSize(20)
                        .enableFocusability(false)
                        .wrapSelectorWheel(true)
                        .build();
                new AlertDialog.Builder(getActivity())
                        .setTitle(getActivity().getString(R.string.ent_balance_pos))
                        .setView(numberPicker)
                        .setPositiveButton(getString(android.R.string.ok), new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                mSender.setBalancePos(numberPicker.getValue()-1);
                                mEditTextBalancePos.setText(String.valueOf(numberPicker.getValue()));
                            }
                        })
                        .show();
                break;
            case R.id.imageButtonSelectSender:
                FragmentSenderEditPermissionsDispatcher.getSystemSendersWithPermissionCheck(this);
                break;
            case R.id.checkBoxAddCreditLimitToBalance:
                mSender.setAddCreditLimitToBalance(mCheckBoxAddCreditLimitToBalance.isChecked());
                break;
        }
    }

    private class OnOkListener implements View.OnClickListener {

        @Override
        public void onClick(View v) {
            mSender.setName(mEditTextName.getText().toString());
            mSender.setPhoneNo(mEditTextPhoneNo.getText().toString());
            if (mSender != null && mSender.getName().length() > 0) {
                SendersDAO sendersDAO = SendersDAO.getInstance(getActivity());
                try {
                    try {
                        mSender = (Sender) sendersDAO.createModel(mSender);
                    } catch (Exception e) {
                        Toast.makeText(getActivity(), R.string.msg_error_on_write_to_db, Toast.LENGTH_SHORT).show();
                        return;
                    }
                    if (mSender.getID() >= 0) {
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

    @NeedsPermission(Manifest.permission.READ_SMS)
    public void getSystemSenders() {
        if (getActivity() == null) {
            return;
        }
        if (ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.READ_SMS) != PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(getActivity(), getActivity().getString(R.string.msg_permission_rw_external_storage_denied), Toast.LENGTH_SHORT).show();
            return;
        }
        Uri mSmsinboxQueryUri = Uri.parse("content://sms/inbox");
        ContentResolver resolver = getActivity().getContentResolver();
        String projection[] = new String[]{"DISTINCT address"};
        Cursor cursor = resolver.query(mSmsinboxQueryUri, projection, null, null, null);
        final List<String> senders = new ArrayList<>();
        if (cursor != null) {
            try {
                if (cursor.moveToFirst()) {
                    while (!cursor.isAfterLast()) {
                        senders.add(cursor.getString(0));
                        cursor.moveToNext();
                    }
                }
            } finally {
                cursor.close();
            }
        }

        AlertDialog.Builder builderSingle = new AlertDialog.Builder(getActivity());
        final ArrayAdapter<String> arrayAdapterSenders = new ArrayAdapter<>(getActivity(), android.R.layout.select_dialog_singlechoice);
        arrayAdapterSenders.addAll(senders);
        if (!senders.isEmpty()) {
            builderSingle.setSingleChoiceItems(arrayAdapterSenders, -1,
                    new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(final DialogInterface dialog, final int which) {
                            new Handler().postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    dialog.cancel();
                                    mEditTextPhoneNo.setText(senders.get(which));
                                }
                            }, 200);

                        }
                    });
            builderSingle.setTitle(getString(R.string.title_senders_phone_no));

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
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        // NOTE: delegate the permission handling to generated method
        FragmentSenderEditPermissionsDispatcher.onRequestPermissionsResult(this, requestCode, grantResults);
    }

    @OnShowRationale(android.Manifest.permission.READ_SMS)
    void showRationaleForReadSms(PermissionRequest request) {
        // NOTE: Show a rationale to explain why the permission is needed, e.g. with a dialog.
        // Call proceed() or cancel() on the provided PermissionRequest to continue or abort
        showRationaleDialog(R.string.msg_permission_read_sms_rationale, request);
    }

    @OnPermissionDenied(android.Manifest.permission.READ_SMS)
    void onReadSmsDenied() {
        // NOTE: Deal with a denied permission, e.g. by showing specific UI
        // or disabling certain functionality
        Toast.makeText(getActivity(), R.string.msg_permission_read_sms_denied, Toast.LENGTH_SHORT).show();
    }

    @OnNeverAskAgain(android.Manifest.permission.READ_SMS)
    void onReadSmsNeverAskAgain() {
        Toast.makeText(getActivity(), R.string.msg_permission_read_sms_never_askagain, Toast.LENGTH_SHORT).show();
    }

    private void showRationaleDialog(@StringRes int messageResId, final PermissionRequest request) {
        new AlertDialog.Builder(getActivity())
                .setPositiveButton(R.string.act_next, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        request.proceed();
                    }
                })
                .setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        request.cancel();
                    }
                })
                .setCancelable(false)
                .setMessage(messageResId)
                .show();
    }
}