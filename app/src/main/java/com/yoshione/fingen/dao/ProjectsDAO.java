package com.yoshione.fingen.dao;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Color;
import android.util.Pair;


import com.yoshione.fingen.DBHelper;
import com.yoshione.fingen.interfaces.IAbstractModel;
import com.yoshione.fingen.interfaces.IDaoInheritor;
import com.yoshione.fingen.model.Project;
import com.yoshione.fingen.utils.ColorUtils;

import java.util.HashMap;
import java.util.List;

/**
 * Created by slv on 13.08.2015.
 * +
 */
public class ProjectsDAO extends BaseDAO implements AbstractDAO, IDaoInheritor {
    private static ProjectsDAO sInstance;

    private ProjectsDAO(Context context) {
        super(context, DBHelper.T_REF_PROJECTS, IAbstractModel.MODEL_TYPE_PROJECT , DBHelper.T_REF_PROJECTS_ALL_COLUMNS);
        super.setDaoInheritor(this);
    }

    public synchronized static ProjectsDAO getInstance(Context context) {
        if (sInstance == null) {
            sInstance = new ProjectsDAO(context);
        }
        return sInstance;
    }

    public Project createProject(Project project, Context context) throws Exception {
        List<?> models = getItems(getTableName(), null,
                String.format("%s = '%s' AND %s = %s AND %s != %s",
                        DBHelper.C_REF_PROJECTS_NAME, project.getName(),
                        DBHelper.C_PARENTID, project.getParentID(),
                        DBHelper.C_ID, String.valueOf(project.getID()))
                , null, null, null);
        if (!models.isEmpty()) {
            return project;
        }
        if (project.getID() < 0 && context != null) {
            project.setColor(ColorUtils.getColor(context));
        }
        return (Project) super.createModel(project);
    }

    @Override
    public IAbstractModel cursorToModel(Cursor cursor) {
        return cursorToProject(cursor);
    }

    private Project cursorToProject(Cursor cursor) {
        Project project = new Project();
        project.setID(cursor.getLong(mColumnIndexes.get(DBHelper.C_ID)));
        project.setName(cursor.getString(mColumnIndexes.get(DBHelper.C_REF_PROJECTS_NAME)));
        project.setFullName(cursor.getString(mColumnIndexes.get(DBHelper.C_FULL_NAME)));
        project.setParentID(cursor.getLong(mColumnIndexes.get(DBHelper.C_PARENTID)));
        project.setIsActive(Boolean.valueOf(cursor.getString(mColumnIndexes.get(DBHelper.C_REF_PROJECTS_ISACTIVE))));
        project.setOrderNum(cursor.getInt(mColumnIndexes.get(DBHelper.C_ORDERNUMBER)));
        try {
            project.setColor(Color.parseColor(cursor.getString(mColumnIndexes.get(DBHelper.C_REF_PROJECTS_COLOR))));
        } catch (Exception e) {
            project.setColor(Color.TRANSPARENT);
        }


        project = (Project) DBHelper.getSyncDataFromCursor(project, cursor, mColumnIndexes);

        return project;
    }

    @Override
    public void deleteModel(IAbstractModel model, boolean resetTS, Context context) {
        ContentValues values = new ContentValues();
        //Обнуляем таблице транзакций
        values.clear();
        values.put(DBHelper.C_LOG_TRANSACTIONS_PROJECT, -1);
        TransactionsDAO.getInstance(context).bulkUpdateItem(DBHelper.C_LOG_TRANSACTIONS_PROJECT + " = " + model.getID(), values, resetTS);

        super.deleteModel(model, resetTS, context);
    }

    @SuppressWarnings("unchecked")
    public List<Project> getAllProjects() throws Exception {
        return (List<Project>) getItems(getTableName(), null, null,
                null, DBHelper.C_ORDERNUMBER + "," + DBHelper.C_REF_PROJECTS_NAME, null);
    }

    public Project getProjectByID(long id) {
        return (Project) getModelById(id);
    }

    @Override
    public List<?> getAllModels() throws Exception {
        return getAllProjects();
    }
}
