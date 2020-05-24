package com.yoshione.fingen.dao;

import android.content.Context;
import android.database.Cursor;

import com.yoshione.fingen.db.DbUtil;
import com.yoshione.fingen.interfaces.IAbstractModel;
import com.yoshione.fingen.interfaces.IDaoInheritor;
import com.yoshione.fingen.model.Product;

import java.util.List;

public class ProductsDAO extends BaseDAO<Product> implements IDaoInheritor {

    //<editor-fold desc="ref_Products">
    public static final String TABLE = "ref_Products";

    public static final String[] ALL_COLUMNS = joinArrays(COMMON_COLUMNS, new String[]{
            COL_NAME, COL_SEARCH_STRING
    });

    public static final String SQL_CREATE_TABLE = "CREATE TABLE " + TABLE + " ("
            + COMMON_FIELDS +       ", "
            + COL_NAME +            " TEXT NOT NULL DEFAULT '', "
            + COL_SEARCH_STRING +   " TEXT NOT NULL DEFAULT '', "
            + "UNIQUE (" + COL_NAME + ", " + COL_SYNC_DELETED + ") ON CONFLICT ABORT);";
    //</editor-fold>

    private static ProductsDAO sInstance;

    public synchronized static ProductsDAO getInstance(Context context) {
        if (sInstance == null) {
            sInstance = new ProductsDAO(context);
        }
        return sInstance;
    }

    private ProductsDAO(Context context) {
        super(context, TABLE, ALL_COLUMNS);
        super.setDaoInheritor(this);
    }

    @Override
    public IAbstractModel createEmptyModel() {
        return new Product();
    }

    @Override
    public IAbstractModel cursorToModel(Cursor cursor) {
        return cursorToProduct(cursor);
    }

    private Product cursorToProduct(Cursor cursor) {
        Product product = new Product();
        product.setID(DbUtil.getLong(cursor, COL_ID));
        product.setName(DbUtil.getString(cursor, COL_NAME));
        product.setSearchString(DbUtil.getString(cursor, COL_SEARCH_STRING));

        return product;
    }

    @Override
    public void deleteModel(IAbstractModel model, boolean resetTS, Context context) {
        //удаляем из таблиы log_Products все записи с данным товаром
        ProductEntrysDAO productEntrysDAO = ProductEntrysDAO.getInstance(context);
        productEntrysDAO.bulkDeleteModel(productEntrysDAO.getModels(ProductEntrysDAO.COL_PRODUCT_ID + " = " + model.getID()), true);

        super.deleteModel(model, resetTS, context);
    }

    public Product getProductByID(long id) {
        return (Product) getModelById(id);
    }

    @Override
    public List<Product> getAllModels() {
        return getItems(getTableName(), null, null, null, COL_NAME, null);
    }
}
