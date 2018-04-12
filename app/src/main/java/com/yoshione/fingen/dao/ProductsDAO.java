package com.yoshione.fingen.dao;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.util.Pair;


import com.yoshione.fingen.DBHelper;
import com.yoshione.fingen.interfaces.IAbstractModel;
import com.yoshione.fingen.interfaces.IDaoInheritor;
import com.yoshione.fingen.model.Product;

import java.util.HashMap;
import java.util.List;

/**
 * Created by slv on 30.01.2018.
 * 
 */
public class ProductsDAO extends BaseDAO implements AbstractDAO, IDaoInheritor {
    public static final String TAG = "ProductDAO";
    private static ProductsDAO sInstance;

    public synchronized static ProductsDAO getInstance(Context context) {
        if (sInstance == null) {
            sInstance = new ProductsDAO(context);
        }
        return sInstance;
    }

    private ProductsDAO(Context context) {
        super(context, DBHelper.T_REF_PRODUCTS, IAbstractModel.MODEL_TYPE_PRODUCT , DBHelper.T_REF_PRODUCTS_ALL_COLUMNS);
        super.setDaoInheritor(this);
    }

    @Override
    public IAbstractModel cursorToModel(Cursor cursor) {
        return cursorToProduct(cursor);
    }

    private Product cursorToProduct(Cursor cursor) {
        Product product = new Product();
        product.setID(cursor.getLong(mColumnIndexes.get(DBHelper.C_ID)));
        product.setName(cursor.getString(mColumnIndexes.get(DBHelper.C_REF_PRODUCTS_NAME)));

        product = (Product) DBHelper.getSyncDataFromCursor(product, cursor, mColumnIndexes);

        return product;
    }

    @Override
    public void deleteModel(IAbstractModel model, boolean resetTS, Context context) {
        //удаляем из таблиы log_Products все записи с данным товаром
        ProductEntrysDAO productEntrysDAO = ProductEntrysDAO.getInstance(context);
        productEntrysDAO.bulkDeleteModel(productEntrysDAO.getModels(DBHelper.C_LOG_PRODUCTS_PRODUCTID
                        + " = " + String.valueOf(model.getID())), true);

        super.deleteModel(model, resetTS, context);
    }

    @SuppressWarnings("unchecked")
    public List<Product> getAllProducts() throws Exception {
        return (List<Product>) getItems(getTableName(), null, null, null, DBHelper.C_REF_PRODUCTS_NAME, null);
    }

    public Product getProductByID(long id) {
        return (Product) getModelById(id);
    }

    @Override
    public List<?> getAllModels() throws Exception {
        return getAllProducts();
    }
}
