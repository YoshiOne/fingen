package com.yoshione.fingen.adapter.viewholders;

import android.content.Context;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.style.ForegroundColorSpan;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import com.yoshione.fingen.R;
import com.yoshione.fingen.dao.CabbagesDAO;
import com.yoshione.fingen.dao.TransactionsDAO;
import com.yoshione.fingen.model.Cabbage;
import com.yoshione.fingen.model.SimpleDebt;
import com.yoshione.fingen.utils.AmountColorizer;
import com.yoshione.fingen.utils.CabbageFormatter;

import java.math.BigDecimal;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by slv on 03.03.2016.
 * VhSimpleDebt
 */
public class VhSimpleDebt extends VhModelBase {
    private final Context mContext;
    @BindView(R.id.imageViewIcon)
    ImageView mImageViewIcon;
    @BindView(R.id.textViewName)
    TextView mTextViewName;
    @BindView(R.id.textViewOweMe)
    TextView mTextViewOweMe;
    @BindView(R.id.textViewOweMeTitle)
    TextView mTextViewOweMeTitle;
    @BindView(R.id.textViewIOwe)
    TextView mTextViewIOwe;
    @BindView(R.id.textViewIOweTitle)
    TextView mTextViewIOweTitle;
    @BindView(R.id.textViewAccountTotal)
    TextView mTextViewAccountTotal;
    @BindView(R.id.textViewTotalTitle)
    TextView mTextViewTotalTitle;
    @BindView(R.id.spaceBottom)
    FrameLayout mSpaceBottom;
    @BindView(R.id.spaceBottomFinal)
    FrameLayout mSpaceBottomFinal;
    private TransactionsDAO mTransactionsDAO;
    private AmountColorizer mAmountColorizer;

    public VhSimpleDebt(View itemView, IOnItemClickListener onItemClickListener, Context context) {
        super(itemView, onItemClickListener, context);
        ButterKnife.bind(this, itemView);
        mContext = context;
        mTransactionsDAO = TransactionsDAO.getInstance(context);
        mAmountColorizer = new AmountColorizer(context);
    }

    @Override
    public void bindViewHolder(List<?> mModelList, int position) {
        super.bindViewHolder(mModelList, position);
        SimpleDebt simpleDebt = (SimpleDebt) mModelList.get(position);
        mTextViewName.setText(simpleDebt.getName());

        mTransactionsDAO.getSumForSimpleDebt(simpleDebt);

        BigDecimal sum = simpleDebt.getStartAmount().add(simpleDebt.getAmount().negate());

        Spannable text = new SpannableString("");
        Cabbage cabbage = CabbagesDAO.getInstance(mContext).getCabbageByID(simpleDebt.getCabbageID());
        CabbageFormatter cabbageFormatter = new CabbageFormatter(cabbage);
        Spannable s = new SpannableString(cabbageFormatter.format(sum.abs()));

        String suffix = String.format(" (%s %s)", mContext.getString(R.string.ent_init_amount_short), cabbageFormatter.format(simpleDebt.getStartAmount().abs()));

        mTextViewOweMe.setText(cabbageFormatter.format(simpleDebt.getOweMe().abs()));
        mTextViewIOwe.setText(cabbageFormatter.format(simpleDebt.getIOwe().abs()));

        switch (simpleDebt.getStartAmount().compareTo(BigDecimal.ZERO)) {
            case 1:
                mTextViewOweMe.setText(String.format("%s %s", mTextViewOweMe.getText(), suffix));
                break;
            case -1:
                mTextViewIOwe.setText(String.format("%s %s", mTextViewIOwe.getText(), suffix));
                break;
        }

        switch (sum.compareTo(BigDecimal.ZERO)) {
            case -1:
                s.setSpan(new ForegroundColorSpan(mAmountColorizer.getColorNegative()), 0, s.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                mImageViewIcon.setImageDrawable(mAmountColorizer.getIconExpense());
                mTextViewTotalTitle.setText(mContext.getString(R.string.ttl_total_I_owe));
                break;
            case 1:
                s.setSpan(new ForegroundColorSpan(mAmountColorizer.getColorPositive()), 0, s.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                mImageViewIcon.setImageDrawable(mAmountColorizer.getIconIncome());
                mTextViewTotalTitle.setText(mContext.getString(R.string.ttl_total_owe_me));
                break;
            case 0:
                s.setSpan(new ForegroundColorSpan(mAmountColorizer.getColorInactive()), 0, s.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                mImageViewIcon.setImageDrawable(mAmountColorizer.getIconZero());
                mTextViewTotalTitle.setText(mContext.getString(R.string.ttl_total_owe_me));
                break;
        }

        if (!simpleDebt.isActive()) {
            mImageViewIcon.setImageDrawable(mAmountColorizer.getIconClosed());
        }

        if (text.length() > 0) {
            text = new SpannableString(TextUtils.concat(text, "\n"));
        }
        text = new SpannableString(TextUtils.concat(text, s));
        mTextViewAccountTotal.setText(text);

        if (position == mModelList.size() - 1) {
            mSpaceBottom.setVisibility(View.GONE);
            mSpaceBottomFinal.setVisibility(View.VISIBLE);
        } else {
            mSpaceBottom.setVisibility(View.VISIBLE);
            mSpaceBottomFinal.setVisibility(View.GONE);
        }
    }
}
