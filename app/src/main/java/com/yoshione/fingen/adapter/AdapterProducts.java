package com.yoshione.fingen.adapter;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.graphics.drawable.GradientDrawable;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.BackgroundColorSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import com.yoshione.fingen.FragmentProductEntryEdit;
import com.yoshione.fingen.R;
import com.yoshione.fingen.dao.AccountsDAO;
import com.yoshione.fingen.dao.CabbagesDAO;
import com.yoshione.fingen.dao.CategoriesDAO;
import com.yoshione.fingen.dao.ProductsDAO;
import com.yoshione.fingen.dao.ProjectsDAO;
import com.yoshione.fingen.model.Account;
import com.yoshione.fingen.model.Cabbage;
import com.yoshione.fingen.model.Category;
import com.yoshione.fingen.model.ProductEntry;
import com.yoshione.fingen.model.Project;
import com.yoshione.fingen.model.Transaction;
import com.yoshione.fingen.tag.Tag;
import com.yoshione.fingen.tag.TagView;
import com.yoshione.fingen.utils.CabbageFormatter;
import com.yoshione.fingen.utils.ColorUtils;
import com.yoshione.fingen.utils.ScreenUtils;

import java.math.BigDecimal;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by slv on 02.02.2018.
 *
 */

public class AdapterProducts extends RecyclerView.Adapter {

    private static final int ADD_ITEM_ID = 0;
    private static final int VIEW_TYPE_PRODUCT = 0;
    private static final int VIEW_TYPE_ADD_PRODUCT = 1;

    private Transaction mTransaction;
    private CabbageFormatter mCabbageFormatter;
    private Activity mActivity;
    private IProductChangedListener mProductChangedListener;
    private Float mTagTextSize;

    public AdapterProducts(Transaction transaction, Activity activity, IProductChangedListener productChangedListener) {
        mActivity = activity;
        mTransaction = transaction;
        Account account = AccountsDAO.getInstance(activity).getAccountByID(transaction.getAccountID());
        Cabbage cabbage = CabbagesDAO.getInstance(activity).getCabbageByID(account.getCabbageId());
        mCabbageFormatter = new CabbageFormatter(cabbage);
        mProductChangedListener = productChangedListener;
        mTagTextSize = ScreenUtils.PxToDp(activity, activity.getResources().getDimension(R.dimen.text_size_micro));
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        RecyclerView.ViewHolder vh;
        View v;
        switch (viewType) {
            case VIEW_TYPE_PRODUCT:
            default:
                v = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item_product, parent, false);
                vh = new ProductViewHolder(v);
                break;
            case VIEW_TYPE_ADD_PRODUCT:
                v = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item_add_product, parent, false);
                vh = new AddProductViewHolder(v);
                break;
        }

        return vh;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        if (position < mTransaction.getProductEntries().size()) {
            ((ProductViewHolder) holder).bindViewHolder(holder.itemView, position, mTransaction, false,
                    mActivity, mCabbageFormatter, mTagTextSize, mProductChangedListener);
        } else if (mTransaction.getResidue().compareTo(BigDecimal.ZERO) != 0 & position == mTransaction.getProductEntries().size()) {
            ((ProductViewHolder) holder).bindViewHolder(holder.itemView, position, mTransaction, true,
                    mActivity, mCabbageFormatter, mTagTextSize, mProductChangedListener);
        } else {
            ((AddProductViewHolder) holder).bindViewHolder(holder.itemView, position, mTransaction, mActivity, mProductChangedListener);
        }
    }

    @Override
    public int getItemCount() {
        int count = mTransaction.getProductEntries().size() + 1;
        if (mTransaction.getResidue().compareTo(BigDecimal.ZERO) != 0) {
            count++;
        }
        return count;
    }

    @Override
    public long getItemId(int position) {
        if (position < mTransaction.getProductEntries().size()) {
            return mTransaction.getProductEntries().get(position).getName().hashCode();
        } else {
            return ADD_ITEM_ID;
        }
    }

    @Override
    public int getItemViewType(int position) {
        if (position < mTransaction.getProductEntries().size()) {
            return VIEW_TYPE_PRODUCT;
        } else if (mTransaction.getResidue().compareTo(BigDecimal.ZERO) != 0 & position == mTransaction.getProductEntries().size()) {
            return VIEW_TYPE_PRODUCT;
        } else {
            return VIEW_TYPE_ADD_PRODUCT;
        }
    }

    static class ProductViewHolder extends RecyclerView.ViewHolder {
        @BindView(R.id.textViewProductName)
        TextView mTextViewProductName;
        @BindView(R.id.layoutTagView)
        TagView mTagView;
        @BindView(R.id.imageButtonDeleteProduct)
        ImageButton mImageButtonDeleteProduct;
        @BindView(R.id.textViewSum)
        TextView mTextViewSum;

        ProductViewHolder(View view) {
            super(view);
            ButterKnife.bind(this, view);
        }

        void bindViewHolder(View itemView, final int position, Transaction transaction, boolean residue, final Activity activity,
                            CabbageFormatter formatter, Float tagTextSize, final IProductChangedListener listener) {
            final ProductEntry productEntry;
            if (!residue) {
                productEntry = transaction.getProductEntries().get(position);
                mTextViewProductName.setText(ProductsDAO.getInstance(activity).getProductByID(productEntry.getProductID()).getName());
            } else {
                productEntry = new ProductEntry(-1, 0, BigDecimal.ONE, transaction.getResidue(), -1, -1, transaction.getID());
                mTextViewProductName.setText(activity.getString(R.string.ttl_split_unknown));
            }

            @SuppressLint("DefaultLocale") String s = String.format("%s x %s = %s", formatter.format(productEntry.getPrice()),
                    productEntry.getQuantity().toString(),
                    formatter.format(productEntry.getPrice().multiply(productEntry.getQuantity())));
            mTextViewSum.setText(s);
            mImageButtonDeleteProduct.setVisibility(residue ? View.INVISIBLE : View.VISIBLE);
            mImageButtonDeleteProduct.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    listener.onProductDeleted(position);
                }
            });

            int color;
            switch (productEntry.getPrice().compareTo(BigDecimal.ZERO)) {
                case -1 :color = ContextCompat.getColor(activity, R.color.negative_color); break;
                case 0 : default: color = ContextCompat.getColor(activity, R.color.light_gray_text); break;
                case 1 :color = ContextCompat.getColor(activity, R.color.positive_color); break;
            }
            mTextViewSum.setTextColor(color);


            //<editor-fold desc="Category & Project">
            long categoryID = productEntry.getCategoryID() < 0 ? transaction.getCategoryID() : productEntry.getCategoryID();
            Category category = CategoriesDAO.getInstance(activity).getCategoryByID(categoryID);
            long projectID = productEntry.getProjectID() < 0 ? transaction.getProjectID() : productEntry.getProjectID();
            Project project = ProjectsDAO.getInstance(activity).getProjectByID(projectID);
            mTagView.setAlignEnd(true);
            mTagView.getTags().clear();
            mTagView.setTextPaddingTop(1);
            mTagView.setTexPaddingBottom(1);
            mTagView.setTextPaddingRight(3);
            mTagView.setTextPaddingLeft(3);
            mTagView.setLineMargin(0f);
            mTagView.setVisibility(category.getID() >= 0 | project.getID() >= 0 ? View.VISIBLE : View.GONE);
            Tag tag;
            if (category.getID() >= 0) {
                mTagView.addTag(getTag(category.getFullName(), category.getColor(), 100f, tagTextSize));
            }
            if (project.getID() >= 0) {
                mTagView.addTag(getTag(project.getFullName(), project.getColor(), 5f, tagTextSize));
            }
            if (mTagView.getVisibility() == View.GONE) {
                tag = new Tag(new SpannableString("T"));
                tag.tagTextSize = tagTextSize;
                mTagView.addTag(tag);
            }
            //</editor-fold>
//            long categoryID = productEntry.getCategoryID() < 0 ? transaction.getCategoryID() : productEntry.getCategoryID();
//            if (categoryID >= 0) {
//                mTextViewCategoryName.setVisibility(View.VISIBLE);
//                Category category = CategoriesDAO.getInstance(activity).getCategoryByID(categoryID);
//                mTextViewCategoryName.setText(category.getFullName());
//                mTextViewCategoryName.setVisibility(View.VISIBLE);
//                mTextViewCategoryName.setBackgroundResource(R.drawable.rounded_corner);
//                mTextViewCategoryName.setTextColor(ColorUtils.ContrastColor(category.getColor()));
//                ((GradientDrawable) mTextViewCategoryName.getBackground()).setColor(category.getColor());
//            } else {
//                mTextViewCategoryName.setVisibility(View.GONE);
//            }

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    FragmentProductEntryEdit alertDialog = FragmentProductEntryEdit.newInstance(activity.getString(R.string.ttl_edit_product), productEntry);
                    alertDialog.setEntryEditListener(new FragmentProductEntryEdit.IProductEntryEditListener() {
                        @Override
                        public void onProductEntryEdited(ProductEntry productEntry1) {
                            listener.onProductChanged(position, productEntry1);
                        }
                    });
                    alertDialog.show(activity.getFragmentManager(), "fragment_product_entry_edit");
                }
            });
        }
    }

    private static Tag getTag(String text, int color, float radius, float tagTextSize) {
        SpannableString spannable = new SpannableString(text.toLowerCase());

        Tag tag = new Tag(spannable, color);
        tag.radius = radius;
        tag.isDeletable = false;
        tag.tagTextSize = tagTextSize;
        tag.tagTextColor = ColorUtils.ContrastColor(color);
        return tag;
    }

    static class AddProductViewHolder extends RecyclerView.ViewHolder {
        @BindView(R.id.textViewAddProduct)
        TextView mTextViewAddProduct;

        AddProductViewHolder(View view) {
            super(view);
            ButterKnife.bind(this, view);
        }

        void bindViewHolder(View itemView, final int position, final Transaction transaction, final Activity activity, final IProductChangedListener listener) {
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    ProductEntry entry = new ProductEntry();
                    entry.setTransactionID(transaction.getID());
                    FragmentProductEntryEdit alertDialog = FragmentProductEntryEdit.newInstance(activity.getString(R.string.ttl_add_product), entry);
                    alertDialog.setEntryEditListener(new FragmentProductEntryEdit.IProductEntryEditListener() {
                        @Override
                        public void onProductEntryEdited(ProductEntry productEntry1) {
                            listener.onProductChanged(position, productEntry1);
                        }
                    });
                    alertDialog.show(activity.getFragmentManager(), "fragment_product_entry_edit");
                }
            });
        }
    }

    public interface IProductChangedListener {
        void onProductChanged(int position, ProductEntry productEntry);

        void onProductDeleted(int position);
    }

}
