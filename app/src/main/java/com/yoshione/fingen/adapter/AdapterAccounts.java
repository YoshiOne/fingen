package com.yoshione.fingen.adapter;

import android.app.Activity;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.constraint.ConstraintLayout;
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
import android.widget.ProgressBar;
import android.widget.TextView;

import com.yoshione.fingen.FgConst;
import com.yoshione.fingen.R;
import com.yoshione.fingen.adapter.helper.ItemTouchHelperAdapter;
import com.yoshione.fingen.adapter.helper.OnStartDragListener;
import com.yoshione.fingen.dao.AccountsDAO;
import com.yoshione.fingen.managers.AccountManager;
import com.yoshione.fingen.model.Account;
import com.yoshione.fingen.model.BaseModel;
import com.yoshione.fingen.model.Cabbage;
import com.yoshione.fingen.utils.CabbageFormatter;
import com.yoshione.fingen.utils.ColorUtils;
import com.yoshione.fingen.utils.IconGenerator;
import com.yoshione.fingen.widgets.ToolbarActivity;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;

/**
 * Created by slv on 17.09.2015.
 *
 */
public class AdapterAccounts extends RecyclerView.Adapter implements ItemTouchHelperAdapter {

    public void setAccountList(List<Account> accountList) {
        this.accountList = accountList;
    }

    private List<Account> accountList;

    private final ToolbarActivity mActivity;

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
    public AdapterAccounts(ToolbarActivity activity, OnStartDragListener dragStartListener) {
        this.mActivity = activity;
        accountList = new ArrayList<>();

        mDragStartListener = dragStartListener;

        if (PreferenceManager.getDefaultSharedPreferences(activity).getBoolean(FgConst.PREF_COMPACT_VIEW_MODE, false)) {
            mContextThemeWrapper = new ContextThemeWrapper(activity, R.style.StyleListItemTransationsCompact);
        } else {
            mContextThemeWrapper = new ContextThemeWrapper(activity, R.style.StyleListItemTransationsNormal);
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


    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent,
                                                      int viewType) {
        RecyclerView.ViewHolder vh;
        View v = LayoutInflater.from(mContextThemeWrapper).inflate(
                R.layout.list_item_account_2, parent, false);

        vh = new AccountViewHolder(v);

        return vh;
    }

    @Override
    public void onBindViewHolder(@NonNull final RecyclerView.ViewHolder viewHolder, int position) {

        Account account = accountList.get(position);

        AccountViewHolder avh = (AccountViewHolder) viewHolder;

        ImageView icon = avh.imageViewIcon;

        avh.itemView.setLongClickable(true);

        avh.textViewName.setText(account.getName());

        Cabbage cabbage = AccountManager.getCabbage(account, mActivity);
        switch (account.getAccountType()) {
            case atDebtCard:
            case atCreditCard:
                avh.textViewType.setText(String.format("%s #%04d (%s)", account.getEmitent(), account.getLast4Digits(), cabbage.getCode()));
                break;
            default:
                String[] accountTypes = mActivity.getResources().getStringArray(R.array.account_types);
                String accountType = accountTypes[account.getAccountType().ordinal()];
                avh.textViewType.setText(String.format("%s (%s)", accountType, cabbage.getCode()));
                break;
        }

        CabbageFormatter cabbageFormatter = new CabbageFormatter(cabbage);
        Boolean showInEx = PreferenceManager.getDefaultSharedPreferences(mActivity).getBoolean(FgConst.PREF_SHOW_INCOME_EXPENSE_FOR_ACCOUNTS, true);
        if (showInEx) {
            mActivity.unsubscribeOnDestroy(
                    cabbageFormatter.formatRx(account.getIncome())
                            .subscribeOn(Schedulers.io())
                            .observeOn(AndroidSchedulers.mainThread())
                            .subscribe(s -> {
                                avh.textViewIncome.setText(s);
                                avh.textViewIncome.setVisibility(View.VISIBLE);
                            }));
            mActivity.unsubscribeOnDestroy(
                    cabbageFormatter.formatRx(account.getExpense())
                            .subscribeOn(Schedulers.io())
                            .observeOn(AndroidSchedulers.mainThread())
                            .subscribe(s -> {
                                avh.textViewOutcome.setText(s);
                                avh.textViewOutcome.setVisibility(View.VISIBLE);
                            }));
            avh.textViewOutcome.setVisibility(View.VISIBLE);
        } else {
            avh.textViewIncome.setVisibility(View.GONE);
            avh.textViewOutcome.setVisibility(View.GONE);
        }

        mActivity.unsubscribeOnDestroy(
                cabbageFormatter.formatRx(account.getCurrentBalance())
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(s -> avh.textViewCurBalance.setText(s))
        );

        int compareToZero = account.getCurrentBalance().setScale(cabbage.getDecimalCount(), RoundingMode.HALF_EVEN).compareTo(BigDecimal.ZERO);

        switch (compareToZero) {
            case -1:
                avh.textViewCurBalance.setTextColor(ContextCompat.getColor(mActivity, R.color.negative_color));
                break;
            case 0:
                avh.textViewCurBalance.setTextColor(ContextCompat.getColor(mActivity, R.color.light_gray_text));
                break;
            case 1:
                avh.textViewCurBalance.setTextColor(ContextCompat.getColor(mActivity, R.color.positive_color));
                break;
        }

        if (mDragMode) {
            icon.setImageDrawable(mActivity.getDrawable(R.drawable.ic_drag));
            icon.setOnTouchListener((v, event) -> {
                if (MotionEventCompat.getActionMasked(event) == MotionEvent.ACTION_DOWN) {
                    mDragStartListener.onStartDrag(viewHolder);
                    return true;
                } else {
                    return false;
                }
            });
        } else {
            icon.setImageDrawable(IconGenerator.getAccountIcon(account.getAccountType(), compareToZero, account.getIsClosed(), mActivity));
            icon.setOnTouchListener((view, motionEvent) -> false);
        }

        if (position == accountList.size() - 1) {
            avh.mSpaceBottom.setVisibility(View.GONE);
            avh.mSpaceBottomFinal.setVisibility(View.VISIBLE);
        } else {
            avh.mSpaceBottom.setVisibility(View.VISIBLE);
            avh.mSpaceBottomFinal.setVisibility(View.GONE);
        }

        if (account.getCreditLimit().compareTo(BigDecimal.ZERO) < 0) {
            avh.mProgresBarLayout.setVisibility(View.VISIBLE);
            buildProgresBar(avh, account, cabbage, mActivity);
        } else {
            avh.mProgresBarLayout.setVisibility(View.GONE);
        }

        avh.account = account;

    }

    private void buildProgresBar(AccountViewHolder avh, Account account, Cabbage cabbage, Activity context) {
        BigDecimal restOfCredit;
        if (account.getCurrentBalance().compareTo(BigDecimal.ZERO) >= 0) {
            restOfCredit = account.getCreditLimit();
        } else if (account.getCurrentBalance().compareTo(account.getCreditLimit()) < 0) {
            restOfCredit = BigDecimal.ZERO;
        } else {
            restOfCredit = account.getCreditLimit().subtract(account.getCurrentBalance()).abs();
        }
        avh.mProgressBar.setMax(account.getCreditLimit().abs().intValue());
        avh.mProgressBar.setProgress(restOfCredit.intValue());
        CabbageFormatter cabbageFormatter = new CabbageFormatter(cabbage);
        String s = String.format("%s (%d%%)", cabbageFormatter.format(restOfCredit), account.getCreditLimitUsage());
        avh.mProgressBarTextView.setText(s);
        avh.mProgressBarTextView.setShadowLayer(3f,1.5f, 1.5f, ColorUtils.getTextInverseColor(context));
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
        AccountsDAO.getInstance(mActivity).updateOrder(pairs);
        PreferenceManager.getDefaultSharedPreferences(mActivity)
                .edit()
                .putInt("accounts_sort_type", BaseModel.SORT_BY_ACCOUNT_CUSTOM)
                .apply();
        return true;
    }

    @Override
    public void onItemSwypeRight(int position) {

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
        @BindView(R.id.progres_bar_layout)
        ConstraintLayout mProgresBarLayout;
        @BindView(R.id.progress_bar)
        ProgressBar mProgressBar;
        @BindView(R.id.progress_bar_text)
        TextView mProgressBarTextView;

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
