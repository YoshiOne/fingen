package com.yoshione.fingen.dao;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.util.Pair;


import com.yoshione.fingen.DBHelper;
import com.yoshione.fingen.interfaces.IDaoInheritor;
import com.yoshione.fingen.interfaces.IAbstractModel;
import com.yoshione.fingen.model.Department;

import java.util.HashMap;
import java.util.List;

/**
 * Created by slv on 13.08.2015.
 *
 */
public class DepartmentsDAO extends BaseDAO implements AbstractDAO, IDaoInheritor {
    public static final String TAG = "DepartmentDAO";
    private static DepartmentsDAO sInstance;

    public synchronized static DepartmentsDAO getInstance(Context context) {
        if (sInstance == null) {
            sInstance = new DepartmentsDAO(context);
        }
        return sInstance;
    }

    private DepartmentsDAO(Context context) {
        super(context, DBHelper.T_REF_DEPARTMENTS, IAbstractModel.MODEL_TYPE_DEPARTMENT , DBHelper.T_REF_DEPARTMENTS_ALL_COLUMNS);
        super.setDaoInheritor(this);
    }

    @Override
    public IAbstractModel cursorToModel(Cursor cursor) {
        return cursorToDepartment(cursor);
    }

    private Department cursorToDepartment(Cursor cursor) {
        Department department = new Department();
        department.setID(cursor.getLong(mColumnIndexes.get(DBHelper.C_ID)));
        department.setParentID(cursor.getLong(mColumnIndexes.get(DBHelper.C_PARENTID)));
        department.setName(cursor.getString(mColumnIndexes.get(DBHelper.C_REF_DEPARTMENTS_NAME)));
        department.setFullName(cursor.getString(mColumnIndexes.get(DBHelper.C_FULL_NAME)));
        department.setIsActive(Boolean.valueOf(cursor.getString(mColumnIndexes.get(DBHelper.C_REF_DEPARTMENTS_ISACTIVE))));
        department.setOrderNum(cursor.getInt(mColumnIndexes.get(DBHelper.C_ORDERNUMBER)));

        department = (Department) DBHelper.getSyncDataFromCursor(department, cursor, mColumnIndexes);

        return department;
    }

    @Override
    public void deleteModel(IAbstractModel model, boolean resetTS, Context context) {
        //Обнуляем таблице транзакций
        ContentValues values = new ContentValues();
        values.put(DBHelper.C_LOG_TRANSACTIONS_DEPARTMENT, -1);
        TransactionsDAO.getInstance(context).bulkUpdateItem(DBHelper.C_LOG_TRANSACTIONS_DEPARTMENT + " = " + model.getID(), values, resetTS);

        super.deleteModel(model, resetTS, context);
    }

    @SuppressWarnings("unchecked")
    public List<Department> getAllDepartments() throws Exception {
        return (List<Department>) getItems(getTableName(), null, null, null, DBHelper.C_REF_DEPARTMENTS_NAME, null);
    }

    public Department getDepartmentByID(long id) {
        return (Department) getModelById(id);
    }

    @Override
    public List<?> getAllModels() throws Exception {
        return getAllDepartments();
    }
}
