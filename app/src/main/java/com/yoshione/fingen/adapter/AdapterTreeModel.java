package com.yoshione.fingen.adapter;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.os.Handler;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.MotionEventCompat;
import android.support.v7.preference.PreferenceManager;
import android.support.v7.widget.RecyclerView;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.yoshione.fingen.FgConst;
import com.yoshione.fingen.R;
import com.yoshione.fingen.adapter.helper.ItemTouchHelperAdapter;
import com.yoshione.fingen.adapter.helper.OnStartDragListener;
import com.yoshione.fingen.dao.AbstractDAO;
import com.yoshione.fingen.dao.BaseDAO;
import com.yoshione.fingen.interfaces.IAbstractModel;
import com.yoshione.fingen.interfaces.IAdapterEventsListener;
import com.yoshione.fingen.interfaces.IOrderable;
import com.yoshione.fingen.interfaces.IUpdateTreeListsEvents;
import com.yoshione.fingen.utils.BaseNode;
import com.yoshione.fingen.utils.IconGenerator;
import com.yoshione.fingen.utils.ScreenUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import eu.davidea.flipview.FlipView;

/**
 * Created by slv on 25.12.2015.
 * a
 */
public class AdapterTreeModel extends RecyclerView.Adapter implements ItemTouchHelperAdapter {

    public static final int MODE_VIEW = 0;
    public static final int MODE_SINGLECHOICE = 1;
    public static final int MODE_MULTICHOICE = 2;

    //    private static String TAG = "AdapterTreeModel";
    private final OnStartDragListener mDragStartListener;
    private final Context mContext;
    private BaseNode mTree;
    private HashMap<Long, Boolean> mExpandMap;
    private IAdapterEventsListener mAdapterEventsListener;
    private IUpdateTreeListsEvents mUpdateListsEvents;
    private int offset16;
    private int offset8;
    private int mViewMode;
    private boolean mPrefExpand;
    private Drawable mIconCheckBoxChecked;
    private Drawable mIconCheckBoxUnChecked;
    private Drawable mIconRadioChecked;
    private Drawable mIconRadioUnChecked;

    public AdapterTreeModel(Context mContext, OnStartDragListener dragStartListener, IUpdateTreeListsEvents updateListsEvents) {
        this.mContext = mContext;
        mDragStartListener = dragStartListener;
        mUpdateListsEvents = updateListsEvents;
        offset16 = ScreenUtils.dpToPx(16, mContext);
        offset8 = ScreenUtils.dpToPx(8, mContext);
        mViewMode = MODE_VIEW;
        mExpandMap = new HashMap<>();
        mPrefExpand = PreferenceManager.getDefaultSharedPreferences(mContext).getBoolean(FgConst.PREF_EXPAND_LISTS, true);
        mIconCheckBoxChecked = ContextCompat.getDrawable(mContext, R.drawable.ic_check_box_checked);
        mIconCheckBoxUnChecked = ContextCompat.getDrawable(mContext, R.drawable.ic_check_box_unchecked);
        mIconRadioChecked = ContextCompat.getDrawable(mContext, R.drawable.ic_radio_button_checked);
        mIconRadioUnChecked = ContextCompat.getDrawable(mContext, R.drawable.ic_radio_button_unchecked);
    }

    public void setmAdapterEventsListener(IAdapterEventsListener adapterEventsListener) {
        this.mAdapterEventsListener = adapterEventsListener;
    }

    public int getViewMode() {
        return mViewMode;
    }

    public void setViewMode(int viewMode) {
        mViewMode = viewMode;

    }

    public BaseNode getTree() {
        return mTree;
    }

    public void setTree(BaseNode tree) {
        mTree = tree;
        if (mExpandMap.isEmpty()) {
            for (BaseNode node : mTree.getFlatChildrenList()) {
                mExpandMap.put(node.getModel().getID(), mPrefExpand);
                node.getModel().setExpanded(mPrefExpand);
            }
        } else {
            long id;
            for (BaseNode node : mTree.getFlatChildrenList()) {
                id = node.getModel().getID();
                if (mExpandMap.containsKey(id)) {
                    node.getModel().setExpanded(mExpandMap.get(id));
                } else {
                    node.getModel().setExpanded(mPrefExpand);
                }
            }
        }
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        RecyclerView.ViewHolder vh;
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item_tree_model_2, parent, false);

        vh = new ViewHolderTreeModel(v);

        return vh;
    }

    @Override
    public void onBindViewHolder(final RecyclerView.ViewHolder holder, int position) {
        BaseNode node = mTree.getChildrenAtFlatPos(holder.getAdapterPosition());
        if (node == null) return;
        final IAbstractModel model = node.getModel();
        final ViewHolderTreeModel vh = ((ViewHolderTreeModel) holder);

        vh.itemView.setLongClickable(true);
        ViewGroup.MarginLayoutParams p = (ViewGroup.MarginLayoutParams) vh.itemView.getLayoutParams();
        int leftMargin = offset16 * (node.getLevel() - 2);
        p.setMargins(leftMargin, 0, 0, 0);
        vh.itemView.requestLayout();

        vh.itemView.setLongClickable(true);

        vh.expandableIndicator.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                model.setExpanded(!model.isExpanded());
                mExpandMap.put(model.getID(), model.isExpanded());
                AdapterTreeModel.this.notifyDataSetChanged();
            }
        });

        String modelName = model.getName();

        if (node.getNumberOfChildren() == 0) {
            vh.expandableIndicator.setVisibility(View.GONE);
        } else {
            vh.expandableIndicator.setVisibility(View.VISIBLE);
            boolean isExpanded;
            if (mExpandMap.containsKey(model.getID())) {
                isExpanded = mExpandMap.get(model.getID());
            } else {
                isExpanded = mPrefExpand;
            }
            if (!isExpanded) {
                modelName = String.format("%s (+%s)", modelName, String.valueOf(node.getNumberOfChildren()));
            }
            vh.expandableIndicator.setImageDrawable(IconGenerator.getExpandIndicatorIcon(isExpanded, mContext));
        }

        vh.textViewModelName.setText(modelName);

        vh.dragHandle.setEnabled(mViewMode != MODE_VIEW || model instanceof IOrderable);
        vh.dragHandle.getFrontImageView().setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (MotionEventCompat.getActionMasked(event) == MotionEvent.ACTION_DOWN) {
                    mDragStartListener.onStartDrag(holder);
                }
                return false;
            }
        });

        p = (ViewGroup.MarginLayoutParams) vh.textViewModelName.getLayoutParams();
        if (model.getModelType() == IAbstractModel.MODEL_TYPE_CATEGORY | model.getModelType() == IAbstractModel.MODEL_TYPE_PROJECT) {
            vh.colorTag.setVisibility(View.VISIBLE);
            GradientDrawable bgShape = (GradientDrawable) vh.colorTag.getBackground();
            bgShape.setColor(model.getColor());
            p.setMargins(offset8, 0, 0, 0);
        } else {
            vh.colorTag.setVisibility(View.GONE);
            p.setMargins(0, 0, 0, 0);
        }
        vh.itemView.requestLayout();
        switch (mViewMode) {
            case MODE_VIEW :
                if (model instanceof IOrderable) {
                    vh.dragHandle.getFrontImageView().setImageDrawable(ContextCompat.getDrawable(mContext, R.drawable.ic_drag));
                } else {
                    vh.dragHandle.getFrontImageView().setImageDrawable(null);
                }
                break;
            case MODE_SINGLECHOICE:
                vh.dragHandle.getFrontImageView().setImageDrawable(mIconRadioUnChecked);
                vh.dragHandle.getRearImageView().setImageDrawable(mIconRadioChecked);
                if (model.isSelected()) {
                    vh.dragHandle.flipSilently(true);
                } else {
                    vh.dragHandle.flipSilently(false);
                }
                break;
            case MODE_MULTICHOICE:
                vh.dragHandle.getFrontImageView().setImageDrawable(mIconCheckBoxUnChecked);
                vh.dragHandle.getRearImageView().setImageDrawable(mIconCheckBoxChecked);
                if (model.isSelected()) {
                    vh.dragHandle.flipSilently(true);
                } else {
                    vh.dragHandle.flipSilently(false);
                }
                break;
        }

        View.OnClickListener onClickListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                switch (mViewMode) {
                    case MODE_VIEW:
                        break;
                    case MODE_SINGLECHOICE:
                        model.setSelected(!model.isSelected());
                        vh.dragHandle.flip(model.isSelected());
                        new Handler().postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                mAdapterEventsListener.onItemClick(model);
                            }
                        }, 100);

                        break;
                    case MODE_MULTICHOICE:
                        model.setSelected(!model.isSelected());
                        vh.dragHandle.flip(model.isSelected());
                        break;
                }
            }
        };

        vh.itemView.setOnClickListener(onClickListener);

        vh.dragHandle.getFrontImageView().setScaleType(ImageView.ScaleType.CENTER);
        vh.dragHandle.getRearImageView().setScaleType(ImageView.ScaleType.CENTER);
        vh.dragHandle.setOnClickListener(onClickListener);


    }

    @Override
    public int getItemCount() {
        return mTree.getNumberOfChildren();
    }

    @Override
    public long getItemId(int position) {
        BaseNode node = mTree.getChildrenAtFlatPos(position);
        if (node != null) {
            return node.getModel().getID();
        } else {
            return -1;
        }
    }

    @Override
    public void onDrop(RecyclerView.ViewHolder vh, int fromPosition, int toPosition) {
        BaseNode node = mTree.getChildrenAtFlatPos(toPosition);

        if (node != null && node.getModel() != null) {
            AbstractDAO dao = BaseDAO.getDAO(node.getModel().getClass(), mContext);
            if (dao != null) {
                dao.updateOrder(mTree.getOrderList());
                ((IOrderable)node.getModel()).setOrderNum(toPosition);
                try {
                    dao.createModel(node.getModel());
                } catch (Exception e) {
                    Toast.makeText(mContext, R.string.msg_error_on_write_to_db, Toast.LENGTH_SHORT).show();
                }
            }
        }

        mUpdateListsEvents.update();
    }

    @Override
    public boolean onItemMove(RecyclerView.ViewHolder vh, int fromPosition, int toPosition) {
        List<Pair<BaseNode, Integer>> children = new ArrayList<>();
        BaseNode node = mTree.getChildrenAtFlatPos(fromPosition);
        if (node != null) {
            for (BaseNode child : node.getFlatChildrenList()) {
                children.add(new Pair<>(child, mTree.getFlatPos(child)));
            }
            IAbstractModel model = mTree.moveItemFromToFlatPos(fromPosition, toPosition);
            if (model == null) {
                return false;
            }

            if (toPosition < fromPosition) {
                notifyItemMoved(fromPosition, toPosition);
                onBindViewHolder(vh, toPosition);
                for (Pair<BaseNode, Integer> pair : children) {
                    notifyItemMoved(pair.second, mTree.getFlatPos(pair.first));
                }
            } else {
                for (int i = children.size() - 1; i >= 0; i--) {
                    notifyItemMoved(children.get(i).second, mTree.getFlatPos(children.get(i).first));
                }
                notifyItemMoved(fromPosition, mTree.getFlatPos(node));
                onBindViewHolder(vh, mTree.getFlatPos(node));
            }
            return true;
        } else {
            return false;
        }
    }

    @Override
    public void onItemDismiss(int position) {

    }

    public BaseNode getItemAtPos(int position) {
        return mTree.getFlatChildrenList().get(position);
    }

    static class ViewHolderTreeModel extends RecyclerView.ViewHolder {
        @BindView(R.id.drag_handle)
        FlipView dragHandle;
        @BindView(R.id.textViewModelName)
        TextView textViewModelName;
        @BindView(R.id.expandableIndicator)
        ImageView expandableIndicator;
        @BindView(R.id.color_tag)
        ImageView colorTag;

        ViewHolderTreeModel(View view) {
            super(view);
            ButterKnife.bind(this, view);
        }
    }

    public void selectAll() {
        for (BaseNode node : mTree.getFlatChildrenList()) {
            node.getModel().setSelected(true);
        }
        notifyDataSetChanged();
    }

    public void unselectAll() {
        for (BaseNode node : mTree.getFlatChildrenList()) {
            node.getModel().setSelected(false);
        }
        notifyDataSetChanged();
    }
}
