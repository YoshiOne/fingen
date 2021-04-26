package com.yoshione.fingen.utils;

import android.content.Context;
import android.util.Log;

import com.yoshione.fingen.R;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.http.GET;
import retrofit2.http.Query;

public class ModUtils {

    private static final String TAG = "ModUtils";
    private static String BASE_URL = null;

    public static int LAST_VERSION_CHECKED = -1;

    public static void checkVersion(Context context, int version, CheckVersionCallback callback) {
        if (BASE_URL == null)
            BASE_URL = context.getString(R.string.urlWebAPI);
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        FingenWebAPI fingenWebAPI = retrofit.create(FingenWebAPI.class);
        fingenWebAPI.checkVersion(version).enqueue(new Callback<CheckVersionResponse>() {
            @Override
            public void onResponse(Call<CheckVersionResponse> call, Response<CheckVersionResponse> response) {
                Log.d(TAG, response.toString());
                if (response.isSuccessful()) {
                    LAST_VERSION_CHECKED = version + 1;
                    if (callback != null) {
                        callback.callback(response.body());
                    }
                }
            }

            @Override
            public void onFailure(Call<CheckVersionResponse> call, Throwable t) {
                Log.e(TAG, t.toString());
            }
        });
    }

    public interface CheckVersionCallback {
        void callback(CheckVersionResponse response);
    }

    public class CheckVersionResponse {
        boolean exists;
        public boolean getExists() {
            return exists;
        }
        public void setExists(boolean exists) {
            this.exists = exists;
        }
    }

    public interface FingenWebAPI {
        @GET("check")
        Call<CheckVersionResponse> checkVersion(@Query("version") Integer version);
    }

}
