package com.yoshione.fingen.fts;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Base64;
import android.util.Log;

import com.yoshione.fingen.FGApplication;
import com.yoshione.fingen.FgConst;
import com.yoshione.fingen.R;
import com.yoshione.fingen.dao.ProductEntrysDAO;
import com.yoshione.fingen.dao.ProductsDAO;
import com.yoshione.fingen.fts.models.FtsResponse;
import com.yoshione.fingen.fts.models.Item;
import com.yoshione.fingen.model.Product;
import com.yoshione.fingen.model.ProductEntry;
import com.yoshione.fingen.model.Transaction;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.inject.Inject;

import io.reactivex.Single;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;
import retrofit2.Response;

/**
 * Created by slv on 30.01.2018.
 *
 */

public class FtsHelper {

    @Inject
    FtsApi mApi;
    @Inject
    Context mContext;

    public FtsHelper() {
        FGApplication.getAppComponent().inject(this);
    }

    public Disposable downloadProductEntryList(final Transaction transaction,
                                               final IDownloadProductsListener downloadProductsListener) {
        String url = String.format("https://proverkacheka.nalog.ru:9999/v1/inns/*/kkts/*/" +
                "fss/%s/" +
                "tickets/%s" +
                "?fiscalSign=%s" +
                "&sendToEmail=no",
                String.valueOf(transaction.getFN()),
                String.valueOf(transaction.getFD()),
                String.valueOf(transaction.getFP()));
        String auth = getAuth(mContext).replaceAll("\n", "");
        String date = android.text.format.DateFormat.format("yyyy-MM-ddTHH:mm:00", transaction.getDateTime()).toString();
        String sum = Long.toString(Math.round(transaction.getAmount().doubleValue() * -100.0));
        String checkUrl = String.format("https://proverkacheka.nalog.ru:9999/v1/ofds/*/inns/*/" +
                        "fss/%s/" +
                        "operations/1/" +
                        "tickets/%s" +
                        "?fiscalSign=%s" +
                        "&date=%s" +
                        "&sum=%s",
                String.valueOf(transaction.getFN()),
                String.valueOf(transaction.getFD()),
                String.valueOf(transaction.getFP()),
                date,
                sum);

        return Single.zip(mApi.getData(checkUrl,
                "Basic " + auth,
                "okhttp/3.0.1",
                "748036d688ec41c6",
                "Android 8.0",
                "2",
                "1.4.4.1",
                "https://proverkacheka.nalog.ru:9999",
                "https://proverkacheka.nalog.ru:9999"),
                mApi.getData(url,
                        "Basic " + auth,
                        "okhttp/3.0.1",
                        "748036d688ec41c6",
                        "Android 8.0",
                        "2",
                        "1.4.4.1",
                        "https://proverkacheka.nalog.ru:9999",
                        "https://proverkacheka.nalog.ru:9999"),
                mApi.getData(url,
                        "Basic " + auth,
                        "okhttp/3.0.1",
                        "748036d688ec41c6",
                        "Android 8.0",
                        "2",
                        "1.4.4.1",
                        "https://proverkacheka.nalog.ru:9999",
                        "https://proverkacheka.nalog.ru:9999"),
                (u1, u2, u3) -> Arrays.asList(u1, u2, u3))
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(responseList -> {
                    for (Response<FtsResponse> response : responseList) {
                        if (response.code() == 202 || response.code() == 204) {
                          downloadProductsListener.onAccepted();
                            continue;
                        } else if (response.code() == 200 & response.body() != null) {
                            List<Item> items = new ArrayList<>(response.body().getDocument().getReceipt().getItems());
                            List<ProductEntry> productEntries = new ArrayList<>();
                            ProductsDAO productsDAO = ProductsDAO.getInstance(mContext);
                            Product product;
                            ProductEntry productEntry;
                            ProductEntrysDAO productEntrysDAO = ProductEntrysDAO.getInstance(mContext);
                            for (Item item : items) {
//                              product = new Product(-1, item.getName());
                                if (item.getName() == null) {
                                    item.setName(mContext.getString(R.string.ent_unknown_product));
                                }
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
//                                  productEntry.setCategoryID(productEntrysDAO.getLastCategoryID(product.getName()));
//                                  productEntry.setProjectID(productEntrysDAO.getLastProjectID(product.getID()));
                                    productEntry.setTransactionID(transaction.getID());
                                    productEntry.setProductID(product.getID());
                                    productEntries.add(productEntry);
                                }
                            }
                            downloadProductsListener.onDownload(productEntries, response.body().getDocument().getReceipt().getUser());
                            break;
                        } else {
                            try {
                                downloadProductsListener.onFailure(response.errorBody().string(), false);
                            } catch (IOException e) {
                                downloadProductsListener.onFailure("", false);
                                e.printStackTrace();
                            }
                        }
                    }
                }, throwable -> {
                    if (throwable.getMessage() != null) {
                        Log.d(getClass().getName(), throwable.getMessage());
                    }
                    downloadProductsListener.onFailure(throwable.getMessage(), true);
                });
    }

    private String getAuth(Context context) {
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

    public boolean isFtsCredentialsAvailiable(Context context) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        return !preferences.getString(FgConst.PREF_FTS_LOGIN, "").isEmpty()
                & !preferences.getString(FgConst.PREF_FTS_PASS, "").isEmpty();
    }
}
