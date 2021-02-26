package com.yoshione.fingen.fts;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;

import com.google.gson.JsonObject;
import com.yoshione.fingen.FGApplication;
import com.yoshione.fingen.FgConst;
import com.yoshione.fingen.R;
import com.yoshione.fingen.dao.ProductEntrysDAO;
import com.yoshione.fingen.dao.ProductsDAO;
import com.yoshione.fingen.fts.models.FtsResponse;
import com.yoshione.fingen.fts.models.Item;
import com.yoshione.fingen.fts.models.ReceiptStatus;
import com.yoshione.fingen.model.Product;
import com.yoshione.fingen.model.ProductEntry;
import com.yoshione.fingen.model.Transaction;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
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

    String clientKey = "mnALjKobrqT/sC9um4wXlamXnOo=";
    String host = "https://irkkt-mobile.nalog.ru:8888";
    String clientVer = "2.9.0";
    String deviceID = "noFirebaseToken";
    String deviceOS = "Android";
    String userAgent = "okhttp/4.2.2";

    public FtsHelper() {
        FGApplication.getAppComponent().inject(this);
    }

    public Disposable downloadProductEntryList(final Transaction transaction,
                                               final IDownloadProductsListener downloadProductsListener) {
        String checkUrl = host + "/v2/ticket";
        String date = android.text.format.DateFormat.format("yyyyMMddTHHmm", transaction.getDateTime()).toString();
        String sum = String.format("%.2f", transaction.getAmount().doubleValue() * -1).replaceAll(",", ".");
        String qr = "t=" + date
                + "&s=" + sum
                + "&fn=" + transaction.getFN()
                + "&i=" + transaction.getFD()
                + "&fp=" + transaction.getFP()
                + "&n=1";

        JsonObject bodyRequest = new JsonObject();
        bodyRequest.addProperty("qr", qr);

        return mApi.getReceiptStatus(checkUrl,
                bodyRequest, getSession(),
                clientVer,
                deviceID,
                deviceOS,
                userAgent)
                .subscribeOn(Schedulers.io())
                .flatMap(response ->
                {
                    if (response.code() == 401) {
                        return mApi.getReceiptStatus(checkUrl,
                                //todo: need replace blocking get!!
                                bodyRequest, refreshSession().blockingGet(),
                                clientVer,
                                deviceID,
                                deviceOS,
                                userAgent);
                    } else {
                        return Single.just(response);
                    }
                })
                .flatMap(response ->
                        mApi.getData(host + "/v2/tickets/" + response.body().getId(),
                                getSession(),
                                clientVer,
                                deviceID,
                                deviceOS,
                                userAgent)
                )
                .flatMap(response ->
                {
                    if (response.body().hasTicket()) {
                        return Single.just(response);
                    }
                    return Single.error(new Throwable("There is no data in the ticket"));
                })
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(response -> fillTransaction(response, downloadProductsListener, transaction),
                        throwable -> {
                            if (throwable.getMessage() != null) {
                                Log.d(getClass().getName(), throwable.getMessage());
                            }
                            downloadProductsListener.onFailure(throwable.getMessage(), true);
                        });
    }

    private void fillTransaction(Response<FtsResponse> response,
                                 IDownloadProductsListener downloadProductsListener,
                                 Transaction transaction) {
        if (response.code() == 202 || response.code() == 204) {
            downloadProductsListener.onAccepted();
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
            String payee = response.body().getDocument().getReceipt().getUser();
            if (payee == null) {
                payee = response.body().getDocument().getReceipt().getUserInn();
            }
            downloadProductsListener.onDownload(productEntries, payee);
        } else {
            try {
                String error;
                if(response.errorBody() != null) {
                    error = "error: " + response.errorBody().string();
                } else {
                    error = "response error, code: " + response.code() + ", content: [" + response.body() +"]";
                }

                downloadProductsListener.onFailure(error, false);
            } catch (IOException e) {
                downloadProductsListener.onFailure("system error: " + e.getMessage(), false);
                e.printStackTrace();
            }
        }
    }


    public Single<Boolean> registration(String phone, String smsCode) {
        String registrationUrl = host + "/v2/auth/phone/verify";
        JsonObject bodyRequest = new JsonObject();
        bodyRequest.addProperty("client_secret", clientKey);
        bodyRequest.addProperty("code", smsCode);
        bodyRequest.addProperty("phone", phone);

        return mApi.getSessionAndToken(registrationUrl,
                bodyRequest,
                clientVer,
                deviceID,
                deviceOS,
                userAgent)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .flatMap(res ->
                {
                    if (res.code() == 200) {
                        updateAuthInfo(res.body().getSession(), res.body().getToken());
                        return Single.just(true);
                    }
                    return Single.error(new Throwable("Error: code=" + res.code() + " message=" + res.message()));
                });
    }

    private Single<String> refreshSession() {
        String refreshSessionUrl = host + "/v2/mobile/users/refresh";

        JsonObject bodyRequest = new JsonObject();
        bodyRequest.addProperty("client_secret", clientKey);
        bodyRequest.addProperty("refresh_token", getToken());

        return mApi.getSessionAndToken(refreshSessionUrl,
                bodyRequest,
                clientVer,
                deviceID,
                deviceOS,
                userAgent)
                .subscribeOn(Schedulers.io())
                .map(res ->
                {
                    // may return http code 498 - with empty body (when refresh token is out of date)
                    String session = res.body().getSession();
                    String token = res.body().getToken();
                    if (res.code() == 200) {
                        updateAuthInfo(session, token);
                    }
                    return session;
                });
    }

    public boolean isFtsCredentialsAvailiable(Context context) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        return !preferences.getString(FgConst.PREF_FTS_LOGIN, "").isEmpty()
                & !preferences.getString(FgConst.PREF_FTS_PASS, "").isEmpty();
    }

    private void updateAuthInfo(String sess, String token) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(mContext);
        preferences.edit()
                .putString(FgConst.PREF_FTS_SESSION, sess)
                .putString(FgConst.PREF_FTS_TOKEN, token)
                .apply();
    }

    private String getSession() {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(mContext);
        return preferences.getString(FgConst.PREF_FTS_SESSION, "");
    }

    private String getToken() {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(mContext);
        return preferences.getString(FgConst.PREF_FTS_TOKEN, "");
    }
}
