package com.yoshione.fingen.fts;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.util.Base64;
import android.util.Log;

import com.yoshione.fingen.FGApplication;
import com.yoshione.fingen.FgConst;
import com.yoshione.fingen.dao.ProductEntrysDAO;
import com.yoshione.fingen.dao.ProductsDAO;
import com.yoshione.fingen.fts.models.FtsResponse;
import com.yoshione.fingen.fts.models.Item;
import com.yoshione.fingen.model.Product;
import com.yoshione.fingen.model.ProductEntry;
import com.yoshione.fingen.model.Transaction;

import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Created by slv on 30.01.2018.
 *
 */

public class FtsHelper {
    public static Call<FtsResponse> downloadProductEntryList(final Transaction transaction,
                                                final IDownloadProductsListener downloadProductsListener,
                                                final Context context) {
        String url = String.format("http://proverkacheka.nalog.ru:8888/v1/inns/*/kkts/*/" +
                "fss/%s/" +
                "tickets/%s" +
                "?fiscalSign=%s" +
                "&sendToEmail=no",
                String.valueOf(transaction.getFN()),
                String.valueOf(transaction.getFD()),
                String.valueOf(transaction.getFP()));
        String auth = getAuth(context).replaceAll("\n", "");
        Call<FtsResponse> call = FGApplication.getFtsApi().getData(url,
                "Basic " + auth,
                "okhttp/3.0.1",
                "748036d688ec41c6",
                "Android 8.0",
                "2",
                "1.4.4.1",
                "proverkacheka.nalog.ru:8888",
                "proverkacheka.nalog.ru:8888"
        );
        call.enqueue(new Callback<FtsResponse>() {
            @Override
            public void onResponse(@NonNull Call<FtsResponse> call, @NonNull Response<FtsResponse> response) {
                if (response.body() != null) {
                    List<Item> items = new ArrayList<>();
                    items.addAll(response.body().getDocument().getReceipt().getItems());
                    List<ProductEntry> productEntries = new ArrayList<ProductEntry>();
                    ProductsDAO productsDAO = ProductsDAO.getInstance(context);
                    Product product;
                    ProductEntry productEntry;
                    ProductEntrysDAO productEntrysDAO = ProductEntrysDAO.getInstance(context);
                    for (Item item : items) {
//                        product = new Product(-1, item.getName());
                        try {
                            product = (Product) productsDAO.getModelByName(item.getName());
                        } catch (Exception e) {
                            product = new Product();
                        }
                        if (product.getID() < 0) {
                            try {
                                product.setName(item.getName());
                                product = (Product) productsDAO.createModel(product);
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                        if (product.getID() > 0) {
                            productEntry = new ProductEntry();
                            productEntry.setPrice(new BigDecimal(item.getPrice() / -100d));
                            productEntry.setQuantity(new BigDecimal(item.getQuantity()));
//                            productEntry.setCategoryID(productEntrysDAO.getLastCategoryID(product.getName()));
//                            productEntry.setProjectID(productEntrysDAO.getLastProjectID(product.getID()));
                            productEntry.setTransactionID(transaction.getID());
                            productEntry.setProductID(product.getID());
                            productEntries.add(productEntry);
                        }
                        downloadProductsListener.onDownload(productEntries);
                    }
                }
            }

            @Override
            public void onFailure(@NonNull Call<FtsResponse> call, @NonNull Throwable t) {
                Log.d(getClass().getName(), t.getMessage());
                downloadProductsListener.onFailure(t);
            }
        });
        return call;
    }

    private static String getAuth(Context context) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        String phone = preferences.getString(FgConst.PREF_FTS_LOGIN, "");
        String code = preferences.getString(FgConst.PREF_FTS_PASS, "");
        String auth = String.format("%s:%s", phone, code);

        byte[] data = null;
        try {
            data = auth.getBytes("UTF-8");
        } catch (UnsupportedEncodingException e1) {
            e1.printStackTrace();
        }
        auth = Base64.encodeToString(data, Base64.DEFAULT);
        if (auth == null) {
            auth = "";
        }
        return auth;
    }

    public static boolean isFtsCredentialsAvailiable(Context context) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        return !preferences.getString(FgConst.PREF_FTS_LOGIN, "").isEmpty()
                & !preferences.getString(FgConst.PREF_FTS_PASS, "").isEmpty();
    }
}
