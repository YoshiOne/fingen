package com.yoshione.fingen.dao;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;

import com.yoshione.fingen.db.DbUtil;
import com.yoshione.fingen.interfaces.IDaoInheritor;
import com.yoshione.fingen.interfaces.IAbstractModel;
import com.yoshione.fingen.model.Department;

import java.util.List;

public class DepartmentsDAO extends BaseDAO implements AbstractDAO, IDaoInheritor {

    //<editor-fold desc="ref_Departments">
    public static final String TABLE = "ref_Departments";

    public static final String COL_IS_ACTIVE = "IsActive";

    public static final String[] ALL_COLUMNS = joinArrays(COMMON_COLUMNS, new String[]{
            COL_NAME, COL_IS_ACTIVE, COL_PARENT_ID, COL_ORDER_NUMBER, COL_FULL_NAME, COL_SEARCH_STRING
    });

    public static final String SQL_CREATE_TABLE = "CREATE TABLE " + TABLE + " ("
            + COMMON_FIELDS +       ", "
            + COL_NAME +            " TEXT NOT NULL, "
            + COL_IS_ACTIVE +       " INTEGER NOT NULL, "
            + COL_PARENT_ID +       " INTEGER REFERENCES [" + TABLE + "]([" + COL_ID + "]) ON DELETE SET NULL ON UPDATE CASCADE, "
            + COL_ORDER_NUMBER +    " INTEGER, "
            + COL_FULL_NAME +       " TEXT, "
            + COL_SEARCH_STRING +   " TEXT, "
            + "UNIQUE (" + COL_NAME + ", " + COL_PARENT_ID + ", " + COL_SYNC_DELETED + ") ON CONFLICT ABORT);";
    //</editor-fold>

    private static DepartmentsDAO sInstance;

    public synchronized static DepartmentsDAO getInstance(Context context) {
        if (sInstance == null) {
            sInstance = new DepartmentsDAO(context);
        }
        return sInstance;
    }

    private DepartmentsDAO(Context context) {
        super(context, TABLE, ALL_COLUMNS);
        super.setDaoInheritor(this);
    }

    @Override
    public IAbstractModel createEmptyModel() {
        return new Department();
    }

    @Override
    public IAbstractModel cursorToModel(Cursor cursor) {
        return cursorToDepartment(cursor);
    }

    private Department cursorToDepartment(Cursor cursor) {
        Department department = new Department();
        department.setID(DbUtil.getLong(cursor, COL_ID));
        department.setParentID(DbUtil.getLong(cursor, COL_PARENT_ID));
        department.setName(DbUtil.getString(cursor, COL_NAME));
        department.setFullName(DbUtil.getString(cursor, COL_FULL_NAME));
        department.setIsActive(DbUtil.getBoolean(cursor, COL_IS_ACTIVE));
        department.setOrderNum(DbUtil.getInt(cursor, COL_ORDER_NUMBER));

        return department;
    }

    @Override
    public void deleteModel(IAbstractModel model, boolean resetTS, Context context) {
        //Обнуляем таблице транзакций
        ContentValues values = new ContentValues();
        values.put(TransactionsDAO.COL_DEPARTMENT, -1);
        TransactionsDAO.getInstance(context).bulkUpdateItem(TransactionsDAO.COL_DEPARTMENT + " = " + model.getID(), values, resetTS);

        super.deleteModel(model, resetTS, context);
    }

    @SuppressWarnings("unchecked")
    public List<Department> getAllDepartments() {
        return (List<Department>) getItems(getTableName(), null, null, null, COL_NAME, null);
    }

    public Department getDepartmentByID(long id) {
        return (Department) getModelById(id);
    }

    @Override
    public List<?> getAllModels() {
        return getAllDepartments();
    }
}
