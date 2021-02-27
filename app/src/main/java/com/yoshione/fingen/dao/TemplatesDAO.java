package com.yoshione.fingen.dao;

import android.content.Context;
import android.database.Cursor;

import com.yoshione.fingen.db.DbUtil;
import com.yoshione.fingen.interfaces.IDaoInheritor;
import com.yoshione.fingen.interfaces.IAbstractModel;
import com.yoshione.fingen.model.Template;
import com.yoshione.fingen.model.Transaction;

import java.math.BigDecimal;
import java.util.List;

public class TemplatesDAO extends BaseDAO implements AbstractDAO, IDaoInheritor {

    //<editor-fold desc="log_Templates">
    public static final String TABLE = "log_Templates";

    public static final String COL_SRC_ACCOUNT = "SrcAccount";
    public static final String COL_PAYEE = "Payee";
    public static final String COL_CATEGORY = "Category";
    public static final String COL_AMOUNT = "Amount";
    public static final String COL_PROJECT = "Project";
    public static final String COL_DEPARTMENT = "Department";
    public static final String COL_LOCATION = "Location";
    public static final String COL_DEST_ACCOUNT = "DestAccount";
    public static final String COL_EXCHANGE_RATE = "ExchangeRate";
    public static final String COL_COMMENT = "Comment";
    public static final String COL_TYPE = "Type";

    public static final String[] ALL_COLUMNS = joinArrays(COMMON_COLUMNS, new String[]{
            COL_SRC_ACCOUNT, COL_PAYEE, COL_CATEGORY,
            COL_AMOUNT, COL_PROJECT, COL_DEPARTMENT,
            COL_LOCATION, COL_NAME, COL_DEST_ACCOUNT,
            COL_EXCHANGE_RATE, COL_TYPE, COL_COMMENT,
            COL_SEARCH_STRING
    });

    public static final String SQL_CREATE_TABLE = "CREATE TABLE " + TABLE + " ("
            + COMMON_FIELDS +       ", "
            + COL_SRC_ACCOUNT +     " INTEGER NOT NULL REFERENCES [" + AccountsDAO.TABLE + "]([" + COL_ID + "]) ON DELETE CASCADE ON UPDATE CASCADE, "
            + COL_PAYEE +           " INTEGER REFERENCES [" + PayeesDAO.TABLE + "]([" + COL_ID + "]) ON DELETE SET NULL ON UPDATE CASCADE, "
            + COL_CATEGORY +        " INTEGER REFERENCES [" + CategoriesDAO.TABLE + "]([" + COL_ID + "]) ON DELETE SET NULL ON UPDATE CASCADE, "
            + COL_AMOUNT +          " REAL NOT NULL, "
            + COL_PROJECT +         " INTEGER REFERENCES [" + ProjectsDAO.TABLE + "]([" + COL_ID + "]) ON DELETE SET NULL ON UPDATE CASCADE, "
            + COL_DEPARTMENT +      " INTEGER REFERENCES [" + DepartmentsDAO.TABLE + "]([" + COL_ID + "]) ON DELETE SET NULL ON UPDATE CASCADE, "
            + COL_LOCATION +        " INTEGER REFERENCES [" + LocationsDAO.TABLE + "]([" + COL_ID + "]) ON DELETE SET NULL ON UPDATE CASCADE, "
            + COL_NAME +            " TEXT NOT NULL, "
            + COL_DEST_ACCOUNT +    " INTEGER NOT NULL REFERENCES [" + AccountsDAO.TABLE + "]([" + COL_ID + "]) ON DELETE CASCADE ON UPDATE CASCADE, "
            + COL_EXCHANGE_RATE +   " REAL NOT NULL, "
            + COL_COMMENT +         " TEXT, "
            + COL_SEARCH_STRING +   " TEXT, "
            + COL_TYPE +            " INTEGER NOT NULL);";
    //</editor-fold>

    private static TemplatesDAO sInstance;

    public synchronized static TemplatesDAO getInstance(Context context) {
        if (sInstance == null) {
            sInstance = new TemplatesDAO(context);
        }
        return sInstance;
    }

    private TemplatesDAO(Context context) {
        super(context, TABLE, ALL_COLUMNS);
        super.setDaoInheritor(this);
    }

    @Override
    public IAbstractModel createEmptyModel() {
        return new Template();
    }

    @Override
    public IAbstractModel cursorToModel(Cursor cursor) {
        return cursorToTemplate(cursor);
    }

    private Template cursorToTemplate(Cursor cursor) {
        Template template = new Template();

        template.setID(DbUtil.getLong(cursor, COL_ID));
        template.setName(DbUtil.getString(cursor, COL_NAME));
        template.setAmount(new BigDecimal(DbUtil.getDouble(cursor, COL_AMOUNT)));
        template.setAccountID(DbUtil.getLong(cursor, COL_SRC_ACCOUNT));
        template.setDestAccountID(DbUtil.getLong(cursor, COL_DEST_ACCOUNT));
        template.setPayeeID(!DbUtil.isNull(cursor, COL_PAYEE) ? DbUtil.getLong(cursor, COL_PAYEE) : -1);
        template.setCategoryID(!DbUtil.isNull(cursor, COL_CATEGORY) ? DbUtil.getLong(cursor, COL_CATEGORY) : -1);
        template.setProjectID(!DbUtil.isNull(cursor, COL_PROJECT) ? DbUtil.getLong(cursor, COL_PROJECT) : -1);
        template.setDepartmentID(!DbUtil.isNull(cursor, COL_DEPARTMENT) ? DbUtil.getLong(cursor, COL_DEPARTMENT) : -1);
        template.setLocationID(!DbUtil.isNull(cursor, COL_LOCATION) ? DbUtil.getLong(cursor, COL_LOCATION) : -1);
        template.setComment(!DbUtil.isNull(cursor, COL_COMMENT) ? DbUtil.getString(cursor, COL_COMMENT) : "");

        if (!DbUtil.isNull(cursor, COL_TYPE)) {
            int type = DbUtil.getInt(cursor, COL_TYPE);
            if (type < -1 || type > 1) {
                template.setTrType(Transaction.TRANSACTION_TYPE_EXPENSE);
            } else {
                template.setTrType(type);
            }
        } else {
            template.setTrType(Transaction.TRANSACTION_TYPE_EXPENSE);
        }

        return template;
    }

    @SuppressWarnings("unchecked")
    public List<Template> getAllTemplates() {
        return (List<Template>) getItems(getTableName(), null, null,
                null, COL_NAME, null);
    }

    @Override
    public List<?> getAllModels() {
        return getAllTemplates();
    }
}
