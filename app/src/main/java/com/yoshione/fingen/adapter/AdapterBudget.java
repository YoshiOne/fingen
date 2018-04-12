package com.yoshione.fingen.adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.preference.PreferenceManager;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import android.util.Log;
import com.yoshione.fingen.R;
import com.yoshione.fingen.classes.SumsByCabbage;
import com.yoshione.fingen.dao.CabbagesDAO;
import com.yoshione.fingen.model.Category;
import com.yoshione.fingen.utils.CNode;
import com.yoshione.fingen.utils.CabbageFormatter;
import com.yoshione.fingen.utils.ScreenUtils;

import java.math.BigDecimal;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by slv on 07.03.2016.
 *
 */
public class AdapterBudget extends RecyclerView.Adapter {
    private static final String TAG = "AdapterBudget";

//    public static final long BUDGET_ITEM_TOTAL_START = -1;
//    public static final long BUDGET_ITEM_TOTAL_END = -2;
    public static final long BUDGET_ITEM_INCOME = -3;
    public static final long BUDGET_ITEM_OUTCOME = -4;
    public static final long BUDGET_ITEM_TOTAL_IO = -5;
    public static final long BUDGET_ITEM_BORROW = -6;
    public static final long BUDGET_ITEM_REPAY = -7;
    public static final long BUDGET_ITEM_TOTAL_DEBTS = -8;
    public static final long BUDGET_ITEM_DEBTS_ROOT = -1000;

    public static final int INFO_TYPE_TR_INCOME = 0;
    public static final int INFO_TYPE_TR_OUTCOME = 1;
//    public static final int INFO_TYPE_TF_INCOME = 2;
//    public static final int INFO_TYPE_TF_OUTCOME = 3;
    public static final int INFO_TYPE_TR_TOTAL = 4;
//    public static final int INFO_TYPE_TF_TOTAL = 5;
    public static final int INFO_TYPE_INCOME_TOTAL = 6;
    public static final int INFO_TYPE_OUTCOME_TOTAL = 7;
    public static final int INFO_TYPE_ALL_TOTAL = 8;
    private CNode mTree;
    private final Context mContext;
    private int mYear = 0;
    private int mMonth = 0;
    private IOnItemClickListener onItemClickListener;
    private final int mTheme;
    private int offset;
    private int margin;

    public void setOnItemClickListener(IOnItemClickListener onItemClickListener) {
        this.onItemClickListener = onItemClickListener;
    }

    public AdapterBudget(Context mContext) {
        this.mContext = mContext;
        offset = ScreenUtils.dpToPx(16, mContext);
        margin = ScreenUtils.dpToPx(2, mContext);
        mTheme = Integer.valueOf(PreferenceManager.getDefaultSharedPreferences(mContext).getString("theme", "0"));
    }

//    public Integer getPosById(long id) {
//        for (int i = 0; i < mList.size(); i++) {
//            if (mList.get(i).getmId() == id) {
//                return i;
//            }
//        }
//        return null;
//    }

    public CNode getmTree() {
        return mTree;
    }

    public void setmTree(CNode mTree) {
        this.mTree = mTree;
    }

    public void setmYear(int mYear) {
        this.mYear = mYear;
    }

    public void setmMonth(int mMonth) {
        this.mMonth = mMonth;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        RecyclerView.ViewHolder vh;
        View v = LayoutInflater.from(parent.getContext()).inflate(
                R.layout.list_item_budget, parent, false);

        vh = new BudgetViewHolder(v);

        return vh;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        BudgetViewHolder vh = (BudgetViewHolder) holder;
        vh.itemView.setLongClickable(true);
        CNode node = mTree.getChildrenAtFlatPos(position);
        if (node != null) {
            Category category = node.getmCategory();

            ViewGroup.MarginLayoutParams p = (ViewGroup.MarginLayoutParams) vh.itemView.getLayoutParams();
            int leftMargin = offset * (node.getLevel() - 2);
            p.setMargins(leftMargin, margin, margin, margin);
            vh.itemView.requestLayout();

            vh.textViewCategoryName.setText(category.getName());
            vh.container.removeAllViews();
            try {
                for (SumsByCabbage sum : category.getBudget().getmSums().getmList()) {
                    if (!sum.isEmpty(false)) {
                        LinearLayout linearLayout = createTableLayout();
                        fillSumsForCabbage(sum, category.getBudget().getmType(), linearLayout, node, position);
                        vh.container.addView(linearLayout);
                    }
                }
            } catch (Exception e) {
                Log.e(TAG, "binding exception");
            }
        }
    }

    private class OnCLickListener implements View.OnClickListener {
        final long mCabbageId;
        final int mPosition;
        final BigDecimal mAmount;
        final CNode mNode;

        OnCLickListener(long mCabbageId, int mPosition, BigDecimal mAmount, CNode mNode) {
            this.mCabbageId = mCabbageId;
            this.mPosition = mPosition;
            this.mAmount = mAmount;
            this.mNode = mNode;
        }

        @Override
        public void onClick(View v) {
            long id = mNode.getmCategory().getID();
            if (onItemClickListener != null & (id >= 0 | id < BUDGET_ITEM_DEBTS_ROOT) & !mNode.isHasChildren()) {
                onItemClickListener.onItemClick(mNode.getmCategory(), mYear, mMonth, mCabbageId, mPosition,mAmount);
            }
        }
    }


    private LinearLayout createTableLayout() {
        LinearLayout linearLayout = new LinearLayout(mContext);
        linearLayout.setOrientation(LinearLayout.HORIZONTAL);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        linearLayout.setLayoutParams(params);

//        if(android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.JELLY_BEAN) {
//            linearLayout.setBackgroundDrawable(ContextCompat.getDrawable(mContext, R.drawable.bg_stroked_rect));
//        } else {
//            linearLayout.setBackground(ContextCompat.getDrawable(mContext, R.drawable.bg_stroked_rect));
//        }

        return linearLayout;
    }

    private void fillSumsForCabbage(SumsByCabbage sums,
                                    int infoType, LinearLayout linearLayout,CNode node, int position) {
        BigDecimal fact = BigDecimal.ZERO;
        BigDecimal plan = BigDecimal.ZERO;

        switch (infoType) {
            case INFO_TYPE_TR_INCOME:
                fact = sums.getInTrSum();
                plan = sums.getInPlan();
                break;
            case INFO_TYPE_TR_OUTCOME:
                fact = sums.getOutTrSum();
                plan = sums.getOutPlan();
                break;
            case INFO_TYPE_TR_TOTAL:
                fact = sums.getInTrSum().add(sums.getOutTrSum());
                plan = sums.getInPlan().add(sums.getOutPlan());
                break;
            case INFO_TYPE_INCOME_TOTAL:
                fact = sums.getInTrSum();
                plan = sums.getInPlan();
                break;
            case INFO_TYPE_OUTCOME_TOTAL:
                fact = sums.getOutTrSum();
                plan = sums.getOutPlan();
                break;
            case INFO_TYPE_ALL_TOTAL:
                fact = sums.getInTrSum().add(sums.getOutTrSum());
                plan = sums.getInPlan().add(sums.getOutPlan());
                break;
        }

        CabbageFormatter cabbageFormatter = new CabbageFormatter(CabbagesDAO.getInstance(mContext).getCabbageByID(sums.getCabbageId()));

        @SuppressLint("PrivateResource") int textAppearanceNormal = R.style.StyleTextSum;

        BigDecimal dif = fact.subtract(plan);
        boolean positive = dif.compareTo(BigDecimal.ZERO) >= 0;

        OnCLickListener onCLickListener = new OnCLickListener(sums.getCabbageId(), position, plan, node);

        TextView textViewPlan = createTextView(
                cabbageFormatter.format(plan), 1, textAppearanceNormal, Gravity.START, positive, onCLickListener);
        linearLayout.addView(textViewPlan);

        linearLayout.addView(createTextView(cabbageFormatter.format(fact), 1, textAppearanceNormal, Gravity.CENTER, positive, onCLickListener));


        linearLayout.addView(createTextView(cabbageFormatter.format(fact.subtract(plan)), 1, textAppearanceNormal, Gravity.END, positive, onCLickListener));
    }


    private TextView createTextView(String text, float weight, int textAppearance, int gravity,
                                    boolean positive, OnCLickListener onCLickListener) {
        TextView textView = new TextView(mContext);
        textView.setText(text);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT);
        params.weight = weight;
        textView.setLayoutParams(params);

        textView.setGravity(Gravity.CENTER_VERTICAL | gravity);
        textView.setPadding(4, 16, 4, 16);
        textView.setTextAppearance(mContext, textAppearance);
        textView.setOnClickListener(onCLickListener);

        if (positive) {
            textView.setTextColor(ContextCompat.getColor(mContext, R.color.positive_color));
        } else {
            textView.setTextColor(ContextCompat.getColor(mContext, R.color.negative_color));
        }

        return textView;
    }

    @Override
    public int getItemCount() {
        if (mTree == null) {
            return 0;
        } else {
            return mTree.getNumberOfChildren();
        }
    }

    @Override
    public long getItemId(int position) {
        CNode node = mTree.getChildrenAtFlatPos(position);
        if (node != null) {
            return node.getmCategory().getID();
        } else {
            return -1;
        }
    }

    static class BudgetViewHolder extends RecyclerView.ViewHolder {
        @BindView(R.id.textViewModelName)
        TextView textViewCategoryName;
        @BindView(R.id.container)
        LinearLayout container;

        BudgetViewHolder(View view) {
            super(view);
            ButterKnife.bind(this, view);
        }
    }

    public interface IOnItemClickListener {
        void onItemClick(Category category, int year, int month, long cabbageId, int position, BigDecimal amount);
    }

}
