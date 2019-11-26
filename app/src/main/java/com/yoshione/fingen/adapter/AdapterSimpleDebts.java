package com.yoshione.fingen.adapter;

import android.annotation.SuppressLint;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.yoshione.fingen.R;
import com.yoshione.fingen.adapter.viewholders.VhModelBase;
import com.yoshione.fingen.adapter.viewholders.VhSimpleDebt;
import com.yoshione.fingen.interfaces.IAbstractModel;
import com.yoshione.fingen.interfaces.IBaseModelEventListener;
import com.yoshione.fingen.model.SimpleDebt;
import com.yoshione.fingen.widgets.ToolbarActivity;

import java.util.ArrayList;
import java.util.List;

public class AdapterSimpleDebts extends RecyclerView.Adapter implements VhSimpleDebt.IOnItemSelectedClickListener {

    private final ArrayList<SimpleDebt> debtList;
    private IBaseModelEventListener mItemEventListener;
    private ToolbarActivity mActivity;

    private String mSearchString;

    public void setSearchString(String searchString) {
        mSearchString = searchString;
    }

    //Конструктор
    @SuppressLint("UseSparseArrays")
    public AdapterSimpleDebts(IBaseModelEventListener itemEventListener, ToolbarActivity activity) {
        mActivity = activity;

        setHasStableIds(true);

        mItemEventListener = itemEventListener;
        debtList = new ArrayList<>();
    }

    public void clearDebtList() {
        debtList.clear();
    }

    public void addDebts(List<SimpleDebt> input, boolean clearLists) {
        if (clearLists) {
            debtList.clear();
        }

        if (input.size() == 0) {
            return;
        }

        debtList.addAll(input);
    }

    @Override
    public long getItemId(int position) {
        return debtList.get(position).getID();
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item_simple_dept, parent, false);
        return new VhSimpleDebt(view, this, mActivity);
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder viewHolder, int position) {
        ((VhModelBase) viewHolder).bindViewHolder(debtList, position);
    }

    @Override
    public int getItemCount() {
        return debtList.size();
    }

    public synchronized int getSelectedCount() {
        int count = 0;
        for (SimpleDebt debt : debtList) {
            if (debt.isSelected()) {
                count++;
            }
        }
        return count;
    }

    public void selectAll() {
        for (SimpleDebt debt : debtList) {
            debt.setSelected(true);
        }
        mItemEventListener.onSelectionChange(AdapterSimpleDebts.this.getSelectedCount());
        AdapterSimpleDebts.this.notifyDataSetChanged();
    }

    public void selectByModel(final IAbstractModel model) {
        mItemEventListener.onSelectionChange(AdapterSimpleDebts.this.getSelectedCount());
        AdapterSimpleDebts.this.notifyDataSetChanged();
    }

    public void unselectAll() {
        for (SimpleDebt debt : debtList) {
            debt.setSelected(false);
        }
        mItemEventListener.onSelectionChange(getSelectedCount());
        notifyDataSetChanged();
    }

    public List<IAbstractModel> removeSelectedDebts() {
        List<IAbstractModel> debts = new ArrayList<>();
        for (int i = debtList.size() - 1; i >= 0; i--) {
            if (debtList.get(i).isSelected()) {
                debts.add(debtList.get(i));
                debtList.remove(i);
            }
        }

        return debts;
    }

    public ArrayList<Long> getSelectedDebtsIDsAsLong() {
        ArrayList<Long> selectedItems = new ArrayList<>();
        for (SimpleDebt debt : debtList) {
            if (debt.isSelected()) {
                selectedItems.add(debt.getID());
            }
        }

        return selectedItems;
    }

    public ArrayList<String> getSelectedDebtsIDs() {
        ArrayList<String> selectedItems = new ArrayList<>();
        for (SimpleDebt debt : debtList) {
            if (debt.isSelected()) {
                selectedItems.add(String.valueOf(debt.getID()));
            }
        }

        return selectedItems;
    }

    public ArrayList<SimpleDebt> getSelectedDebts() {
        ArrayList<SimpleDebt> selectedItems = new ArrayList<>();
        for (SimpleDebt debt : debtList) {
            if (debt.isSelected()) {
                selectedItems.add(debt);
            }
        }

        return selectedItems;
    }

    @Override
    public void OnItemSelected() {
        mItemEventListener.onSelectionChange(getSelectedCount());
    }

    @Override
    public void OnItemClick(int position) {
        mItemEventListener.onItemClick(debtList.get(position));
    }

}
