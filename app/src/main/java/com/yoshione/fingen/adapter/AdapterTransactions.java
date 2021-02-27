package com.yoshione.fingen.adapter;

import android.annotation.SuppressLint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.l4digital.fastscroll.FastScroller;
import com.yoshione.fingen.R;
import com.yoshione.fingen.adapter.viewholders.TransactionViewHolder;
import com.yoshione.fingen.adapter.viewholders.TransactionViewHolderParams;
import com.yoshione.fingen.interfaces.IAbstractModel;
import com.yoshione.fingen.interfaces.ILoadMoreFinish;
import com.yoshione.fingen.interfaces.IOnLoadMore;
import com.yoshione.fingen.interfaces.ITransactionClickListener;
import com.yoshione.fingen.interfaces.ITransactionItemEventListener;
import com.yoshione.fingen.model.Transaction;
import com.yoshione.fingen.utils.DateTimeFormatter;
import com.yoshione.fingen.widgets.ToolbarActivity;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import ca.barrenechea.widget.recyclerview.decoration.StickyHeaderAdapter;

/**
 * Created by slv on 10.09.2015.
 * a
 */
public class AdapterTransactions extends RecyclerView.Adapter implements FastScroller.SectionIndexer,
        StickyHeaderAdapter<AdapterTransactions.HeaderViewHolder>, ITransactionClickListener {
//    private static final String TAG = "AdapterTransactions";

    private int lastYear;
    private int lastDay;
    private Date lastDate;
    private final ArrayList<Transaction> transactionList;
    private final List<String> headerList;
    private IOnLoadMore mOnLoadMore;
    private ITransactionItemEventListener mTransactionItemEventListener;
    private ToolbarActivity mActivity;

    public TransactionViewHolderParams getParams() {
        return mParams;
    }

    private TransactionViewHolderParams mParams;

    public void setSearchString(String searchString) {
        mParams.mSearchString = searchString;
    }

    //Конструктор
    @SuppressLint("UseSparseArrays")
    public AdapterTransactions(IOnLoadMore onLoadMore, ITransactionItemEventListener transactionItemEventListener, ToolbarActivity activity) {
        mActivity = activity;

        setHasStableIds(true);

        mTransactionItemEventListener = transactionItemEventListener;
        mOnLoadMore = onLoadMore;
        transactionList = new ArrayList<>();
        headerList = new ArrayList<>();

        mParams = new TransactionViewHolderParams(activity);
        mParams.mShowDateInsteadOfRunningBalance = false;
    }

    private void loadMore(final int numberItems, final ILoadMoreFinish loadMoreFinish) {
        mOnLoadMore.loadMore(numberItems, loadMoreFinish);
    }

    public void clearTransactionList() {
        transactionList.clear();
        headerList.clear();
    }

    public int getTransactionListSize() {
        return transactionList.size();
    }

    public int getItemIndexByID(long itemID) {
        for (int i = 0; i < getTransactionListSize(); i++) {
            if (transactionList.get(i).getID() == itemID) {
                return i;
            }
        }
        return -1;
    }

    public void addTransactions(List<Transaction> input, boolean clearLists) {
        synchronized (headerList) {
            if (clearLists) {
                transactionList.clear();
                headerList.clear();
            }

            Transaction transaction;
            DateTimeFormatter dateTimeFormatter = DateTimeFormatter.getInstance(mActivity);

            if (input.size() == 0) {
                headerList.add(dateTimeFormatter.getDateLongStringWithDayOfWeekName(new Date()));
                return;
            }

            int curYear;
            int curDay;
            Calendar c = Calendar.getInstance();

            boolean first;
            if (transactionList.size() == 0) {
                headerList.add(dateTimeFormatter.getDateLongStringWithDayOfWeekName(input.get(0).getDateTime()));
                transaction = input.get(0);
                transaction.headerPosition = 0;
                transaction.setDayFirst(true);
                transaction.setDayLast(input.size() == 1);
                transactionList.add(transaction);
                c.setTime(transaction.getDateTime());
                first = true;
                lastYear = c.get(Calendar.YEAR);
                lastDay = c.get(Calendar.DAY_OF_YEAR);
            } else {
                first = false;
            }
            for (Transaction tr : input) {
                if (!first) {
                    c.setTime(tr.getDateTime());
                    curYear = c.get(Calendar.YEAR);
                    curDay = c.get(Calendar.DAY_OF_YEAR);
                    tr.setDayFirst(false);
                    tr.setDayLast(false);
                    if (curYear != lastYear || curDay != lastDay) {
                        headerList.add(dateTimeFormatter.getDateLongStringWithDayOfWeekName(tr.getDateTime()));
                        lastYear = curYear;
                        lastDay = curDay;
                        transactionList.get(transactionList.size() - 1).setDayLast(true);
                        tr.setDayFirst(true);
                    }
                    tr.headerPosition = headerList.size() - 1;
                    transactionList.add(tr);
                } else {
                    first = false;
                }
            }
        }
    }

    @Override
    public long getItemId(int position) {
        return transactionList.get(position).getID();
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mParams.mContextThemeWrapper).inflate(R.layout.list_item_transactions_2, parent, false);
        return new TransactionViewHolder(mParams, this, mActivity, view);
    }

    @Override
    public String getSectionText(int position) {
        if (position >= 0) {
            lastDate = transactionList.get(position).getDateTime();
            return mParams.mDateTimeFormatter.getDateShortString(transactionList.get(position).getDateTime());
        } else {
            if (lastDate != null) {
                return mParams.mDateTimeFormatter.getDateShortString(lastDate);
            } else {
                return mParams.mDateTimeFormatter.getDateShortString(new Date());
            }
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder viewHolder, int position) {
        Transaction transaction = transactionList.get(position);

        TransactionViewHolder tvh = (TransactionViewHolder) viewHolder;
        tvh.bindTransaction(transaction);
    }

    @Override
    public int getItemCount() {
        return transactionList.size();
    }

    @Override
    public long getHeaderId(int position) {
//        Lg.log("transactionList size == %s, position == %s", String.valueOf(transactionList.size()), String.valueOf(position));
        return transactionList.get(position).headerPosition;
    }

    @Override
    public HeaderViewHolder onCreateHeaderViewHolder(ViewGroup parent) {
        View view = LayoutInflater.from(mParams.mContextThemeWrapper).inflate(R.layout.header_item, parent, false);
        return new HeaderViewHolder(view);
    }

    @Override
    public void onBindHeaderViewHolder(HeaderViewHolder viewholder, int position) {
        viewholder.bindHeader(headerList.get(transactionList.get(position).headerPosition));
    }

    public synchronized int getSelectedCount() {
        int count = 0;
        for (Transaction transaction : transactionList) {
            if (transaction.isSelected()) {
                count++;
            }
        }
        return count;
    }

    public void selectAll() {
        loadMore(Integer.MAX_VALUE, () -> {
            for (Transaction transaction : transactionList) {
                transaction.setSelected(true);
            }
            mTransactionItemEventListener.onSelectionChange(AdapterTransactions.this.getSelectedCount());
            AdapterTransactions.this.notifyDataSetChanged();
        });
    }

    public void selectByModel(final IAbstractModel model) {
        loadMore(Integer.MAX_VALUE, () -> {
            for (Transaction transaction : transactionList) {
                switch (model.getModelType()) {
                    case IAbstractModel.MODEL_TYPE_ACCOUNT:
                        if (transaction.getAccountID() == model.getID()) {
                            transaction.setSelected(true);
                        }
                        break;
                    case IAbstractModel.MODEL_TYPE_CATEGORY:
                        if (transaction.getCategoryID() == model.getID()) {
                            transaction.setSelected(true);
                        }
                        break;
                    case IAbstractModel.MODEL_TYPE_PAYEE:
                        if (transaction.getPayeeID() == model.getID()) {
                            transaction.setSelected(true);
                        }
                        break;
                    case IAbstractModel.MODEL_TYPE_LOCATION:
                        if (transaction.getLocationID() == model.getID()) {
                            transaction.setSelected(true);
                        }
                        break;
                    case IAbstractModel.MODEL_TYPE_DEPARTMENT:
                        if (transaction.getDepartmentID() == model.getID()) {
                            transaction.setSelected(true);
                        }
                        break;
                    case IAbstractModel.MODEL_TYPE_PROJECT:
                        if (transaction.getProjectID() == model.getID()) {
                            transaction.setSelected(true);
                        }
                        break;
                }

            }
            mTransactionItemEventListener.onSelectionChange(AdapterTransactions.this.getSelectedCount());
            AdapterTransactions.this.notifyDataSetChanged();
        });
    }

    public void unselectAll() {
        for (Transaction transaction : transactionList) {
            transaction.setSelected(false);
        }
        mTransactionItemEventListener.onSelectionChange(getSelectedCount());
        notifyDataSetChanged();
    }

    public List<IAbstractModel> removeSelectedTransactions() {
        List<IAbstractModel> transactions = new ArrayList<>();
        for (int i = transactionList.size() - 1; i >= 0; i--) {
            if (transactionList.get(i).isSelected()) {
                transactions.add(transactionList.get(i));
                transactionList.remove(i);
            }
        }

        return transactions;
    }

    public ArrayList<Long> getSelectedTransactionsIDsAsLong() {
        ArrayList<Long> selectedTransactions = new ArrayList<>();
        for (Transaction transaction : transactionList) {
            if (transaction.isSelected()) {
                selectedTransactions.add(transaction.getID());
            }
        }

        return selectedTransactions;
    }

    public ArrayList<String> getSelectedTransactionsIDs() {
        ArrayList<String> selectedTransactions = new ArrayList<>();
        for (Transaction transaction : transactionList) {
            if (transaction.isSelected()) {
                selectedTransactions.add(String.valueOf(transaction.getID()));
            }
        }

        return selectedTransactions;
    }

    public ArrayList<Transaction> getSelectedTransactions() {
        ArrayList<Transaction> selectedTransactions = new ArrayList<>();
        for (Transaction transaction : transactionList) {
            if (transaction.isSelected()) {
                selectedTransactions.add(transaction);
            }
        }

        return selectedTransactions;
    }

    @Override
    public void onSelectButtonClick() {
        mTransactionItemEventListener.onSelectionChange(getSelectedCount());
    }

    @Override
    public void onTransactionItemClick(Transaction transaction) {
        mTransactionItemEventListener.onTransactionItemClick(transaction);
    }

    class HeaderViewHolder extends RecyclerView.ViewHolder {

        public Transaction transaction;
        @BindView(R.id.text)
        TextView text;

        HeaderViewHolder(View v) {
            super(v);
            ButterKnife.bind(this, v);
        }

        void bindHeader(String s) {
            text.setText(s);
        }
    }
}
