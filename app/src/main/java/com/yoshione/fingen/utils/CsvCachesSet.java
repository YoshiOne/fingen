package com.yoshione.fingen.utils;

import android.content.Context;

import com.yoshione.fingen.interfaces.IAbstractModel;

/**
 * Created by slv on 24.01.2017.
 */

public class CsvCachesSet {

    private CsvImportCache cabbagesCache;
    private CsvImportCache accountsCache;
    private CsvImportCache categoriesCache;
    private CsvImportCache payeesCache;
    private CsvImportCache projectsCache;
    private CsvImportCache locationsCache;
    private CsvImportCache departmentsCache;

    public CsvCachesSet(Context context) {
        cabbagesCache      = new CsvImportCache(IAbstractModel.MODEL_TYPE_CABBAGE, context);
        accountsCache      = new CsvImportCache(IAbstractModel.MODEL_TYPE_ACCOUNT, context);
        categoriesCache    = new CsvImportCache(IAbstractModel.MODEL_TYPE_CATEGORY, context);
        payeesCache        = new CsvImportCache(IAbstractModel.MODEL_TYPE_PAYEE, context);
        projectsCache      = new CsvImportCache(IAbstractModel.MODEL_TYPE_PROJECT, context);
        locationsCache     = new CsvImportCache(IAbstractModel.MODEL_TYPE_LOCATION, context);
        departmentsCache   = new CsvImportCache(IAbstractModel.MODEL_TYPE_DEPARTMENT, context);
    }

    public CsvImportCache getCabbagesCache() {
        return cabbagesCache;
    }

    public CsvImportCache getAccountsCache() {
        return accountsCache;
    }

    public CsvImportCache getCategoriesCache() {
        return categoriesCache;
    }

    public CsvImportCache getPayeesCache() {
        return payeesCache;
    }

    public CsvImportCache getProjectsCache() {
        return projectsCache;
    }

    public CsvImportCache getLocationsCache() {
        return locationsCache;
    }

    public CsvImportCache getDepartmentsCache() {
        return departmentsCache;
    }
}
