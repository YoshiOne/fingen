/*
 * Copyright (c) 2015.
 */

package com.yoshione.fingen.dao;

import android.content.Context;
import android.util.Pair;


import com.yoshione.fingen.db.IOnConflict;
import com.yoshione.fingen.interfaces.IAbstractModel;

import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

/**
 * Created by slv on 07.12.2015.
 * *
 */
public interface AbstractDAO {

    HashSet<Long> getAllModelsIDs();

    IAbstractModel createModel(IAbstractModel model) throws Exception;

    IAbstractModel createModelWithoutEvent(IAbstractModel model) throws Exception;

    IAbstractModel getModelById(long id);

    IAbstractModel getModelByName(String name) throws Exception;

    IAbstractModel getModelByFullName(String fullName) throws Exception;

    List<?> getAllModels() throws Exception;

    List<IAbstractModel> bulkDeleteModel(List<IAbstractModel> modelList, boolean resetTS);

    List<IAbstractModel> getModelsByIDs(List<Long> idList);

    long getIDByFBID(String FBID);

    long getLastTS();

    void deleteAllModels();

    List<IAbstractModel> bulkCreateModel(List<IAbstractModel> models, IOnConflict onConflictListener, boolean updateDependencies) throws Exception;

    void deleteModel(IAbstractModel model, boolean resetTS, Context context);

    void updateOrder(List<Pair<Long, Integer>> pairs);
}
