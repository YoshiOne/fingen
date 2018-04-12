package com.yoshione.fingen;


import android.app.Dialog;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.preference.PreferenceManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;

import com.yoshione.fingen.adapter.AdapterTabOrder;
import com.yoshione.fingen.adapter.helper.OnStartDragListener;
import com.yoshione.fingen.adapter.helper.SimpleItemTouchHelperCallback;
import com.yoshione.fingen.utils.IconGenerator;
import com.yoshione.fingen.widgets.ContextMenuRecyclerView;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;

/**
 * Created by Leonid on 23.11.2016.
 * Диалог для редактирования порядка отображения вкладок в главном окне
 */

public class FragmentTabOrderDialog extends DialogFragment implements OnStartDragListener {


    @BindView(R.id.recycler_view)
    ContextMenuRecyclerView mRecyclerView;
    Unbinder unbinder;
    AdapterTabOrder adapter;
    private ItemTouchHelper mItemTouchHelper;

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        View view = getActivity().getLayoutInflater().inflate(R.layout.fragment_tab_order_dialog, null);
        unbinder = ButterKnife.bind(this, view);

        adapter = new AdapterTabOrder(this,
                getActivity().getDrawable(R.drawable.ic_drag));
        adapter.setHasStableIds(true);

        mRecyclerView.setAdapter(adapter);

        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false);
        mRecyclerView.setLayoutManager(mLayoutManager);

        ItemTouchHelper.Callback callback = new SimpleItemTouchHelperCallback(adapter);
        mItemTouchHelper = new ItemTouchHelper(callback);
        mItemTouchHelper.attachToRecyclerView(mRecyclerView);

        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(getActivity());
        alertDialogBuilder.setView(view);
        alertDialogBuilder.setTitle(getActivity().getString(R.string.pref_tab_order));
        alertDialogBuilder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String order = "";
                for (Pair<String, String> pair : adapter.getList()) {
                    order = String.format("%s%s;", order, pair.first);
                }
                SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getActivity());
                preferences.edit().putString(FgConst.PREF_TAB_ORDER, order).apply();
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
    public void onResume() {
        super.onResume();

        loadData();

    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = super.onCreateView(inflater, container, savedInstanceState);

        Window window = getDialog().getWindow();

        if (window != null) {
//            window.setGravity(Gravity.BOTTOM);
        }

        return view;
    }

    @Override
    public void onStartDrag(RecyclerView.ViewHolder viewHolder) {
        mItemTouchHelper.startDrag(viewHolder);
    }

    private void loadData() {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getActivity());
        String order = preferences.getString(FgConst.PREF_TAB_ORDER, "");
        String items[] = order.split(";");
        List<Pair<String, String>> list = new ArrayList<>();

        String name;
        try {
            for (int i = 0; i < 3; i++) {
                if (items[i].equals(FgConst.FRAGMENT_ACCOUNTS)) {
                    name = getString(R.string.ent_accounts);
                } else if (items[i].equals(FgConst.FRAGMENT_TRANSACTIONS)) {
                    name = getString(R.string.ent_transactions);
                } else if (items[i].equals(FgConst.FRAGMENT_SUMMARY)) {
                    name = getString(R.string.ent_summary);
                } else {
                    throw new Exception();
                }
                list.add(new Pair<>(items[i], name));
            }
        } catch (Exception e) {
            list.add(new Pair<>(FgConst.FRAGMENT_SUMMARY, getString(R.string.ent_summary)));
            list.add(new Pair<>(FgConst.FRAGMENT_ACCOUNTS, getString(R.string.ent_accounts)));
            list.add(new Pair<>(FgConst.FRAGMENT_TRANSACTIONS, getString(R.string.ent_transactions)));
        }
        adapter.setList(list);
        adapter.notifyDataSetChanged();
    }
}
