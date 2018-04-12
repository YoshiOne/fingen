package com.yoshione.fingen.dao;

import android.content.Context;
import android.database.Cursor;
import android.util.Pair;


import com.yoshione.fingen.DBHelper;
import com.yoshione.fingen.interfaces.IAbstractModel;
import com.yoshione.fingen.interfaces.IDaoInheritor;
import com.yoshione.fingen.model.Product;
import com.yoshione.fingen.model.ProductEntry;
import com.yoshione.fingen.utils.Translit;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Created by slv on 30.01.2018.
 * 
 */
public class ProductEntrysDAO extends BaseDAO implements AbstractDAO, IDaoInheritor {
    public static final String TAG = "ProductDAO";
    private static ProductEntrysDAO sInstance;

    public synchronized static ProductEntrysDAO getInstance(Context context) {
        if (sInstance == null) {
            sInstance = new ProductEntrysDAO(context);
        }
        return sInstance;
    }

    private ProductEntrysDAO(Context context) {
        super(context, DBHelper.T_LOG_PRODUCTS, IAbstractModel.MODEL_TYPE_PRODUCT_ENTRY, DBHelper.T_LOG_PRODUCTS_ALL_COLUMNS);
        super.setDaoInheritor(this);
    }

    @Override
    public IAbstractModel cursorToModel(Cursor cursor) {
        return cursorToProductEntry(cursor);
    }

    private ProductEntry cursorToProductEntry(Cursor cursor) {
        ProductEntry productEntry = new ProductEntry();
        productEntry.setID(cursor.getLong(mColumnIndexes.get(DBHelper.C_ID)));
        productEntry.setProductID(cursor.getLong(mColumnIndexes.get(DBHelper.C_LOG_PRODUCTS_PRODUCTID)));
        productEntry.setTransactionID(cursor.getLong(mColumnIndexes.get(DBHelper.C_LOG_PRODUCTS_TRANSACTIONID)));
        productEntry.setCategoryID(cursor.getLong(mColumnIndexes.get(DBHelper.C_LOG_PRODUCTS_CATEGORY_ID)));
        productEntry.setProjectID(cursor.getLong(mColumnIndexes.get(DBHelper.C_LOG_PRODUCTS_PROJECT_ID)));
        productEntry.setPrice(new BigDecimal(cursor.getDouble(mColumnIndexes.get(DBHelper.C_LOG_PRODUCTS_PRICE))).setScale(2, RoundingMode.HALF_EVEN));
        productEntry.setQuantity(new BigDecimal(cursor.getDouble(mColumnIndexes.get(DBHelper.C_LOG_PRODUCTS_QUANTITY))));

        productEntry = (ProductEntry) DBHelper.getSyncDataFromCursor(productEntry, cursor, mColumnIndexes);

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
            return getModels(DBHelper.C_LOG_PRODUCTS_TRANSACTIONID + " = " + String.valueOf(transactionID) +
                    " AND " + DBHelper.C_LOG_PRODUCTS_PRODUCTID + " != 0");
        } else {
            return getModels(DBHelper.C_LOG_PRODUCTS_TRANSACTIONID + " = " + String.valueOf(transactionID));
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
                "WHERE p.[_id] = "+ String.valueOf(productEntryID) + "\n" +
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
}
