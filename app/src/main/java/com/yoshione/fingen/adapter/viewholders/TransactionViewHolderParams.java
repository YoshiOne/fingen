package com.yoshione.fingen.adapter.viewholders;

import android.app.Activity;
import android.graphics.drawable.Drawable;
import android.preference.PreferenceManager;
import android.support.v4.content.ContextCompat;
import android.view.ContextThemeWrapper;

import com.yoshione.fingen.FgConst;
import com.yoshione.fingen.R;
import com.yoshione.fingen.adapter.AdapterTransactions;
import com.yoshione.fingen.model.Account;
import com.yoshione.fingen.model.Category;
import com.yoshione.fingen.model.Department;
import com.yoshione.fingen.model.Location;
import com.yoshione.fingen.model.Payee;
import com.yoshione.fingen.model.Project;
import com.yoshione.fingen.utils.AmountColorizer;
import com.yoshione.fingen.utils.CabbageFormatter;
import com.yoshione.fingen.utils.DateTimeFormatter;
import com.yoshione.fingen.utils.ScreenUtils;

import java.util.HashMap;

public class TransactionViewHolderParams {

    public final AmountColorizer mAmountColorizer;
    public final Drawable iconSelected;
    public final DateTimeFormatter mDateTimeFormatter;
    public final int mColorSpan;
    public int mPositiveAmountColor;
    public int mNegativeAmountColor;
    public int mColorInactive;
    public int mColorSplit;
    public int mColorTag;
    public String mSplitStringCategory;
    public String mSplitStringProject;
    public HashMap<Long, Account> mAccountCache;
    public HashMap<Long, Payee> mPayeeCache;
    public HashMap<Long, Category> mCategoryCache;
    public HashMap<Long, Department> mDepartmentCache;
    public HashMap<Long, Project> mProjectCache;
    public HashMap<Long, Location> mLocationCache;
    public HashMap<Long, CabbageFormatter> mCabbageCache;
    public String mSearchString;
    public ContextThemeWrapper mContextThemeWrapper;
    public Float mTagTextSize;
    public boolean isTagsColored;
    public AdapterTransactions.OnTransactionItemEventListener mOnTransactionItemEventListener;
    public boolean mShowDateInsteadOfRunningBalance;
    public final Activity mContext;

    public TransactionViewHolderParams(Activity context) {
        mContext = context;
        mSearchString = "";

        mAmountColorizer = new AmountColorizer(context);
        iconSelected = ContextCompat.getDrawable(mContext, R.drawable.ic_check_circle_blue);

        mDateTimeFormatter = DateTimeFormatter.getInstance(context);

        mPositiveAmountColor = ContextCompat.getColor(mContext, R.color.positive_color);
        mNegativeAmountColor = ContextCompat.getColor(mContext, R.color.negative_color);
        mColorInactive = ContextCompat.getColor(mContext, R.color.light_gray_text);
        mColorSplit = ContextCompat.getColor(mContext, R.color.blue_color);
        mColorTag = ContextCompat.getColor(mContext, R.color.ColorAccent);
        mColorSpan = ContextCompat.getColor(context, R.color.ColorPrimary);
        mSplitStringCategory = mContext.getString(R.string.ent_split_category);
        mSplitStringProject = mContext.getString(R.string.ent_split_project);
        mAccountCache = new HashMap<>();
        mPayeeCache = new HashMap<>();
        mCategoryCache = new HashMap<>();
        mDepartmentCache = new HashMap<>();
        mProjectCache = new HashMap<>();
        mLocationCache = new HashMap<>();
        mCabbageCache = new HashMap<>();

        if (PreferenceManager.getDefaultSharedPreferences(context).getBoolean(FgConst.PREF_COMPACT_VIEW_MODE, false)) {
            mContextThemeWrapper = new ContextThemeWrapper(context, R.style.StyleListItemTransationsCompact);
        } else {
            mContextThemeWrapper = new ContextThemeWrapper(context, R.style.StyleListItemTransationsNormal);
        }

        isTagsColored = PreferenceManager.getDefaultSharedPreferences(context).getBoolean(FgConst.PREF_COLORED_TAGS, true);
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
}
