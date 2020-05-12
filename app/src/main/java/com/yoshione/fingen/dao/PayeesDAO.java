package com.yoshione.fingen.dao;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;

import com.yoshione.fingen.db.DbUtil;
import com.yoshione.fingen.interfaces.IDaoInheritor;
import com.yoshione.fingen.interfaces.IAbstractModel;
import com.yoshione.fingen.model.Payee;
import com.yoshione.fingen.utils.SmsParser;

import java.util.List;

public class PayeesDAO extends BaseDAO implements AbstractDAO, IDaoInheritor {

    //<editor-fold desc="ref_Payees">
    public static final String TABLE = "ref_Payees";

    public static final String COL_DEF_CATEGORY = "DefCategory";

    public static final String[] ALL_COLUMNS = joinArrays(COMMON_COLUMNS, new String[]{
            COL_NAME, COL_DEF_CATEGORY, COL_PARENT_ID, COL_ORDER_NUMBER, COL_FULL_NAME, COL_SEARCH_STRING
    });

    public static final String SQL_CREATE_TABLE = "CREATE TABLE " + TABLE + " ("
            + COMMON_FIELDS +       ", "
            + COL_NAME +            " TEXT NOT NULL, "
            + COL_DEF_CATEGORY +    " INTEGER REFERENCES [" + CategoriesDAO.TABLE + "]([" + COL_ID + "]) ON DELETE SET NULL ON UPDATE CASCADE, "
            + COL_PARENT_ID +       " INTEGER REFERENCES [" + TABLE + "]([" + COL_ID + "]) ON DELETE SET NULL ON UPDATE CASCADE, "
            + COL_ORDER_NUMBER +    " INTEGER, "
            + COL_FULL_NAME +       " TEXT, "
            + COL_SEARCH_STRING +   " TEXT, "
            + "UNIQUE (" + COL_NAME + ", " + COL_PARENT_ID + ", " + COL_SYNC_DELETED + ") ON CONFLICT ABORT);";
    //</editor-fold>

    private static PayeesDAO sInstance;

    public synchronized static PayeesDAO getInstance(Context context) {
        if (sInstance == null) {
            sInstance = new PayeesDAO(context);
        }
        return sInstance;
    }

    private PayeesDAO(Context context) {
        super(context, TABLE, ALL_COLUMNS);
        super.setDaoInheritor(this);
    }

    @Override
    public IAbstractModel createEmptyModel() {
        return new Payee();
    }

    @Override
    public IAbstractModel cursorToModel(Cursor cursor) {
        return cursorToPayee(cursor);
    }

    private Payee cursorToPayee(Cursor cursor) {
        Payee payee = new Payee();

        payee.setID(DbUtil.getLong(cursor, COL_ID));
        payee.setName(DbUtil.getString(cursor, COL_NAME));
        payee.setFullName(DbUtil.getString(cursor, COL_FULL_NAME));
        payee.setParentID(DbUtil.getLong(cursor, COL_PARENT_ID));

        payee.setOrderNum(DbUtil.getInt(cursor, COL_ORDER_NUMBER));

        payee.setDefCategoryID(!DbUtil.isNull(cursor, COL_DEF_CATEGORY) ? DbUtil.getLong(cursor, COL_DEF_CATEGORY) : -1);

        return payee;
    }

    @Override
    public void deleteModel(IAbstractModel model, boolean resetTS, Context context) {
        ContentValues values = new ContentValues();
        //Обнуляем таблице транзакций
        values.clear();
        values.put(TransactionsDAO.COL_PAYEE, -1);
        TransactionsDAO.getInstance(context).bulkUpdateItem(TransactionsDAO.COL_PAYEE + " = " + model.getID(), values, resetTS);

        //Обнуляем таблице шаблонов
        values.clear();
        values.put(TemplatesDAO.COL_PAYEE, -1);
        TemplatesDAO.getInstance(context).bulkUpdateItem(TemplatesDAO.COL_PAYEE + " = " + model.getID(), values, resetTS);

        //Обнуляем таблице долгов
        values.clear();
        values.put(CreditsDAO.COL_PAYEE, -1);
        CreditsDAO.getInstance(context).bulkUpdateItem(CreditsDAO.COL_PAYEE + " = " + model.getID(), values, resetTS);

        //Удаляем все маркеры
        SmsMarkersDAO smsMarkersDAO = SmsMarkersDAO.getInstance(context);
        smsMarkersDAO.bulkDeleteModel(smsMarkersDAO.getModels(String.format("%s = %s AND %s = %s",
                SmsMarkersDAO.COL_TYPE, String.valueOf(SmsParser.MARKER_TYPE_PAYEE),
                SmsMarkersDAO.COL_OBJECT, String.valueOf(model.getID()))), resetTS);

        super.deleteModel(model, resetTS, context);
    }

    @SuppressWarnings("unchecked")
    public List<Payee> getAllPayees() throws Exception {
        return (List<Payee>) getItems(getTableName(), null, null, null,
                COL_ORDER_NUMBER + "," + COL_NAME, null);
    }

    public Payee getPayeeByID(long id) {
        return (Payee) getModelById(id);
    }

    @Override
    public List<?> getAllModels() throws Exception {
        return getAllPayees();
    }
}
