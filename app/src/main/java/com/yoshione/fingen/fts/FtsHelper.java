package com.yoshione.fingen.fts;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.Signature;
import android.text.format.DateFormat;
import android.util.Base64;
import android.util.Log;

import androidx.preference.PreferenceManager;

import com.yoshione.fingen.FGApplication;
import com.yoshione.fingen.FgConst;
import com.yoshione.fingen.fts.api.LoginApi;
import com.yoshione.fingen.fts.api.TicketApi;
import com.yoshione.fingen.fts.models.login.PhoneLoginRequest;
import com.yoshione.fingen.fts.models.tickets.TicketQrCodeRequest;
import com.yoshione.fingen.model.Transaction;

import java.io.IOException;
import java.security.MessageDigest;

import javax.inject.Inject;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;
import retrofit2.Response;


public class FtsHelper {

    @Inject
    TicketApi mApi;
    @Inject
    LoginApi loginApi;
    @Inject
    Context mContext;

    public FtsHelper() {
        FGApplication.getAppComponent().inject(this);
    }

    /**
     * Проверка авторизации в ФНС
     *
     * @param phone номер телефона в формате "+79001234567"
     * @param code код из смс KKT.NALOG
     * @param callback интерфейс ответа
     * @return
     */
    public Disposable checkAuth(final String phone, final String code, final IFtsCallback callback) {
        String hashKey = getHashKey(mContext);
        if (hashKey.isEmpty()) {
            callback.onFailure("system error: FNS application is not found", -1);
            return null;
        }
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(mContext);
        preferences.edit().putString(FgConst.PREF_FTS_CLIENT_SECRET, hashKey).apply();
        return loginApi.loginUser(new PhoneLoginRequest(hashKey, phone, code))
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(response -> responseAuth(200, response, callback), throwable -> throwableAuth(throwable, callback));
    }

    /**
     * Проверка существование чека в базе ФНС
     *
     * @param transaction информация о транзакции, содержащей данные электронного чека
     * @param callback интерфейс ответа
     * @return
     */
    public Disposable isCheckExists(final Transaction transaction, final IFtsCallback callback) {
        String date = DateFormat.format("yyyy-MM-ddTHH:mm:00", transaction.getDateTime()).toString();
        Long sum = Math.round(transaction.getAmount().doubleValue() * -100.0);

        return mApi.validateTicket(String.valueOf(transaction.getFN()), 1, String.valueOf(transaction.getFD()), String.valueOf(transaction.getFP()), date, sum)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(response -> responseAuth(204, response, callback), throwable -> throwableAuth(throwable, callback));
    }

    /**
     * Добавление чека в ФНС
     *
     * @param transaction информация о транзакции, содержащей данные электронного чека
     * @param callback интерфейс ответа
     * @return
     */
    public Disposable addCheck(final Transaction transaction, final IFtsCallback callback) {
        return mApi.addTicketQR(new TicketQrCodeRequest(transaction.getQRCode()))
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(response -> responseAuth(200, response, callback), throwable -> throwableAuth(throwable, callback));
    }

    /**
     * Получение чека из ФНС
     *
     * @param ticketId идентификатор чека в ФНС
     * @param callback интерфейс ответа
     * @return
     */
    public Disposable getCheck(final String ticketId, final IFtsCallback callback) {
        return mApi.getTicket(ticketId)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(response -> responseAuth(200, response, callback), throwable -> throwableAuth(throwable, callback));
    }

    private <T> void responseAuth(final int acceptCode, final Response<T> response, final IFtsCallback callback) {
        if (response.code() == acceptCode) {
            callback.onAccepted(response.body());
        } else {
            try {
                String error;
                if (response.errorBody() != null)
                    error = "error: " + response.errorBody().string();
                else
                    error = "response error, url: " + response.raw().request().url() +
                            ", code: " + response.code() + ", content: [" + response.body() +"]";

                callback.onFailure(error, response.code());
            } catch (IOException e) {
                callback.onFailure("system error: " + e.getMessage(), -1);
                e.printStackTrace();
            }
        }
    }

    private void throwableAuth(final Throwable throwable, final IFtsCallback callback) {
        if (throwable.getMessage() != null) {
            Log.d(getClass().getName(), throwable.getMessage());
        }
        callback.onFailure(throwable.getMessage(), -1);
    }

    private String getHashKey(Context pContext) {
        try {
            PackageInfo info = pContext.getPackageManager().getPackageInfo("ru.fns.billchecker", PackageManager.GET_SIGNATURES);
            for (Signature signature : info.signatures) {
                MessageDigest md = MessageDigest.getInstance("SHA");
                md.update(signature.toByteArray());
                String hashKey = Base64.encodeToString(md.digest(), Base64.NO_WRAP);
                Log.d("HashKey", "printHashKey() Hash Key: " + hashKey);
                return hashKey;
            }
        } catch (Exception e) {
            Log.e("HashKey", "printHashKey()", e);
        }
        return "";
    }

    public static boolean isFtsCredentialsAvailable(SharedPreferences preferences) {
        return preferences.getBoolean(FgConst.PREF_FTS_ENABLED, true)
                & !preferences.getString(FgConst.PREF_FTS_SESSION_ID, "").isEmpty();
    }

    public static void clearFtsCredentials(SharedPreferences preferences) {
        preferences.edit()
                .remove(FgConst.PREF_FTS_LOGIN)
                .remove(FgConst.PREF_FTS_PASS)
                .remove(FgConst.PREF_FTS_NAME)
                .remove(FgConst.PREF_FTS_EMAIL)
                .remove(FgConst.PREF_FTS_CLIENT_SECRET)
                .remove(FgConst.PREF_FTS_REFRESH_TOKEN)
                .remove(FgConst.PREF_FTS_SESSION_ID)
                .apply();
    }
}
