package com.yoshione.fingen;

import android.app.Dialog;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;
import androidx.preference.PreferenceManager;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.yoshione.fingen.adapter.AdapterTrEditConstructor;
import com.yoshione.fingen.adapter.helper.OnStartDragListener;
import com.yoshione.fingen.adapter.helper.SimpleItemTouchHelperCallback;
import com.yoshione.fingen.model.TrEditItem;
import com.yoshione.fingen.utils.PrefUtils;
import com.yoshione.fingen.widgets.ContextMenuRecyclerView;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;

/**
 * Created by Leonid on 23.11.2016.
 * Диалог для редактирования порядка отображения вкладок в главном окне
 */

public class FragmentTrEditConstructorDialog extends DialogFragment implements OnStartDragListener {


    @BindView(R.id.recycler_view)
    ContextMenuRecyclerView mRecyclerView;
    Unbinder unbinder;
    AdapterTrEditConstructor adapter;
    private ItemTouchHelper mItemTouchHelper;

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        View view = getActivity().getLayoutInflater().inflate(R.layout.fragment_tab_order_dialog, null);
        unbinder = ButterKnife.bind(this, view);

        adapter = new AdapterTrEditConstructor(this);
        adapter.setHasStableIds(true);

        mRecyclerView.setAdapter(adapter);

        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false);
        mRecyclerView.setLayoutManager(mLayoutManager);

        ItemTouchHelper.Callback callback = new SimpleItemTouchHelperCallback(adapter);
        mItemTouchHelper = new ItemTouchHelper(callback);
        mItemTouchHelper.attachToRecyclerView(mRecyclerView);

        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(getActivity());
        alertDialogBuilder.setView(view);
        alertDialogBuilder.setTitle(getActivity().getString(R.string.pref_title_tredit_layout_constructor));
        alertDialogBuilder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String order = "";
                for (TrEditItem item : adapter.getList()) {
                    order = String.format("%s%s&%b&%b;", order, item.getID(), item.isVisible(), item.isHideUnderMore());
                }
                SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getActivity());
                preferences.edit().putString(FgConst.PREF_TRANSACTION_EDITOR_CONSTRUCTOR, order).apply();
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
        adapter.setList(PrefUtils.getTrEditorLayout(preferences, getActivity()));
        adapter.notifyDataSetChanged();
    }
}
