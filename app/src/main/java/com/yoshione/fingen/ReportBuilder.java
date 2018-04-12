package com.yoshione.fingen;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.support.v4.util.LongSparseArray;
import android.support.v7.preference.PreferenceManager;
import android.util.Pair;

import com.yoshione.fingen.dao.AbstractDAO;
import com.yoshione.fingen.dao.BaseDAO;
import com.yoshione.fingen.dao.CabbagesDAO;
import com.yoshione.fingen.dao.TransactionsDAO;
import com.yoshione.fingen.filters.AbstractFilter;
import com.yoshione.fingen.filters.DateRangeFilter;
import com.yoshione.fingen.filters.FilterListHelper;
import com.yoshione.fingen.interfaces.IAbstractModel;
import com.yoshione.fingen.managers.TreeManager;
import com.yoshione.fingen.model.BaseModel;
import com.yoshione.fingen.model.Cabbage;
import com.yoshione.fingen.model.DateEntry;
import com.yoshione.fingen.utils.BaseNode;
import com.yoshione.fingen.utils.DateTimeFormatter;

import java.math.BigDecimal;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;

/**
 * Created by slv on 16.02.2017.
 * ReportBuilder
 */

public class ReportBuilder {

    static final int SHOW_EXPENSE = 0;
    static final int SHOW_INCOME = 1;
    static final int SHOW_INCOME_MINUS_EXPENSE = 2;
    static final int SHOW_INCOME_AND_EXPENSE = 3;
    private static final int DATA_CATEGORY = 0;
    private static final int DATA_PAYEE = 1;
    private static final int DATA_ACCOUNT = 2;
    private static final int DATA_PROJECT = 3;
    private static final int DATA_LOCATION = 4;
    private static final int DATA_DEPARTMENT = 5;
    static final int DATE_RANGE_YEAR = 0;
    static final int DATE_RANGE_Month = 1;
    static final int DATE_RANGE_Day = 2;

    private static ReportBuilder sInstance = null;
    private TransactionsDAO mTransactionsDAO;
    private CabbagesDAO mCabbagesDAO;
    private LinkedHashMap<Long, BaseNode> mEntitiesDataset;
    private LinkedHashMap<Long, List<DateEntry>> mDatesDataset;
    private String mDataCaptions[];
    private String mShowCaptions[];
    private String dateRangePatterns[];
    private String dateRangeCaptions[];
    private String dateRangeFormats[];
    private int mShowIndex = 0;
    private long mParentID = -1;
    private List<AbstractFilter> mFilterList;
    private List<Pair<Date, Date>> mDates;
    private SharedPreferences mPreferences;
    private DateTimeFormatter mDateTimeFormatter;

    public synchronized static ReportBuilder newInstance(Context context) {
        sInstance = null;
        return getInstance(context);
    }

    public synchronized static ReportBuilder getInstance(Context context) {
        if (sInstance == null) {
            sInstance = new ReportBuilder(context.getApplicationContext());
        }
        return sInstance;
    }

    private ReportBuilder(Context context) {
        mPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        mDateTimeFormatter = DateTimeFormatter.getInstance(context);

        mDataCaptions = new String[]{context.getString(R.string.ent_categories),
                context.getString(R.string.ent_payees),
                context.getString(R.string.ent_accounts),
                context.getString(R.string.ent_projects),
                context.getString(R.string.ent_locations),
                context.getString(R.string.ent_departments)};

        mShowCaptions = new String[]{
                context.getString(R.string.ent_outcome),
                context.getString(R.string.ent_income),
                context.getString(R.string.ent_income) + " - " + context.getString(R.string.ent_outcome),
                context.getString(R.string.ent_income) + " & " + context.getString(R.string.ent_outcome)
        };

        dateRangeCaptions = new String[]{
                context.getString(R.string.ent_date_range_year),
                context.getString(R.string.ent_date_range_month),
                context.getString(R.string.ent_date_range_day)
        };

        dateRangePatterns = new String[]{
                "%Y",
                "%Y-%m",
                "%Y-%m-%d"
        };

        dateRangeFormats = new String[]{
                "yyyy",
                "yyyy-MM",
                "yyyy-MM-dd"
        };
        mTransactionsDAO = TransactionsDAO.getInstance(context);
        mCabbagesDAO = CabbagesDAO.getInstance(context);
    }

    BaseNode getEntitiesDataset() {
        Cabbage cabbage;
        try {
            cabbage = getActiveCabbage();
        } catch (Exception e) {
            return new BaseNode(BaseModel.createModelByType(getModelType()), null);
        }
        if (mEntitiesDataset.containsKey(cabbage.getID())) {
            if (mParentID < 0) {
                return mEntitiesDataset.get(cabbage.getID());
            } else {
                try {
                    return mEntitiesDataset.get(cabbage.getID()).getNodeById(mParentID);
                } catch (Exception e) {
                    return new BaseNode(BaseModel.createModelByType(getModelType()), null);
                }
            }
        } else {
            return new BaseNode(BaseModel.createModelByType(getModelType()), null);
        }
    }

    List<DateEntry> getDatesDataset() {
        Cabbage cabbage;
        try {
            cabbage = getActiveCabbage();
        } catch (Exception e) {
            return new ArrayList<>();
        }
        if (mDatesDataset.containsKey(cabbage.getID())) {
            return mDatesDataset.get(cabbage.getID());
        } else {
            return new ArrayList<>();
        }
    }

    private String getDatePattern() {
        int dateRangeInd = mPreferences.getInt("report_date_range", 0);
        return dateRangePatterns[dateRangeInd];
    }

    private String getDateFormat() {
        int dateRangeInd = mPreferences.getInt("report_date_range", 0);
        return dateRangeFormats[dateRangeInd];
    }

    long getParentID() {
        return mParentID;
    }

    void setParentID(long parentID) {
        mParentID = parentID;
    }

    @SuppressWarnings("unchecked")
    Cabbage getActiveCabbage() throws Exception {
        Cabbage cabbage = mCabbagesDAO.getCabbageByID(mPreferences.getLong("report_cabbage_id", -1));
        if (cabbage.getID() < 0) {
            List<Cabbage> list;
            list = (List<Cabbage>) mCabbagesDAO.getAllModels();
            if (list.size() == 0) {
                throw new Exception();
            } else {
                cabbage = list.get(0);
                mPreferences.edit().putLong("report_cabbage_id", cabbage.getID()).apply();
            }
        }
        return cabbage;
    }

    String getActiveShowParam() {
        mShowIndex = mPreferences.getInt("report_show", 0);
        return mShowCaptions[mShowIndex];
    }

    int getActiveShowIndex() {
        return mPreferences.getInt("report_show", 0);
    }

    String[] getShowCaptions() {
        return mShowCaptions;
    }

    List<Pair<Date, Date>> getDates() {
        return mDates;
    }

    public List<AbstractFilter> getFilterList() {
        return mFilterList;
    }

    void setFilters(List<AbstractFilter> filterList) {
        mFilterList = filterList;

        mDates = new ArrayList<>();

        DateRangeFilter dateRangeFilter;
        for (AbstractFilter filter : mFilterList) {
            if (filter.getClass().equals(DateRangeFilter.class)) {
                dateRangeFilter = (DateRangeFilter) filter;
                mDates.add(new Pair<>(dateRangeFilter.getmStartDate(), dateRangeFilter.getmEndDate()));
            }
        }

        if (mDates.size() == 0) {
            mDates.add(mTransactionsDAO.getFullDateRange());
        }
    }

    @SuppressLint("SimpleDateFormat")
    DateFormat getDateFormatter() {
        int dateRangeInd = mPreferences.getInt("report_date_range", 0);
        switch (dateRangeInd) {
            case DATE_RANGE_YEAR:
                return new SimpleDateFormat("yyyy");
            case DATE_RANGE_Month:
                return new SimpleDateFormat("LLLL yyyy");
            case DATE_RANGE_Day:
                return mDateTimeFormatter.getDateMediumFormat();
            default:
                return mDateTimeFormatter.getDateMediumFormat();
        }
    }

    private int getDataInd() {
        return mPreferences.getInt("report_data", DATA_CATEGORY);
    }

    private int getDateRangeInd() {
        return mPreferences.getInt("report_date_range", DATE_RANGE_Month);
    }

    String[] getDataCaptions() {
        return mDataCaptions;
    }

    String[] getDateRangeCaptions() {
        return dateRangeCaptions;
    }

    String getActiveDataCaption() {
        return mDataCaptions[getDataInd()];
    }

    String getActiveDateRangeCaption() {
        return dateRangeCaptions[getDateRangeInd()];
    }

    public int getModelType() {
        int modelType = IAbstractModel.MODEL_TYPE_CATEGORY;

        switch (getDataInd()) {
            case DATA_CATEGORY:
                modelType = IAbstractModel.MODEL_TYPE_CATEGORY;
                break;
            case DATA_PAYEE:
                modelType = IAbstractModel.MODEL_TYPE_PAYEE;
                break;
            case DATA_ACCOUNT:
                modelType = IAbstractModel.MODEL_TYPE_ACCOUNT;
                break;
            case DATA_PROJECT:
                modelType = IAbstractModel.MODEL_TYPE_PROJECT;
                break;
            case DATA_LOCATION:
                modelType = IAbstractModel.MODEL_TYPE_LOCATION;
                break;
            case DATA_DEPARTMENT:
                modelType = IAbstractModel.MODEL_TYPE_DEPARTMENT;
                break;
        }
        return modelType;
    }

    @SuppressWarnings("unchecked")
    void loadEntitiesDataset(Context context) {
        LongSparseArray<LongSparseArray<IAbstractModel>> reportData;

        try {
            //В данных отчета могут присутсвовать не все модели, например, может не быть родителей.
            //Для решения проблемы загружаем все модели и джойним списки
            reportData = mTransactionsDAO.getEntityReport(getModelType(), new FilterListHelper(mFilterList, "", context), context);
        } catch (Exception e) {
            reportData = new LongSparseArray<>();
        }

        mEntitiesDataset = new LinkedHashMap<>();
        List<IAbstractModel> allModelsForCabbage;
        BaseNode tree;

        for(int i = 0; i < reportData.size(); i++) {
            long key = reportData.keyAt(i);
            LongSparseArray<IAbstractModel> dataForCabbage = reportData.get(key);
            if (dataForCabbage.size() > 0) {
                try {
                    allModelsForCabbage = (List<IAbstractModel>) BaseDAO.getDAO(getModelType(), context).getAllModels();
                } catch (Exception e) {
                    allModelsForCabbage = new ArrayList<>();
                }
                for (IAbstractModel model : allModelsForCabbage) {
                    model.setIncome(BigDecimal.ZERO);
                    model.setExpense(BigDecimal.ZERO);
                    if (dataForCabbage.indexOfKey(model.getID()) >= 0) {
                        model.setExpense(dataForCabbage.get(model.getID()).getExpense());
                        model.setIncome(dataForCabbage.get(model.getID()).getIncome());
                    }
                }

                tree = TreeManager.convertListToTree(allModelsForCabbage, getModelType());
                TreeManager.updateInExSumsForParents(tree);
//                for (BaseNode node : tree.getFlatChildrenList()) {
//                    if (node.getChildren().size() == 0) {
//                        TreeManager.updateInExSumsForParents(node);
//                    }
//                }
//                TreeManager.updateInExSumsForParents(tree);
                mEntitiesDataset.put(key, tree);
            } else {
                mEntitiesDataset.put(key, new BaseNode(BaseModel.createModelByType(getModelType()), null));
            }
        }

        sortDataset();
    }

    void loadDateDataset(Context context) {
        try {
            mDatesDataset = mTransactionsDAO.getCommonDateReport(getDatePattern(), getDateFormat(), new FilterListHelper(mFilterList, "", context), context);
        } catch (Exception e) {
            mDatesDataset = new LinkedHashMap<>();
        }
    }

    void sortDataset() {
        for (BaseNode tree : mEntitiesDataset.values()) {
            for (BaseNode node : tree.getFlatChildrenList()) {
                switch (mShowIndex) {
                    case SHOW_INCOME_AND_EXPENSE :
                        node.getModel().setSortType(BaseModel.SORT_BY_INCOME_AND_EXPENSE);
                        break;
                    case SHOW_INCOME_MINUS_EXPENSE:
                        node.getModel().setSortType(BaseModel.SORT_BY_INCOME_MINUS_EXPENSE);
                        break;
                    case SHOW_INCOME :
                        node.getModel().setSortType(BaseModel.SORT_BY_INCOME);
                        break;
                    case SHOW_EXPENSE :
                        node.getModel().setSortType(BaseModel.SORT_BY_EXPENSE);
                        break;
                    default:
                        node.getModel().setSortType(BaseModel.SORT_BY_EXPENSE);
                }
            }
            tree.sort();
        }
    }

    void levelUp(Context context) {
        AbstractDAO dao = BaseDAO.getDAO(getModelType(), context);
        if (dao != null) {
            IAbstractModel model = dao.getModelById(mParentID);
            mParentID = model.getParentID();
        } else {
            mParentID = -1;
        }
    }
}
