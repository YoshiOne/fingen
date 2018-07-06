package com.yoshione.fingen.adapter;

import android.support.annotation.NonNull;
import android.support.constraint.ConstraintLayout;
import android.support.v4.view.MotionEventCompat;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.yoshione.fingen.R;
import com.yoshione.fingen.adapter.helper.ItemTouchHelperAdapter;
import com.yoshione.fingen.adapter.helper.OnStartDragListener;
import com.yoshione.fingen.model.TrEditItem;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import eu.davidea.flipview.FlipView;

public class AdapterTrEditConstructor extends RecyclerView.Adapter implements ItemTouchHelperAdapter {
    private final OnStartDragListener mDragStartListener;
    private List<TrEditItem> mList;

    public AdapterTrEditConstructor(OnStartDragListener dragStartListener) {
        mDragStartListener = dragStartListener;
        mList = new ArrayList<>();
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        RecyclerView.ViewHolder vh;
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item_tredit, parent, false);

        vh = new ViewHolderTabOrder(v);

        return vh;
    }

    @Override
    public void onBindViewHolder(@NonNull final RecyclerView.ViewHolder holder, int position) {
        final ViewHolderTabOrder vh = ((ViewHolderTabOrder) holder);
        final TrEditItem item = mList.get(position);

        vh.textViewModelName.setText(item.getName());

        vh.dragHandle.setEnabled(true);
        vh.dragHandle.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (MotionEventCompat.getActionMasked(event) == MotionEvent.ACTION_DOWN) {
                    mDragStartListener.onStartDrag(holder);
                }
                return false;
            }
        });

        vh.mFlipViewVisible.setVisibility(item.isLockVisible() ? View.GONE : View.VISIBLE);
        vh.mflipViewHideUnderMore.setVisibility(item.isLockHide() ? View.GONE : View.VISIBLE);

        vh.mFlipViewVisible.flipSilently(item.isVisible());
        vh.mflipViewHideUnderMore.flipSilently(item.isHideUnderMore());

        vh.mFlipViewVisible.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!item.isLockVisible()) {
                    item.setVisible(!item.isVisible());
                    vh.mFlipViewVisible.flip(item.isVisible());
                }
            }
        });

        vh.mflipViewHideUnderMore.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!item.isLockHide()) {
                    item.setHideUnderMore(!item.isHideUnderMore());
                    vh.mflipViewHideUnderMore.flip(item.isHideUnderMore());
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return mList.size();
    }

    @Override
    public long getItemId(int position) {
        return mList.get(position).getID().hashCode();
    }

    @Override
    public boolean onItemMove(RecyclerView.ViewHolder vh, int fromPosition, int toPosition) {
        TrEditItem item = mList.remove(fromPosition);
        mList.add(toPosition, item);
        notifyItemMoved(fromPosition, toPosition);
        onBindViewHolder(vh, toPosition);
        return true;
    }

    @Override
    public void onDrop(RecyclerView.ViewHolder vh, int fromPosition, int toPosition) {

    }

    @Override
    public void onItemSwypeRight(int position) {

    }

    static class ViewHolderTabOrder extends RecyclerView.ViewHolder {
        @BindView(R.id.drag_handle)
        ImageView dragHandle;
        @BindView(R.id.textViewModelName)
        TextView textViewModelName;
        @BindView(R.id.container)
        ConstraintLayout container;
        @BindView(R.id.flip_view_visible)
        FlipView mFlipViewVisible;
        @BindView(R.id.flip_view_hide_under_more)
        FlipView mflipViewHideUnderMore;

        ViewHolderTabOrder(View view) {
            super(view);
            ButterKnife.bind(this, view);
        }
    }

    public void setList(List<TrEditItem> list) {
        mList = list;
    }

    public List<TrEditItem> getList() {
        return mList;
    }
}
