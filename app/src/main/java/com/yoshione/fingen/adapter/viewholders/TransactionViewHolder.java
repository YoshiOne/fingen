package com.yoshione.fingen.adapter.viewholders;

import android.graphics.Color;
import android.support.constraint.ConstraintLayout;
import android.support.v7.widget.RecyclerView;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.BackgroundColorSpan;
import android.text.style.ForegroundColorSpan;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import com.yoshione.fingen.R;
import com.yoshione.fingen.interfaces.ITransactionClickListener;
import com.yoshione.fingen.interfaces.IUnsubscribeOnDestroy;
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
import com.yoshione.fingen.utils.CabbageFormatter;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.HashSet;

import butterknife.BindView;
import butterknife.ButterKnife;
import eu.davidea.flipview.FlipView;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;

public class TransactionViewHolder extends RecyclerView.ViewHolder {

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

    private TransactionViewHolderParams mParams;

    private IUnsubscribeOnDestroy mUnsubscriber;

    private ITransactionClickListener mTransactionClickListener;

    public TransactionViewHolder(TransactionViewHolderParams params, ITransactionClickListener transactionClickListener, IUnsubscribeOnDestroy unsubscriber, View v) {
        super(v);
        ButterKnife.bind(this, v);
        mParams = params;
        mTransactionClickListener = transactionClickListener;
        mUnsubscriber = unsubscriber;
    }

    public void bindTransaction(final Transaction t, final HashSet<Long> filterCategory, HashSet<Long> filterProject) {
        itemView.setLongClickable(true);

        //<editor-fold desc="Get accounts data">
        Account srcAccount;
        Account destAccount;
        if (mParams.mAccountCache.indexOfKey(t.getAccountID()) >= 0) {
            srcAccount = mParams.mAccountCache.get(t.getAccountID());
        } else {
            srcAccount = mParams.mAccountsDAO.getAccountByID(t.getAccountID());
            mParams.mAccountCache.put(srcAccount.getID(), srcAccount);
        }

        if (mParams.mAccountCache.indexOfKey(t.getDestAccountID()) >= 0) {
            destAccount = mParams.mAccountCache.get(t.getDestAccountID());
        } else {
            destAccount = mParams.mAccountsDAO.getAccountByID(t.getDestAccountID());
            mParams.mAccountCache.put(destAccount.getID(), destAccount);
        }
        //</editor-fold>

        //<editor-fold desc="Account & Department">
        String text;
        Spannable spannable;
        String search = mParams.mSearchString.toLowerCase();
        if (t.getDepartmentID() < 0) {
            text = srcAccount.getName();
        } else {
            Department department;
            if (mParams.mDepartmentCache.indexOfKey(t.getDepartmentID()) >= 0) {
                department = mParams.mDepartmentCache.get(t.getDepartmentID());
            } else {
                department = mParams.mDepartmentsDAO.getDepartmentByID(t.getDepartmentID());
                mParams.mDepartmentCache.put(department.getID(), department);
            }

            text = srcAccount.getName() + "(" + department.getFullName() + ")";
        }
        if (search.isEmpty() || !text.toLowerCase().contains(search)) {
            textViewAccount.setText(text);
        } else {
            spannable = new SpannableString(text);
            spannable.setSpan(new BackgroundColorSpan(mParams.mColorSpan), text.toLowerCase().indexOf(search), text.toLowerCase().indexOf(search) + search.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            textViewAccount.setText(spannable);
        }
        //</editor-fold>

        //<editor-fold desc="Running balance">
        CabbageFormatter cabbageFormatter;
        if (mParams.mCabbageCache.indexOfKey(srcAccount.getCabbageId()) >= 0) {
            cabbageFormatter = mParams.mCabbageCache.get(srcAccount.getCabbageId());
        } else {
            Cabbage cabbage = mParams.mCabbagesDAO.getCabbageByID(srcAccount.getCabbageId());
            cabbageFormatter = new CabbageFormatter(cabbage);
            mParams.mCabbageCache.put(cabbage.getID(), cabbageFormatter);
        }

        textViewAccountBalance.setTextColor(getAmountColor(t.getFromAccountBalance()));
        mUnsubscriber.unsubscribeOnDestroy(
                cabbageFormatter.formatRx(t.getFromAccountBalance())
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(s -> textViewAccountBalance.setText(s))
        );

        CabbageFormatter destCabbageFormatter = null;
        if (t.getDestAccountID() >= 0) {
            if (mParams.mCabbageCache.indexOfKey(destAccount.getCabbageId()) >= 0) {
                destCabbageFormatter = mParams.mCabbageCache.get(destAccount.getCabbageId());
            } else {
                Cabbage cabbage = mParams.mCabbagesDAO.getCabbageByID(destAccount.getCabbageId());
                destCabbageFormatter = new CabbageFormatter(cabbage);
                mParams.mCabbageCache.put(cabbage.getID(), destCabbageFormatter);
            }

            textViewDestAccountBalance.setTextColor(getAmountColor(t.getToAccountBalance()));
            mUnsubscriber.unsubscribeOnDestroy(
                    destCabbageFormatter.formatRx(t.getToAccountBalance())
                            .subscribeOn(Schedulers.io())
                            .observeOn(AndroidSchedulers.mainThread())
                            .subscribe(s -> textViewDestAccountBalance.setText(s))
            );

            imageViewChevronRight.setVisibility(View.VISIBLE);
            textViewDestAccountBalance.setVisibility(View.VISIBLE);
        } else {
            imageViewChevronRight.setVisibility(View.GONE);
            textViewDestAccountBalance.setVisibility(View.GONE);
        }
        //</editor-fold>

        if (mParams.mShowDateInsteadOfRunningBalance) {
            textViewAccountBalance.setText(mParams.mDateTimeFormatter.getDateMediumString(t.getDateTime()));
        }

        //<editor-fold desc="DateTime">
        textViewDate.setText(mParams.mDateTimeFormatter.getTimeShortString(t.getDateTime()));
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
            if (mParams.mPayeeCache.indexOfKey(t.getPayeeID()) >= 0) {
                payee = mParams.mPayeeCache.get(t.getPayeeID());
            } else {
                payee = mParams.mPayeesDAO.getPayeeByID(t.getPayeeID());
                mParams.mPayeeCache.put(payee.getID(), payee);
            }
            String payeeFullName = payee.getFullName();
            if (t.getLocationID() < 0) {
                text = payeeFullName;
            } else {
                Location location;
                if (mParams.mLocationCache.indexOfKey(t.getLocationID()) >= 0) {
                    location = mParams.mLocationCache.get(t.getLocationID());
                } else {
                    location = mParams.mLocationsDAO.getLocationByID(t.getLocationID());
                    mParams.mLocationCache.put(location.getID(), location);
                }
                text = payeeFullName + " (" + location.getFullName() + ")";
            }
            lp.setMarginStart(0);
        }

        boolean isEmptyPayee = false;
        if (!text.isEmpty()) {
            if (search.isEmpty() || !text.toLowerCase().contains(search)) {
                textViewPayee.setText(text);
            } else {
                spannable = new SpannableString(text);
                spannable.setSpan(new BackgroundColorSpan(mParams.mColorSpan), text.toLowerCase().indexOf(search), text.toLowerCase().indexOf(search) + search.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                textViewPayee.setText(spannable);
            }
            textViewPayee.setVisibility(View.VISIBLE);
            textViewPayee.setLayoutParams(lp);
        } else {
            isEmptyPayee = true;
            textViewPayee.setVisibility(View.GONE);
        }
        //</editor-fold>

        //<editor-fold desc="Category & Project & Amount">
        Category category;
        Project project;
        boolean isSplitCategory = false;
        boolean isSplitProject = false;
        boolean sameCategory = true;
        boolean sameProject = true;
        if (t.getProductEntries().size() == 0) {
            isSplitCategory = false;
            isSplitProject = false;
        } else {
            Long lastID = null;
            for (ProductEntry entry : t.getProductEntries()) {
                if (lastID == null) {
                    lastID = entry.getCategoryID();
                } else {
                    sameCategory = sameCategory & entry.getCategoryID() == lastID;
                    lastID = entry.getCategoryID();
                }
                if (entry.getCategoryID() != t.getCategoryID()) {
                    isSplitCategory = true;
                }
            }
            lastID = null;
            for (ProductEntry entry : t.getProductEntries()) {
                if (lastID == null) {
                    lastID = entry.getProjectID();
                } else {
                    sameProject = sameProject & entry.getProjectID() == lastID;
                    lastID = entry.getProjectID();
                }
                if (entry.getProjectID() != t.getProjectID()) {
                    isSplitProject = true;
                }
            }
        }
        if (!isSplitCategory) {//это не сплит, выводим имя категории транзакции
            if (mParams.mCategoryCache.indexOfKey(t.getCategoryID()) >= 0) {
                category = mParams.mCategoryCache.get(t.getCategoryID());
            } else {
                category = mParams.mCategoriesDAO.getCategoryByID(t.getCategoryID());
                mParams.mCategoryCache.put(category.getID(), category);
            }
        } else if (!sameCategory) {//это сплит и у товаров разные категории, выводим надпись "Сплит"
            category = new Category();
            category.setID(0);
            category.setColor(mParams.mColorSplit);
            category.setFullName(mParams.mSplitStringCategory);
        } else {//это сплит, но у него все категории одинаковые, выводим название категории
            ProductEntry entry = t.getProductEntries().get(0);
            long catID;
            if (entry.getCategoryID() < 0) {
                catID = t.getCategoryID();
            } else {
                catID = entry.getCategoryID();
            }
            if (mParams.mCategoryCache.indexOfKey(catID) >= 0) {
                category = mParams.mCategoryCache.get(catID);
            } else {
                category = mParams.mCategoriesDAO.getCategoryByID(catID);
                mParams.mCategoryCache.put(category.getID(), category);
            }
        }
        if (!isSplitProject) {//это не сплит, выводим имя проекта транзакции
            if (mParams.mProjectCache.indexOfKey(t.getProjectID()) >= 0) {
                project = mParams.mProjectCache.get(t.getProjectID());
            } else {
                project = mParams.mProjectsDAO.getProjectByID(t.getProjectID());
                mParams.mProjectCache.put(project.getID(), project);
            }
        } else if (!sameProject) {//это сплит и у товаров разные проекты, выводим надпись "Сплит"
            project = new Project();
            project.setID(0);
            project.setColor(mParams.mColorSplit);
            project.setFullName(mParams.mSplitStringProject);
        } else {//это сплит, но у него все проекты одинаковые, выводим название проекта
            ProductEntry entry = t.getProductEntries().get(0);
            long catID;
            if (entry.getProjectID() < 0) {
                catID = t.getProjectID();
            } else {
                catID = entry.getProjectID();
            }
            if (mParams.mProjectCache.indexOfKey(catID) >= 0) {
                project = mParams.mProjectCache.get(catID);
            } else {
                project = mParams.mProjectsDAO.getProjectByID(catID);
                mParams.mProjectCache.put(project.getID(), project);
            }
        }
        boolean isSplitAmount = false;
        if ((isSplitCategory  || isSplitProject)
                && ((filterCategory != null && filterCategory.size() != 0) || (filterProject != null && filterProject.size() != 0))
                && t.getTransactionType() != Transaction.TRANSACTION_TYPE_TRANSFER
        ) {
            BigDecimal sum = BigDecimal.ZERO;
            boolean allAddedProducts = true;
            for (ProductEntry entry : t.getProductEntries()) {
                if (filterCategory.contains(entry.getCategoryID()) || (entry.getCategoryID() < 0 && filterCategory.contains(t.getCategoryID())) ||
                        filterProject.contains(entry.getProductID()) || (entry.getProjectID() < 0 && filterProject.contains(t.getProjectID()))) {
                    sum = sum.add(entry.getPrice().multiply(entry.getQuantity())).setScale(cabbageFormatter.getDecimalCount(), RoundingMode.HALF_UP);
                } else {
                    allAddedProducts = false;
                }
            }
            if (!allAddedProducts) {
                textViewAmount.setText(String.format("%s / %s", cabbageFormatter.format(sum), cabbageFormatter.format(t.getAmount())));
                isSplitAmount = true;
            }
        }
        if (!isSplitAmount) {
            if (t.getTransactionType() == Transaction.TRANSACTION_TYPE_TRANSFER && destCabbageFormatter != null && srcAccount.getCabbageId() != destAccount.getCabbageId()) {
                textViewAmount.setText(String.format("%s \u00BB %s", cabbageFormatter.format(t.getAmount().abs()), destCabbageFormatter.format(t.getAmount().multiply(t.getExchangeRate()).abs())));
            } else {
                mUnsubscriber.unsubscribeOnDestroy(
                        cabbageFormatter.formatRx(t.getTransactionType() == Transaction.TRANSACTION_TYPE_TRANSFER ? t.getAmount().abs() : t.getAmount())
                                .subscribeOn(Schedulers.io())
                                .observeOn(AndroidSchedulers.mainThread())
                                .subscribe(s -> textViewAmount.setText(s))
                );
            }
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
        int categoryColor;
        int projectColor;
        if (mParams.isTagsColored) {
            categoryColor = category.getColor();
            projectColor = project.getColor();
        }else {
            categoryColor = mParams.mColorTag;
            projectColor = mParams.mColorTag;
        }
        if (category.getID() >= 0) {
            mTagView.addTag(getTag(category.getFullName(), categoryColor, 100f));
        }
        if (project.getID() >= 0) {
            mTagView.addTag(getTag(project.getFullName(), projectColor, 5f));
        }
        if (mTagView.getVisibility() == View.INVISIBLE) {
            tag = new Tag(new SpannableString("T"));
            tag.tagTextSize = mParams.mTagTextSize;
            mTagView.addTag(tag);
        }
        //</editor-fold>

        //<editor-fold desc="Comment">
        if (t.getComment().isEmpty()) {
            textViewComment.setText("");
            textViewComment.setVisibility(View.GONE);
        } else {
            text = t.getComment();

            TextView textView = textViewComment;
            if (isEmptyPayee) {
                textView = textViewPayee;
                textViewComment.setVisibility(View.GONE);
            }

            if (search.isEmpty() || !text.toLowerCase().contains(search)) {
                textView.setText(text);
            } else {
                spannable = new SpannableString(text);
                spannable.setSpan(new ForegroundColorSpan(mParams.mWhiteColor), text.toLowerCase().indexOf(search), text.toLowerCase().indexOf(search) + search.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                spannable.setSpan(new BackgroundColorSpan(mParams.mColorSpan), text.toLowerCase().indexOf(search), text.toLowerCase().indexOf(search) + search.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                textView.setText(spannable);
            }
            textView.setVisibility(View.VISIBLE);
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
        flipViewIcon.getFrontImageView().setImageDrawable(mParams.mAmountColorizer.getTransactionIcon(t.getTransactionType()));
        textViewAmount.setTextColor(mParams.mAmountColorizer.getTransactionColor(t.getTransactionType()));

        flipViewIcon.getFrontImageView().setScaleType(ImageView.ScaleType.CENTER);
        flipViewIcon.getRearImageView().setImageDrawable(mParams.iconSelected);
        flipViewIcon.getRearImageView().setScaleType(ImageView.ScaleType.CENTER);
        flipViewIcon.flipSilently(t.isSelected());

        flipViewIcon.setOnClickListener(view -> {
            t.setSelected(!t.isSelected());
            flipViewIcon.flip(t.isSelected());
            if (mTransactionClickListener != null) {
                mTransactionClickListener.onSelectButtonClick();
            }
        });

        itemView.setOnClickListener(v -> {
            if (mTransactionClickListener != null) {
                mTransactionClickListener.onTransactionItemClick(t);
            }
        });
        //</editor-fold>



        spaceBottom.setVisibility(t.isDayLast() ? View.INVISIBLE : View.VISIBLE);
    }

    private Tag getTag(String text, int color, float radius) {
        String search = mParams.mSearchString.toLowerCase();
        SpannableString spannable;
        if (search.isEmpty() || !text.toLowerCase().contains(search)) {
            spannable = new SpannableString(text.toLowerCase());
        } else {
            spannable = new SpannableString(text.toLowerCase());
            spannable.setSpan(new BackgroundColorSpan(mParams.mColorSpan), text.toLowerCase().indexOf(search), text.toLowerCase().indexOf(search) + search.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        }
        Tag tag = new Tag(spannable, color);
        tag.radius = radius;
        tag.isDeletable = false;
        tag.tagTextSize = mParams.mTagTextSize;
        tag.tagTextColor = Color.WHITE;
        return tag;
    }

    private int getAmountColor(BigDecimal amount) {
        switch (amount.compareTo(BigDecimal.ZERO)) {
            case -1:
                return mParams.mNegativeAmountColor;
            case 0:
                return mParams.mColorInactive;
            case 1:
                return mParams.mPositiveAmountColor;
            default:
                return mParams.mColorInactive;
        }
    }

}
