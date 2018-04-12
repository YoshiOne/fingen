package com.yoshione.fingen.adapter.viewholders;

import android.content.Context;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import com.yoshione.fingen.managers.AccountManager;
import com.yoshione.fingen.managers.DebtsManager;
import com.yoshione.fingen.model.Account;
import com.yoshione.fingen.model.Cabbage;
import com.yoshione.fingen.model.Credit;
import com.yoshione.fingen.R;
import com.yoshione.fingen.model.Payee;
import com.yoshione.fingen.utils.AmountColorizer;
import com.yoshione.fingen.utils.CabbageFormatter;

import java.math.BigDecimal;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by slv on 03.03.2016.
 *
 */
public class VhCredit extends VhModelBase {
    @BindView(R.id.imageViewAccountIcon)
    ImageView imageViewIcon;
    @BindView(R.id.textViewAccountName)
    TextView mTextViewName;
    @BindView(R.id.textViewAccountType)
    TextView mTextViewType;
    @BindView(R.id.textViewAccountCurBalance)
    TextView mTextViewCurBalance;
    @BindView(R.id.textViewIncome)
    TextView mTextViewIncome;
    @BindView(R.id.textViewOutcome)
    TextView mTextViewExpense;
    @BindView(R.id.spaceBottom)
    FrameLayout mSpaceBottom;
    @BindView(R.id.spaceBottomFinal)
    FrameLayout mSpaceBottomFinal;

    private final Context mContext;
    private AmountColorizer mAmountColorizer;

    public VhCredit(View itemView, IOnItemClickListener onItemClickListener, Context mContext) {
        super(itemView, onItemClickListener, mContext);
        ButterKnife.bind(this, itemView);
        this.mContext = mContext;
        mAmountColorizer = new AmountColorizer(mContext);
    }

    @Override
    public void bindViewHolder(List<?> mModelList, int position) {
        super.bindViewHolder(mModelList, position);
        Credit credit = (Credit) mModelList.get(position);
        Account account = DebtsManager.getAccount(credit, mContext);
        mTextViewName.setText(account.getName());
        Payee payee = DebtsManager.getPayee(credit, mContext);
        mTextViewType.setText(payee.getFullName());

        Cabbage cabbage = AccountManager.getCabbage(account, mContext);
        CabbageFormatter cabbageFormatter = new CabbageFormatter(cabbage);
        BigDecimal sum = DebtsManager.getSum(credit, mContext);


        mTextViewIncome.setText(cabbageFormatter.format(account.getIncome()));
        mTextViewExpense.setText(cabbageFormatter.format(account.getExpense()));

        mTextViewCurBalance.setText(cabbageFormatter.format(sum));

        switch (sum.compareTo(BigDecimal.ZERO)) {
            case -1 :
                mTextViewCurBalance.setTextColor(mAmountColorizer.getColorNegative());
                imageViewIcon.setImageDrawable(mAmountColorizer.getIconExpense());
                break;
            case  0 :
                mTextViewCurBalance.setTextColor(mAmountColorizer.getColorInactive());
                imageViewIcon.setImageDrawable(mAmountColorizer.getIconZero());
                break;
            case  1 :
                mTextViewCurBalance.setTextColor(mAmountColorizer.getColorPositive());
                imageViewIcon.setImageDrawable(mAmountColorizer.getIconIncome());
                break;
        }

        if (position == mModelList.size() - 1) {
            mSpaceBottom.setVisibility(View.GONE);
            mSpaceBottomFinal.setVisibility(View.VISIBLE);
        } else {
            mSpaceBottom.setVisibility(View.VISIBLE);
            mSpaceBottomFinal.setVisibility(View.GONE);
        }
    }
}
