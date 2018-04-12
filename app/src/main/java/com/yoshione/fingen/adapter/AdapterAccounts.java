package com.yoshione.fingen.adapter;

import android.content.Context;
import android.preference.PreferenceManager;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.MotionEventCompat;
import android.support.v7.widget.RecyclerView;
import android.util.Pair;
import android.view.ContextThemeWrapper;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import com.yoshione.fingen.FgConst;
import com.yoshione.fingen.dao.AccountsDAO;
import com.yoshione.fingen.managers.AccountManager;
import com.yoshione.fingen.model.Account;
import com.yoshione.fingen.model.BaseModel;
import com.yoshione.fingen.model.Cabbage;
import com.yoshione.fingen.R;
import com.yoshione.fingen.utils.CabbageFormatter;
import com.yoshione.fingen.utils.IconGenerator;
import com.yoshione.fingen.adapter.helper.ItemTouchHelperAdapter;
import com.yoshione.fingen.adapter.helper.OnStartDragListener;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by slv on 17.09.2015.
 *
 */
public class AdapterAccounts extends RecyclerView.Adapter implements ItemTouchHelperAdapter {

    public void setAccountList(List<Account> accountList) {
        this.accountList = accountList;
    }

    private List<Account> accountList;

    private final Context mContext;

    private boolean mDragMode = false;
    private final OnStartDragListener mDragStartListener;
    private ContextThemeWrapper mContextThemeWrapper;

    public boolean ismDragMode() {
        return mDragMode;
    }

    public void setmDragMode(boolean mDragMode) {
        this.mDragMode = mDragMode;
    }

    private OnAccountItemClickListener mOnAccountItemClickListener;

    public void setmOnAccountItemClickListener(OnAccountItemClickListener mOnAccountItemClickListener) {
        this.mOnAccountItemClickListener = mOnAccountItemClickListener;
    }

    //Конструктор
    public AdapterAccounts(Context context, OnStartDragListener dragStartListener) {
        this.mContext = context;
        accountList = new ArrayList<>();

        mDragStartListener = dragStartListener;

        if (PreferenceManager.getDefaultSharedPreferences(context).getBoolean(FgConst.PREF_COMPACT_VIEW_MODE, false)) {
            mContextThemeWrapper = new ContextThemeWrapper(context, R.style.StyleListItemTransationsCompact);
        } else {
            mContextThemeWrapper = new ContextThemeWrapper(context, R.style.StyleListItemTransationsNormal);
        }
    }

    @Override
    public long getItemId(int position) {
        return accountList.get(position).getID();
    }

    @Override
    public int getItemViewType(int position) {
        return 0;
    }


    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent,
                                                      int viewType) {
        RecyclerView.ViewHolder vh;
        View v = LayoutInflater.from(mContextThemeWrapper).inflate(
                R.layout.list_item_account_2, parent, false);

        vh = new AccountViewHolder(v);

        return vh;
    }

    @Override
    public void onBindViewHolder(final RecyclerView.ViewHolder viewHolder, int position) {

        Account account = accountList.get(position);

        AccountViewHolder avh = (AccountViewHolder) viewHolder;

        ImageView icon = avh.imageViewIcon;

        avh.itemView.setLongClickable(true);

        avh.textViewName.setText(account.getName());

        String[] accountTypes = mContext.getResources().getStringArray(R.array.account_types);
        String accountType = accountTypes[account.getAccountType().ordinal()];

        Cabbage cabbage = AccountManager.getCabbage(account, mContext);
        avh.textViewType.setText(String.format("%s (%s)", accountType, cabbage.getCode()));

        CabbageFormatter cabbageFormatter = new CabbageFormatter(cabbage);
        Boolean showInEx = PreferenceManager.getDefaultSharedPreferences(mContext).getBoolean(FgConst.PREF_SHOW_INCOME_EXPENSE_FOR_ACCOUNTS, true);
        if (showInEx) {
            avh.textViewIncome.setText(cabbageFormatter.format(account.getIncome()));
            avh.textViewOutcome.setText(cabbageFormatter.format(account.getExpense()));
            avh.textViewIncome.setVisibility(View.VISIBLE);
            avh.textViewOutcome.setVisibility(View.VISIBLE);
        } else {
            avh.textViewIncome.setVisibility(View.GONE);
            avh.textViewOutcome.setVisibility(View.GONE);
        }
        avh.textViewCurBalance.setText(cabbageFormatter.format(account.getCurrentBalance()));

        int compareToZero = account.getCurrentBalance().setScale(cabbage.getDecimalCount(), RoundingMode.HALF_EVEN).compareTo(BigDecimal.ZERO);

        switch (compareToZero) {
            case -1:
                avh.textViewCurBalance.setTextColor(ContextCompat.getColor(mContext, R.color.negative_color));
                break;
            case 0:
                avh.textViewCurBalance.setTextColor(ContextCompat.getColor(mContext, R.color.light_gray_text));
                break;
            case 1:
                avh.textViewCurBalance.setTextColor(ContextCompat.getColor(mContext, R.color.positive_color));
                break;
        }

        if (mDragMode) {
            icon.setImageDrawable(mContext.getDrawable(R.drawable.ic_drag));
            icon.setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    if (MotionEventCompat.getActionMasked(event) == MotionEvent.ACTION_DOWN) {
                        mDragStartListener.onStartDrag(viewHolder);
                        return true;
                    } else {
                        return false;
                    }
                }
            });
        } else {
            icon.setImageDrawable(IconGenerator.getAccountIcon(account.getAccountType(), compareToZero, account.getIsClosed(), mContext));
        }

        if (position == accountList.size() - 1) {
            avh.mSpaceBottom.setVisibility(View.GONE);
            avh.mSpaceBottomFinal.setVisibility(View.VISIBLE);
        } else {
            avh.mSpaceBottom.setVisibility(View.VISIBLE);
            avh.mSpaceBottomFinal.setVisibility(View.GONE);
        }

        avh.account = account;

    }

    @Override
    public int getItemCount() {
        return accountList.size();
    }

    @Override
    public void onDrop(RecyclerView.ViewHolder vh, int fromPosition, int toPosition) {

    }

    @Override
    public boolean onItemMove(RecyclerView.ViewHolder vh, int fromPosition, int toPosition) {
        accountList.add(toPosition, accountList.remove(fromPosition));
        List<Pair<Long, Integer>> pairs = new ArrayList<>();
        int i = 0;
        for (Account account : accountList) {
            pairs.add(new Pair<>(account.getID(), i++));
        }
        AccountsDAO.getInstance(mContext).updateOrder(pairs);
        PreferenceManager.getDefaultSharedPreferences(mContext)
                .edit()
                .putInt("accounts_sort_type", BaseModel.SORT_BY_ACCOUNT_CUSTOM)
                .apply();
        return true;
    }

    @Override
    public void onItemDismiss(int position) {

    }

    class AccountViewHolder extends RecyclerView.ViewHolder {

        @BindView(R.id.imageViewAccountIcon)
        ImageView imageViewIcon;
        @BindView(R.id.textViewAccountName)
        TextView textViewName;
        @BindView(R.id.textViewAccountType)
        TextView textViewType;
        @BindView(R.id.textViewAccountCurBalance)
        TextView textViewCurBalance;
        @BindView(R.id.textViewIncome)
        TextView textViewIncome;
        @BindView(R.id.textViewOutcome)
        TextView textViewOutcome;
        @BindView(R.id.spaceBottom)
        FrameLayout mSpaceBottom;
        @BindView(R.id.spaceBottomFinal)
        FrameLayout mSpaceBottomFinal;

        public Account account;

        AccountViewHolder(View v) {
            super(v);

            ButterKnife.bind(this, v);

            v.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v1) {
                    if (mOnAccountItemClickListener != null) {
                        mOnAccountItemClickListener.OnAccountItemClick(account);
                    }

                }
            });
        }
    }

    public interface OnAccountItemClickListener {
        void OnAccountItemClick(Account account);
    }
}
