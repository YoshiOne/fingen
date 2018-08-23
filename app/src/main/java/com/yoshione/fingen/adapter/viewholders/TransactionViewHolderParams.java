package com.yoshione.fingen.adapter.viewholders;

import android.app.Activity;
import android.graphics.drawable.Drawable;
import android.preference.PreferenceManager;
import android.support.v4.content.ContextCompat;
import android.util.LongSparseArray;
import android.view.ContextThemeWrapper;

import com.yoshione.fingen.FGApplication;
import com.yoshione.fingen.FgConst;
import com.yoshione.fingen.R;
import com.yoshione.fingen.adapter.AdapterTransactions;
import com.yoshione.fingen.dao.AccountsDAO;
import com.yoshione.fingen.dao.CabbagesDAO;
import com.yoshione.fingen.dao.CategoriesDAO;
import com.yoshione.fingen.dao.DepartmentsDAO;
import com.yoshione.fingen.dao.LocationsDAO;
import com.yoshione.fingen.dao.PayeesDAO;
import com.yoshione.fingen.dao.ProjectsDAO;
import com.yoshione.fingen.model.Account;
import com.yoshione.fingen.model.Cabbage;
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

import javax.inject.Inject;

public class TransactionViewHolderParams {

    public final AmountColorizer mAmountColorizer;
    public final Drawable iconSelected;
    public final DateTimeFormatter mDateTimeFormatter;
    public final int mColorSpan;
    public final int mWhiteColor;
    public int mPositiveAmountColor;
    public int mNegativeAmountColor;
    public int mColorInactive;
    public int mColorSplit;
    public int mColorTag;
    public String mSplitStringCategory;
    public String mSplitStringProject;
    public LongSparseArray<Account> mAccountCache;
    public LongSparseArray<Payee> mPayeeCache;
    public LongSparseArray<Category> mCategoryCache;
    public LongSparseArray<Department> mDepartmentCache;
    public LongSparseArray<Project> mProjectCache;
    public LongSparseArray<Location> mLocationCache;
    public LongSparseArray<CabbageFormatter> mCabbageCache;
    public String mSearchString;
    public ContextThemeWrapper mContextThemeWrapper;
    public Float mTagTextSize;
    public boolean isTagsColored;
    public boolean mShowDateInsteadOfRunningBalance;
    @Inject
    public AccountsDAO mAccountsDAO;
    @Inject
    public DepartmentsDAO mDepartmentsDAO;
    @Inject
    public CabbagesDAO mCabbagesDAO;
    @Inject
    public PayeesDAO mPayeesDAO;
    @Inject
    public LocationsDAO mLocationsDAO;
    @Inject
    public CategoriesDAO mCategoriesDAO;
    @Inject
    public ProjectsDAO  mProjectsDAO;

    public TransactionViewHolderParams(Activity context) {
        FGApplication.getAppComponent().inject(this);
        mSearchString = "";

        mAmountColorizer = new AmountColorizer(context);
        iconSelected = ContextCompat.getDrawable(context, R.drawable.ic_check_circle_blue);

        mDateTimeFormatter = DateTimeFormatter.getInstance(context);

        mPositiveAmountColor = ContextCompat.getColor(context, R.color.positive_color);
        mNegativeAmountColor = ContextCompat.getColor(context, R.color.negative_color);
        mColorInactive = ContextCompat.getColor(context, R.color.light_gray_text);
        mColorSplit = ContextCompat.getColor(context, R.color.blue_color);
        mColorTag = ContextCompat.getColor(context, R.color.ColorAccent);
        mColorSpan = ContextCompat.getColor(context, R.color.ColorPrimary);
        mWhiteColor = ContextCompat.getColor(context, R.color.fg_white_color);
        mSplitStringCategory = context.getString(R.string.ent_split_category);
        mSplitStringProject = context.getString(R.string.ent_split_project);
        mAccountCache = new LongSparseArray<>();
        mPayeeCache = new LongSparseArray<>();
        mCategoryCache = new LongSparseArray<>();
        mDepartmentCache = new LongSparseArray<>();
        mProjectCache = new LongSparseArray<>();
        mLocationCache = new LongSparseArray<>();
        mCabbageCache = new LongSparseArray<>();

        if (PreferenceManager.getDefaultSharedPreferences(context).getBoolean(FgConst.PREF_COMPACT_VIEW_MODE, false)) {
            mContextThemeWrapper = new ContextThemeWrapper(context, R.style.StyleListItemTransationsCompact);
        } else {
            mContextThemeWrapper = new ContextThemeWrapper(context, R.style.StyleListItemTransationsNormal);
        }

        isTagsColored = PreferenceManager.getDefaultSharedPreferences(context).getBoolean(FgConst.PREF_COLORED_TAGS, true);
        mTagTextSize = ScreenUtils.PxToDp(context, context.getResources().getDimension(R.dimen.text_size_micro));
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
