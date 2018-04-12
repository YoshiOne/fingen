package com.yoshione.fingen.adapter;

import android.support.constraint.ConstraintLayout;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.yoshione.fingen.R;
import com.yoshione.fingen.interfaces.IAdapterEventsListener;
import com.yoshione.fingen.model.AccountsSet;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import eu.davidea.flipview.FlipView;

/**
 * Created by slv on 01.12.2016.
 * d
 */

public class AdapterAccountsSets extends RecyclerView.Adapter {
    private List<AccountsSet> mList;
    private IAdapterEventsListener mAdapterEventsListener;

    public AdapterAccountsSets(IAdapterEventsListener adapterEventsListener) {
        mList = new ArrayList<>();
        mAdapterEventsListener = adapterEventsListener;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        RecyclerView.ViewHolder vh;
        View v = LayoutInflater.from(parent.getContext()).inflate(
                R.layout.list_item_account_set, parent, false);

        vh = new AccountsSetViewHolder(v);

        return vh;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        final AccountsSet accountsSet;
        accountsSet = mList.get(position);
        final AccountsSetViewHolder vh = ((AccountsSetViewHolder) holder);

        if (accountsSet.getAccountsSetRef().getID() >= 0) {
            vh.itemView.setLongClickable(true);
        }
        vh.textViewModelName.setText(accountsSet.getAccountsSetRef().getName());

        if (accountsSet.isSelected()) {
            vh.dragHandle.flipSilently(true);
        } else {
            vh.dragHandle.flipSilently(false);
        }
        View.OnClickListener onClickListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                accountsSet.setSelected(!accountsSet.isSelected());
                vh.dragHandle.flip(accountsSet.isSelected());
                mAdapterEventsListener.onItemClick(accountsSet.getAccountsSetRef());
            }
        };

        vh.itemView.setOnClickListener(onClickListener);

        vh.dragHandle.getFrontImageView().setScaleType(ImageView.ScaleType.CENTER);
        vh.dragHandle.getRearImageView().setScaleType(ImageView.ScaleType.CENTER);
        vh.dragHandle.setOnClickListener(onClickListener);
    }

    @Override
    public int getItemCount() {
        return mList.size() + 1;
    }

    @Override
    public long getItemId(int position) {
        return mList.get(position).getAccountsSetRef().getID();
    }

    public List<AccountsSet> getList() {
        return mList;
    }

    public void setList(List<AccountsSet> list) {
        mList = list;
    }

    class AccountsSetViewHolder extends RecyclerView.ViewHolder {
        @BindView(R.id.radioButton)
        FlipView dragHandle;
        @BindView(R.id.textViewModelName)
        TextView textViewModelName;
        @BindView(R.id.container)
        ConstraintLayout container;

        AccountsSetViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }
    }
}
