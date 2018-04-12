/*
 * Copyright (c) 2015.
 */

package com.yoshione.fingen.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.yoshione.fingen.R;
import com.yoshione.fingen.adapter.viewholders.VhCredit;
import com.yoshione.fingen.adapter.viewholders.VhModelBase;
import com.yoshione.fingen.adapter.viewholders.VhProduct;
import com.yoshione.fingen.adapter.viewholders.VhSimpleDebt;
import com.yoshione.fingen.adapter.viewholders.VhSmsMarker;
import com.yoshione.fingen.adapter.viewholders.VhTemplate;
import com.yoshione.fingen.interfaces.IAbstractModel;

import java.util.List;

/**
 * Created by slv on 07.12.2015.
 *
 */
public class AdapterAbstractModel extends RecyclerView.Adapter {

    private List<?> mModelList;
    private final Context mContext;
    private AdapterAbstractModelEventsListener mAbstractModelEventsListener;

    public void setmAbstractModelEventsListener(AdapterAbstractModelEventsListener mAbstractModelEventsListener) {
        this.mAbstractModelEventsListener = mAbstractModelEventsListener;
    }

    public AdapterAbstractModel(Context mContext) {
        this.mContext = mContext;
    }

    public void setmModelList(List<?> mModelList) {
        this.mModelList = mModelList;
    }

    public void applyFilter(String filter) {
        if (!filter.isEmpty()) {
            for (int i = mModelList.size() - 1; i >= 0; i--) {
                if (!((IAbstractModel) mModelList.get(i)).getSearchString().isEmpty()) {
                    if (!((IAbstractModel) mModelList.get(i)).getSearchString().toLowerCase().contains(filter.toLowerCase())) {
                        mModelList.remove(i);
                    }
                }
            }
        }
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        RecyclerView.ViewHolder vh = null;
        View v;
        OnItemClickListener onItemClickListener = new OnItemClickListener();
        switch (viewType) {
            case IAbstractModel.MODEL_TYPE_SMSMARKER: {
                v = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item_sms_marker, parent, false);
                vh = new VhSmsMarker(v, onItemClickListener, mContext);
                break;
            }
            case IAbstractModel.MODEL_TYPE_CREDIT: {
                v = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item_credit, parent, false);
                vh = new VhCredit(v, onItemClickListener, mContext);
                break;
            }
            case IAbstractModel.MODEL_TYPE_SIMPLEDEBT: {
                v = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item_simple_dept, parent, false);
                vh = new VhSimpleDebt(v, onItemClickListener, mContext);
                break;
            }
            case IAbstractModel.MODEL_TYPE_TEMPLATE: {
                v = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item_templates, parent, false);
                vh = new VhTemplate(v, onItemClickListener, mContext);
                break;
            }
            case IAbstractModel.MODEL_TYPE_PRODUCT: {
                v = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item_product_ref, parent, false);
                vh = new VhProduct(v, onItemClickListener, mContext);
                break;
            }
            default:
                throw new Error("Missing viewholder for model");
        }
        return vh;
    }

    private class OnItemClickListener implements VhModelBase.IOnItemClickListener {
        @Override
        public void OnItemClick(int position) {
            if (mAbstractModelEventsListener != null) {
                mAbstractModelEventsListener.OnAbstractModelItemClick((IAbstractModel) mModelList.get(position));
            }
        }
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        ((VhModelBase) holder).bindViewHolder(mModelList, position);
    }

    @Override
    public int getItemCount() {
        return (mModelList != null)? mModelList.size():0;
    }

    @Override
    public long getItemId(int position) {
        return ((IAbstractModel) mModelList.get(position)).getID();
    }

    @Override
    public int getItemViewType(int position) {
        return ((IAbstractModel) mModelList.get(position)).getModelType();
    }

    public interface AdapterAbstractModelEventsListener {
        void OnAbstractModelItemClick(IAbstractModel abstractModel);
    }
}
