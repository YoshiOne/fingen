package com.yoshione.fingen.adapter;

import android.content.Context;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.yoshione.fingen.FGApplication;
import com.yoshione.fingen.R;
import com.yoshione.fingen.iab.models.ReportsItem;

import java.util.List;
import java.util.Objects;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by slv on 21.01.2016.
 *
 */
public class AdapterSku extends RecyclerView.Adapter {

    private List<Object> mItemList;
    private int mColorPurchased;
    private Context mContext;

    public AdapterSku(List<Object> itemList, int colorPurchased) {
        this.mItemList = itemList;
        mColorPurchased = colorPurchased;
        mContext = FGApplication.getAppComponent().getContext();
    }

    public List<Object> getItemList() {
        return mItemList;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        RecyclerView.ViewHolder vh;
        View v = LayoutInflater.from(parent.getContext()).inflate(
                R.layout.list_item_sku, parent, false);

        vh = new SkuDetailsItemViewHolder(v);

        return vh;
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        SkuDetailsItemViewHolder mvh = (SkuDetailsItemViewHolder) holder;
        if (mItemList.get(position).getClass().equals(ReportsItem.class)) {
            ReportsItem reportsItem = (ReportsItem) mItemList.get(position);
            if (reportsItem.getSkuDetailsWrapper().getSkuDetails() == null) {
                return;
            }
            mvh.textViewSkuTitle.setText(reportsItem.getSkuDetailsWrapper().getSkuDetails().title);
            mvh.textViewSkuDescription.setText(reportsItem.getSkuDetailsWrapper().getSkuDetails().description);

            Drawable icon = FGApplication.getContext().getDrawable(reportsItem.getIconID());
            if (reportsItem.getSkuDetailsWrapper().isPurchased()) {
                Objects.requireNonNull(icon).setColorFilter(mColorPurchased, PorterDuff.Mode.SRC_ATOP);
                mvh.textViewSkuPrice.setText(mContext.getString(R.string.ttl_purchased));
            } else {
                mvh.textViewSkuPrice.setText(reportsItem.getSkuDetailsWrapper().getSkuDetails().priceText);
            }
            mvh.imageViewIcon.setImageDrawable(icon);
            mvh.itemView.setOnClickListener(reportsItem.getOnClickListener());
        }
    }

    @Override
    public int getItemCount() {
        return mItemList.size();
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public int getItemViewType(int position) {
        return position;
    }

    class SkuDetailsItemViewHolder extends RecyclerView.ViewHolder {
        @BindView(R.id.imageViewSkuIcon)
        ImageView imageViewIcon;
        @BindView(R.id.textViewSkuTitle)
        TextView textViewSkuTitle;
        @BindView(R.id.textViewSkuDescription)
        TextView textViewSkuDescription;
        @BindView(R.id.textViewSkuPrice)
        TextView textViewSkuPrice;

        SkuDetailsItemViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }
    }
}
