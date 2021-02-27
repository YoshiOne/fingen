package com.yoshione.fingen.dao;

import android.content.Context;
import android.util.Pair;

import com.yoshione.fingen.db.IOnConflict;
import com.yoshione.fingen.interfaces.IAbstractModel;

import java.util.HashSet;
import java.util.List;

public interface AbstractDAO {
    //Todo: Transform to AbstractDAO<T>, where T is IAbstractModel

    HashSet<Long> getAllModelsIDs();

    IAbstractModel createModel(IAbstractModel model) throws Exception;

    IAbstractModel createEmptyModel() throws Exception;

    IAbstractModel createModelWithoutEvent(IAbstractModel model) throws Exception;

    IAbstractModel getModelById(long id);

    IAbstractModel getModelByName(String name) throws Exception;

    IAbstractModel getModelByFullName(String fullName) throws Exception;

    List<?> getAllModels() throws Exception;

    List<IAbstractModel> bulkDeleteModel(List<IAbstractModel> modelList, boolean resetTS);

    void deleteAllModels();

    List<IAbstractModel> bulkCreateModel(List<IAbstractModel> models, IOnConflict onConflictListener, boolean updateDependencies) throws Exception;

    void deleteModel(IAbstractModel model, boolean resetTS, Context context);

    void updateOrder(List<Pair<Long, Integer>> pairs);
}
