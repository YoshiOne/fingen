package com.yoshione.fingen.adapter;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import com.yoshione.fingen.R;
import com.yoshione.fingen.model.MenuItem;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by slv on 21.01.2016.
 *
 */
public class AdapterMenuItems extends RecyclerView.Adapter {

    public AdapterMenuItems(List<MenuItem> menuItemList) {
        this.menuItemList = menuItemList;
    }

    private final List<MenuItem> menuItemList;

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        RecyclerView.ViewHolder vh;
        View v = LayoutInflater.from(parent.getContext()).inflate(
                R.layout.list_item_menu, parent, false);

        vh = new MenuItemViewHolder(v);

        return vh;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        MenuItemViewHolder mvh = (MenuItemViewHolder) holder;
        MenuItem menuItem = menuItemList.get(position);

        mvh.imageViewIcon.setImageDrawable(menuItem.getmIcon());
        mvh.textViewTitle.setText(menuItem.getmTitle());
        mvh.itemView.setOnClickListener(menuItem.getmOnClickListener());

        if (position == menuItemList.size() - 1) {
            mvh.mSpaceBottom.setVisibility(View.GONE);
            mvh.mSpaceBottomFinal.setVisibility(View.VISIBLE);
        } else {
            mvh.mSpaceBottom.setVisibility(View.VISIBLE);
            mvh.mSpaceBottomFinal.setVisibility(View.GONE);
        }
    }

    @Override
    public int getItemCount() {
        return menuItemList.size();
    }

    class MenuItemViewHolder extends RecyclerView.ViewHolder {
        @BindView(R.id.imageViewIcon)
        ImageView imageViewIcon;
        @BindView(R.id.textViewTitle)
        TextView textViewTitle;
        @BindView(R.id.spaceBottom)
        FrameLayout mSpaceBottom;
        @BindView(R.id.spaceBottomFinal)
        FrameLayout mSpaceBottomFinal;

        MenuItemViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }
    }

    @Override
    public long getItemId(int position) {
        return menuItemList.get(position).getId();
    }
}
