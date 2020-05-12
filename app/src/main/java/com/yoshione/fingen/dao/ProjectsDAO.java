package com.yoshione.fingen.dao;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Color;

import com.yoshione.fingen.db.DbUtil;
import com.yoshione.fingen.interfaces.IAbstractModel;
import com.yoshione.fingen.interfaces.IDaoInheritor;
import com.yoshione.fingen.model.Project;
import com.yoshione.fingen.utils.ColorUtils;

import java.util.List;

public class ProjectsDAO extends BaseDAO implements AbstractDAO, IDaoInheritor {

    //<editor-fold desc="ref_Projects">
    public static final String TABLE = "ref_Projects";

    public static final String COL_COLOR = "Color";
    public static final String COL_IS_ACTIVE = "IsActive";

    public static final String[] ALL_COLUMNS = joinArrays(COMMON_COLUMNS, new String[]{
            COL_NAME, COL_COLOR, COL_IS_ACTIVE, COL_PARENT_ID, COL_ORDER_NUMBER, COL_FULL_NAME, COL_SEARCH_STRING
    });

    public static final String SQL_CREATE_TABLE = "CREATE TABLE " + TABLE + " ("
            + COMMON_FIELDS +       ", "
            + COL_NAME +            " TEXT NOT NULL, "
            + COL_COLOR +           " TEXT DEFAULT '#ffffff', "
            + COL_IS_ACTIVE +       " INTEGER NOT NULL, "
            + COL_PARENT_ID +       " INTEGER REFERENCES [" + TABLE + "]([" + COL_ID + "]) ON DELETE SET NULL ON UPDATE CASCADE, "
            + COL_ORDER_NUMBER +    " INTEGER, "
            + COL_FULL_NAME +       " TEXT, "
            + COL_SEARCH_STRING +   " TEXT, "
            + "UNIQUE (" + COL_NAME + ", " + COL_PARENT_ID + ", " + COL_SYNC_DELETED + ") ON CONFLICT ABORT);";
    //</editor-fold>

    private static ProjectsDAO sInstance;

    public synchronized static ProjectsDAO getInstance(Context context) {
        if (sInstance == null) {
            sInstance = new ProjectsDAO(context);
        }
        return sInstance;
    }

    private ProjectsDAO(Context context) {
        super(context, TABLE, ALL_COLUMNS);
        super.setDaoInheritor(this);
    }

    @Override
    public IAbstractModel createEmptyModel() {
        return new Project();
    }

    public Project createProject(Project project, Context context) throws Exception {
        List<?> models = getItems(getTableName(), null,
                String.format("%s = '%s' AND %s = %s AND %s != %s",
                        COL_NAME, project.getName(),
                        COL_PARENT_ID, project.getParentID(),
                        COL_ID, String.valueOf(project.getID()))
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
        project.setID(DbUtil.getLong(cursor, COL_ID));
        project.setName(DbUtil.getString(cursor, COL_NAME));
        project.setFullName(DbUtil.getString(cursor, COL_FULL_NAME));
        project.setParentID(DbUtil.getLong(cursor, COL_PARENT_ID));
        project.setIsActive(DbUtil.getBoolean(cursor, COL_IS_ACTIVE));
        project.setOrderNum(DbUtil.getInt(cursor, COL_ORDER_NUMBER));
        try {
            project.setColor(Color.parseColor(DbUtil.getString(cursor, COL_COLOR)));
        } catch (Exception e) {
            project.setColor(Color.TRANSPARENT);
        }

        return project;
    }

    @Override
    public void deleteModel(IAbstractModel model, boolean resetTS, Context context) {
        ContentValues values = new ContentValues();
        //Обнуляем таблице транзакций
        values.clear();
        values.put(TransactionsDAO.COL_PROJECT, -1);
        TransactionsDAO.getInstance(context).bulkUpdateItem(TransactionsDAO.COL_PROJECT + " = " + model.getID(), values, resetTS);

        super.deleteModel(model, resetTS, context);
    }

    @SuppressWarnings("unchecked")
    public List<Project> getAllProjects() {
        return (List<Project>) getItems(getTableName(), null, null,
                null, COL_ORDER_NUMBER + "," + COL_NAME, null);
    }

    public Project getProjectByID(long id) {
        return (Project) getModelById(id);
    }

    @Override
    public List<?> getAllModels() {
        return getAllProjects();
    }
}
