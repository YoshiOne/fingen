package com.yoshione.fingen.fts;

import android.annotation.SuppressLint;
import android.content.Context;

import com.yoshione.fingen.R;
import com.yoshione.fingen.dao.ProductsDAO;
import com.yoshione.fingen.model.Product;
import com.yoshione.fingen.model.ProductEntry;
import com.yoshione.fingen.model.Transaction;

import org.json.JSONArray;
import org.json.JSONObject;

import java.math.BigDecimal;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class FTSJsonToTransaction {
    private JSONObject jsonObject;
    private String jsonAsString;
    private Transaction transaction;
    private Context mContext;

    public FTSJsonToTransaction(Context context, Transaction transaction, String jsonAsString) throws Exception
    {
        this.jsonAsString = jsonAsString;
        this.transaction = transaction;
        this.mContext = context;
        jsonObject = new JSONObject(this.jsonAsString);
    }

    public Transaction generateTransaction(boolean loadProducts) throws Exception
    {
            transaction.setFD(jsonObject.getInt("fiscalDocumentNumber"));
            String ss = jsonObject.getString("fiscalDriveNumber");
            long g = Long.parseLong(ss);
            transaction.setFN(g);
            transaction.setFP(jsonObject.getLong("fiscalSign"));
            transaction.setAmount(new BigDecimal(jsonObject.getInt("totalSum") / -100d), Transaction.TRANSACTION_TYPE_EXPENSE);

            if (jsonObject.has("localDateTime")) {
                @SuppressLint("SimpleDateFormat") DateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm");
                Date date = new Date();
                try {
                    date = df.parse(jsonObject.getString("localDateTime"));
                } catch (ParseException e) {
                    e.printStackTrace();
                }
                transaction.setDateTime(date);
            }
            if (loadProducts)
                fillProduct();

            return transaction;
    }

    public String getPayerName() throws Exception
    {
        return jsonObject.getString("user");
    }

    private void fillProduct() throws Exception
    {
        transaction.getProductEntries().clear();
        List<ProductEntry> productEntries = new ArrayList<>();
        ProductsDAO productsDAO = ProductsDAO.getInstance(mContext);
        JSONArray jsonArray = jsonObject.getJSONArray("items");
        for (int i = 0; i < jsonArray.length(); i++)
        {
            ProductEntry productEntry;
            Product product;
            JSONObject item = jsonArray.getJSONObject(i);
            if (item.getString("name") == null) {
                item.put("name", mContext.getString(R.string.ent_unknown_product));
            }
            try {
                product = (Product) productsDAO.getModelByName(item.getString("name"));
            } catch (Exception e) {
                product = new Product();
            }
            if (product.getID() < 0) {
                try {
                    product.setName(item.getString("name"));
                    product = (Product) productsDAO.createModel(product);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            if (product.getID() > 0) {
                productEntry = new ProductEntry();
                productEntry.setPrice(new BigDecimal(item.getInt("price") / -100d));
                productEntry.setQuantity(new BigDecimal(item.getDouble("quantity")));
                productEntry.setTransactionID(transaction.getID());
                productEntry.setProductID(product.getID());
                productEntries.add(productEntry);
            }
        }

        transaction.getProductEntries().addAll(productEntries);
    }

}
