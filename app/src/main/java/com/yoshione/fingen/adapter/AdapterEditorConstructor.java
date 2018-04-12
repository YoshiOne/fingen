package com.yoshione.fingen.adapter;

import android.app.Activity;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.MotionEventCompat;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.yoshione.fingen.R;
import com.yoshione.fingen.adapter.helper.ItemTouchHelperAdapter;
import com.yoshione.fingen.adapter.helper.OnStartDragListener;
import com.yoshione.fingen.managers.EditorConstructorManager;
import com.yoshione.fingen.model.EditorConstructorItem;
import com.yoshione.fingen.utils.ColorUtils;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by slv on 27.02.2018.
 *
 */

public class AdapterEditorConstructor extends RecyclerView.Adapter implements ItemTouchHelperAdapter {

    public static final int ID_DATE_TIME = 1;
    public static final int ID_ACCOUNT = 2;
    public static final int ID_PAYEE_DESTACCOUNT = 3;
    public static final int ID_CATEGORY = 4;
    public static final int ID_AMOUNT = 5;
    public static final int ID_SMS = 6;
    public static final int ID_PROJECT = 7;
    public static final int ID_SIMPLEDEBT = 8;
    public static final int ID_DEPARTMENT = 9;
    public static final int ID_LOCATION = 10;
    public static final int ID_COMMENT = 11;

    private List<EditorConstructorItem> mList;
    private Drawable mVisible;
    private Drawable mInvisible;
    private Drawable mEnabled;
    private Drawable mDisabled;
    private final OnStartDragListener mDragStartListener;
    private int mVisibleTextColor;
    private int mInvisibleTextColor;

    public AdapterEditorConstructor(List<EditorConstructorItem> list, OnStartDragListener dragStartListener, Activity activity) {
        mList = list;
        mVisible = ContextCompat.getDrawable(activity, R.drawable.ic_visibile_blue);
        mInvisible = ContextCompat.getDrawable(activity, R.drawable.ic_invisible_gray);
        mEnabled = ContextCompat.getDrawable(activity, R.drawable.ic_check_box_checked);
        mDisabled = ContextCompat.getDrawable(activity, R.drawable.ic_check_box_unchecked);
        mDragStartListener = dragStartListener;
        mVisibleTextColor = ColorUtils.getTextColor(activity);
        mInvisibleTextColor = ContextCompat.getColor(activity, R.color.light_gray_text);
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        RecyclerView.ViewHolder vh;
        View v = LayoutInflater.from(parent.getContext()).inflate(
                R.layout.list_item_editor_constuctor, parent, false);

        vh = new EditorConstructorViewHolder(v);

        return vh;
    }

    @Override
    public void onBindViewHolder(final RecyclerView.ViewHolder holder, int position) {
        final EditorConstructorViewHolder vh = (EditorConstructorViewHolder) holder;
        final EditorConstructorItem item = mList.get(position);

        vh.mTextViewItemName.setText(item.getName());

        vh.mImageViewVisible.setVisibility(item.isAlwaysVisible() ? View.INVISIBLE : View.VISIBLE);
        vh.mImageViewVisible.setImageDrawable(item.isVisible() ? mVisible : mInvisible);
        vh.mImageViewVisible.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                item.setVisible(!item.isVisible());
                vh.mImageViewVisible.setImageDrawable(item.isVisible() ? mVisible : mInvisible);
            }
        });

        vh.mImageViewEnable.setVisibility(item.isAlwaysEnabled() ? View.INVISIBLE : View.VISIBLE);
        vh.mImageViewEnable.setImageDrawable(item.isEnabled() ? mEnabled : mDisabled);
        vh.mImageViewEnable.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                item.setEnabled(!item.isEnabled());
                vh.mImageViewEnable.setImageDrawable(item.isEnabled() ? mEnabled : mDisabled);
            }
        });

        vh.mImageViewDrag.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (MotionEventCompat.getActionMasked(event) == MotionEvent.ACTION_DOWN) {
                    mDragStartListener.onStartDrag(holder);
                }
                return false;
            }
        });
    }

    @Override
    public int getItemCount() {
        return mList.size();
    }

    @Override
    public long getItemId(int position) {
        return mList.get(position).getID();
    }

    public List<EditorConstructorItem> getList() {
        return mList;
    }

    @Override
    public boolean onItemMove(RecyclerView.ViewHolder vh, int fromPosition, int toPosition) {
        EditorConstructorItem item = mList.remove(fromPosition);
        mList.add(toPosition, item);
        notifyItemMoved(fromPosition, toPosition);
        onBindViewHolder(vh, toPosition);
//        notifyDataSetChanged();
        return true;
    }

    @Override
    public void onDrop(RecyclerView.ViewHolder vh, int fromPosition, int toPosition) {

    }

    @Override
    public void onItemDismiss(int position) {

    }

    static class EditorConstructorViewHolder extends RecyclerView.ViewHolder {
        @BindView(R.id.imageViewDrag)
        ImageView mImageViewDrag;
        @BindView(R.id.imageViewVisible)
        ImageView mImageViewVisible;
        @BindView(R.id.imageViewEnable)
        ImageView mImageViewEnable;
        @BindView(R.id.textViewItemName)
        TextView mTextViewItemName;

        EditorConstructorViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }
    }
}
