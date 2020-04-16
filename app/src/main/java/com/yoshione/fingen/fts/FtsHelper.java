package com.yoshione.fingen.fts;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Base64;
import android.util.Log;

import androidx.preference.PreferenceManager;

import com.yoshione.fingen.FGApplication;
import com.yoshione.fingen.FgConst;
import com.yoshione.fingen.fts.models.RestoreRequest;
import com.yoshione.fingen.fts.models.SignUpRequest;
import com.yoshione.fingen.model.Transaction;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

import javax.inject.Inject;

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

    /**
     * Проверка авторизации в ФНС
     *
     * @param phone номер телефона в формате "+79001234567"
     * @param code код из смс KKT.NALOG
     * @param callback интерфейс ответа
     * @return
     */
    public Disposable checkAuth(final String phone, final String code, final IFtsCallback callback) {
        String auth = getAuth(phone, code).replaceAll("\n", "");

        return mApi.checkAuth("Basic " + auth,
                "748036d688ec41c6",
                "Android 8.0")
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(response -> responseAuth(200, response, callback), throwable -> throwableAuth(throwable, callback));
    }

    /**
     *  Регистрация пользователя. Посылает POST запрос в ФНС для регистрации нового пользователя. В качестве логина будет использован
     *  его телефон, пароль выдает ФНС через смс.
     *
     * @param phone номер телефона в формате "+79001234567"
     * @param name имя
     * @param email электронный адрес
     * @param callback интерфейс ответа
     * @return
     */
    public Disposable signUpAuth(final String phone, final String name, final String email, final IFtsCallback callback) {
        SignUpRequest signUpBody = new SignUpRequest();
        signUpBody.setPhone(phone);
        signUpBody.setName(name);
        signUpBody.setEmail(email);

        return mApi.signUp(signUpBody)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(response -> responseAuth(204, response, callback), throwable -> throwableAuth(throwable, callback));
    }

    /**
     * Процедура восстановления пароля. Отправка POST запроса в ФНС
     *
     * @param phone номер телефона в формате "+79001234567"
     * @param callback интерфейс ответа
     * @return
     */
    public Disposable recoveryCode(final String phone, IFtsCallback callback) {
        RestoreRequest restoreBody = new RestoreRequest();
        restoreBody.setPhone(phone);

        return mApi.restoreCode(restoreBody)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(response -> responseAuth(204, response, callback), throwable -> throwableAuth(throwable, callback));
    }

    /**
     * Проверка существование чека в базе ФНС
     *
     * @param transaction информация о транзакции, содержащей данные электронного чека
     * @param callback интерфейс ответа
     * @return
     */
    public Disposable isCheckExists(final Transaction transaction, final IFtsCallback callback) {
        String date = android.text.format.DateFormat.format("yyyy-MM-ddTHH:mm:00", transaction.getDateTime()).toString();
        String sum = Long.toString(Math.round(transaction.getAmount().doubleValue() * -100.0));
        String url = String.format("/v1/ofds/*/inns/*/fss/%s/operations/1/tickets/%s?fiscalSign=%s&date=%s&sum=%s",
                String.valueOf(transaction.getFN()),
                String.valueOf(transaction.getFD()),
                String.valueOf(transaction.getFP()),
                date,
                sum);

        return mApi.checkExists(url)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(response -> responseAuth(204, response, callback), throwable -> throwableAuth(throwable, callback));
    }

    /**
     * Получение чека из ФНС
     *
     * @param transaction информация о транзакции, содержащей данные электронного чека
     * @param callback интерфейс ответа
     * @return
     */
    public Disposable getCheck(final Transaction transaction, final IFtsCallback callback) {
        String url = String.format("/v1/inns/*/kkts/*/fss/%s/tickets/%s?fiscalSign=%s&sendToEmail=no",
                String.valueOf(transaction.getFN()),
                String.valueOf(transaction.getFD()),
                String.valueOf(transaction.getFP()));
        String auth = getAuth(mContext).replaceAll("\n", "");

        return mApi.getData(url,
                "Basic " + auth,
                "748036d688ec41c6",
                "Android 8.0")
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

    private String getAuth(String phone, String code) {
        String auth = String.format("%s:%s", phone, code);

        byte[] data = auth.getBytes(StandardCharsets.UTF_8);
        auth = Base64.encodeToString(data, Base64.DEFAULT);
        if (auth == null) {
            auth = "";
        }
        return auth;
    }

    private String getAuth(Context context) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        String phone = preferences.getString(FgConst.PREF_FTS_LOGIN, "");
        String code = preferences.getString(FgConst.PREF_FTS_PASS, "");
        return getAuth(phone, code);
    }

    public boolean isFtsCredentialsAvailable(Context context) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        return preferences.getBoolean(FgConst.PREF_FTS_ENABLED, true)
                & !preferences.getString(FgConst.PREF_FTS_LOGIN, "").isEmpty()
                & !preferences.getString(FgConst.PREF_FTS_PASS, "").isEmpty();
    }
}
