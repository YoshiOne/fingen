package com.yoshione.fingen.adapter;

import android.graphics.PorterDuff;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import android.graphics.drawable.Drawable;
import com.yoshione.fingen.R;
import com.yoshione.fingen.model.SkuDetailsItem;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by slv on 21.01.2016.
 *
 */
public class AdapterSku extends RecyclerView.Adapter {

    private List<SkuDetailsItem> skuDetailsItemList;
    private int mColorPurchased;

    public AdapterSku(List<SkuDetailsItem> skuDetailsItemList, int colorPurchased) {
        this.skuDetailsItemList = skuDetailsItemList;
        mColorPurchased = colorPurchased;
    }

    public List<SkuDetailsItem> getSkuDetailsItemList() {
        return skuDetailsItemList;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        RecyclerView.ViewHolder vh;
        View v = LayoutInflater.from(parent.getContext()).inflate(
                R.layout.list_item_sku, parent, false);

        vh = new SkuDetailsItemViewHolder(v);

        return vh;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        SkuDetailsItemViewHolder mvh = (SkuDetailsItemViewHolder) holder;
        SkuDetailsItem skuDetailsItem = skuDetailsItemList.get(position);

        mvh.textViewSkuTitle.setText(skuDetailsItem.getSkuDetails().title);
        mvh.textViewSkuDescription.setText(skuDetailsItem.getSkuDetails().description);

        Drawable icon = skuDetailsItem.getIcon();
        if (skuDetailsItem.isPurchased()) {
            mvh.textViewSkuPrice.setVisibility(View.GONE);
            icon.setColorFilter(mColorPurchased, PorterDuff.Mode.SRC_ATOP);
        } else {
            mvh.textViewSkuPrice.setVisibility(View.VISIBLE);
            mvh.textViewSkuPrice.setText(skuDetailsItem.getSkuDetails().priceText);
        }
        mvh.imageViewIcon.setImageDrawable(icon);
        mvh.itemView.setOnClickListener(skuDetailsItem.getOnClickListener());
    }

    @Override
    public int getItemCount() {
        return skuDetailsItemList.size();
    }

    @Override
    public long getItemId(int position) {
        return skuDetailsItemList.get(position).getId();
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
