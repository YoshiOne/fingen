package com.yoshione.fingen.utils;

import android.content.Context;
import android.util.SparseArray;

import com.yoshione.fingen.dao.AbstractDAO;
import com.yoshione.fingen.dao.BaseDAO;
import com.yoshione.fingen.dao.CabbagesDAO;
import com.yoshione.fingen.interfaces.IAbstractModel;
import com.yoshione.fingen.model.Account;
import com.yoshione.fingen.model.Cabbage;

/**
 * Created by slv on 23.01.2017.
 */

class CsvImportCache {
    private SparseArray<IAbstractModel> mCache;
    private AbstractDAO mDAO;
    private int mModelType;

    CsvImportCache(int modelType, Context context) {
        mModelType = modelType;
        mDAO = BaseDAO.getDAO(modelType, context);
        mCache = new SparseArray<>();
    }

    void add(int key, IAbstractModel object) {
        mCache.put(key, object);
    }

    IAbstractModel getNestedModelByName(String name) throws Exception {
        IAbstractModel model = mCache.get(name.hashCode());
        if (model == null) {
            model = mDAO.getModelByFullName(name);
            if (model.getID() >= 0) {
                mCache.put(name.hashCode(), model);
            }
        }
        return model;
    }

    Account getAccountByName(String name) throws Exception {
        Account account = (Account) mCache.get(name.hashCode());
        if (account == null) {
            account = (Account) mDAO.getModelByName(name);
            if (account.getID() >= 0) {
                mCache.put(name.hashCode(), account);
            }
        }
        return account;
    }

    Cabbage getCabbageByCode(String code) throws Exception {
        Cabbage cabbage = (Cabbage) mCache.get(code.hashCode());
        if (cabbage== null) {
            cabbage = ((CabbagesDAO) mDAO).getCabbageByCode(code);
            if (cabbage.getID() >= 0) {
                mCache.put(code.hashCode(), cabbage);
            }
        }
        return cabbage;
    }
}
