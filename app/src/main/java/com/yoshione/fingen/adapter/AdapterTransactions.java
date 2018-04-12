package com.yoshione.fingen.adapter;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.support.constraint.ConstraintLayout;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.BackgroundColorSpan;
import android.text.style.ForegroundColorSpan;
import android.view.ContextThemeWrapper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.l4digital.fastscroll.FastScroller;
import com.yoshione.fingen.FgConst;
import com.yoshione.fingen.FragmentTransactions;
import com.yoshione.fingen.R;
import com.yoshione.fingen.dao.CabbagesDAO;
import com.yoshione.fingen.dao.CategoriesDAO;
import com.yoshione.fingen.dao.DepartmentsDAO;
import com.yoshione.fingen.dao.LocationsDAO;
import com.yoshione.fingen.dao.PayeesDAO;
import com.yoshione.fingen.dao.ProjectsDAO;
import com.yoshione.fingen.interfaces.IAbstractModel;
import com.yoshione.fingen.interfaces.ILoadMoreFinish;
import com.yoshione.fingen.interfaces.IOnLoadMore;
import com.yoshione.fingen.managers.TransactionManager;
import com.yoshione.fingen.model.Account;
import com.yoshione.fingen.model.Cabbage;
import com.yoshione.fingen.model.Category;
import com.yoshione.fingen.model.Department;
import com.yoshione.fingen.model.Location;
import com.yoshione.fingen.model.Payee;
import com.yoshione.fingen.model.ProductEntry;
import com.yoshione.fingen.model.Project;
import com.yoshione.fingen.model.Transaction;
import com.yoshione.fingen.tag.Tag;
import com.yoshione.fingen.tag.TagView;
import com.yoshione.fingen.utils.AmountColorizer;
import com.yoshione.fingen.utils.CabbageFormatter;
import com.yoshione.fingen.utils.ColorUtils;
import com.yoshione.fingen.utils.DateTimeFormatter;
import com.yoshione.fingen.utils.ScreenUtils;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import ca.barrenechea.widget.recyclerview.decoration.StickyHeaderAdapter;
import eu.davidea.flipview.FlipView;

/**
 * Created by slv on 10.09.2015.
 * a
 */
public class AdapterTransactions extends RecyclerView.Adapter implements FastScroller.SectionIndexer,
        StickyHeaderAdapter<AdapterTransactions.HeaderViewHolder> {
//    private static final String TAG = "AdapterTransactions";

    protected final Handler handler;
    private final Context mContext;
    private final ArrayList<Transaction> transactionList;
    private final List<String> headerList;
    private final int visibleThreshold = 10;
    private final AmountColorizer mAmountColorizer;
    private final Drawable iconSelected;
    private final DateTimeFormatter mDateTimeFormatter;
    private final int mColorSpan;
    public volatile boolean endOfList = false;
    private int lastVisibleItem, totalItemCount;
    private volatile boolean loading;
    private OnTransactionItemEventListener mOnTransactionItemEventListener;
    private Date lastDate;
    private IOnLoadMore mOnLoadMore;
    private int mPositiveAmountColor;
    private int mNegativeAmountColor;
    private int mColorInactive;
    private int mColorSplit;
    private String mSplitStringCategory;
    private String mSplitStringProject;
    private HashMap<Long, Account> mAccountCache;
    private HashMap<Long, Payee> mPayeeCache;
    private HashMap<Long, Category> mCategoryCache;
    private HashMap<Long, Department> mDepartmentCache;
    private HashMap<Long, Project> mProjectCache;
    private HashMap<Long, Location> mLocationCache;
    private HashMap<Long, CabbageFormatter> mCabbageCache;
    private String mSearchString;
    private ContextThemeWrapper mContextThemeWrapper;
    private Float mTagTextSize;

    public void setSearchString(String searchString) {
        mSearchString = searchString;
    }

    //Конструктор
    @SuppressLint("UseSparseArrays")
    public AdapterTransactions(RecyclerView recyclerView, IOnLoadMore onLoadMore, Context context) {
        setHasStableIds(true);

        mSearchString = "";
        mContext = context;
        mOnLoadMore = onLoadMore;
        transactionList = new ArrayList<>();
        headerList = new ArrayList<>();

        LinearLayout linearLayout = new LinearLayout(context);
        linearLayout.layout(0, 0, 56, 56);
        linearLayout.setLayoutParams(new LinearLayout.LayoutParams(56, 56));
        linearLayout.setOrientation(LinearLayout.HORIZONTAL);

        mAmountColorizer = new AmountColorizer(context);
        iconSelected = ContextCompat.getDrawable(mContext, R.drawable.ic_check_circle_blue);


        mDateTimeFormatter = DateTimeFormatter.getInstance(context);
        handler = new Handler();
        mColorSpan = ContextCompat.getColor(context, R.color.ColorPrimary);

        mPositiveAmountColor = ContextCompat.getColor(mContext, R.color.positive_color);
        mNegativeAmountColor = ContextCompat.getColor(mContext, R.color.negative_color);
        mColorInactive = ContextCompat.getColor(mContext, R.color.light_gray_text);
        mColorSplit = ContextCompat.getColor(mContext, R.color.blue_color);
        mSplitStringCategory = mContext.getString(R.string.ent_split_category);
        mSplitStringProject = mContext.getString(R.string.ent_split_project);
        mAccountCache = new HashMap<>();
        mPayeeCache = new HashMap<>();
        mCategoryCache = new HashMap<>();
        mDepartmentCache = new HashMap<>();
        mProjectCache = new HashMap<>();
        mLocationCache = new HashMap<>();
        mCabbageCache = new HashMap<>();

        if (recyclerView.getLayoutManager() instanceof LinearLayoutManager) {

            final LinearLayoutManager linearLayoutManager = (LinearLayoutManager) recyclerView
                    .getLayoutManager();


            recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
                @Override
                public synchronized void onScrolled(RecyclerView recyclerView,
                                       int dx, int dy) {
                    super.onScrolled(recyclerView, dx, dy);

                    totalItemCount = linearLayoutManager.getItemCount();
                    lastVisibleItem = linearLayoutManager.findLastVisibleItemPosition();
                    if (!loading && totalItemCount <= (lastVisibleItem + visibleThreshold)) {
                        // End has been reached
                        // Do something
                        if (!endOfList) {
                            loading = true;
                            loadMore(FragmentTransactions.NUMBER_ITEMS_TO_BE_LOADED, new ILoadMoreFinish() {
                                @Override
                                public void onLoadMoreFinish() {
                                    new Handler().postDelayed(new Runnable() {
                                        @Override
                                        public void run() {
                                            notifyDataSetChanged();
                                        }
                                    }, 100);

                                }
                            });
                        }
                    }
                }
            });
        }

        if (PreferenceManager.getDefaultSharedPreferences(context).getBoolean(FgConst.PREF_COMPACT_VIEW_MODE, false)) {
            mContextThemeWrapper = new ContextThemeWrapper(context, R.style.StyleListItemTransationsCompact);
        } else {
            mContextThemeWrapper = new ContextThemeWrapper(context, R.style.StyleListItemTransationsNormal);
        }

        mTagTextSize = ScreenUtils.PxToDp(mContext, mContext.getResources().getDimension(R.dimen.text_size_micro));
    }

    public void clearCaches() {
        mAccountCache.clear();
        mPayeeCache.clear();
        mCategoryCache.clear();
        mDepartmentCache.clear();
        mProjectCache.clear();
        mLocationCache.clear();
        mCabbageCache.clear();
    }

    private void loadMore(final int numberItems, final ILoadMoreFinish loadMoreFinish) {
        mOnLoadMore.loadMore(numberItems, loadMoreFinish);
    }

    public ArrayList<Transaction> getTransactionList() {
        return transactionList;
    }

    public synchronized void setTransactionList(List<Transaction> input) {
        addTransactions(input, true);
    }

    public void addTransactions(List<Transaction> input, boolean clearLists) {
        synchronized (headerList) {
            if (clearLists) {
                transactionList.clear();
                headerList.clear();
            }

            Transaction transaction;
            DateTimeFormatter dateTimeFormatter = DateTimeFormatter.getInstance(mContext);

            if (input.size() == 0) {
                headerList.add(dateTimeFormatter.getDateLongStringWithDayOfWeekName(new Date()));
                return;
            }

            int lastYear = 0;
            int lastDay = 0;
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

    public void setmOnTransactionItemClickListener(OnTransactionItemEventListener mOnTransactionItemEventListener) {
        this.mOnTransactionItemEventListener = mOnTransactionItemEventListener;
    }

    @Override
    public long getItemId(int position) {
        return transactionList.get(position).getID();
    }


    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContextThemeWrapper).inflate(R.layout.list_item_transactions_2, parent, false);
        return new TransactionViewHolder(view);
    }

    @Override
    public String getSectionText(int position) {
        if (position >= 0) {
            lastDate = transactionList.get(position).getDateTime();
            return mDateTimeFormatter.getDateShortString(transactionList.get(position).getDateTime());
        } else {
            if (lastDate != null) {
                return mDateTimeFormatter.getDateShortString(lastDate);
            } else {
                return mDateTimeFormatter.getDateShortString(new Date());
            }
        }
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder viewHolder, int position) {
        Transaction transaction = transactionList.get(position);

        TransactionViewHolder tvh = (TransactionViewHolder) viewHolder;
        tvh.bindTransaction(transaction);
    }

    public void setLoaded() {
        loading = false;
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
        View view = LayoutInflater.from(mContextThemeWrapper).inflate(R.layout.header_item, parent, false);
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
        loadMore(Integer.MAX_VALUE, new ILoadMoreFinish() {
            @Override
            public void onLoadMoreFinish() {
                for (Transaction transaction : transactionList) {
                    transaction.setSelected(true);
                }
                mOnTransactionItemEventListener.onSelectionChange(AdapterTransactions.this.getSelectedCount());
                AdapterTransactions.this.notifyDataSetChanged();
            }
        });
    }

    public void selectByModel(final IAbstractModel model) {
        loadMore(Integer.MAX_VALUE, new ILoadMoreFinish() {
            @Override
            public void onLoadMoreFinish() {
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
                mOnTransactionItemEventListener.onSelectionChange(AdapterTransactions.this.getSelectedCount());
                AdapterTransactions.this.notifyDataSetChanged();
            }
        });
    }

    public void unselectAll() {
        for (Transaction transaction : transactionList) {
            transaction.setSelected(false);
        }
        mOnTransactionItemEventListener.onSelectionChange(getSelectedCount());
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

    public interface OnTransactionItemEventListener {
        void onTransactionItemClick(Transaction transaction);

        void onSelectionChange(int selectedCount);
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

    class TransactionViewHolder extends RecyclerView.ViewHolder {

//        public Transaction transaction;
        @BindView(R.id.textViewPayee)
        TextView textViewPayee;
        @BindView(R.id.textViewAmount)
        TextView textViewAmount;
        @BindView(R.id.textViewDate)
        TextView textViewDate;
        @BindView(R.id.textViewAccount)
        TextView textViewAccount;
        @BindView(R.id.textViewAccountBalance)
        TextView textViewAccountBalance;
        @BindView(R.id.textViewDestAccountBalance)
        TextView textViewDestAccountBalance;
        @BindView(R.id.layoutTagView)
        TagView mTagView;
        @BindView(R.id.imageViewAutoCreated)
        ImageView imageViewAutoCreated;
        @BindView(R.id.imageViewHasLocation)
        ImageView imageViewHasLocation;
        @BindView(R.id.imageViewHasQR)
        ImageView imageViewHasQR;
        @BindView(R.id.imageViewHasProducts)
        ImageView imageViewHasProducts;
        @BindView(R.id.imageViewChevronRight)
        ImageView imageViewChevronRight;
        @BindView(R.id.flipViewIcon)
        FlipView flipViewIcon;
        @BindView(R.id.textViewComment)
        TextView textViewComment;
        @BindView(R.id.spaceBottom)
        FrameLayout spaceBottom;
        @BindView(R.id.imageViewDestAccount)
        ImageView mImageViewDestAccount;

        TransactionViewHolder(View v) {
            super(v);
            ButterKnife.bind(this, v);
        }

        void bindTransaction(final Transaction t) {
            itemView.setLongClickable(true);

            //<editor-fold desc="Get accounts data">
            Account srcAccount;
            Account destAccount;
            if (mAccountCache.containsKey(t.getAccountID())) {
                srcAccount = mAccountCache.get(t.getAccountID());
            } else {
                srcAccount = TransactionManager.getSrcAccount(t, mContext);
                mAccountCache.put(srcAccount.getID(), srcAccount);
            }

            if (mAccountCache.containsKey(t.getDestAccountID())) {
                destAccount = mAccountCache.get(t.getDestAccountID());
            } else {
                destAccount = TransactionManager.getDestAccount(t, mContext);
                mAccountCache.put(destAccount.getID(), destAccount);
            }
            //</editor-fold>

            //<editor-fold desc="Account & Department">
            String text;
            Spannable spannable;
            String search = mSearchString.toLowerCase();
            if (t.getDepartmentID() < 0) {
                text = srcAccount.getName();
            } else {
                Department department;
                if (mDepartmentCache.containsKey(t.getDepartmentID())) {
                    department = mDepartmentCache.get(t.getDepartmentID());
                } else {
                    department = DepartmentsDAO.getInstance(mContext).getDepartmentByID(t.getDepartmentID());
                    mDepartmentCache.put(department.getID(), department);
                }

                text = srcAccount.getName() + "(" + department.getFullName() + ")";
            }
            if (search.isEmpty() || !text.toLowerCase().contains(search)) {
                textViewAccount.setText(text);
            } else {
                spannable = new SpannableString(text);
                spannable.setSpan(new BackgroundColorSpan(mColorSpan), text.toLowerCase().indexOf(search), text.toLowerCase().indexOf(search) + search.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                textViewAccount.setText(spannable);
            }
            //</editor-fold>

            //<editor-fold desc="Running balance & Amount">
            CabbageFormatter cabbageFormatter;
            if (mCabbageCache.containsKey(srcAccount.getCabbageId())) {
                cabbageFormatter = mCabbageCache.get(srcAccount.getCabbageId());
            } else {
                Cabbage cabbage = CabbagesDAO.getInstance(mContext).getCabbageByID(srcAccount.getCabbageId());
                cabbageFormatter = new CabbageFormatter(cabbage);
                mCabbageCache.put(cabbage.getID(), cabbageFormatter);
            }
            if (t.getTransactionType() == Transaction.TRANSACTION_TYPE_TRANSFER) {
                textViewAmount.setText(cabbageFormatter.format(t.getAmount().abs()));
            } else {
                textViewAmount.setText(cabbageFormatter.format(t.getAmount()));
            }
            String fromAmount = cabbageFormatter.format(t.getFromAccountBalance());

            textViewAccountBalance.setText(fromAmount);
            textViewAccountBalance.setTextColor(getAmountColor(t.getFromAccountBalance()));

            if (t.getDestAccountID() >= 0) {
                if (mCabbageCache.containsKey(destAccount.getCabbageId())) {
                    cabbageFormatter = mCabbageCache.get(destAccount.getCabbageId());
                } else {
                    Cabbage cabbage = CabbagesDAO.getInstance(mContext).getCabbageByID(destAccount.getCabbageId());
                    cabbageFormatter = new CabbageFormatter(cabbage);
                    mCabbageCache.put(cabbage.getID(), cabbageFormatter);
                }
                String toAmount = cabbageFormatter.format(t.getToAccountBalance());
                textViewDestAccountBalance.setText(toAmount);
                textViewDestAccountBalance.setTextColor(getAmountColor(t.getToAccountBalance()));
                imageViewChevronRight.setVisibility(View.VISIBLE);
                textViewDestAccountBalance.setVisibility(View.VISIBLE);
            } else {
                imageViewChevronRight.setVisibility(View.GONE);
                textViewDestAccountBalance.setVisibility(View.GONE);
            }
            //</editor-fold>

            //<editor-fold desc="DateTime">
            textViewDate.setText(String.format("%s", mDateTimeFormatter.getTimeShortString(t.getDateTime())));
            //</editor-fold>

            //<editor-fold desc="(Payee & Location) or DestAccount">
            ConstraintLayout.LayoutParams lp = (ConstraintLayout.LayoutParams) textViewPayee.getLayoutParams();
            if (t.getTransactionType() == Transaction.TRANSACTION_TYPE_TRANSFER) {
                mImageViewDestAccount.setVisibility(View.VISIBLE);
                text = destAccount.getName();
                lp.setMarginStart(16);
            } else {
                mImageViewDestAccount.setVisibility(View.GONE);
                Payee payee;
                if (mPayeeCache.containsKey(t.getPayeeID())) {
                    payee = mPayeeCache.get(t.getPayeeID());
                } else {
                    payee = PayeesDAO.getInstance(mContext).getPayeeByID(t.getPayeeID());
                    mPayeeCache.put(payee.getID(), payee);
                }
                String payeeFullName = payee.getFullName();
                if (t.getLocationID() < 0) {
                    text = payeeFullName;
                } else {
                    Location location;
                    if (mLocationCache.containsKey(t.getLocationID())) {
                        location = mLocationCache.get(t.getLocationID());
                    } else {
                        location = LocationsDAO.getInstance(mContext).getLocationByID(t.getLocationID());
                        mLocationCache.put(location.getID(), location);
                    }
                    text = payeeFullName + " (" + location.getFullName() + ")";
                }
                lp.setMarginStart(0);
            }

            if (!text.isEmpty()) {
                if (search.isEmpty() || !text.toLowerCase().contains(search)) {
                    textViewPayee.setText(text);
                } else {
                    spannable = new SpannableString(text);
                    spannable.setSpan(new BackgroundColorSpan(mColorSpan), text.toLowerCase().indexOf(search), text.toLowerCase().indexOf(search) + search.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                    textViewPayee.setText(spannable);
                }
                textViewPayee.setVisibility(View.VISIBLE);
                textViewPayee.setLayoutParams(lp);
            } else {
                textViewPayee.setVisibility(View.GONE);
            }
            //</editor-fold>

            //<editor-fold desc="Category & Project">
            Category category;
            Project project;
            boolean isSplitCategory = false;
            boolean isSplitProject = false;
            if (t.getProductEntries().size() == 0) {
                isSplitCategory = false;
                isSplitProject = false;
            } else {
                for (ProductEntry entry : t.getProductEntries()) {
                    if (entry.getCategoryID() != t.getCategoryID() & entry.getCategoryID() >= 0) {
                        isSplitCategory = true;
                        break;
                    }
                }
                for (ProductEntry entry : t.getProductEntries()) {
                    if (entry.getProjectID() != t.getProjectID() & entry.getCategoryID() >= 0) {
                        isSplitProject = true;
                        break;
                    }
                }
            }
            if (!isSplitCategory) {
                if (mCategoryCache.containsKey(t.getCategoryID())) {
                    category = mCategoryCache.get(t.getCategoryID());
                } else {
                    category = CategoriesDAO.getInstance(mContext).getCategoryByID(t.getCategoryID());
                    mCategoryCache.put(category.getID(), category);
                }
            } else {
                category = new Category();
                category.setID(0);
                category.setColor(mColorSplit);
                category.setFullName(mSplitStringCategory);
            }
            if (!isSplitProject) {
                if (mProjectCache.containsKey(t.getProjectID())) {
                    project = mProjectCache.get(t.getProjectID());
                } else {
                    project = ProjectsDAO.getInstance(mContext).getProjectByID(t.getProjectID());
                    mProjectCache.put(project.getID(), project);
                }
            } else {
                project = new Project();
                project.setID(0);
                project.setColor(mColorSplit);
                project.setFullName(mSplitStringProject);
            }
            mTagView.setAlignEnd(true);
            mTagView.getTags().clear();
            mTagView.setTextPaddingTop(1);
            mTagView.setTexPaddingBottom(1);
            mTagView.setTextPaddingRight(3);
            mTagView.setTextPaddingLeft(3);
            mTagView.setLineMargin(0f);
            mTagView.setVisibility(category.getID() >= 0 | project.getID() >= 0 ? View.VISIBLE : View.INVISIBLE);
            Tag tag;
            if (category.getID() >= 0) {
                mTagView.addTag(getTag(category.getFullName(), category.getColor(), 100f));
            }
            if (project.getID() >= 0) {
                mTagView.addTag(getTag(project.getFullName(), project.getColor(), 5f));
            }
            if (mTagView.getVisibility() == View.INVISIBLE) {
                tag = new Tag(new SpannableString("T"));
                tag.tagTextSize = mTagTextSize;
                mTagView.addTag(tag);
            }
            //</editor-fold>

            //<editor-fold desc="Comment">
            if (t.getComment().isEmpty()) {
                textViewComment.setText("");
                textViewComment.setVisibility(View.GONE);
            } else {
                text = t.getComment();

                if (search.isEmpty() || !text.toLowerCase().contains(search)) {
                    textViewComment.setText(text);
                } else {
                    spannable = new SpannableString(text);
                    spannable.setSpan(new ForegroundColorSpan(ContextCompat.getColor(mContext, R.color.fg_white_color)), text.toLowerCase().indexOf(search), text.toLowerCase().indexOf(search) + search.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                    spannable.setSpan(new BackgroundColorSpan(mColorSpan), text.toLowerCase().indexOf(search), text.toLowerCase().indexOf(search) + search.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                    textViewComment.setText(spannable);
                }
                textViewComment.setVisibility(View.VISIBLE);
            }
            //</editor-fold>

            //<editor-fold desc="Icon indicators">
            if (t.hasLocation()) {
                imageViewHasLocation.setVisibility(View.VISIBLE);
            } else {
                imageViewHasLocation.setVisibility(View.GONE);
            }

            if (t.isAutoCreated()) {
                imageViewAutoCreated.setVisibility(View.VISIBLE);
            } else {
                imageViewAutoCreated.setVisibility(View.GONE);
            }

            if (t.getFN() > 0) {
                imageViewHasQR.setVisibility(View.VISIBLE);
            } else {
                imageViewHasQR.setVisibility(View.GONE);
            }

            if (t.getProductEntries().size() > 0) {
                imageViewHasProducts.setVisibility(View.VISIBLE);
            } else {
                imageViewHasProducts.setVisibility(View.GONE);
            }
            //</editor-fold>

            //<editor-fold desc="Main icon (FlipView)">
            flipViewIcon.getFrontImageView().setImageDrawable(mAmountColorizer.getTransactionIcon(t.getTransactionType()));
            textViewAmount.setTextColor(mAmountColorizer.getTransactionColor(t.getTransactionType()));

            flipViewIcon.getFrontImageView().setScaleType(ImageView.ScaleType.CENTER);
            flipViewIcon.getRearImageView().setImageDrawable(iconSelected);
            flipViewIcon.getRearImageView().setScaleType(ImageView.ScaleType.CENTER);
            flipViewIcon.flipSilently(t.isSelected());

            flipViewIcon.setOnClickListener(new ImageViewIconClickListener(t));

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (mOnTransactionItemEventListener != null) {
                        mOnTransactionItemEventListener.onTransactionItemClick(t);
                    }
                }
            });
            //</editor-fold>

            spaceBottom.setVisibility(t.isDayLast() ? View.INVISIBLE : View.VISIBLE);
        }

        private Tag getTag(String text, int color, float radius) {
            String search = mSearchString.toLowerCase();
            SpannableString spannable;
            if (search.isEmpty() || !text.toLowerCase().contains(search)) {
                spannable = new SpannableString(text.toLowerCase());
            } else {
                spannable = new SpannableString(text.toLowerCase());
                spannable.setSpan(new BackgroundColorSpan(mColorSpan), text.toLowerCase().indexOf(search), text.toLowerCase().indexOf(search) + search.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            }
            Tag tag = new Tag(spannable, color);
            tag.radius = radius;
            tag.isDeletable = false;
            tag.tagTextSize = mTagTextSize;
            tag.tagTextColor = ColorUtils.ContrastColor(color);
            return tag;
        }

        private int getAmountColor(BigDecimal amount) {
            switch (amount.compareTo(BigDecimal.ZERO)) {
                case -1:
                    return mNegativeAmountColor;
                case 0:
                    return mColorInactive;
                case 1:
                    return mPositiveAmountColor;
                default:
                    return mColorInactive;
            }
        }

        private class ImageViewIconClickListener implements View.OnClickListener {
            private final Transaction mTransaction;

            ImageViewIconClickListener(Transaction transaction) {
                mTransaction = transaction;
            }

            @Override
            public void onClick(View v) {
                mTransaction.setSelected(!mTransaction.isSelected());
                flipViewIcon.flip(mTransaction.isSelected());
                mOnTransactionItemEventListener.onSelectionChange(getSelectedCount());
            }
        }

    }

}
