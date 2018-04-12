package com.yoshione.fingen;

import android.app.Dialog;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;

import com.yoshione.fingen.model.Events;

import butterknife.BindView;
import butterknife.ButterKnife;
import org.greenrobot.eventbus.EventBus;

/**
 * Created by slv on 24.02.2016.
 *
 */
public class FragmentSortAccounts extends DialogFragment {

    private static final String TAG = "FragmentSortAccounts";

    @BindView(R.id.spinnerSortType)
    EditText spinnerSortType;
    @BindView(R.id.spinnerSortOrder)
    EditText spinnerSortOrder;

    private SharedPreferences preferences;
    private int sortType;
    private int sortOrder;

    public FragmentSortAccounts() {
    }

    public static FragmentSortAccounts newInstance(String title) {
        FragmentSortAccounts frag = new FragmentSortAccounts();
        Bundle args = new Bundle();
        args.putString("title", title);
        frag.setArguments(args);

        return frag;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        preferences = PreferenceManager.getDefaultSharedPreferences(getActivity());
        sortType = preferences.getInt("accounts_sort_type", 0);
        sortOrder = preferences.getInt("accounts_sort_order", 0);
        String title = getArguments().getString("title");
        View view = getActivity().getLayoutInflater().inflate(R.layout.fragment_sort_accounts, null);
        ButterKnife.bind(this, view);

        final String[] sortTypes = getActivity().getResources().getStringArray(R.array.acc_sort_types);
        spinnerSortType.setText(sortTypes[sortType]);
        spinnerSortType.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AlertDialog.Builder builderSingle = new AlertDialog.Builder(getActivity());
                final ArrayAdapter<String> arrayAdapterTypes = new ArrayAdapter<>(getActivity(), android.R.layout.select_dialog_singlechoice);
                arrayAdapterTypes.addAll(sortTypes);

                builderSingle.setSingleChoiceItems(arrayAdapterTypes, sortType,
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(final DialogInterface dialog, final int which) {
                                new Handler().postDelayed(new Runnable() {
                                    @Override
                                    public void run() {
                                        dialog.cancel();
                                        sortType = which;
                                        spinnerSortType.setText(sortTypes[sortType]);
                                    }
                                }, 200);

                            }
                        });
                builderSingle.setTitle(getString(R.string.ttl_accounts_sort_type));

                builderSingle.setNegativeButton(getResources().getString(android.R.string.cancel),
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                            }
                        });
                builderSingle.show();
            }
        });

        final String[] sortOrders = getActivity().getResources().getStringArray(R.array.acc_sort_orders);
        spinnerSortOrder.setText(sortOrders[sortOrder]);
        spinnerSortOrder.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AlertDialog.Builder builderSingle = new AlertDialog.Builder(getActivity());
                final ArrayAdapter<String> arrayAdapterOrders = new ArrayAdapter<>(getActivity(), android.R.layout.select_dialog_singlechoice);
                arrayAdapterOrders.addAll(sortOrders);

                builderSingle.setSingleChoiceItems(arrayAdapterOrders, sortOrder,
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(final DialogInterface dialog, final int which) {
                                new Handler().postDelayed(new Runnable() {
                                    @Override
                                    public void run() {
                                        dialog.cancel();
                                        sortOrder = which;
                                        spinnerSortOrder.setText(sortOrders[sortOrder]);
                                    }
                                }, 200);

                            }
                        });
                builderSingle.setTitle(getString(R.string.ttl_accounts_sort_order));

                builderSingle.setNegativeButton(getResources().getString(android.R.string.cancel),
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                            }
                        });
                builderSingle.show();
            }
        });

        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(getActivity());
        alertDialogBuilder.setView(view);
        alertDialogBuilder.setTitle(title);
        alertDialogBuilder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                preferences
                        .edit()
                        .putInt("accounts_sort_type", sortType)
                        .putInt("accounts_sort_order", sortOrder)
                        .apply();
                EventBus.getDefault().postSticky(new Events.EventOnSortAccounts());
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
}
