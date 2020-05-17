package com.yoshione.fingen.dao;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;

import androidx.annotation.NonNull;

import com.yoshione.fingen.db.DbUtil;
import com.yoshione.fingen.interfaces.IAbstractModel;
import com.yoshione.fingen.interfaces.IDaoInheritor;
import com.yoshione.fingen.model.ProductEntry;
import com.yoshione.fingen.utils.Translit;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

import io.requery.android.database.sqlite.SQLiteDatabase;

public class ProductEntrysDAO extends BaseDAO implements AbstractDAO, IDaoInheritor {

    //<editor-fold desc="log_Products">
    public static final String TABLE = " log_Products";

    public static final String COL_TRANSACTION_ID = "TransactionID";
    public static final String COL_PRODUCT_ID = "ProductID";
    public static final String COL_CATEGORY_ID = "CategoryID";
    public static final String COL_PROJECT_ID = "ProjectID";
    public static final String COL_DEPARTMENT_ID = "DepartmentID";
    public static final String COL_PRICE = "Price";
    public static final String COL_QUANTITY = "Quantity";

    public static final String[] ALL_COLUMNS = joinArrays(COMMON_COLUMNS, new String[]{
            COL_TRANSACTION_ID, COL_PRODUCT_ID, COL_CATEGORY_ID, COL_PROJECT_ID,
            COL_PRICE, COL_QUANTITY, COL_DEPARTMENT_ID
    });

    public static final String SQL_CREATE_TABLE = "CREATE TABLE " + TABLE + "("
            + COMMON_FIELDS      + ", "
            + COL_TRANSACTION_ID + " INTEGER REFERENCES [" + TransactionsDAO.TABLE + "]([" + COL_ID + "]) ON DELETE SET NULL ON UPDATE CASCADE, "
            + COL_PRODUCT_ID     + " INTEGER REFERENCES [" + ProductsDAO.TABLE   + "]([" + COL_ID + "]) ON DELETE SET NULL ON UPDATE CASCADE, "
            + COL_CATEGORY_ID    + " INTEGER DEFAULT -1 REFERENCES [" + CategoriesDAO.TABLE + "]([" + COL_ID + "]) ON DELETE SET NULL ON UPDATE CASCADE, "
            + COL_PROJECT_ID     + " INTEGER DEFAULT -1 REFERENCES [" + ProjectsDAO.TABLE  + "]([" + COL_ID + "]) ON DELETE SET NULL ON UPDATE CASCADE, "
            + COL_DEPARTMENT_ID  + " INTEGER DEFAULT -1 REFERENCES [" + DepartmentsDAO.TABLE  + "]([" + COL_ID + "]) ON DELETE SET NULL ON UPDATE CASCADE, "
            + COL_PRICE          + " REAL NOT NULL DEFAULT 0, "
            + COL_QUANTITY       + " REAL NOT NULL DEFAULT 1 CHECK (Quantity >= 0));";

    public static final String SQL_CREATE_INDEX = "CREATE INDEX [idx_Products] ON [log_Products] ([Deleted], [TransactionID], [ProductID]);";
    //</editor-fold>

    private static ProductEntrysDAO sInstance;

    public synchronized static ProductEntrysDAO getInstance(Context context) {
        if (sInstance == null) {
            sInstance = new ProductEntrysDAO(context);
        }
        return sInstance;
    }

    private ProductEntrysDAO(Context context) {
        super(context, TABLE, ALL_COLUMNS);
        super.setDaoInheritor(this);
    }

    @Override
    public IAbstractModel createEmptyModel() {
        return new ProductEntry();
    }

    @Override
    public IAbstractModel cursorToModel(Cursor cursor) {
        return cursorToProductEntry(cursor);
    }

    private ProductEntry cursorToProductEntry(Cursor cursor) {
        ProductEntry productEntry = new ProductEntry();

        productEntry.setID(DbUtil.getLong(cursor, COL_ID));
        productEntry.setProductID(DbUtil.getLong(cursor, COL_PRODUCT_ID));
        productEntry.setTransactionID(DbUtil.getLong(cursor, COL_TRANSACTION_ID));
        productEntry.setCategoryID(DbUtil.getLong(cursor, COL_CATEGORY_ID));
        productEntry.setProjectID(DbUtil.getLong(cursor, COL_PROJECT_ID));
        productEntry.setDepartmentID(DbUtil.getLong(cursor, COL_DEPARTMENT_ID));
        productEntry.setPrice(new BigDecimal(DbUtil.getDouble(cursor, COL_PRICE)).setScale(2, RoundingMode.HALF_UP));
        productEntry.setQuantity(new BigDecimal(DbUtil.getDouble(cursor, COL_QUANTITY)));

        return productEntry;
    }

    @Override
    public void deleteModel(IAbstractModel model, boolean resetTS, Context context) {
        //удаляем из таблиы log_Products
        //ToDo Обработать удаление!!!

        super.deleteModel(model, resetTS, context);
    }

    public ProductEntry getProductEntryByID(long id) {
        return (ProductEntry) getModelById(id);
    }

    List<IAbstractModel> getAllEntriesOfTransaction(long transactionID, boolean getDefaultEntry) {
        if (!getDefaultEntry) {
            return getModels(COL_TRANSACTION_ID + " = " + transactionID +
                    " AND " + COL_PRODUCT_ID + " != 0");
        } else {
            return getModels(COL_TRANSACTION_ID + " = " + transactionID);
        }
    }

    public long getLastCategoryID(String productName) {
        long lastCategoryID = -1;
        String sql = "SELECT CategoryID\n" +
                "FROM log_Products as p \n" +
                "LEFT OUTER JOIN log_Transactions t ON CategoryID = Category AND t.[_id] = TransactionID\n" +
                "INNER JOIN ref_Products rp ON p.[_id] = ProductID\n" +
                "WHERE rp.SearchString = '"+ Translit.toTranslit(productName).toLowerCase().replaceAll("'", "''") + "'\n" +
                "ORDER BY t.DateTime DESC\n" +
                "LIMIT 1";
        Cursor cursor;
        try {
            cursor = mDatabase.rawQuery(sql, null);
        } catch (Exception e) {
            return lastCategoryID;
        }
        if (cursor != null) {
            try {
                if (cursor.moveToFirst()) {
                    while (!cursor.isAfterLast()) {
                        lastCategoryID = cursor.getLong(0);
                        cursor.moveToNext();
                    }
                }
            } finally {
                cursor.close();
            }
        }
        return lastCategoryID;
    }

    public long getLastProjectID(long productEntryID) {
        long lastProjectID = -1;
        String sql = "SELECT ProjectID\n" +
                "FROM log_Products as p \n" +
                "LEFT OUTER JOIN log_Transactions t ON ProjectID = Project AND t.[_id] = TransactionID\n" +
                "WHERE p.[_id] = "+ productEntryID + "\n" +
                "ORDER BY t.DateTime DESC\n" +
                "LIMIT 1";
        Cursor cursor;
        try {
            cursor = mDatabase.rawQuery(sql, null);
        } catch (Exception e) {
            return lastProjectID;
        }
        if (cursor != null) {
            try {
                if (cursor.moveToFirst()) {
                    while (!cursor.isAfterLast()) {
                        lastProjectID = cursor.getLong(0);
                        cursor.moveToNext();
                    }
                }
            } finally {
                cursor.close();
            }
        }
        return lastProjectID;
    }

    /**
     *
     * @param db DB connection
     * @param cursor rawQuery(SELECT _id, Amount FROM log_Transactions)
     */
    public static void updateLogProducts(@NonNull SQLiteDatabase db, @NonNull Cursor cursor) {
        ContentValues cv = new ContentValues();
        if (cursor.moveToFirst()) {
            while (!cursor.isAfterLast()) {
                cv.clear();
                cv.put(ProductEntrysDAO.COL_SYNC_FBID, "");
                cv.put(ProductEntrysDAO.COL_SYNC_TS, -1);
                cv.put(ProductEntrysDAO.COL_SYNC_DELETED, 0);
                cv.put(ProductEntrysDAO.COL_SYNC_DIRTY, 0);
                cv.put(ProductEntrysDAO.COL_SYNC_LAST_EDITED, "");
                cv.put(ProductEntrysDAO.COL_TRANSACTION_ID, cursor.getLong(0));
                cv.put(ProductEntrysDAO.COL_PRODUCT_ID, 0);
                cv.put(ProductEntrysDAO.COL_CATEGORY_ID, -1);
                cv.put(ProductEntrysDAO.COL_PROJECT_ID, -1);
                cv.put(ProductEntrysDAO.COL_DEPARTMENT_ID, -1);
                cv.put(ProductEntrysDAO.COL_PRICE, cursor.getDouble(1));
                cv.put(ProductEntrysDAO.COL_QUANTITY, 1);
                db.insert(ProductEntrysDAO.TABLE, null, cv);
                cursor.moveToNext();
            }
        }
    }
}
