package com.yoshione.fingen.adapter;

import android.graphics.drawable.Drawable;
import android.support.constraint.ConstraintLayout;
import android.support.v4.view.MotionEventCompat;
import android.support.v7.widget.RecyclerView;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.yoshione.fingen.R;
import com.yoshione.fingen.adapter.helper.ItemTouchHelperAdapter;
import com.yoshione.fingen.adapter.helper.OnStartDragListener;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import eu.davidea.flipview.FlipView;

/**
 * Created by Leonid on 23.11.2016.
 * a
 */

public class AdapterTabOrder extends RecyclerView.Adapter implements ItemTouchHelperAdapter {
    private Drawable mIcon;
    private final OnStartDragListener mDragStartListener;
    private List<Pair<String, String>> mList;

    public AdapterTabOrder(OnStartDragListener dragStartListener, Drawable icon) {
        mDragStartListener = dragStartListener;
        mIcon = icon;
        mList = new ArrayList<>();
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        RecyclerView.ViewHolder vh;
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item_tree_model_2, parent, false);

        vh = new ViewHolderTabOrder(v);

        return vh;
    }

    @Override
    public void onBindViewHolder(final RecyclerView.ViewHolder holder, int position) {
        final ViewHolderTabOrder vh = ((ViewHolderTabOrder) holder);

        vh.textViewModelName.setText(mList.get(position).second);
        vh.expandableIndicator.setVisibility(View.GONE);
        vh.colorTag.setVisibility(View.GONE);

        vh.container.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                if (MotionEventCompat.getActionMasked(motionEvent) == MotionEvent.ACTION_DOWN) {
                    mDragStartListener.onStartDrag(holder);
                }
                return false;
            }
        });
        vh.dragHandle.setEnabled(true);
        vh.dragHandle.getFrontImageView().setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (MotionEventCompat.getActionMasked(event) == MotionEvent.ACTION_DOWN) {
                    mDragStartListener.onStartDrag(holder);
                }
                return false;
            }
        });

        vh.dragHandle.getFrontImageView().setImageDrawable(mIcon);
        vh.dragHandle.getFrontImageView().setScaleType(ImageView.ScaleType.CENTER);
    }

    @Override
    public int getItemCount() {
        return mList.size();
    }

    @Override
    public long getItemId(int position) {
        return mList.get(position).first.hashCode();
    }

    @Override
    public boolean onItemMove(RecyclerView.ViewHolder vh, int fromPosition, int toPosition) {
        Pair<String, String> pair = mList.remove(fromPosition);
        mList.add(toPosition, pair);
        notifyItemMoved(fromPosition, toPosition);
        onBindViewHolder(vh, toPosition);
        return true;
    }

    @Override
    public void onDrop(RecyclerView.ViewHolder vh, int fromPosition, int toPosition) {

    }

    @Override
    public void onItemDismiss(int position) {

    }

    static class ViewHolderTabOrder extends RecyclerView.ViewHolder {
        @BindView(R.id.drag_handle)
        FlipView dragHandle;
        @BindView(R.id.textViewModelName)
        TextView textViewModelName;
        @BindView(R.id.expandableIndicator)
        ImageView expandableIndicator;
        @BindView(R.id.container)
        ConstraintLayout container;
        @BindView(R.id.color_tag)
        ImageView colorTag;

        ViewHolderTabOrder(View view) {
            super(view);
            ButterKnife.bind(this, view);
        }
    }

    public void setList(List<Pair<String, String>> list) {
        mList = list;
    }

    public List<Pair<String, String>> getList() {
        return mList;
    }
}
